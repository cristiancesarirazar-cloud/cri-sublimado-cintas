package com.crisublimado.cintas;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Base64;
import android.content.Context;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> uploadCallback;
    private static final int FILE_CHOOSER = 101;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setSupportMultipleWindows(false);
        webView.addJavascriptInterface(new PrintBridge(this), "AndroidPrint");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (uploadCallback != null) uploadCallback.onReceiveValue(null);
                uploadCallback = callback;
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    String[] accepted = params.getAcceptTypes();
                    String type = "image/*";
                    if (accepted != null) {
                        for (String a : accepted) {
                            if (a != null && a.startsWith("image/")) { type = a; break; }
                        }
                    }
                    intent.setType(type);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                    startActivityForResult(Intent.createChooser(intent, "Elegí una o varias imágenes"), FILE_CHOOSER);
                    return true;
                } catch (Exception e) {
                    uploadCallback.onReceiveValue(null);
                    uploadCallback = null;
                    return false;
                }
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER && uploadCallback != null) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) results[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    results = new Uri[] { data.getData() };
                }
            }
            uploadCallback.onReceiveValue(results);
            uploadCallback = null;
        }
    }

    public class PrintBridge {
        private final Context context;
        PrintBridge(Context c) { context = c; }
        @JavascriptInterface public void printPng(String base64, double widthCm, double heightCm) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    WebView printView = new WebView(context);
                    String html = "<html><head><style>@page{size:"+widthCm+"cm "+heightCm+"cm;margin:0}html,body{margin:0;padding:0;width:"+widthCm+"cm;height:"+heightCm+"cm}img{width:"+widthCm+"cm;height:"+heightCm+"cm;display:block}</style></head><body><img src='data:image/png;base64,"+base64+"'></body></html>";
                    printView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                    printView.setWebViewClient(new WebViewClient() {
                        @Override public void onPageFinished(WebView view, String url) {
                            PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                            pm.print("Cri_Sublimado_Cintas", view.createPrintDocumentAdapter("Cri Sublimado Cintas"), new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build());
                        }
                    });
                } catch (Exception ignored) { }
            });
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
