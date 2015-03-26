package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.share.Constants;
import com.pkjiao.friends.mm.share.PType;
import com.pkjiao.friends.mm.share.ShareMsg;
import com.pkjiao.friends.mm.share.Util;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final int THUMB_SIZE = 150;

    private IWXAPI mWXApi;

    boolean isFirstIn = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wx_send_empty);
        mWXApi = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID, false);
        mWXApi.registerApp(Constants.WX_APP_ID);
        mWXApi.handleIntent(getIntent(), this);

        ShareMsg msg = getIntent().getParcelableExtra(
                Constants.KEY_BUNDLE_SHARE);
        shareMsg(msg);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstIn) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstIn = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        mWXApi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
        case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
            goToGetMsg();
            break;
        case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
            goToShowMsg((ShowMessageFromWX.Req) req);
            break;
        default:
            break;
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        int result = 0;

        switch (resp.errCode) {
        case BaseResp.ErrCode.ERR_OK:
            result = R.string.errcode_success;
            break;
        case BaseResp.ErrCode.ERR_USER_CANCEL:
            result = R.string.errcode_cancel;
            break;
        case BaseResp.ErrCode.ERR_AUTH_DENIED:
            result = R.string.errcode_deny;
            break;
        default:
            result = R.string.errcode_unknown;
            break;
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        finish();
    }

    private void shareMsg(final ShareMsg msg) {
        new Thread() {
            public void run() {
                shareWebPage(msg);
            };
        }.start();
    }

    public void shareWebPage(ShareMsg msg) {

        if (msg == null)
            return;

        boolean isTimelineCb = false;
        if (msg.pType == PType.PLATFORM_WX_friends) {
            isTimelineCb = true;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = msg.targetUrl;
        WXMediaMessage mediaMsg = new WXMediaMessage(webpage);
        mediaMsg.title = msg.title;
        mediaMsg.description = msg.summary;

        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        mediaMsg.thumbData = Util.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = mediaMsg;
        req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline
                : SendMessageToWX.Req.WXSceneSession;
        mWXApi.sendReq(req);
    }

    private void goToGetMsg() {
        finish();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        finish();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }
}