const fs=require('fs'),vm=require('vm'),assert=require('assert');
const source=fs.readFileSync('assets/shorts_guard.js','utf8');
function setup(href){
 const state={paused:0,replaced:null,clicked:false,push:[],events:{},timers:[]};
 const document={documentElement:{style:{}},head:{appendChild(s){state.style=s.textContent}},createElement(){return{}},querySelectorAll(){return[{pause(){state.paused++}}]},addEventListener(n,f){state.events[n]=f}};
 const location={href,replace(u){state.replaced=u}};
 const history={pushState(...a){state.push.push(a)},replaceState(...a){state.push.push(a)}};
 const ctx={window:{addEventListener(n,f){state.events[n]=f}},URL,document,location,history,setInterval(f){state.timers.push(f)}};
 vm.runInNewContext(source,ctx);return{state,ctx};
}
let x=setup('https://m.youtube.com/watch?v=lecture');assert.equal(x.state.replaced,null);assert(x.state.style.includes('ytm-reel-shelf-renderer'));
x.ctx.history.pushState({},'', '/shorts/abc');assert.equal(x.state.push.length,0);assert.equal(x.state.replaced,'https://m.youtube.com/');assert.equal(x.ctx.document.documentElement.style.visibility,'hidden');
x.ctx.history.pushState({},'', '/watch?v=physics');assert.equal(x.state.push.length,1);
x.state.events.click({target:{closest(){return{href:'https://m.youtube.com/shorts/a'}}},preventDefault(){x.state.clicked=true},stopImmediatePropagation(){}});assert(x.state.clicked);
x=setup('https://m.youtube.com/shorts/abc');assert.equal(x.state.paused,1);assert.equal(x.state.replaced,'https://m.youtube.com/');
x=setup('https://m.youtube.com/watch?v=a&next=/shorts/x');assert.equal(x.state.replaced,null);
console.log('Web guard: normal playback, SPA navigation, link interception, pause and redirect checks passed');
