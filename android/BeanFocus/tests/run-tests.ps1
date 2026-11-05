[CmdletBinding()]
param([string]$JdkHome = $env:JAVA_HOME)

$ErrorActionPreference = 'Stop'
if (-not $JdkHome) { throw 'Set -JdkHome or JAVA_HOME to a JDK (17 or newer).' }
$java = Join-Path $JdkHome 'bin/java.exe'
$javac = Join-Path $JdkHome 'bin/javac.exe'
foreach ($tool in @($java, $javac)) {
    if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) { throw "JDK tool not found: $tool" }
}
$node = (Get-Command node -ErrorAction Stop).Source
$projectRoot = Split-Path -Parent $PSScriptRoot
$work = Join-Path $projectRoot ('build/windows-tests/run-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $work -Force | Out-Null
$script:javaTests = 0

function Source-Files([string]$Prefix, [string[]]$Names) {
    return @($Names | ForEach-Object { "$Prefix/$_.java" })
}
function Invoke-Checked([string]$Program, [string[]]$Arguments) {
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$([IO.Path]::GetFileName($Program)) failed with exit code $LASTEXITCODE." }
}
function Run-Suite([string]$Name, [string[]]$Sources, [string[]]$Tests) {
    $out = Join-Path $work $Name
    New-Item -ItemType Directory -Path $out -Force | Out-Null
    Invoke-Checked $javac (@('--release', '8', '-Xlint:-options', '-encoding', 'UTF-8', '-d', $out) + $Sources + (Source-Files 'tests' $Tests))
    foreach ($test in $Tests) {
        Invoke-Checked $java @('-cp', $out, "app.bean.focus.$test")
        $script:javaTests++
    }
}

Push-Location $projectRoot
try {
    Run-Suite 'pure' (Source-Files 'src/app/bean/focus' @('UrlRules', 'BrowserAddress', 'BrowserScanPolicy', 'BlockPolicy', 'DeviceConstants',
        'UpdateRules', 'WindowPolicy', 'PipDismissTracker', 'RecentClose', 'InstallTimePolicy', 'TimerRules', 'GrowthRules', 'XpMath', 'ShortsFilterRules')) @(
        'UrlRulesTest', 'BrowserAddressTest', 'BrowserScanPolicyTest', 'BlockPolicyTest', 'WindowPolicyTest', 'UpdateRulesTest', 'RecentCloseTest', 'TimerRulesTest', 'GrowthRulesTest', 'XpMathTest', 'ShortsFilterRulesTest')
    Run-Suite 'state' ((Source-Files 'tests/fakes/android' @('content/Context', 'content/SharedPreferences', 'os/SystemClock', 'os/Build', 'provider/Settings')) +
        @('tests/fakes/app/bean/focus/Prefs.java') + (Source-Files 'src/app/bean/focus' @('FocusLock', 'TimerRules', 'NetworkTime', 'RemovalAccess',
        'BlockPolicy', 'BrowserAddress', 'UrlRules', 'DeviceConstants', 'NewPackageLedger', 'ShortcutCheck', 'InstallTimePolicy'))) @(
        'FocusLockStateTest', 'RemovalAccessTest', 'LedgerShortcutTest', 'NetworkTimeTest')
    Run-Suite 'growth' ((Source-Files 'tests/fakes/android' @('content/Context', 'content/SharedPreferences', 'os/SystemClock')) +
        (Source-Files 'tests/fakes/app/bean/focus' @('Prefs', 'AnalyticsDb')) + (Source-Files 'src/app/bean/focus' @('GrowthTracker', 'GrowthRules'))) @('GrowthTrackerTest')
    Run-Suite 'telemetry' ((Source-Files 'tests/fakes/android' @('content/Context', 'os/SystemClock')) +
        @('tests/fakes/app/bean/focus/AnalyticsDb.java', 'src/app/bean/focus/TelemetrySession.java')) @('TelemetrySessionTest')
    Run-Suite 'browser' ((Source-Files 'tests/browser_fakes/android' @('os/Bundle', 'graphics/Rect', 'view/accessibility/AccessibilityNodeInfo')) +
        (Source-Files 'src/app/bean/focus' @('BrowserInspector', 'BrowserPlayerInspector', 'BrowserScanPolicy', 'WebShortsRules', 'BrowserAddress', 'UrlRules',
        'BlockPolicy', 'YouTubeInspector', 'SettingsGuard', 'SettingsNodeCollector', 'DeviceConstants', 'ViewIdSnapshot', 'StructureSnapshot', 'RecentsInspector'))) @(
        'BrowserInspectorTest', 'YouTubeSettingsTest', 'SettingsGuardDecideTest', 'BrowserPlayerTest', 'BrowserNavigationTest', 'BrowserHiddenAddressTest', 'BrowserDiagnosticsTest')
    $nodeTests = @('guard_test.cjs', 'channel_test.cjs', 'stats_test.cjs', 'stats_dashboard_test.cjs')
    foreach ($test in $nodeTests) { Invoke-Checked $node @("tests/$test") }
    Write-Output "PASS: $script:javaTests Java test programs and $($nodeTests.Count) Node test programs. Android node fakes do not replace device tests."
} finally { Pop-Location }
