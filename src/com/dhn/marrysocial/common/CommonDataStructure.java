package com.dhn.marrysocial.common;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.provider.DBContentChangeProvider;

public class CommonDataStructure {

    public static final String UID = "uid";
    public static final String COMMENT_CONTENT = "content";
    public static final String TOKEN = "token";
    public static final String AUTHOR_NAME = "author_name";
    public static final String COMMENT_ID = "tid";
    public static final String TOUID = "touid";
    public static final String BACKGROUND_PIC = "background_pic";
    public static final String BACKGROUD_PIC_NUM = "picnum";
    public static final String PASSWORD = "password";
    public static final String MAC = "mac";
    public static final String PHONE = "phone";
    public static final String FULLNAME = "fullname";
    public static final String NICKNAME = "nickname";
    public static final String GENDER = "gender";
    public static final String ASTRO = "astro";
    public static final String HOBBY = "hobby";
    public static final String INTRODUCE = "intro";
    public static final String LOGINSTATUS = "login_status";

    public static final String IMAGE_CACHE_DIR = ".com.dhn.marrysocial";
    public static final String DOWNLOAD_PICS_DIR = "downloadPics";
    public static final String HEAD_PICS_DIR = "headPics";
    public static final String BACKGROUND_PICS_DIR = "backgroundPics";

    public static final String HEAD_PICS_ORG_PATH = "http://static.pkjiao.com/avatar/";
    public static final String HEAD_PICS_THUMB_PATH = "http://static.pkjiao.com/102x102/avatar/";

    public static final String DOWNLOAD_PICS_DIR_URL = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + IMAGE_CACHE_DIR + File.separator + DOWNLOAD_PICS_DIR;
    public static final String HEAD_PICS_DIR_URL = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + IMAGE_CACHE_DIR + File.separator + HEAD_PICS_DIR;
    public static final String BACKGROUND_PICS_DIR_URL = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + IMAGE_CACHE_DIR + File.separator + BACKGROUND_PICS_DIR;

    public static final String HEAD_PIC_NAME = "head_pic_name";
    public static final String HEAD_PIC_CROP_NAME = "head_pic_crop_name";
    public static final String IS_FIRST_STARTUP = "is_first_startup";
    public static final String PREFS_LAIQIAN_DEFAULT = "marrysocial_default";

    public static final int THREAD_POOL_SIZE = 10;

    public static final int TIME_JUST_NOW = 5 * 60; // milliseconds
    public static final int TIME_FIVE_MINUTES_BEFORE = 5 * 60;
    public static final int TIME_TEN_MINUTES_BEFORE = 10 * 60;
    public static final int TIME_FIFTEEN_MINUTES_BEFORE = 15 * 60;
    public static final int TIME_THIRTY_MINUTES_BEFORE = 30 * 60;
    public static final int TIME_ONE_HOUR_BEFORE = 60 * 60;
    public static final int TIME_TWO_HOURS_BEFORE = 2 * 60 * 60;
    public static final int TIME_THREE_HOURS_BEFORE = 3 * 60 * 60;
    public static final int TIME_FIVE_HOURS_BEFORE = 5 * 60 * 60;
    public static final int TIME_ONE_DAY_BEFORE = 24 * 60 * 60;
    public static final int TIME_TWO_DAY_BEFORE = 2 * 24 * 60 * 60;

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

    public static final String CMD_TOPIC_DISPRAISE = "eyJpdiI6IndNaWVDQnNrejNiMlBLR2JYS2hYdkwzektCeTZTeGd"
            + "nTWJUNzMrUklIQzA9IiwidmFsdWUiOiI0M1oxR2RLNUJscFwvNGhpZGRSREJhMWlwakdPd0tnb2JxUUt4QyttcHlpV"
            + "T0iLCJtYWMiOiJjOWY2ODQ4YjBkMTY0ZjZjM2M5NDYyYjY5MmRmYTE1ZTFhMzA5NjAwMjk3MDlkMTU0MmEzOGE4ZDAzYzNkMjZkIn0=";

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

    public static final String CMD_INDIRECT_LIST = "eyJpdiI6IlBkc3g3WksxM0xRbnI2bjNrM3FyOW4yMU5GRU5tWmhJb"
            + "DZcLzd6KzRRMFY4PSIsInZhbHVlIjoiUU1BeUthbmhzRlVERkYyU0g0TjZ5Q1wvTENxR2R4Z0RHRDgyN2dKcmJXUTQ"
            + "9IiwibWFjIjoiZjA0YWNhZDY0OTM3YWQ3NTQxMjk0OWYzMjdhNzJiMmM5ZmM1YTQ1NTlkOWM4ZmE0ODk2MmZmYzc3NTAxYWE3OSJ9";

    public static final String CMD_REPLY_LIST = "eyJpdiI6IlRLNVZcL1ZUam5zb1VVb28xSk1jMG8ySXpCZWxUQ3ZqWnV2"
            + "NkhTNE1ZXC9JZz0iLCJ2YWx1ZSI6IlRLMSt1bEJJekhuTGJrK0s2SVwvTjdRTVwvNllGSWxcL1pQV3JUNGZjYTVROU"
            + "09IiwibWFjIjoiYjM0MTlhNWYwZDEyNDdlZTY1OTNjM2Q5ZDg3Yjg5OGIwZDg2M2NkM2Q5YTQzOWZmNWNhYjM0NjlhMjgzMGY2NiJ9";

    public static final String CMD_INDIRECT_NOTICE_LIST = "eyJpdiI6IjhxVnZJVzY4NlpNc2hwbTdmNXQ2SFRBM0tOY2wzS1lZK0N"
            + "ZNHJiMWpxMFk9IiwidmFsdWUiOiJlTEZGcEcwSXdBQ05yTDNBblhRb2d2QTE4c2hKR1FZek9HV2Q5MUh4aEU0PSIsI"
            + "m1hYyI6IjA1NDc0ZmI2ODAyMTk5NzczOWY5MTg3ODBkMTA5NWVkMmEzZDczZTNmZmQ4NDc0MjdlZGZhZDBhZTIyYjFiOWQifQ==";

    public static final String CMD_MYSELF_NOTICE_LIST = "eyJpdiI6IkJhOGw2b3FqUGZPZE96MTMxdFMzS2RtSDBDenlB"
            + "SDJza3BIVDRkWnM5TjA9IiwidmFsdWUiOiJqdVp3c1hQTGxwVk4rWjhlMk1jMW1iUThqM3FucFJ5TWw5Y2xMVlA2VG"
            + "JzPSIsIm1hYyI6IjYzYzJhNmRlZDI4MDQ0YjY4ODlhMTlhM2NjMGQ0OWIwY2MwZWZlZTZkNjVhYTBiNGE4MDNiNTVlY2NiMTgxOTAifQ==";

    public static final String CMD_TOPIC_COMMENT_LIST = "eyJpdiI6ImxCbVAwUUdqSjJJbHJoNk9RNlRjektZNnZsbDRGeW5HQm1F"
            + "ckowUURya2M9IiwidmFsdWUiOiJHY3pcL29iYmo3VklNWkNZSm9oanNnbGdEXC9wcEpvVkhxdXpISnMzc3ArVms9Ii"
            + "wibWFjIjoiOGU5MGQ1ZWVjYzQxOGU2ZTM2MjIyZTA1M2FhODNhNDNkMGQ0N2YzYjQ3ZmZlMTBlY2Q5NWNlZWYyOGIzZTM3NSJ9";

    public static final String CMD_CHAT_TEXT = "eyJpdiI6Im9GbWVUMGtWXC9Hb0ExNDlrdzAzbjNLU2tkdWdkN05CelpUb"
            + "U80TWhvV2hzPSIsInZhbHVlIjoiQnRPUXBlV3hEWGltaW12cDhtM2J1YzZJU3FTa0ZTXC9BbkVNYzRBQXJ3dE09Iiw"
            + "ibWFjIjoiZGE5YmQ1NjU0MjUyNzZkYmUxNGU1NjZjYmE0ZDQ4NDgwYjlhYWE3MzQyNDc2MWI4NzM0MDAxNzhlZjZiYTlkZiJ9";

    public static final String CMD_GET_CHAT_TEXT = "eyJpdiI6IjJ3MDE3RDArVW5CdG1oN0c3YXJRcDBDeVpuWmdFelp5e"
            + "lkyZWgxbTV2MGs9IiwidmFsdWUiOiJ1bDkzYXNiWTdqSjAyNXczVDA5dlduc0FOT2pzRGlvZTAxczAyeGlMb3E4PSI"
            + "sIm1hYyI6IjA2NzQ2MmM2MDdkMDZiNTRlMzM0NjA1NDFkODQ5NmZmN2NiMWY5YzljNWUwNmE0ZjM0NjQ2ODYzNmE1Y2Q3NDAifQ==";

    public static final String CMD_GET_USER_PROFILE = "eyJpdiI6IlJiMmdMNFwvS0drSE5rVnVsWHNudVdxVDVTdlEwWW"
            + "g1aHhmY1hrU3E3eHFRPSIsInZhbHVlIjoicEM1dzNkXC9PbE9oQVpDYlNkUkI5WU45YXJEbmkyXC9La3YxM3c2YnJO"
            + "aHBZPSIsIm1hYyI6IjRmOWU4YmM4ZWNkMGJlMTEzMWI5ZjljYWY3MTQ2OTZhN2NhNTc0ZDExNzgwYTUzMmE0MWVjYzllYjM1ODJjYWQifQ==";

    public static final String CMD_GET_USER_HEAD_PIC = "eyJpdiI6IkVabHBub21weW5jSlJnYlVzTlJJMnVsVjZ1RXFZc"
            + "FY4U0VTZWUraEc4Rlk9IiwidmFsdWUiOiJTdHV2bzlpUnNVXC9tcFJMU0JYSU03Z2c4RDAyWmZEdjVodjdyRGhmUkt"
            + "cL2c9IiwibWFjIjoiYTVlNjMwZGY0MzUxMDg2MDY4MmNkYzQ0N2NmNzk4Y2E3ZmVhNDFhMjg4YzAxMTg2MmRmMmY4MmYyZmM0ZTA3YyJ9";

    public static final String CMD_PROFILE_BACKGROUND = "eyJpdiI6IkFtbGlhRnNcL0R0aENDbXFQMmRKdExPVnJtc1o1"
            + "UGoyZ2dnSFpOdXNGc2tRPSIsInZhbHVlIjoiSnpZN01MZVd2RldENWJCZ21jQ1wvUmk5T2toQmRDRDFmd2xNTWdhSm"
            + "1CVlE9IiwibWFjIjoiZDU5ODZmNWVkYjBkMGEyYzYyOWI0OWRlNWNhNWUxYjlhNjNjOWMxNjk0OGY1NDZiODBhZmJmN2E3MDc2MjY4OCJ9";

    public static final String CMD_USER_REGISTER = "eyJpdiI6IlZmTHNNYUU5dVRkWUVBSVhheFR3dGppcWNnRXpnO"
            + "GNNejFrS3NwblNtbTA9IiwidmFsdWUiOiJvemVwbUV4TzI2UStjWjFnK21vSXk3ODhYbXpjbk1ESHpkbnhnc1lIcCt"
            + "zPSIsIm1hYyI6ImNkMTFkYWIyZjBlZTJiMDJmYWU1YTMxM2Y0ZDVjZDM2MzI3OWYwMzVmYzJkYjdjYWYwYTcyMDA4NzVkYzc0OTgifQ==";

    public static final String CMD_USER_LOGIN = "eyJpdiI6InZKYmZ3UWM0RFB2TURwQVdPRDVKcXNmM2xWQWxiU2tPUzIy"
            + "Qlh0S2pWdEE9IiwidmFsdWUiOiJkYmNCM3FEWFNKTmNWajhSVktaZURoR2FmT25kb2dyc2Qxa2poSjQ0XC9Jdz0iLC"
            + "JtYWMiOiIyYjgwZjlkNTFkOThmZjI4ZGE3ZDc1ODcxOGIyOTg2NzBjYzViOGNlNmZlZmE0YjBhODg4YzhlYjJmNWM0ZGU4In0=";

    public static final String CMD_UPDATE_USER_INFO = "eyJpdiI6IkxcL28zME00cGxBRkJLM2tMWWcxWEZJU2FmbTlXKz"
            + "hrZm04ZzM4UnR4bWFvPSIsInZhbHVlIjoiQTRoQXBSZVZzZHU1T3JJcEpcLzVwM05ldEFjZlV4M0ljYjJPVlhjNmF2"
            + "WkU9IiwibWFjIjoiNGM1MTk2ZDZhNzFlY2UzNDY2ZGJlZjY2M2EwM2JhYTA2ZjllZDc4N2I1ODhmYTBiY2M0YzFlMzU2NjUwZGZmYyJ9";

    public static final String CMD_UPLOAD_CONTACTS = "eyJpdiI6ImhRM3lsaDk5ZlNYOFcwZXJxdWVkaU9BdDVHMGNsYjF"
            + "xUnB4RlN1SE1XQ009IiwidmFsdWUiOiJoRExcL3o4QW1zNWtTc3hIZk9YVzBuODh1MytxZWFUTnV5VXFPOUtwY1NlQ"
            + "T0iLCJtYWMiOiJlNjRmNzYwOWU2YmVkOWFlNmE0MTY1ZDA4YzhkMDM3M2NmZDY4ZDljZjYzOTYwZjc3ZWY2Y2JhZjVhNDZkYWY0In0=";

    public static final String CMD_UPDATE_USERINFO = "eyJpdiI6IllRUFdscis2dHA4aW5PTCtCaE1tc1wvS2taUkNEYmd"
            + "NTEpTWmhJK09YSWswPSIsInZhbHVlIjoiT2Rpd05YTUhVamduTnZ1SGJ3ZDllMlFRY1hRWndudGJzOHNPXC9XQnZ0a"
            + "WM9IiwibWFjIjoiNTc2NDQ1MWU1ZjM3ZjI4ZDczZTA0ZmMzODk0ODhiOGY4ZjRhM2NhMTUxZDQ5NDc0NTNlYjEwZjdjZDc4NWQ2YSJ9";

    public static final String CMD_INDIRECT_SERVER_UPDATE = "eyJpdiI6IjJkVWRFWGJyU1hmZU91b2NvbVQxZTRMbGk3"
            + "RW9jS0w1dmVvVUZKTjhpNjQ9IiwidmFsdWUiOiJKWU1jWjdhMmVac213N09jb2drSTJ3UDgxV25TZWdHZHE3M1wvdV"
            + "FvcXFEST0iLCJtYWMiOiIwOGYzZDVlMDEzMGIyNjMwMzgwN2UzZjdjZjc4OThiN2U2NjZiMDQwMWUxOTc4NjRhMWQ2ZjA5NTI0Y2NiY2ExIn0=";

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
    public static final String URL_INDIRECT_LIST = URL_UPLOAD_COMMON
            + CMD_INDIRECT_LIST;
    public static final String URL_REPLY_LIST = URL_UPLOAD_COMMON
            + CMD_REPLY_LIST;
    public static final String URL_INDIRECT_NOTICE_LIST = URL_UPLOAD_COMMON
            + CMD_INDIRECT_NOTICE_LIST;
    public static final String URL_MYSELF_NOTICE_LIST = URL_UPLOAD_COMMON
            + CMD_MYSELF_NOTICE_LIST;
    public static final String URL_TOPIC_COMMENT_LIST = URL_UPLOAD_COMMON
            + CMD_TOPIC_COMMENT_LIST;
    public static final String URL_CHAT_TEXT = URL_UPLOAD_COMMON
            + CMD_CHAT_TEXT;
    public static final String URL_GET_CHAT_TEXT = URL_UPLOAD_COMMON
            + CMD_GET_CHAT_TEXT;
    public static final String URL_GET_USER_PROFILE = URL_UPLOAD_COMMON
            + CMD_GET_USER_PROFILE;
    public static final String URL_GET_USER_HEAD_PIC = URL_UPLOAD_COMMON
            + CMD_GET_USER_HEAD_PIC;
    public static final String URL_UPLOAD_HEAD_PIC = "http://www.pkjiao.com/upload/avatar";
    public static final String URL_PROFILE_BACKGROUND = URL_UPLOAD_COMMON
            + CMD_PROFILE_BACKGROUND;
    public static final String URL_USER_REGISTER = URL_UPLOAD_COMMON
            + CMD_USER_REGISTER;
    public static final String URL_USER_LOGIN = URL_UPLOAD_COMMON
            + CMD_USER_LOGIN;
    public static final String URL_UPDATE_USER_INFO = URL_UPLOAD_COMMON
            + CMD_UPDATE_USER_INFO;
    public static final String URL_UPLOAD_CONTACTS = URL_UPLOAD_COMMON
            + CMD_UPLOAD_CONTACTS;
    public static final String URL_UPDATE_USERINFO = URL_UPLOAD_COMMON
            + CMD_UPDATE_USERINFO;
    public static final String URL_INDIRECT_SERVER_UPDATE = URL_UPLOAD_COMMON
            + CMD_INDIRECT_SERVER_UPDATE;

    public static final Uri COMMENTURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_COMMENTS_TABLE);

    public static final Uri BRAVOURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_BRAVOS_TABLE);

    public static final Uri REPLYURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_REPLYS_TABLE);

    public static final Uri IMAGEURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_IMAGES_TABLE);

    public static final Uri CHATURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_CHATS_TABLE);

    public static final Uri BRIEFCHATURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE);

    public static final Uri HEADPICSURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE);

    public static final Uri HEADBACKGROUNDURL = Uri.parse("content://"
            + DBContentChangeProvider.AUTHORITY + "/"
            + MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE);

    public static final String REMOTE_ORG_PHOTO_PATH = "http://static.pkjiao.com/topic/";
    public static final String REMOTE_THUMB_PHOTO_PATH = "http://static.pkjiao.com/thumbnail/topic/";

    public static final String KEY_UPLOAD_TYPE = "upload_type";
    public static final String KEY_DELETE_TYPE = "delete_type";

    public static final int KEY_COMMENTS = 100;
    public static final int KEY_BRAVOS = 101;
    public static final int KEY_REPLYS = 102;
    public static final int KEY_IMAGES = 103;

    public static final int NOTICE_COMMENT = 1;
    public static final int NOTICE_REPLY = 2;
    public static final int NOTICE_BRAVO = 3;
    public static final int NOTICE_IMAGE = 4;

    public static final int INVALID_NUM = -1;
    public static final String INVALID_STR = "-1";

//    public static final String INDIRECTIDS = "4";

    public static final String KEY_SECRET_CODE = "marrysocial";

    public static final int LOGIN_STATUS_NO_USER = 0;
    public static final int LOGIN_STATUS_REGISTERED = 1;
    public static final int LONIN_STATUS_FILLED_INFO = 2;
    public static final int LOGIN_STATUS_LOGIN = 3;
    public static final int LONIN_STATUS_LOGOUT = 4;
    // public static final Uri REPLYURL = Uri.parse("content://"
    // + DataSetProvider.AUTHORITY + "/"
    // + MarrySocialDBHelper.DATABASE_COMMENTS_TABLE);

    // public static class ContactEntry {
    // public String contact_name;
    // public String contact_phone_number;
    // public int contact_id;
    // public String contact_sortKey;
    // }

    public static String[] HEADER_BKG_PATH = {
            "http://static.pkjiao.com/sysbackground/1.jpg",
            "http://static.pkjiao.com/sysbackground/2.jpg",
            "http://static.pkjiao.com/sysbackground/3.jpg",
            "http://static.pkjiao.com/sysbackground/4.jpg" };

    public static class UploadImageResultEntry {
        public boolean result;
        public int pos;
        public String photoId;
        public String orgUrl;
        public String thumbUrl;
    }

    public static class UploadHeadPicResultEntry {
        public String uid;
        public String orgUrl;
        public String bigThumbUrl;
        public String smallThumbUrl;
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

    public static class HeaderBackgroundEntry {
        public String photoName;
        public String photoLocalPath;
        public String photoRemotePath;
        public String currentStatus;
        public Bitmap bkgBitmap;
        public String headerBkgIndex;
    }

    public static class ContactEntry {
        public String contact_name;
        public String contact_phone_number;
        public int contact_id;
        public String contact_sortKey;
        public String direct_id;
        public String direct_uid;
    }
}
