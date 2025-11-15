-- ======================================================
-- 添加用户可用的所有模型配置
-- 日期: 2025-10-10
-- 描述: 根据用户提供的55个可用模型更新ai_model_config表
-- ======================================================

USE ai_code_mother;

-- 清理旧的测试数据（保留已有的deepseek-reasoner等）
-- DELETE FROM ai_model_config WHERE id > 12;

-- ======================================================
-- 1. GPT-5 Codex 系列 (EXPERT级别，15积分/1K tokens)
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('gpt-5', 'GPT-5 标准版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 15, 'OpenAI GPT-5 标准版，顶级通用模型', 1, 100),
('gpt-5-minimal', 'GPT-5 精简版', 'openai', 'https://204992.xyz/v1', 'HARD', 10, 'GPT-5 精简版，性能优化', 1, 101),
('gpt-5-low', 'GPT-5 低配版', 'openai', 'https://204992.xyz/v1', 'MEDIUM', 5, 'GPT-5 低配版，经济实惠', 1, 102),
('gpt-5-medium', 'GPT-5 中配版', 'openai', 'https://204992.xyz/v1', 'HARD', 10, 'GPT-5 中配版，平衡性能', 1, 103),
('gpt-5-high', 'GPT-5 高配版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 15, 'GPT-5 高配版，卓越性能', 1, 104),
('gpt-5-codex', 'GPT-5 Codex 编程版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 15, 'GPT-5 Codex 编程专用，顶级代码生成', 1, 105),
('gpt-5-codex-low', 'GPT-5 Codex 低配版', 'openai', 'https://204992.xyz/v1', 'MEDIUM', 5, 'GPT-5 Codex 低配版，适合简单编程', 1, 106),
('gpt-5-codex-medium', 'GPT-5 Codex 中配版', 'openai', 'https://204992.xyz/v1', 'HARD', 10, 'GPT-5 Codex 中配版，标准编程', 1, 107),
('gpt-5-codex-high', 'GPT-5 Codex 高配版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 15, 'GPT-5 Codex 高配版，企业级编程', 1, 108),
('codex-mini-latest', 'Codex Mini 最新版', 'openai', 'https://204992.xyz/v1', 'SIMPLE', 2, 'Codex Mini 最新版，轻量级编程', 1, 109);

-- ======================================================
-- 2. Qwen 系列模型 (MEDIUM-EXPERT级别)
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('qwen3-coder', 'Qwen3 Coder', 'iflow', 'https://204992.xyz/v1', 'MEDIUM', 5, 'Qwen3 Coder 编程模型', 1, 200),
('qwen3-coder-plus', 'Qwen3 Coder Plus', 'iflow', 'https://204992.xyz/v1', 'HARD', 8, 'Qwen3 Coder Plus 增强编程', 1, 201),
('qwen3-max', 'Qwen3 Max', 'iflow', 'https://204992.xyz/v1', 'EXPERT', 12, 'Qwen3 Max 最大性能', 1, 202),
('qwen3-max-preview', 'Qwen3 Max 预览版', 'iflow', 'https://204992.xyz/v1', 'EXPERT', 15, 'Qwen3 Max 预览版，最新特性', 1, 203),
('qwen3-235b-a22b-instruct', 'Qwen3 235B A22B', 'iflow', 'https://204992.xyz/v1', 'EXPERT', 15, 'Qwen3 235B A22B 指令版', 1, 204),
('qwen3-235b-a22b-thinking-2507', 'Qwen3 235B 思维版', 'iflow', 'https://204992.xyz/v1', 'EXPERT', 18, 'Qwen3 235B 思维推理版', 1, 205);

-- ======================================================
-- 3. DeepSeek 系列模型 (MEDIUM-EXPERT级别)
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('deepseek-r1-0528-free', 'DeepSeek R1 0528 免费版', 'openrouter', 'https://204992.xyz/v1', 'HARD', 8, 'DeepSeek R1 0528 免费版', 1, 300),
('deepseek-v3.1-free', 'DeepSeek V3.1 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'DeepSeek V3.1 免费版', 1, 301),
('deepseek-v3.1', 'DeepSeek V3.1', 'iflow', 'https://204992.xyz/v1', 'HARD', 8, 'DeepSeek V3.1 标准版', 1, 302),
('deepseek-v3.2', 'DeepSeek V3.2', 'iflow', 'https://204992.xyz/v1', 'HARD', 10, 'DeepSeek V3.2 最新版', 1, 303),
('deepseek-r1', 'DeepSeek R1', 'iflow', 'https://204992.xyz/v1', 'HARD', 8, 'DeepSeek R1 推理模型', 1, 304);

-- ======================================================
-- 4. Kimi 系列模型 (MEDIUM-HARD级别) - 已移除 kimi-k2 和 kimi-k2-0905
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('kimi-k2-free', 'Kimi K2 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Moonshot Kimi K2 免费版', 1, 402);

-- ======================================================
-- 5. 其他免费优质模型 (SIMPLE-MEDIUM级别)
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('llama-3.3-70b-free', 'Llama 3.3 70B 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Meta Llama 3.3 70B 免费版', 1, 500),
('qwen-2.5-72b-free', 'Qwen 2.5 72B 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Qwen 2.5 72B 免费版', 1, 501),
('qwen3-235b-free', 'Qwen3 235B 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Qwen3 235B 免费版（推荐）', 1, 502),
('deepseek-r1-free', 'DeepSeek R1 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'DeepSeek R1 推理免费版', 1, 503),
('deepseek-r1-70b-free', 'DeepSeek R1 70B 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'DeepSeek R1 70B 免费版', 1, 504),
('qwen-coder-32b-free', 'Qwen Coder 32B 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Qwen Coder 32B 编程免费版', 1, 505),
('qwen3-coder-free', 'Qwen3 Coder 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Qwen3 Coder 编程免费版', 1, 506),
('devstral-free', 'Devstral 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Mistral Devstral 编程免费版', 1, 507);

-- ======================================================
-- 6. 多模态和特殊模型 (MEDIUM-HARD级别)
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('qwen-vl-72b-free', 'Qwen VL 72B 免费版', 'openrouter', 'https://204992.xyz/v1', 'HARD', 8, 'Qwen VL 72B 多模态免费版', 1, 600),
('qwen-vl-32b-free', 'Qwen VL 32B 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 5, 'Qwen VL 32B 多模态免费版', 1, 601),
('qwen3-vl-plus', 'Qwen3 VL Plus', 'iflow', 'https://204992.xyz/v1', 'HARD', 10, 'Qwen3 VL Plus 多模态', 1, 602),
('gemini-2.0-flash-free', 'Gemini 2.0 Flash 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Google Gemini 2.0 Flash', 1, 603);

-- ======================================================
-- 7. 其他特色模型
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('tongyi-deepresearch-free', '通义深度研究免费版', 'openrouter', 'https://204992.xyz/v1', 'HARD', 8, '阿里通义深度研究免费版', 1, 700),
('llama-4-maverick-free', 'Llama 4 Maverick 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Llama 4 Maverick 免费版', 1, 701),
('llama-4-scout-free', 'Llama 4 Scout 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Llama 4 Scout 免费版', 1, 702),
('mistral-small-3.2-free', 'Mistral Small 3.2 免费版', 'openrouter', 'https://204992.xyz/v1', 'SIMPLE', 2, 'Mistral Small 3.2 免费版', 1, 703),
('hunyuan-free', '腾讯混元免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, '腾讯混元免费版', 1, 704),
('nemotron-free', 'Nemotron 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'NVIDIA Nemotron 免费版', 1, 705),
('microsoft-r1-free', 'Microsoft R1 免费版', 'openrouter', 'https://204992.xyz/v1', 'HARD', 8, 'Microsoft R1 推理免费版', 1, 706),
('shisa-70b-free', 'Shisa 70B 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Shisa 70B 免费版', 1, 707),
('glm-4.5', 'GLM 4.5', 'iflow', 'https://204992.xyz/v1', 'HARD', 8, '智谱 GLM 4.5', 1, 708),
('tstars2.0', 'TStars 2.0', 'iflow', 'https://204992.xyz/v1', 'HARD', 8, 'TStars 2.0 模型', 1, 709);

-- ======================================================
-- 8. Akash ChatAPI 模型（部分可能超时，默认禁用或低优先级）
-- ======================================================
INSERT IGNORE INTO `ai_model_config` (`model_key`, `model_name`, `provider`, `base_url`, `tier`, `points_per_k_token`, `description`, `is_enabled`, `sort_order`) VALUES
('llama-3.3-70b', 'Llama 3.3 70B', 'akash', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Meta Llama 3.3 70B (Akash)', 0, 800),
('llama-3.1-8b', 'Llama 3.1 8B', 'akash', 'https://204992.xyz/v1', 'SIMPLE', 1, 'Meta Llama 3.1 8B (Akash)', 0, 801),
('llama-4-maverick-17b', 'Llama 4 Maverick 17B', 'akash', 'https://204992.xyz/v1', 'MEDIUM', 3, 'Llama 4 Maverick 17B (Akash)', 0, 802),
('deepseek-v3', 'DeepSeek V3', 'akash', 'https://204992.xyz/v1', 'HARD', 8, 'DeepSeek V3 (Akash)', 0, 803),
('deepseek-r1-32b', 'DeepSeek R1 32B', 'akash', 'https://204992.xyz/v1', 'MEDIUM', 5, 'DeepSeek R1 32B (Akash)', 0, 804),
('qwen3-235b', 'Qwen3 235B', 'akash', 'https://204992.xyz/v1', 'EXPERT', 12, 'Qwen3 235B (Akash)', 0, 805),
('gpt-oss-120b', 'GPT OSS 120B', 'akash', 'https://204992.xyz/v1', 'HARD', 10, 'GPT OSS 120B (Akash)', 0, 806),
('all-proxy', 'All Proxy Models', 'akash', 'https://204992.xyz/v1', 'MEDIUM', 5, 'All Proxy Models (Akash)', 0, 807);

-- ======================================================
-- 数据验证查询
-- ======================================================
-- 查看所有启用的模型
SELECT
    model_key,
    model_name,
    tier,
    points_per_k_token AS '积分/1K',
    provider,
    is_enabled AS '启用',
    description
FROM ai_model_config
WHERE is_delete = 0 AND is_enabled = 1
ORDER BY sort_order;

-- 按等级统计
SELECT
    tier AS '等级',
    COUNT(*) AS '模型数量',
    AVG(points_per_k_token) AS '平均积分',
    GROUP_CONCAT(model_name ORDER BY sort_order SEPARATOR ', ') AS '模型列表'
FROM ai_model_config
WHERE is_delete = 0 AND is_enabled = 1
GROUP BY tier
ORDER BY FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT');

-- 按提供商统计
SELECT
    provider AS '提供商',
    COUNT(*) AS '模型数量',
    SUM(CASE WHEN is_enabled = 1 THEN 1 ELSE 0 END) AS '启用数量'
FROM ai_model_config
WHERE is_delete = 0
GROUP BY provider
ORDER BY COUNT(*) DESC;

-- ======================================================
-- 脚本执行完成
-- ======================================================
