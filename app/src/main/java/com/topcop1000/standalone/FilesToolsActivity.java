package com.topcop1000.standalone;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
   items="DATEIEN".equals(mode)?new String[]{"DATEI ÖFFNEN","DOWNLOADS","LIVE LINES","ZURÜCK"}:new String[]{"DIAGNOSE","PY REPARATUR","RUNTIME CENTER","ZURÜCK"};
   setFocusable(true);requestFocus();}
  @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(5,5,8));int w=getWidth(),h=getHeight();p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.065f);c.drawText(mode,w/2f,h*.14f,p);
   for(int i=0;i<items.length;i++){float y=h*.27f+i*h*.13f;RectF r=new RectF(w*.18f,y,w*.82f,y+h*.09f);p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,22,22,p);p.setColor(Color.WHITE);p.setTextSize(h*.034f);c.drawText(items[i],r.centerX(),r.centerY()+10,p);}}
  void choose(){
   if(focus==items.length-1){finish();return;}
   if("DATEIEN".equals(mode)){
    if(focus==0){pick(30,"*/*");return;}
    if(focus==1){
     Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);
     if(android.os.Build.VERSION.SDK_INT>=26)i.putExtra("android.provider.extra.INITIAL_URI",Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload"));
     try{startActivityForResult(i,31);}catch(Exception e){Toast.makeText(FilesToolsActivity.this,"Downloads konnten nicht geöffnet werden",Toast.LENGTH_SHORT).show();}
     return;
    }
    if(focus==2){pick(32,"text/*");return;}
   }
   if(focus==0){showDiagnosis();return;}
   if(focus==1){Intent i=getPackageManager().getLaunchIntentForPackage("ru.iiec.pydroid3");if(i!=null)startActivity(i);else Toast.makeText(FilesToolsActivity.this,"Pydroid ist nicht installiert",Toast.LENGTH_SHORT).show();return;}
   if(focus==2){showRuntime();return;}
  }
  void pick(int req,String type){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(type);i.addCategory(Intent.CATEGORY_OPENABLE);try{startActivityForResult(i,req);}catch(Exception e){Toast.makeText(FilesToolsActivity.this,"Dateiauswahl nicht verfügbar",Toast.LENGTH_SHORT).show();}}
  void showDiagnosis(){StatFs s=new StatFs(getFilesDir().getAbsolutePath());long free=s.getAvailableBytes()/1024/1024;String msg="Android "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\nGerät: "+Build.MANUFACTURER+" "+Build.MODEL+"\nFreier App-Speicher: "+free+" MB";new android.app.AlertDialog.Builder(FilesToolsActivity.this).setTitle("DIAGNOSE").setMessage(msg).setPositiveButton("OK",null).show();}
  void showRuntime(){String msg="Java: "+System.getProperty("java.version")+"\nVM: "+System.getProperty("java.vm.name")+"\nABI: "+java.util.Arrays.toString(Build.SUPPORTED_ABIS);new android.app.AlertDialog.Builder(FilesToolsActivity.this).setTitle("RUNTIME CENTER").setMessage(msg).setPositiveButton("OK",null).show();}
  @Override public boolean onKeyDown(int k,KeyEvent e){if(k==KeyEvent.KEYCODE_DPAD_DOWN)focus=Math.min(items.length-1,focus+1);else if(k==KeyEvent.KEYCODE_DPAD_UP)focus=Math.max(0,focus-1);else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)choose();else if(k==KeyEvent.KEYCODE_BACK){finish();return true;}else return super.onKeyDown(k,e);invalidate();return true;}
 }
 @Override protected void onActivityResult(int r,int result,Intent data){super.onActivityResult(r,result,data);if((r==30||r==31||r==32)&&result==RESULT_OK&&data!=null&&data.getData()!=null){Uri u=data.getData();if(r==32){Intent hub=new Intent(this,MediaHubActivity.class);hub.putExtra(MediaHubActivity.EXTRA_MODE,"LIVE LINES");hub.putExtra("playlist_uri",u.toString());startActivity(hub);return;}Intent open=new Intent(Intent.ACTION_VIEW);String mime=getContentResolver().getType(u);open.setDataAndType(u,mime==null?"*/*":mime);open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{startActivity(open);}catch(Exception e){Toast.makeText(this,"Keine passende App zum Öffnen der Datei",Toast.LENGTH_SHORT).show();}}}
}
