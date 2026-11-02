const assert=require('assert'),s=require('../assets/stats_core.js');
const day=(x={})=>Object.assign({coverage:3600000,lecture:0,leisure:0,unknown:0,shorts:0,game:0,growth:0,xp:0},x);
let d=Array.from({length:63},(_,i)=>day({day:String(i),leisure:100000})),p=s.periods(d,7);assert.equal(p.now.length,7);assert.equal(p.before.length,7);assert.equal(p.now[6].day,'61');assert.equal(p.before[6].day,'54');assert.equal(s.periods(d,30).before.length,30);assert.equal(s.periods(d,1).before.length,0);
let result=s.compare([day({game:60000})],[day({game:120000})],'game');assert(result.ok);assert.equal(result.diff,60000);assert.equal(result.percent,50);
assert(!s.compare([day({game:null})],[day()],'game').ok);assert(!s.compare([day({coverage:0})],[day()],'web').ok);assert(!s.compare([day({coverage:1000})],[day()],'web').ok);
assert(!s.compare([day({unknown:60000})],[day({leisure:60000})],'web').ok);
result=s.compare([day({leisure:60000,lecture:120000})],[day({leisure:180000})],'web');assert(result.ok);assert.equal(result.diff,120000);
assert.equal(s.compare([day()],[day()],'game').percent,null);assert.equal(s.fmt(null),'기록 없음');assert.equal(s.fmt(0),'0분');
console.log('Statistics: period alignment, missing data, coverage, unknown classification, lecture exclusion, zero baseline passed');

assert.deepEqual(s.composition([day({game:null})]),[]);let parts=s.composition([day({lecture:60000,leisure:120000,unknown:30000,game:90000})]);assert.equal(parts.reduce((a,b)=>a+b.value,0),300000);assert.equal(parts.length,4);console.log('Statistics composition: missing data and category totals passed');
