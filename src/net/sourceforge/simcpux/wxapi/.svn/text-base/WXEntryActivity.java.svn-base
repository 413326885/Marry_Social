package net.sourceforge.simcpux.wxapi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.jrj.sharesdk.Constants;
import com.jrj.sharesdk.PType;
import com.jrj.sharesdk.Util;
import com.jrj.sharesdk.msg.AbsShareMsg;
import com.jrj.sharesdk.msg.MsgImage;
import com.jrj.sharesdk.msg.MsgImageText;
import com.jrj.sharesdk.test.MainActivity;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

import net.sourceforge.simcpux.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	public final static String BUNDLE_SHOW="WXEnetry_jrj_show";
	private static final int THUMB_SIZE = 150;
	// IWXAPI
	private IWXAPI api;

	boolean isFirstIn = true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.wx_send_empty);
		api = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID, false);
		api.registerApp(Constants.WX_APP_ID);
		api.handleIntent(getIntent(), this);
		
		AbsShareMsg msg = getIntent().getParcelableExtra(BUNDLE_SHOW);
		shareMsg(msg);
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(!isFirstIn){
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
		api.handleIntent(intent, this);
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
	private void shareMsg(final AbsShareMsg msg){
		new Thread(){
			public void run() {
				if(msg instanceof MsgImage){
					shareImage((MsgImage) msg);
				}else if(msg instanceof MsgImageText){
					shareWebPage((MsgImageText)msg);
				}
			};
		}.start();
	}
	public void shareImage(MsgImage msg){
		if(msg==null)return;
		boolean isTimelineCb = false;
		if (msg.pType == PType.PLATFORM_WX_friends) {
			isTimelineCb = true;
		}
		WXImageObject wxImage = new WXImageObject();
		WXMediaMessage wxmsg = new WXMediaMessage(wxImage);
		if (msg.image != null) {
			wxmsg.thumbData = Util.bmpToByteArray(msg.image, true);
		} else if (msg.imagePath != null) {
			wxImage.imagePath=msg.imagePath;
			Bitmap bmp = BitmapFactory.decodeFile(msg.imagePath);
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
			bmp.recycle();
			wxmsg.thumbData =Util.bmpToByteArray(thumbBmp, true);
		}else if(msg.imageUrl!=null){
			wxImage.imageUrl=msg.imageUrl;
			Bitmap bmp=null;
			try {
				bmp = BitmapFactory.decodeStream(new URL(msg.imageUrl).openStream());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
			bmp.recycle();
			wxmsg.thumbData = Util.bmpToByteArray(thumbBmp, true);
		}
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("image");
		req.message = wxmsg;
		req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
	}
	public void shareWebPage(MsgImageText mt) {
		if(mt==null)return;
		boolean isTimelineCb = false;
		if (mt.pType == PType.PLATFORM_WX_friends) {
			isTimelineCb = true;
		}
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = mt.targetUrl;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = mt.title;
		msg.description = mt.summary;
		if (mt.image != null) {
			msg.thumbData = Util.bmpToByteArray(mt.image, true);
		} else if (mt.imageUrl != null) {
			msg.thumbData = Util.readFromFile(mt.imageUrl, 0, (int) new File(mt.imageUrl).length());
		}

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = isTimelineCb ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
	}
	private void goToGetMsg() {
		// Intent intent = new Intent(this, GetFromWXActivity.class);
		// intent.putExtras(getIntent());
		// startActivity(intent);
		// finish();
	}

	private void goToShowMsg(ShowMessageFromWX.Req showReq) {
		WXMediaMessage wxMsg = showReq.message;
		WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

		StringBuffer msg = new StringBuffer();
		msg.append("description: ");
		msg.append(wxMsg.description);
		msg.append("\n");
		msg.append("extInfo: ");
		msg.append(obj.extInfo);
		msg.append("\n");
		msg.append("filePath: ");
		msg.append(obj.filePath);

		// Intent intent = new Intent(this, ShowFromWXActivity.class);
		// intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
		// intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
		// intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
		// startActivity(intent);
		finish();
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}