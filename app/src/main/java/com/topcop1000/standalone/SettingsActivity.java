package com.topcop1000.standalone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;

public class SettingsActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(new SettingsView());}
 final class SettingsView extends View{
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); final SharedPreferences prefs=getSharedPreferences("topcop",MODE_PRIVATE); int focus=0;
  SettingsView(){super(SettingsActivity.this);setFocusable(true);requestFocus();}
  String[] rows(){return new String[]{"ZOMBIES: "+(prefs.getBoolean("zombies",true)?"EIN":"AUS"),"IDLE-MODUS: "+(prefs.getBoolean("idle",true)?"EIN":"AUS"),"KACHELN ZURÜCKSETZEN","EINSTELLUNGEN ZURÜCKSETZEN","ZURÜCK"};}
  @Override protected void onDraw(Canvas c){String[] a=rows();c.drawColor(Color.rgb(5,5,8));int w=getWidth(),h=getHeight();p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.065f);c.drawText("EINSTELLUNGEN",w/2f,h*.14f,p);for(int i=0;i<a.length;i++){float y=h*.28f+i*h*.13f;RectF r=new RectF(w*.18f,y,w*.82f,y+h*.09f);p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,22,22,p);p.setColor(Color.WHITE);p.setTextSize(h*.033f);c.drawText(a[i],r.centerX(),r.centerY()+10,p);}}
  void choose(){if(focus==0)prefs.edit().putBoolean("zombies",!prefs.getBoolean("zombies",true)).apply();else if(focus==1)prefs.edit().putBoolean("idle",!prefs.getBoolean("idle",true)).apply();else if(focus==2){SharedPreferences.Editor ed=prefs.edit().remove("tile_order");for(int i=0;i<10;i++)ed.remove("tile_visible_"+i);ed.apply();}else if(focus==3){boolean z=prefs.getBoolean("zombies",true),idle=prefs.getBoolean("idle",true);prefs.edit().clear().putBoolean("zombies",z).putBoolean("idle",idle).apply();}else finish();invalidate();}
  @Override public boolean onKeyDown(int k,KeyEvent e){if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(4,focus+1);else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)choose();else if(k==KeyEvent.KEYCODE_BACK){finish();return true;}else return super.onKeyDown(k,e);invalidate();return true;}
 }
}
