from pathlib import Path
import re
root=Path(__file__).resolve().parents[1];src=root/'src/app/bean/focus';service=(src/'FocusService.java').read_text(encoding='utf-8-sig');shield=(src/'GameShield.java').read_text(encoding='utf-8-sig')
def body(signature):
 start=service.index(signature);start=service.index('{',start);depth=1;end=start+1
 while depth:
  depth+=(service[end]=='{')-(service[end]=='}');end+=1
 return service[start:end]
assert service.count('performGlobalAction(')==2
assert 'performGlobalAction(' in body('boolean userGlobalAction(')
assert 'performGlobalAction(' in body('boolean globalActionFor(')
assert 'performGlobalAction(' not in shield
assert not re.search(r'\baction\s*\(\s*[12]\s*\)',service)
windows=body('void checkBlockedWindows(')
assert windows.count('blockApp(')==1 and windows.index('blockApp(')>windows.index('shield.show(')
assert 'SHIELD_DISMISS_PIP' in windows and 'ACTION_DISMISS' in windows
assert 'isInPictureInPictureMode()' in body('boolean globalActionFor(')
assert 'emergencySurface' in body('boolean blockedApp(')
assert 'protectedForeground' in windows and 'shield.hide()' in windows
assert 'FocusLock.active(this)' in body('boolean guard(') and 'guard_locked_settings' in body('boolean guard(')
assert 'surface(p)' in body('boolean guard(')
assert 'windowCheck,300' in body('void handleEvent(')
assert 'BrowserInspector.read(' not in body('void handleEvent(')
assert 'queueBrowserScan(' in body('void handleEvent(')
assert 'BlockPolicy.browserShorts(' in body('void queueClickedShorts(')
assert 'globalActionFor(GLOBAL_ACTION_BACK,p,window)' in body('void queueClickedShorts(')
assert 'globalActionFor(GLOBAL_ACTION_HOME,p,window)' in body('void verifyExit(')
assert 'latest.getWindowId()!=expectedWindow' in body('boolean globalActionFor(')
assert service.count('BrowserInspector.read(')==1
assert '.commit()' not in (src/'FocusLock.java').read_text(encoding='utf-8-sig')+(src/'RemovalAccess.java').read_text(encoding='utf-8-sig')
manifest=(root/'AndroidManifest.xml').read_text(encoding='utf-8-sig');assert 'versionCode="14"' in manifest and 'versionName="0.14-beta"' in manifest
assert 'QUERY_ALL_PACKAGES' in manifest and 'WRITE_SECURE_SETTINGS' not in manifest
for f in src.glob('*.java'):assert 'androidx.' not in f.read_text(encoding='utf-8-sig')
print('Source invariants audit: passed (automatic actions, PiP, protected UI, lock gating, persistence, manifest)')


