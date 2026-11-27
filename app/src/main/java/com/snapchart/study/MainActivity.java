package com.snapchart.study;
import android.app.*; import android.os.*; import android.content.*; import android.graphics.*; import android.provider.MediaStore; import android.net.Uri; import android.view.*; import android.widget.*; import java.io.*; import java.util.*;
public class MainActivity extends Activity {
 Bitmap source; Chart chart; TextView status; Spinner labels; EditText notes; boolean marked=true; ArrayList<Mark> marks=new ArrayList<>();
 int practiceNumber=0; boolean practiceActive=false; TextView resultSummary; ArrayList<Zone> zones=new ArrayList<>();
 static class Zone {float x1,x2,y1,y2; String label; Zone(float a,float b,float c,float d,String l){x1=a;x2=b;y1=c;y2=d;label=l;}}
 String[] terms={"BOS","CHoCH","MSS","i-BOS","i-MSS","FVG","IFVG","POI","AOI","OB","Swing high","Swing low"};
 String[] descriptions={"Break of structure","Change of character","Market structure shift","Internal break of structure","Internal market structure shift","Fair value gap","Inverse fair value gap","Point of interest","Area of interest","Order block","Local high","Local low"};
 static class Mark { float x,y; String label; Mark(float x,float y,String l){this.x=x;this.y=y;label=l;} }
 void button(LinearLayout root,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setOnClickListener(v->action.run());root.addView(b);}
 @Override public void onCreate(Bundle b){super.onCreate(b);ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(22,22,22,22);root.setBackgroundColor(0xff101820);scroll.addView(root);setContentView(scroll);
 TextView title=new TextView(this);title.setText("SnapChart AI | Chart Study");title.setTextColor(0xff74e8bc);title.setTextSize(25);root.addView(title);
 status=new TextView(this);status.setText("Upload a chart screenshot or generate a fictional practice worksheet. Analyze to mark BOS, MSS, FVG and OB.");status.setTextColor(-1);root.addView(status);
 button(root,"Upload chart screenshot / practice worksheet",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,1);});
 button(root,"New practice worksheet",()->practiceChart());
 button(root,"Analyze worksheet — BOS / MSS / FVG",()->analyzeWorksheet());
 button(root,"Analyze screenshot — mark all patterns",()->analyzeScreenshot());
 button(root,"Save clean worksheet PNG",()->save(false,false));
 resultSummary=new TextView(this);resultSummary.setTextColor(0xffb9e9d7);resultSummary.setTextSize(16);resultSummary.setPadding(0,12,0,12);
 chart=new Chart();root.addView(chart,new LinearLayout.LayoutParams(-1,(int)(getResources().getDisplayMetrics().density*300)));root.addView(resultSummary);
 labels=new Spinner(this);labels.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,terms));root.addView(labels);
 button(root,"Undo marker",()->{if(practiceActive){status.setText("Automatic practice results are preserved. Use Original / marked view to hide them.");}else if(!marks.isEmpty())marks.remove(marks.size()-1);chart.invalidate();});
 button(root,"Original / marked view",()->{marked=!marked;chart.invalidate();});
 button(root,"Pattern glossary",()->{int idx=labels.getSelectedItemPosition();new AlertDialog.Builder(this).setTitle(terms[idx]).setMessage(descriptions[idx]+" — a study label, not a prediction.").setPositiveButton("OK",null).show();});
 notes=new EditText(this);notes.setHint("Study notes");notes.setTextColor(-1);notes.setHintTextColor(0xffaab5bf);root.addView(notes);
 button(root,"Save notes on device",()->{getPreferences(0).edit().putString("notes",notes.getText().toString()).apply();status.setText("Notes saved.");});notes.setText(getPreferences(0).getString("notes",""));
 button(root,"Save marked PNG",()->save(false,true));button(root,"Share marked PNG",()->save(true,true));
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=1||result!=RESULT_OK||data==null)return;try{ImageDecoder.Source s=ImageDecoder.createSource(getContentResolver(),data.getData());source=ImageDecoder.decodeBitmap(s,(d,info,src)->{d.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);int w=info.getSize().getWidth(),h=info.getSize().getHeight();float f=Math.min(1f,2000f/Math.max(w,h));d.setTargetSize(Math.max(1,(int)(w*f)),Math.max(1,(int)(h*f)));});marks.clear();zones.clear();practiceActive=false;resultSummary.setText("");marked=true;chart.invalidate();analyzeWorksheet();}catch(Exception e){status.setText("Could not open image.");}}
 void practiceChart(){
  final int w=1200,h=760; practiceActive=true;
  // Fictional, built-in exercises only. No imported prices or screenshot inference.
  int[][] a={{44,46,40,41},{41,45,38,44},{44,52,43,50},{50,54,48,52},{52,53,44,46},{46,49,42,45},{45,51,44,50},{52,62,52,60},{61,66,60,64},{64,65,55,57},{57,59,50,52},{52,57,48,54},{54,61,53,60},{60,62,56,57},{57,58,46,48},{45,46,36,38},{37,40,32,34},{34,45,33,43},{43,46,40,41},{41,43,30,32}};
  boolean mirror=(practiceNumber++%2)==1;
  if(mirror)for(int[] b:a){int high=b[1];b[0]=100-b[0];b[1]=100-b[2];b[2]=100-high;b[3]=100-b[3];}
  source=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
  Canvas c=new Canvas(source);c.drawColor(0xff12202b);Paint p=new Paint(3);
  p.setColor(0xff314453);p.setStrokeWidth(1);
  for(int y=140;y<680;y+=90)c.drawLine(40,y,1160,y,p);
  p.setColor(0xffd8e6ef);p.setTextSize(33);p.setTypeface(Typeface.DEFAULT_BOLD);
  c.drawText("FICTIONAL PRACTICE CHART "+(mirror?"B":"A"),40,47,p);
  p.setTypeface(Typeface.DEFAULT);p.setTextSize(24);
  c.drawText("Built-in synthetic data | Pattern exercise only",40,83,p);
  p.setColor(0xffe7bd32);c.drawRect(12,12,36,36,p);p.setColor(0xffaf63f4);c.drawRect(1164,724,1188,748,p);
  for(int i=0;i<a.length;i++){
   int[] b=a[i];float x=practiceX(i),top=practiceY(Math.max(b[0],b[3])),bottom=practiceY(Math.min(b[0],b[3]));
   p.setColor(b[3]>=b[0]?0xff56c9a4:0xffe9858d);p.setStrokeWidth(3);
   c.drawLine(x,practiceY(b[1]),x,practiceY(b[2]),p);
   c.drawRect(x-13,top,x+13,Math.max(top+3,bottom),p);
  }
  marks.clear();zones.clear();marked=true;chart.invalidate();
  resultSummary.setText("Unmarked worksheet. Tap Analyze worksheet to reveal the answers.");
  status.setText("Fictional worksheet ready. You can save its clean image and import it again.");
 }
 // The two corner swatches identify the fictional worksheet format and establish image scale.
 // Real chart screenshots are handled separately by analyzeScreenshot().
 boolean near(int pixel,int color,int tolerance){
  return Math.abs(Color.red(pixel)-Color.red(color))<=tolerance &&
    Math.abs(Color.green(pixel)-Color.green(color))<=tolerance &&
    Math.abs(Color.blue(pixel)-Color.blue(color))<=tolerance;
 }
 float[] locate(int color){
  int w=source.getWidth(),h=source.getHeight();long xx=0,yy=0,count=0;
  int minX=w,minY=h,maxX=-1,maxY=-1;int[] row=new int[w];
  for(int y=0;y<h;y++){source.getPixels(row,0,w,0,y,w,1);
   for(int x=0;x<w;x++)if(near(row[x],color,12)){xx+=x;yy+=y;count++;
    minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}}
  if(count<16||maxX-minX>80||maxY-minY>80||maxX-minX<3||maxY-minY<3||
    count<0.65*(maxX-minX+1)*(maxY-minY+1))return null;
  return new float[]{xx/(float)count,yy/(float)count};
 }
 void analyzeWorksheet(){
  if(source==null){status.setText("Create or upload a fictional worksheet first.");return;}
  float[] left=locate(0xffe7bd32),right=locate(0xffaf63f4);
  if(left==null||right==null){analyzeScreenshot();return;}
  float sx=(right[0]-left[0])/1152f,sy=(right[1]-left[1])/712f;
  float ox=left[0]-23.5f*sx,oy=left[1]-23.5f*sy;
  int w=source.getWidth(),h=source.getHeight();
  if(sx<0.3f||sy<0.3f||Math.abs(sx/sy-1)>0.06f||ox< -3||oy< -3||ox+1200*sx>w+3||oy+760*sy>h+3){analyzeScreenshot();return;}
  int[][] a=new int[20][4];
  // Decode each candle's wick and body from the visible image pixels, not a saved answer list.
  for(int i=0;i<a.length;i++){
   int x=Math.round(ox+practiceX(i)*sx),side=Math.round(ox+(practiceX(i)+7)*sx);
   int green=0xff56c9a4,red=0xffe9858d,top=Integer.MAX_VALUE,bottom=-1,bodyTop=Integer.MAX_VALUE,bodyBottom=-1,up=0,down=0;
   for(int y=Math.max(0,Math.round(oy+100*sy));y<Math.min(h,Math.round(oy+700*sy));y++){
    int p=source.getPixel(x,y),q=source.getPixel(side,y);
    if(near(p,green,36)||near(p,red,36)){top=Math.min(top,y);bottom=y;}
    if(near(q,green,36)||near(q,red,36)){bodyTop=Math.min(bodyTop,y);bodyBottom=y;if(near(q,green,36))up++;else down++;}
   }
   if(bottom<=top||bodyBottom<bodyTop||up+down<2||bodyTop<top-2||bodyBottom>bottom+2){analyzeScreenshot();return;}
   int hi=decodeValue(top,oy,sy),lo=decodeValue(bottom,oy,sy),bt=decodeValue(bodyTop,oy,sy),bb=decodeValue(bodyBottom,oy,sy);
   int open=up>down?bb:bt,close=up>down?bt:bb;
   if(hi<Math.max(open,close)||lo>Math.min(open,close)){analyzeScreenshot();return;}
   a[i]=new int[]{open,hi,lo,close};
  }
  marks.clear();zones.clear();int hi=-1,lo=-1,previous=0,bos=0,mss=0,fvg=0;
  StringBuilder explanation=new StringBuilder();
  for(int i=2;i<a.length;i++){
   int j=i-1;
   if(a[j][1]>a[j-1][1]&&a[j][1]>a[i][1])hi=j;
   if(a[j][2]<a[j-1][2]&&a[j][2]<a[i][2])lo=j;
   int d=hi>=0&&a[i][3]>a[hi][1]?1:lo>=0&&a[i][3]<a[lo][2]?-1:0;
   if(d!=0){
    boolean shift=previous!=0&&previous!=d;String label=shift?"MSS":"BOS";
    if(shift)mss++;else bos++;
    int pivot=d>0?hi:lo,level=d>0?a[hi][1]:a[lo][2];
    marks.add(new Mark((ox+practiceX(i)*sx)/w,(oy+practiceY(level)*sy)/h,label));
    explanation.append(label).append(" at candle ").append(i+1).append(": close crosses the previous local ").append(d>0?"high":"low").append(" at candle ").append(pivot+1).append(shift?"; direction differs from the previous break.":"; structural break in this exercise.").append("\n");
    previous=d;if(d>0)hi=-1;else lo=-1;
   }
   int lower=0,upper=0;
   if(a[i][2]>a[i-2][1]){lower=a[i-2][1];upper=a[i][2];}
   else if(a[i][1]<a[i-2][2]){lower=a[i][1];upper=a[i-2][2];}
   if(upper>lower){fvg++;
    zones.add(new Zone((ox+(practiceX(i-2)-15)*sx)/w,(ox+(practiceX(i)+15)*sx)/w,(oy+practiceY(upper)*sy)/h,(oy+practiceY(lower)*sy)/h,"FVG"));
    explanation.append("FVG at candles ").append(i-1).append("–").append(i+1).append(": first and third candle wick ranges leave a gap.\n");
   }
  }
  practiceActive=true;marked=true;chart.invalidate();
  status.setText("Worksheet pixels analyzed. Simplified educational rules; not predictions.");
  resultSummary.setText("BOS: "+bos+"   MSS: "+mss+"   FVG: "+fvg+"\nGold: BOS | Pink: MSS | Blue: FVG\n\n"+explanation.toString());
 }
 // ---- Real screenshot analysis: read solid green/red candles from any chart screenshot ----
 // Relaxed colour test so JPEG-washed and thin candles still read.
 // Covers TradingView (#26a69a / #ef5350), Binance, MetaTrader and the built-in worksheet colours.
 boolean isBullPixel(int pixel){
  int r=Color.red(pixel),g=Color.green(pixel),b=Color.blue(pixel);
  int mx=Math.max(r,Math.max(g,b)),mn=Math.min(r,Math.min(g,b));
  if(mx-mn<25||mx<50)return false;
  return g==mx&&g>r+15&&g>=b;
 }
 boolean isBearPixel(int pixel){
  int r=Color.red(pixel),g=Color.green(pixel),b=Color.blue(pixel);
  int mx=Math.max(r,Math.max(g,b)),mn=Math.min(r,Math.min(g,b));
  if(mx-mn<25||mx<50)return false;
  return r==mx&&r>g+15&&r>b+8;
 }
 void analyzeScreenshot(){
  if(source==null){status.setText("Upload a chart screenshot first.");return;}
  int w=source.getWidth(),h=source.getHeight();
  int[] rowD=new int[h],row=new int[w];
  for(int y=0;y<h;y++){
   source.getPixels(row,0,w,0,y,w,1);
   for(int x=0;x<w;x++)if(isBullPixel(row[x])||isBearPixel(row[x]))rowD[y]++;
  }
  // Chart band: longest run of rows with enough candle pixels (ignores UI text and volume bars).
  int thr=Math.max(2,w/300);
  int bestTop=-1,bestBot=-1,s=-1,gap=0;
  for(int y=0;y<h;y++){
   if(rowD[y]>=thr){if(s<0)s=y;gap=0;}
   else if(s>=0){gap++;if(gap>15){int e=y-gap;if(bestTop<0||e-s>bestBot-bestTop){bestTop=s;bestBot=e;}s=-1;gap=0;}}
  }
  if(s>=0){int e=h-1;if(bestTop<0||e-s>bestBot-bestTop){bestTop=s;bestBot=e;}}
  if(bestTop<0||bestBot<=bestTop){screenshotUnsupported();return;}
  int bandTop=bestTop,bandBot=bestBot,bandH=bandBot-bandTop+1;
  // Candle columns inside the band; merge 1-2px gaps only while the result stays narrow.
  int[] col2=new int[w];
  for(int y=bandTop;y<=bandBot;y++){
   source.getPixels(row,0,w,0,y,w,1);
   for(int x=0;x<w;x++)if(isBullPixel(row[x])||isBearPixel(row[x]))col2[x]++;
  }
  ArrayList<int[]> raw=new ArrayList<>();
  s=-1;
  for(int x=0;x<w;x++){
   if(col2[x]>=1){if(s<0)s=x;}
   else if(s>=0){raw.add(new int[]{s,x-1});s=-1;}
  }
  if(s>=0)raw.add(new int[]{s,w-1});
  ArrayList<int[]> clusters=new ArrayList<>();
  for(int[] cl:raw){
   if(!clusters.isEmpty()&&cl[0]-clusters.get(clusters.size()-1)[1]<=2&&(cl[1]-clusters.get(clusters.size()-1)[0]+1)<=12)clusters.get(clusters.size()-1)[1]=cl[1];
   else clusters.add(new int[]{cl[0],cl[1]});
  }
  ArrayList<int[]> kept=new ArrayList<>();
  for(int[] cl:clusters)if(cl[1]-cl[0]+1<=60)kept.add(cl);
  int n=kept.size();
  if(n<8){screenshotUnsupported();return;}
  // Decode OHLC for every candle in one pass over the band rows.
  // Value = -y, so a higher price is a larger value; structure rules only compare values.
  int[] tops=new int[n],bots=new int[n],ups=new int[n],downs=new int[n],rowMax=new int[n];
  int[][] rowCounts=new int[n][bandH];
  for(int i=0;i<n;i++){tops[i]=Integer.MAX_VALUE;bots[i]=-1;}
  for(int y=bandTop;y<=bandBot;y++){
   source.getPixels(row,0,w,0,y,w,1);
   for(int i=0;i<n;i++){
    int x0=kept.get(i)[0],x1=kept.get(i)[1],ru=0,rd=0;
    for(int x=x0;x<=x1;x++){if(isBullPixel(row[x]))ru++;else if(isBearPixel(row[x]))rd++;}
    int c=ru+rd;
    rowCounts[i][y-bandTop]=c;
    if(c>0){if(y<tops[i])tops[i]=y;if(y>bots[i])bots[i]=y;ups[i]+=ru;downs[i]+=rd;if(c>rowMax[i])rowMax[i]=c;}
   }
  }
  int[][] a=new int[n][4];
  float[] cx=new float[n];
  int m=0;
  for(int i=0;i<n;i++){
   int top=tops[i],bottom=bots[i];
   if(bottom<=top||bottom-top>0.85*bandH)continue;
   int need=Math.max(1,rowMax[i]/2),bodyTop=Integer.MAX_VALUE,bodyBottom=-1;
   for(int k=0;k<bandH;k++)if(rowCounts[i][k]>=need){int y=bandTop+k;if(y<bodyTop)bodyTop=y;if(y>bodyBottom)bodyBottom=y;}
   if(bodyBottom<bodyTop||ups[i]+downs[i]<2)continue;
   boolean bull=ups[i]>=downs[i];
   int hi=-top,lo=-bottom,bt=-bodyTop,bb=-bodyBottom;
   int open=bull?bb:bt,close=bull?bt:bb;
   if(hi<Math.max(open,close)||lo>Math.min(open,close))continue;
   a[m]=new int[]{open,hi,lo,close};
   cx[m]=(kept.get(i)[0]+kept.get(i)[1])/2f;
   m++;
  }
  if(m<8){screenshotUnsupported();return;}
  n=m;
  marks.clear();zones.clear();int hi=-1,lo=-1,previous=0,bos=0,mss=0,fvg=0,ob=0;
  StringBuilder explanation=new StringBuilder();
  for(int i=2;i<n;i++){
   int j=i-1;
   if(a[j][1]>a[j-1][1]&&a[j][1]>a[i][1])hi=j;
   if(a[j][2]<a[j-1][2]&&a[j][2]<a[i][2])lo=j;
   int d=hi>=0&&a[i][3]>a[hi][1]?1:lo>=0&&a[i][3]<a[lo][2]?-1:0;
   if(d!=0){
    boolean shift=previous!=0&&previous!=d;String label=shift?"MSS":"BOS";
    if(shift)mss++;else bos++;
    int pivot=d>0?hi:lo,level=d>0?a[hi][1]:a[lo][2];
    marks.add(new Mark(cx[i]/w,(-level)/(float)h,label));
    explanation.append(label).append(" at candle ").append(i+1).append(": close crosses the previous local ").append(d>0?"high":"low").append(" at candle ").append(pivot+1).append(shift?"; direction differs from the previous break.":".").append("\n");
    // Order block: last opposite-colour candle just before the break.
    int obIdx=-1;
    for(int k=i-1;k>=Math.max(0,i-4);k--){boolean kBull=a[k][3]>=a[k][0];if(d>0&&!kBull){obIdx=k;break;}if(d<0&&kBull){obIdx=k;break;}}
    if(obIdx>=0){
     float zy1=(-Math.max(a[obIdx][0],a[obIdx][3]))/(float)h,zy2=(-Math.min(a[obIdx][0],a[obIdx][3]))/(float)h;
     zones.add(new Zone((cx[obIdx]-18)/w,(cx[obIdx]+18)/w,zy1,zy2,"OB"));
     ob++;
     explanation.append(d>0?"Bullish OB":"Bearish OB").append(" at candle ").append(obIdx+1).append(": last opposite candle before the ").append(label).append(".\n");
    }
    previous=d;if(d>0)hi=-1;else lo=-1;
   }
   int lower=0,upper=0;
   if(a[i][2]>a[i-2][1]){lower=a[i-2][1];upper=a[i][2];}
   else if(a[i][1]<a[i-2][2]){lower=a[i][1];upper=a[i-2][2];}
   if(upper>lower){fvg++;
    zones.add(new Zone((cx[i-2]-18)/w,(cx[i]+18)/w,(-upper)/(float)h,(-lower)/(float)h,"FVG"));
    explanation.append("FVG at candles ").append(i-1).append("–").append(i+1).append(": first and third candle wick ranges leave a gap.\n");
   }
  }
  practiceActive=true;marked=true;chart.invalidate();
  status.setText("Screenshot analyzed: "+n+" candles found. Educational labels, not predictions.");
  resultSummary.setText("BOS: "+bos+"   MSS: "+mss+"   FVG: "+fvg+"   OB: "+ob+"\nGold: BOS | Pink: MSS | Blue: FVG | Purple: OB\n\n"+explanation.toString());
 }
 void screenshotUnsupported(){
  marks.clear();zones.clear();practiceActive=false;marked=true;chart.invalidate();
  status.setText("Could not read candles from this screenshot. Use a clear chart with solid green/red candles, cropped close to the chart, without indicators covering the candles. Tip: zoom in so the candles are bigger.");
  resultSummary.setText("No automatic result for this image. Manual annotation is still available.");
 }
 int decodeValue(int y,float oy,float sy){return Math.round(25+(690-(y-oy)/sy)/12f);}
 void unsupported(){
  marks.clear();zones.clear();practiceActive=false;marked=true;chart.invalidate();
  status.setText("Unsupported image. Import a clean SnapChart fictional worksheet PNG or its clear, uncropped screenshot.");
  resultSummary.setText("No automatic result for this image. Manual annotation is still available.");
 }
 float practiceX(int i){return 65+i*56;}
 float practiceY(int value){return 690-(value-25)*12f;}
 void drawMarks(Canvas c,float w,float h){Paint p=new Paint(3);p.setTextSize(Math.max(12,w/38));for(Zone z:zones){boolean isOb="OB".equals(z.label);p.setColor(isOb?0x55b07fe0:0x5558a9ff);c.drawRect(z.x1*w,z.y1*h,z.x2*w,z.y2*h,p);p.setColor(isOb?0xffc39bff:0xff83caff);p.setStrokeWidth(2);c.drawLine(z.x1*w,z.y1*h,z.x2*w,z.y1*h,p);c.drawText(z.label,z.x1*w,Math.max(p.getTextSize(),z.y1*h-2),p);}p.setStrokeWidth(Math.max(2,w/350));for(Mark m:marks){float x=m.x*w,y=m.y*h;p.setColor(m.label.equals("MSS")?0xfff8a0ce:0xfffbd46d);c.drawCircle(x,y,5,p);c.drawLine(Math.max(0,x-35),y,Math.min(w,x+35),y,p);float tx=Math.max(2,Math.min(w-p.measureText(m.label)-4,x+8));float ty=Math.max(p.getTextSize(),y-8);p.setColor(0xff101820);c.drawRect(tx-2,ty-p.getTextSize(),tx+p.measureText(m.label)+2,ty+3,p);p.setColor(m.label.equals("MSS")?0xfff8a0ce:0xfffbd46d);c.drawText(m.label,tx,ty,p);}}
 class Chart extends View{RectF area=new RectF();Paint p=new Paint(3);Chart(){super(MainActivity.this);}protected void onDraw(Canvas c){c.drawColor(0xff24313d);if(source==null){p.setColor(-1);p.setTextSize(26);c.drawText("No chart selected",22,45,p);return;}float f=Math.min(getWidth()/(float)source.getWidth(),getHeight()/(float)source.getHeight());float w=source.getWidth()*f,h=source.getHeight()*f;area.set((getWidth()-w)/2,(getHeight()-h)/2,(getWidth()+w)/2,(getHeight()+h)/2);c.drawBitmap(source,null,area,p);if(marked){c.save();c.translate(area.left,area.top);drawMarks(c,w,h);c.restore();}}public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP&&source!=null&&!practiceActive&&area.contains(e.getX(),e.getY())){marks.add(new Mark((e.getX()-area.left)/area.width(),(e.getY()-area.top)/area.height(),labels.getSelectedItem().toString()));marked=true;invalidate();performClick();}return true;}public boolean performClick(){super.performClick();return true;}}
 void save(boolean share, boolean overlay){if(source==null){status.setText("Upload a chart first.");return;}try{Bitmap out=source.copy(Bitmap.Config.ARGB_8888,true);if(overlay)drawMarks(new Canvas(out),out.getWidth(),out.getHeight());ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"SnapChart_"+System.currentTimeMillis()+".png");v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/SnapChart");Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new IOException();try(OutputStream stream=getContentResolver().openOutputStream(uri)){out.compress(Bitmap.CompressFormat.PNG,100,stream);}status.setText("Saved to Pictures/SnapChart.");if(share){Intent i=new Intent(Intent.ACTION_SEND);i.setType("image/png");i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share chart"));}}catch(Exception e){status.setText("Could not save image.");}}
}
