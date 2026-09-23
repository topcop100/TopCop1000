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
    int streamFocus=0; boolean showingPlaylist=false;
    final ArrayList<String> playlistNames=new ArrayList<>(), playlistUrls=new ArrayList<>();
    public static final String EXTRA_MODE="mode";
    @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(new HubView());}
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
        void openStream(){ if(playlistUrls.isEmpty())return; String url=playlistUrls.get(streamFocus); FavoriteStore.add(MediaHubActivity.this,"livetv",playlistNames.get(streamFocus)+" | "+url); Intent v=new Intent(Intent.ACTION_VIEW,Uri.parse(url)); try{startActivity(v);}catch(Exception e){Toast.makeText(MediaHubActivity.this,"Kein kompatibler Stream-Player installiert",Toast.LENGTH_SHORT).show();} }

        void choose(){
            if(focus==items.length-1){finish();return;}
            if(("LIVE TV".equals(mode)||"PORTALE".equals(mode))&&focus==0){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQ_M3U);return; }
            if("VIDEO".equals(mode)&&focus==0){
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("video/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQ_VIDEO);return;
            }
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
        void importRemoteM3u(String url){
            Toast.makeText(MediaHubActivity.this,"Xtream Playlist wird geladen",Toast.LENGTH_SHORT).show();
            new Thread(()->{
                playlistNames.clear();playlistUrls.clear();
                try{
                    HttpURLConnection con=(HttpURLConnection)new URL(url).openConnection();con.setConnectTimeout(8000);con.setReadTimeout(12000);con.setInstanceFollowRedirects(true);
                    try(BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){
                        String line,name=null;while((line=br.readLine())!=null){line=line.trim();if(line.startsWith("#EXTINF:")){int comma=line.indexOf(",");name=comma>=0?line.substring(comma+1).trim():"STREAM";}else if(!line.isEmpty()&&!line.startsWith("#")){playlistNames.add(name==null?"STREAM "+(playlistNames.size()+1):name);playlistUrls.add(line);name=null;}}
                    } finally {con.disconnect();}
                    runOnUiThread(()->{showingPlaylist=!playlistUrls.isEmpty();streamFocus=0;Toast.makeText(MediaHubActivity.this,playlistUrls.size()+" Xtream Streams geladen",Toast.LENGTH_SHORT).show();invalidate();});
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
                    runOnUiThread(()->Toast.makeText(MediaHubActivity.this,ok?"Stalker Portal erreichbar":"Stalker antwortet nicht korrekt",Toast.LENGTH_SHORT).show());
                }catch(Exception e){runOnUiThread(()->Toast.makeText(MediaHubActivity.this,"Stalker Verbindung fehlgeschlagen",Toast.LENGTH_SHORT).show());}
                finally{if(con!=null)con.disconnect();}
            }).start();
        }

        void askStalker(){
            final EditText input=new EditText(MediaHubActivity.this); input.setHint("Server URL | MAC");
            new AlertDialog.Builder(MediaHubActivity.this).setTitle("Stalker / MAC").setView(input)
                .setPositiveButton("Speichern",(d,w)->{String[] a=input.getText().toString().trim().split("\\|",2); if(a.length==2){PortalProfile p=new PortalProfile(PortalProfile.Type.STALKER,"Stalker",a[0].trim(),"",a[1].trim()); if(p.isValid()){getSharedPreferences("topcop_portals",MODE_PRIVATE).edit().putString("stalker_server",p.server).putString("stalker_mac",p.secret).apply();Toast.makeText(MediaHubActivity.this,"Stalker-Profil gespeichert",Toast.LENGTH_SHORT).show();}}})
                .setNegativeButton("Abbrechen",null).show();
        }
        void askXtream(){
            final EditText input=new EditText(MediaHubActivity.this); input.setHint("Server URL | Benutzer | Passwort");
            new AlertDialog.Builder(MediaHubActivity.this).setTitle("Xtream").setView(input)
                .setPositiveButton("Speichern",(d,w)->{String[] a=input.getText().toString().trim().split("\\|",3); if(a.length==3){PortalProfile p=new PortalProfile(PortalProfile.Type.XTREAM,"Xtream",a[0].trim(),a[1].trim(),a[2].trim()); if(p.isValid()){getSharedPreferences("topcop_portals",MODE_PRIVATE).edit().putString("xtream_server",p.server).putString("xtream_user",p.user).putString("xtream_secret",p.secret).apply();Toast.makeText(MediaHubActivity.this,"Xtream-Profil gespeichert",Toast.LENGTH_SHORT).show();}}})
                .setNegativeButton("Abbrechen",null).show();
        }

        @Override public boolean onKeyDown(int k,KeyEvent e){
            if(showingPlaylist){
                if(k==KeyEvent.KEYCODE_DPAD_DOWN)streamFocus=Math.min(playlistNames.size()-1,streamFocus+1);
                else if(k==KeyEvent.KEYCODE_DPAD_UP)streamFocus=Math.max(0,streamFocus-1);
                else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)openStream();
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

    void invalidateHub(){ View v=findViewById(android.R.id.content); if(v!=null)v.invalidate(); }

    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);
        if(req==REQ_M3U&&res==RESULT_OK&&data!=null&&data.getData()!=null){parseM3u(data.getData());return;}
        if(req==REQ_VIDEO&&res==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri u=data.getData();Intent play=new Intent(Intent.ACTION_VIEW);play.setDataAndType(u,"video/*");play.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try{startActivity(play);}catch(Exception e){Toast.makeText(this,"Kein Videoplayer installiert",Toast.LENGTH_SHORT).show();}
        }}
}
