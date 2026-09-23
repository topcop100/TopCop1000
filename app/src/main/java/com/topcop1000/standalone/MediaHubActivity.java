package com.topcop1000.standalone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import android.widget.EditText;
import android.app.AlertDialog;
import java.io.*;
import java.util.*;
import java.net.*;

public class MediaHubActivity extends Activity {
    static final int REQ_VIDEO=20, REQ_M3U=21;
    int streamFocus=0; boolean showingPlaylist=false; String stalkerBase="",stalkerMac="",stalkerToken=""; HubView hubView;
    final ArrayList<String> playlistNames=new ArrayList<>(), playlistUrls=new ArrayList<>();
    public static final String EXTRA_MODE="mode";
    @Override public void onCreate(Bundle b){super.onCreate(b);hubView=new HubView();setContentView(hubView);String raw=getIntent().getStringExtra("playlist_uri");if(raw!=null&&!raw.isEmpty())parseM3u(Uri.parse(raw));String favCmd=getIntent().getStringExtra("favorite_stalker_cmd");if(favCmd!=null&&!favCmd.isEmpty())openSavedStalkerFavorite(favCmd);}
    void openSavedStalkerFavorite(String cmd){
        android.content.SharedPreferences sp=getSharedPreferences("topcop_portals",MODE_PRIVATE);
        String server=sp.getString("stalker_server",""),mac=sp.getString("stalker_mac","");
        String base=PortalUrlBuilder.normalizeServer(server);
        if(base.isEmpty()||mac.isEmpty()){Toast.makeText(this,"Stalker-Profil fehlt",Toast.LENGTH_SHORT).show();return;}
        refreshStalkerTokenAndResolve(base,mac,cmd);
    }
    void refreshStalkerTokenAndResolve(String base,String mac,String cmd){
        new Thread(()->{
            HttpURLConnection con=null;
            try{
                URL u=new URL(base+"/server/load.php?type=stb&action=handshake&token=&JsHttpRequest=1-xml");
                con=(HttpURLConnection)u.openConnection();con.setConnectTimeout(8000);con.setReadTimeout(10000);
                con.setRequestProperty("Cookie","mac="+mac.replace(":","%3A")+"; stb_lang=en; timezone=UTC");
                con.setRequestProperty("User-Agent","Mozilla/5.0 (QtEmbedded; U; Linux; C) MAG200 stbapp");
                if(con.getResponseCode()<200||con.getResponseCode()>=400)throw new IOException();
                StringBuilder sb=new StringBuilder();try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){String ln;while((ln=br.readLine())!=null)sb.append(ln);}
                java.util.regex.Matcher tm=java.util.regex.Pattern.compile("\\\"token\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(sb.toString());
                if(!tm.find())throw new IOException();
                String token=tm.group(1);getSharedPreferences("topcop_portals",MODE_PRIVATE).edit().putString("stalker_token",token).apply();
                stalkerBase=base;stalkerMac=mac;stalkerToken=token;runOnUiThread(()->hubView.resolveStalkerLink(cmd));
            }catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Stalker-Favorit konnte nicht verbunden werden",Toast.LENGTH_SHORT).show());}
            finally{if(con!=null)con.disconnect();}
        }).start();
    }
    final class HubView extends View{
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final String mode; final String[] items; int focus=0;
        HubView(){super(MediaHubActivity.this);mode=getIntent().getStringExtra(EXTRA_MODE);
            items="VIDEO".equals(mode)?new String[]{"DATEI ÖFFNEN","VIDEO-URL","FAVORITEN","ZURÜCK"}:
                  new String[]{"M3U / M3U8","STALKER / MAC","XTREAM","FAVORITEN","ZURÜCK"};
            setFocusable(true);requestFocus();}
        @Override protected void onDraw(Canvas c){
            c.drawColor(Color.rgb(5,5,8)); int w=getWidth(),h=getHeight();
            p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);
            if(showingPlaylist){ drawPlaylist(c,w,h); return; }
            p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.07f);c.drawText(mode,w/2f,h*.15f,p);
            for(int i=0;i<items.length;i++){float y=h*.28f+i*h*.12f;RectF r=new RectF(w*.18f,y,w*.82f,y+h*.085f);
                p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,22,22,p);
                p.setColor(Color.WHITE);p.setTextSize(h*.035f);c.drawText(items[i],r.centerX(),r.centerY()+10,p);}
        }
        void drawPlaylist(Canvas c,int w,int h){
            p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.06f);c.drawText("M3U PLAYLIST",w/2f,h*.12f,p);
            int first=Math.max(0,Math.min(streamFocus-3,Math.max(0,playlistNames.size()-7)));
            for(int row=0;row<7&&first+row<playlistNames.size();row++){int i=first+row;float y=h*.19f+row*h*.10f;RectF r=new RectF(w*.12f,y,w*.88f,y+h*.07f);p.setColor(i==streamFocus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,18,18,p);p.setColor(Color.WHITE);p.setTextSize(h*.027f);String s=playlistNames.get(i);if(s.length()>58)s=s.substring(0,55)+"...";c.drawText(s,r.centerX(),r.centerY()+8,p);}
        }
        void openStream(){ if(playlistUrls.isEmpty())return; String url=playlistUrls.get(streamFocus).trim(); if("PORTALE".equals(mode)&&!stalkerToken.isEmpty()&&isStalkerCommand(url)){resolveStalkerLink(url);return;} playResolvedStream(url); }
        boolean isStalkerCommand(String value){String v=value==null?"":value.trim().toLowerCase(java.util.Locale.ROOT);return v.startsWith("ffmpeg ")||v.startsWith("auto ")||v.startsWith("ffrt ")||v.startsWith("ffmpeg://");}
        void playResolvedStream(String raw){String url=raw.trim();if(url.startsWith("ffmpeg "))url=url.substring(7).trim();int sp=url.indexOf(' ');if(sp>0&&url.substring(0,sp).indexOf("://")<0)url=url.substring(sp+1).trim();Intent v=new Intent(Intent.ACTION_VIEW,Uri.parse(url));try{startActivity(v);}catch(Exception e){Toast.makeText(MediaHubActivity.this,"Kein kompatibler Stream-Player installiert",Toast.LENGTH_SHORT).show();}}
        void resolveStalkerLink(String cmd){
            if(cmd.startsWith("http://")||cmd.startsWith("https://")){playResolvedStream(cmd);return;}
            if(stalkerBase.isEmpty()||stalkerMac.isEmpty()||stalkerToken.isEmpty()){Toast.makeText(MediaHubActivity.this,"Stalker-Verbindung fehlt",Toast.LENGTH_SHORT).show();return;}
            new Thread(()->{HttpURLConnection con=null;try{String q=URLEncoder.encode(cmd,"UTF-8");URL u=new URL(stalkerBase+"/server/load.php?type=itv&action=create_link&cmd="+q+"&JsHttpRequest=1-xml");con=(HttpURLConnection)u.openConnection();con.setConnectTimeout(8000);con.setReadTimeout(12000);con.setRequestProperty("Authorization","Bearer "+stalkerToken);con.setRequestProperty("Cookie","mac="+stalkerMac.replace(":","%3A")+"; stb_lang=en; timezone=UTC");con.setRequestProperty("User-Agent","Mozilla/5.0 (QtEmbedded; U; Linux; C) MAG200 stbapp");if(con.getResponseCode()<200||con.getResponseCode()>=400)throw new IOException();StringBuilder sb=new StringBuilder();try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){String ln;while((ln=br.readLine())!=null)sb.append(ln);}java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\\"cmd\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(sb.toString());if(!m.find())throw new IOException();String link=m.group(1).replace("\\/","/");runOnUiThread(()->playResolvedStream(link));}catch(Exception ex){runOnUiThread(()->Toast.makeText(MediaHubActivity.this,"Stalker Stream-Link konnte nicht erstellt werden",Toast.LENGTH_SHORT).show());}finally{if(con!=null)con.disconnect();}}).start();
        }

        void choose(){
            if(focus==items.length-1){finish();return;}
            if(("LIVE TV".equals(mode)||"PORTALE".equals(mode))&&focus==0){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQ_M3U);return; }
            if("VIDEO".equals(mode)&&focus==0){
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("video/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQ_VIDEO);return;
            }
            if("VIDEO".equals(mode)&&focus==1){askVideoUrl();return;}
            if("VIDEO".equals(mode)&&focus==2){Intent fav=new Intent(MediaHubActivity.this,FavoritesActivity.class);fav.putExtra("category","video");startActivity(fav);return;}
            if(("LIVE TV".equals(mode)||"PORTALE".equals(mode))&&focus==3){Intent fav=new Intent(MediaHubActivity.this,FavoritesActivity.class);fav.putExtra("category","PORTALE".equals(mode)?"portals":"livetv");startActivity(fav);return;}
            if("PORTALE".equals(mode)&&focus==1){
                android.content.SharedPreferences sp=getSharedPreferences("topcop_portals",MODE_PRIVATE);
                String server=sp.getString("stalker_server",""), mac=sp.getString("stalker_mac","");
                if(server.isEmpty()||mac.isEmpty()) askStalker(); else testStalker(server,mac);
                return;
            }
            if("PORTALE".equals(mode)&&focus==2){
                android.content.SharedPreferences sp=getSharedPreferences("topcop_portals",MODE_PRIVATE);
                String server=sp.getString("xtream_server",""), user=sp.getString("xtream_user",""), secret=sp.getString("xtream_secret","");
                PortalProfile p=new PortalProfile(PortalProfile.Type.XTREAM,"Xtream",server,user,secret);
                String url=PortalUrlBuilder.xtreamPlaylist(p);
                if(url.isEmpty()) askXtream(); else importRemoteM3u(url);
                return;
            }
            Toast.makeText(MediaHubActivity.this,items[focus]+" vorbereitet",Toast.LENGTH_SHORT).show();
        }
        void askVideoUrl(){
            final EditText input=new EditText(MediaHubActivity.this);input.setHint("https://... Video URL");
            new AlertDialog.Builder(MediaHubActivity.this).setTitle("VIDEO-URL").setView(input)
                .setPositiveButton("Öffnen",(d,w)->{String url=input.getText().toString().trim();if(!(url.startsWith("http://")||url.startsWith("https://"))){Toast.makeText(MediaHubActivity.this,"Ungültige Video-Adresse",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url));try{startActivity(i);}catch(Exception e){Toast.makeText(MediaHubActivity.this,"Kein kompatibler Videoplayer installiert",Toast.LENGTH_SHORT).show();}})
                .setNegativeButton("Abbrechen",null).show();
        }


        void addVideoUrlFavorite(){
            final EditText input=new EditText(MediaHubActivity.this);input.setHint("https://... Video URL");
            new AlertDialog.Builder(MediaHubActivity.this).setTitle("VIDEO-FAVORIT").setView(input)
                .setPositiveButton("Speichern",(d,w)->{String url=input.getText().toString().trim();if(!(url.startsWith("http://")||url.startsWith("https://"))){Toast.makeText(MediaHubActivity.this,"Ungültige Video-Adresse",Toast.LENGTH_SHORT).show();return;}FavoriteStore.add(MediaHubActivity.this,"video","VIDEO | "+url);Toast.makeText(MediaHubActivity.this,"Video-Favorit gespeichert",Toast.LENGTH_SHORT).show();})
                .setNegativeButton("Abbrechen",null).show();
        }

        void importRemoteM3u(String url){
            Toast.makeText(MediaHubActivity.this,"Xtream Playlist wird geladen",Toast.LENGTH_SHORT).show();
            new Thread(()->{
                try{
                    HttpURLConnection con=(HttpURLConnection)new URL(url).openConnection();con.setConnectTimeout(8000);con.setReadTimeout(12000);con.setInstanceFollowRedirects(true);
                    if(con.getResponseCode()<200||con.getResponseCode()>=400){con.disconnect();throw new IOException("HTTP "+con.getResponseCode());}
                    final ArrayList<String> names=new ArrayList<>(), urls=new ArrayList<>();
                    try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){
                        String line,name=null;while((line=br.readLine())!=null){line=line.trim();if(line.startsWith("#EXTINF:")){int comma=line.indexOf(",");name=comma>=0?line.substring(comma+1).trim():"STREAM";}else if(!line.isEmpty()&&!line.startsWith("#")){names.add(name==null?"STREAM "+(names.size()+1):name);urls.add(line);name=null;}}
                    } finally {con.disconnect();}
                    runOnUiThread(()->{playlistNames.clear();playlistUrls.clear();playlistNames.addAll(names);playlistUrls.addAll(urls);showingPlaylist=!playlistUrls.isEmpty();streamFocus=0;Toast.makeText(MediaHubActivity.this,playlistUrls.size()+" Xtream Streams geladen",Toast.LENGTH_SHORT).show();invalidate();});
                }catch(Exception e){runOnUiThread(()->Toast.makeText(MediaHubActivity.this,"Xtream Verbindung fehlgeschlagen",Toast.LENGTH_SHORT).show());}
            }).start();
        }

        void testStalker(String server,String mac){
            String base=PortalUrlBuilder.normalizeServer(server);
            if(base.isEmpty()){Toast.makeText(MediaHubActivity.this,"Ungültige Stalker Serveradresse",Toast.LENGTH_SHORT).show();return;}
            Toast.makeText(MediaHubActivity.this,"Stalker Verbindung wird geprüft",Toast.LENGTH_SHORT).show();
            new Thread(()->{
                HttpURLConnection con=null;
                try{
                    URL u=new URL(base+"/server/load.php?type=stb&action=handshake&token=&JsHttpRequest=1-xml");
                    con=(HttpURLConnection)u.openConnection();con.setConnectTimeout(8000);con.setReadTimeout(10000);
                    con.setRequestProperty("Cookie","mac="+mac.replace(":","%3A")+"; stb_lang=en; timezone=UTC");
                    con.setRequestProperty("User-Agent","Mozilla/5.0 (QtEmbedded; U; Linux; C) MAG200 stbapp");
                    int code=con.getResponseCode();
                    final boolean ok=code>=200&&code<400;
                    String token="";
                    if(ok){try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){StringBuilder sb=new StringBuilder();String ln;while((ln=br.readLine())!=null)sb.append(ln);String body=sb.toString();java.util.regex.Matcher tm=java.util.regex.Pattern.compile("\\\"token\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(body);if(tm.find())token=tm.group(1);}}
                    final String finalToken=token;
                    if(!finalToken.isEmpty())getSharedPreferences("topcop_portals",MODE_PRIVATE).edit().putString("stalker_token",finalToken).apply();
                    runOnUiThread(()->{
                        Toast.makeText(MediaHubActivity.this,!finalToken.isEmpty()?"Stalker Handshake OK · Token gespeichert":(ok?"Portal erreichbar · kein Token erkannt":"Stalker antwortet nicht korrekt"),Toast.LENGTH_SHORT).show();
                        if(!finalToken.isEmpty()) loadStalkerChannels(base,mac,finalToken);
                    });
                }catch(Exception e){runOnUiThread(()->Toast.makeText(MediaHubActivity.this,"Stalker Verbindung fehlgeschlagen",Toast.LENGTH_SHORT).show());}
                finally{if(con!=null)con.disconnect();}
            }).start();
        }

        void loadStalkerChannels(String base,String mac,String token){
            stalkerBase=base;stalkerMac=mac;stalkerToken=token;
            new Thread(()->{
                HttpURLConnection con=null;
                try{
                    URL u=new URL(base+"/server/load.php?type=itv&action=get_all_channels&JsHttpRequest=1-xml");
                    con=(HttpURLConnection)u.openConnection();con.setConnectTimeout(8000);con.setReadTimeout(12000);
                    con.setRequestProperty("Authorization","Bearer "+token);
                    con.setRequestProperty("Cookie","mac="+mac.replace(":","%3A")+"; stb_lang=en; timezone=UTC");
                    con.setRequestProperty("User-Agent","Mozilla/5.0 (QtEmbedded; U; Linux; C) MAG200 stbapp");
                    if(con.getResponseCode()<200||con.getResponseCode()>=400)throw new IOException("HTTP "+con.getResponseCode());
                    StringBuilder sb=new StringBuilder();try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){String ln;while((ln=br.readLine())!=null)sb.append(ln);}
                    String body=sb.toString();
                    final ArrayList<String> names=new ArrayList<>(), cmds=new ArrayList<>();
                    java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"[\\s\\S]*?\\\"cmd\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").matcher(body);
                    while(m.find()){String n=m.group(1).replace("\\\\/","/");String cmd=m.group(2).replace("\\\\/","/");if(!n.isEmpty()&&!cmd.isEmpty()){names.add(n);cmds.add(cmd);}}
                    runOnUiThread(()->{playlistNames.clear();playlistUrls.clear();playlistNames.addAll(names);playlistUrls.addAll(cmds);showingPlaylist=!playlistNames.isEmpty();streamFocus=0;Toast.makeText(MediaHubActivity.this,playlistNames.isEmpty()?"Stalker verbunden · keine Sender erkannt":playlistNames.size()+" Stalker Sender geladen",Toast.LENGTH_SHORT).show();invalidate();});
                }catch(Exception e){runOnUiThread(()->Toast.makeText(MediaHubActivity.this,"Stalker Senderabfrage fehlgeschlagen",Toast.LENGTH_SHORT).show());}
                finally{if(con!=null)con.disconnect();}
            }).start();
        }

        void askStalker(){
            final EditText input=new EditText(MediaHubActivity.this); input.setHint("Server URL | MAC");
            new AlertDialog.Builder(MediaHubActivity.this).setTitle("Stalker / MAC").setView(input)
                .setPositiveButton("Speichern",(d,w)->{String[] a=input.getText().toString().trim().split("\\|",2); if(a.length==2){PortalProfile p=new PortalProfile(PortalProfile.Type.STALKER,"Stalker",a[0].trim(),"",a[1].trim()); if(p.isValid()){getSharedPreferences("topcop_portals",MODE_PRIVATE).edit().putString("stalker_server",p.server).putString("stalker_mac",p.secret.toUpperCase(java.util.Locale.ROOT)).remove("stalker_token").apply();Toast.makeText(MediaHubActivity.this,"Stalker-Profil gespeichert · Verbindung wird geprüft",Toast.LENGTH_SHORT).show();testStalker(p.server,p.secret.toUpperCase(java.util.Locale.ROOT));}else Toast.makeText(MediaHubActivity.this,"Format: Server URL | 00:1A:79:XX:XX:XX",Toast.LENGTH_SHORT).show();}})
                .setNegativeButton("Abbrechen",null).show();
        }
        void askXtream(){
            final EditText input=new EditText(MediaHubActivity.this); input.setHint("Server URL | Benutzer | Passwort");
            new AlertDialog.Builder(MediaHubActivity.this).setTitle("Xtream").setView(input)
                .setPositiveButton("Speichern",(d,w)->{String[] a=input.getText().toString().trim().split("\\|",3); if(a.length==3){PortalProfile p=new PortalProfile(PortalProfile.Type.XTREAM,"Xtream",a[0].trim(),a[1].trim(),a[2].trim()); if(p.isValid()){getSharedPreferences("topcop_portals",MODE_PRIVATE).edit().putString("xtream_server",p.server).putString("xtream_user",p.user).putString("xtream_secret",p.secret).apply();Toast.makeText(MediaHubActivity.this,"Xtream-Profil gespeichert · Playlist wird geladen",Toast.LENGTH_SHORT).show();String url=PortalUrlBuilder.xtreamPlaylist(p);if(!url.isEmpty())importRemoteM3u(url);}else Toast.makeText(MediaHubActivity.this,"Format: Server URL | Benutzer | Passwort",Toast.LENGTH_SHORT).show();}})
                .setNegativeButton("Abbrechen",null).show();
        }

        @Override public boolean onKeyDown(int k,KeyEvent e){
            if(!showingPlaylist&&"VIDEO".equals(mode)&&k==KeyEvent.KEYCODE_MENU&&focus==1){addVideoUrlFavorite();return true;}
            if(showingPlaylist){
                if(k==KeyEvent.KEYCODE_DPAD_DOWN)streamFocus=Math.min(playlistNames.size()-1,streamFocus+1);
                else if(k==KeyEvent.KEYCODE_DPAD_UP)streamFocus=Math.max(0,streamFocus-1);
                else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)openStream();
                else if(k==KeyEvent.KEYCODE_MENU&&!playlistUrls.isEmpty()){String url=playlistUrls.get(streamFocus).trim();String cat="PORTALE".equals(mode)?"portals":"livetv";FavoriteStore.add(MediaHubActivity.this,cat,playlistNames.get(streamFocus)+" | "+url);Toast.makeText(MediaHubActivity.this,"Favorit gespeichert",Toast.LENGTH_SHORT).show();}
                else if(k==KeyEvent.KEYCODE_BACK){showingPlaylist=false;invalidate();return true;} else return super.onKeyDown(k,e);
                invalidate();return true;
            }
            if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(items.length-1,focus+1);
            else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)choose();
            else if(k==KeyEvent.KEYCODE_BACK){finish();return true;} else return super.onKeyDown(k,e);
            invalidate();return true;
        }
    }
    void parseM3u(Uri u){ playlistNames.clear(); playlistUrls.clear(); try(BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(u)))){String line,name=null; while((line=br.readLine())!=null){line=line.trim(); if(line.startsWith("#EXTINF:")){int comma=line.indexOf(","); name=comma>=0?line.substring(comma+1).trim():"STREAM";} else if(!line.isEmpty()&&!line.startsWith("#")){playlistNames.add(name==null?"STREAM "+(playlistNames.size()+1):name);playlistUrls.add(line);name=null;}} showingPlaylist=!playlistUrls.isEmpty(); streamFocus=0; Toast.makeText(this,playlistUrls.size()+" Streams eingelesen",Toast.LENGTH_SHORT).show(); invalidateHub();}catch(Exception e){Toast.makeText(this,"Playlist konnte nicht gelesen werden",Toast.LENGTH_SHORT).show();}}

    void invalidateHub(){ if(hubView!=null)hubView.invalidate(); }

    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);
        if(req==REQ_M3U&&res==RESULT_OK&&data!=null&&data.getData()!=null){parseM3u(data.getData());return;}
        if(req==REQ_VIDEO&&res==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri u=data.getData();Intent play=new Intent(Intent.ACTION_VIEW);play.setDataAndType(u,"video/*");play.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try{startActivity(play);}catch(Exception e){Toast.makeText(this,"Kein Videoplayer installiert",Toast.LENGTH_SHORT).show();}
        }}
}
