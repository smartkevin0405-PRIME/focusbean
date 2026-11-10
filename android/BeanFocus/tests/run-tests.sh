#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ECJ_JAR:?Set ECJ_JAR to Eclipse ECJ jar}"
mkdir -p build/test-pure build/test-state build/test-growth build/test-telemetry build/test-browser
java -jar "$ECJ_JAR" -nowarn -encoding UTF-8 -source 1.8 -target 1.8 -d build/test-pure src/app/bean/focus/{UrlRules,BrowserAddress,BrowserScanPolicy,BlockPolicy,DeviceConstants,UpdateRules,WindowPolicy,PipDismissTracker,RecentClose,InstallTimePolicy,TimerRules,GrowthRules,XpMath,ShortsFilterRules}.java tests/{UrlRulesTest,BrowserAddressTest,BrowserScanPolicyTest,BlockPolicyTest,WindowPolicyTest,UpdateRulesTest,RecentCloseTest,TimerRulesTest,GrowthRulesTest,XpMathTest,ShortsFilterRulesTest}.java
for t in UrlRulesTest BrowserAddressTest BrowserScanPolicyTest BlockPolicyTest WindowPolicyTest UpdateRulesTest RecentCloseTest TimerRulesTest GrowthRulesTest XpMathTest ShortsFilterRulesTest; do java -cp build/test-pure "app.bean.focus.$t"; done
java -jar "$ECJ_JAR" -nowarn -encoding UTF-8 -source 1.8 -target 1.8 -d build/test-state tests/fakes/android/{content/Context,content/SharedPreferences,os/SystemClock,os/Build,provider/Settings}.java tests/fakes/app/bean/focus/Prefs.java src/app/bean/focus/{FocusLock,TimerRules,NetworkTime,RemovalAccess,BlockPolicy,BrowserAddress,UrlRules,DeviceConstants,NewPackageLedger,ShortcutCheck,InstallTimePolicy}.java tests/{FocusLockStateTest,RemovalAccessTest,LedgerShortcutTest,NetworkTimeTest}.java
java -cp build/test-state app.bean.focus.FocusLockStateTest
java -cp build/test-state app.bean.focus.RemovalAccessTest
java -cp build/test-state app.bean.focus.LedgerShortcutTest
java -cp build/test-state app.bean.focus.NetworkTimeTest
java -jar "$ECJ_JAR" -nowarn -encoding UTF-8 -source 1.8 -target 1.8 -d build/test-growth tests/fakes/android/{content/Context,content/SharedPreferences,os/SystemClock}.java tests/fakes/app/bean/focus/{Prefs,AnalyticsDb}.java src/app/bean/focus/{GrowthTracker,GrowthRules}.java tests/GrowthTrackerTest.java
java -cp build/test-growth app.bean.focus.GrowthTrackerTest
java -jar "$ECJ_JAR" -nowarn -encoding UTF-8 -source 1.8 -target 1.8 -d build/test-telemetry tests/fakes/android/{content/Context,os/SystemClock}.java tests/fakes/app/bean/focus/AnalyticsDb.java src/app/bean/focus/TelemetrySession.java tests/TelemetrySessionTest.java
java -cp build/test-telemetry app.bean.focus.TelemetrySessionTest
java -jar "$ECJ_JAR" -nowarn -encoding UTF-8 -source 1.8 -target 1.8 -d build/test-browser tests/browser_fakes/android/{os/Bundle,graphics/Rect,view/accessibility/AccessibilityNodeInfo}.java src/app/bean/focus/{BrowserInspector,BrowserPlayerInspector,BrowserScanPolicy,WebShortsRules,BrowserAddress,UrlRules,BlockPolicy,YouTubeInspector,SettingsGuard,SettingsNodeCollector,DeviceConstants,ViewIdSnapshot,RecentsInspector,BlockerGuard}.java tests/{BlockerGuardTest,BrowserInspectorTest,BrowserPlayerTest,BrowserNavigationTest,BrowserHiddenAddressTest,BrowserEmptyPageTest,YouTubeSettingsTest,SettingsGuardDecideTest}.java
java -cp build/test-browser app.bean.focus.BrowserInspectorTest
java -cp build/test-browser app.bean.focus.YouTubeSettingsTest
java -cp build/test-browser app.bean.focus.SettingsGuardDecideTest
java -cp build/test-browser app.bean.focus.BrowserPlayerTest
java -cp build/test-browser app.bean.focus.BrowserNavigationTest
java -cp build/test-browser app.bean.focus.BrowserHiddenAddressTest
java -cp build/test-browser app.bean.focus.BrowserEmptyPageTest
java -cp build/test-browser app.bean.focus.BlockerGuardTest
node tests/guard_test.cjs
node tests/channel_test.cjs
node tests/stats_test.cjs

node tests/stats_dashboard_test.cjs
