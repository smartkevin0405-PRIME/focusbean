(function () {
  if (window.__beanFocusGuard) return;
  window.__beanFocusGuard = true;
  function shorts(raw) {
    try { var u = new URL(raw, location.href); return /(^|\.)youtube\.com$/i.test(u.hostname) && /^\/shorts(?:\/|$)/.test(decodeURIComponent(u.pathname)); } catch (_) { return false; }
  }
  function stopAndLeave() {
    document.querySelectorAll('video').forEach(function(v) { v.pause(); });
    document.documentElement.style.visibility = 'hidden';
    location.replace('https://m.youtube.com/');
    return true;
  }
  function redirect() { return shorts(location.href) ? stopAndLeave() : false; }
  var style = document.createElement('style');
  style.textContent = 'ytm-reel-shelf-renderer,ytm-shorts-lockup-view-model,ytm-shorts-lockup-view-model-v2,ytd-reel-shelf-renderer,a[href^="/shorts"],a[href*="youtube.com/shorts/"]{display:none!important}';
  (document.head || document.documentElement).appendChild(style);
  document.addEventListener('click', function(e) {
    var link = e.target.closest && e.target.closest('a[href]');
    if (link && shorts(link.href)) { e.preventDefault(); e.stopImmediatePropagation(); }
  }, true);
  ['pushState','replaceState'].forEach(function(name) {
    var original = history[name];
    history[name] = function(state,title,url) { if (url && shorts(url)) { stopAndLeave(); return; } return original.apply(this,arguments); };
  });
  window.addEventListener('popstate', redirect);
  document.addEventListener('yt-navigate-finish', redirect);
  setInterval(redirect, 300);
  redirect();
})();
