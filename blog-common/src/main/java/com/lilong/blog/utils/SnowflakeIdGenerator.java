package com.lilong.blog.utils;

public class SnowflakeIdGenerator {

    // 起始时间戳（2023-01-01 00:00:00）
    private final static long START_STAMP = 1672531200000L;

    // 各部分位数分配
    private final static long SEQUENCE_BIT = 12;  // 序列号位数
    private final static long WORKER_BIT = 10;    // 工作节点位数

    // 最大值计算（位运算优化）
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private final static long MAX_WORKER_ID = ~(-1L << WORKER_BIT);

    // 移位偏移量
    private final static long WORKER_LEFT = SEQUENCE_BIT;
    private final static long TIMESTAMP_LEFT = WORKER_LEFT + WORKER_BIT;

    private final long workerId;     // 工作节点ID
    private long sequence = 0L;     // 序列号
    private long lastStamp = -1L;   // 上次生成时间

    public SnowflakeIdGenerator(long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID超出范围 [0, " + MAX_WORKER_ID + "]");
        }
        this.workerId = workerId;
    }

    /**
     * 生成下一个ID（线程安全）
     */
    public synchronized long nextId() {
        long currentStamp = getCurrentStamp();

        // 时钟回拨检查
        if (currentStamp < lastStamp) {
            throw new RuntimeException("时钟回拨异常，拒绝生成ID。回拨时间：" + (lastStamp - currentStamp) + "ms");
        }

        if (currentStamp == lastStamp) {
            // 同一毫秒内序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) { // 当前毫秒序列号用尽
                currentStamp = waitNextMillis(lastStamp);
            }
        } else {
            sequence = 0L; // 新毫秒重置序列号
        }

        lastStamp = currentStamp;

        // 组合各部分生成最终ID
        return (currentStamp - START_STAMP) << TIMESTAMP_LEFT
                | workerId << WORKER_LEFT
                | sequence;
    }

    /**
     * 阻塞到下一毫秒
     */
    private long waitNextMillis(long lastStamp) {
        long current = getCurrentStamp();
        while (current <= lastStamp) {
            current = getCurrentStamp();
        }
        return current;
    }

    /**
     * 获取当前毫秒数
     */
    private long getCurrentStamp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        // 创建发号器实例（实际项目中workerId应从配置中心获取）
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1);

        // 连续生成10个ID
        for (int i = 0; i < 10; i++) {
            System.out.println(idGenerator.nextId());
        }
    }
}