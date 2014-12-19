package com.dhn.marrysocial.common;

import java.util.ArrayList;

import android.net.Uri;

import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.provider.DataSetProvider;

public class CommonDataStructure {

    public static final String UID = "uid";
    public static final String COMMENT_CONTENT = "content";
    public static final String TOKEN = "token";
    public static final String AUTHOR_NAME = "author_name";
    public static final String COMMENT_ID = "tid";

    public static final String IS_FIRST_STARTUP = "is_first_startup";

    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    public static final int THREAD_POOL_SIZE = 10;

    public static final int TIME_JUST_NOW = 5 * 60 * 1000; // milliseconds
    public static final int TIME_FIVE_MINUTES_BEFORE = 5 * 60 * 1000;
    public static final int TIME_TEN_MINUTES_BEFORE = 10 * 60 * 1000;
    public static final int TIME_FIFTEEN_MINUTES_BEFORE = 15 * 60 * 1000;
    public static final int TIME_THIRTY_MINUTES_BEFORE = 30 * 60 * 1000;
    public static final int TIME_ONE_HOUR_BEFORE = 60 * 60 * 1000;
    public static final int TIME_TWO_HOURS_BEFORE = 2 * 60 * 60 * 1000;
    public static final int TIME_THREE_HOURS_BEFORE = 3 * 60 * 60 * 1000;
    public static final int TIME_FIVE_HOURS_BEFORE = 5 * 60 * 60 * 1000;
    public static final int TIME_ONE_DAY_BEFORE = 24 * 60 * 60 * 1000;
    public static final int TIME_TWO_DAY_BEFORE = 2 * 24 * 60 * 60 * 1000;

    public static final String CMD_TOKEN_CHECK = "eyJpdiI6IjUydGNkWnlhOUhoVUJ2TmlmRUtUMjhTdlJKcTZLSGVvbzV"
            + "WXC9QRTd0TkVVPSIsInZhbHVlIjoiWmQ0dVFCVWpneWxWZXRwWWFmbEdaMVhLSmlibWgyaVpxSjhDbkJDTmRuRT0iL"
            + "CJtYWMiOiI1ZDVjYzNjMWQ0OTRhNWU2ZWIwMTk2MzRmODIxMWI4Mjc0Mjc5MmMwZjg5NGRmZmZkYTZjZGEyYWNiYWI4ZmUyIn0=";

    public static final String CMD_TOKEN_REFRESH = "eyJpdiI6Inp6cFBZYlF4NmUzUzlibWI1bXhVVm0yNFVjYVdOZEZ4R"
            + "0xJN3Z1MWJsa2c9IiwidmFsdWUiOiJHa3RSVlp5bStnSUdzUXhWaVc3YzV0clFZUTJDelU4KzBmNDFkaGJ5UmlNPSI"
            + "sIm1hYyI6IjkwMzFlYmE0NThjZDEyODIyOTg4OGRmY2NiZDdkODg3MDA3MzIwNjhmNGZmNTk1OWQyYmM3OTQ3MGNiZjQ3ODcifQ==";

    public static final String CMD_TOKEN_GET = "eyJpdiI6IlYxSk5jeVB6UThIMGE3Q1ZpTDlFaUJ0eDZ2cjFCMDUyWlJyM"
            + "VIrSjlGbjQ9IiwidmFsdWUiOiJqTjBjN09hZmpmOXIrTklCYnJDSjVsNkJJRnBmZWtsbmxMM3dLVWd1YWdVPSIsIm1"
            + "hYyI6ImFmZDkxYmNhNTA0OTZiYWYxZTI4Y2FiOTgyNThiNmMwNjU5MTVjODcxYWQyMTM5YzVkMWVhMmE1ZWVkZGVjNjAifQ==";

    public static final String CMD_TOPIC_PIC = "eyJpdiI6InpQRmc4aFgwVFU5c0hRUENLVzlTeG9Ca2M2Z1dvYmlrb0piW"
            + "VwvamFDWGJ3PSIsInZhbHVlIjoialF2ZzVMRWtcL2l1VnV4akF0V2tkSVwvS3hpRzdhT0RPbUVHaHZpRHNzdXZJPSI"
            + "sIm1hYyI6Ijg0YTBkNjUwY2U4ZjQ2ZDNiOGJlNWZkYjdkYTZhN2FjNzQ0ZTQzNjdmNjhkNDhiZmE0ZTA3ODI4NzMwOWU4NjQifQ==";

    public static final String CMD_TOPIC_POST = "eyJpdiI6Im9KdUhaZmFkSnlTWjdjRGtNbUhtXC9GK1wvTDVwUTZVNmR6"
            + "MDNQWm1qdk45ND0iLCJ2YWx1ZSI6IjY2cWJhc2d2WENSRkF4NnZjXC9UbWYrNDF5aW55RlZoVUlkbDI3bjVVSk1ZPS"
            + "IsIm1hYyI6Ijg2YTAxNjg4Y2MyNzFiNDIxMzU3MjMwNjFlOTViMmJhN2Y2MjMyOWIyMmEyZTMwNTBhY2QyMmQyMDU2MWMxMzQifQ==";

    public static final String CMD_TOPIC_DROP = "eyJpdiI6IlF2dHQ2bVZaM29CdThIQWdPUzc4NU4yOTZmZitPbVkyMjdI"
            + "MEdFVG9jQUE9IiwidmFsdWUiOiI5RitQcVFhbnRYU3Y2azg5Z3Q1M3NnUWgwbHJiRXhVcitKYjdhV1JXdG9rPSIsIm"
            + "1hYyI6IjQ2NmI5ZjZlMjVlNjNmNTc3MmVhMDhhYzVkN2Y2MzUyOTM2NWIzMDRmMjYwMWVkYTcxYWM3OGVlNmExNTA1NGIifQ==";

    public static final String CMD_TOPIC_PRAISE = "eyJpdiI6InJhdGdiaXRiejlwSjJCdFJyQkRxWWZYUDBmUzJYckYyNG"
            + "ZjQmZpYzV3b1U9IiwidmFsdWUiOiJINU9PYktaQVEwSElXMmsrcTFvazh2RTFYeXhoMVZ6UzJxQmJ3dUFnWXJZPSIs"
            + "Im1hYyI6IjVlZDE5YjBjNTU2MDk2ZmFmNGI5OThlODUwNmQyY2Q3YTQyYTgzYWE2N2Q2YTNmZjI5M2NlM2NkNjQ3M2U4YzYifQ==";

    public static final String CMD_TOPIC_DISPRAISE = "eyJpdiI6InlrXC95OE5QM0dYWmJqbjc0OVRTSHpBT3FhQ3ZsXC8"
            + "5WmI2TGRsTDBHMmF3ND0iLCJ2YWx1ZSI6ImwzYndVcHY5bk9salR4YzdNK2ZIQk1ab1BzVkt0UUlUMUhmUjZGUFMwV"
            + "jA9IiwibWFjIjoiYWY5NzA5OTNlNGZlYjdmM2I4OWI3ZmYxMjI0NjIzNzU5NWRjMjYzYzEyOWRiNTRjZGNhNTJmNWM2NDk4NzI0NSJ9";

    public static final String CMD_PROFILE = "eyJpdiI6InJKTGc1V3AreHBOS0hveHVRbEx0ZXptMWxYb09MTmtBRDNJS3R"
            + "UN2g3Z0k9IiwidmFsdWUiOiJmK1lLOTVpcnp4ZENqMG5xUElzSGpVMzlRNVp1SHNRSUdTUW03Ymd3OGpFPSIsIm1hY"
            + "yI6IjA1ZjZjMzNmZGVlNjQyMjdjMWEzMGJjNjk1MjIwYTkxNDA0N2QxOWRmMzNlMTNhZTkzMDhhOWJjNTAwNjNhNzcifQ==";

    public static final String CMD_PROFILE_AVATAR = "eyJpdiI6IlRmM1lQNnczekpTcDVIOHFOeFkzSmt4bmJFaUhIUlVE"
            + "K3htQ1BtOGZKS2s9IiwidmFsdWUiOiI3OFwvaStxK2pHRkdsY3lURDJwaEZ0aE5zWmlCRXJEbCtiTlM0RUJjRVJYYz"
            + "0iLCJtYWMiOiI0NDRmNjYwNjU0MjcwZjg5NDgwYzU4MGZjOTE0YjFmOWE0ODkyMmQ2Zjk4OGM3ZmRlYzZlOTRiYTJjZWI2YjZjIn0=";

    public static final String CMD_DIRECT_ADD = "eyJpdiI6InE2VEFEXC9mb1Qwb20zaXlYeVwvakpTZXJmMm91eURHRkw4"
            + "aldcL0FXT25cL3pVPSIsInZhbHVlIjoiK3lhejFubXhUVHg4WlljV21xSjRXMlB5dFM1eitWandXK1p6NVBUUk5oST"
            + "0iLCJtYWMiOiJmNjZmYTA0YTlhNTYyNmI4ZTAwMWZkMjA2ZmRjZjNlNWI5NThhYTZkZWM4NzdmMTdkOTUyNWYxNzUzNWJjNzViIn0=";

    public static final String CMD_TOPIC_REPLY_POST = "eyJpdiI6IkloWU1xYnNvUVNJalV6WkwwMzRqSkhPVnAydThhOW"
            + "FzQUV5V1Zja05FeDg9IiwidmFsdWUiOiJIT1wvNEhhcUI3aVh1dzF0eCtNRm5nUmpXbjZaYXpVNlwvdSs2VG5sRXN6"
            + "Umc9IiwibWFjIjoiZmQ3OWU5ZWRmZDU3M2MxYWExYTdkNTU4OTRmMTllZGE3NDkwOGY5YWMyZmJjZjUyMjVhM2Q0NzVmZDQ3OWY3MiJ9";

    public static final String CMD_TOPIC_COMMENT_WITH_REPLY_LIST = "eyJpdiI6IjZJSEM0ZjAyTUdxTXZldzNPWUR2T"
            + "0QrajZ4Zjh5RHlDUlRTM2F3aWF2Y0U9IiwidmFsdWUiOiJnWCt4bVpnaWpRV0E3aFBjXC9FXC9HRjU1SktwQWRoQ1h"
            + "FdXFTWm9GbHpzZTA9IiwibWFjIjoiYWM0ZDIwYmUyNzc4NzA3YjE0NzVkZTNjNDg0NmRkYzA5NDIzOGJiNjgxMzc2M2ZmZTAwZGE4OTNkY2IwZjI3OCJ9";

    public static final String URL_UPLOAD_COMMON = "http://www.pkjiao.com/verify/post/";

    public static final String URL_TOKEN_CHECK = URL_UPLOAD_COMMON
            + CMD_TOKEN_CHECK;
    public static final String URL_TOKEN_REFRESH = URL_UPLOAD_COMMON
            + CMD_TOKEN_REFRESH;
    public static final String URL_TOKEN_GET = URL_UPLOAD_COMMON
            + CMD_TOKEN_GET;
    public static final String URL_TOPIC_PIC = "http://www.pkjiao.com/upload/topicpic";
    public static final String URL_TOPIC_POST = URL_UPLOAD_COMMON
            + CMD_TOPIC_POST;
    public static final String URL_TOPIC_DROP = URL_UPLOAD_COMMON
            + CMD_TOPIC_DROP;
    public static final String URL_TOPIC_PRAISE = URL_UPLOAD_COMMON
            + CMD_TOPIC_PRAISE;
    public static final String URL_TOPIC_DISPRAISE = URL_UPLOAD_COMMON
            + CMD_TOPIC_DISPRAISE;
    public static final String URL_PROFILE = URL_UPLOAD_COMMON + CMD_PROFILE;
    public static final String URL_PROFILE_AVATAR = URL_UPLOAD_COMMON
            + CMD_PROFILE_AVATAR;
    public static final String URL_DIRECT_ADD = URL_UPLOAD_COMMON
            + CMD_DIRECT_ADD;
    public static final String URL_TOPIC_REPLY_POST = URL_UPLOAD_COMMON
            + CMD_TOPIC_REPLY_POST;
    public static final String URL_TOPIC_COMMENT_WITH_REPLY_LIST = URL_UPLOAD_COMMON
            + CMD_TOPIC_COMMENT_WITH_REPLY_LIST;

    public static final Uri COMMENTURL = Uri.parse("content://"
            + DataSetProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_COMMENTS_TABLE);

    public static final Uri BRAVOURL = Uri.parse("content://"
            + DataSetProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_BRAVOS_TABLE);

    public static final Uri REPLYURL = Uri.parse("content://"
            + DataSetProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_REPLYS_TABLE);

    public static final String KEY_UPLOAD_TYPE = "upload_type";
    public static final int KEY_COMMENTS = 100;
    public static final int KEY_BRAVOS = 101;
    public static final int KEY_REPLYS = 102;

    // public static final Uri REPLYURL = Uri.parse("content://"
    // + DataSetProvider.AUTHORITY + "/"
    // + MarrySocialDBHelper.DATABASE_COMMENTS_TABLE);

    // public static class ContactEntry {
    // public String contact_name;
    // public String contact_phone_number;
    // public int contact_id;
    // public String contact_sortKey;
    // }

    public static class UploadImageResultEntry {
        public boolean result;
        public int pos;
        public String orgUrl;
        public String thumbUrl;
    }

    public static class DownloadCommentsEntry {
        public String uid;
        public ArrayList<String> indirectUids;
        public String addedTime;
    }

    public static class UploadReplysResultEntry {
        public String uId;
        public String commentId;
        public String replyId;
        public String addTime;
    }
}
