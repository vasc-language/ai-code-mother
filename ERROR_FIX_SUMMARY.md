# 应用版本保存错误修复总结

## 问题
部署应用时报错：
```
PacketTooBigException: Packet for query is too large (141,346,186 > 67,108,864)
```

## 原因
代码内容（141MB）超过 MySQL 的 `max_allowed_packet` 限制（64MB）。

## 解决方案
✅ 已实现 **COS 对象存储方案**，将大型代码内容存储到腾讯云 COS，数据库只存储 URL。

## 修改的文件

### 1. 实体类
- `src/main/java/com/spring/aicodemother/model/entity/AppVersion.java`
  - 添加 `codeStorageUrl` 字段
  - 标记 `codeContent` 为 `@Deprecated`

### 2. COS 管理器
- `src/main/java/com/spring/aicodemother/manager/CosManager.java`
  - 新增 `downloadFile()` - 从COS下载文件
  - 新增 `extractKeyFromUrl()` - 提取COS对象键

### 3. 业务逻辑
- `src/main/java/com/spring/aicodemother/service/impl/AppVersionServiceImpl.java`
  - **saveVersion()**: 上传代码到COS，存储URL
  - **rollbackToVersion()**: 从COS下载代码内容

### 4. 数据库脚本
- `sql/create_table.sql` - 更新表结构定义
- `sql/add_codeContentUrl_to_app_version.sql` - 迁移说明（字段已存在，无需执行）

### 5. 文档
- `docs/APP_VERSION_COS_STORAGE_OPTIMIZATION.md` - 详细优化方案文档

## 工作流程

### 保存版本（deployApp 时触发）
```
代码目录 → 打包JSON → 临时文件 → 上传COS → 存储URL到数据库
                                  ↓ 失败
                              存储到数据库（回退）
```

### 版本回滚
```
从 codeStorageUrl 下载 (COS)
         ↓ 失败
从 codeContent 读取 (数据库，兼容旧版本)
         ↓
解压到代码目录
```

## 优势

1. **突破限制**：不再受 MySQL packet size 限制
2. **性能提升**：数据库查询更快，备份更小
3. **成本优化**：大文件存储在对象存储更经济
4. **向后兼容**：支持读取旧版本的数据库存储
5. **自动回退**：COS 上传失败时自动降级到数据库

## 部署步骤

### 1. 确认 COS 配置

确保 `application.yml` 中已配置腾讯云 COS：

```yaml
cos:
  client:
    secretId: ${COS_SECRET_ID}
    secretKey: ${COS_SECRET_KEY}
    region: ${COS_REGION:ap-guangzhou}
    bucket: ${COS_BUCKET}
    host: ${COS_HOST}
```

### 2. 编译部署

```bash
# Windows
mvnw.cmd clean package

# Linux/Mac
./mvnw clean package

# 部署 JAR 文件
java -jar target/ai-code-mother-backend-*.jar
```

### 3. 验证

```bash
# 1. 生成并部署应用
curl -X POST "http://localhost:8123/api/app/generate/{id}"
curl -X POST "http://localhost:8123/api/app/deploy/{id}"

# 2. 检查日志
grep "代码内容已上传到COS" logs/application.log

# 3. 查询数据库
mysql -u root -p ai_code_mother -e "
SELECT id, appId, versionNum, 
       codeStorageUrl IS NOT NULL as 'Using COS',
       LENGTH(codeContent) as 'DB Size'
FROM app_version 
ORDER BY id DESC 
LIMIT 5;"
```

## 监控建议

### 检查 COS 使用率
```sql
-- COS 存储的版本数
SELECT COUNT(*) as cos_versions FROM app_version WHERE codeStorageUrl IS NOT NULL;

-- 数据库存储的版本数（应该逐渐减少）
SELECT COUNT(*) as db_versions FROM app_version 
WHERE codeContent IS NOT NULL AND codeStorageUrl IS NULL;
```

### 查看日志
```bash
# 成功上传到COS
tail -f logs/application.log | grep "代码内容已上传到COS"

# 上传失败（需关注）
tail -f logs/application.log | grep "代码内容上传COS失败"
```

## 注意事项

1. **COS 费用**：对象存储会产生存储和流量费用
2. **网络依赖**：需要确保服务器可以访问腾讯云 COS
3. **权限配置**：COS 的 Bucket 需要正确配置访问权限
4. **生命周期**：可以为 COS 中的旧版本文件配置自动删除或归档策略

## 测试结果

✅ 代码编译成功  
✅ 实体类字段匹配数据库（`codeStorageUrl`）  
✅ COS 上传/下载功能已实现  
✅ 回退机制已实现  
✅ 兼容旧版本数据

## 下一步（可选）

1. **迁移历史数据**：将现有的大文件版本迁移到 COS
2. **配置生命周期**：为 COS 中的旧文件配置自动归档
3. **监控告警**：设置 COS 上传失败的监控告警
4. **清理数据库**：将已迁移到 COS 的 `codeContent` 字段设为 NULL

## 参考文档

详细信息请参阅：
- `docs/APP_VERSION_COS_STORAGE_OPTIMIZATION.md` - 完整优化方案
- `ERROR.md` - 原始错误日志

---

**修复完成时间**：2025-10-13  
**问题状态**：✅ 已解决
