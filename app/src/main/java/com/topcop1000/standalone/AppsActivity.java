package com.topcop1000.standalone;

import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import java.util.*;

public class AppsActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(new AppView());}
 final class AppView extends View{
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); final ArrayList<String> names=new ArrayList<>(),pkgs=new ArrayList<>(); int focus=0;
  AppView(){super(AppsActivity.this); load(); setFocusable(true);requestFocus();}
  void load(){Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);
   for(android.content.pm.ResolveInfo r:getPackageManager().queryIntentActivities(q,0)){String pkg=r.activityInfo.packageName;if(pkg.equals(getPackageName()))continue;names.add(r.loadLabel(getPackageManager()).toString());pkgs.add(pkg);}
   ArrayList<Integer> idx=new ArrayList<>();for(int i=0;i<names.size();i++)idx.add(i);Collections.sort(idx,(a,b)->names.get(a).compareToIgnoreCase(names.get(b)));
   ArrayList<String> n=new ArrayList<>(),k=new ArrayList<>();for(int i:idx){n.add(names.get(i));k.add(pkgs.get(i));}names.clear();names.addAll(n);pkgs.clear();pkgs.addAll(k);
  }
  @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(5,5,8));int w=getWidth(),h=getHeight();p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.rgb(255,30,80));p.setTextSize(h*.065f);c.drawText("APPS",w/2f,h*.12f,p);
   int first=Math.max(0,Math.min(focus-3,Math.max(0,names.size()-7)));for(int row=0;row<7&&first+row<names.size();row++){int i=first+row;float y=h*.19f+row*h*.105f;RectF r=new RectF(w*.16f,y,w*.84f,y+h*.075f);p.setColor(i==focus?Color.rgb(120,10,35):Color.rgb(42,42,50));c.drawRoundRect(r,20,20,p);p.setColor(Color.WHITE);p.setTextSize(h*.03f);c.drawText(names.get(i),r.centerX(),r.centerY()+9,p);}}
  @Override public boolean onKeyDown(int key,KeyEvent e){if(key==KeyEvent.KEYCODE_DPAD_DOWN&&focus<names.size()-1)focus++;else if(key==KeyEvent.KEYCODE_DPAD_UP&&focus>0)focus--;else if((key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)&&!pkgs.isEmpty()){Intent i=getPackageManager().getLaunchIntentForPackage(pkgs.get(focus));if(i!=null)startActivity(i);else Toast.makeText(AppsActivity.this,"App kann nicht geöffnet werden",Toast.LENGTH_SHORT).show();}else if(key==KeyEvent.KEYCODE_BACK){finish();return true;}else return super.onKeyDown(key,e);invalidate();return true;}
 }
}
