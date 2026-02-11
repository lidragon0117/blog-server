# 博客系统缓存与MQ使用方案详解

## 📋 目录

- [一、缓存使用方案](#一缓存使用方案)
  - [1.1 缓存策略总览](#11-缓存策略总览)
  - [1.2 文章模块缓存](#12-文章模块缓存)
  - [1.3 用户模块缓存](#13-用户模块缓存)
  - [1.4 评论模块缓存](#14-评论模块缓存)
  - [1.5 分类/标签模块缓存](#15-分类标签模块缓存)
  - [1.6 统计数据缓存](#16-统计数据缓存)
  - [1.7 配置数据缓存](#17-配置数据缓存)
  - [1.8 会话数据缓存](#18-会话数据缓存)
  - [1.9 搜索结果缓存](#19-搜索结果缓存)
  - [1.10 热点数据缓存](#110-热点数据缓存)
- [二、MQ使用方案](#二mq使用方案)
  - [2.1 MQ场景总览](#21-mq场景总览)
  - [2.2 文章发布流程](#22-文章发布流程)
  - [2.3 评论发布流程](#23-评论发布流程)
  - [2.4 点赞/互动流程](#24-点赞互动流程)
  - [2.5 关注/取关流程](#25-关注取关流程)
  - [2.6 消息通知流程](#26-消息通知流程)
  - [2.7 数据同步流程](#27-数据同步流程)
  - [2.8 统计数据更新](#28-统计数据更新)
  - [2.9 定时任务异步化](#29-定时任务异步化)
  - [2.10 延迟任务处理](#210-延迟任务处理)
- [三、缓存+MQ组合方案](#三缓存mq组合方案)
- [四、实施优先级](#四实施优先级)

---

## 一、缓存使用方案

### 1.1 缓存策略总览

#### 缓存使用原则

| 场景类型 | 是否缓存 | 缓存策略 | 过期时间 | 更新策略 |
|---------|---------|---------|---------|---------|
| 读多写少 | ✅ | 多级缓存 | 5-30分钟 | Cache-Aside |
| 读多写多 | ✅ | 短期缓存 | 1-5分钟 | Write-Through |
| 写多读少 | ❌ | 不缓存 | - | - |
| 实时统计 | ✅ | 定时更新 | 1-10分钟 | Refresh-Ahead |
| 计算结果 | ✅ | 长期缓存 | 1-24小时 | Cache-Aside |
| 配置数据 | ✅ | 永久/长期 | 永久/1小时 | Write-Through |
| 会话数据 | ✅ | 永久 | 永久 | 滑动过期 |
| 用户画像 | ✅ | 长期缓存 | 1-6小时 | 定时刷新 |

#### 缓存更新策略对比

| 策略 | 说明 | 优点 | 缺点 | 适用场景 |
|------|------|------|------|---------|
| **Cache-Aside** | 读时缓存，写时删除 | 简单实用 | 缓存未命中时有抖动 | 通用场景 |
| **Write-Through** | 写时同步更新缓存 | 数据一致性好 | 写入性能下降 | 强一致性要求 |
| **Write-Behind** | 写时异步更新缓存 | 写入性能高 | 可能丢失数据 | 允许短暂不一致 |
| **Refresh-Ahead** | 定时主动刷新缓存 | 命中率高 | 占用额外资源 | 热点数据 |

#### 缓存Key命名规范

```java
/**
 * 缓存Key命名规范
 * 格式: {业务前缀}:{模块}:{操作}:{标识}[:{参数}]
 * 长度: 建议不超过100字符
 * 字符集: 只使用字母、数字、冒号、下划线、横线
 */

// ✅ 正确示例
"blog:article:info:12345"                    // 文章详情
"blog:article:list:home:1:20"                // 首页文章列表（第1页，20条）
"blog:user:info:67890"                       // 用户信息
"blog:comment:list:12345:1"                  // 文章评论列表
"blog:stats:article:12345:view"              // 文章阅读量统计

// ❌ 错误示例
"blog_article_info_12345"                    // 不要用下划线分隔业务层级
"blog:article:info:文章:12345"               // 不要用中文
"Blog:Article:Info:12345"                    // 不要用大写（Redis对大小写敏感）
"a:very:long:cache:key:name:that:exceeds:recommendation:length"  // 太长
```

#### 缓存层级架构

```
L1: 浏览器缓存 (静态资源)
    ↓ miss
L2: CDN缓存 (图片、CSS、JS)
    ↓ miss
L3: Nginx本地缓存 (热点数据)
    ↓ miss
L4: 应用本地缓存 (Caffeine)
    ↓ miss
L5: Redis分布式缓存
    ↓ miss
L6: 数据库
```

---

### 1.2 文章模块缓存

#### 1.2.1 文章详情缓存

```java
/**
 * 文章详情缓存
 * 缓存Key: blog:article:info:{articleId}
 * 数据类型: String (JSON)
 * 过期时间: 30分钟
 * 更新策略: 更新时删除缓存，查询时重建
 * 使用场景: 文章详情页
 */
public class ArticleDetailCache {

    private static final String KEY_PREFIX = "blog:article:info:";
    private static final int EXPIRE_SECONDS = 1800; // 30分钟

    /**
     * 获取文章详情
     */
    public ArticleDetailVO getArticle(Long articleId) {
        String key = KEY_PREFIX + articleId;

        // L1: 本地缓存
        ArticleDetailVO article = localCache.getIfPresent(key);
        if (article != null) {
            return article;
        }

        // L2: Redis缓存
        String value = redisService.get(key);
        if (value != null) {
            article = JsonUtil.fromJson(value, ArticleDetailVO.class);
            localCache.put(key, article);
            return article;
        }

        // L3: 数据库
        article = articleMapper.selectById(articleId);
        if (article != null) {
            // 写入缓存
            redisService.set(key, JsonUtil.toJsonString(article), EXPIRE_SECONDS);
            localCache.put(key, article);
        }

        return article;
    }

    /**
     * 更新文章时删除缓存
     */
    public void evictArticle(Long articleId) {
        String key = KEY_PREFIX + articleId;
        localCache.invalidate(key);
        redisService.remove(key);
    }
}
```

**缓存时机**：
- 文章首次访问时缓存
- 文章更新后删除缓存，下次查询时重建
- 文章删除后删除缓存

**注意事项**：
- 大字段（content、contentMd）建议单独缓存或分离存储
- 可以考虑压缩存储（GZIP）
- 热门文章可以延长过期时间

---

#### 1.2.2 文章列表缓存

```java
/**
 * 文章列表缓存
 * 缓存Key: blog:article:list:{type}:{param}
 * 数据类型: String (JSON)
 * 过期时间: 10分钟
 * 更新策略: 文章发布/更新/删除时删除相关列表
 */

// 首页文章列表
blog:article:list:home:1:10  // 第1页，每页10条

// 分类文章列表
blog:article:list:category:5:1:20  // 分类ID=5，第1页，20条

// 标签文章列表
blog:article:list:tag:10:1:20  // 标签ID=10，第1页，20条

// 用户文章列表
blog:article:list:user:100:1:20  // 用户ID=100，第1页，20条

// 热门文章列表
blog:article:list:hot:7:20  // 最近7天，20条

// 推荐文章列表
blog:article:list:recommend:1:20  // 第1页，20条

// 置顶文章列表
blog:article:list:stick:10  // 置顶文章

// 搜索结果列表
blog:article:list:search:关键词:1:20
```

**实现方案**：

```java
@Service
public class ArticleListCacheService {

    /**
     * 获取首页文章列表
     */
    public PageResult<ArticleVO> getHomeArticles(int page, int size) {
        String key = String.format("blog:article:list:home:%d:%d", page, size);

        // 尝试从缓存获取
        String value = redisService.get(key);
        if (value != null) {
            return JsonUtil.fromJson(value, new TypeReference<PageResult<ArticleVO>>() {});
        }

        // 查询数据库
        PageResult<ArticleVO> result = articleMapper.selectHomeArticles(page, size);

        // 写入缓存
        redisService.set(key, JsonUtil.toJsonString(result), 600); // 10分钟

        return result;
    }

    /**
     * 文章发布后清除相关列表缓存
     */
    public void evictArticleListCache(Long articleId, Integer categoryId, List<Long> tagIds) {
        // 清除首页列表
        for (int i = 1; i <= 5; i++) {
            redisService.remove(String.format("blog:article:list:home:%d:10", i));
            redisService.remove(String.format("blog:article:list:home:%d:20", i));
        }

        // 清除分类列表
        if (categoryId != null) {
            for (int i = 1; i <= 5; i++) {
                redisService.remove(String.format("blog:article:list:category:%d:%d:*", categoryId, i));
            }
        }

        // 清除标签列表
        if (CollectionUtils.isNotEmpty(tagIds)) {
            for (Long tagId : tagIds) {
                for (int i = 1; i <= 3; i++) {
                    redisService.remove(String.format("blog:article:list:tag:%d:%d:*", tagId, i));
                }
            }
        }

        // 清除热门文章列表
        redisService.remove("blog:article:list:hot:*");

        // 清除推荐文章列表
        redisService.remove("blog:article:list:recommend:*");
    }
}
```

**缓存清理策略**：
- 新文章发布：清除首页、分类、标签、热门、推荐列表
- 文章更新：同上
- 文章删除：同上
- 定时刷新：每10分钟自动刷新热门文章

---

#### 1.2.3 文章内容缓存

```java
/**
 * 文章内容缓存（大字段分离）
 * 缓存Key: blog:article:content:{articleId}
 * 数据类型: String (JSON)
 * 过期时间: 1小时
 * 说明: 将content和contentMd大字段单独缓存
 */
public class ArticleContentCache {

    private static final String KEY_PREFIX = "blog:article:content:";
    private static final int EXPIRE_SECONDS = 3600;

    /**
     * 获取文章内容
     */
    public ArticleContentVO getContent(Long articleId) {
        String key = KEY_PREFIX + articleId;
        String value = redisService.get(key);

        if (value != null) {
            return JsonUtil.fromJson(value, ArticleContentVO.class);
        }

        ArticleContent content = articleContentMapper.selectById(articleId);
        if (content != null) {
            // 压缩存储
            String compressed = GzipUtil.compress(content.getContentMd());
            ArticleContentVO vo = ArticleContentVO.builder()
                .articleId(content.getArticleId())
                .content(content.getContent())
                .contentMd(compressed)
                .build();

            redisService.set(key, JsonUtil.toJsonString(vo), EXPIRE_SECONDS);
            return vo;
        }

        return null;
    }
}
```

---

#### 1.2.4 文章归档缓存

```java
/**
 * 文章归档缓存
 * 缓存Key: blog:article:archive
 * 数据类型: String (JSON)
 * 过期时间: 1小时
 * 说明: 按年份归档的文章列表
 */
public class ArticleArchiveCache {

    /**
     * 获取文章归档
     */
    public Map<Integer, List<ArticleVO>> getArchive() {
        String key = "blog:article:archive";
        String value = redisService.get(key);

        if (value != null) {
            return JsonUtil.fromJson(value, new TypeReference<Map<Integer, List<ArticleVO>>>() {});
        }

        // 查询数据库
        Map<Integer, List<ArticleVO>> archive = articleMapper.selectArchive();

        // 缓存1小时
        redisService.set(key, JsonUtil.toJsonString(archive), 3600);

        return archive;
    }
}
```

---

#### 1.2.5 相关文章缓存

```java
/**
 * 相关文章推荐缓存
 * 缓存Key: blog:article:related:{articleId}
 * 数据类型: List
 * 过期时间: 2小时
 * 说明: 基于标签和分类的相关文章推荐
 */
public class RelatedArticleCache {

    /**
     * 获取相关文章
     */
    public List<ArticleVO> getRelatedArticles(Long articleId, Integer categoryId, List<Long> tagIds) {
        String key = "blog:article:related:" + articleId;
        List<String> values = redisService.lrange(key, 0, -1);

        if (CollectionUtils.isNotEmpty(values)) {
            return values.stream()
                .map(v -> JsonUtil.fromJson(v, ArticleVO.class))
                .collect(Collectors.toList());
        }

        // 查询相关文章（同分类或同标签）
        List<ArticleVO> related = articleMapper.selectRelatedArticles(articleId, categoryId, tagIds);

        // 缓存
        redisService.lrightPushAll(key, related.stream()
            .map(JsonUtil::toJsonString)
            .collect(Collectors.toList()));
        redisService.expire(key, 7200); // 2小时

        return related;
    }
}
```

---

### 1.3 用户模块缓存

#### 1.3.1 用户基本信息缓存

```java
/**
 * 用户基本信息缓存
 * 缓存Key: blog:user:info:{userId}
 * 数据类型: String (JSON)
 * 过期时间: 1小时
 * 更新策略: 用户信息更新时删除缓存
 */
public class UserInfoCache {

    private static final String KEY_PREFIX = "blog:user:info:";
    private static final int EXPIRE_SECONDS = 3600;

    /**
     * 获取用户信息
     */
    public UserVO getUser(Long userId) {
        String key = KEY_PREFIX + userId;

        // L1: 本地缓存（5分钟）
        UserVO user = localCache.getIfPresent(key);
        if (user != null) {
            return user;
        }

        // L2: Redis缓存
        String value = redisService.get(key);
        if (value != null) {
            user = JsonUtil.fromJson(value, UserVO.class);
            localCache.put(key, user);
            return user;
        }

        // L3: 数据库
        User dbUser = userMapper.selectById(userId);
        if (dbUser != null) {
            user = convertToVO(dbUser);
            redisService.set(key, JsonUtil.toJsonString(user), EXPIRE_SECONDS);
            localCache.put(key, user);
        }

        return user;
    }

    /**
     * 批量获取用户信息
     */
    public Map<Long, UserVO> getUsersBatch(List<Long> userIds) {
        // 批量从缓存获取
        List<String> keys = userIds.stream()
            .map(id -> KEY_PREFIX + id)
            .collect(Collectors.toList());

        Map<String, String> cachedData = redisService.mgetAndParseMap(keys);

        Map<Long, UserVO> result = new HashMap<>();
        List<Long> uncachedIds = new ArrayList<>();

        // 从缓存中获取
        cachedData.forEach((key, value) -> {
            if (value != null) {
                Long userId = Long.parseLong(key.substring(key.lastIndexOf(":") + 1));
                UserVO user = JsonUtil.fromJson(value, UserVO.class);
                result.put(userId, user);
            }
        });

        // 找出未缓存的ID
        for (Long userId : userIds) {
            if (!result.containsKey(userId)) {
                uncachedIds.add(userId);
            }
        }

        // 批量查询数据库
        if (!uncachedIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(uncachedIds);
            for (User user : users) {
                UserVO vo = convertToVO(user);
                result.put(user.getId(), vo);

                // 写入缓存
                String key = KEY_PREFIX + user.getId();
                redisService.set(key, JsonUtil.toJsonString(vo), EXPIRE_SECONDS);
            }
        }

        return result;
    }
}
```

---

#### 1.3.2 用户粉丝列表缓存

```java
/**
 * 用户粉丝列表缓存
 * 缓存Key: blog:user:followers:{userId}
 * 数据类型: ZSet (按关注时间排序)
 * 过期时间: 30分钟
 * 说明: 缓存粉丝ID列表，分页查询
 */
public class UserFollowersCache {

    /**
     * 获取粉丝列表
     */
    public PageResult<UserVO> getFollowers(Long userId, int page, int size) {
        String key = "blog:user:followers:" + userId;

        // 从ZSet获取（分页）
        long start = (page - 1) * size;
        long end = page * size - 1;

        Set<String> followerIds = redisService.zrange(key, start, end);

        if (CollectionUtils.isEmpty(followerIds)) {
            // 查询数据库
            PageResult<UserVO> result = userMapper.selectFollowers(userId, page, size);

            // 重建缓存
            rebuildFollowersCache(userId);

            return result;
        }

        // 批量获取用户信息
        List<Long> ids = followerIds.stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());

        Map<Long, UserVO> userMap = userInfoCache.getUsersBatch(ids);
        List<UserVO> followers = ids.stream()
            .map(userMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 获取总数
        long total = redisService.zsize(key);

        return new PageResult<>(followers, total, page, size);
    }

    /**
     * 关注/取消关注时更新缓存
     */
    public void addFollower(Long userId, Long followerId, Long timestamp) {
        String key = "blog:user:followers:" + userId;
        redisService.zadd(key, followerId.toString(), timestamp);
    }

    public void removeFollower(Long userId, Long followerId) {
        String key = "blog:user:followers:" + userId;
        redisService.zremove(key, followerId.toString());
    }
}
```

---

#### 1.3.3 用户关注列表缓存

```java
/**
 * 用户关注列表缓存
 * 缓存Key: blog:user:following:{userId}
 * 数据类型: ZSet
 * 过期时间: 30分钟
 */
public class UserFollowingCache {

    /**
     * 获取关注列表
     */
    public PageResult<UserVO> getFollowing(Long userId, int page, int size) {
        String key = "blog:user:following:" + userId;

        long start = (page - 1) * size;
        long end = page * size - 1;

        Set<String> followingIds = redisService.zrange(key, start, end);

        if (CollectionUtils.isEmpty(followingIds)) {
            rebuildFollowingCache(userId);
            return userMapper.selectFollowing(userId, page, size);
        }

        List<Long> ids = followingIds.stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());

        Map<Long, UserVO> userMap = userInfoCache.getUsersBatch(ids);
        List<UserVO> following = ids.stream()
            .map(userMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        long total = redisService.zsize(key);

        return new PageResult<>(following, total, page, size);
    }
}
```

---

#### 1.3.4 用户统计数据缓存

```java
/**
 * 用户统计数据缓存
 * 缓存Key: blog:user:stats:{userId}
 * 数据类型: Hash
 * 过期时间: 10分钟
 * 说明: 文章数、粉丝数、关注数、获赞数等
 */
public class UserStatsCache {

    /**
     * 获取用户统计
     */
    public UserStatsVO getStats(Long userId) {
        String key = "blog:user:stats:" + userId;

        Map<String, String> stats = redisService.hgetAll(key);
        if (!stats.isEmpty()) {
            return UserStatsVO.builder()
                .articleCount(Integer.parseInt(stats.getOrDefault("articleCount", "0")))
                .followerCount(Integer.parseInt(stats.getOrDefault("followerCount", "0")))
                .followingCount(Integer.parseInt(stats.getOrDefault("followingCount", "0")))
                .likeCount(Integer.parseInt(stats.getOrDefault("likeCount", "0")))
                .viewCount(Integer.parseInt(stats.getOrDefault("viewCount", "0")))
                .build();
        }

        // 查询数据库
        UserStatsVO statsVO = userMapper.selectStats(userId);

        // 写入缓存
        Map<String, String> data = new HashMap<>();
        data.put("articleCount", statsVO.getArticleCount().toString());
        data.put("followerCount", statsVO.getFollowerCount().toString());
        data.put("followingCount", statsVO.getFollowingCount().toString());
        data.put("likeCount", statsVO.getLikeCount().toString());
        data.put("viewCount", statsVO.getViewCount().toString());

        redisService.hmSet(key, data, 600);

        return statsVO;
    }

    /**
     * 增加文章数
     */
    public void incrementArticleCount(Long userId) {
        String key = "blog:user:stats:" + userId;
        redisService.hIncrementVal(key, "articleCount", 1);
    }
}
```

---

#### 1.3.5 用户权限缓存

```java
/**
 * 用户权限缓存
 * 缓存Key: blog:user:permissions:{userId}
 * 数据类型: Set
 * 过期时间: 30分钟
 * 说明: 用户权限标识集合
 */
public class UserPermissionCache {

    /**
     * 获取用户权限
     */
    public Set<String> getPermissions(Long userId) {
        String key = "blog:user:permissions:" + userId;

        Set<String> permissions = redisService.smembers(key);
        if (permissions != null && !permissions.isEmpty()) {
            return permissions;
        }

        // 查询数据库
        Set<String> dbPermissions = userMapper.selectPermissions(userId);

        // 写入缓存
        redisService.sadd(key, dbPermissions.toArray(new String[0]));
        redisService.expire(key, 1800);

        return dbPermissions;
    }

    /**
     * 检查权限
     */
    public boolean hasPermission(Long userId, String permission) {
        String key = "blog:user:permissions:" + userId;
        return redisService.sIsMember(key, permission);
    }
}
```

---

### 1.4 评论模块缓存

#### 1.4.1 评论列表缓存

```java
/**
 * 文章评论列表缓存
 * 缓存Key: blog:article:comments:{articleId}:{page}
 * 数据类型: List
 * 过期时间: 5分钟
 * 说明: 评论更新频繁，使用短期缓存
 */
public class CommentListCache {

    /**
     * 获取评论列表
     */
    public List<CommentVO> getComments(Long articleId, int page, int size) {
        String key = String.format("blog:article:comments:%d:%d", articleId, page);

        List<String> values = redisService.lrange(key, 0, -1);
        if (CollectionUtils.isNotEmpty(values)) {
            return values.stream()
                .map(v -> JsonUtil.fromJson(v, CommentVO.class))
                .collect(Collectors.toList());
        }

        // 查询数据库
        List<CommentVO> comments = commentMapper.selectByArticleId(articleId, page, size);

        // 缓存5分钟
        if (!comments.isEmpty()) {
            redisService.lrightPushAll(key, comments.stream()
                .map(JsonUtil::toJsonString)
                .collect(Collectors.toList()));
            redisService.expire(key, 300);
        }

        return comments;
    }

    /**
     * 新增评论后删除缓存
     */
    public void evictCommentCache(Long articleId) {
        // 删除该文章的所有评论页缓存
        Set<String> keys = redisService.keys("blog:article:comments:" + articleId + ":*");
        redisService.removeAll(keys);
    }
}
```

---

#### 1.4.2 评论点赞用户缓存

```java
/**
 * 评论点赞用户集合缓存
 * 缓存Key: blog:comment:like:{commentId}
 * 数据类型: Set
 * 过期时间: 24小时
 * 说明: 记录点赞用户ID，用于去重和展示
 */
public class CommentLikeCache {

    /**
     * 点赞评论
     */
    public boolean likeComment(Long commentId, Long userId) {
        String key = "blog:comment:like:" + commentId;

        // 检查是否已点赞
        if (redisService.sIsMember(key, userId.toString())) {
            return false;
        }

        // 添加到集合
        redisService.sadd(key, userId.toString());
        redisService.expire(key, 86400);

        // 增加点赞数
        redisService.hincrby("blog:comment:stats:" + commentId, "likeCount", 1);

        return true;
    }

    /**
     * 取消点赞
     */
    public void unlikeComment(Long commentId, Long userId) {
        String key = "blog:comment:like:" + commentId;
        redisService.sremove(key, userId.toString());

        // 减少点赞数
        redisService.hincrby("blog:comment:stats:" + commentId, "likeCount", -1);
    }

    /**
     * 检查是否点赞
     */
    public boolean isLiked(Long commentId, Long userId) {
        String key = "blog:comment:like:" + commentId;
        return redisService.sIsMember(key, userId.toString());
    }
}
```

---

### 1.5 分类/标签模块缓存

#### 1.5.1 分类列表缓存

```java
/**
 * 分类列表缓存
 * 缓存Key: blog:category:list
 * 数据类型: List
 * 过期时间: 1小时
 * 说明: 全部分类列表，变化不频繁
 */
public class CategoryListCache {

    /**
     * 获取全部分类
     */
    public List<CategoryVO> getAllCategories() {
        String key = "blog:category:list";

        List<String> values = redisService.lrange(key, 0, -1);
        if (CollectionUtils.isNotEmpty(values)) {
            return values.stream()
                .map(v -> JsonUtil.fromJson(v, CategoryVO.class))
                .collect(Collectors.toList());
        }

        // 查询数据库
        List<CategoryVO> categories = categoryMapper.selectAll();

        // 缓存
        if (!categories.isEmpty()) {
            redisService.lrightPushAll(key, categories.stream()
                .map(JsonUtil::toJsonString)
                .collect(Collectors.toList()));
            redisService.expire(key, 3600);
        }

        return categories;
    }

    /**
     * 更新分类后删除缓存
     */
    public void evictCache() {
        redisService.remove("blog:category:list");
    }
}
```

---

#### 1.5.2 标签列表缓存

```java
/**
 * 标签列表缓存
 * 缓存Key: blog:tag:list
 * 缓存Key: blog:tag:hot
 * 数据类型: ZSet (按使用频率排序)
 * 过期时间: 1小时
 */
public class TagListCache {

    /**
     * 获取全部标签
     */
    public List<TagVO> getAllTags() {
        String key = "blog:tag:list";

        List<String> values = redisService.lrange(key, 0, -1);
        if (CollectionUtils.isNotEmpty(values)) {
            return values.stream()
                .map(v -> JsonUtil.fromJson(v, TagVO.class))
                .collect(Collectors.toList());
        }

        List<TagVO> tags = tagMapper.selectAll();

        if (!tags.isEmpty()) {
            redisService.lrightPushAll(key, tags.stream()
                .map(JsonUtil::toJsonString)
                .collect(Collectors.toList()));
            redisService.expire(key, 3600);
        }

        return tags;
    }

    /**
     * 获取热门标签
     */
    public List<TagVO> getHotTags(int limit) {
        String key = "blog:tag:hot";

        Set<String> tagIds = redisService.zreverseRange(key, 0, limit - 1);

        if (CollectionUtils.isNotEmpty(tagIds)) {
            List<Long> ids = tagIds.stream().map(Long::parseLong).collect(Collectors.toList());
            Map<Long, TagVO> tagMap = tagMapper.selectByIds(ids);
            return ids.stream().map(tagMap::get).collect(Collectors.toList());
        }

        // 查询热门标签
        List<TagVO> hotTags = tagMapper.selectHotTags(limit);

        // 缓存
        for (TagVO tag : hotTags) {
            redisService.zadd(key, tag.getId().toString(), tag.getArticleCount());
        }
        redisService.expire(key, 3600);

        return hotTags;
    }
}
```

---

### 1.6 统计数据缓存

#### 1.6.1 文章统计数据缓存

```java
/**
 * 文章统计数据缓存
 * 缓存Key: blog:article:stats:{articleId}
 * 数据类型: Hash
 * 过期时间: 10分钟
 * 说明: 阅读量、点赞数、评论数、收藏数
 */
public class ArticleStatsCache {

    /**
     * 获取文章统计
     */
    public ArticleStatsVO getStats(Long articleId) {
        String key = "blog:article:stats:" + articleId;

        Map<String, String> stats = redisService.hgetAll(key);
        if (!stats.isEmpty()) {
            return ArticleStatsVO.builder()
                .viewCount(Long.parseLong(stats.getOrDefault("viewCount", "0")))
                .likeCount(Integer.parseInt(stats.getOrDefault("likeCount", "0")))
                .commentCount(Integer.parseInt(stats.getOrDefault("commentCount", "0")))
                .collectCount(Integer.parseInt(stats.getOrDefault("collectCount", "0")))
                .build();
        }

        // 查询数据库
        ArticleStatsVO statsVO = articleMapper.selectStats(articleId);

        // 缓存
        Map<String, String> data = new HashMap<>();
        data.put("viewCount", statsVO.getViewCount().toString());
        data.put("likeCount", statsVO.getLikeCount().toString());
        data.put("commentCount", statsVO.getCommentCount().toString());
        data.put("collectCount", statsVO.getCollectCount().toString());

        redisService.hmSet(key, data, 600);

        return statsVO;
    }

    /**
     * 增加阅读量（使用计数器）
     */
    public void incrementViewCount(Long articleId) {
        // 计数器（定时同步到数据库）
        String counterKey = "blog:article:view:counter:" + articleId;
        redisService.increx(counterKey, 3600);

        // 统计缓存
        String statsKey = "blog:article:stats:" + articleId;
        redisService.hIncrementVal(statsKey, "viewCount", 1);
    }

    /**
     * 增加点赞数
     */
    public void incrementLikeCount(Long articleId) {
        String key = "blog:article:stats:" + articleId;
        redisService.hIncrementVal(key, "likeCount", 1);
    }

    /**
     * 增加评论数
     */
    public void incrementCommentCount(Long articleId) {
        String key = "blog:article:stats:" + articleId;
        redisService.hIncrementVal(key, "commentCount", 1);
    }
}
```

---

#### 1.6.2 全站统计数据缓存

```java
/**
 * 全站统计数据缓存
 * 缓存Key: blog:stats:global
 * 数据类型: Hash
 * 过期时间: 5分钟
 * 说明: 全站文章数、用户数、评论数、今日访问等
 */
public class GlobalStatsCache {

    /**
     * 获取全站统计
     */
    public GlobalStatsVO getGlobalStats() {
        String key = "blog:stats:global";

        Map<String, String> stats = redisService.hgetAll(key);
        if (!stats.isEmpty()) {
            return GlobalStatsVO.builder()
                .articleCount(Long.parseLong(stats.getOrDefault("articleCount", "0")))
                .userCount(Long.parseLong(stats.getOrDefault("userCount", "0")))
                .commentCount(Long.parseLong(stats.getOrDefault("commentCount", "0")))
                .viewCountToday(Long.parseLong(stats.getOrDefault("viewCountToday", "0")))
                .viewCountTotal(Long.parseLong(stats.getOrDefault("viewCountTotal", "0")))
                .build();
        }

        // 查询数据库
        GlobalStatsVO statsVO = statsMapper.selectGlobalStats();

        // 缓存
        Map<String, String> data = new HashMap<>();
        data.put("articleCount", statsVO.getArticleCount().toString());
        data.put("userCount", statsVO.getUserCount().toString());
        data.put("commentCount", statsVO.getCommentCount().toString());
        data.put("viewCountToday", statsVO.getViewCountToday().toString());
        data.put("viewCountTotal", statsVO.getViewCountTotal().toString());

        redisService.hmSet(key, data, 300);

        return statsVO;
    }
}
```

---

### 1.7 配置数据缓存

#### 1.7.1 系统配置缓存

```java
/**
 * 系统配置缓存
 * 缓存Key: blog:config:{configKey}
 * 数据类型: String
 * 过期时间: 永久（主动刷新）
 * 说明: 系统配置参数
 */
public class SystemConfigCache {

    /**
     * 获取配置
     */
    public String getConfig(String key) {
        String cacheKey = "blog:config:" + key;
        String value = redisService.get(cacheKey);

        if (value != null) {
            return value;
        }

        // 查询数据库
        SysConfig config = configMapper.selectByKey(key);
        if (config != null) {
            redisService.set(cacheKey, config.getConfigValue());
            return config.getConfigValue();
        }

        return null;
    }

    /**
     * 刷新配置
     */
    public void refreshConfig(String key) {
        String cacheKey = "blog:config:" + key;
        redisService.remove(cacheKey);

        // 重新加载
        getConfig(key);
    }
}
```

---

#### 1.7.2 字典数据缓存

```java
/**
 * 数据字典缓存
 * 缓存Key: blog:dict:{dictType}
 * 数据类型: Hash
 * 过期时间: 1小时
 */
public class DictCache {

    /**
     * 获取字典数据
     */
    public Map<String, String> getDict(String dictType) {
        String key = "blog:dict:" + dictType;

        Map<String, String> dict = redisService.hgetAll(key);
        if (!dict.isEmpty()) {
            return dict;
        }

        // 查询数据库
        List<DictData> dictDataList = dictMapper.selectByType(dictType);

        Map<String, String> data = dictDataList.stream()
            .collect(Collectors.toMap(
                DictData::getDictValue,
                DictData::getDictLabel
            ));

        redisService.hmSet(key, data, 3600);

        return data;
    }
}
```

---

### 1.8 会话数据缓存

#### 1.8.1 用户登录状态缓存

```java
/**
 * 用户登录状态缓存
 * 缓存Key: blog:login:token:{token}
 * 数据类型: String (用户ID)
 * 过期时间: 7天
 * 说明: Token -> 用户ID映射
 */
public class LoginTokenCache {

    /**
     * 保存登录状态
     */
    public void saveLogin(String token, Long userId) {
        String key = "blog:login:token:" + token;
        redisService.set(key, userId.toString(), 7 * 24 * 3600);
    }

    /**
     * 获取登录用户
     */
    public Long getLoginUser(String token) {
        String key = "blog:login:token:" + token;
        String userId = redisService.get(key);

        if (userId != null) {
            return Long.parseLong(userId);
        }

        return null;
    }

    /**
     * 登出（删除token）
     */
    public void logout(String token) {
        String key = "blog:login:token:" + token;
        redisService.remove(key);
    }

    /**
     * 刷新token有效期
     */
    public void refreshToken(String token) {
        String key = "blog:login:token:" + token;
        redisService.expire(key, 7 * 24 * 3600);
    }
}
```

---

#### 1.8.2 在线用户缓存

```java
/**
 * 在线用户缓存
 * 缓存Key: blog:online:users
 * 数据类型: ZSet (score为最后活跃时间)
 * 过期时间: 30分钟无活动自动移除
 */
public class OnlineUsersCache {

    /**
     * 记录用户活动
     */
    public void recordActivity(Long userId) {
        String key = "blog:online:users";
        redisService.zadd(key, userId.toString(), System.currentTimeMillis());
        redisService.expire(key, 1800); // 30分钟
    }

    /**
     * 获取在线用户数
     */
    public long getOnlineCount() {
        String key = "blog:online:users";
        Long count = redisService.zsize(key);
        return count != null ? count : 0;
    }

    /**
     * 获取在线用户列表
     */
    public List<Long> getOnlineUsers(int limit) {
        String key = "blog:online:users";
        Set<String> userIds = redisService.zreverseRange(key, 0, limit - 1);

        return userIds.stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    /**
     * 清理过期用户
     */
    @Scheduled(fixedDelay = 300000) // 每5分钟
    public void cleanExpiredUsers() {
        String key = "blog:online:users";
        long expireTime = System.currentTimeMillis() - 1800000; // 30分钟前
        redisService.zremoveRangeByScore(key, 0, expireTime);
    }
}
```

---

### 1.9 搜索结果缓存

#### 1.9.1 搜索关键词缓存

```java
/**
 * 搜索结果缓存
 * 缓存Key: blog:search:result:{md5(关键词+筛选条件)}
 * 数据类型: String (JSON)
 * 过期时间: 10分钟
 * 说明: 缓存搜索结果，相同查询直接返回
 */
public class SearchResultCache {

    /**
     * 缓存搜索结果
     */
    public void cacheSearchResult(SearchRequest request, PageResult<ArticleDocument> result) {
        String cacheKey = generateCacheKey(request);

        redisService.set(cacheKey, JsonUtil.toJsonString(result), 600);
    }

    /**
     * 获取缓存结果
     */
    public PageResult<ArticleDocument> getCachedResult(SearchRequest request) {
        String cacheKey = generateCacheKey(request);
        String value = redisService.get(cacheKey);

        if (value != null) {
            return JsonUtil.fromJson(value, new TypeReference<PageResult<ArticleDocument>>() {});
        }

        return null;
    }

    /**
     * 生成缓存Key
     */
    private String generateCacheKey(SearchRequest request) {
        String data = String.format("%s-%s-%s-%s-%s",
            request.getKeyword(),
            request.getCategory(),
            request.getTags(),
            request.getStartTime(),
            request.getEndTime()
        );
        return "blog:search:result:" + DigestUtil.md5Hex(data);
    }
}
```

---

#### 1.9.2 热门搜索词缓存

```java
/**
 * 热门搜索词缓存
 * 缓存Key: blog:search:hot:keywords
 * 数据类型: ZSet (score为搜索次数)
 * 过期时间: 1天
 */
public class HotKeywordsCache {

    /**
     * 记录搜索
     */
    public void recordSearch(String keyword) {
        String key = "blog:search:hot:keywords";
        redisService.zadd(key, keyword.toLowerCase(), System.currentTimeMillis());
        redisService.expire(key, 86400);
    }

    /**
     * 获取热门搜索词
     */
    public List<String> getHotKeywords(int limit) {
        String key = "blog:search:hot:keywords";

        // 统计最近24小时
        long yesterday = System.currentTimeMillis() - 86400000;
        Set<String> keywords = redisService.zreverseRangeByScore(key, yesterday, System.currentTimeMillis(), 0, limit);

        return keywords.stream().collect(Collectors.toList());
    }
}
```

---

### 1.10 热点数据缓存

#### 1.10.1 热点文章自动缓存

```java
/**
 * 热点文章自动缓存
 * 缓存Key: blog:hot:article:{articleId}
 * 数据类型: String (JSON)
 * 过期时间: 1小时
 * 说明: 自动发现热点文章并缓存
 */
public class HotArticleCache {

    private final LoadingCache<Long, AtomicLong> accessCounter = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(key -> new AtomicLong(0));

    /**
     * 记录文章访问
     */
    public void recordAccess(Long articleId) {
        AtomicLong counter = accessCounter.get(articleId);
        long count = counter.incrementAndGet();

        // 访问超过100次，自动缓存为热点文章
        if (count == 100) {
            cacheHotArticle(articleId);
        }
    }

    /**
     * 缓存热点文章
     */
    @Async
    public void cacheHotArticle(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            String key = "blog:hot:article:" + articleId;
            redisService.set(key, JsonUtil.toJsonString(article), 3600);

            // 添加到热点文章排行榜
            String rankKey = "blog:hot:article:rank";
            redisService.zadd(rankKey, articleId.toString(), System.currentTimeMillis());
        }
    }
}
```

---

## 二、MQ使用方案

### 2.1 MQ场景总览

| 场景类型 | 是否使用MQ | Topic | 说明 |
|---------|-----------|-------|------|
| 数据同步 | ✅ | article-sync-es | 文章同步到ES |
| 统计更新 | ✅ | stats-update | 各类统计数据更新 |
| 消息通知 | ✅ | notification | 站内信、邮件通知 |
| 内容审核 | ✅ | content-audit | 文章/评论审核 |
| 定时发布 | ✅ | schedule-publish | 定时发布文章 |
| 数据归档 | ✅ | data-archive | 历史数据归档 |
| 日志收集 | ✅ | log-collection | 操作日志收集 |
| 缓存刷新 | ✅ | cache-refresh | 缓存刷新 |
| 搜索索引 | ✅ | search-index | 搜索索引更新 |
| 延迟任务 | ✅ | delay-task | 延迟任务处理 |

---

### 2.2 文章发布流程

#### 2.2.1 流程图

```
┌─────────────┐
│ 用户发布文章 │
└──────┬──────┘
       │
       ↓
┌──────────────────────────────────────┐
│ 1. 保存文章到数据库（事务）           │
│ 2. 删除相关缓存                       │
│ 3. 发送MQ消息（异步）                 │
└──────────────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────────────────────────────┐
│                      MQ消息分发                               │
├─────────────────┬─────────────────┬─────────────────────────┤
│  消费者1         │  消费者2         │  消费者3                │
│  同步到ES       │  更新统计       │  发送通知               │
└─────────────────┴─────────────────┴─────────────────────────┘
```

#### 2.2.2 消息定义

```java
/**
 * 文章发布消息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticlePublishedMessage implements Serializable {
    private Long articleId;           // 文章ID
    private Long userId;              // 作者ID
    private String title;             // 文章标题
    private Integer categoryId;       // 分类ID
    private List<String> tags;        // 标签列表
    private String summary;           // 摘要
    private Integer status;           // 状态
    private LocalDateTime publishTime; // 发布时间
    private String extra;             // 扩展信息
}
```

#### 2.2.3 生产者实现

```java
@Service
public class ArticlePublishService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CacheService cacheService;

    /**
     * 发布文章
     */
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDTO dto) {
        // 1. 保存文章
        Article article = BeanUtil.copyProperties(dto, Article.class);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.insert(article);

        // 2. 删除缓存
        cacheService.evictArticleListCache();

        // 3. 发送MQ消息
        ArticlePublishedMessage message = ArticlePublishedMessage.builder()
            .articleId(article.getId())
            .userId(article.getUserId())
            .title(article.getTitle())
            .categoryId(article.getCategoryId())
            .tags(dto.getTags())
            .summary(article.getSummary())
            .status(article.getStatus())
            .publishTime(LocalDateTime.now())
            .build();

        rocketMQTemplate.asyncSend("article-published-topic", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("文章发布消息发送成功: articleId={}", article.getId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("文章发布消息发送失败: articleId={}", article.getId(), e);
                // 记录到失败表，后续补偿
                saveFailedMessage(message);
            }
        });

        return article.getId();
    }
}
```

#### 2.2.4 消费者实现

```java
/**
 * 消费者1：同步到Elasticsearch
 */
@Component
@RocketMQMessageListener(
    topic = "article-published-topic",
    consumerGroup = "article-sync-es-consumer-group",
    consumeThreadNumber = 10
)
public class ArticleSyncEsConsumer implements RocketMQListener<ArticlePublishedMessage> {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public void onMessage(ArticlePublishedMessage message) {
        try {
            log.info("开始同步文章到ES: articleId={}", message.getArticleId());

            Article article = articleMapper.selectById(message.getArticleId());
            if (article == null) {
                log.warn("文章不存在: articleId={}", message.getArticleId());
                return;
            }

            // 转换为ES文档
            ArticleDocument document = convertToDocument(article);

            // 保存到ES
            elasticsearchTemplate.save(document);

            log.info("文章同步到ES成功: articleId={}", message.getArticleId());

        } catch (Exception e) {
            log.error("同步文章到ES失败: articleId={}", message.getArticleId(), e);
            throw e; // 抛出异常触发重试
        }
    }
}

/**
 * 消费者2：更新统计数据
 */
@Component
@RocketMQMessageListener(
    topic = "article-published-topic",
    consumerGroup = "article-stats-update-consumer-group"
)
public class ArticleStatsUpdateConsumer implements RocketMQListener<ArticlePublishedMessage> {

    @Autowired
    private StatisticsService statisticsService;

    @Override
    public void onMessage(ArticlePublishedMessage message) {
        try {
            // 更新用户文章数
            statisticsService.incrementArticleCount(message.getUserId());

            // 更新分类文章数
            if (message.getCategoryId() != null) {
                statisticsService.incrementCategoryCount(message.getCategoryId());
            }

            // 更新标签文章数
            if (CollectionUtils.isNotEmpty(message.getTags())) {
                statisticsService.incrementTagCount(message.getTags());
            }

        } catch (Exception e) {
            log.error("更新统计数据失败: articleId={}", message.getArticleId(), e);
        }
    }
}

/**
 * 消费者3：通知粉丝
 */
@Component
@RocketMQMessageListener(
    topic = "article-published-topic",
    consumerGroup = "article-notify-consumer-group"
)
public class ArticleNotifyConsumer implements RocketMQListener<ArticlePublishedMessage> {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onMessage(ArticlePublishedMessage message) {
        try {
            // 获取粉丝列表
            List<Long> followerIds = notificationService.getFollowers(message.getUserId());

            // 批量发送通知
            for (Long followerId : followerIds) {
                notificationService.sendArticlePublishNotification(followerId, message);
            }

        } catch (Exception e) {
            log.error("发送通知失败: articleId={}", message.getArticleId(), e);
        }
    }
}
```

---

### 2.3 评论发布流程

#### 2.3.1 消息定义

```java
/**
 * 评论创建消息
 */
@Data
@Builder
public class CommentCreatedMessage implements Serializable {
    private Long commentId;       // 评论ID
    private Long articleId;       // 文章ID
    private Long userId;          // 评论用户ID
    private String userName;      // 评论用户名
    private Long replyUserId;     // 被回复用户ID
    private Integer parentId;     // 父评论ID
    private String content;       // 评论内容
    private LocalDateTime createTime;
}
```

#### 2.3.2 生产者

```java
@Service
public class CommentService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Transactional(rollbackFor = Exception.class)
    public Long addComment(CommentDTO dto) {
        // 1. 保存评论
        Comment comment = BeanUtil.copyProperties(dto, Comment.class);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        // 2. 删除缓存
        cacheService.evictCommentCache(comment.getArticleId());

        // 3. 发送MQ消息
        CommentCreatedMessage message = CommentCreatedMessage.builder()
            .commentId(comment.getId())
            .articleId(comment.getArticleId())
            .userId(comment.getUserId())
            .userName(dto.getUserName())
            .replyUserId(dto.getReplyUserId())
            .parentId(comment.getParentId())
            .content(comment.getContent())
            .createTime(LocalDateTime.now())
            .build();

        rocketMQTemplate.syncSend("comment-created-topic", message);

        return comment.getId();
    }
}
```

#### 2.3.3 消费者

```java
/**
 * 消费者1：更新文章评论数
 */
@Component
@RocketMQMessageListener(
    topic = "comment-created-topic",
    consumerGroup = "comment-stats-consumer-group"
)
public class CommentStatsConsumer implements RocketMQListener<CommentCreatedMessage> {

    @Override
    public void onMessage(CommentCreatedMessage message) {
        // 更新文章评论数
        articleMapper.incrementCommentCount(message.getArticleId());
    }
}

/**
 * 消费者2：通知文章作者
 */
@Component
@RocketMQMessageListener(
    topic = "comment-created-topic",
    consumerGroup = "comment-notify-author-consumer-group"
)
public class CommentNotifyAuthorConsumer implements RocketMQListener<CommentCreatedMessage> {

    @Override
    public void onMessage(CommentCreatedMessage message) {
        // 获取文章作者
        Article article = articleMapper.selectById(message.getArticleId());

        // 如果评论者不是作者本人，发送通知
        if (!message.getUserId().equals(article.getUserId())) {
            notificationService.sendCommentNotification(article.getUserId(), message);
        }
    }
}

/**
 * 消费者3：通知被回复人
 */
@Component
@RocketMQMessageListener(
    topic = "comment-created-topic",
    consumerGroup = "comment-notify-reply-consumer-group"
)
public class CommentNotifyReplyConsumer implements RocketMQListener<CommentCreatedMessage> {

    @Override
    public void onMessage(CommentCreatedMessage message) {
        // 如果是回复评论，通知被回复人
        if (message.getReplyUserId() != null &&
            !message.getUserId().equals(message.getReplyUserId())) {
            notificationService.sendReplyNotification(message.getReplyUserId(), message);
        }
    }
}

/**
 * 消费者4：内容审核
 */
@Component
@RocketMQMessageListener(
    topic = "comment-created-topic",
    consumerGroup = "comment-audit-consumer-group"
)
public class CommentAuditConsumer implements RocketMQListener<CommentCreatedMessage> {

    @Override
    public void onMessage(CommentCreatedMessage message) {
        // 异步审核评论内容
        boolean pass = auditService.auditComment(message.getContent());

        if (!pass) {
            // 审核不通过，删除评论
            commentMapper.deleteById(message.getCommentId());
        }
    }
}
```

---

### 2.4 点赞/互动流程

#### 2.4.1 点赞消息定义

```java
/**
 * 点赞消息
 */
@Data
@Builder
public class LikeMessage implements Serializable {
    private Long targetId;        // 目标ID（文章ID/评论ID）
    private Long userId;          // 用户ID
    private String targetType;    // 目标类型（article/comment）
    private String action;        // 操作类型（like/unlike）
    private LocalDateTime createTime;
}
```

#### 2.4.2 点赞流程

```java
@Service
public class LikeService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 点赞文章
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean likeArticle(Long articleId, Long userId) {
        // 1. 检查是否已点赞
        String key = "blog:article:like:" + articleId;
        if (redisService.sIsMember(key, userId.toString())) {
            return false;
        }

        // 2. 添加点赞记录
        ArticleLike record = ArticleLike.builder()
            .articleId(articleId)
            .userId(userId)
            .createTime(LocalDateTime.now())
            .build();
        articleLikeMapper.insert(record);

        // 3. 更新缓存
        redisService.sadd(key, userId.toString());
        redisService.hincrby("blog:article:stats:" + articleId, "likeCount", 1);

        // 4. 发送MQ消息（异步处理）
        LikeMessage message = LikeMessage.builder()
            .targetId(articleId)
            .userId(userId)
            .targetType("article")
            .action("like")
            .createTime(LocalDateTime.now())
            .build();

        rocketMQTemplate.asyncSend("like-action-topic", message);

        return true;
    }

    /**
     * 取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeArticle(Long articleId, Long userId) {
        // 1. 检查是否已点赞
        String key = "blog:article:like:" + articleId;
        if (!redisService.sIsMember(key, userId.toString())) {
            return false;
        }

        // 2. 删除点赞记录
        articleLikeMapper.deleteByArticleIdAndUserId(articleId, userId);

        // 3. 更新缓存
        redisService.sremove(key, userId.toString());
        redisService.hincrby("blog:article:stats:" + articleId, "likeCount", -1);

        // 4. 发送MQ消息
        LikeMessage message = LikeMessage.builder()
            .targetId(articleId)
            .userId(userId)
            .targetType("article")
            .action("unlike")
            .createTime(LocalDateTime.now())
            .build();

        rocketMQTemplate.asyncSend("like-action-topic", message);

        return true;
    }
}
```

#### 2.4.3 点赞消息消费者

```java
/**
 * 点赞消息消费者
 */
@Component
@RocketMQMessageListener(
    topic = "like-action-topic",
    consumerGroup = "like-action-consumer-group"
)
public class LikeActionConsumer implements RocketMQListener<LikeMessage> {

    @Override
    public void onMessage(LikeMessage message) {
        try {
            if ("article".equals(message.getTargetType())) {
                handleArticleLike(message);
            } else if ("comment".equals(message.getTargetType())) {
                handleCommentLike(message);
            }
        } catch (Exception e) {
            log.error("处理点赞消息失败", e);
        }
    }

    /**
     * 处理文章点赞
     */
    private void handleArticleLike(LikeMessage message) {
        if ("like".equals(message.getAction())) {
            // 增加文章热度
            String hotKey = "blog:article:hot:" + message.getTargetId();
            redisService.increx(hotKey, 3600);

            // 增加作者获赞数
            Article article = articleMapper.selectById(message.getTargetId());
            redisService.hincrby("blog:user:stats:" + article.getUserId(), "likeCount", 1);

            // 增加用户积分
            scoreService.addScore(message.getUserId(), ScoreAction.LIKE_ARTICLE);

        } else if ("unlike".equals(message.getAction())) {
            // 减少作者获赞数
            Article article = articleMapper.selectById(message.getTargetId());
            redisService.hincrby("blog:user:stats:" + article.getUserId(), "likeCount", -1);
        }
    }
}
```

---

### 2.5 关注/取关流程

#### 2.5.1 关注消息定义

```java
/**
 * 关注消息
 */
@Data
@Builder
public class FollowMessage implements Serializable {
    private Long followerId;       // 粉丝ID
    private Long followingId;      // 被关注者ID
    private String action;         // follow/unfollow
    private LocalDateTime createTime;
}
```

#### 2.5.2 关注流程

```java
@Service
public class FollowService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 关注用户
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean followUser(Long followerId, Long followingId) {
        // 1. 检查是否已关注
        if (isFollowing(followerId, followingId)) {
            return false;
        }

        // 2. 不能关注自己
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }

        // 3. 创建关注记录
        Follow follow = Follow.builder()
            .followerId(followerId)
            .followingId(followingId)
            .createTime(LocalDateTime.now())
            .build();
        followMapper.insert(follow);

        // 4. 更新缓存
        // 添加到关注列表
        redisService.zadd("blog:user:following:" + followerId, followingId.toString(), System.currentTimeMillis());

        // 添加到粉丝列表
        redisService.zadd("blog:user:followers:" + followingId, followerId.toString(), System.currentTimeMillis());

        // 更新统计
        redisService.hincrby("blog:user:stats:" + followerId, "followingCount", 1);
        redisService.hincrby("blog:user:stats:" + followingId, "followerCount", 1);

        // 5. 发送MQ消息
        FollowMessage message = FollowMessage.builder()
            .followerId(followerId)
            .followingId(followingId)
            .action("follow")
            .createTime(LocalDateTime.now())
            .build();

        rocketMQTemplate.asyncSend("follow-action-topic", message);

        return true;
    }
}
```

#### 2.5.3 关注消息消费者

```java
/**
 * 关注消息消费者
 */
@Component
@RocketMQMessageListener(
    topic = "follow-action-topic",
    consumerGroup = "follow-action-consumer-group"
)
public class FollowActionConsumer implements RocketMQListener<FollowMessage> {

    @Override
    public void onMessage(FollowMessage message) {
        try {
            if ("follow".equals(message.getAction())) {
                // 发送通知给被关注者
                notificationService.sendFollowNotification(message.getFollowingId(), message.getFollowerId());

                // 增加积分
                scoreService.addScore(message.getFollowerId(), ScoreAction.FOLLOW);

            } else if ("unfollow".equals(message.getAction())) {
                // 取消关注，删除相关通知
                notificationService.deleteFollowNotification(message.getFollowingId(), message.getFollowerId());
            }

        } catch (Exception e) {
            log.error("处理关注消息失败", e);
        }
    }
}
```

---

### 2.6 消息通知流程

#### 2.6.1 通知消息定义

```java
/**
 * 通知消息
 */
@Data
@Builder
public class NotificationMessage implements Serializable {
    private Long notificationId;   // 通知ID
    private Long userId;           // 接收用户ID
    private String type;           // 通知类型（comment/reply/follow/like/system）
    private String title;          // 通知标题
    private String content;        // 通知内容
    private String link;           // 跳转链接
    private Boolean isRead;        // 是否已读
    private LocalDateTime createTime;
}
```

#### 2.6.2 通知发送流程

```java
@Service
public class NotificationService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送评论通知
     */
    public void sendCommentNotification(Long userId, CommentCreatedMessage commentMsg) {
        NotificationMessage message = NotificationMessage.builder()
            .userId(userId)
            .type("comment")
            .title("新评论通知")
            .content(String.format("%s评论了你的文章", commentMsg.getUserName()))
            .link("/article/" + commentMsg.getArticleId())
            .isRead(false)
            .createTime(LocalDateTime.now())
            .build();

        // 先保存到数据库
        Notification notification = BeanUtil.copyProperties(message, Notification.class);
        notificationMapper.insert(notification);

        // 发送MQ消息（异步推送给在线用户）
        rocketMQTemplate.asyncSend("notification-push-topic", message);
    }
}
```

#### 2.6.3 通知推送消费者

```java
/**
 * 通知推送消费者（WebSocket推送）
 */
@Component
@RocketMQMessageListener(
    topic = "notification-push-topic",
    consumerGroup = "notification-push-consumer-group"
)
public class NotificationPushConsumer implements RocketMQListener<NotificationMessage> {

    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    public void onMessage(NotificationMessage message) {
        try {
            // 检查用户是否在线
            if (webSocketServer.isOnline(message.getUserId())) {
                // WebSocket推送给用户
                webSocketServer.sendMessage(message.getUserId(), message);
            }

            // 如果用户不在线，发送邮件通知（可选）
            if (!webSocketServer.isOnline(message.getUserId())) {
                emailService.sendNotification(message);
            }

        } catch (Exception e) {
            log.error("推送通知失败", e);
        }
    }
}
```

---

### 2.7 数据同步流程

#### 2.7.1 同步到Elasticsearch

```java
/**
 * 数据同步消息
 */
@Data
@Builder
public class DataSyncMessage implements Serializable {
    private Long dataId;          // 数据ID
    private String dataType;      // 数据类型（article/comment/user）
    private String action;        // 操作类型（create/update/delete）
    private LocalDateTime createTime;
}

/**
 * ES同步消费者
 */
@Component
@RocketMQMessageListener(
    topic = "data-sync-es-topic",
    consumerGroup = "data-sync-es-consumer-group"
)
public class DataSyncEsConsumer implements RocketMQListener<DataSyncMessage> {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Override
    public void onMessage(DataSyncMessage message) {
        try {
            if ("article".equals(message.getDataType())) {
                syncArticle(message);
            } else if ("comment".equals(message.getDataType())) {
                syncComment(message);
            }

        } catch (Exception e) {
            log.error("同步数据到ES失败", e);
            throw e;
        }
    }

    private void syncArticle(DataSyncMessage message) {
        if ("delete".equals(message.getAction())) {
            // 删除索引
            elasticsearchTemplate.delete(message.getDataId().toString(), ArticleDocument.class);
        } else {
            // 创建/更新索引
            Article article = articleMapper.selectById(message.getDataId());
            if (article != null && article.getStatus() == 1) {
                ArticleDocument document = convertToDocument(article);
                elasticsearchTemplate.save(document);
            }
        }
    }
}
```

---

### 2.8 统计数据更新

#### 2.8.1 统计更新消息

```java
/**
 * 统计更新消息
 */
@Data
@Builder
public class StatsUpdateMessage implements Serializable {
    private String statsType;     // 统计类型（article/comment/user）
    private Long targetId;        // 目标ID
    private String field;         // 更新字段
    private Long delta;           // 变化量
}

/**
 * 统计更新消费者（批量更新到数据库）
 */
@Component
@RocketMQMessageListener(
    topic = "stats-update-topic",
    consumerGroup = "stats-update-consumer-group"
)
public class StatsUpdateConsumer implements RocketMQListener<StatsUpdateMessage> {

    private final Map<String, Map<Long, Map<String, Long>>> statsBuffer = new ConcurrentHashMap<>();

    @Override
    public void onMessage(StatsUpdateMessage message) {
        try {
            // 累加到内存缓冲区
            String key = message.getStatsType();
            statsBuffer.putIfAbsent(key, new ConcurrentHashMap<>());

            Map<Long, Map<String, Long>> targetMap = statsBuffer.get(key);
            targetMap.putIfAbsent(message.getTargetId(), new ConcurrentHashMap<>());

            Map<String, Long> fieldMap = targetMap.get(message.getTargetId());
            fieldMap.merge(message.getField(), message.getDelta(), Long::sum);

        } catch (Exception e) {
            log.error("更新统计数据失败", e);
        }
    }

    /**
     * 定时刷新统计数据到数据库（每分钟）
     */
    @Scheduled(fixedDelay = 60000)
    public void flushStatsToDB() {
        if (statsBuffer.isEmpty()) {
            return;
        }

        // 批量更新数据库
        for (Map.Entry<String, Map<Long, Map<String, Long>>> entry : statsBuffer.entrySet()) {
            String statsType = entry.getKey();
            Map<Long, Map<String, Long>> data = entry.getValue();

            if ("article".equals(statsType)) {
                articleMapper.batchUpdateStats(data);
            } else if ("user".equals(statsType)) {
                userMapper.batchUpdateStats(data);
            }
        }

        statsBuffer.clear();
    }
}
```

---

### 2.9 定时任务异步化

#### 2.9.1 数据归档

```java
/**
 * 数据归档消息
 */
@Data
@Builder
public class DataArchiveMessage implements Serializable {
    private String archiveType;    // 归档类型（article/comment/log）
    private Long dataId;          // 数据ID
    private String archivePath;   // 归档路径
}

/**
 * 数据归档消费者
 */
@Component
@RocketMQMessageListener(
    topic = "data-archive-topic",
    consumerGroup = "data-archive-consumer-group"
)
public class DataArchiveConsumer implements RocketMQListener<DataArchiveMessage> {

    @Override
    public void onMessage(DataArchiveMessage message) {
        try {
            if ("article".equals(message.getArchiveType())) {
                archiveArticle(message);
            } else if ("log".equals(message.getArchiveType())) {
                archiveLog(message);
            }

        } catch (Exception e) {
            log.error("数据归档失败", e);
        }
    }

    private void archiveArticle(DataArchiveMessage message) {
        // 1. 查询文章数据
        Article article = articleMapper.selectById(message.getDataId());

        // 2. 序列化到文件
        String archivePath = message.getArchivePath();
        FileUtil.writeUtf8String(JsonUtil.toJsonString(article), archivePath);

        // 3. 删除数据库记录（或标记为已归档）
        articleMapper.deleteById(message.getDataId());
    }
}
```

---

### 2.10 延迟任务处理

#### 2.10.1 定时发布

```java
/**
 * 定时发布消息
 */
@Data
@Builder
public class SchedulePublishMessage implements Serializable {
    private Long articleId;
    private LocalDateTime publishTime;
}

/**
 * 定时发布生产者
 */
@Service
public class SchedulePublishService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void schedulePublish(Long articleId, LocalDateTime publishTime) {
        long delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), publishTime);

        if (delayMillis <= 0) {
            // 立即发布
            publishArticleNow(articleId);
            return;
        }

        // 计算延迟级别
        int delayLevel = calculateDelayLevel(delayMillis);

        SchedulePublishMessage message = SchedulePublishMessage.builder()
            .articleId(articleId)
            .publishTime(publishTime)
            .build();

        Message<SchedulePublishMessage> msg = MessageBuilder.withPayload(message).build();
        rocketMQTemplate.syncSend("schedule-publish-topic", msg, 3000, delayLevel);
    }

    private int calculateDelayLevel(long delayMillis) {
        if (delayMillis <= 1000) return 1;  // 1s
        if (delayMillis <= 5000) return 2;  // 5s
        if (delayMillis <= 10000) return 3; // 10s
        // ... 其他级别
        if (delayMillis <= 3600000) return 17; // 1h
        return 18; // 2h
    }
}

/**
 * 定时发布消费者
 */
@Component
@RocketMQMessageListener(
    topic = "schedule-publish-topic",
    consumerGroup = "schedule-publish-consumer-group"
)
public class SchedulePublishConsumer implements RocketMQListener<SchedulePublishMessage> {

    @Override
    public void onMessage(SchedulePublishMessage message) {
        // 检查是否到达发布时间
        if (LocalDateTime.now().isAfter(message.getPublishTime())) {
            publishArticleNow(message.getArticleId());
        }
    }
}
```

---

## 三、缓存+MQ组合方案

### 3.1 阅读量更新

```
用户阅读文章
    ↓
1. 记录阅读（Redis Set去重）
    blog:article:view:users:{articleId}:{date}
    ↓
2. 阅读量+1（Redis计数器）
    blog:article:view:count:{articleId}
    ↓
3. 每10次阅读发送MQ消息
    article-view-batch-topic
    ↓
4. 消费者批量更新到数据库
    （每分钟或达到阈值）
```

### 3.2 点赞/取消点赞

```
用户点赞
    ↓
1. 检查是否已点赞（Redis Set）
    blog:article:like:{articleId}
    ↓
2. 添加点赞记录
    Redis Set + 数据库
    ↓
3. 点赞数+1（Redis Hash）
    blog:article:stats:{articleId}
    ↓
4. 发送MQ消息（异步处理）
    like-action-topic
    ↓
5. 消费者处理：
   - 更新文章热度
   - 更新作者获赞数
   - 增加用户积分
   - 发送通知
```

---

## 四、实施优先级

### 第一阶段（立即实施）

| 优先级 | 缓存/MQ | 说明 |
|--------|---------|------|
| P0 | 文章详情缓存 | 高频访问，必须缓存 |
| P0 | 用户信息缓存 | 高频访问，必须缓存 |
| P0 | 分类/标签缓存 | 变化不频繁，永久缓存 |
| P0 | 文章发布MQ | 解耦搜索、统计、通知 |
| P0 | 评论发布MQ | 解耦通知、审核 |

### 第二阶段（1-2周内）

| 优先级 | 缓存/MQ | 说明 |
|--------|---------|------|
| P1 | 文章列表缓存 | 中频访问，短期缓存 |
| P1 | 评论列表缓存 | 高频更新，超短期缓存 |
| P1 | 点赞/取消点赞MQ | 解耦积分、通知 |
| P1 | 阅读量批量更新MQ | 削峰填谷 |

### 第三阶段（1个月内）

| 优先级 | 缓存/MQ | 说明 |
|--------|---------|------|
| P2 | 统计数据缓存 | 定时更新 |
| P2 | 搜索结果缓存 | 短期缓存 |
| P2 | 数据同步MQ | 异步同步到ES |
| P2 | 定时发布MQ | 延迟消息 |

---

## 五、缓存高级细节

### 5.1 缓存预热机制

```java
/**
 * 缓存预热服务
 */
@Component
public class CacheWarmUpService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    /**
     * 应用启动时预热
     */
    @PostConstruct
    public void warmUpOnStart() {
        log.info("开始缓存预热...");

        // 异步预热，不阻塞应用启动
        CompletableFuture.runAsync(() -> {
            warmUpHotArticles();
            warmUpCategories();
            warmUpTags();
            warmUpConfig();
        });

        log.info("缓存预热任务已提交");
    }

    /**
     * 预热热门文章
     */
    private void warmUpHotArticles() {
        try {
            // 查询最近7天热门文章（按阅读量排序）
            List<Article> hotArticles = articleMapper.getHotArticles(7, 100);

            for (Article article : hotArticles) {
                String key = "blog:article:info:" + article.getId();
                redisService.set(key, JsonUtil.toJsonString(article), 1800);
            }

            log.info("热门文章预热完成: {} 篇", hotArticles.size());

        } catch (Exception e) {
            log.error("预热热门文章失败", e);
        }
    }

    /**
     * 预热分类数据
     */
    private void warmUpCategories() {
        try {
            List<Category> categories = categoryMapper.selectAll();

            for (Category category : categories) {
                String key = "blog:category:info:" + category.getId();
                redisService.set(key, JsonUtil.toJsonString(category), 3600);
            }

            // 缓存分类列表
            String listKey = "blog:category:list";
            redisService.lrightPushAll(listKey, categories.stream()
                .map(JsonUtil::toJsonString)
                .collect(Collectors.toList()));
            redisService.expire(listKey, 3600);

            log.info("分类数据预热完成: {} 个", categories.size());

        } catch (Exception e) {
            log.error("预热分类数据失败", e);
        }
    }

    /**
     * 预热标签数据
     */
    private void warmUpTags() {
        try {
            List<Tag> tags = tagMapper.selectAll();

            for (Tag tag : tags) {
                String key = "blog:tag:info:" + tag.getId();
                redisService.set(key, JsonUtil.toJsonString(tag), 3600);
            }

            log.info("标签数据预热完成: {} 个", tags.size());

        } catch (Exception e) {
            log.error("预热标签数据失败", e);
        }
    }

    /**
     * 预热系统配置
     */
    private void warmUpConfig() {
        try {
            List<SysConfig> configs = configMapper.selectAll();

            for (SysConfig config : configs) {
                String key = "blog:config:" + config.getConfigKey();
                redisService.set(key, config.getConfigValue());
            }

            log.info("系统配置预热完成: {} 个", configs.size());

        } catch (Exception e) {
            log.error("预热系统配置失败", e);
        }
    }

    /**
     * 定时预热（每小时）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledWarmUp() {
        log.info("开始定时缓存预热...");
        warmUpHotArticles();
    }
}
```

---

### 5.2 缓存序列化选择

#### 5.2.1 序列化方案对比

| 序列化方式 | 优点 | 缺点 | 适用场景 |
|-----------|------|------|---------|
| **JSON** | 可读、跨语言 | 性能一般、体积大 | 通用场景 |
| **Protobuf** | 性能好、体积小 | 不便调试 | 高性能场景 |
| **Kryo** | 性能极佳 | 需注册类 | 内部服务 |
| **FST** | 性能好 | JDK版本敏感 | Java应用 |
| **Hessian** | 跨语言 | 性能一般 | 跨语言场景 |

#### 5.2.2 Redis序列化配置

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化value
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        // 指定要序列化的域
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        serializer.setObjectMapper(mapper);

        // 使用StringRedisSerializer来序列化和反序列化key
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 值使用JSON序列化
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

---

### 5.3 大Key/热Key问题处理

#### 5.3.1 大Key拆分

```java
/**
 * 大Key拆分示例：文章标签关联
 */
public class ArticleTagCacheService {

    /**
     * ❌ 不好的做法：大Key存储所有标签
     * Key: blog:article:tags:12345
     * Value: [1,2,3,4,5,...,1000]  // 假设文章有1000个标签
     */
    public void saveArticleTagsBad(Long articleId, List<Long> tagIds) {
        String key = "blog:article:tags:" + articleId;
        redisService.set(key, JsonUtil.toJsonString(tagIds));
    }

    /**
     * ✅ 好的做法：拆分成多个小Key
     * Key: blog:article:tags:12345:0, blog:article:tags:12345:1, ...
     * Value: [1,2,3,4,5]  // 每个Key最多100个标签
     */
    public void saveArticleTagsGood(Long articleId, List<Long> tagIds) {
        int batchSize = 100;
        List<List<Long>> batches = Lists.partition(tagIds, batchSize);

        for (int i = 0; i < batches.size(); i++) {
            String key = "blog:article:tags:" + articleId + ":" + i;
            redisService.set(key, JsonUtil.toJsonString(batches.get(i)), 3600);
        }

        // 记录总批次
        String countKey = "blog:article:tags:count:" + articleId;
        redisService.set(countKey, String.valueOf(batches.size()), 3600);
    }

    /**
     * 分页读取标签
     */
    public List<Long> getArticleTags(Long articleId, int page, int size) {
        String key = "blog:article:tags:" + articleId + ":" + page;
        String value = redisService.get(key);

        if (value != null) {
            return JsonUtil.fromJson(value, new TypeReference<List<Long>>() {});
        }

        return Collections.emptyList();
    }
}
```

#### 5.3.2 热Key处理

```java
/**
 * 热Key处理：添加随机后缀分散到不同Redis节点
 */
@Service
public class HotKeyService {

    private static final String HOT_KEY_PREFIX = "hot:article:";

    /**
     * ✅ 热Key分散：添加随机后缀
     */
    public void cacheHotArticle(Long articleId, Article article) {
        // 生成多个副本，分散到不同槽位
        for (int i = 0; i < 3; i++) {
            String key = HOT_KEY_PREFIX + articleId + ":" + i;
            redisService.set(key, JsonUtil.toJsonString(article), 1800);
        }
    }

    /**
     * 随机读取副本
     */
    public Article getHotArticle(Long articleId) {
        int random = ThreadLocalRandom.current().nextInt(3);
        String key = HOT_KEY_PREFIX + articleId + ":" + random;
        String value = redisService.get(key);

        if (value != null) {
            return JsonUtil.fromJson(value, Article.class);
        }

        // 如果没读到，尝试其他副本
        for (int i = 0; i < 3; i++) {
            if (i != random) {
                key = HOT_KEY_PREFIX + articleId + ":" + i;
                value = redisService.get(key);
                if (value != null) {
                    return JsonUtil.fromJson(value, Article.class);
                }
            }
        }

        return null;
    }

    /**
     * 本地缓存 + Redis二级缓存（推荐）
     */
    private final Cache<Long, Article> localHotCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build();

    public Article getHotArticleWithLocalCache(Long articleId) {
        // L1: 本地缓存
        Article article = localHotCache.getIfPresent(articleId);
        if (article != null) {
            return article;
        }

        // L2: Redis缓存
        article = getHotArticle(articleId);
        if (article != null) {
            localHotCache.put(articleId, article);
        }

        return article;
    }
}
```

---

### 5.4 缓存监控指标

```java
/**
 * 缓存监控服务
 */
@Component
public class CacheMonitorService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 记录缓存命中率
     */
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong cacheMissCount = new AtomicLong(0);

    public void recordCacheHit() {
        long hits = cacheHitCount.incrementAndGet();
        // 记录到Micrometer
        meterRegistry.counter("cache.hits").increment();
    }

    public void recordCacheMiss() {
        long misses = cacheMissCount.incrementAndGet();
        meterRegistry.counter("cache.misses").increment();
    }

    public double getHitRate() {
        long hits = cacheHitCount.get();
        long misses = cacheMissCount.get();
        long total = hits + misses;

        return total == 0 ? 0 : (double) hits / total;
    }

    /**
     * 定时上报缓存统计
     */
    @Scheduled(fixedDelay = 60000)
    public void reportCacheStats() {
        double hitRate = getHitRate();

        // 上报到监控系统
        meterRegistry.gauge("cache.hit.rate", hitRate);

        log.info("缓存命中率: {:.2f}%, 命中次数: {}, 未命中次数: {}",
            hitRate * 100, cacheHitCount.get(), cacheMissCount.get());
    }

    /**
     * 获取Redis内存使用情况
     */
    public Map<String, Object> getRedisMemoryStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取Redis info memory
        String info = redisService.executeScript(
            new DefaultRedisScript<>("return redis.call('info', 'memory')", String.class),
            Collections.emptyList()
        );

        stats.put("memoryInfo", info);

        // 获取Key数量
        Long dbSize = redisService.executeScript(
            new DefaultRedisScript<>("return redis.call('dbsize')", Long.class),
            Collections.emptyList()
        );

        stats.put("keyCount", dbSize);

        return stats;
    }

    /**
     * 获取慢查询日志
     */
    public List<String> getSlowLog() {
        return redisService.executeScript(
            new DefaultRedisScript<>("return redis.call('slowlog', 'get', 10)", List.class),
            Collections.emptyList()
        );
    }
}
```

---

### 5.5 缓存容量规划

#### 5.5.1 容量估算

| 数据类型 | 单条大小 | 数量 | 总大小 | 过期时间 |
|---------|---------|------|--------|---------|
| 文章详情 | 5KB | 10万 | 500MB | 30分钟 |
| 用户信息 | 2KB | 10万 | 200MB | 1小时 |
| 文章列表 | 50KB | 1万 | 500MB | 10分钟 |
| 评论列表 | 30KB | 5万 | 1.5GB | 5分钟 |
| 统计数据 | 100B | 100万 | 100MB | 10分钟 |
| **合计** | - | - | **~3GB** | - |

**Redis集群规划**：
- 单节点内存：8GB
- 预留空间：40%（用于复制、过期等）
- 有效容量：4.8GB
- 推荐节点数：1个主节点 + 1个从节点

---

## 六、MQ高级细节

### 6.1 消息幂等性保障

#### 6.1.1 生产者幂等

```java
/**
 * 生产者幂等性：使用唯一消息ID
 */
@Service
public class IdempotentProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送幂等消息
     */
    public <T> void sendIdempotentMessage(String topic, T message, String businessKey) {
        // 生成唯一消息ID
        String messageId = generateMessageId(topic, businessKey);

        // 检查是否已发送（Redis记录）
        String key = "mq:message:sent:" + topic + ":" + businessKey;
        if (redisService.exists(key)) {
            log.warn("消息已发送，跳过: topic={}, businessKey={}", topic, businessKey);
            return;
        }

        // 构建消息
        Message<T> msg = MessageBuilder.withPayload(message)
            .setHeader(RocketMQHeaders.KEYS, messageId)
            .build();

        // 发送消息
        SendResult result = rocketMQTemplate.syncSend(topic, msg);

        if (result.getSendStatus() == SendStatus.SEND_OK) {
            // 记录已发送（24小时过期）
            redisService.set(key, messageId, 86400);
        }
    }

    /**
     * 生成唯一消息ID
     */
    private String generateMessageId(String topic, String businessKey) {
        return DigestUtil.md5Hex(topic + ":" + businessKey + ":" + System.currentTimeMillis());
    }
}
```

#### 6.1.2 消费者幂等

```java
/**
 * 消费者幂等性处理
 */
@Component
public class IdempotentConsumer {

    /**
     * 幂等处理模板
     */
    public <T> void consumeWithIdempotent(
        String topic,
        T message,
        String uniqueKey,
        Consumer<T> consumer
    ) {
        // 检查是否已消费
        String key = "mq:message:consumed:" + topic + ":" + uniqueKey;

        Boolean success = redisService.setNxEx(key, "1", 3600); // 1小时
        if (Boolean.FALSE.equals(success)) {
            log.warn("消息已处理，跳过: topic={}, uniqueKey={}", topic, uniqueKey);
            return;
        }

        try {
            // 处理消息
            consumer.accept(message);

            log.info("消息处理成功: topic={}, uniqueKey={}", topic, uniqueKey);

        } catch (Exception e) {
            log.error("消息处理失败: topic={}, uniqueKey={}", topic, uniqueKey, e);

            // 删除标记，允许重试
            redisService.remove(key);

            throw new RuntimeException("消息处理失败", e);
        }
    }
}

/**
 * 使用示例
 */
@Component
@RocketMQMessageListener(
    topic = "article-published-topic",
    consumerGroup = "article-consumer-group"
)
public class ArticleConsumer implements RocketMQListener<ArticlePublishedMessage> {

    @Autowired
    private IdempotentConsumer idempotentConsumer;

    @Override
    public void onMessage(ArticlePublishedMessage message) {
        idempotentConsumer.consumeWithIdempotent(
            "article-published",
            message,
            String.valueOf(message.getArticleId()), // 使用文章ID作为唯一标识
            (msg) -> {
                // 实际的业务处理
                handleArticlePublish(msg);
            }
        );
    }
}
```

---

### 6.2 消息顺序性保障

```java
/**
 * 顺序消息生产者
 */
@Service
public class OrderedMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送顺序消息
     * @param queueId 队列ID（同一业务使用相同ID保证顺序）
     */
    public <T> void sendOrderedMessage(String topic, T message, String businessKey, int queueId) {
        Message<T> msg = MessageBuilder.withPayload(message)
            .setHeader(RocketMQHeaders.KEYS, businessKey)
            .build();

        // 同步发送，指定队列
        rocketMQTemplate.syncSendOrderly(topic, msg, String.valueOf(queueId));
    }
}

/**
 * 顺序消息消费者
 */
@Component
@RocketMQMessageListener(
    topic = "ordered-article-topic",
    consumerGroup = "ordered-article-consumer-group",
    consumeMode = ConsumeMode.ORDERLY  // 顺序消费模式
)
public class OrderedArticleConsumer implements RocketMQListener<ArticlePublishedMessage> {

    @Override
    public void onMessage(ArticlePublishedMessage message) {
        // 单线程顺序处理
        log.info("顺序处理文章: articleId={}", message.getArticleId());
        // 处理逻辑...
    }
}
```

---

### 6.3 消息重试策略

```java
/**
 * 消息重试配置
 */
@Component
@RocketMQMessageListener(
    topic = "article-published-topic",
    consumerGroup = "article-consumer-group",
    maxReconsumeTimes = 3,  // 最大重试次数
    enableMsgTrace = true,   // 启用消息轨迹
    traceTopic = "RMQ_SYS_TRACE_TOPIC"
)
public class RetryMessageConsumer implements RocketMQListener<ArticlePublishedMessage> {

    @Override
    public void onMessage(ArticlePublishedMessage message) {
        try {
            // 业务处理
            processArticle(message);

        } catch (Exception e) {
            log.error("处理消息失败，将进行重试: articleId={}", message.getArticleId(), e);

            // 抛出异常触发重试
            throw new RuntimeException("处理失败，请重试", e);
        }
    }

    /**
     * 处理消息（带最大重试次数判断）
     */
    private void processArticle(ArticlePublishedMessage message) {
        // 获取重试次数
        int reconsumeTimes = MessageAccessUtil.getReconsumeTimes(message);

        if (reconsumeTimes >= 3) {
            // 超过最大重试次数，记录到失败表
            log.error("消息重试次数超限，记录到失败表: articleId={}", message.getArticleId());
            saveToFailedTable(message);

            // 不再抛出异常，避免无限重试
            return;
        }

        // 正常处理逻辑
        doProcess(message);
    }
}
```

---

### 6.4 死信队列处理

```java
/**
 * 死信队列消费者
 */
@Component
@RocketMQMessageListener(
    topic = "%DLQ%article-consumer-group",  // 死信队列Topic
    consumerGroup = "dlq-article-consumer-group"
)
public class DeadLetterQueueConsumer implements RocketMQListener<ArticlePublishedMessage> {

    @Autowired
    private FailedMessageService failedMessageService;

    @Override
    public void onMessage(ArticlePublishedMessage message) {
        log.error("收到死信消息: articleId={}", message.getArticleId());

        try {
            // 1. 记录到失败消息表
            failedMessageService.save(message);

            // 2. 发送告警通知
            alertService.sendAlert("文章处理失败", message);

            // 3. 判断是否需要人工介入
            if (needManualIntervention(message)) {
                createManualTask(message);
            }

        } catch (Exception e) {
            log.error("处理死信消息失败", e);
        }
    }

    /**
     * 判断是否需要人工介入
     */
    private boolean needManualIntervention(ArticlePublishedMessage message) {
        // 例如：文章内容包含敏感词
        return sensitiveWordService.containsSensitiveWord(message.getTitle());
    }

    /**
     * 创建人工处理任务
     */
    private void createManualTask(ArticlePublishedMessage message) {
        ManualTask task = ManualTask.builder()
            .taskType("ARTICLE_PUBLISH_FAILED")
            .businessId(String.valueOf(message.getArticleId()))
            .content(JsonUtil.toJsonString(message))
            .status("PENDING")
            .createTime(LocalDateTime.now())
            .build();

        manualTaskService.create(task);
    }
}
```

---

### 6.5 消息积压处理

```java
/**
 * 消息积压监控和处理
 */
@Component
public class MessageBacklogMonitor {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.consumer.group}")
    private String consumerGroup;

    /**
     * 监控消息积压
     */
    @Scheduled(fixedDelay = 60000)  // 每分钟检查
    public void monitorBacklog() {
        try {
            DefaultMQAdminExt adminExt = new DefaultMQAdminExt();
            adminExt.setNamesrvAddr(nameServer);
            adminExt.start();

            // 获取消费组统计信息
            ConsumeStats consumeStats = adminExt.examineConsumeStats(consumerGroup);

            for (Map.Entry<MessageQueue, OffsetWrapper> entry : consumeStats.getOffsetTable().entrySet()) {
                MessageQueue mq = entry.getKey();
                OffsetWrapper offset = entry.getValue();

                long diff = offset.getLogOffsetOffset() - offset.getConsumerOffset();

                // 积压超过10000条，告警
                if (diff > 10000) {
                    log.warn("消息积压告警: topic={}, queueId={}, 积压={}",
                        mq.getTopic(), mq.getQueueId(), diff);

                    // 发送告警
                    alertService.sendAlert("消息积压", mq.getTopic() + " 积压: " + diff);

                    // 触发消费者扩容
                    scaleUpConsumers(mq.getTopic());
                }
            }

            adminExt.shutdown();

        } catch (Exception e) {
            log.error("监控消息积压失败", e);
        }
    }

    /**
     * 消费者扩容（动态增加实例）
     */
    private void scaleUpConsumers(String topic) {
        // 通知运维平台或K8s扩容
        log.info("触发消费者扩容: topic={}", topic);

        // 调用扩容接口
        scalingService.scaleUpConsumers(consumerGroup, 2);  // 增加2个实例
    }
}
```

---

### 6.6 消息监控告警

```java
/**
 * MQ监控服务
 */
@Component
public class MQMonitorService {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 记录消息发送成功
     */
    public void recordMessageSent(String topic) {
        meterRegistry.counter("mq.message.sent", "topic", topic).increment();
    }

    /**
     * 记录消息发送失败
     */
    public void recordMessageSendFailed(String topic) {
        meterRegistry.counter("mq.message.send.failed", "topic", topic).increment();
    }

    /**
     * 记录消息消费成功
     */
    public void recordMessageConsumed(String topic, String group) {
        meterRegistry.counter("mq.message.consumed",
            "topic", topic, "group", group).increment();
    }

    /**
     * 记录消息消费失败
     */
    public void recordMessageConsumeFailed(String topic, String group) {
        meterRegistry.counter("mq.message.consume.failed",
            "topic", topic, "group", group).increment();
    }

    /**
     * 记录消息处理耗时
     */
    public void recordMessageConsumeTime(String topic, long millis) {
        meterRegistry.timer("mq.message.consume.time",
            "topic", topic).record(millis, TimeUnit.MILLISECONDS);
    }
}

/**
 * 消息消费者切面（自动记录指标）
 */
@Aspect
@Component
public class MQConsumerAspect {

    @Autowired
    private MQMonitorService mqMonitorService;

    @Around("@annotation(org.apache.rocketmq.spring.annotation.RocketMQMessageListener)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String topic = getTopic(point);
        String group = getGroup(point);

        long startTime = System.currentTimeMillis();

        try {
            Object result = point.proceed();

            mqMonitorService.recordMessageConsumed(topic, group);

            long duration = System.currentTimeMillis() - startTime;
            mqMonitorService.recordMessageConsumeTime(topic, duration);

            return result;

        } catch (Throwable e) {
            mqMonitorService.recordMessageConsumeFailed(topic, group);
            throw e;
        }
    }
}
```

---

## 七、更多业务缓存细节

### 7.1 用户浏览历史

```java
/**
 * 用户浏览历史缓存
 * 缓存Key: blog:user:browse:{userId}
 * 数据类型: ZSet (score为浏览时间)
 * 过期时间: 30天
 */
@Service
public class UserBrowseHistoryCache {

    private static final String KEY_PREFIX = "blog:user:browse:";
    private static final int MAX_HISTORY = 500;  // 最多保存500条

    /**
     * 记录浏览历史
     */
    public void addBrowseHistory(Long userId, Long articleId) {
        String key = KEY_PREFIX + userId;

        // 添加到ZSet
        redisService.zadd(key, articleId.toString(), System.currentTimeMillis());

        // 只保留最近的500条
        redisService.zremoveRangeByScore(key, 0, System.currentTimeMillis() - 30L * 24 * 3600 * 1000);

        long size = redisService.zsize(key);
        if (size > MAX_HISTORY) {
            // 删除最老的数据
            long removeCount = size - MAX_HISTORY;
            redisService.zremoveRange(key, 0, removeCount - 1);
        }

        redisService.expire(key, 30 * 86400);  // 30天过期
    }

    /**
     * 获取浏览历史（分页）
     */
    public List<Long> getBrowseHistory(Long userId, int page, int size) {
        String key = KEY_PREFIX + userId;

        long start = (page - 1) * size;
        long end = page * size - 1;

        Set<String> articleIds = redisService.zreverseRange(key, start, end);

        return articleIds.stream()
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    /**
     * 清除浏览历史
     */
    public void clearBrowseHistory(Long userId) {
        String key = KEY_PREFIX + userId;
        redisService.remove(key);
    }
}
```

---

### 7.2 用户搜索历史

```java
/**
 * 用户搜索历史缓存
 * 缓存Key: blog:user:search:{userId}
 * 数据类型: ZSet (score为搜索时间)
 * 过期时间: 30天
 */
@Service
public class UserSearchHistoryCache {

    private static final String KEY_PREFIX = "blog:user:search:";
    private static final int MAX_HISTORY = 100;

    /**
     * 记录搜索历史
     */
    public void addSearchHistory(Long userId, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return;
        }

        String key = KEY_PREFIX + userId;

        // 添加到ZSet
        redisService.zadd(key, keyword.toLowerCase(), System.currentTimeMillis());

        // 只保留最近100条
        long size = redisService.zsize(key);
        if (size > MAX_HISTORY) {
            redisService.zremoveRange(key, 0, size - MAX_HISTORY - 1);
        }

        redisService.expire(key, 30 * 86400);
    }

    /**
     * 获取搜索历史
     */
    public List<String> getSearchHistory(Long userId, int limit) {
        String key = KEY_PREFIX + userId;

        Set<String> keywords = redisService.zreverseRange(key, 0, limit - 1);

        return keywords.stream().collect(Collectors.toList());
    }

    /**
     * 清空搜索历史
     */
    public void clearSearchHistory(Long userId) {
        String key = KEY_PREFIX + userId;
        redisService.remove(key);
    }

    /**
     * 获取热词推荐（基于搜索历史）
     */
    public List<String> getHotKeywords(Long userId, int limit) {
        String key = KEY_PREFIX + userId;

        // 统计最近7天的搜索词频率
        long sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 3600 * 1000;

        // 这里需要使用额外的计数器来统计频率
        // 简化版本：返回最近的搜索词
        return getSearchHistory(userId, limit);
    }
}
```

---

### 7.3 文章草稿缓存

```java
/**
 * 文章草稿缓存（自动保存）
 * 缓存Key: blog:article:draft:{userId}
 * 数据类型: Hash
 * 过期时间: 7天
 */
@Service
public class ArticleDraftCache {

    private static final String KEY_PREFIX = "blog:article:draft:";

    /**
     * 保存草稿（自动保存）
     */
    public void saveDraft(Long userId, ArticleDTO draft) {
        String key = KEY_PREFIX + userId;

        Map<String, String> data = new HashMap<>();
        data.put("title", draft.getTitle());
        data.put("content", draft.getContent());
        data.put("contentMd", draft.getContentMd());
        data.put("categoryId", draft.getCategoryId() != null ? draft.getCategoryId().toString() : "");
        data.put("tags", JsonUtil.toJsonString(draft.getTags()));
        data.put("saveTime", String.valueOf(System.currentTimeMillis()));

        redisService.hmSet(key, data, 7 * 86400);  // 7天
    }

    /**
     * 获取草稿
     */
    public ArticleDTO getDraft(Long userId) {
        String key = KEY_PREFIX + userId;

        Map<String, String> data = redisService.hgetAll(key);

        if (data.isEmpty()) {
            return null;
        }

        ArticleDTO draft = new ArticleDTO();
        draft.setTitle(data.getOrDefault("title", ""));
        draft.setContent(data.get("content"));
        draft.setContentMd(data.get("contentMd"));

        if (StringUtils.isNotBlank(data.get("categoryId"))) {
            draft.setCategoryId(Integer.parseInt(data.get("categoryId")));
        }

        if (StringUtils.isNotBlank(data.get("tags"))) {
            draft.setTags(JsonUtil.fromJson(data.get("tags"), new TypeReference<List<String>>() {}));
        }

        return draft;
    }

    /**
     * 删除草稿（发布后）
     */
    public void deleteDraft(Long userId) {
        String key = KEY_PREFIX + userId;
        redisService.remove(key);
    }

    /**
     * 定时自动保存（每30秒）
     * 前端需配合调用此接口
     */
    @Scheduled(fixedDelay = 30000)
    public void autoSaveDrafts() {
        // 扫描所有正在编辑的草稿，从内存中获取最新数据并保存到Redis
        // 这里需要配合前端实现，前端需要定期调用保存草稿接口
    }
}
```

---

### 7.4 图片/头像缓存

```java
/**
 * 图片缓存策略
 */
@Service
public class ImageCacheService {

    /**
     * 用户头像缓存
     * 缓存Key: blog:image:avatar:{userId}
     * 过期时间: 7天
     */
    public String getAvatarUrl(Long userId) {
        String key = "blog:image:avatar:" + userId;
        String url = redisService.get(key);

        if (url != null) {
            return url;
        }

        // 查询数据库
        User user = userMapper.selectById(userId);
        url = user.getAvatar();

        if (StringUtils.isNotBlank(url)) {
            // 缓存7天
            redisService.set(key, url, 7 * 86400);
        }

        return url;
    }

    /**
     * 文章封面图缓存
     * 缓存Key: blog:image:cover:{articleId}
     * 过期时间: 1天
     */
    public String getCoverUrl(Long articleId) {
        String key = "blog:image:cover:" + articleId;
        String url = redisService.get(key);

        if (url != null) {
            return url;
        }

        Article article = articleMapper.selectById(articleId);
        url = article.getCover();

        if (StringUtils.isNotBlank(url)) {
            redisService.set(key, url, 86400);
        }

        return url;
    }

    /**
     * 图片URL签名（防止盗链）
     */
    public String generateSignedUrl(String originalUrl, int expireSeconds) {
        // 生成带签名的URL
        long timestamp = System.currentTimeMillis();
        String sign = DigestUtil.md5Hex(originalUrl + timestamp + "secret");

        return originalUrl + "?sign=" + sign + "&timestamp=" + timestamp + "&expire=" + expireSeconds;
    }
}
```

---

### 7.5 评论树结构缓存

```java
/**
 * 评论树结构缓存
 * 缓存Key: blog:comment:tree:{articleId}
 * 数据类型: String (JSON)
 * 过期时间: 5分钟
 */
@Service
public class CommentTreeCache {

    /**
     * 获取评论树
     */
    public List<CommentTreeNode> getCommentTree(Long articleId) {
        String key = "blog:comment:tree:" + articleId;
        String value = redisService.get(key);

        if (value != null) {
            return JsonUtil.fromJson(value, new TypeReference<List<CommentTreeNode>>() {});
        }

        // 查询数据库
        List<Comment> comments = commentMapper.selectByArticleId(articleId);

        // 构建树结构
        List<CommentTreeNode> tree = buildCommentTree(comments);

        // 缓存5分钟
        redisService.set(key, JsonUtil.toJsonString(tree), 300);

        return tree;
    }

    /**
     * 构建评论树
     */
    private List<CommentTreeNode> buildCommentTree(List<Comment> comments) {
        Map<Integer, CommentTreeNode> nodeMap = new HashMap<>();
        List<CommentTreeNode> rootNodes = new ArrayList<>();

        // 第一遍：创建所有节点
        for (Comment comment : comments) {
            CommentTreeNode node = CommentTreeNode.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .build();

            nodeMap.put(node.getId(), node);
        }

        // 第二遍：建立父子关系
        for (Comment comment : comments) {
            CommentTreeNode node = nodeMap.get(comment.getId());

            if (comment.getParentId() == null || comment.getParentId() == 0) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点
                CommentTreeNode parent = nodeMap.get(comment.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }

        return rootNodes;
    }

    /**
     * 清除评论树缓存
     */
    public void evictCommentTree(Long articleId) {
        String key = "blog:comment:tree:" + articleId;
        redisService.remove(key);
    }
}
```

---

### 7.6 用户行为数据缓存

```java
/**
 * 用户行为数据缓存
 * 用于推荐算法、用户画像等
 */
@Service
public class UserBehaviorCache {

    /**
     * 用户感兴趣的话题（基于阅读历史）
     * 缓存Key: blog:user:interest:{userId}
     * 数据类型: ZSet (score为兴趣分数)
     * 过期时间: 1天
     */
    public void recordInterest(Long userId, Long categoryId) {
        String key = "blog:user:interest:" + userId;

        // 增加兴趣分数
        redisService.hincrby(key, categoryId.toString(), 1);
        redisService.expire(key, 86400);
    }

    /**
     * 获取用户兴趣标签
     */
    public List<Long> getUserInterests(Long userId, int limit) {
        String key = "blog:user:interest:" + userId;

        Map<String, String> interests = redisService.hgetAll(key);

        return interests.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(
                Long.parseLong(e2.getValue()),
                Long.parseLong(e1.getValue())
            ))
            .limit(limit)
            .map(e -> Long.parseLong(e.getKey()))
            .collect(Collectors.toList());
    }

    /**
     * 用户活跃时间统计
     * 缓存Key: blog:user:active:{userId}:{date}
     * 数据类型: String (活跃分钟数)
     */
    public void recordUserActiveTime(Long userId, int minutes) {
        String date = LocalDate.now().toString();
        String key = "blog:user:active:" + userId + ":" + date;

        redisService.increx(key, minutes);
        redisService.expire(key, 7 * 86400);  // 保留7天
    }

    /**
     * 获取用户今日活跃时长
     */
    public int getTodayActiveTime(Long userId) {
        String date = LocalDate.now().toString();
        String key = "blog:user:active:" + userId + ":" + date;

        String value = redisService.get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }
}
```

---

## 八、最佳实践与注意事项

### 8.1 缓存最佳实践

| 最佳实践 | 说明 | 示例 |
|---------|------|------|
| **Key命名规范** | 使用冒号分隔，统一前缀 | `blog:article:info:123` |
| **过期时间设置** | 根据数据更新频率设置 | 热点数据30分钟，配置数据永久 |
| **避免大Key** | 单个Key不超过1MB | 拆分大数据、压缩存储 |
| **避免热Key** | 添加本地缓存、添加随机后缀 | 使用Caffeine + Redis |
| **缓存雪崩** | 随机过期时间 | baseExpire + random(0, 300) |
| **缓存穿透** | 布隆过滤器、空对象缓存 | BloomFilter + NULL缓存 |
| **缓存击穿** | 互斥锁、逻辑过期 | Redisson分布式锁 |
| **先更新DB后删缓存** | 保证数据一致性 | 先写DB，再删除缓存 |
| **批量操作** | 减少网络往返 | mget、pipeline |
| **监控命中率** | 及时发现缓存问题 | Micrometer统计 |

### 8.2 MQ最佳实践

| 最佳实践 | 说明 | 示例 |
|---------|------|------|
| **消息幂等** | 使用唯一业务ID | messageId + businessKey |
| **消息重试** | 设置最大重试次数 | maxReconsumeTimes=3 |
| **死信队列** | 处理失败消息 | %DLQ%consumerGroup |
| **消息顺序** | 同一业务发同一队列 | 相同queueId |
| **消息体大小** | 不超过4MB | 分批发送大对象 |
| **消费幂等** | Redis记录已消费 | setNx防重复 |
| **监控告警** | 积压、失败率告警 | 消费延迟>10000告警 |
| **批量消费** | 提高吞吐量 | @ConsumerThreadNumber=10 |
| **消息轨迹** | 启用trace | enableMsgTrace=true |
| **异步发送** | 提高性能 | asyncSend + callback |

### 8.3 常见问题与解决方案

#### 问题1：缓存和数据库不一致

**原因**：
- 先删缓存，更新DB失败
- 并发更新导致脏数据
- 缓存更新失败

**解决方案**：
```java
// 方案1：延时双删
public void updateArticle(Article article) {
    // 1. 先删除缓存
    cacheService.evictArticle(article.getId());

    // 2. 更新数据库
    articleMapper.updateById(article);

    // 3. 延迟删除缓存（500ms）
    Thread.sleep(500);
    cacheService.evictArticle(article.getId());
}

// 方案2：Canal监听binlog异步更新缓存
// 监听MySQL binlog，数据变更后自动刷新缓存
```

#### 问题2：消息丢失

**原因**：
- 生产者发送失败
- Broker宕机未持久化
- 消费者消费失败未重试

**解决方案**：
```java
// 1. 使用同步发送
SendResult result = rocketMQTemplate.syncSend(topic, message);

// 2. 开启Broker持久化
broker.properties = flushDiskType = SYNC_FLUSH

// 3. 消费者幂等+重试
maxReconsumeTimes = 3
```

#### 问题3：消息重复消费

**解决方案**：
```java
// 使用Redis实现幂等
String key = "mq:consumed:" + topic + ":" + messageId;
Boolean success = redisService.setNxEx(key, "1", 3600);
if (!success) {
    return;  // 已消费，跳过
}
```

---

*文档版本: v1.1*
*更新时间: 2025-01-11*
*新增内容：缓存高级细节、MQ高级细节、更多业务缓存、最佳实践*
