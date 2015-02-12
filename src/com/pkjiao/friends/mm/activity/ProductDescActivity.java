package com.pkjiao.friends.mm.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.common.CommonDataStructure;

public class ProductDescActivity extends Activity implements OnClickListener {

    private RelativeLayout mReturnBtn;
    private WebView mWebViewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.product_desc_layout);
        mReturnBtn = (RelativeLayout) findViewById(R.id.product_return);
        mWebViewBtn = (WebView) findViewById(R.id.product_url);

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
        mWebViewBtn.loadUrl(CommonDataStructure.PRODUCT_DESC_PATH);
        // mWebViewBtn.loadUrl("http://wangjiwei.baijia.baidu.com/article/43422");
        // 设置Web视图
        mWebViewBtn.setWebViewClient(new MyWebViewClient());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.product_return: {
            this.finish();
            break;
        }
        default:
            break;
        }
    }

    @Override
    // 设置回退
    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebViewBtn.canGoBack()) {
            mWebViewBtn.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        finish();// 结束退出程序
        return false;
    }

    // Web视图
    private class MyWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
