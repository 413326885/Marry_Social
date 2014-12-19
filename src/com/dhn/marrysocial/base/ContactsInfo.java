package com.dhn.marrysocial.base;

//    [uid] => 999        //系统用户唯一id
//    [phone] => 13611221101  //用户手机号码
//    [avatar] => 0       //用户头像id
//    [nickname] =>       //用户昵称
//    [realname] => max   //用户真实姓名(非登录名)
//    [gender] => x       //用户性别 x 未填  1 男  2  女
//    [astro] => 1        //星座 0 未填 1-12 对应星座
//    [hobby] =>          //爱好 1 男 0 女  x 未填
//    [lastlogin] => 0    //最后登录时间
//    [lastip] =>         //最后登录ip

public class ContactsInfo {

    public enum ASTRO {
        ARIES, // 白羊座
        TAURUS, // 金牛座
        GEMINI, // 双子座
        CANCER, // 巨蟹座
        LEO, // 狮子座
        VIRGO, // 处女座
        LIBRA, // 天秤座
        SCORPIO, // 天蝎座
        SAGITTARIUS, // 人马座
        CAPRICPRN, // 山羊座
        AQUARIUS, // 水瓶座
        PISCES // 双鱼座
    }

    public enum GENDER {
        MALE, // 男
        FEMALE // 女
    }

    private String mUid;
    private String mPhoneNum;
    private String mAvatar;
    private String mNikeName;
    private String mRealName;
    private String mGender;
    private String mAstro;
    private String mHobby;
    private String mLastLogin;
    private String mLastIp;
    private String[] mFriends;

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public void setPhoneNum(String mPhoneNum) {
        this.mPhoneNum = mPhoneNum;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String mAvatar) {
        this.mAvatar = mAvatar;
    }

    public String getNikeName() {
        return mNikeName;
    }

    public void setNikeName(String mNikeName) {
        this.mNikeName = mNikeName;
    }

    public String getRealName() {
        return mRealName;
    }

    public void setRealName(String mRealName) {
        this.mRealName = mRealName;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String mGender) {
        this.mGender = mGender;
    }

    public String getAstro() {
        return mAstro;
    }

    public void setAstro(String mAstro) {
        this.mAstro = mAstro;
    }

    public String getHobby() {
        return mHobby;
    }

    public void setHobby(String mHobby) {
        this.mHobby = mHobby;
    }

    public String getLastLogin() {
        return mLastLogin;
    }

    public void setLastLogin(String mLastLogin) {
        this.mLastLogin = mLastLogin;
    }

    public String getLastIp() {
        return mLastIp;
    }

    public void setLastIp(String mLastIp) {
        this.mLastIp = mLastIp;
    }

    public String[] getFriends() {
        return mFriends;
    }

    public void setFriends(String friend) {
        String[] friends = friend.split(",");
        this.mFriends = friends;
    }

}
