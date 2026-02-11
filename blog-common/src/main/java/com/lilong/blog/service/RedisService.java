package com.lilong.blog.service;

import com.alibaba.nacos.common.utils.MapUtil;
import com.lilong.blog.exception.BusinessException;
import com.lilong.blog.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis 工具类
 */
@Slf4j
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 发布消息
     *
     * @param channel
     * @param message
     */
    public void sendMessage(String channel, String message) {

//        redisTemplate.convertAndSend(channel, message);
    }

    /**
     * 转换字符串
     *
     * @param body
     * @return
     */
    public String convertStringSerializer(byte[] body) {
        return redisTemplate.getStringSerializer().deserialize(body);
    }

    /**
     * 执行LUA脚本
     *
     * @param script
     * @param keys
     * @param args
     * @param <T>
     * @return
     */
    public <T> T executeScript(RedisScript<T> script, List<String> keys, Object... args) {
        try {
            return redisTemplate.execute(script, keys, args);
        } catch (Exception e) {
            log.error("redis executeScript error! script:{}, keys:{}, args:{}",
                    script.getScriptAsString(), keys, args, e);
        }
        return null;
    }

    /**
     * get取值
     *
     * @param key
     * @return
     */
    public String get(final String key) {
        String result = null;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            result = operations.get(key);
        } catch (Exception e) {
            log.error("redis get error! key:{}", key, e);
        }
        return result;
    }

    /***
     * 批量Get
     * @param keys
     *
     * [1,2,3]
     * [u1,null,u3]
     *
     * @return
     */
    public List<String> mget(final List<String> keys) {
        List<String> result = new ArrayList<>();
        try {
            ValueOperations<String, String> operations = this.redisTemplate.opsForValue();
            return operations.multiGet(keys);
        } catch (Exception e) {
            log.error("redis get error! key:{}", JsonUtil.toJsonString(keys), e);
        }
        return result;
    }

    public List<Object> mget(final String key, final Collection<Object> hashKeys) {
        List<Object> result = new ArrayList<>();
        try {
            return redisTemplate.opsForHash().multiGet(key, hashKeys);
        } catch (Exception e) {
            log.error("redis get error! key:{}", JsonUtil.toJsonString(hashKeys), e);
        }
        return result;
    }


    public Map<String, String> mgetAndParseMap(final List<String> keys) {
        Map<String, String> resultMap = new HashMap<>(keys.size());
        try {
            List<String> result = this.mget(keys);
            for (int i = 0; i < keys.size(); i++) {
                resultMap.put(keys.get(i), result.get(i));
            }
            return resultMap;
        } catch (Exception e) {
            log.error("redis get error! key:{}", JsonUtil.toJsonString(keys), e);
        }
        return resultMap;
    }

    /***
     * 获取定时任务的锁, liveTime毫秒
     * @param key
     * @param requestId
     * @param liveTime
     * @return
     */
    public boolean installLockForMS(String key, String requestId, long liveTime) {
        if (org.springframework.util.StringUtils.isEmpty(key) || org.springframework.util.StringUtils.isEmpty(requestId)) {
            return false;
        }
        return setNxPx(key, requestId, liveTime);
    }

    /***
     * @param key
     * @param value
     * @param exptime
     * @return
     *
     * 1 ---> setnx(key, none , 2000) return true;
     * 2 ---> setnx return false;
     */

    public boolean setNxPx(final String key, final String value, final long exptime) {
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            Object obj = connection.execute("set", keySerializer.serialize(key),
                    valueSerializer.serialize(value),
                    SafeEncoder.encode("NX"),
                    SafeEncoder.encode("PX"),
                    Protocol.toByteArray(exptime));
            return obj != null && "OK".equals(String.valueOf(obj));
        });
        return result;
    }

    /**
     * @param key
     * @param value
     * @param exptime
     * @return
     */
    public boolean setNxEx(final String key, final String value, final long exptime) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return false;
        }
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            Object obj = connection.execute("set", keySerializer.serialize(key),
                    valueSerializer.serialize(value),
                    SafeEncoder.encode("NX"),
                    SafeEncoder.encode("EX"),
                    Protocol.toByteArray(exptime));
            return ObjectUtils.equals("OK", obj);
        });
        return result;
    }

    /**
     * 带有过期时间的set传值
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, final String value, long expireTime, TimeUnit timeUnit) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            if (expireTime > 0) {
                operations.set(key, value, expireTime, timeUnit);
            } else {
                operations.set(key, value);
            }
            result = true;
        } catch (Exception e) {
            log.error("redis set error! key:{}, value:{}", key, value, e);
        }
        return result;
    }

    public boolean set(final String key, final String value, long expireTime) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            if (expireTime > 0) {
                operations.set(key, value, expireTime, TimeUnit.SECONDS);
            } else {
                operations.set(key, value);
            }
            result = true;
        } catch (Exception e) {
            log.error("redis set error! key:{}, value:{}", key, value, e);
        }
        return result;
    }

    /**
     * set传值
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, String value) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            log.error("redis set error! key:{}, value:{}", key, value);
        }
        return result;
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public boolean sadd(String key, String... value) {
        boolean result = false;
        try {
            SetOperations<String, String> set = redisTemplate.opsForSet();
            set.add(key, value);
            result = true;
        } catch (Exception e) {
            log.error("redis set error! key:{}, value:{}", key, value, e);
        }
        return result;
    }

    /**
     * @param key
     * @param setValue
     */
    public void sadd(String key, Set<String> setValue) {
        try {
            SetOperations<String, String> set = redisTemplate.opsForSet();
            set.add(key, setValue.toArray(new String[0]));
        } catch (Exception e) {
            log.error("sadd error! key:{}", key, e);
        }
    }

    /**
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        try {
            SetOperations<String, String> set = redisTemplate.opsForSet();
            return set.members(key);
        } catch (Exception e) {
            log.error("redis set error! key:{}", key, e);
            return Collections.emptySet();
        }
    }

    public Long ssize(String key) {
        Long size = null;
        try {
            size = redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("redis ssize error! key:{}", key, e);
        }
        return size;
    }

    /**
     * @param key
     * @param values
     * @return
     */
    public Long sremove(String key, Object... values) {
        try {
            SetOperations<String, String> set = redisTemplate.opsForSet();
            return set.remove(key, values);
        } catch (Exception e) {
            log.error("redis set error! key:{}", key, e);
            return null;
        }
    }

    /**
     * 元素是否在set集合中
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean sIsMember(String key, String value) {
        try {
            SetOperations<String, String> set = redisTemplate.opsForSet();
            return set.isMember(key, value);
        } catch (Exception ex) {
            log.error("redis sIsMember error! key:{}", key, ex);
            return false;
        }
    }

    /**
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean zadd(String key, String value, long score) {
        boolean result = false;
        try {
            ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
            zset.add(key, value, score);
            result = true;
        } catch (Exception e) {
            log.error("redis set error! key:{}, value:{}", key, value, e);
        }
        return result;
    }

    /***
     * 判断zset中是否包含元素
     * @param key
     * @param value
     * @return
     */
    public boolean zContains(String key, String value) {
        try {
            ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
            Long rank = zset.rank(key, value);
            return rank != null && rank >= 0;
        } catch (Exception e) {
            log.error("redis zContains error! key:{}, value:{}", key, value, e);
        }
        return false;
    }

    public Map<String, Boolean> zContains(String key, Set<String> values) {
        if (StringUtils.isBlank(key) || CollectionUtils.isEmpty(values)) {
            return Collections.emptyMap();
        }
        Map<String, Boolean> containMap = new HashMap<>();
        values.forEach(val -> {
            containMap.put(val, this.zContains(key, val));
        });
        return containMap;
    }


    /**
     * @param key
     * @param value
     * @param score
     * @param liveTime
     * @return
     */
    public boolean zadd(String key, String value, long score, long liveTime) {
        boolean result = false;
        try {
            ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
            zset.add(key, value, score);
            if (liveTime > 0) {
                expire(key, liveTime);
            }
            result = true;
        } catch (Exception e) {
            log.error("redis set error! key:{}, value:{}", key, value, e);
        }
        return result;
    }

    /**
     * @param key
     * @param tuples
     * @param liveTime
     * @return
     */
    public boolean zadd(String key, Set<ZSetOperations.TypedTuple<String>> tuples, long liveTime) {
        boolean result = false;
        try {
            ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
            zset.add(key, tuples);
            if (liveTime > 0) {
                expire(key, liveTime);
            }
            result = true;
        } catch (Exception e) {
            log.error("redis set error! key:{}, value:{}", key, tuples, e);
        }
        return result;
    }

    public boolean zIsExist(String key, String value) {
        try {
            ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
            return zset.score(key, value) != null;
        } catch (Exception e) {
            log.error("redis zIsExist error! key:{}, value:{}", key, value, e);
        }
        return false;
    }

    public List<Double> batchZIsExist(String key, String... values) {
        try {
            ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
            return zset.score(key, values);
        } catch (Exception e) {
            log.error("redis zIsExist error! key:{}, values:{}", key, values, e);
        }
        return Collections.emptyList();
    }

    /**
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrange(String key, long start, long end) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        try {
            Set<String> result = zset.range(key, start, end - 1L);
            return result;
        } catch (Exception e) {
            log.error("redis zrange error,key:{}", key, e);
        }
        return null;
    }

    /***
     * 获取Score
     * @param key
     * @param value
     * @return
     */
    public Long zscore(String key, String value) {
        Double score = redisTemplate.opsForZSet().score(key, value);
        if (score == null) {
            return 0L;
        }
        return score.longValue();
    }

    /**
     * 批量查询zset score评分
     *
     * @param key
     * @param values
     * @return
     */
    public List<Long> zscore(String key, List<String> values) {
        if (StringUtils.isBlank(key) || CollectionUtils.isEmpty(values)) {
            return new ArrayList<>();
        }
        List<Double> scores = redisTemplate.opsForZSet().score(key, values.toArray(new String[values.size()]));
        if (CollectionUtils.isEmpty(scores)) {
            return new ArrayList<>();
        }
        return scores.stream().map(Double::longValue).collect(Collectors.toList());
    }

    /**
     * 批量查询redis zset value对应score评分
     *
     * @param key
     * @param values
     * @return
     */
    public Map<String, Long> zscoreTomap(String key, List<String> values) {
        List<Long> scores = this.zscore(key, values);
        if (CollectionUtils.isEmpty(scores)) {
            return new HashMap<>();
        }
        Map<String, Long> scoreMap = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            scoreMap.put(values.get(i), scores.get(i));
        }
        return scoreMap;
    }

    /**
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zreverseRange(String key, long start, long end) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        try {
            Set<String> result = zset.reverseRange(key, start, end - 1L);
            return result;
        } catch (Exception e) {
            log.error("redis zrange error,errorMessage:{}", e.getMessage());
        }
        return new HashSet<>();
    }

    /**
     * 获取ZSet分数区间中前几位的元素
     *
     * @param key
     * @param maxScore
     * @param offset
     * @param count
     * @return
     */
    public Set<String> zreverseRangeByScore(String key, double maxScore, long offset, long count) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        try {
            Set<String> result = zset.reverseRangeByScore(key, 1 - Double.MAX_VALUE, maxScore, offset, count);
            return result;
        } catch (Exception e) {
            log.error("redis zrangeByScoreWithScores error,errorMessage:{}",
                    e.getMessage());
        }
        return null;
    }

    /**
     * 获取ZSet分数区间中前几位的元素
     *
     * @param key
     * @param minScore
     * @param maxScore
     * @param offset
     * @param count
     * @return
     */
    public Set<String> zreverseRangeByScore(String key, double minScore, double maxScore, long offset, long count) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        try {
            Set<String> result = zset.reverseRangeByScore(key, minScore, maxScore, offset, count);
            return result;
        } catch (Exception e) {
            log.error("redis zrangeByScoreWithScores error,errorMessage:{}",
                    e.getMessage());
        }
        return null;
    }

    /**
     * 获取ZSet中所有元素总数
     *
     * @param key
     */
    public long zSetSize(String key) {
        if (StringUtils.isEmpty(key)) {
            return 0;
        }
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception ex) {
            log.error("zSetSize error", ex);
            return 0;
        }
    }

    /**
     * 获取ZSet中所有分数区间元素总数
     *
     * @param key
     */
    public long zSetCount(String key, double minScore, double maxScore) {
        if (StringUtils.isEmpty(key)) {
            return 0;
        }
        try {
            return redisTemplate.opsForZSet().count(key, minScore, maxScore);
        } catch (Exception ex) {
            log.error("zSetCount error", ex);
            return 0;
        }
    }

    /**
     * 校验redis sorted set下是否存在某个score
     *
     * @param key
     * @param score
     * @return
     */
    public boolean zExistScore(String key, Long score) {

        return zSetCount(key, score, score) > 0;
    }

    /**
     * 获取ZSet中所有元素
     *
     * @param key
     */
    public Set<String> zSetGetAll(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        } catch (Exception ex) {
            log.error("zSetGetAll error", ex);
            return null;
        }
    }


    /**
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrangeByScore(String key, long start, long end) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        try {
            Set<String> result = zset.rangeByScore(key, start, end);
            return result;
        } catch (Exception e) {
            log.error("redis zrangeByScore error,errorMessage:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 按照分数范围删除
     *
     * @param key
     * @param start
     * @param end
     */
    public void zremoveRangeByScore(String key, long start, long end) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        try {
            zset.removeRangeByScore(key, start, end);
        } catch (Exception e) {
            log.error("redis zremoveRangeByScore error,errorMessage:{}", e.getMessage());
        }
    }

    /**
     * 删除ZSet中某一元素
     *
     * @param key
     * @param value
     */
    public void zremove(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        try {
            redisTemplate.opsForZSet().remove(key, value);
        } catch (Exception ex) {
            log.error("zSetAdd error", ex);
        }
    }

    /**
     * Zset取交集
     * {key} 与 {otherKey} 交集赋值给 {destKey}
     */
    public void zintersectAndStore(String key, String otherKey, String destKey) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(otherKey) || StringUtils.isEmpty(destKey)) {
            return;
        }
        try {
            redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
        } catch (Exception ex) {
            log.error("zSetAdd error", ex);
        }
    }

    /**
     * Zset取交集(使用lua的方式)
     * {key} 与 {otherKey} 交集赋值给 {destKey}，分数取最小的值
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    public Long zinterstoreMin(String key, String otherKey, String destKey) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(otherKey) || StringUtils.isEmpty(destKey)) {
            return null;
        }
        List<String> keys = Arrays.asList(destKey, key, otherKey);
        String script = " return redis.call('zinterstore', KEYS[1], '2', KEYS[2], KEYS[3], 'aggregate', 'min') ";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        try {
            Object res = redisTemplate.execute(redisScript, keys);
            return (Long) res;
        } catch (Exception ex) {
            log.error("zinterstoreMin error, key:{}, otherKey:{}, destKey:{}", key, otherKey, destKey, ex);
        }
        return null;
    }

    /**
     * zset复制数据
     * 将{key}的数据复制到{destKey}上
     *
     * @param key
     * @param destKey
     * @return
     */
    public Long zunionstore(String key, String destKey) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(destKey)) {
            return null;
        }
        List<String> keys = Arrays.asList(destKey, key);
        String script = " return redis.call('zunionstore', KEYS[1], '1', KEYS[2]) ";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        try {
            Object res = redisTemplate.execute(redisScript, keys);
            return (Long) res;
        } catch (Exception ex) {
            log.error("zunionstore error, key:{}, destKey:{}", key, destKey, ex);
        }
        return null;
    }


    /**
     * @param key
     * @return
     */
    public Long zsize(String key) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        try {
            Long result = zset.size(key);
            return result;
        } catch (Exception e) {
            log.error("redis zrange error,errorMessage:{}", e.getMessage());
        }
        return null;
    }

    /**
     * hset
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public boolean hset(String key, String field, String value, long... expire) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field) || StringUtils.isEmpty(value)) {
            return false;
        }
        boolean result = false;
        try {
            final byte[] rawKey = redisTemplate.getStringSerializer().serialize(key);
            final byte[] rawField = redisTemplate.getStringSerializer().serialize(field);
            final byte[] rawValue = redisTemplate.getStringSerializer().serialize(value);
            result = redisTemplate.execute(connection -> {
                boolean ret = connection.hSet(rawKey, rawField, rawValue);
                if (expire.length > 0 && expire[0] > 0) {
                    connection.expire(rawKey, expire[0]);
                }
                return ret;
            }, true);
        } catch (Exception ex) {
            log.error("hset error", ex);
        }
        return result;
    }

    public boolean hsetAll(String key, Map<String, String> hash, long... expire) {
        try {
            redisTemplate.opsForHash().putAll(key, hash);
            if (expire.length > 0 && expire[0] > 0) {
                redisTemplate.expire(key, expire[0], TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception ex) {
            log.error("hsetAll error ===> key:{}, hash:{}", key, JsonUtil.toJsonString(hash), ex);
            return false;
        }
    }

    /**
     * hget
     *
     * @param key
     * @param field
     * @return
     */
    public String hget(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        final byte[] rawKey = redisTemplate.getStringSerializer().serialize(key);
        final byte[] rawField = redisTemplate.getStringSerializer().serialize(field);
        final byte[] rawValue = redisTemplate.execute(connection -> connection.hGet(rawKey, rawField), true);
        return redisTemplate.getStringSerializer().deserialize(rawValue);
    }


    public boolean hexists(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return false;
        }
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception ex) {
            log.error("hexists error, key:{}, field:{}", key, field, ex);
        }
        return false;
    }

    public boolean hexists(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception ex) {
            log.error("hexists error, key:{}", key, ex);
        }
        return false;
    }

    /**
     * @param key
     * @param field
     * @return
     */
    public Long hdel(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        long result = redisTemplate.opsForHash().delete(key, field);
        return result;
    }

    public Long hmdel(String key, List<String> fields) {
        if (StringUtils.isEmpty(key) || CollectionUtils.isEmpty(fields)) {
            return null;
        }
        Object[] filteredFields = fields.stream()
                .filter(Objects::nonNull)
                .toArray();
        long result = redisTemplate.opsForHash().delete(key, filteredFields);
        return result;
    }

    /**
     * @param key
     * @param queryFields
     * @return
     */
    public List<String> hmget(String key, List<String> queryFields) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.multiGet(key, queryFields);
    }

    /**
     * @param key
     * @param map
     * @return
     */
    public void hmSet(String key, Map<Long, Long> map) {
        if (StringUtils.isEmpty(key) || MapUtil.isEmpty(map)) {
            return;
        }
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * @param key
     * @param map
     */
    public void hmSet(String key, Map<String, String> map, long... expire) {
        if (StringUtils.isEmpty(key) || MapUtil.isEmpty(map)) {
            return;
        }
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * @param key
     * @param field
     * @return
     */
    public Long hincrex(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        Long result = redisTemplate.opsForHash().increment(key, field, 1);
        return result;
    }

    public Long hIncrementVal(String key, String field, long val) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        Long result = redisTemplate.opsForHash().increment(key, field, val);
        return result;
    }

    /**
     * @param key
     * @param field
     * @return
     */
    public Long hdecrex(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            return null;
        }
        Long result = redisTemplate.opsForHash().increment(key, field, -1);
        return result;
    }

    public void lleftPush(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        redisTemplate.opsForList().leftPush(key, value);
    }

    public void lleftPushAll(String key, Collection values) {
        if (StringUtils.isEmpty(key) || CollectionUtils.isEmpty(values)) {
            return;
        }
        redisTemplate.opsForList().leftPushAll(key, values);
    }

    public void lrightPush(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        redisTemplate.opsForList().rightPush(key, value);
    }

    public void lrightPushAll(String key, List<String> valueList) {
        if (StringUtils.isEmpty(key) || CollectionUtils.isEmpty(valueList)) {
            return;
        }
        redisTemplate.opsForList().rightPushAll(key, valueList);
    }

    public void lleftPushAll(String key, List<String> valueList) {
        if (StringUtils.isEmpty(key) || CollectionUtils.isEmpty(valueList)) {
            return;
        }
        redisTemplate.opsForList().leftPushAll(key, valueList);
    }

    public Long lsize(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForList().size(key);
    }

    /**
     * @param key
     * @param start
     * @param end
     */
    public List<String> lrange(String key, long start, long end) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * @param key
     * @return
     */
    public String lleftPop(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForList().leftPop(key);
    }

    public String lrightPop(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * @param key
     * @param time
     * @param timeUnit
     * @return
     */
    public String lleftPop(String key, long time, TimeUnit timeUnit) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForList().leftPop(key, time, timeUnit);
        } catch (Exception e) {
            log.error("redis lleftPop error! key:{}", key, e);
        }
        return null;
    }

    /**
     * @param key
     * @param time
     * @param timeUnit
     * @return
     */
    public String lrightPop(String key, long time, TimeUnit timeUnit) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            return redisTemplate.opsForList().rightPop(key, time, timeUnit);
        } catch (Exception e) {
            log.error("redis lrightPop error! key:{}", key, e);
        }
        return null;
    }

    /**
     * 带失效时间的值原子自增
     *
     * @param key
     * @param sec
     * @return
     */
    public Long increx(String key, long sec) {
        Long result = null;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            result = operations.increment(key, 1);
            // 第一次设置，设置有效时间
            if (result == 1 && sec > 0) {
                expire(key, sec);
            }
        } catch (Exception e) {
            log.error("redis get error! key:{}", key, e);
        }
        return result;
    }

    /**
     * 带失效时间的值原子自减
     *
     * @param key
     * @param sec
     * @return
     */
    public Long decrex(String key, long sec) {
        Long result = null;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            result = operations.increment(key, -1);
            // 第一次设置，设置有效时间
            if (result == -1 && sec > 0) {
                expire(key, sec);
            }
        } catch (Exception e) {
            log.error("redis get error! key:{}", key, e);
        }
        return result;
    }

    /**
     * 获取定时任务的锁（非集群方式）
     *
     * @param key       键值
     * @param requestId 锁ID，通过此来判断是哪个实例的锁
     * @param liveTime  过期时间
     * @return
     */
    public boolean installDistributedLock(String key, String requestId, long liveTime) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(requestId)) {
            return false;
        }
        return setNxEx(key, requestId, liveTime);
    }

    /**
     * 释放定时任务的锁
     *
     * @param key       键值
     * @param requestId 锁ID
     */
    public void releaseDistributedLock(String key, String requestId) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(requestId)) {
            return;
        }
        try {
            String redisId = redisTemplate.opsForValue().get(key);
            // 保证只解自己拿到的锁
            if (requestId.equals(redisId)) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("release distributed lock error,requestId:{},errorMessage:{}",
                    requestId, e.getMessage());
            throw new BusinessException("release distributed lock error");
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        redisTemplate.delete(key);
    }

    public void removeAll(final Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public Boolean exists(final String key) {

        return redisTemplate.hasKey(key);
    }

    public Boolean existsHash(final String key, final String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("redis existsHash error! key:{}, field:{}", key, field, e);
        }
        return false;
    }

    /**
     * 设置过期时间
     *
     * @param key
     * @param livetime
     */
    public void expire(String key, long livetime) {
        redisTemplate.expire(key, livetime, TimeUnit.SECONDS);
    }

    /***
     * 求两个set的交集
     */
    public Set<String> intersect(String key1, String key2) {
        try {
            SetOperations<String, String> set = redisTemplate.opsForSet();
            return set.intersect(key1, key2);
        } catch (Exception e) {
            log.error("redis intersect error! key1:{}, key2:{}", key1, key2, e);
            return Collections.emptySet();
        }
    }

    /***
     * 求两个set的并集
     */
    public Set<String> union(String key1, String key2) {
        try {
            SetOperations<String, String> set = redisTemplate.opsForSet();
            return set.union(key1, key2);
        } catch (Exception e) {
            log.error("redis union error! key1:{}, key2:{}", key1, key2, e);
            return Collections.emptySet();
        }
    }

    public Long size(String key) {
        SetOperations<String, String> set = redisTemplate.opsForSet();
        try {
            Long result = set.size(key);
            return result;
        } catch (Exception e) {
            log.error("redis size error, key:{}, errorMessage:{}", key, e.getMessage());
        }
        return null;
    }

    /***
     * 查询剩余存活时间
     * @param key
     * @return
     */
    public Long getKeysExpireTime(String key) {
        Set<String> keys = redisTemplate.keys(key);
        return redisTemplate.opsForValue().getOperations().getExpire(key);
    }

    /***
     * 根据前缀查询所有key的剩余存活时间
     * @return
     */
    public Map<String, Long> batchGetKeysExpireTime(String keyPrefix) {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        Map<String, Long> keyExpireMap = new HashMap<>();
        for (String key : keys) {
            Long expireTime = redisTemplate.opsForValue().getOperations().getExpire(key);
            keyExpireMap.put(key, expireTime);
        }
        return keyExpireMap;
    }

    /**
     * @param start
     * @param end
     * @return
     */
    public Map<Long, Set<DefaultTypedTuple>> zreverseRangeWithScoreByPipelined(List<String> keys,
                                                                               List<Long> indexs,
                                                                               long start, long end) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>();
        }
        List<Object> redisResult = redisTemplate.executePipelined((RedisCallback<List<Object>>) connection -> {
            for (String key : keys) {
                byte[] rewKey = redisTemplate.getStringSerializer().serialize(key);
                connection.zRevRangeWithScores(rewKey, start, end - 1);
            }
            return null;
        });
        Map<Long, Set<DefaultTypedTuple>> resultMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(redisResult)) {
            for (int i = 0; i < indexs.size(); i++) {
                Set<DefaultTypedTuple> result = (Set<DefaultTypedTuple>) redisResult.get(i);
                resultMap.put(indexs.get(i), result);
            }
        }
        return resultMap;
    }

    /**
     * 评分倒序根据游标批量查询zset中value
     *
     * @param keys
     * @param indexs
     * @param start
     * @param end
     * @return
     */
    public Map<String, Set<String>> zreverseRangeByPipelined(List<String> keys,
                                                             List<String> indexs,
                                                             long start, long end) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>();
        }
        List<Object> redisResult = redisTemplate.executePipelined((RedisCallback<List<Object>>) connection -> {
            for (String key : keys) {
                byte[] rewKey = redisTemplate.getStringSerializer().serialize(key);
                connection.zRevRange(rewKey, start, end);
            }
            return null;
        });
        Map<String, Set<String>> resultMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(redisResult)) {
            for (int i = 0; i < indexs.size(); i++) {
                Object res;
                if ((res = redisResult.get(i)) != null) {
                    resultMap.put(indexs.get(i), (Set<String>) res);
                } else {
                    resultMap.put(indexs.get(i), Collections.emptySet());
                }
            }
        }
        return resultMap;
    }

    /**
     * 获取string序列化器
     *
     * @return
     */
    public RedisSerializer getStringSerializer() {
        return redisTemplate.getStringSerializer();
    }

    public RedisSerializer getValueSerializer() {
        return redisTemplate.getValueSerializer();
    }
}