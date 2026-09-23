package com.topcop1000.standalone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

public class MediaHubActivity extends Activity {
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
            p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.07f);c.drawText(mode,w/2f,h*.15f,p);
            for(int i=0;i<items.length;i++){float y=h*.28f+i*h*.12f;RectF r=new RectF(w*.18f,y,w*.82f,y+h*.085f);
                p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,22,22,p);
                p.setColor(Color.WHITE);p.setTextSize(h*.035f);c.drawText(items[i],r.centerX(),r.centerY()+10,p);}
        }
        void choose(){
            if(focus==items.length-1){finish();return;}
            if("VIDEO".equals(mode)&&focus==0){
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("video/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,20);return;
            }
            Toast.makeText(MediaHubActivity.this,items[focus]+" vorbereitet",Toast.LENGTH_SHORT).show();
        }
        @Override public boolean onKeyDown(int k,KeyEvent e){
            if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(items.length-1,focus+1);
            else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)choose();
            else if(k==KeyEvent.KEYCODE_BACK){finish();return true;} else return super.onKeyDown(k,e);
            invalidate();return true;
        }
    }
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);
        if(req==20&&res==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri u=data.getData();Intent play=new Intent(Intent.ACTION_VIEW);play.setDataAndType(u,"video/*");play.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try{startActivity(play);}catch(Exception e){Toast.makeText(this,"Kein Videoplayer installiert",Toast.LENGTH_SHORT).show();}
        }}
}
