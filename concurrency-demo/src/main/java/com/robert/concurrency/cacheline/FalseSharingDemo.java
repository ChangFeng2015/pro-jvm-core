package com.robert.concurrency.cacheline;

/**
 * 
 * @author：李艳鹏
 * @since：Jun 11, 2017 1:01:29 AM
 * @version: 1.0
 */
public final class FalseSharingDemo {

    // 测试用的线程数
    private final static int NUM_THREADS = 4;

    // 测试的次数
    private final static int NUM_TEST_TIMES = 10;

    // 无填充、无缓存行对齐的对象类
    static class PlainHotVariable {

        public volatile long value = 0L;
    }

    // 有填充、有缓存行对齐的对象类
    static final class AlignHotVariable extends PlainHotVariable {

        public long p1, p2, p3, p4, p5, p6;
    }

    static final class CompetitorThread extends Thread {

        private final static long ITERATIONS = 500L * 1000L * 1000L;

        private PlainHotVariable plainHotVariable;

        public CompetitorThread(final PlainHotVariable plainHotVariable) {
            this.plainHotVariable = plainHotVariable;
        }

        @Override
        public void run() {
            // 一个线程对一个变量进行大量的存取操作
            for (int i = 0; i < ITERATIONS; i++) {
                plainHotVariable.value = i;
            }

        }

    }

    public static long runOneTest(PlainHotVariable[] plainHotVariables) throws Exception {
        // 开启多个线程进行测试
        CompetitorThread[] competitorThreads = new CompetitorThread[plainHotVariables.length];
        for (int i = 0; i < plainHotVariables.length; i++) {
            competitorThreads[i] = new CompetitorThread(plainHotVariables[i]);
        }

        final long start = System.nanoTime();
        for (Thread t : competitorThreads) {
            t.start();
        }

        for (Thread t : competitorThreads) {
            t.join();
        }

        // 统计每次测试使用的时间
        return System.nanoTime() - start;
    }

    public static boolean runOneCompare(int theadNum) throws Exception {
        PlainHotVariable[] plainHotVariables = new PlainHotVariable[theadNum];

        for (int i = 0; i < theadNum; i++) {
            plainHotVariables[i] = new PlainHotVariable();
        }

        // 进行无填充、无缓存行对齐的测试
        long t1 = runOneTest(plainHotVariables);

        AlignHotVariable[] alignHotVariable = new AlignHotVariable[theadNum];

        for (int i = 0; i < NUM_THREADS; i++) {
            alignHotVariable[i] = new AlignHotVariable();
        }

        // 进行有填充、有缓存行对齐的测试

        long t2 = runOneTest(alignHotVariable);

        System.out.println("Plain: " + t1);
        System.out.println("Align: " + t2);

        // 返回对比结果
        return t1 > t2;
    }

    public static void runOneSuit(int threadsNum, int testNum) throws Exception {
        int expectedCount = 0;
        for (int i = 0; i < testNum; i++) {
            if (runOneCompare(threadsNum))
                expectedCount++;
        }

        // 计算有填充、有缓存行对齐的测试场景下响应时间更短的情况的概率
        System.out.println("Radio (Plain > Align) : " + expectedCount * 100D / testNum + "%");
    }

    public static void main(String[] args) throws Exception {
        runOneSuit(NUM_THREADS, NUM_TEST_TIMES);
    }
}
