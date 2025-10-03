package com.spring.aicodemother.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.spring.aicodemother.langgraph4j.model.ImageResource;
import com.spring.aicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pixabay插画搜索工具
 * 使用Pixabay API搜索高质量插画图片
 */
@Slf4j
@Component
public class UndrawIllustrationTool {

    private static final String PIXABAY_API_URL = "https://pixabay.com/api/";
    
    @Value("${pixabay.api-key:<Your Pixabay API Key>}")
    private String pixabayApiKey;

    @Tool("搜索插画图片，用于网站美化和装饰")
    public List<ImageResource> searchIllustrations(@P("搜索关键词") String query) {
        List<ImageResource> imageList = new ArrayList<>();
        
        if (StrUtil.isBlank(query)) {
            log.warn("搜索关键词不能为空");
            return imageList;
        }
        
        if ("<Your Pixabay API Key>".equals(pixabayApiKey)) {
            log.error("Pixabay API Key 未配置，请在 application.yml 中设置 pixabay.api-key");
            return imageList;
        }
        
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String apiUrl = buildPixabayUrl(encodedQuery);
            
            log.info("开始搜索插画，关键词: {}", query);
            
            try (HttpResponse response = HttpRequest.get(apiUrl)
                    .timeout(15000)
                    .header("User-Agent", "AI-Code-Mother/1.0")
                    .execute()) {
                
                if (!response.isOk()) {
                    log.error("Pixabay API 请求失败，状态码: {}, 响应: {}", response.getStatus(), response.body());
                    return imageList;
                }
                
                JSONObject result = JSONUtil.parseObj(response.body());
                return parsePixabayResponse(result);
                
            }
        } catch (Exception e) {
            log.error("搜索插画失败：{}", e.getMessage(), e);
        }
        
        return imageList;
    }
    
    /**
     * 构建Pixabay API请求URL
     */
    private String buildPixabayUrl(String encodedQuery) {
        return PIXABAY_API_URL + "?" +
                "key=" + pixabayApiKey +
                "&q=" + encodedQuery +
                "&image_type=illustration" +
                "&orientation=all" +
                "&category=" +
                "&min_width=640" +
                "&min_height=480" +
                "&safesearch=true" +
                "&order=popular" +
                "&per_page=12";
    }
    
    /**
     * 解析Pixabay API响应
     */
    private List<ImageResource> parsePixabayResponse(JSONObject result) {
        List<ImageResource> imageList = new ArrayList<>();
        
        if (result == null) {
            return imageList;
        }
        
        Integer total = result.getInt("total", 0);
        log.info("Pixabay搜索结果总数: {}", total);
        
        JSONArray hits = result.getJSONArray("hits");
        if (hits == null || hits.isEmpty()) {
            log.info("未找到匹配的插画");
            return imageList;
        }
        
        for (int i = 0; i < hits.size(); i++) {
            try {
                JSONObject hit = hits.getJSONObject(i);
                if (hit != null) {
                    ImageResource image = parseImageFromHit(hit);
                    if (image != null) {
                        imageList.add(image);
                    }
                }
            } catch (Exception e) {
                log.warn("解析第{}个图片失败: {}", i, e.getMessage());
            }
        }
        
        log.info("成功解析{}张插画", imageList.size());
        return imageList;
    }
    
    /**
     * 从Pixabay响应中解析单个图片信息
     */
    private ImageResource parseImageFromHit(JSONObject hit) {
        String webformatURL = hit.getStr("webformatURL");
        if (StrUtil.isBlank(webformatURL)) {
            return null;
        }
        
        String tags = hit.getStr("tags", "插画");
        Integer id = hit.getInt("id", 0);
        
        // 构建更友好的描述
        String description = buildImageDescription(tags, id);
        
        return ImageResource.builder()
                .category(ImageCategoryEnum.ILLUSTRATION)
                .description(description)
                .url(webformatURL)
                .build();
    }
    
    /**
     * 构建图片描述
     */
    private String buildImageDescription(String tags, Integer id) {
        if (StrUtil.isNotBlank(tags)) {
            // 取前3个标签作为描述
            String[] tagArray = tags.split(", ");
            if (tagArray.length > 0) {
                StringBuilder desc = new StringBuilder();
                for (int i = 0; i < Math.min(3, tagArray.length); i++) {
                    if (i > 0) desc.append(", ");
                    desc.append(tagArray[i]);
                }
                return desc.toString() + " 插画";
            }
        }
        return "插画 ID: " + id;
    }
}
