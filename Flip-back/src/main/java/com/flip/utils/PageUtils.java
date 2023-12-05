package com.flip.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageUtils {

    /**
     * 处理分页对象
     * @param page 分页对象
     * @param recordsKeyName 键值对集合中存放具体数据的键名
     * @return 键值对集合对象
     * @param <T> 具体数据的类型
     */
    public static <T> Map<String, Object> handlePage(IPage<T> page, String recordsKeyName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("currentPage", page.getCurrent());    // 当前页码
        map.put("totalPageNum", page.getPages());     // 总页数
        map.put("sizePerPage", page.getSize());       // 每页数量大小
        map.put("totalRecordsNum", page.getTotal());  // 总记录数
        map.put(recordsKeyName, page.getRecords());   // 具体数据
        return map;
    }

    /**
     * 处理分页对象
     * @param page 分页对象
     * @param recordsKeyName 键值对集合中存放具体数据的键名
     * @param records 具体数据
     * @return 键值对集合对象
     * @param <T> 具体数据的原类型
     * @param <R> 具体数据的新类型
     */
    public static <T, R> Map<String, Object> handlePage(IPage<T> page, String recordsKeyName, List<R> records) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("currentPage", page.getCurrent());    // 当前页码
        map.put("totalPageNum", page.getPages());     // 总页数
        map.put("sizePerPage", page.getSize());       // 每页数量大小
        map.put("totalRecordsNum", page.getTotal());  // 总记录数
        map.put(recordsKeyName, records);             // 具体数据
        return map;
    }

    /**
     * 获取分页对象中的具体数据集
     * @param map 存放了分页数据的键值对集合
     * @param recordsKeyName 具体数据的键名
     * @param recordClassType 具体数据的类型
     * @return 具体数据集合
     * @param <T> 具体数据的类型
     */
    public static <T> List<T> getRecords(Map<String, Object> map, String recordsKeyName, Class<T> recordClassType) {
        if (map.containsKey(recordsKeyName)) {
            List<T> out = new ArrayList<>();
            Object objects = map.get(recordsKeyName);
            if (objects instanceof List<?>) {
                ((List<?>) objects).forEach(o -> out.add(recordClassType.cast(o)));
                return out;
            }
        }
        return new ArrayList<>();
    }
}
