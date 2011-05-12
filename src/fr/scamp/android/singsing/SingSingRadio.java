package fr.scamp.android.singsing;

import android.app.Activity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener; 
import android.media.MediaPlayer.OnBufferingUpdateListener ; 
import android.media.MediaPlayer.OnCompletionListener; 
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;
import android.view.View; 
import android.view.View.OnClickListener ;
import android.widget.Button; 
import android.widget.TextView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element; 
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SingSingRadio extends Activity implements OnClickListener, OnInfoListener, OnBufferingUpdateListener, OnCompletionListener
{
  private static final String LOG_TAG = "SingSingRadio";
  private MediaPlayer mp = new MediaPlayer();
  private TextView artist, title  ; 
  private ImageView cover ; 
  private String metadata_url = "http://www.sing-sing.org/navi/titre/actuellement.xml" ;

  @Override
    public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    Button b;
    b = (Button)findViewById(R.id.low);
    b.setOnClickListener(this);
    b = (Button)findViewById(R.id.high);
    b.setOnClickListener(this);
    b = (Button)findViewById(R.id.exit);
    b.setOnClickListener(this);

    artist = (TextView)findViewById(R.id.artist);
    title = (TextView)findViewById(R.id.title);
    cover = (ImageView) findViewById(R.id.cover);   
  }

  public void onClick(View v) {
    String url = "http://stream.sing-sing.org:8000/singsing24" ;
    switch(v.getId()) {
    case R.id.low :
      url = "http://stream.sing-sing.org:8000/singsing24" ;
      break ;
    case R.id.high :
      url = "http://stream.sing-sing.org:8000/singsing128" ;
      break ;
    case R.id.exit :
      Process.killProcess(Process.myPid());
    }
    update_metadata();
    try {
      mp.stop();
      mp.reset();
      mp.setDataSource(url);
      mp.prepare();
      mp.setOnInfoListener(this);
      mp.setOnBufferingUpdateListener(this);
      mp.setOnCompletionListener(this);
      mp.start();
    } catch (IOException e) {
      Log.e(LOG_TAG, "Mediaplayer failed");
    }
  }

  public boolean onInfo(MediaPlayer mp, int what, int extra){
    Log.i(LOG_TAG, "MEDIA_INFO "+what);
    if ( what == mp.MEDIA_INFO_METADATA_UPDATE){
      update_metadata();
      Log.i(LOG_TAG, "MEDIA_INFO_METADATA_UPDATE");
    }    
    return true;
  }

  public void onBufferingUpdate(MediaPlayer mp, int percent){
    Log.i(LOG_TAG, "Buffered "+percent+"%");
  }

  public void onCompletion(MediaPlayer arg0) {
    Log.d(LOG_TAG, "onCompletion called");
  }

  
  private void update_metadata()
  {
    InputStream in = null;
    try {
      in = OpenHttpConnection(metadata_url);
      Document doc = null;
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db;            
      try {
	db = dbf.newDocumentBuilder();
	doc = db.parse(in);
      } catch (ParserConfigurationException e) {
	Log.e(LOG_TAG, "Config XML Parser error "+e);
      } catch (SAXException e) {
	Log.e(LOG_TAG, "SAX XML Parser error "+e);
      }           
      doc.getDocumentElement().normalize();             

      NodeList itemNodes = doc.getElementsByTagName("artist"); 
      Element element = (Element) doc.getElementsByTagName("artist").item(0);
      artist.setText(element.getFirstChild().getNodeValue()+" - ");

      itemNodes = doc.getElementsByTagName("title"); 
      element = (Element) doc.getElementsByTagName("title").item(0);
      title.setText(element.getFirstChild().getNodeValue());

      itemNodes = doc.getElementsByTagName("cover"); 
      element = (Element) doc.getElementsByTagName("cover").item(0);
      String cover_url = element.getFirstChild().getNodeValue();
      Bitmap bitmap = DownloadImage("http://"+cover_url) ;
      cover.setImageBitmap(bitmap);     
    } catch (IOException e) {
	Log.e(LOG_TAG, "update metadata error "+e);     
    }
  }

  private Bitmap DownloadImage(String URL)
  {        
    Bitmap bitmap = null;
    InputStream in = null;        
    try {
      in = OpenHttpConnection(URL);
      bitmap = BitmapFactory.decodeStream(in);
      in.close();
    } catch (IOException e) {
      Log.e(LOG_TAG, "Download Image error "+e);
    }
    return bitmap;                
  }

  private InputStream OpenHttpConnection(String urlString) 
    throws IOException
  {
    InputStream in = null;
    int response = -1;               
    URL url = new URL(urlString); 
    URLConnection conn = url.openConnection();                 
    if (!(conn instanceof HttpURLConnection))                     
      throw new IOException("Not an HTTP connection");        
    try{
      HttpURLConnection httpConn = (HttpURLConnection) conn;
      httpConn.setAllowUserInteraction(false);
      httpConn.setInstanceFollowRedirects(true);
      httpConn.setRequestMethod("GET");
      httpConn.connect(); 

      response = httpConn.getResponseCode();                 
      if (response == HttpURLConnection.HTTP_OK) {
	in = httpConn.getInputStream();                                 
      }                     
    }
    catch (Exception ex)
      {
	throw new IOException("Error connecting");            
      }
    return in;     
  }

}



