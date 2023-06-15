package com.flip.utils;

/**
 * 专用于生产 Redis Key 的工具类
 */
public class RedisKeyUtils {

    private static final String SPLIT = ":";
    private static final String PREFIX_CAPTCHA = "code:captcha";
    private static final String PREFIX_ACTIVATE_EMAIL_CODE = "code:emailCode";
    private static final String PREFIX_LOGGED_USER = "user:logged";
    private static final String PREFIX_TAG_OPTIONS = "unit:tag:options";
    private static final String PREFIX_TAGS = "unit:tag:tags";
    private static final String PREFIX_SENSITIVE_OBJS = "unit:sensitive:objs";
    private static final String PREFIX_SENSITIVE_WORDS = "unit:sensitive:words";
    private static final String PREFIX_USER_NUMBER = "status:user:number";
    private static final String PREFIX_POST_NUMBER = "status:post:number";

    public static String getCaptchaKey(String captchaOwnerUUID) {
        return PREFIX_CAPTCHA + SPLIT + captchaOwnerUUID;
    }

    public static String getEmailCodeKey(String uid) {
        return PREFIX_ACTIVATE_EMAIL_CODE + SPLIT + uid + SPLIT + "code";
    }

    public static String getEmailCodeRequestTimesKey(String uid) {
        return PREFIX_ACTIVATE_EMAIL_CODE + SPLIT + uid + SPLIT + "times";
    }

    public static String getLoggedUserKey(String uid) {
        return PREFIX_LOGGED_USER + SPLIT + uid;
    }

    public static String getTagsKey() {
        return PREFIX_TAGS;
    }

    public static String getTagOptionsKey() {
        return PREFIX_TAG_OPTIONS;
    }

    public static String getSensitiveObjsKey() {
        return PREFIX_SENSITIVE_OBJS;
    }

    public static String getSensitiveWordsKey() {
        return PREFIX_SENSITIVE_WORDS;
    }

    public static String getUserNumberKey() {
        return PREFIX_USER_NUMBER;
    }

    public static String getPostNumberKey() {
        return PREFIX_POST_NUMBER;
    }
}
