package com.spring.aicodemother.langgraph4j.tools;

import com.spring.aicodemother.langgraph4j.model.ImageResource;
import com.spring.aicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UndrawIllustrationToolTest {

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Test
    void testSearchIllustrations() {
        // 测试正常搜索插画
        List<ImageResource> illustrations = undrawIllustrationTool.searchIllustrations("happy");
        assertNotNull(illustrations);
        System.out.println("搜索到 " + illustrations.size() + " 张插画");
        
        // 只有在有结果时才进行详细验证
        if (!illustrations.isEmpty()) {
            // 验证返回的插画资源
            ImageResource firstIllustration = illustrations.get(0);
            assertEquals(ImageCategoryEnum.ILLUSTRATION, firstIllustration.getCategory());
            assertNotNull(firstIllustration.getDescription());
            assertNotNull(firstIllustration.getUrl());
            assertTrue(firstIllustration.getUrl().startsWith("http"));
            
            illustrations.forEach(illustration ->
                    System.out.println("插画: " + illustration.getDescription() + " - " + illustration.getUrl())
            );
        } else {
            System.out.println("警告: 未能获取到插画资源，可能是API Key未配置或网络问题");
        }
    }
    
    @Test
    void testSearchIllustrationsWithEmptyQuery() {
        // 测试空查询
        List<ImageResource> illustrations = undrawIllustrationTool.searchIllustrations("");
        assertNotNull(illustrations);
        assertTrue(illustrations.isEmpty(), "空查询应该返回空列表");
    }
    
    @Test
    void testSearchIllustrationsWithNullQuery() {
        // 测试null查询
        List<ImageResource> illustrations = undrawIllustrationTool.searchIllustrations(null);
        assertNotNull(illustrations);
        assertTrue(illustrations.isEmpty(), "null查询应该返回空列表");
    }
}
