package com.flip.utils;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.StringTokenizer;

@Slf4j
public class IpUtils {
    /**
     * 获取访问者ip
     * @param request
     * @return
     */
    public static String getIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     *
     * @param checkString 被检测的字符串
     * @return 是否未知
     */
    public static boolean isUnknown(String checkString) {
        return StrUtil.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    public static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0) {
            final String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 获取访问者操作系统
     *
     * @param request
     * @return
     */
    public static String getOS(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");
        log.info("User-Agent: {}", agent);
        StringTokenizer st = new StringTokenizer(agent, ";");
        st.nextToken();
        // 得到访问者的操作系统名
        String os = st.nextToken();

        boolean isWin2K = agent.contains("Windows NT 5.0") || agent.contains("Windows 2000");
        if (isWin2K) {
            os = "Windows 2000";
        }
        boolean isWinXP = agent.contains("Windows NT 5.1") || agent.contains("Windows XP");
        if (isWinXP) {
            os = "Windows XP";
        }
        boolean isWin2003 = agent.contains("Windows NT 5.2") || agent.contains("Windows 2003");
        if (isWin2003) {
            os = "Windows 2003";
        }
        boolean isWinVista = agent.contains("Windows NT 6.0") || agent.contains("Windows Vista");
        if (isWinVista) {
            os = "Windows Vista";
        }
        boolean isWin7 = agent.contains("Windows NT 6.1") || agent.contains("Windows 7");
        if (isWin7) {
            os = "Windows 7";
        }
        boolean isWin8 = agent.contains("Windows NT 6.2") || agent.contains("Windows NT 6.3") || agent.contains("Windows 8");
        if (isWin8) {
            os = "Windows 8";
        }
        boolean isWin10 = agent.contains("Windows NT 10") || agent.contains("Windows 10");
        if (isWin10) {
            os = "Windows 10";
        }
        // mac
        boolean mac = agent.contains("Mac OS X");
        if (mac) {
            os = "macOS";
        }
        // linux
        boolean linux = agent.contains("Linux x86_64");
        if (linux) {
            os = "Linux x86_64";
        }
        // Android 5.0) AppleWebKit
        boolean android = agent.contains("Android 5.0) AppleWebKit");
        if (android) {
            os = "Android";
        }

        // 特殊处理U的情况
        if ("U".equalsIgnoreCase(os.trim())) {
            os = st.nextToken();
        }

        return os;
    }


    /**
     * 检查是否为内部IP地址
     *
     * @param ip IP地址
     * @return 结果
     */
    public static boolean internalIp(String ip) {
        return NetUtil.isInnerIP(ip) || "127.0.0.1".equals(ip);
    }

    /**
     * 检查是否为内部IP地址
     *
     * @param addr byte地址
     * @return 结果
     */
    private static boolean internalIp(byte[] addr) {
        if (ObjectUtil.isNull(addr) || addr.length < 2) {
            return true;
        }
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        // 10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        // 172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        // 192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                if (b1 == SECTION_6) {
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * 将IPv4地址转换成字节
     *
     * @param text IPv4地址
     * @return byte 字节
     */
    public static byte[] textToNumericFormatV4(String text) {
        if (text.length() == 0) {
            return null;
        }

        byte[] bytes = new byte[4];
        String[] elements = text.split("\\.", -1);
        try {
            long l;
            int i;
            switch (elements.length) {
                case 1:
                    l = Long.parseLong(elements[0]);
                    if ((l < 0L) || (l > 4294967295L)) {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l >> 24 & 0xFF);
                    bytes[1] = (byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 2:
                    l = Integer.parseInt(elements[0]);
                    if ((l < 0L) || (l > 255L)) {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l & 0xFF);
                    l = Integer.parseInt(elements[1]);
                    if ((l < 0L) || (l > 16777215L)) {
                        return null;
                    }
                    bytes[1] = (byte) (int) (l >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 3:
                    for (i = 0; i < 2; ++i) {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    l = Integer.parseInt(elements[2]);
                    if ((l < 0L) || (l > 65535L)) {
                        return null;
                    }
                    bytes[2] = (byte) (int) (l >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 4:
                    for (i = 0; i < 4; ++i) {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    break;
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return bytes;
    }
}
