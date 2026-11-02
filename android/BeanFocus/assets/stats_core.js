function total(rows,key){return rows.reduce((a,r)=>a+(Number(r[key])||0),0)}
function fmt(ms){if(ms==null)return '기록 없음';let m=Math.round(ms/60000);return m>=60?Math.floor(m/60)+'시간 '+m%60+'분':m+'분'}
function exact(ms){if(ms==null)return '기록 없음';let s=Math.round(ms/1000),h=Math.floor(s/3600),m=Math.floor(s%3600/60);return (h?h+'시간 ':'')+(m?m+'분 ':'')+s%60+'초'}
function webKnown(r){return r.coverage>0||r.lecture+r.leisure+r.shorts+r.unknown>0}
function periods(days,n){if(n===1)return {now:days.slice(-1),before:[],label:'오늘 누적 · 전일 비교 제외'};return {now:days.slice(-n-1,-1),before:days.slice(-2*n-1,-n-1),label:'최근 '+n+'일과 직전 '+n+'일 · 오늘 제외'}}
function compare(rows,previous,key){
 if(!previous.length)return {ok:false,text:'오늘은 누적만 표시'};
 if(key==='game'){if(rows.some(r=>r.game==null)||previous.some(r=>r.game==null))return {ok:false,text:'기간 내 Android 기록 부족'};}
 else{if(rows.some(r=>!webKnown(r))||previous.some(r=>!webKnown(r)))return {ok:false,text:'기간 내 관측 기록 부족'};
 let a=total(rows,'coverage'),b=total(previous,'coverage');if(!a||!b||a/b<.8||a/b>1.25)return {ok:false,text:'관측 범위가 달라 비교 보류'};
 for(let r of [rows,previous]){let unknown=total(r,'unknown'),all=total(r,'lecture')+total(r,'leisure')+total(r,'shorts')+unknown;if(all>0&&unknown/all>.1)return {ok:false,text:'미분류 비중이 높아 비교 보류'};}}
 let cur=key==='web'?total(rows,'leisure')+total(rows,'shorts'):total(rows,key),prev=key==='web'?total(previous,'leisure')+total(previous,'shorts'):total(previous,key),diff=prev-cur;
 return {ok:true,current:cur,previous:prev,diff,percent:prev>0?100*diff/prev:null,text:(diff>=0?'감소 ':'증가 ')+fmt(Math.abs(diff))+(prev>0?' · '+Math.abs(100*diff/prev).toFixed(1)+'%':' · 이전 0분, 비율 계산 제외')};
}
function composition(rows){return [{key:'lecture',label:'웹 강의',value:total(rows,'lecture'),color:'#78a98e'},{key:'leisure',label:'웹 여가',value:total(rows,'leisure'),color:'#d6a756'},{key:'unknown',label:'웹 미분류',value:total(rows,'unknown'),color:'#b9c2bd'},{key:'shorts',label:'웹 쇼츠',value:total(rows,'shorts'),color:'#d17d76'},{key:'game',label:'선택 게임',value:total(rows,'game'),color:'#467c60'}].filter(s=>s.value>0)}
if(typeof module!=='undefined')module.exports={total,fmt,webKnown,periods,compare,composition};
