package com.topcop1000.standalone;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new GraveyardView());
    }

    final class GraveyardView extends View {
        final Paint p = new Paint(3);
        final String[] labels = {"MP3 CENTER","VIDEO","LIVE TV","PORTALE","APPS","DATEIEN","TOOLS","EINSTELLUNGEN","FAVORITEN","RESERVE","EXIT"};
        int focus = 0;
        boolean zombies = true;
        long start = System.currentTimeMillis();

        GraveyardView() { super(MainActivity.this); setFocusable(true); requestFocus(); }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            int w=getWidth(), h=getHeight();
            c.drawColor(Color.rgb(5,5,8));
            drawMoon(c,w,h);
            if(zombies) drawZombies(c,w,h);
            p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.DEFAULT_BOLD);
            for(int i=0;i<11;i++) drawStone(c,i,w,h);
            postInvalidateDelayed(80);
        }

        void drawMoon(Canvas c,int w,int h){
            p.setColor(Color.rgb(45,45,55)); c.drawCircle(w*.82f,h*.18f,h*.10f,p);
        }

        void drawZombies(Canvas c,int w,int h){
            float t=((System.currentTimeMillis()-start)%22000)/22000f;
            for(int i=0;i<3;i++){
                float x=((t+i*.37f)%1.15f)*w-w*.08f;
                float y=h*(.70f+i*.045f);
                p.setColor(Color.rgb(25,55,35));
                c.drawCircle(x,y-42,15,p); c.drawRect(x-10,y-28,x+10,y+18,p);
                p.setStrokeWidth(7); c.drawLine(x-6,y+15,x-20,y+48,p); c.drawLine(x+6,y+15,x+18,y+48,p);
                c.drawLine(x-8,y-12,x-28,y+4,p); c.drawLine(x+8,y-12,x+30,y-2,p);
            }
        }

        void drawStone(Canvas c,int i,int w,int h){
            int cols=6; float gap=w*.018f, sw=(w-gap*7)/6f, sh=h*.25f;
            int row=i/cols,col=i%cols;
            float x=gap+col*(sw+gap), y=h*.27f+row*(sh+h*.10f);
            if(i==10){ x=w-sw-gap; y=h-sh-gap; }
            RectF r=new RectF(x,y,x+sw,y+sh);
            p.setColor(i==focus?Color.rgb(95,15,25):Color.rgb(42,42,48)); c.drawRoundRect(r,26,26,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(i==focus?6:3);
            p.setColor(i==focus?Color.rgb(255,20,65):Color.rgb(110,110,120)); c.drawRoundRect(r,26,26,p); p.setStyle(Paint.Style.FILL);
            p.setTextSize(Math.max(18,h*.028f)); p.setColor(Color.WHITE); c.drawText(labels[i],r.centerX(),r.centerY()+8,p);
            if(i==focus) drawBloodDrop(c,r.centerX(),y-18);
        }

        void drawBloodDrop(Canvas c,float x,float y){
            Path d=new Path(); d.moveTo(x,y-18); d.cubicTo(x-25,y+14,x-16,y+34,x,y+34); d.cubicTo(x+16,y+34,x+25,y+14,x,y-18); d.close();
            p.setColor(Color.rgb(190,0,30)); c.drawPath(d,p);
        }

        @Override public boolean onKeyDown(int key, KeyEvent e){
            int row=focus/6,col=focus%6;
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT) focus=Math.min(10,focus+1);
            else if(key==KeyEvent.KEYCODE_DPAD_LEFT) focus=Math.max(0,focus-1);
            else if(key==KeyEvent.KEYCODE_DPAD_DOWN) focus=Math.min(10,focus+6);
            else if(key==KeyEvent.KEYCODE_DPAD_UP) focus=Math.max(0,focus-6);
            else if(key==KeyEvent.KEYCODE_DPAD_CENTER || key==KeyEvent.KEYCODE_ENTER){
                if(focus==10) finish();
                else if(focus==7) zombies=!zombies;
            } else return super.onKeyDown(key,e);
            invalidate(); return true;
        }
    }
}
