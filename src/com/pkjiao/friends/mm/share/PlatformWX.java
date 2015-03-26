package com.pkjiao.friends.mm.share;

import java.io.File;
import java.util.Map;

import net.sourceforge.simcpux.wxapi.WXEntryActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class PlatformWX extends AbsSharePlatform {

    public PlatformWX(Context appCtx, Map<String, String> params) {
        super(appCtx, params);
    }

    private IWXAPI api;

    @Override
    public void shareMsg(Activity ctx, AbsShareMsg msg,
            CallbackListener listener) {
        if (api.isWXAppInstalled()) {
            Intent i = new Intent(ctx, WXEntryActivity.class);
            i.putExtra(WXEntryActivity.BUNDLE_SHOW, msg);
            ctx.startActivity(i);
            // shareWebPage((MsgImageText) msg);
        } else {
            Toast.makeText(ctx, "抱歉，您未安装微信，不能进行微信分享", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public void shareWebPage(MsgImageText mt) {
        boolean isTimelineCb = false;
        if (mt.pType == PType.PLATFORM_WX_friends) {
            isTimelineCb = true;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = mt.targetUrl + "?from=weixin";
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = mt.title;
        msg.description = mt.summary;
        if (mt.image != null) {
            msg.thumbData = Util.bmpToByteArray(mt.image, true);
        } else if (mt.imageUrl != null) {
            msg.thumbData = Util.readFromFile(mt.imageUrl, 0, (int) new File(
                    mt.imageUrl).length());
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline
                : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    @Override
    public void init(Context ctx) {
        String appid = Constants.WX_APP_ID;
        if (appid != null) {
            api = WXAPIFactory.createWXAPI(ctx, Constants.WX_APP_ID);
            return;
        }
        throw new IllegalArgumentException("PlatformWX init error");
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis())
                : type + System.currentTimeMillis();
    }

}
