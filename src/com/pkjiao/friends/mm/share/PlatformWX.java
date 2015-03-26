package com.pkjiao.friends.mm.share;

import net.sourceforge.simcpux.wxapi.WXEntryActivity;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class PlatformWX extends AbsSharePlatform {

    private IWXAPI mWXApi;

    @Override
    public void shareMsg(Activity context, ShareMsg msg, CallbackListener listener) {
        mWXApi = WXAPIFactory.createWXAPI(context, Constants.WX_APP_ID);
        if (mWXApi.isWXAppInstalled()) {
            Intent intent = new Intent(context, WXEntryActivity.class);
            intent.putExtra(Constants.KEY_BUNDLE_SHARE, msg);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "抱歉，您未安装微信，不能进行微信分享", Toast.LENGTH_SHORT)
                    .show();
        }

    }

}
