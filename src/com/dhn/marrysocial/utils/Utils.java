package com.dhn.marrysocial.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.ChatMsgViewAdapter.IMsgViewType;
import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.base.ImagesItem;
import com.dhn.marrysocial.base.NoticesItem;
import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.common.CommonDataStructure.DownloadCommentsEntry;
import com.dhn.marrysocial.common.CommonDataStructure.UploadReplysResultEntry;
import com.dhn.marrysocial.database.MarrySocialDBHelper;

public class Utils {

    private static final String TAG = "MarrySocialUtils";

    public static int mThumbPhotoWidth = 720;
    public static int mCropCenterThumbPhotoWidth = 200;

    private static final int TIME_OUT = 100 * 1000; // 超时时间
    private static final String CHARSET = "utf-8";

    public static boolean isActiveNetWorkAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isGpsEnabled(Context context) {
        LocationManager lm = ((LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE));
        List<String> accessibleProviders = lm.getProviders(true);
        return accessibleProviders != null && accessibleProviders.size() > 0;
    }

    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return ((connManager.getActiveNetworkInfo() != null && connManager
                .getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || telManager
                .getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    public static boolean is3Grd(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null
                && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public Bitmap getRoundedCornerBitmap(Context context, float ratio) {

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.person_default_pic);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, bitmap.getWidth() / ratio,
                bitmap.getHeight() / ratio, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap createWidgetBitmap(Bitmap bitmap, int rotation) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        float scale;
        if (((rotation / 90) & 1) == 0) {
            scale = Math.max((float) mThumbPhotoWidth / w,
                    (float) mCropCenterThumbPhotoWidth / h);
        } else {
            scale = Math.max((float) mThumbPhotoWidth / h,
                    (float) mCropCenterThumbPhotoWidth / w);
        }

        Bitmap target = Bitmap.createBitmap(mThumbPhotoWidth,
                mCropCenterThumbPhotoWidth, Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.translate(mThumbPhotoWidth / 2, mCropCenterThumbPhotoWidth / 2);
        canvas.rotate(rotation);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, -w / 2, -h / 2, paint);
        bitmap.recycle();
        bitmap = null;
        return target;
    }

    public static void hideSoftInputMethod(View view) {
        InputMethodManager ime = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (ime.isActive()) {
            ime.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    public static void showSoftInputMethod(View view) {
        InputMethodManager ime = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // if (!ime.isActive()) {
        ime.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        // }
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size,
            boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size)
            return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w, h);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle)
            bitmap.recycle();
        return target;
    }

    public static Bitmap cropImages(Bitmap bitmap, int cropWidth,
            int cropHeight, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == cropWidth && h == cropHeight)
            return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) Math.max((float) cropWidth / w,
                (float) cropHeight / h);

        Bitmap target = Bitmap.createBitmap(cropWidth, cropHeight,
                getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((cropWidth - width) / 2f, (cropHeight - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle)
            bitmap.recycle();
        return target;
    }

    public static void recycleSilently(Bitmap bitmap) {
        if (bitmap == null)
            return;
        try {
            bitmap.recycle();
        } catch (Throwable t) {
            Log.w(TAG, "unable recycle bitmap", t);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, int rotation,
            boolean recycle) {
        if (rotation == 0)
            return source;
        int w = source.getWidth();
        int h = source.getHeight();
        Matrix m = new Matrix();
        m.postRotate(rotation);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
        if (recycle)
            source.recycle();
        return bitmap;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap decodeThumbnail(String filePath, Options options,
            int targetSize) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            return decodeThumbnail(fd, options, targetSize);
        } catch (Exception ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            Utils.closeSilently(fis);
        }
    }

    public static Bitmap decodeThumbnail(FileDescriptor fd, Options options,
            int targetSize) {
        if (options == null)
            options = new Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        int w = options.outWidth;
        int h = options.outHeight;

        // We center-crop the original image as it's micro thumbnail. In this
        // case,
        // we want to make sure the shorter side >= "targetSize".
        float scale = (float) targetSize / Math.min(w, h);
        options.inSampleSize = computeSampleSizeLarger(scale);

        // For an extremely wide image, e.g. 300x30000, we may got OOM when
        // decoding
        // it for TYPE_MICROTHUMBNAIL. So we add a max number of pixels limit
        // here.
        final int MAX_PIXEL_COUNT = 640000; // 400 x 1600
        if ((w / options.inSampleSize) * (h / options.inSampleSize) > MAX_PIXEL_COUNT) {
            options.inSampleSize = computeSampleSize(FloatMath
                    .sqrt((float) MAX_PIXEL_COUNT / (w * h)));
        }

        options.inJustDecodeBounds = false;

        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null)
            return null;

        // We need to resize down if the decoder does not support inSampleSize
        // (For example, GIF images)
        scale = (float) targetSize
                / (Math.min(result.getWidth(), result.getHeight()));

        if (scale <= 0.5)
            result = resizeBitmapByScale(result, scale, true);
        return ensureGLCompatibleBitmap(result);
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        try {
            if (fd != null)
                fd.close();
        } catch (Throwable t) {
            Log.w(TAG, "fail to close", t);
        }
    }

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            Log.w(TAG, "close fail", t);
        }
    }

    // Find the min x that 1 / x >= scale
    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) FloatMath.floor(1f / scale);
        if (initialSize <= 1)
            return 1;

        return initialSize <= 8 ? Utils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Returns the previous power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0
    public static int prevPowerOf2(int n) {
        if (n <= 0)
            throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    // Find the max x that 1 / x <= scale.
    public static int computeSampleSize(float scale) {
        Utils.assertTrue(scale > 0);
        int initialSize = Math.max(1, (int) FloatMath.ceil(1 / scale));
        return initialSize <= 8 ? Utils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    // Returns the next power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0 or
    // the answer overflows.
    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30))
            throw new IllegalArgumentException("n is invalid: " + n);
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    // Throws AssertionError if the input is false.
    public static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static Bitmap resizeBitmapByScale(Bitmap bitmap, float scale,
            boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth() && height == bitmap.getHeight())
            return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle)
            bitmap.recycle();
        return target;
    }

    // TODO: This function should not be called directly from
    // DecodeUtils.requestDecode(...), since we don't have the knowledge
    // if the bitmap will be uploaded to GL.
    public static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getConfig() != null)
            return bitmap;
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
        bitmap.recycle();
        return newBitmap;
    }

    public static byte[] Bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, output);
        return output.toByteArray();
    }

    public static CommonDataStructure.UploadImageResultEntry uploadImageFile(
            String requestURL, String filePath, String fileName, String uid,
            String tid, int pos) {
        Bitmap bitmap = decodeThumbnail(filePath, null, mThumbPhotoWidth);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("uid", uid);
        params.put("tid", tid);
        params.put("pos", String.valueOf(pos));
        return uploadBitmapFile(requestURL, bitmap, fileName, params);
    }

    public static CommonDataStructure.UploadImageResultEntry uploadBitmapFile(
            String requestURL, Bitmap bitmap, String fileName,
            HashMap<String, String> params) {

        HttpURLConnection connection = null;

        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String PREFIX = "--";
        String LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型

        CommonDataStructure.UploadImageResultEntry resultEntry = new CommonDataStructure.UploadImageResultEntry();

        try {
            URL url = new URL(requestURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(TIME_OUT);
            connection.setConnectTimeout(TIME_OUT);
            connection.setDoInput(true); // 允许输入流
            connection.setDoOutput(true); // 允许输出流
            connection.setUseCaches(false); // 不允许使用缓存
            connection.setRequestMethod("POST"); // 请求方式
            connection.setRequestProperty("Charset", CHARSET); // 设置编码
            connection.setRequestProperty("connection", "keep-alive");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE
                    + ";boundary=" + BOUNDARY);

            if (bitmap != null) {
                /**
                 * 当bitmap不为空，把bitmap包装并且上传
                 */
                DataOutputStream dos = new DataOutputStream(
                        connection.getOutputStream());
                StringBuffer sb = new StringBuffer();

                // 首先组拼文本类型的参数
                for (HashMap.Entry<String, String> entry : params.entrySet()) {
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\""
                            + entry.getKey() + "\"" + LINE_END);
                    sb.append("Content-Type: text/plain; charset=" + CHARSET
                            + LINE_END);
                    sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                    sb.append(LINE_END);
                    sb.append(entry.getValue());
                    sb.append(LINE_END);
                }

                // 上传图片内容
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"upfile\"; filename=\""
                        + fileName + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINE_END);
                sb.append(LINE_END);

                dos.write(sb.toString().getBytes());

                byte[] bytes = Bitmap2Bytes(bitmap);
                dos.write(bytes);

                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                        .getBytes();
                dos.write(end_data);

                dos.flush();
                dos.close();

                BufferedReader inputReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuffer resp = new StringBuffer();
                String line = null;
                while ((line = inputReader.readLine()) != null) {
                    resp.append(line);
                }
                inputReader.close();

                Log.e(TAG, "nannan response = " + resp);
                JSONObject response = new JSONObject(resp.toString());
                String code = response.getString("code");
                if (!"200".equalsIgnoreCase(code)) {
                    return resultEntry;
                }

                JSONObject respData = response.getJSONObject("data");
                int pos = respData.getInt("pos");
                String photoId = respData.getString("pid");
                String orginal = respData.getString("url");
                String thumb = respData.getString("thumbnailurl");
                resultEntry.result = true;
                resultEntry.photoId = photoId;
                resultEntry.pos = pos;
                resultEntry.orgUrl = orginal;
                resultEntry.thumbUrl = thumb;

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return resultEntry;
    }

    public static CommonDataStructure.UploadHeadPicResultEntry uploadHeadPicBitmap(
            String requestURL, String uid, Bitmap bitmap, String bitmapName) {

        HttpURLConnection connection = null;

        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String PREFIX = "--";
        String LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型

        CommonDataStructure.UploadHeadPicResultEntry resultEntry = new CommonDataStructure.UploadHeadPicResultEntry();

        try {
            URL url = new URL(requestURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(TIME_OUT);
            connection.setConnectTimeout(TIME_OUT);
            connection.setDoInput(true); // 允许输入流
            connection.setDoOutput(true); // 允许输出流
            connection.setUseCaches(false); // 不允许使用缓存
            connection.setRequestMethod("POST"); // 请求方式
            connection.setRequestProperty("Charset", CHARSET); // 设置编码
            connection.setRequestProperty("connection", "keep-alive");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE
                    + ";boundary=" + BOUNDARY);

            if (bitmap != null) {
                /**
                 * 当bitmap不为空，把bitmap包装并且上传
                 */
                DataOutputStream dos = new DataOutputStream(
                        connection.getOutputStream());
                StringBuffer sb = new StringBuffer();

                // 首先组拼文本类型的参数
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"" + "uid"
                        + "\"" + LINE_END);
                sb.append("Content-Type: text/plain; charset=" + CHARSET
                        + LINE_END);
                sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                sb.append(LINE_END);
                sb.append(uid);
                sb.append(LINE_END);

                // 上传图片内容
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"upfile\"; filename=\""
                        + bitmapName + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINE_END);
                sb.append(LINE_END);

                dos.write(sb.toString().getBytes());

                byte[] bytes = Bitmap2Bytes(bitmap);
                dos.write(bytes);

                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                        .getBytes();
                dos.write(end_data);

                dos.flush();
                dos.close();

                BufferedReader inputReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuffer resp = new StringBuffer();
                String line = null;
                while ((line = inputReader.readLine()) != null) {
                    resp.append(line);
                }
                inputReader.close();

                Log.e(TAG, "nannan response = " + resp);
                JSONObject response = new JSONObject(resp.toString());
                String code = response.getString("code");
                if (!"200".equalsIgnoreCase(code)) {
                    return resultEntry;
                }

                JSONObject respData = response.getJSONObject("data");
                String orginal = respData.getString("url");
                String bigThumb = respData.getString("bigurl");
                String smallThumb = respData.getString("smallurl");
                resultEntry.uid = uid;
                resultEntry.orgUrl = orginal;
                resultEntry.bigThumbUrl = bigThumb;
                resultEntry.smallThumbUrl = smallThumb;

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return resultEntry;
    }

    public static boolean uploadHeaderBackground(String RequestURL, String uId,
            String picnum) {

        boolean resultCode = false;

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return resultCode;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return resultCode;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(TIME_OUT);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject commentContent = new JSONObject();
            commentContent.put(CommonDataStructure.UID, uId);
            commentContent.put(CommonDataStructure.BACKGROUD_PIC_NUM, picnum);

            String content = "jsondata="
                    + URLEncoder.encode(commentContent.toString(), "UTF-8");
            Log.e(TAG, "nannan commentContent = " + commentContent.toString());
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return resultCode;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString());
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return resultCode;
            }

            resultCode = response.getBoolean("data");

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return resultCode;
    }

    public static String uploadCommentContentFile(String RequestURL,
            String uId, String contents) {

        String resultCode = null;

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return resultCode;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return resultCode;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject commentContent = new JSONObject();
            commentContent.put(CommonDataStructure.UID, uId);
            commentContent.put(CommonDataStructure.COMMENT_CONTENT, contents);

            String content = "jsondata="
                    + URLEncoder.encode(commentContent.toString(), "UTF-8");
            Log.e(TAG, "nannan commentContent = " + commentContent.toString());
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return resultCode;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString());
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return resultCode;
            }

            JSONObject respData = response.getJSONObject("data");
            resultCode = respData.getString("tid");

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return resultCode;
    }

    public static boolean uploadBravoFile(String RequestURL, String uId,
            String commentId) {

        Log.e(TAG, "nannan uploadBravoFile ");
        boolean resultCode = false;

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return resultCode;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return resultCode;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject bravoContent = new JSONObject();
            bravoContent.put(CommonDataStructure.UID, uId);
            bravoContent.put(CommonDataStructure.COMMENT_ID, commentId);

            String content = "jsondata="
                    + URLEncoder.encode(bravoContent.toString(), "UTF-8");
            Log.e(TAG, "nannan bravoContent = " + bravoContent.toString());
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return resultCode;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString());
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return resultCode;
            }

            resultCode = "true".equalsIgnoreCase(response.getString("data"));

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return resultCode;
    }

    public static boolean deleteBravoFileFromCloud(String RequestURL,
            String uId, String commentId) {

        Log.e(TAG, "nannan deleteBravoFileFromCloud ");
        boolean resultCode = false;

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return resultCode;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return resultCode;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject bravoContent = new JSONObject();
            bravoContent.put(CommonDataStructure.UID, uId);
            bravoContent.put(CommonDataStructure.COMMENT_ID, commentId);

            String content = "jsondata="
                    + URLEncoder.encode(bravoContent.toString(), "UTF-8");
            Log.e(TAG, "nannan bravoContent = " + bravoContent.toString());
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return resultCode;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString());
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return resultCode;
            }

            resultCode = "true".equalsIgnoreCase(response.getString("data"));

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return resultCode;
    }

    public static UploadReplysResultEntry uploadReplyFile(String RequestURL,
            String uId, String commentId, String reply) {

        Log.e(TAG, "nannan uploadReplyFile ");
        UploadReplysResultEntry result = new UploadReplysResultEntry();

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return result;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return result;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject replyContent = new JSONObject();
            replyContent.put(CommonDataStructure.UID, uId);
            replyContent.put(CommonDataStructure.COMMENT_ID, commentId);
            replyContent.put(CommonDataStructure.COMMENT_CONTENT, reply);

            String content = "jsondata="
                    + URLEncoder.encode(replyContent.toString(), "UTF-8");
            Log.e(TAG, "nannan replyContent = " + replyContent.toString());
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return result;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString());
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return result;
            }

            JSONObject respData = response.getJSONObject("data");
            String replyId = respData.getString("rid");
            String addTime = respData.getString("addtime");
            result.uId = uId;
            result.commentId = commentId;
            result.replyId = replyId;
            result.addTime = addTime;

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    public static String getHttpToken(String RequestURL, String uId) {

        String resultCode = null;

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        BufferedReader inputReader = null;
        OutputStreamWriter outputWriter = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return resultCode;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return resultCode;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputWriter = new OutputStreamWriter(connection.getOutputStream());
            outputWriter.write("uid=" + uId);
            Log.e(TAG, "uid = " + uId);
            outputWriter.flush();
            outputWriter.close();

            // outputStream = new
            // DataOutputStream(connection.getOutputStream());
            // JSONObject token = new JSONObject();
            // token.put(CommonDataStructure.UID, uId);
            //
            // String content = "jsondata="
            // + URLEncoder.encode(token.toString(), "UTF-8");
            // Log.e(TAG, "nannan commentContent = " + token.toString());
            // Log.e(TAG, "nannan content = " + content);
            // if (content == null)
            // return resultCode;
            //
            // outputStream.writeBytes(content);
            // outputStream.flush();
            // outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            inputReader.close();

            Log.e(TAG, "nannan line = " + resp.toString());
            JSONObject response = new JSONObject(resp.toString());
            resultCode = response.getString("token");
            if (resultCode != null) {
                return resultCode;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return resultCode;
    }

    public static boolean isHttpTokenValid(String RequestURL, String uId,
            String token) {

        boolean resultCode = false;

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return resultCode;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return resultCode;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputWriter = new OutputStreamWriter(connection.getOutputStream());
            outputWriter.write("uid=" + uId + "&token=" + token);
            Log.e(TAG, "uid = " + uId);
            Log.e(TAG, "token = " + token);
            outputWriter.flush();
            outputWriter.close();

            // outputStream = new
            // DataOutputStream(connection.getOutputStream());
            // JSONObject tokenEntry = new JSONObject();
            // tokenEntry.put(CommonDataStructure.UID, uId);
            // tokenEntry.put(CommonDataStructure.TOKEN, token);
            //
            // String content = "jsondata="
            // + URLEncoder.encode(tokenEntry.toString(), "UTF-8");
            // Log.e(TAG, "nannan commentContent = " + tokenEntry.toString());
            // Log.e(TAG, "nannan content = " + content);
            // if (content == null)
            // return resultCode;
            //
            // outputStream.writeBytes(content);
            // outputStream.flush();
            // outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            inputReader.close();

            Log.e(TAG, "nannan line = " + resp.toString());
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return resultCode;
            }
            String data = response.getString("data");
            if ("true".equalsIgnoreCase(data)) {
                resultCode = true;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return resultCode;
    }

    public static String getAddedTimeTitle(Context context, String time) {
        Long just_now = System.currentTimeMillis() / 1000;
        Long added_time = Long.valueOf(time);

        int timeSpaces = (int) (just_now - added_time);
        if (0 <= timeSpaces
                && timeSpaces < CommonDataStructure.TIME_FIVE_MINUTES_BEFORE) {
            return context.getString(R.string.time_just_now);
        } else if (timeSpaces < CommonDataStructure.TIME_TEN_MINUTES_BEFORE) {
            return context.getString(R.string.time_five_seconds_before);
        } else if (timeSpaces < CommonDataStructure.TIME_FIFTEEN_MINUTES_BEFORE) {
            return context.getString(R.string.time_ten_seconds_before);
        } else if (timeSpaces < CommonDataStructure.TIME_THIRTY_MINUTES_BEFORE) {
            return context.getString(R.string.time_fifteen_seconds_before);
        } else if (timeSpaces < CommonDataStructure.TIME_ONE_HOUR_BEFORE) {
            return context.getString(R.string.time_thirty_seconds_before);
        } else if (timeSpaces < CommonDataStructure.TIME_TWO_HOURS_BEFORE) {
            return context.getString(R.string.time_one_hour_before);
        } else if (timeSpaces < CommonDataStructure.TIME_THREE_HOURS_BEFORE) {
            return context.getString(R.string.time_two_hours_before);
        } else if (timeSpaces < CommonDataStructure.TIME_FIVE_HOURS_BEFORE) {
            return context.getString(R.string.time_three_hours_before);
        } else if (timeSpaces < CommonDataStructure.TIME_ONE_DAY_BEFORE) {
            return context.getString(R.string.time_five_hours_before);
        } else if (timeSpaces < CommonDataStructure.TIME_TWO_DAY_BEFORE) {
            return context.getString(R.string.time_one_day_before);
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date curDate = new Date(added_time * 1000);
            return formatter.format(curDate);
        }
    }

    public static ArrayList<CommentsItem> downloadCommentsWithReplyList(
            String RequestURL, DownloadCommentsEntry entry, String indirects) {

        Log.e(TAG, "nannan downloadCommentsList   4444444444");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<CommentsItem> commentItems = new ArrayList<CommentsItem>();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject commentContent = new JSONObject();
            commentContent.put("uid", "3");
            commentContent.put("indirectuids", indirects);
            // commentContent.put("datetime", entry.addedTime);

            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(commentContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONArray respData = response.getJSONArray("data");
            for (int index = 0; index < respData.length(); index++) {
                JSONObject comment = respData.getJSONObject(index);
                CommentsItem commentItem = new CommentsItem();
                commentItem.setUid(comment.getString("uid"));
                commentItem.setCommentId(comment.getString("tid"));
                commentItem.setAddTime(comment.getString("addtime"));
                commentItem.setContents(comment.getString("content"));
                commentItem.setRealName(comment.getString("fullname"));
                commentItem.setPhotoCount(Integer.valueOf(comment
                        .getString("pics")));
                int replyCount = Integer.valueOf(comment.getString("replies"));
                ArrayList<ReplysItem> replys = new ArrayList<ReplysItem>();
                if (replyCount > 0) {
                    JSONArray replyLists = comment.getJSONArray("replies_info");
                    for (int pointer = 0; pointer < replyLists.length(); pointer++) {
                        JSONObject item = replyLists.getJSONObject(pointer);
                        ReplysItem reply = new ReplysItem();
                        reply.setCommentId(item.getString("tid"));
                        reply.setReplyContents(item.getString("content"));
                        reply.setNickname(item.getString("fullname"));
                        reply.setUid(item.getString("tid"));
                        reply.setReplyId(item.getString("rid"));
                        reply.setReplyTime(item.getString("addtime"));
                        replys.add(reply);
                    }

                }
                commentItem.setReplyLists(replys);
                commentItems.add(commentItem);
            }

            reader.close();

            return commentItems;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static ArrayList<NoticesItem> downloadNoticesList(String RequestURL,
            String uId, String timeStamp, int noticeType) {

        Log.e(TAG, "nannan downloadNoticesList");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<NoticesItem> noticeItems = new ArrayList<NoticesItem>();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject noticeContent = new JSONObject();
            noticeContent.put("uid", uId);
            noticeContent.put("noticetype", noticeType);
            noticeContent.put("timestamp", timeStamp);

            Log.e(TAG, "nannan noticeContent  = " + noticeContent.toString());
            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(noticeContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            // Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONArray respData = response.getJSONArray("data");
            for (int index = 0; index < respData.length(); index++) {
                JSONObject notice = respData.getJSONObject(index);
                String noticeId = notice.getString("lid");
                String uid = notice.getString("uid");
                String fromUid = notice.getString("fromuid");
                String timeLine = notice.getString("timeline");
                int type = Integer.valueOf(notice.getString("noticetype"));
                String commentId = notice.getString("tid");
                int isReceived = Integer.valueOf(notice.getString("recived"));

                NoticesItem item = new NoticesItem();
                item.setNoticeId(noticeId);
                item.setUid(uid);
                item.setFromUid(fromUid);
                item.setTimeLine(timeLine);
                item.setNoticeType(type);
                item.setCommentId(commentId);
                item.setIsReceived(isReceived);
                noticeItems.add(item);
            }

            reader.close();

            return noticeItems;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static ArrayList<NoticesItem> downloadMyselfNoticesList(
            String RequestURL, String uId, String timeStamp, int noticeType) {

        Log.e(TAG, "nannan downloadNoticesList");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<NoticesItem> noticeItems = new ArrayList<NoticesItem>();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject noticeContent = new JSONObject();
            noticeContent.put("uid", uId);
            noticeContent.put("noticetype", noticeType);
            noticeContent.put("timestamp", timeStamp);

            Log.e(TAG, "nannan noticeContent  = " + noticeContent.toString());
            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(noticeContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONArray respData = response.getJSONArray("data");
            for (int index = 0; index < respData.length(); index++) {
                JSONObject notice = respData.getJSONObject(index);
                String noticeId = notice.getString("iid");
                String uid = notice.getString("uid");
                String timeLine = notice.getString("addtime");
                int type = Integer.valueOf(notice.getString("logtype"));
                String commentId = notice.getString("tid");

                NoticesItem item = new NoticesItem();
                item.setNoticeId(noticeId);
                item.setUid(uid);
                item.setFromUid(uid);
                item.setTimeLine(timeLine);
                item.setNoticeType(type);
                item.setCommentId(commentId);
                noticeItems.add(item);
            }

            reader.close();

            return noticeItems;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static ArrayList<ContactsInfo> downloadInDirectFriendsList(
            String RequestURL, String uId, String timeStamp) {

        Log.e(TAG, "nannan downloadDirectFriendsList ");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<ContactsInfo> contactsList = new ArrayList<ContactsInfo>();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject contactContent = new JSONObject();
            contactContent.put("uid", uId);
            contactContent.put("timestamp", timeStamp);

            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(contactContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONArray respData = response.getJSONArray("data");
            for (int index = 0; index < respData.length(); index++) {

                JSONObject contactsInfo = respData.getJSONObject(index);
                String inDirectId = contactsInfo.getString("indirectid");
                String fromDirectUid = contactsInfo.getString("fromdirectuid");
                String[] fromDirectUids = fromDirectUid.split(",");
                int directFriendsCount = fromDirectUids.length;

                JSONObject fromDirectName = contactsInfo
                        .getJSONObject("fromdirectname");
                String firstDirectName = fromDirectName
                        .getString(fromDirectUids[0]);
                StringBuffer directFriends = new StringBuffer();
                for (int point = 0; point < directFriendsCount; point++) {
                    directFriends.append(
                            fromDirectName.getString(fromDirectUids[point]))
                            .append(" ");
                }

                JSONObject userinfo = contactsInfo.getJSONObject("userinfo");
                String uid = userinfo.getString("uid");
                String phoneNum = userinfo.getString("phone");
                String nickname = userinfo.getString("nickname");
                String realname = userinfo.getString("realname");
                int avatar = Integer.valueOf(userinfo.getString("avatar"));
                int gender = Integer.valueOf(userinfo.getString("gender"));
                int astro = Integer.valueOf(userinfo.getString("astro"));
                int hobby = Integer.valueOf(userinfo.getString("hobby"));
                String headerbkg = userinfo.getString("systembackground");
                String introduce = userinfo.getString("intro");

                ContactsInfo contactItem = new ContactsInfo();
                contactItem.setUid(uid);
                contactItem.setPhoneNum(phoneNum);
                contactItem.setNickName(nickname);
                contactItem.setRealName(realname);
                contactItem.setHeadPic(avatar);
                contactItem.setGender(gender);
                contactItem.setAstro(astro);
                contactItem.setHobby(hobby);
                contactItem.setIntroduce(introduce);
                contactItem.setIndirectId(inDirectId);
                contactItem.setFirstDirectFriend(firstDirectName);
                contactItem.setDirectFriends(directFriends.toString());
                contactItem.setDirectFriendsCount(directFriendsCount);
                contactItem.setHeaderBkgIndex(headerbkg);
                contactsList.add(contactItem);
            }

            reader.close();

            return contactsList;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static ContactsInfo downloadUserInfo(String RequestURL, String uId) {

        Log.e(TAG, "nannan downloadUserInfo ");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ContactsInfo contact = new ContactsInfo();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject contactContent = new JSONObject();
            contactContent.put("uid", uId);

            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(contactContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONObject respData = response.getJSONObject("data");

            String uid = respData.getString("uid");
            String phoneNum = respData.getString("phone");
            String nickname = respData.getString("nickname");
            String realname = respData.getString("realname");
            int avatar = Integer.valueOf(respData.getString("avatar"));
            int gender = Integer.valueOf(respData.getString("gender"));
            int astro = Integer.valueOf(respData.getString("astro"));
            int hobby = Integer.valueOf(respData.getString("hobby"));
            String headerbkg = respData.getString("systembackground");
            String intruduce = respData.getString("intro");

            contact.setUid(uid);
            contact.setPhoneNum(phoneNum);
            contact.setNickName(nickname);
            contact.setRealName(realname);
            contact.setHeadPic(avatar);
            contact.setGender(gender);
            contact.setAstro(astro);
            contact.setHobby(hobby);
            contact.setIndirectId("-1");
            contact.setFirstDirectFriend(nickname);
            contact.setDirectFriends(nickname);
            contact.setDirectFriendsCount(0);
            contact.setHeaderBkgIndex(headerbkg);
            contact.setIntroduce(intruduce);

            reader.close();

            return contact;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static ArrayList<ReplysItem> downloadReplysList(String RequestURL,
            String uId, String commentId, String indirectIds, String timeStamp) {

        Log.e(TAG, "nannan downloadReplysList");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<ReplysItem> replyItems = new ArrayList<ReplysItem>();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject replyContent = new JSONObject();
            replyContent.put("uid", uId);
            replyContent.put("tid", commentId);
            replyContent.put("indirectuids", indirectIds);
            replyContent.put("datetime", timeStamp);

            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(replyContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            // Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONArray respData = response.getJSONArray("data");
            for (int index = 0; index < respData.length(); index++) {
                JSONObject reply = respData.getJSONObject(index);
                String replyid = reply.getString("rid");
                String commentid = reply.getString("tid");
                String uid = reply.getString("uid");
                String replycontent = reply.getString("content");
                String addtime = reply.getString("addtime");
                String bucketId = String.valueOf(addtime.hashCode());
                String nickname = reply.getString("fullname");

                ReplysItem item = new ReplysItem();
                item.setReplyId(replyid);
                item.setCommentId(commentid);
                item.setUid(uid);
                item.setReplyContents(replycontent);
                item.setReplyTime(addtime);
                item.setNickname(nickname);
                item.setBucketId(bucketId);

                replyItems.add(item);
            }

            reader.close();

            return replyItems;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static ArrayList<CommentsItem> downloadCommentsList(
            String RequestURL, String uid, String indirectid, String tid,
            String count, String timestamp) {

        Log.e(TAG, "nannan downloadCommentsList   4444444444");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<CommentsItem> commentItems = new ArrayList<CommentsItem>();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject commentContent = new JSONObject();
            commentContent.put("uid", uid);
            commentContent.put("indirectuids", indirectid);
            commentContent.put("tid", tid);
            commentContent.put("count", count);
            commentContent.put("datetime", timestamp);

            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(commentContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONArray respData = response.getJSONArray("data");
            for (int index = 0; index < respData.length(); index++) {
                JSONObject comment = respData.getJSONObject(index);
                CommentsItem commentItem = new CommentsItem();
                commentItem.setUid(comment.getString("uid"));
                commentItem.setCommentId(comment.getString("tid"));
                commentItem.setAddTime(comment.getString("addtime"));
                commentItem.setContents(comment.getString("content"));
                commentItem.setRealName(comment.getString("fullname"));
                commentItem.setNickName(comment.getString("fullname"));
                int photoCount = Integer.valueOf(comment.getString("pics"));
                commentItem.setPhotoCount(photoCount);
                if (photoCount > 0) {
                    ArrayList<ImagesItem> images = new ArrayList<ImagesItem>();
                    JSONObject picsInfo = comment.getJSONObject("pics_info");
                    Iterator<String> iterator = picsInfo.keys();
                    while (iterator.hasNext()) {
                        ImagesItem image = new ImagesItem();
                        image.setUid(comment.getString("uid"));
                        image.setCommentId(comment.getString("tid"));
                        image.setAddTime(comment.getString("addtime"));
                        image.setBucketId(String.valueOf(comment.getString(
                                "addtime").hashCode()));
                        String position = iterator.next();
                        image.setPhotoPosition(position);
                        JSONObject info = picsInfo.getJSONObject(position);
                        String pid = info.getString("pid");
                        String type = info.getString("ext");
                        image.setPhotoId(pid);
                        image.setPhotoType(type);
                        image.setPhotoName(comment.getString("tid") + "_"
                                + position);
                        image.setPhotoRemoteOrgPath(CommonDataStructure.REMOTE_ORG_PHOTO_PATH
                                + comment.getString("tid")
                                + "_"
                                + position
                                + "." + type);
                        image.setPhotoRemoteThumbPath(CommonDataStructure.REMOTE_THUMB_PHOTO_PATH
                                + comment.getString("tid")
                                + "_"
                                + position
                                + "." + type);
                        images.add(image);
                    }
                    commentItem.setImages(images);
                }
                commentItems.add(commentItem);
            }

            reader.close();

            return commentItems;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static void showMesage(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    public static File getCachedImageFile(String imageUri, String cacheFilePath) {
        File imageFile = null;
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String imageName = getImageName(imageUri);

                File cacheDir = new File(cacheFilePath);
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                    File nomedia = new File(cacheDir, ".nomedia");
                    nomedia.createNewFile();
                }

                imageFile = new File(cacheDir, imageName);
                if (imageFile.isFile() && imageFile.exists()) {
                    imageFile.delete();
                }
                Log.i(TAG, "exists:" + imageFile.exists() + ", cacheDir:"
                        + cacheDir + ", imageName:" + imageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getCacheFileError:" + e.getMessage());
        }

        return imageFile;
    }

    public static String getImageName(String path) {
        int index = path.lastIndexOf(File.separator);
        return path.substring(index + 1);
    }

    public static File downloadImageAndCache(String RequestURL,
            String cacheFilePath) {

        URL postUrl = null;
        HttpURLConnection connection = null;
        File imageFile = getCachedImageFile(RequestURL, cacheFilePath);

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            BufferedOutputStream output = null;
            output = new BufferedOutputStream(new FileOutputStream(imageFile));
            Log.e(TAG, "write file to " + imageFile.getAbsolutePath());

            byte[] buf = new byte[1024];
            int len = 0;
            // cache the image to local
            while ((len = input.read(buf)) > 0) {
                output.write(buf, 0, len);
            }

            input.close();
            output.close();

        } catch (IOException exp) {
            exp.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return imageFile;
    }

    public static Bitmap downloadHeadPicBitmap(String RequestURL) {

        URL postUrl = null;
        HttpURLConnection connection = null;
        Bitmap headPicBitmap = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            headPicBitmap = ImageUtils.inputStreamToBitmap(input);

            input.close();

        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return headPicBitmap;
    }

    public static String uploadChatMsg(String RequestURL, String fromUid,
            String toUid, String chatContent) {

        Log.e(TAG, "nannan uploadChatMsg ");
        String chatTime = "";

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return chatTime;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return chatTime;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject chatMsg = new JSONObject();
            chatMsg.put(CommonDataStructure.UID, fromUid);
            chatMsg.put(CommonDataStructure.TOUID, toUid);
            chatMsg.put(CommonDataStructure.COMMENT_CONTENT, chatContent);

            String content = "jsondata="
                    + URLEncoder.encode(chatMsg.toString(), "UTF-8");
            Log.e(TAG, "nannan chatMsg = " + chatMsg.toString());
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return chatTime;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString() + "#################");
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return chatTime;
            }

            chatTime = response.getString("data");

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return chatTime;
    }

    public static String registerUserInfo(String RequestURL, String phoneNum,
            String password, String macAddr) {

        Log.e(TAG, "nannan registerUserInfo ");
        String result = "";

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return result;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return result;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject chatMsg = new JSONObject();
            chatMsg.put(CommonDataStructure.PHONE, phoneNum);
            chatMsg.put(CommonDataStructure.PASSWORD, password);
            chatMsg.put(CommonDataStructure.MAC, macAddr);

            String content = "jsondata="
                    + URLEncoder.encode(chatMsg.toString(), "UTF-8");
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return result;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString() + "#################");
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return result;
            }

            result = response.getJSONObject("data").getString("uid");

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    public static String loginSystem(String RequestURL, String phoneNum,
            String password, String macAddr) {

        Log.e(TAG, "nannan loginSystem ");
        String result = "";

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return result;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return result;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject chatMsg = new JSONObject();
            chatMsg.put(CommonDataStructure.PHONE, phoneNum);
            chatMsg.put(CommonDataStructure.PASSWORD, password);
            chatMsg.put(CommonDataStructure.MAC, macAddr);

            String content = "jsondata="
                    + URLEncoder.encode(chatMsg.toString(), "UTF-8");
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return result;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString() + "#################");
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return result;
            }

            result = response.getJSONObject("data").getString("uid");

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    public static String updateUserInfo(String RequestURL, String uid,
            String nickname, int gender, int astro, int hobby, String intro) {

        Log.e(TAG, "nannan updateUserInfo ");
        String result = "";

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return result;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return result;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());
            JSONObject chatMsg = new JSONObject();
            chatMsg.put(CommonDataStructure.UID, uid);
            chatMsg.put(CommonDataStructure.NICKNAME, nickname);
            chatMsg.put(CommonDataStructure.GENDER, String.valueOf(gender));
            chatMsg.put(CommonDataStructure.ASTRO, String.valueOf(astro));
            chatMsg.put(CommonDataStructure.HOBBY, String.valueOf(hobby));
            chatMsg.put(CommonDataStructure.INTRODUCE, intro);

            String content = "jsondata="
                    + URLEncoder.encode(chatMsg.toString(), "UTF-8");
            Log.e(TAG, "nannan content = " + content);
            if (content == null)
                return result;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }
            // String line = inputReader.readLine();
            // inputReader.close();

            Log.e(TAG, "nannan resp = " + resp.toString() + "#################");
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return result;
            }

            result = response.getJSONObject("data").getString("uid");

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    public static void waitWithoutInterrupt(Object object) {
        try {
            object.wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "unexpected interrupt: " + object);
        }
    }

    public static boolean handleInterrruptedException(Throwable e) {
        // A helper to deal with the interrupt exception
        // If an interrupt detected, we will setup the bit again.
        if (e instanceof InterruptedIOException
                || e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return true;
        }
        return false;
    }

    public static ChatMsgItem downloadChatMsg(String RequestURL, String uId) {

        Log.e(TAG, "nannan downloadChatMsg");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ChatMsgItem chatItems = new ChatMsgItem();

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return null;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return null;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject replyContent = new JSONObject();
            replyContent.put("uid", uId);

            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(replyContent.toString(), "UTF-8");

            if (content == null)
                return null;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return null;
            }

            JSONObject reply = response.getJSONObject("data");
            String fromUid = reply.getString("from");
            String toUid = reply.getString("to");
            String chatmsg = reply.getString("content");
            String chattime = reply.getString("timeline");
            String chatId = toUid + "_" + fromUid;

            chatItems.setUid(uId);
            chatItems.setFromUid(fromUid);
            chatItems.setToUid(toUid);
            chatItems.setMsgType(IMsgViewType.IMVT_COM_MSG);
            chatItems.setChatContent(chatmsg);
            chatItems.setChatId(chatId);
            chatItems.setAddedTime(chattime);
            chatItems
                    .setCurrentStatus(MarrySocialDBHelper.DOWNLOAD_FROM_CLOUD_SUCCESS);

            reader.close();

            return chatItems;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    public static boolean isMobilePhoneNum(String mobiles) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][3578]\\d{9}";// "[1]"代表第1位为数字1，"[3578]"代表第二位可以为3、5、7、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }

    public static boolean isPassworkValid(String password) {
        return password.length() >= 6;
    }

    public static String getMacAddress(Context context) {
        String macAddress = "";
        try {
            WifiManager wifiMgr = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (wifiMgr == null ? null : wifiMgr
                    .getConnectionInfo());
            if (info != null) {
                macAddress = info.getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddress;
    }

    public static ArrayList<CommonDataStructure.ContactEntry> uploadUserContacts(
            String RequestURL, String uid,
            ArrayList<CommonDataStructure.ContactEntry> contactsList) {

        Log.e(TAG, "nannan uploadUserContacts ");
        ArrayList<CommonDataStructure.ContactEntry> contactEntrys = new ArrayList<CommonDataStructure.ContactEntry>();

        URL postUrl = null;
        DataOutputStream outputStream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter outputWriter = null;
        BufferedReader inputReader = null;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return contactEntrys;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return contactEntrys;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.connect();

            outputStream = new DataOutputStream(connection.getOutputStream());

            JSONArray contactList = new JSONArray();
            for (CommonDataStructure.ContactEntry entry : contactsList) {
                JSONObject contact = new JSONObject();
                contact.put(CommonDataStructure.FULLNAME, entry.contact_name);
                contact.put(CommonDataStructure.PHONE,
                        entry.contact_phone_number);
                contact.put(CommonDataStructure.UID, uid);

                contactList.put(contact);
            }
            JSONObject contact = new JSONObject();
            contact.put("contacts", contactList.toString());

            String content = "jsondata="
                    + URLEncoder.encode(contact.toString(), "UTF-8");
            Log.e(TAG, "nannan contacts = " + contactList.toString());
            Log.e(TAG, "nannan content = " + content);

            if (content == null)
                return contactEntrys;

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            inputReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = inputReader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp = " + resp.toString() + "#################");
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return contactEntrys;
            }

            JSONObject data = response.getJSONObject("data");
            Iterator<String> iterator = data.keys();
            while (iterator.hasNext()) {
                String phone_num = iterator.next();
                JSONObject result = data.getJSONObject(phone_num);
                String direct_id = result.getString("directid");
                String direct_uid = result.getString("directuid");
                String direct_name = result.getString("directname");
                CommonDataStructure.ContactEntry entry = new CommonDataStructure.ContactEntry();
                entry.direct_id = direct_id;
                entry.direct_uid = direct_uid;
                entry.contact_phone_number = phone_num;
                entry.contact_name = direct_name;
                contactEntrys.add(entry);
            }

            inputReader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return contactEntrys;
    }

    public static boolean updateIndirectServer(String RequestURL, String uId) {

        Log.e(TAG, "nannan updateIndirectServer ");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        boolean result = false;

        try {
            postUrl = new URL(RequestURL);
            if (postUrl == null)
                return result;

            connection = (HttpURLConnection) postUrl.openConnection();
            if (connection == null)
                return result;

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();

            OutputStream stream = connection.getOutputStream();
            output = new DataOutputStream(stream);

            JSONObject contactContent = new JSONObject();
            contactContent.put("uid", uId);

            String content = null;
            content = "jsondata="
                    + URLEncoder.encode(contactContent.toString(), "UTF-8");

            if (content == null)
                return result;

            output.writeBytes(content);
            output.flush();
            output.close();

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            StringBuffer resp = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }

            Log.e(TAG, "nannan resp 555555555555 = " + resp);
            JSONObject response = new JSONObject(resp.toString());
            String code = response.getString("code");
            if (!"200".equalsIgnoreCase(code)) {
                return result;
            }

            String data = response.getString("data");
            result = "true".equalsIgnoreCase(data);

            reader.close();

            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

}
