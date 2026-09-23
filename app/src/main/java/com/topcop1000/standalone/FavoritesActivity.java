package com.topcop1000.standalone;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;

public class FavoritesActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(new FavView());}
 final class FavView extends View{
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); final String[] rows={"MP3 FAVORITEN","VIDEO FAVORITEN","LIVE-TV FAVORITEN","PORTAL FAVORITEN","ZURÜCK"}; int focus=0;
  FavView(){super(FavoritesActivity.this);setFocusable(true);requestFocus();}
  @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(5,5,8));int w=getWidth(),h=getHeight();p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.065f);c.drawText("FAVORITEN",w/2f,h*.14f,p);for(int i=0;i<rows.length;i++){float y=h*.27f+i*h*.12f;RectF r=new RectF(w*.18f,y,w*.82f,y+h*.085f);p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,22,22,p);p.setColor(Color.WHITE);p.setTextSize(h*.033f);c.drawText(rows[i],r.centerX(),r.centerY()+10,p);}}
  @Override public boolean onKeyDown(int k,KeyEvent e){if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(rows.length-1,focus+1);else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);else if(k==KeyEvent.KEYCODE_BACK||(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)&&focus==rows.length-1){finish();return true;}else return super.onKeyDown(k,e);invalidate();return true;}
 }
}
