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

    @Override protected void onResume(){ super.onResume(); if(getWindow().getDecorView()!=null) getWindow().getDecorView().invalidate(); }

    final class GraveyardView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final String[] labels={"MP3 CENTER","VIDEO","LIVE TV","PORTALE","APPS","DATEIEN","TOOLS","EINSTELLUNGEN","FAVORITEN","RESERVE","EXIT"};
        final int[] order=new int[10];
        final int[][] grid={{0,1,2,3,4},{5,6,7,8,9}};
        int focus=0;
        boolean editMode=false;
        final boolean[] visible=new boolean[10];
        boolean zombies=true;
        long start=System.currentTimeMillis(), lastInput=System.currentTimeMillis();
        final android.content.SharedPreferences prefs=getSharedPreferences("topcop",MODE_PRIVATE);

        GraveyardView(){ super(MainActivity.this); zombies=prefs.getBoolean("zombies",true); loadOrder(); loadVisibility(); setFocusable(true); setFocusableInTouchMode(true); requestFocus(); }

        void loadOrder(){String raw=prefs.getString("tile_order","0,1,2,3,4,5,6,7,8,9");String[] a=raw.split(",");boolean ok=a.length==10;boolean[] seen=new boolean[10];if(ok)try{for(int i=0;i<10;i++){order[i]=Integer.parseInt(a[i]);if(order[i]<0||order[i]>9||seen[order[i]])ok=false;else seen[order[i]]=true;}}catch(Exception e){ok=false;}if(!ok)for(int i=0;i<10;i++)order[i]=i;}
        void loadVisibility(){for(int i=0;i<10;i++)visible[i]=prefs.getBoolean("tile_visible_"+i,true);}
        void resetTiles(){for(int i=0;i<10;i++){order[i]=i;visible[i]=true;}android.content.SharedPreferences.Editor e=prefs.edit().remove("tile_order");for(int i=0;i<10;i++)e.remove("tile_visible_"+i);e.apply();Toast.makeText(MainActivity.this,"Kacheln auf Standard zurückgesetzt",Toast.LENGTH_SHORT).show();}
        void toggleVisibility(){if(focus>=10)return;int id=tileAt(focus);if(id==9){Toast.makeText(MainActivity.this,"RESERVE bleibt als freier Platz verfügbar",Toast.LENGTH_SHORT).show();return;}visible[id]=!visible[id];prefs.edit().putBoolean("tile_visible_"+id,visible[id]).apply();Toast.makeText(MainActivity.this,visible[id]?labels[id]+" eingeblendet":labels[id]+" ausgeblendet",Toast.LENGTH_SHORT).show();}
        void saveOrder(){StringBuilder s=new StringBuilder();for(int i=0;i<10;i++){if(i>0)s.append(',');s.append(order[i]);}prefs.edit().putString("tile_order",s.toString()).apply();}
        int tileAt(int position){return position==10?10:order[position];}

        @Override protected void onDraw(Canvas c){
            zombies=prefs.getBoolean("zombies",true);
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
            boolean idle=prefs.getBoolean("idle",true) && System.currentTimeMillis()-lastInput>7000;
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
            int tile=tileAt(i); boolean shown=i==10||visible[tile];
            p.setColor(!shown?Color.rgb(18,18,22):(selected?Color.rgb(88,12,24):Color.rgb(39,39,46))); c.drawRoundRect(r,28,28,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(selected?6:3);
            p.setColor(selected?Color.rgb(255,25,70):Color.rgb(100,100,112)); c.drawRoundRect(r,28,28,p);
            p.setStyle(Paint.Style.FILL); p.setTextSize(Math.max(18,h*.027f)); p.setColor(Color.WHITE);
            if(editMode&&selected){p.setColor(Color.rgb(255,190,30));c.drawText("VERSCHIEBEN",r.centerX(),r.top+24,p);p.setColor(Color.WHITE);}
            c.drawText(labels[tile],r.centerX(),r.centerY()+8,p);
            if(!shown&&i<10){p.setTextSize(Math.max(13,h*.018f));p.setColor(Color.LTGRAY);c.drawText("AUSGEBLENDET",r.centerX(),r.bottom-18,p);}
            if(selected) drawBloodDrop(c,r.centerX(),r.top-20);
        }

        void drawBloodDrop(Canvas c,float x,float y){
            Path d=new Path(); d.moveTo(x,y-20); d.cubicTo(x-24,y+12,x-16,y+34,x,y+34);
            d.cubicTo(x+16,y+34,x+24,y+12,x,y-20); d.close();
            p.setColor(Color.rgb(195,0,32)); c.drawPath(d,p);
        }

        void moveEdit(int dx,int dy){
            if(focus>=10)return;
            int target=focus;
            int row=focus/5,col=focus%5;
            if(dx<0&&col>0)target=focus-1; else if(dx>0&&col<4)target=focus+1;
            else if(dy<0&&row==1)target=focus-5; else if(dy>0&&row==0)target=focus+5;
            if(target!=focus){int tmp=order[focus];order[focus]=order[target];order[target]=tmp;focus=target;saveOrder();}
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
            if(i==1||i==2||i==3){ Intent x=new Intent(MainActivity.this,MediaHubActivity.class); x.putExtra(MediaHubActivity.EXTRA_MODE,i==1?"VIDEO":(i==2?"LIVE TV":"PORTALE")); startActivity(x); return; }
            if(i==4){ startActivity(new Intent(MainActivity.this,AppsActivity.class)); return; }
            if(i==5||i==6){ Intent x=new Intent(MainActivity.this,FilesToolsActivity.class); x.putExtra(FilesToolsActivity.EXTRA_MODE,i==5?"DATEIEN":"TOOLS"); startActivity(x); return; }
            if(i==7){ startActivity(new Intent(MainActivity.this,SettingsActivity.class)); return; }
            if(i==8){ startActivity(new Intent(MainActivity.this,FavoritesActivity.class)); return; }
            if(i==9){ Toast.makeText(MainActivity.this,"RESERVE – frei für spätere Funktion",Toast.LENGTH_SHORT).show(); return; }
            String name=labels[i];
            Toast.makeText(MainActivity.this,name+" vorbereitet",Toast.LENGTH_SHORT).show();
        }

        @Override public boolean onKeyDown(int key,KeyEvent e){
            lastInput=System.currentTimeMillis();
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT){if(editMode)moveEdit(1,0);else move(1,0);}
            else if(key==KeyEvent.KEYCODE_DPAD_LEFT){if(editMode)moveEdit(-1,0);else move(-1,0);}
            else if(key==KeyEvent.KEYCODE_DPAD_DOWN){if(editMode)moveEdit(0,1);else move(0,1);}
            else if(key==KeyEvent.KEYCODE_DPAD_UP){if(editMode)moveEdit(0,-1);else move(0,-1);}
            else if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){
                if(focus==10) finish();
                else if(editMode) toggleVisibility();
                else if(visible[tileAt(focus)]) openTile(tileAt(focus));
                else Toast.makeText(MainActivity.this,"Kachel ist ausgeblendet · MENU zum Bearbeiten",Toast.LENGTH_SHORT).show();
            } else if(key==KeyEvent.KEYCODE_MENU){editMode=!editMode;if(focus==10)focus=9;Toast.makeText(MainActivity.this,editMode?"BEARBEITEN: D-Pad verschiebt · OK blendet ein/aus":"Bearbeitungsmodus beendet",Toast.LENGTH_SHORT).show();}
            else if(editMode && key==KeyEvent.KEYCODE_BUTTON_X){resetTiles();focus=0;}
            else if(key==KeyEvent.KEYCODE_BACK){if(editMode){editMode=false;Toast.makeText(MainActivity.this,"Bearbeitungsmodus beendet",Toast.LENGTH_SHORT).show();}else finish(); }
            else return super.onKeyDown(key,e);
            invalidate(); return true;
        }
    }
}
