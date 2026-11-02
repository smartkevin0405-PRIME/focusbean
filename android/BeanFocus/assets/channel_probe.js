(function(){
 var u=new URL(location.href),video=u.searchParams.get('v');
 if(u.pathname!='/watch'||!video)return {video:'',key:'',name:'',alias:''};
 var owner=document.querySelector('ytm-slim-owner-renderer,ytm-video-owner-renderer,ytd-video-owner-renderer,#owner'),alias='',ownerName='';
 if(owner){var a=owner.querySelector('a[href^="/@"],a[href*="/channel/"],a[href^="/c/"],a[href^="/user/"]');
 if(a){alias=new URL(a.href,location.href).pathname.replace(/\/$/,'');ownerName=(a.innerText||a.textContent||alias).trim().slice(0,150);}}
 var details=window.ytInitialPlayerResponse&&window.ytInitialPlayerResponse.videoDetails;
 if(details&&details.videoId===video&&details.channelId)return {video:video,key:'/channel/'+details.channelId,name:details.author||details.channelId,alias:alias};
 if(alias)return {video:video,key:alias,name:ownerName,alias:alias};
 return {video:video,key:'',name:'',alias:''};
})()
