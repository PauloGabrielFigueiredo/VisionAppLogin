package VisitPlann.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class VideoActivityDisplay extends AppCompatActivity {
    public static final String ObjetoVision = "message";
    public static boolean isRunning = false;

    private WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRunning = true;
        setContentView(R.layout.activity_video_display);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String messageText = intent.getStringExtra(ObjetoVision);
        // Capture the layout's TextView and set the string as its text
        TextView textView;
        textView = findViewById(R.id.textView);
        textView.setText(messageText);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setSupportZoom(true);

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }
        });

        webView.loadUrl("https://raw.githubusercontent.com/JorgeOliveiraFisico/To-Do-Nature/51470e1b86826f6eedff71746a5f8a28144d6c36/CruzMadeira.pdf");


        /**
         * Load a Image to Display
         * **/




    }

    @Override
    protected void onDestroy() {
        isRunning = false;

        super.onDestroy();
    }


    @Override
    protected void onResume() {
        isRunning = true;
        super.onResume();
    }


}