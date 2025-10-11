-- 添加24个精选模型到数据库
-- 使用UTF8编码避免乱码
-- 按积分等级划分: SIMPLE(1-2分) MEDIUM(3-5分) HARD(8-10分) EXPERT(12-18分)

-- 先清理旧数据
DELETE FROM ai_model_config WHERE is_delete = 0;

-- ===========================================
-- SIMPLE级别 (1-2积分/1K tokens) - 1个模型
-- ===========================================
INSERT INTO `ai_model_config`
(model_key, model_name, provider, base_url, tier, points_per_k_token, description, is_enabled, sort_order)
VALUES
('codex-mini-latest', 'Codex Mini 最新版', 'openai', 'https://204992.xyz/v1', 'SIMPLE', 1,
 'Codex Mini 最新版，轻量级编程模型', 1, 101);

-- ===========================================
-- MEDIUM级别 (3-5积分/1K tokens) - 5个模型
-- ===========================================
INSERT INTO `ai_model_config`
(model_key, model_name, provider, base_url, tier, points_per_k_token, description, is_enabled, sort_order)
VALUES
('gpt-5-minimal', 'GPT-5 精简版', 'openai', 'https://204992.xyz/v1', 'MEDIUM', 3,
 'GPT-5 精简版，经济实用', 1, 201),
('gpt-5-low', 'GPT-5 低配版', 'openai', 'https://204992.xyz/v1', 'MEDIUM', 3,
 'GPT-5 低配版，性价比高', 1, 202),
('deepseek-v3.1-free', 'DeepSeek V3.1 免费版', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 4,
 'DeepSeek V3.1 免费版', 1, 203),
('deepseek-r1-0528-free', 'DeepSeek R1 0528版本', 'openrouter', 'https://204992.xyz/v1', 'MEDIUM', 4,
 'DeepSeek R1 0528版本免费', 1, 204),
('qwen3-coder', 'Qwen3 Coder', 'iflow', 'https://204992.xyz/v1', 'MEDIUM', 4,
 'Qwen3 Coder 编程专用', 1, 205);

-- ===========================================
-- HARD级别 (8-10积分/1K tokens) - 10个模型
-- ===========================================
INSERT INTO `ai_model_config`
(model_key, model_name, provider, base_url, tier, points_per_k_token, description, is_enabled, sort_order)
VALUES
('gpt-5-medium', 'GPT-5 中配版', 'openai', 'https://204992.xyz/v1', 'HARD', 8,
 'GPT-5 中配版，性能均衡', 1, 301),
('gpt-5-codex-low', 'GPT-5 Codex 低配版', 'openai', 'https://204992.xyz/v1', 'HARD', 8,
 'GPT-5 Codex 低配版编程', 1, 302),
('gpt-5-codex-medium', 'GPT-5 Codex 中配版', 'openai', 'https://204992.xyz/v1', 'HARD', 9,
 'GPT-5 Codex 中配版编程', 1, 303),
('deepseek-v3.1', 'DeepSeek V3.1', 'iflow', 'https://204992.xyz/v1', 'HARD', 8,
 'DeepSeek V3.1 标准版', 1, 304),
('deepseek-v3.2', 'DeepSeek V3.2', 'iflow', 'https://204992.xyz/v1', 'HARD', 9,
 'DeepSeek V3.2 最新版', 1, 305),
('deepseek-r1', 'DeepSeek R1', 'iflow', 'https://204992.xyz/v1', 'HARD', 10,
 'DeepSeek R1 推理模型', 1, 306),
('qwen3-coder-plus', 'Qwen3 Coder Plus', 'iflow', 'https://204992.xyz/v1', 'HARD', 8,
 'Qwen3 Coder Plus 增强编程', 1, 307),
('qwen3-max-preview', 'Qwen3 Max 预览版', 'iflow', 'https://204992.xyz/v1', 'HARD', 10,
 'Qwen3 Max 预览版', 1, 308),
('kimi-k2', 'Kimi K2', 'iflow', 'https://204992.xyz/v1', 'HARD', 8,
 'Moonshot Kimi K2', 1, 309),
('kimi-k2-0905', 'Kimi K2 0905版本', 'iflow', 'https://204992.xyz/v1', 'HARD', 8,
 'Kimi K2 0905版本', 1, 310);

-- ===========================================
-- EXPERT级别 (12-18积分/1K tokens) - 8个模型
-- ===========================================
INSERT INTO `ai_model_config`
(model_key, model_name, provider, base_url, tier, points_per_k_token, description, is_enabled, sort_order)
VALUES
('gpt-5', 'GPT-5 标准版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 15,
 'OpenAI GPT-5 标准版，顶级性能', 1, 401),
('gpt-5-high', 'GPT-5 高配版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 16,
 'GPT-5 高配版，极致性能', 1, 402),
('gpt-5-codex', 'GPT-5 Codex 编程版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 18,
 'GPT-5 Codex 编程版，顶级编程', 1, 403),
('gpt-5-codex-high', 'GPT-5 Codex 高配版', 'openai', 'https://204992.xyz/v1', 'EXPERT', 18,
 'GPT-5 Codex 高配版，极致编程', 1, 404),
('qwen3-max', 'Qwen3 Max', 'iflow', 'https://204992.xyz/v1', 'EXPERT', 15,
 'Qwen3 Max 最强版本', 1, 405),
('qwen3-235b-a22b-instruct', 'Qwen3 235B A22B', 'iflow', 'https://204992.xyz/v1', 'EXPERT', 16,
 'Qwen3 235B A22B 指令版', 1, 406),
('qwen3-235b-a22b-thinking-2507', 'Qwen3 235B 思维版', 'iflow', 'https://204992.xyz/v1', 'EXPERT', 18,
 'Qwen3 235B 思维版，顶级推理', 1, 407);

-- 验证插入结果
SELECT '插入完成，共24个模型' as status;
SELECT tier, COUNT(*) as count, GROUP_CONCAT(model_name SEPARATOR ', ') as models
FROM ai_model_config
WHERE is_delete = 0
GROUP BY tier
ORDER BY FIELD(tier, 'SIMPLE', 'MEDIUM', 'HARD', 'EXPERT');
