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
  private String url = "http://stream.sing-sing.org:8000/singsing24" ;
  private String metadata_url = "http://www.sing-sing.org/navi/titre/actuellement.xml" ;

  protected String artist, title ;
  protected Bitmap cover_bitmap ;

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
    b = (Button)findViewById(R.id.refresh);
    b.setOnClickListener(this);
    b = (Button)findViewById(R.id.exit);
    b.setOnClickListener(this);

  }

  public void onClick(View v) {
    switch(v.getId()) {
    case R.id.low :
      url = "http://stream.sing-sing.org:8000/singsing24" ;
      play();
      break ;
    case R.id.high :
      url = "http://stream.sing-sing.org:8000/singsing128" ;
      play();
      break ;
    case R.id.refresh :      
      new UpdateMetadata().execute();
      break ;
    case R.id.exit :
      Process.killProcess(Process.myPid());
    }
  }

  private void play(){
    new UpdateMetadata().execute();
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
    Log.d(LOG_TAG, "MEDIA_INFO "+what);
    if ( what == mp.MEDIA_INFO_METADATA_UPDATE){
      new UpdateMetadata().execute();
      Log.i(LOG_TAG, "MEDIA_INFO_METADATA_UPDATE");
    }    
    return true;
  }

  public void onBufferingUpdate(MediaPlayer mp, int percent){
    Log.d(LOG_TAG, "Buffered "+percent+"%");
  }

  public void onCompletion(MediaPlayer arg0) {
    Log.d(LOG_TAG, "onCompletion called");
  }
   
  private class UpdateMetadata extends AsyncTask<Void, Void, Void> {
    protected Void doInBackground(Void... params) {
      Log.d(LOG_TAG, "update metadata");
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
	//artist.setText(element.getFirstChild().getNodeValue()+" - ");
	artist = element.getFirstChild().getNodeValue();

	itemNodes = doc.getElementsByTagName("title"); 
	element = (Element) doc.getElementsByTagName("title").item(0);
	//title.setText(element.getFirstChild().getNodeValue());
	title = element.getFirstChild().getNodeValue();

	itemNodes = doc.getElementsByTagName("cover"); 
	element = (Element) doc.getElementsByTagName("cover").item(0);
	String cover_url = element.getFirstChild().getNodeValue();
	cover_bitmap = DownloadImage("http://"+cover_url) ;
      } catch (IOException e) {
	Log.e(LOG_TAG, "update metadata error "+e);     
      }
      return null;
    }
    
    protected void onPostExecute(Void param) {
      TextView tv ; 
      tv = (TextView)findViewById(R.id.artist);
      tv.setText(artist+" - ");
      tv = (TextView)findViewById(R.id.title);
      tv.setText(title);
      ImageView cover = (ImageView) findViewById(R.id.cover);   
      cover.setImageBitmap(cover_bitmap);     
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
}

