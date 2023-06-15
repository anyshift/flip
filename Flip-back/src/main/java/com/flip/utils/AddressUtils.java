package com.flip.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 根据IP获取真实地址
 */
@Slf4j
public class AddressUtils {

    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        if (IpUtils.internalIp(ip)) {
            return "内网IP";
        }
        String rspStr = HttpUtil.get(IP_URL + "?ip=" + ip + "&json=true", StandardCharsets.UTF_8);
        if (StrUtil.isEmpty(rspStr)) {
            log.error("获取地理位置异常 {}", ip);
            return UNKNOWN;
        }
        JSONObject obj = JSONUtil.parseObj(rspStr);
        String province = obj.getStr("pro");
        String city = obj.getStr("city");
        if (StrUtil.isBlank(province) && StrUtil.isBlank(city)) {
            return obj.getStr("addr");
        }
        return String.format("%s %s", province, city);
    }
}