-- 给app表添加modelKey字段，用于存储用户选择的AI模型
ALTER TABLE app ADD COLUMN modelKey VARCHAR(100) DEFAULT 'codex-mini-lastest' COMMENT 'AI模型标识' AFTER codeGenType;
