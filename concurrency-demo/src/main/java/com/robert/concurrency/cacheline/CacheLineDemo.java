package com.robert.concurrency.cacheline;

/**
 * 
 * @author：李艳鹏
 * @since：Jun 11, 2017 1:01:29 AM
 * @version: 1.0
 */

public final class CacheLineDemo {

    // 缓存行的大小为64个字节，即为8个长整形
    private final static int CACHE_LINE_LONG_NUM = 8;

    // 用于测试的缓存行的数量
    private final static int LINE_NUM = 1024 * 1024;

    // 一次测试的次数
    private final static int NUM_TEST_TIMES = 10;

    // 构造能够填充LINE_NUM个缓存行的数组
    private static final long[] values = new long[CACHE_LINE_LONG_NUM * LINE_NUM];

    public static long runOneTestWithAlign() {

        final long start = System.nanoTime();

        // 进行顺序读取测试，期待在存取每个缓存行的第一个长整形变量的时候系统自动缓存整个缓存行，本行的后续存取都会命中缓存
        for (int i = 0; i < CACHE_LINE_LONG_NUM * LINE_NUM; i++)
            values[i] = i;

        return System.nanoTime() - start;

    }

    public static long runOneTestWithoutAlign() {
        final long start = System.nanoTime();

        // 按照缓存行的步长进行跳跃读取测试，期待每次读取一行中的一个元素，每次读取都不会命中缓存
        for (int i = 0; i < CACHE_LINE_LONG_NUM; i++)
            for (int j = 0; j < LINE_NUM; j++)
                values[j * CACHE_LINE_LONG_NUM + i] = i * j;

        return System.nanoTime() - start;
    }

    public static boolean runOneCompare() {
        long t1 = runOneTestWithAlign();
        long t2 = runOneTestWithoutAlign();

        System.out.println("Sequential: " + t1);
        System.out.println("      Leap: " + t2);

        return t1 < t2;
    }

    public static void runOneSuit(int testNum) throws Exception {
        int expectedCount = 0;
        for (int i = 0; i < testNum; i++) {
            if (runOneCompare())
                expectedCount++;
        }

        // 计算顺序访问数组的测试场景下，响应时间更短的情况的概率

        System.out.println("Radio (Sequential < Leap): " + expectedCount * 100D / testNum + "%");
    }

    public static void main(String[] args) throws Exception {
        runOneSuit(NUM_TEST_TIMES);
    }
}
