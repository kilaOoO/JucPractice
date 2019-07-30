package simple;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author hbs
 * @Date 2019/7/30
 */
public class ThreadFactory {
    public static Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        return t;
    }
}
