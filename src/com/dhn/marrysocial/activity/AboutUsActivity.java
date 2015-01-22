package com.dhn.marrysocial.activity;

import com.dhn.marrysocial.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

public class AboutUsActivity extends Activity implements OnClickListener {

    private RelativeLayout mReturnBtn;
    private WebView mWebViewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about_us_layout);
        mReturnBtn = (RelativeLayout) findViewById(R.id.about_us_return);
        mWebViewBtn = (WebView) findViewById(R.id.about_us_url);

        mReturnBtn.setOnClickListener(this);
        WebSettings webSettings = mWebViewBtn.getSettings();
        // 设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 设置支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        // 加载需要显示的网页
        // mWebViewBtn.loadUrl("http://123.57.136.5");
        mWebViewBtn.loadUrl("http://wangjiwei.baijia.baidu.com/article/43422");
        // 设置Web视图
        mWebViewBtn.setWebViewClient(new MyWebViewClient());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.about_us_return: {
            this.finish();
            break;
        }
        default:
            break;
        }
    }

    // Web视图
    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
