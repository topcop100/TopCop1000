package com.topcop1000.standalone;

import android.app.Activity;
import android.graphics.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.Toast;
import android.widget.EditText;
import android.app.AlertDialog;
import java.io.IOException;

public class Mp3CenterActivity extends Activity {
    static final int REQ_AUDIO=40;
    MediaPlayer player;
    final String[] streamKeys={"mp3_stream_1","mp3_stream_2"};
    @Override public void onCreate(Bundle b){ super.onCreate(b); setContentView(new Mp3View()); }
    @Override protected void onDestroy(){ if(player!=null){player.release();player=null;} super.onDestroy(); }

    void playSavedStream(int slot){ String url=getSharedPreferences("topcop",MODE_PRIVATE).getString(streamKeys[slot],""); playUrl(url); }

    void playUrl(String url){
        if(url==null||url.trim().isEmpty()){ Toast.makeText(this,"Noch kein Stream hinterlegt",Toast.LENGTH_SHORT).show(); return; }
        try{
            if(player!=null) player.release();
            player=new MediaPlayer(); player.setDataSource(url); player.setOnPreparedListener(MediaPlayer::start);
            player.setOnErrorListener((m,w,e)->{Toast.makeText(this,"Stream konnte nicht gestartet werden",Toast.LENGTH_SHORT).show();return true;});
            player.prepareAsync();
        }catch(IOException e){ Toast.makeText(this,"Ungültige Audioquelle",Toast.LENGTH_SHORT).show(); }
    }


    void configureStream(int slot){
        final SharedPreferences prefs=getSharedPreferences("topcop",MODE_PRIVATE);
        final EditText input=new EditText(this); input.setHint("https://..."); input.setText(prefs.getString(streamKeys[slot],""));
        new AlertDialog.Builder(this).setTitle("MP3 STREAM "+(slot+1)+" BEARBEITEN").setView(input)
            .setPositiveButton("Speichern",(d,w)->{String url=input.getText().toString().trim();if(!(url.startsWith("http://")||url.startsWith("https://"))){Toast.makeText(this,"Ungültige Stream-Adresse",Toast.LENGTH_SHORT).show();return;}prefs.edit().putString(streamKeys[slot],url).apply();Toast.makeText(this,"Stream gespeichert",Toast.LENGTH_SHORT).show();})
            .setNegativeButton("Abbrechen",null).show();
    }

    void addCurrentFavorite(int slot){
        String url=getSharedPreferences("topcop",MODE_PRIVATE).getString(streamKeys[slot],"").trim();
        if(url.isEmpty()){Toast.makeText(this,"Noch kein Stream hinterlegt",Toast.LENGTH_SHORT).show();return;}
        FavoriteStore.add(this,"mp3","STREAM "+(slot+1)+" | "+url);
        Toast.makeText(this,"MP3-Favorit gespeichert",Toast.LENGTH_SHORT).show();
    }

    @Override protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);
        if(req==REQ_AUDIO&&res==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri u=data.getData();
            try{
                if(player!=null)player.release();
                player=new MediaPlayer();
                player.setDataSource(Mp3CenterActivity.this,u);
                player.setOnPreparedListener(MediaPlayer::start);
                player.prepareAsync();
                Toast.makeText(this,"Lokale Musik wird geladen",Toast.LENGTH_SHORT).show();
            }catch(Exception e){Toast.makeText(this,"Audiodatei konnte nicht geöffnet werden",Toast.LENGTH_SHORT).show();}
        }
    }

    final class Mp3View extends View{
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); int focus=0;
        final String[] items={"STREAM 1","STREAM 2","LOKALE MUSIK","FAVORITEN","STOP","ZURÜCK"};
        Mp3View(){super(Mp3CenterActivity.this);setFocusable(true);requestFocus();}
        @Override protected void onDraw(Canvas c){
            c.drawColor(Color.rgb(5,5,8)); p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);
            p.setTextSize(getHeight()*.07f);p.setColor(Color.rgb(255,30,80));c.drawText("MP3 STREAMS",getWidth()/2f,getHeight()*.14f,p);
            float y=getHeight()*.27f;
            for(int i=0;i<items.length;i++){p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));RectF r=new RectF(getWidth()*.18f,y+i*getHeight()*.105f,getWidth()*.82f,y+i*getHeight()*.105f+getHeight()*.075f);c.drawRoundRect(r,20,20,p);p.setTextSize(getHeight()*.035f);p.setColor(Color.WHITE);c.drawText(items[i],r.centerX(),r.centerY()+10,p);}
        }
        @Override public boolean onKeyDown(int k,KeyEvent e){
            if(k==KeyEvent.KEYCODE_MENU&&(focus==0||focus==1)){
                final int slot=focus;
                new AlertDialog.Builder(Mp3CenterActivity.this).setTitle("STREAM "+(slot+1))
                    .setItems(new String[]{"BEARBEITEN","ALS FAVORIT SPEICHERN"},(d,which)->{if(which==0)configureStream(slot);else addCurrentFavorite(slot);})
                    .show();return true;
            }
            if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(items.length-1,focus+1);
            else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);
            else if(k==KeyEvent.KEYCODE_BACK){finish();return true;}
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER){
                if(focus==0||focus==1){playSavedStream(focus);}
                else if(focus==4&&player!=null){player.stop();player.release();player=null;}
                else if(focus==2){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("audio/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQ_AUDIO); }
                else if(focus==3){Intent fav=new Intent(Mp3CenterActivity.this,FavoritesActivity.class);fav.putExtra("category","mp3");startActivity(fav);}
                else if(focus==5)finish();
            }else return super.onKeyDown(k,e); invalidate();return true;
        }
    }
}
