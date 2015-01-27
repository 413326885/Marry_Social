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
        SAGITTARIUS, // 射手座
        CAPRICPRN, // 摩羯座
        AQUARIUS, // 水瓶座
        PISCES // 双鱼座
    }

    public enum GENDER {
        FEMALE, // 女
        MALE // 男
    }

    private String mUid;
    private String mPhoneNum;
    private int mAvatar;
    private String mNikeName;
    private String mRealName;
    private int mGender;
    private int mAstro;
    private int mHobby;
    private String mLastLogin;
    private String mLastIp;
    private String mDirectFriends;
    private String mFirstDirectFriend;
    private int mDirectFriendsCount;
    private String mIndirectId;
    private String mHeaderBkgIndex;
    private String mIntroduce;

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

    public int getHeadPic() {
        return mAvatar;
    }

    public void setHeadPic(int mAvatar) {
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

    public int getGender() {
        return mGender;
    }

    public void setGender(int mGender) {
        this.mGender = mGender;
    }

    public int getAstro() {
        return mAstro;
    }

    public void setAstro(int mAstro) {
        this.mAstro = mAstro;
    }

    public int getHobby() {
        return mHobby;
    }

    public void setHobby(int mHobby) {
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

    public String getDirectFriends() {
        return mDirectFriends;
    }

    public void setDirectFriends(String mDirectFriends) {
        this.mDirectFriends = mDirectFriends;
    }

    public String getFirstDirectFriend() {
        return mFirstDirectFriend;
    }

    public void setFirstDirectFriend(String mFirstDirectFriend) {
        this.mFirstDirectFriend = mFirstDirectFriend;
    }

    public int getDirectFriendsCount() {
        return mDirectFriendsCount;
    }

    public void setDirectFriendsCount(int mDirectFriendsCount) {
        this.mDirectFriendsCount = mDirectFriendsCount;
    }

    public String getIndirectId() {
        return mIndirectId;
    }

    public void setIndirectId(String mIndirectId) {
        this.mIndirectId = mIndirectId;
    }

    public String getHeaderBkgIndex() {
        return mHeaderBkgIndex;
    }

    public void setHeaderBkgIndex(String mHeaderBkgIndex) {
        this.mHeaderBkgIndex = mHeaderBkgIndex;
    }

    public String getIntroduce() {
        return mIntroduce;
    }

    public void setIntroduce(String mIntroduce) {
        this.mIntroduce = mIntroduce;
    }
}
