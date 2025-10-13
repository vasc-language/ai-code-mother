# 应用版本存储优化 - COS对象存储方案

## 问题背景

在保存应用版本时，遇到以下错误：

```
PacketTooBigException: Packet for query is too large (141,346,186 > 67,108,864)
```

**原因分析**：
- 代码内容以 JSON 格式直接存储在 MySQL 的 `codeContent` 字段中
- 当生成的代码量较大时（例如 141MB），会超过 MySQL 的 `max_allowed_packet` 限制（默认 64MB）
- 将大量代码存储在数据库中不是最佳实践，会影响数据库性能

## 解决方案

采用 **COS（腾讯云对象存储）+ 数据库** 的混合存储策略：

1. **大文件**：代码内容上传到 COS，数据库只存储访问 URL
2. **小文件**：如果 COS 上传失败，回退到数据库存储（兼容性）
3. **历史兼容**：支持读取旧版本的数据库存储方式

## 实现细节

### 1. 数据库变更

#### 现有字段

`app_version` 表中已存在 `codeStorageUrl` 字段：

- `codeContent` (varchar(512)): 代码内容（JSON格式）- **已废弃**，大文件请使用 `codeStorageUrl`
- `codeStorageUrl` (varchar(512)): 代码内容存储URL（COS对象存储）- **新方案使用此字段**

**无需执行数据库迁移**，字段已存在。如果表中缺少此字段，可参考 `sql/add_codeContentUrl_to_app_version.sql` 中的注释。

### 2. 代码变更

#### 2.1 实体类更新

`AppVersion.java` 添加新字段：

```java
/**
 * 代码内容（JSON格式，包含所有文件）
 * @deprecated 已废弃，大文件请使用 codeStorageUrl
 */
@Deprecated
@Column("codeContent")
private String codeContent;

/**
 * 代码内容存储URL（COS对象存储）
 */
@Column("codeStorageUrl")
private String codeStorageUrl;
```

#### 2.2 COS管理器增强

`CosManager.java` 新增方法：

```java
// 下载文件从COS
public boolean downloadFile(String key, String localFilePath)

// 从URL提取COS对象键
public String extractKeyFromUrl(String url)
```

#### 2.3 版本保存逻辑优化

`AppVersionServiceImpl.saveVersion()` 流程：

```
1. 将代码目录打包成 JSON 字符串
2. 创建临时 JSON 文件
3. 上传到 COS：app-versions/{appId}/{codeGenType}_v{timestamp}.json
4. 如果上传成功：
   - 存储 URL 到 codeStorageUrl
   - codeContent 设为 null
5. 如果上传失败：
   - 保留 codeContent（回退到数据库存储）
6. 删除临时文件
```

#### 2.4 版本回滚逻辑优化

`AppVersionServiceImpl.rollbackToVersion()` 流程：

```
1. 优先从 codeStorageUrl 获取（COS下载）
2. 如果 COS 下载失败，从 codeContent 获取（数据库）
3. 解析 JSON 并恢复文件到代码生成目录
```

## 优势

### 1. 解决问题
- ✅ 突破 MySQL 的 `max_allowed_packet` 限制
- ✅ 支持存储任意大小的代码内容
- ✅ 数据库性能不受大文件影响

### 2. 架构优化
- 🚀 **分离存储**：结构化数据在数据库，大文件在对象存储
- 🔄 **降低耦合**：文件存储可独立扩展
- 💾 **节省空间**：数据库大小显著减小

### 3. 性能提升
- ⚡ **数据库查询更快**：不需要传输大字段
- 📦 **备份更高效**：数据库备份文件更小
- 🌐 **CDN 加速**：COS 支持 CDN，文件访问更快

### 4. 兼容性
- 🔙 **向后兼容**：支持读取旧版本的数据库存储
- 🛡️ **回退机制**：COS 上传失败时自动回退到数据库

## 配置要求

### COS 配置

确保在 `application.yml` 中配置了腾讯云 COS：

```yaml
cos:
  client:
    secretId: ${COS_SECRET_ID}
    secretKey: ${COS_SECRET_KEY}
    region: ${COS_REGION:ap-guangzhou}
    bucket: ${COS_BUCKET}
    host: ${COS_HOST}
```

### 环境变量

```bash
export COS_SECRET_ID=your_secret_id
export COS_SECRET_KEY=your_secret_key
export COS_BUCKET=your_bucket_name
export COS_HOST=https://your-bucket.cos.ap-guangzhou.myqcloud.com/
```

## 监控建议

### 1. COS 上传成功率

在日志中监控以下关键词：
- `代码内容已上传到COS` - 上传成功
- `代码内容上传COS失败` - 上传失败，需关注

### 2. 存储使用情况

定期检查：
```sql
-- 查看使用COS存储的版本数量
SELECT COUNT(*) FROM app_version WHERE codeStorageUrl IS NOT NULL;

-- 查看仍使用数据库存储的版本数量
SELECT COUNT(*) FROM app_version WHERE codeContent IS NOT NULL AND codeStorageUrl IS NULL;
```

### 3. 数据库大小变化

```sql
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'ai_code_mother' 
AND table_name = 'app_version';
```

## 迁移现有数据（可选）

如果需要将现有的大文件版本迁移到 COS：

```sql
-- 查找需要迁移的大文件版本（> 10MB）
SELECT id, appId, versionNum, LENGTH(codeContent) as size_bytes
FROM app_version
WHERE codeContent IS NOT NULL 
AND codeStorageUrl IS NULL
AND LENGTH(codeContent) > 10485760
ORDER BY size_bytes DESC;
```

可以编写数据迁移脚本，将这些版本的 `codeContent` 上传到 COS 并更新 `codeStorageUrl`。

## 回滚方案

如果需要回滚到原方案（仅存储到数据库）：

1. 修改 `AppVersionServiceImpl.saveVersion()` 方法，注释掉 COS 上传部分
2. 保留 `codeContent` 字段不变
3. 但需要先解决 MySQL 的 `max_allowed_packet` 限制问题

## 测试验证

### 1. 功能测试

```bash
# 1. 生成大型应用
curl -X POST "http://localhost:8123/api/app/generate/{id}"

# 2. 部署应用（触发版本保存）
curl -X POST "http://localhost:8123/api/app/deploy/{id}"

# 3. 检查日志
grep "代码内容已上传到COS" logs/application.log

# 4. 查询版本
curl -X GET "http://localhost:8123/api/app/version/list?appId={id}"
```

### 2. 回滚测试

```bash
# 回滚到指定版本
curl -X POST "http://localhost:8123/api/app/version/rollback" \
  -H "Content-Type: application/json" \
  -d '{"versionId": {versionId}, "appId": {appId}}'
```

### 3. 性能对比

对比优化前后的性能指标：
- 数据库备份时间
- 版本保存时间
- 版本查询响应时间

## 注意事项

1. **COS 费用**：对象存储会产生存储和流量费用，需要根据使用量评估成本
2. **数据一致性**：确保 COS 文件和数据库记录的一致性
3. **权限控制**：COS 文件的访问权限需要合理配置
4. **生命周期管理**：考虑为旧版本文件配置自动归档或删除策略

## 总结

通过引入 COS 对象存储，成功解决了应用版本代码内容超出 MySQL 限制的问题，同时优化了存储架构，提升了系统的可扩展性和性能。该方案具有良好的向后兼容性和回退机制，可以安全地在生产环境中部署。
