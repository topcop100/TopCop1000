package com.topcop1000.standalone;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.content.*;
import android.widget.Toast;
import android.view.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new GraveyardView());
    }

    final class GraveyardView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final String[] labels={"MP3 CENTER","VIDEO","LIVE TV","PORTALE","APPS","DATEIEN","TOOLS","EINSTELLUNGEN","FAVORITEN","RESERVE","EXIT"};
        final int[][] grid={{0,1,2,3,4},{5,6,7,8,9}};
        int focus=0;
        boolean zombies=true;
        long start=System.currentTimeMillis(), lastInput=System.currentTimeMillis();
        final android.content.SharedPreferences prefs=getSharedPreferences("topcop",MODE_PRIVATE);

        GraveyardView(){ super(MainActivity.this); zombies=prefs.getBoolean("zombies",true); setFocusable(true); setFocusableInTouchMode(true); requestFocus(); }

        @Override protected void onDraw(Canvas c){
            int w=getWidth(),h=getHeight();
            c.drawColor(Color.rgb(5,5,8));
            drawSky(c,w,h);
            if(zombies) drawZombies(c,w,h);
            p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextAlign(Paint.Align.CENTER);
            for(int i=0;i<11;i++) drawStone(c,i,w,h);
            postInvalidateDelayed(80);
        }

        void drawSky(Canvas c,int w,int h){
            p.setColor(Color.rgb(18,18,25)); c.drawRect(0,h*.62f,w,h,p);
            p.setColor(Color.rgb(55,55,65)); c.drawCircle(w*.82f,h*.16f,h*.09f,p);
        }

        void drawZombies(Canvas c,int w,int h){
            boolean idle=System.currentTimeMillis()-lastInput>7000;
            int count=idle?4:2;
            float speed=idle?1.35f:.65f;
            float t=(((System.currentTimeMillis()-start)*speed)%24000)/24000f;
            for(int i=0;i<count;i++){
                float x=((t+i*.29f)%1.15f)*w-w*.08f;
                float y=h*(.72f+(i%2)*.055f);
                p.setColor(Color.rgb(24+i*3,52+i*4,34));
                c.drawCircle(x,y-42,15,p); c.drawRect(x-10,y-28,x+10,y+18,p);
                p.setStrokeWidth(7);
                c.drawLine(x-6,y+15,x-20,y+48,p); c.drawLine(x+6,y+15,x+18,y+48,p);
                c.drawLine(x-8,y-12,x-28,y+4,p); c.drawLine(x+8,y-12,x+30,y-2,p);
            }
        }

        RectF stoneRect(int i,int w,int h){
            float gap=w*.028f, sw=(w-gap*6)/5f, sh=h*.22f;
            if(i==10) return new RectF(w-sw-gap,h-sh-gap,w-gap,h-gap);
            int row=i/5,col=i%5;
            float x=gap+col*(sw+gap), y=h*.25f+row*(sh+h*.105f);
            return new RectF(x,y,x+sw,y+sh);
        }

        void drawStone(Canvas c,int i,int w,int h){
            RectF r=stoneRect(i,w,h);
            boolean selected=i==focus;
            p.setColor(selected?Color.rgb(88,12,24):Color.rgb(39,39,46)); c.drawRoundRect(r,28,28,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(selected?6:3);
            p.setColor(selected?Color.rgb(255,25,70):Color.rgb(100,100,112)); c.drawRoundRect(r,28,28,p);
            p.setStyle(Paint.Style.FILL); p.setTextSize(Math.max(18,h*.027f)); p.setColor(Color.WHITE);
            c.drawText(labels[i],r.centerX(),r.centerY()+8,p);
            if(selected) drawBloodDrop(c,r.centerX(),r.top-20);
        }

        void drawBloodDrop(Canvas c,float x,float y){
            Path d=new Path(); d.moveTo(x,y-20); d.cubicTo(x-24,y+12,x-16,y+34,x,y+34);
            d.cubicTo(x+16,y+34,x+24,y+12,x,y-20); d.close();
            p.setColor(Color.rgb(195,0,32)); c.drawPath(d,p);
        }

        void move(int dx,int dy){
            if(focus==10){ if(dy<0) focus=9; else if(dx<0) focus=9; return; }
            int row=focus/5,col=focus%5;
            if(dx!=0){ col=Math.max(0,Math.min(4,col+dx)); focus=row*5+col; }
            else if(dy<0 && row==1) focus=col;
            else if(dy>0 && row==0) focus=5+col;
            else if(dy>0 && row==1) focus=10;
        }

        void openTile(int i){
            if(i==0){ startActivity(new Intent(MainActivity.this,Mp3CenterActivity.class)); return; }
            String name=labels[i];
            Toast.makeText(MainActivity.this,name+" vorbereitet",Toast.LENGTH_SHORT).show();
        }

        @Override public boolean onKeyDown(int key,KeyEvent e){
            lastInput=System.currentTimeMillis();
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT) move(1,0);
            else if(key==KeyEvent.KEYCODE_DPAD_LEFT) move(-1,0);
            else if(key==KeyEvent.KEYCODE_DPAD_DOWN) move(0,1);
            else if(key==KeyEvent.KEYCODE_DPAD_UP) move(0,-1);
            else if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){
                if(focus==10) finish();
                else if(focus==7){ zombies=!zombies; prefs.edit().putBoolean("zombies",zombies).apply(); Toast.makeText(MainActivity.this,"Zombie-Hintergrund: "+(zombies?"EIN":"AUS"),Toast.LENGTH_SHORT).show(); }
                else openTile(focus);
            } else if(key==KeyEvent.KEYCODE_BACK){ finish(); }
            else return super.onKeyDown(key,e);
            invalidate(); return true;
        }
    }
}
