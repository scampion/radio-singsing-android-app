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

    //status = (TextView)findViewById(R.id.status);
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
    } catch (IOException e) {
      Log.e(LOG_TAG, "Mediaplayer failed");
    }
  }
}

