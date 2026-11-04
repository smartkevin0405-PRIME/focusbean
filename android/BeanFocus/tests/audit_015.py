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
manifest = (root / 'AndroidManifest.xml').read_text(encoding='utf-8-sig')
assert 'versionCode="15"' in manifest and 'versionName="0.15-beta"' in manifest
assert 'QUERY_ALL_PACKAGES' in manifest and 'WRITE_SECURE_SETTINGS' not in manifest
for path in src.glob('*.java'):
    assert 'androidx.' not in path.read_text(encoding='utf-8-sig')
print('0.15 source invariants: passed (navigation routing, action evidence/window guards, PiP, settings, persistence, manifest)')
