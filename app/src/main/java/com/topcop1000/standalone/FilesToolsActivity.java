package com.topcop1000.standalone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import android.os.Build;
import android.os.StatFs;
import java.io.File;

public class FilesToolsActivity extends Activity {
 public static final String EXTRA_MODE="mode";
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(new CenterView());}
 final class CenterView extends View{
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); final String mode; final String[] items; int focus=0;
  CenterView(){super(FilesToolsActivity.this);mode=getIntent().getStringExtra(EXTRA_MODE);
   items="DATEIEN".equals(mode)?new String[]{"DATEI ÖFFNEN","DOWNLOADS","PORTALE","ZURÜCK"}:new String[]{"DIAGNOSE","PY REPARATUR","RUNTIME CENTER","ZURÜCK"};
   setFocusable(true);requestFocus();}
  @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(5,5,8));int w=getWidth(),h=getHeight();p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.065f);c.drawText(mode,w/2f,h*.14f,p);
   for(int i=0;i<items.length;i++){float y=h*.27f+i*h*.13f;RectF r=new RectF(w*.18f,y,w*.82f,y+h*.09f);p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,22,22,p);p.setColor(Color.WHITE);p.setTextSize(h*.034f);c.drawText(items[i],r.centerX(),r.centerY()+10,p);}}
  void choose(){if(focus==items.length-1){finish();return;} if("DATEIEN".equals(mode)&&focus==0){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,30);return;}Toast.makeText(FilesToolsActivity.this,items[focus]+" vorbereitet",Toast.LENGTH_SHORT).show();}
  @Override public boolean onKeyDown(int k,KeyEvent e){if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(items.length-1,focus+1);else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)choose();else if(k==KeyEvent.KEYCODE_BACK){finish();return true;}else return super.onKeyDown(k,e);invalidate();return true;}
 }
 @Override protected void onActivityResult(int r,int result,Intent data){super.onActivityResult(r,result,data);if(r==30&&result==RESULT_OK&&data!=null)Toast.makeText(this,"Datei ausgewählt",Toast.LENGTH_SHORT).show();}
}
