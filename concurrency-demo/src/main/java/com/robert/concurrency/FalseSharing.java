package com.robert.concurrency;
public final class FalseSharing
    implements Runnable
{
    public final static int NUM_THREADS = 4; // change
    public final static long ITERATIONS = 500L * 1000L * 1000L;
    private final int arrayIndex;
    
    private static boolean align = false;

    private static VolatileLong[] longs = new VolatileLong[NUM_THREADS];
    static
    {
        for (int i = 0; i < longs.length; i++)
        {
            longs[i] = new VolatileLong();
        }
    }

    private static AlignVolatileLong[] alignLongs = new AlignVolatileLong[NUM_THREADS];
    static
    {
        for (int i = 0; i < longs.length; i++)
        {
            alignLongs[i] = new AlignVolatileLong();
        }
    }

    public FalseSharing(final int arrayIndex)
    {
        this.arrayIndex = arrayIndex;
    }

    public static void main(final String[] args) throws Exception
    {
        int succCount = 0;
        for (int i = 0; i < 10; i ++) {
        final long start = System.nanoTime();
        align = false; 
        runTest();
        System.out.println("duration = " + (System.nanoTime() - start));
        long t1 = System.nanoTime() - start;
        
        final long start1 = System.nanoTime();
        align = true; 
        runTest();
        System.out.println("duration2 = " + (System.nanoTime() - start1));
        long t2 = System.nanoTime() - start1;
        
        if ( t1 > t2)
            succCount ++;
        }
        System.out.println( ((double) succCount) / 10d);

    }

    private static void runTest() throws InterruptedException
    {
        Thread[] threads = new Thread[NUM_THREADS];

        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new FalseSharing(i));
        }

        for (Thread t : threads)
        {
            t.start();
        }

        for (Thread t : threads)
        {
            t.join();
        }
    }

    public void run()
    {
        long i = ITERATIONS + 1;
        while (0 != --i)
        {
            if (!align)
            longs[arrayIndex].value = i;
            else 
             alignLongs[arrayIndex].value = i;
        }
    }

    public final static class VolatileLong
    {
        public volatile long value = 0L;
    }
    
    public final static class AlignVolatileLong
    {
        public volatile long value = 0L;
       public long p1, p2, p3, p4, p5, p6; 
    }

}