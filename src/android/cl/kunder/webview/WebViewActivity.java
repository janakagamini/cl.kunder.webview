package cl.kunder.webview;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.util.List;

public class WebViewActivity extends CordovaActivity {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    static Dialog dialog;
    static Activity activity2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Aqui debo crear el loading
        activity2 = this;
        Bundle b = getIntent().getExtras();
        String url = b.getString("url");
        List<String> ignore = b.getStringArrayList("ignore");
        Boolean shouldShowLoading = false;
        try {
            shouldShowLoading = b.getBoolean("shouldShowLoading");
        } catch (Exception e) {

        }
        if (shouldShowLoading) {
            showLoading();
        }

        loadUrl((url.matches("^(.*://|javascript:)[\\s\\S]*$") ? "" : "file:///android_asset/www/") + url);

        final WebView myWebView = (WebView) this.appView.getView();
        myWebView.setWebViewClient(new SystemWebViewClient((SystemWebViewEngine) this.appView.getEngine()) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (containsAnyOf(url, ignore)) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Closing second webview in 2s.");
                            WebViewActivity.this.finish();
                        }
                    }, 2000);
                }

                return false;
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Server responded with an error, e.g. 4xx, 5xx. Closing webview in 2s.");
                        WebViewActivity.this.finish();
                    }
                }, 2000);
            }
        });
    }

    private boolean containsAnyOf(String testString, List<String> testCases) {
        for (String s : testCases) {
            if (testString.contains(s)) {
                Log.d(TAG, "Found '" + s + "' in url!");
                return true;
            }
        }

        return false;
    }

    public static boolean showLoading() {
        // Loading spinner
        activity2.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new Dialog(activity2, android.R.style.Theme_Translucent_NoTitleBar);
                ProgressBar progressBar = new ProgressBar(activity2, null, android.R.attr.progressBarStyle);

                LinearLayout linearLayout = new LinearLayout(activity2);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                RelativeLayout layoutPrincipal = new RelativeLayout(activity2);
                layoutPrincipal.setBackgroundColor(Color.parseColor("#d9000000"));

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);

                linearLayout.addView(progressBar);

                linearLayout.setLayoutParams(params);

                layoutPrincipal.addView(linearLayout);

                dialog.setContentView(layoutPrincipal);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                    }
                });
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
                            return true;
                        return false;
                    }
                });

                dialog.show();
            }
        });

        return true;
    }

    public static boolean hideLoading() {
        // Loading spinner
        activity2.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.hide();
            }
        });
        return true;
    }
}
