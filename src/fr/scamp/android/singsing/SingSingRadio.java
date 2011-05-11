package fr.scamp.android.singsing;

import android.app.Activity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;
import android.view.View; 
import android.view.View.OnClickListener ;
import android.widget.Button; 
import android.widget.TextView; 
import java.net.URL;
import java.net.URLConnection;
import fr.scamp.android.singsing.IcyStreamMeta; 

import java.io.IOException;

public class SingSingRadio extends Activity implements OnClickListener
{
  private static final String LOG_TAG = "SingSingRadio";
  private MediaPlayer mp = new MediaPlayer();
  private TextView status ; 

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
    b = (Button)findViewById(R.id.quit);
    b.setOnClickListener(this);

    status = (TextView)findViewById(R.id.status);
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
    case R.id.quit :
      Process.killProcess(Process.myPid());
    }
    try {
      mp.stop();
      mp.reset();
      mp.setDataSource(url);
      mp.prepare();
      mp.start();
      new HTTPMetadataTask().execute();
    } catch (IOException e) {
      Log.e(LOG_TAG, "Mediaplayer failed");
    }
  }

  private class HTTPMetadataTask extends AsyncTask<Void, Void, String> {
    protected String pochette_php = "http://www.sing-sing.org/programmation/pochette_frame2.php" ; 

    protected String doInBackground(Void... params) {
      try {
	URL u = new URL(pochette_php);
	//URLConnection uconn = u.openConnection();
	//uconn.connect();
	return ""+u.getContent(); 
      } 
      catch (Exception e) {
	Log.e(LOG_TAG, "Cover retrieve failed "+e.getMessage());
	return "No cover" ;
      }
    } 
    
    protected void onPostExecute(String result) {
      TextView status = (TextView)findViewById(R.id.status);
      status.setText(result);    
    }
  }

  private class MetadataTask extends AsyncTask<URL, Void, IcyStreamMeta> {
    protected IcyStreamMeta streamMeta;
    protected IcyStreamMeta doInBackground(URL... urls) {

      //DEBUG
      //TextView status = (TextView)findViewById(R.id.status);
      //status.setText("Debug"+urls[0]);

      streamMeta = new IcyStreamMeta(urls[0]);
      try {
	streamMeta.refreshMeta();
      } catch (IOException e) {
	Log.e(MetadataTask.class.toString(), e.getMessage());
      }
      return streamMeta;
    }
    
    protected void onPostExecute(IcyStreamMeta result) {
      try {
	//TextView status = (TextView)findViewById(R.id.status);
	String t = streamMeta.getArtist();
	//status.setText(t+" done");

      } catch (IOException e) {
	Log.e(MetadataTask.class.toString(), e.getMessage());
      }
    }
    
  }

}
