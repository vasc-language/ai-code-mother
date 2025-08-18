package com.spring.aicodemother.core;

import com.spring.aicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("任务记录网站，50行代码以内", CodeGenTypeEnum.HTML, 1L);
        Assertions.assertNotNull(file);
    }

    /**
     * 测试流式生成和保存代码的功能。
     * <p>
     * 该测试方法会：
     * 1. 调用 {@link AiCodeGeneratorFacade
     *    传入需求和生成类型，以获取一个反应式流 (Flux)。
     * 2. 订阅这个流，并阻塞等待所有代码块 (String) 都被发射完成。
     * 3. 将返回的所有代码块收集到一个列表中。
     * 4. 断言最终的列表和拼接后的完整代码字符串不为空，以验证流式生成过程是否成功。
     * </p>
     */
    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("做一个Join2049的 To Do List，50行代码以内", CodeGenTypeEnum.MULTI_FILE, 314995582680838144L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }
}
