package app.belesify;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.IOException;
import java.io.InputStream;

// Hosts the bundled web app at https://appassets.androidplatform.net so YouTube's embedded player sees a normal https page.
public class MainActivity extends Activity {
    static final String HOST = "appassets.androidplatform.net";
    static final int PICK_AUDIO = 1;
    WebView web;
    ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        web.setBackgroundColor(0xFF000000);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setUserAgentString(s.getUserAgentString() + " BelesifyApp");
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new Client());
        web.setWebChromeClient(new Chrome());
        setContentView(web);
        if (state == null || web.restoreState(state) == null) web.loadUrl("https://" + HOST + "/index.html");
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        // keep playing in the background like a music app instead of closing
        moveTaskToBack(true);
    }

    WebResourceResponse serve(Uri u) {
        if (u == null || !HOST.equals(u.getHost())) return null;
        String path = u.getPath();
        if (path == null || path.equals("/")) path = "/index.html";
        path = path.substring(1);
        String mime = "application/octet-stream";
        if (path.endsWith(".html")) mime = "text/html";
        else if (path.endsWith(".png")) mime = "image/png";
        else if (path.endsWith(".js")) mime = "text/javascript";
        else if (path.endsWith(".json") || path.endsWith(".webmanifest")) mime = "application/json";
        try {
            InputStream in = getAssets().open(path);
            return new WebResourceResponse(mime, "utf-8", in);
        } catch (IOException e) {
            return new WebResourceResponse("text/plain", "utf-8", null);
        }
    }

    boolean openOutside(Uri u) {
        if (u == null || HOST.equals(u.getHost())) return false;
        try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { }
        return true;
    }

    class Client extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) { return serve(r.getUrl()); }
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView v, String url) { return serve(Uri.parse(url)); }
        @Override
        public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) { return r.isForMainFrame() && openOutside(r.getUrl()); }
        @Override
        public boolean shouldOverrideUrlLoading(WebView v, String url) { return openOutside(Uri.parse(url)); }
    }

    class Chrome extends WebChromeClient {
        View custom;
        CustomViewCallback customCb;

        @Override
        public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
            if (fileCallback != null) fileCallback.onReceiveValue(null);
            fileCallback = cb;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("audio/*");
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            try {
                startActivityForResult(Intent.createChooser(i, "Müzik seç"), PICK_AUDIO);
            } catch (Exception e) {
                fileCallback = null;
                return false;
            }
            return true;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback cb) {
            if (custom != null) { cb.onCustomViewHidden(); return; }
            custom = view; customCb = cb;
            setContentView(view);
        }

        @Override
        public void onHideCustomView() {
            if (custom == null) return;
            custom = null;
            setContentView(web);
            if (customCb != null) customCb.onCustomViewHidden();
        }
    }

    @Override
    protected void onActivityResult(int req, int code, Intent data) {
        if (req != PICK_AUDIO || fileCallback == null) { super.onActivityResult(req, code, data); return; }
        Uri[] result = null;
        if (code == RESULT_OK && data != null) {
            ClipData clip = data.getClipData();
            if (clip != null) {
                result = new Uri[clip.getItemCount()];
                for (int k = 0; k < result.length; k++) result[k] = clip.getItemAt(k).getUri();
            } else if (data.getData() != null) {
                result = new Uri[] { data.getData() };
            }
        }
        fileCallback.onReceiveValue(result);
        fileCallback = null;
    }
}
