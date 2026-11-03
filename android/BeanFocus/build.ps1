[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$AndroidJar = $env:ANDROID_JAR,
    [string]$BuildTools = $env:BUILD_TOOLS,
    [switch]$Sign
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot

function Require-File([string]$Path, [string]$Description) {
    if (-not $Path -or -not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Description was not found. Set the corresponding script parameter or environment variable."
    }
    return (Resolve-Path -LiteralPath $Path).Path
}
function Invoke-Checked([string]$Program, [string[]]$Arguments) {
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$([IO.Path]::GetFileName($Program)) failed with exit code $LASTEXITCODE." }
}

if (-not $JdkHome) { throw 'Set -JdkHome or JAVA_HOME to a JDK (17 or newer).' }
if (-not $BuildTools) { throw 'Set -BuildTools or BUILD_TOOLS to an Android SDK build-tools directory.' }
$java = Require-File (Join-Path $JdkHome 'bin/java.exe') 'java.exe'
$javac = Require-File (Join-Path $JdkHome 'bin/javac.exe') 'javac.exe'
$jar = Require-File (Join-Path $JdkHome 'bin/jar.exe') 'jar.exe'
$AndroidJar = Require-File $AndroidJar 'Android platform android.jar'
$aapt2 = Require-File (Join-Path $BuildTools 'aapt2.exe') 'aapt2.exe'
$zipalign = Require-File (Join-Path $BuildTools 'zipalign.exe') 'zipalign.exe'
$d8 = Require-File (Join-Path $BuildTools 'lib/d8.jar') 'd8.jar'
$lambdaStubs = Require-File (Join-Path $BuildTools 'core-lambda-stubs.jar') 'core-lambda-stubs.jar'

if ($Sign) {
    foreach ($name in @('SIGNING_KEYSTORE', 'SIGNING_KEY_ALIAS', 'SIGNING_STORE_PASSWORD', 'SIGNING_KEY_PASSWORD')) {
        if (-not [Environment]::GetEnvironmentVariable($name)) { throw "Set $name outside source control before using -Sign." }
    }
    $keystore = Require-File $env:SIGNING_KEYSTORE 'Existing signing keystore'
    $apksigner = Require-File (Join-Path $BuildTools 'lib/apksigner.jar') 'apksigner.jar'
}

# Every run gets an empty work directory. Never recursively delete user paths.
$outputRoot = Join-Path $projectRoot 'build/windows'
$work = Join-Path $outputRoot ('run-' + [Guid]::NewGuid().ToString('N'))
$gen = Join-Path $work 'gen'
$classes = Join-Path $work 'classes'
$dex = Join-Path $work 'dex'
New-Item -ItemType Directory -Path $gen, $classes, $dex -Force | Out-Null

Push-Location $projectRoot
try {
    Invoke-Checked $aapt2 @('compile', '--dir', 'res', '-o', (Join-Path $work 'resources.zip'))
    Invoke-Checked $aapt2 @('link', '-o', (Join-Path $work 'resources.apk'), '-I', $AndroidJar,
        '--manifest', 'AndroidManifest.xml', '--java', $gen, '-A', 'assets', (Join-Path $work 'resources.zip'))

    $sources = @(Get-ChildItem -LiteralPath 'src', $gen -Filter '*.java' -File -Recurse | Sort-Object FullName | ForEach-Object { $_.FullName })
    # Resolve core Java APIs against Android, not the host JDK's newer library.
    Invoke-Checked $javac (@('-encoding', 'UTF-8', '-source', '8', '-target', '8', '-Xlint:-options',
        '-bootclasspath', "$AndroidJar;$lambdaStubs", '-d', $classes) + $sources)
    $classesJar = Join-Path $work 'classes.jar'
    Invoke-Checked $jar @('--create', '--file', $classesJar, '-C', $classes, '.')
    Invoke-Checked $java @('-cp', $d8, 'com.android.tools.r8.D8', '--release', '--min-api', '26',
        '--lib', $AndroidJar, '--output', $dex, $classesJar)

    $unsigned = Join-Path $work 'unsigned.apk'
    Copy-Item -LiteralPath (Join-Path $work 'resources.apk') -Destination $unsigned
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::Open($unsigned, [IO.Compression.ZipArchiveMode]::Update)
    try {
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, (Join-Path $dex 'classes.dex'), 'classes.dex') | Out-Null
    } finally { $archive.Dispose() }
    $aligned = Join-Path $work 'aligned.apk'
    Invoke-Checked $zipalign @('-f', '-p', '4', $unsigned, $aligned)
    Invoke-Checked $zipalign @('-c', '-p', '4', $aligned)
    $result = Join-Path $outputRoot 'aligned.apk'
    Copy-Item -LiteralPath $aligned -Destination $result -Force

    if ($Sign) {
        [xml]$manifest = Get-Content -LiteralPath 'AndroidManifest.xml' -Raw
        $version = $manifest.manifest.GetAttribute('versionName', 'http://schemas.android.com/apk/res/android')
        if ($version -notmatch '^[0-9A-Za-z.-]+$') { throw 'Unsafe versionName for output filename.' }
        $signed = Join-Path $work "BeanFocus-$version.apk"
        Invoke-Checked $java @('-jar', $apksigner, 'sign', '--ks', $keystore, '--ks-key-alias', $env:SIGNING_KEY_ALIAS,
            '--ks-pass', 'env:SIGNING_STORE_PASSWORD', '--key-pass', 'env:SIGNING_KEY_PASSWORD', '--out', $signed, $aligned)
        Invoke-Checked $java @('-jar', $apksigner, 'verify', '--verbose', $signed)
        $result = Join-Path $outputRoot "BeanFocus-$version.apk"
        Copy-Item -LiteralPath $signed -Destination $result -Force
        Write-Output "Signed APK: $result"
    } else {
        Write-Output "Unsigned APK: $result (not installable until signed with the original key)."
    }
    Get-FileHash -LiteralPath $result -Algorithm SHA256 | Format-List
} finally { Pop-Location }
