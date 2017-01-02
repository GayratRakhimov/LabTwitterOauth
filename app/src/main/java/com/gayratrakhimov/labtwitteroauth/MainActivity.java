package com.gayratrakhimov.labtwitteroauth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    final String URL_TWITTER_SIGN_IN = "http://androidsmile.com/lab/twitter/sign_in.php";
    final String URL_TWITTER_GET_USER_TIMELINE = "http://androidsmile.com/lab/twitter/get_user_timeline.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // open dialog with webview for signin
        signIn();

    }


    /*
        show dialog with webview to sign in
     */
    private void signIn() {

        final Dialog authDialog = new Dialog(this);

        WebView webview = new WebView(this);
        authDialog.setContentView(webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(URL_TWITTER_SIGN_IN);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("callback.php")) {
                    view.loadUrl("javascript:JsonViewer.onJsonReceived(document.getElementsByTagName('body')[0].innerHTML);");
                    authDialog.dismiss();
                }
            }
        });
        webview.addJavascriptInterface(new MyJavaScriptInterface(getApplicationContext()), "JsonViewer");
        authDialog.setCancelable(false);
        authDialog.show();

    }


    /*
        this interface is used to get json from webview
    */
    class MyJavaScriptInterface {

        private Context ctx;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void onJsonReceived(String json) {
            Gson gson = new GsonBuilder().create();
            final OauthResult oauthResult = gson.fromJson(json, OauthResult.class);
            if (oauthResult != null && oauthResult.getOauthToken() != null && oauthResult.getOauthTokenSecret() != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getTweets(oauthResult.getOauthToken(), oauthResult.getOauthTokenSecret(), oauthResult.getScreenName());
                    }
                });
            }
        }

    }


    /*
        requests user timeline
        if successful, show response json in toast
     */
    private void getTweets(String oAuthToken, String oAuthTokenSecret, String screenName) {

        RequestParams params = new RequestParams();
        params.add("oauth_token", oAuthToken);
        params.add("oauth_token_secret", oAuthTokenSecret);
        params.add("screen_name", screenName);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(this, URL_TWITTER_GET_USER_TIMELINE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String json = new String(responseBody);
                Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

}
