package app.example.web_to_app;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FrameLayout fullScreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private static final String MAIN_URL = BuildConfig.BASE_WEB_URL;
    private static final String[] ALLOWED_API = {

    };
    private boolean isFullScreen = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fullScreenContainer = findViewById(R.id.fullscreenContainer);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            setupWebView();
        }

        setupSwipeRefreshLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isFullScreen) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());

        webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY)
                -> swipeRefreshLayout.setEnabled(scrollY == 0));

        webView.loadUrl(MAIN_URL);
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!isFullScreen) {
                webView.reload();
            }
            swipeRefreshLayout.setRefreshing(false);
        });
        swipeRefreshLayout.setEnabled(!isFullScreen);
    }

    private class CustomWebViewClient extends WebViewClient {
        private final Set<String> adDomains = new HashSet<>() {{
            add("googlesyndication.com");
            add("doubleclick.net");
            add("google-analytics.com");
            add("adnxs.com");
            add("facebook.com");
            add("amazon-adsystem.com");
        }};

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString().toLowerCase();
            if (url.startsWith(MAIN_URL) || Arrays.stream(ALLOWED_API).anyMatch(url::startsWith)) {
                return super.shouldInterceptRequest(view, request);
            }
            for (String adDomain : adDomains) {
                if (url.contains(adDomain)) {
                    return new WebResourceResponse("text/plain", "utf-8", null);
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            return !(url.startsWith(MAIN_URL) || Arrays.stream(ALLOWED_API).anyMatch(url::startsWith));
        }
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            isFullScreen = true;
            customView = view;
            customViewCallback = callback;
            fullScreenContainer.addView(customView);
            fullScreenContainer.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            hideSystemUI();
            swipeRefreshLayout.setEnabled(false);
        }

        @Override
        public void onHideCustomView() {
            if (customView != null) {
                isFullScreen = false;
                fullScreenContainer.setVisibility(View.GONE);
                fullScreenContainer.removeView(customView);
                customView = null;
                customViewCallback.onCustomViewHidden();
                webView.setVisibility(View.VISIBLE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                showSystemUI();
                swipeRefreshLayout.setEnabled(true);
            }
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            customViewCallback.onCustomViewHidden();
            fullScreenContainer.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            customView = null;
            isFullScreen = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            showSystemUI();
            swipeRefreshLayout.setEnabled(true);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
