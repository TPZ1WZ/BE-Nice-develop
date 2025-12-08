package com.proj.webprojrct.common.util;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

/**
 * 📄 Page Utility Class
 * Helper methods for pagination
 */
public class PageUtil {

    public static Map<String, Object> getPageInfo(Page<?> page) {
        Map<String, Object> pageInfo = new HashMap<>();
        
        pageInfo.put("currentPage", page.getNumber());
        pageInfo.put("pageSize", page.getSize());
        pageInfo.put("totalElements", page.getTotalElements());
        pageInfo.put("totalPages", page.getTotalPages());
        pageInfo.put("hasNext", page.hasNext());
        pageInfo.put("hasPrevious", page.hasPrevious());
        pageInfo.put("isFirst", page.isFirst());
        pageInfo.put("isLast", page.isLast());
        
        return pageInfo;
    }

    public static Map<String, Object> getPageInfo(Page<?> page, String sortBy, String sortDirection) {
        Map<String, Object> pageInfo = getPageInfo(page);
        pageInfo.put("sortBy", sortBy);
        pageInfo.put("sortDirection", sortDirection);
        
        return pageInfo;
    }
}