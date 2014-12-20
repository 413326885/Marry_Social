package com.dhn.marrysocial.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.os.ParcelFileDescriptor;
import android.telephony.TelephonyManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.activity.EditCommentsActivity.UploadCommentContentEntry;
import com.dhn.marrysocial.base.CommentsItem;
import com.dhn.marrysocial.base.NoticesItem;
import com.dhn.marrysocial.base.ReplysItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.common.CommonDataStructure.DownloadCommentsEntry;
import com.dhn.marrysocial.common.CommonDataStructure.UploadReplysResultEntry;

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
                String orginal = respData.getString("url");
                String thumb = respData.getString("thumbnailurl");
                resultEntry.result = true;
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

    public static boolean uploadBravoFile(String RequestURL,
            String uId, String commentId) {

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
        Long just_now = System.currentTimeMillis();
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
            Date curDate = new Date(added_time);
            return formatter.format(curDate);
        }
    }

    public static ArrayList<CommentsItem> downloadCommentsList(String RequestURL, DownloadCommentsEntry entry) {

        Log.e(TAG, "nannan downloadCommentsList   4444444444");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<CommentsItem> commentItems = new ArrayList<CommentsItem> ();

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
            commentContent.put("uid", "2");
            commentContent.put("indirectuids", "2,5,6");
//            commentContent.put("datetime", entry.addedTime);

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
            for (int index = 0; index < respData.length(); index ++) {
                JSONObject comment = respData.getJSONObject(index);
                CommentsItem commentItem = new CommentsItem();
                commentItem.setUid(comment.getString("uid"));
                commentItem.setCommentId(comment.getString("tid"));
                commentItem.setAddTime(comment.getString("addtime"));
                commentItem.setContents(comment.getString("content"));
                commentItem.setFullName(comment.getString("fullname"));
                commentItem.setPhotoCount(Integer.valueOf(comment.getString("pics")));
                int replyCount = Integer.valueOf(comment.getString("replies"));
                ArrayList<ReplysItem> replys = new ArrayList<ReplysItem> ();
                if (replyCount > 0) {
                    JSONArray replyLists = comment.getJSONArray("replies_info");
                    for (int pointer = 0; pointer < replyLists.length(); pointer ++) {
                        JSONObject item = replyLists.getJSONObject(pointer);
                        ReplysItem reply = new ReplysItem();
                        reply.setCommentId(item.getString("tid"));
                        reply.setReplyContents(item.getString("content"));
                        reply.setFullName(item.getString("fullname"));
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

    public static ArrayList<NoticesItem> downloadNoticesList(String RequestURL, String uId, String timeStamp) {

        Log.e(TAG, "nannan downloadNoticesList");
        URL postUrl = null;
        HttpURLConnection connection = null;
        DataOutputStream output = null;
        ArrayList<NoticesItem> noticeItems = new ArrayList<NoticesItem> ();

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
            noticeContent.put("uid", "2");
            noticeContent.put("timestamp", "");

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
            for (int index = 0; index < respData.length(); index ++) {
                JSONObject notice = respData.getJSONObject(index);
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

    public static void showMesage(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }
}
