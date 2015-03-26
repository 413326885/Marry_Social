package com.pkjiao.friends.mm.share;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

public class PlatformSina extends AbsSharePlatform {

    public PlatformSina(Context appCtx, Map<String, String> params) {
        super(appCtx, params);
    }

    /** 微博 Web 授权类，提供登陆等功能 */
    private WeiboAuth mWeiboAuth;
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能 */
    private Oauth2AccessToken mAccessToken;
    /** 用于获取微博信息流等操作的API */
    private StatusesAPI mStatusesAPI;

    private boolean needReInit = false;

    @Override
    public void shareMsg(Activity ctx, AbsShareMsg msg,
            CallbackListener listener) {
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            if (needReInit) {
                mStatusesAPI = new StatusesAPI(mAccessToken);
            }
            if (msg instanceof MsgText) {
                MsgText mt = (MsgText) msg;

                StringBuilder contentBuilder = new StringBuilder();
                if (mt.title != null && mt.title.length() > 0) {
                    contentBuilder.append("【").append(mt.title).append("】");
                }
                if (mt.summary != null && mt.summary.length() > 0) {
                    contentBuilder.append(mt.summary);
                }
                if (mt.targetUrl != null && mt.targetUrl.length() > 0) {
                    contentBuilder.append(mt.targetUrl);
                }

                if (msg instanceof MsgImageText) {
                    String imgurl = ((MsgImageText) mt).imageUrl;
                    if (imgurl != null && imgurl.length() > 0) {
                        if (imgurl.startsWith("http")) {
                            sendUrlWeiBo(contentBuilder.toString(), imgurl,
                                    null, null, null, new MyRequestListener(
                                            listener));
                        } else {
                            sendImageWeiBo(contentBuilder.toString(),
                                    BitmapFactory.decodeFile(imgurl), null,
                                    null, new MyRequestListener(listener));
                        }
                    } else {
                        sendImageWeiBo(contentBuilder.toString(),
                                ((MsgImageText) mt).image, null, null,
                                new MyRequestListener(listener));
                    }
                } else {
                    sendImageWeiBo(contentBuilder.toString(), null, null, null,
                            new MyRequestListener(listener));
                }

            } else if (msg instanceof MsgImage) {
                shareImageMsg(ctx, (MsgImage) msg, listener);
            }

        } else {
            sinaOauth(ctx);
        }
    }

    public void shareImageMsg(Activity ctx, MsgImage msg,
            CallbackListener listener) {
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            if (needReInit) {
                mStatusesAPI = new StatusesAPI(mAccessToken);
            }
            MsgImage mt = (MsgImage) msg;
            String imageUrl = mt.imageUrl;
            String imagePath = mt.imagePath;
            Bitmap image = mt.image;
            if (imageUrl != null && imageUrl.length() > 0) {
                sendUrlWeiBo(null, imageUrl, null, null, null,
                        new MyRequestListener(listener));
            } else if (imagePath != null && imagePath.length() > 0) {
                sendImageWeiBo(null, BitmapFactory.decodeFile(imagePath), null,
                        null, new MyRequestListener(listener));
            } else {
                sendImageWeiBo(null, image, null, null, new MyRequestListener(
                        listener));
            }
        } else {
            sinaOauth(ctx);
        }
    }

    private void sendImageWeiBo(String content, Bitmap bitmap, String lat,
            String lon, RequestListener listener) {
        mStatusesAPI.upload(content, bitmap, null, null, listener);
    }

    private void sendUrlWeiBo(String status, String imageUrl, String pic_id,
            String lat, String lon, RequestListener listener) {
        mStatusesAPI
                .uploadUrlText(status, imageUrl, null, null, null, listener);
    }

    @Override
    public void init(Context ctx) {
        mAccessToken = AccessTokenKeeper.readAccessToken(ctx);
        mStatusesAPI = new StatusesAPI(mAccessToken);
    }

    /**
     * sina oauth
     */
    public void sinaOauth(Activity activity) {
        mWeiboAuth = new WeiboAuth(activity, Constants.APP_KEY,
                Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(activity, mWeiboAuth);
        mSsoHandler.authorize(new AuthListener(activity));
        needReInit = true;
    }

    class AuthListener implements WeiboAuthListener {

        private Activity activity;

        public AuthListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // 显示 Token
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(activity, mAccessToken);
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                Toast.makeText(activity, "error code " + code,
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(activity, "取消授权", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(activity, "Auth exception : " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    class MyRequestListener implements RequestListener {

        private CallbackListener listener;

        public MyRequestListener(CallbackListener listener) {
            this.listener = listener;
        }

        @Override
        public void onComplete(String arg0) {
            // TODO Auto-generated method stub
            if (listener != null) {
                listener.onSuccess();
            }
        }

        @Override
        public void onWeiboException(WeiboException arg0) {
            // TODO Auto-generated method stub
            if (listener != null) {
                listener.onFailure();
            }
        }

    }
}
