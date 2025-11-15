-- 查询最近创建的应用（最近10个）
SELECT
    id AS '应用ID',
    app_name AS '应用名称',
    code_gen_type AS '生成类型',
    user_id AS '创建者ID',
    create_time AS '创建时间',
    is_delete AS '是否删除'
FROM app
WHERE is_delete = 0
ORDER BY create_time DESC
LIMIT 10;

-- 查询特定应用ID
SELECT
    id,
    app_name,
    code_gen_type,
    user_id,
    create_time,
    is_delete
FROM app
WHERE id = 334241891454754816;

-- 查询当前登录用户的所有应用（假设用户ID是334172691948314624）
SELECT
    id AS '应用ID',
    app_name AS '应用名称',
    code_gen_type AS '生成类型',
    create_time AS '创建时间',
    is_delete AS '是否删除'
FROM app
WHERE user_id = 334172691948314624
  AND is_delete = 0
ORDER BY create_time DESC;
