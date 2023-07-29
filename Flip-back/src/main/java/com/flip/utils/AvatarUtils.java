package com.flip.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;

import java.io.File;

public class AvatarUtils {

    public static String createAvatar(String avatarPath, String avatarPrefix, String avatarRemotePrefix,
                                      String avatarRemoteSuffix, String random, Long uid) {
        String remoteAvatar = avatarRemotePrefix + random + avatarRemoteSuffix;
        File localAvatarFile = FileUtil.file(avatarPath + uid); /* /Users/xxx/Desktop/Filp/avatar/UID */
        if (!localAvatarFile.exists()) {
            boolean ready = localAvatarFile.mkdirs();
            if (!ready) {
                return defaultAvatar(avatarPath, avatarPrefix, random, remoteAvatar);
            }
        }

        long size = HttpUtil.downloadFile(remoteAvatar, localAvatarFile);
        if (size > 0L) {
            File rawAvatarFile = FileUtil.file(localAvatarFile + "/" + random); /* /Users/xxx/Desktop/Filp/avatar/UID/Random */
            String newAvatarName = System.currentTimeMillis() + ".png"; /* TIME.png */
            FileUtil.rename(rawAvatarFile, newAvatarName, true);
            File newAvatarFile = FileUtil.file(localAvatarFile + "/" + newAvatarName); /* /Users/xxx/Desktop/Filp/avatar/UID/TIME.png */
            if (newAvatarFile.exists()) {
                return avatarPrefix + "/avatar/" + uid + "/" + newAvatarName; /* http://localhost:8080/avatar/UID/TIME.png */
            } else {
                return defaultAvatar(avatarPath, avatarPrefix, random, remoteAvatar);
            }
        } else {
            return defaultAvatar(avatarPath, avatarPrefix, random, remoteAvatar);
        }
    }

    public static String defaultAvatar(String avatarPath, String avatarPrefix, String random, String remoteAvatar) {
        File defaultAvatarPath = FileUtil.file(avatarPath + "default"); /* /Users/xxx/Desktop/Filp/avatar/default */
        if (!defaultAvatarPath.exists()) {
            boolean ready = defaultAvatarPath.mkdirs();
            if (!ready) return remoteAvatar;
        } else {
            File file = FileUtil.file(avatarPath + "default/default.png"); /* /Users/xxx/Desktop/Filp/avatar/default/default.png */
            if (file.exists()) {
                return avatarPrefix + "/avatar/default/default.png"; /* http://localhost:8080/avatar/default/default.png */
            }
        }

        long size = HttpUtil.downloadFile(remoteAvatar, defaultAvatarPath);
        if (size > 0L) {
            File rawAvatar = FileUtil.file(avatarPath + "default/" + random); /* /Users/xxx/Desktop/Filp/avatar/default/Random */
            FileUtil.rename(rawAvatar, "default.png", true);
            return avatarPrefix + "/avatar/default/default.png"; /* http://localhost:8080/avatar/default/default.png */
        } else return remoteAvatar;
    }
}
