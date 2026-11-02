const fs=require('fs'),vm=require('vm'),assert=require('assert'),source=fs.readFileSync('assets/channel_probe.js','utf8');
function probe(href,details,anchor){return vm.runInNewContext(source,{URL,location:{href},window:{ytInitialPlayerResponse:{videoDetails:details}},document:{querySelector(){return anchor?{querySelector(){return anchor}}:null}}})}
let r=probe('https://m.youtube.com/watch?v=one',{videoId:'one',channelId:'UCabc',author:'physics'});assert.equal(r.key,'/channel/UCabc');
r=probe('https://m.youtube.com/watch?v=two',{videoId:'one',channelId:'UCwrong'});assert.equal(r.key,'');
r=probe('https://m.youtube.com/watch?v=two',null,{href:'https://www.youtube.com/@lecture/',innerText:'강의'});assert.equal(r.key,'/@lecture');assert.equal(r.name,'강의');
r=probe('https://m.youtube.com/shorts/abc',{videoId:'abc',channelId:'UCabc'});assert.equal(r.key,'');
console.log('Channel probe: matching video, stale player response, owner DOM fallback, non-watch pages passed');
