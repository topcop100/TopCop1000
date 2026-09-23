package com.topcop1000.standalone;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import java.util.*;

public class FavoritesActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(new FavView());}
 final class FavView extends View{
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  final String[] categories={"MP3 STREAMS","VIDEO","LIVE LINES","PORTALE (ALT)"};
  int category=0, focus=0; ArrayList<String> entries=new ArrayList<>();
  FavView(){super(FavoritesActivity.this);String wanted=getIntent().getStringExtra("category");if("video".equals(wanted))category=1;else if("livetv".equals(wanted))category=2;else if("portals".equals(wanted))category=3;reload();setFocusable(true);requestFocus();}
  String key(){return category==0?"mp3":category==1?"video":category==2?"livetv":"portals";}
  void reload(){entries=FavoriteStore.list(FavoritesActivity.this,key());Collections.sort(entries,String.CASE_INSENSITIVE_ORDER);focus=Math.min(focus,Math.max(0,entries.size()-1));invalidate();}
  @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(5,5,8));int w=getWidth(),h=getHeight();p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);
   p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.06f);c.drawText("FAVORITEN · "+categories[category],w/2f,h*.12f,p);
   if(entries.isEmpty()){p.setColor(Color.LTGRAY);p.setTextSize(h*.035f);c.drawText("NOCH KEINE FAVORITEN",w/2f,h*.42f,p);}
   int first=Math.max(0,Math.min(focus-3,Math.max(0,entries.size()-6)));
   for(int row=0;row<6&&first+row<entries.size();row++){int i=first+row;float y=h*.20f+row*h*.105f;RectF r=new RectF(w*.14f,y,w*.86f,y+h*.075f);p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,20,20,p);p.setColor(Color.WHITE);p.setTextSize(h*.028f);String s=entries.get(i);if(s.length()>55)s=s.substring(0,52)+"...";c.drawText(s,r.centerX(),r.centerY()+9,p);}
   p.setTextSize(h*.025f);p.setColor(Color.GRAY);c.drawText("OK öffnet   ·   ◀ ▶ Kategorie   ·   MENU löscht   ·   BACK zurück",w/2f,h*.94f,p);
  }
  void openFavorite(String value){int sep=value.indexOf(" | ");if(sep<0){Toast.makeText(FavoritesActivity.this,"Favorit enthält keine Quelle",Toast.LENGTH_SHORT).show();return;}String url=value.substring(sep+3).trim();if((category==2||category==3)&&isStalkerCommand(url)){Toast.makeText(FavoritesActivity.this,"Stalker-Favorit wird über LIVE LINES geöffnet",Toast.LENGTH_SHORT).show();android.content.Intent hub=new android.content.Intent(FavoritesActivity.this,MediaHubActivity.class);hub.putExtra(MediaHubActivity.EXTRA_MODE,"LIVE LINES");hub.putExtra("favorite_stalker_cmd",url);startActivity(hub);return;}try{
   android.net.Uri uri=android.net.Uri.parse(url);
   android.content.Intent i=new android.content.Intent(android.content.Intent.ACTION_VIEW);
   if(url.startsWith("content://")){String mime=getContentResolver().getType(uri);i.setDataAndType(uri,mime==null?"*/*":mime);i.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);}
   else i.setData(uri);
   startActivity(i);
  }catch(Exception e){Toast.makeText(FavoritesActivity.this,"Favorit kann nicht geöffnet werden",Toast.LENGTH_SHORT).show();}}
  boolean isStalkerCommand(String value){String v=value==null?"":value.trim().toLowerCase(java.util.Locale.ROOT);return v.startsWith("ffmpeg ")||v.startsWith("auto ")||v.startsWith("ffrt ")||v.startsWith("ffmpeg://");}
  @Override public boolean onKeyDown(int k,KeyEvent e){
   if(k==KeyEvent.KEYCODE_DPAD_RIGHT){category=(category+1)%categories.length;focus=0;reload();}
   else if(k==KeyEvent.KEYCODE_DPAD_LEFT){category=(category+categories.length-1)%categories.length;focus=0;reload();}
   else if(k==KeyEvent.KEYCODE_DPAD_DOWN&&focus<entries.size()-1)focus++;
   else if(k==KeyEvent.KEYCODE_DPAD_UP&&focus>0)focus--;
   else if((k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)&&!entries.isEmpty()){openFavorite(entries.get(focus));}
   else if(k==KeyEvent.KEYCODE_MENU&&!entries.isEmpty()){String v=entries.get(focus);FavoriteStore.remove(FavoritesActivity.this,key(),v);Toast.makeText(FavoritesActivity.this,"Favorit entfernt",Toast.LENGTH_SHORT).show();reload();}
   else if(k==KeyEvent.KEYCODE_BACK){finish();return true;}
   else return super.onKeyDown(k,e);invalidate();return true;
  }
 }
}
