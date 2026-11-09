"""Source integration guards; runtime behavior is covered by the Java suites."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
src = root / 'src/app/bean/focus'
service = (src / 'FocusService.java').read_text(encoding='utf-8-sig')
shield = (src / 'GameShield.java').read_text(encoding='utf-8-sig')

def body(signature):
    start = service.index('{', service.index(signature))
    depth, end = 1, start + 1
    while depth:
        depth += (service[end] == '{') - (service[end] == '}')
        end += 1
    return service[start:end]

assert service.count('performGlobalAction(') == 2
assert 'performGlobalAction(' in body('boolean userGlobalAction(')
assert 'performGlobalAction(' in body('boolean globalActionFor(')
assert 'performGlobalAction(' not in shield
assert not re.search(r'\baction\s*\(\s*[12]\s*\)', service)
windows = body('void checkBlockedWindows(')
assert windows.count('blockApp(') == 1 and windows.index('blockApp(') > windows.index('shield.show(')
assert 'SHIELD_DISMISS_PIP' in windows and 'ACTION_DISMISS' in windows
assert 'isInPictureInPictureMode()' in body('boolean globalActionFor(')
assert 'emergencySurface' in body('boolean blockedApp(')
assert 'protectedForeground' in windows and 'shield.hide()' in windows
assert 'FocusLock.active(this)' in body('boolean guard(') and 'guard_locked_settings' in body('boolean guard(')
assert 'surface(p)' in body('boolean guard(')
assert 'windowCheck,300' in body('void handleEvent(')
assert 'BrowserInspector.read(' not in body('void handleEvent(')
assert 'queueBrowserScan(' in body('void handleEvent(')
# Click evidence only arms a scan. It must never itself deliver BACK/HOME.
click = body('void queueClickedShorts(')
assert 'browserBudget.navigation(' in click and 'queueBrowserScan(' in click
assert 'globalActionFor(' not in click and 'readBrowser(' not in click
inspect = body('void inspect(')
assert inspect.index('browserBudget.delay(') < inspect.index('readBrowser(')
assert 'BlockPolicy.browserShorts(' in inspect
assert 'browserBudget.retryDelay(' in inspect
assert 'if(result.editing)browserBudget.endNavigation()' in inspect
assert 'globalActionFor(GLOBAL_ACTION_BACK,p,window)' in body('void backFromShorts(')
assert 'browserBudget.endNavigation()' in body('void backFromShorts(')
assert 'globalActionFor(GLOBAL_ACTION_HOME,p,window)' in body('void verifyExit(')
assert 'latest.getWindowId()!=expectedWindow' in body('boolean globalActionFor(')
assert service.count('BrowserInspector.read(') == 1
assert 'confirmAction||browserBudget.navigating(started)' in body('BrowserInspector.Result readBrowser(')
assert '.commit()' not in (src / 'FocusLock.java').read_text(encoding='utf-8-sig') + (src / 'RemovalAccess.java').read_text(encoding='utf-8-sig')
# Handoff 1: a briefly missing root keeps a bounded retry; another app/window ends it.
check = service[service.index('Runnable browserCheck='):service.index('private void retryMissingRoot(')]
assert 'if(root==null){retryMissingRoot(p,window);return;}' in check and 'if(root==null){browserBudget.endNavigation()' not in check
retry = body('void retryMissingRoot(')
assert 'browserBudget.missingRootDelay(' in retry and 'frontMayBe(p,window)' in retry and 'browserBudget.endNavigation()' in retry
# Hidden toolbar: only a host class (YouTube/other/unknown) is remembered per window.
reader = body('BrowserInspector.Result readBrowser(')
assert 'browserBudget.host()' in reader and 'browserBudget.rememberHost(r.hostEvidence())' in reader
# Handoff 6: native YouTube throttles before tree/window queries and keeps its window for BACK/HOME.
event = body('void handleEvent(')
assert event.index('youtubeEventDue(p)') < event.index('activeRoot(p,-1)')
youtube = body('void inspectYouTube(')
assert 'globalActionFor(GLOBAL_ACTION_BACK,p,window)' in youtube and 'globalActionFor(GLOBAL_ACTION_HOME,p,window)' in youtube
assert 'globalActionFor(GLOBAL_ACTION_BACK,p)' not in youtube and 'globalActionFor(GLOBAL_ACTION_HOME,p)' not in youtube
# Samsung Internet content blocker: read-only provider, sync on state change, Shorts-only rules.
assert 'SamsungContentBlocker.sync(this)' in body('void sampleExternal(')
provider = (src / 'ShortsFilterProvider.java').read_text(encoding='utf-8-sig')
assert "mode.indexOf('w')>=0" in provider and 'MODE_READ_ONLY' in provider and 'MODE_READ_WRITE' not in provider
assert 'return null;}' in provider  # query exposes no rows
rules = (src / 'ShortsFilterRules.java').read_text(encoding='utf-8-sig')
assert '"||youtube.com/shorts"' in rules and '"||youtube.com/youtubei/v1/reel/"' in rules
assert 'watch' not in rules.split('static final String[] BLOCK=')[1].split(';')[0]
manifest = (root / 'AndroidManifest.xml').read_text(encoding='utf-8-sig')
assert 'android:authorities="app.bean.focus.contentBlocker.contentProvider" android:exported="true"' in manifest
assert 'com.samsung.android.sbrowser.contentBlocker.interfaceVersion" android:value="API_1.0"' in manifest
assert 'com.samsung.android.sbrowser.permission.CONTENTBLOCKER' in manifest
blocker = (src / 'SamsungContentBlocker.java').read_text(encoding='utf-8-sig')
assert 'ShortsFilterRules.announce(' in blocker and 'samsung_blocker_notified_wall' in blocker
assert 'SamsungContentBlocker.browserChanged(' in (src / 'FocusService.java').read_text(encoding='utf-8-sig')
assert 'versionCode="15"' in manifest and 'versionName="0.15-beta"' in manifest
assert 'QUERY_ALL_PACKAGES' in manifest and 'WRITE_SECURE_SETTINGS' not in manifest
for path in src.glob('*.java'):
    assert 'androidx.' not in path.read_text(encoding='utf-8-sig')
print('0.15 source invariants: passed (navigation routing, missing-root retry, host memory, native YouTube window guards, Samsung content blocker, PiP, settings, persistence, manifest)')
