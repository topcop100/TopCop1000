package com.topcop1000.standalone;

import android.app.Activity;
import android.graphics.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import java.io.IOException;

public class Mp3CenterActivity extends Activity {
    MediaPlayer player;
    @Override public void onCreate(Bundle b){ super.onCreate(b); setContentView(new Mp3View()); }
    @Override protected void onDestroy(){ if(player!=null){player.release();player=null;} super.onDestroy(); }

    void playUrl(String url){
        if(url==null||url.trim().isEmpty()){ Toast.makeText(this,"Noch kein Stream hinterlegt",Toast.LENGTH_SHORT).show(); return; }
        try{
            if(player!=null) player.release();
            player=new MediaPlayer(); player.setDataSource(url); player.setOnPreparedListener(MediaPlayer::start);
            player.setOnErrorListener((m,w,e)->{Toast.makeText(this,"Stream konnte nicht gestartet werden",Toast.LENGTH_SHORT).show();return true;});
            player.prepareAsync();
        }catch(IOException e){ Toast.makeText(this,"Ungültige Audioquelle",Toast.LENGTH_SHORT).show(); }
    }

    final class Mp3View extends View{
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); int focus=0;
        final String[] items={"STREAM 1","STREAM 2","LOKALE MUSIK","FAVORITEN","STOP","ZURÜCK"};
        Mp3View(){super(Mp3CenterActivity.this);setFocusable(true);requestFocus();}
        @Override protected void onDraw(Canvas c){
            c.drawColor(Color.rgb(5,5,8)); p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(getHeight()*.07f);p.setColor(Color.rgb(255,30,80));c.drawText("MP3 CENTER",getWidth()/2f,getHeight()*.14f,p);
            float y=getHeight()*.27f;
            for(int i=0;i<items.length;i++){p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));RectF r=new RectF(getWidth()*.18f,y+i*getHeight()*.105f,getWidth()*.82f,y+i*getHeight()*.105f+getHeight()*.075f);c.drawRoundRect(r,20,20,p);p.setTextSize(getHeight()*.035f);p.setColor(Color.WHITE);c.drawText(items[i],r.centerX(),r.centerY()+10,p);}
        }
        @Override public boolean onKeyDown(int k,KeyEvent e){
            if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(items.length-1,focus+1);
            else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);
            else if(k==KeyEvent.KEYCODE_BACK){finish();return true;}
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER){
                if(focus==4&&player!=null){player.stop();player.release();player=null;}
                else if(focus==5)finish();
                else Toast.makeText(Mp3CenterActivity.this,items[focus]+" vorbereitet",Toast.LENGTH_SHORT).show();
            }else return super.onKeyDown(k,e); invalidate();return true;
        }
    }
}
