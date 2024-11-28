package cn.hehouhui.util;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * 加锁工具
 *
 * @author HeHui
 * @date 2024-11-28 15:56
 */
public class LockTaskUtil {

    /**
     * 加锁运行指定任务，有返回值
     *
     * @param lock
     *            要加的锁
     * @param task
     *            任务
     * @param <T>
     *            结果类型
     * @return 结果
     */
    public static <T> T runWithLock(Lock lock, Supplier<T> task) {
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加锁运行指定任务，无返回值
     *
     * @param lock
     *            要加的锁
     * @param task
     *            任务
     */
    public static void runWithLock(Lock lock, Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加锁运行可被中断的任务
     *
     * @param lock
     *            锁
     * @param task
     *            可被中断的任务
     * @param <T>
     *            结果实际类型
     * @throws InterruptedException
     *             中断异常
     */
    public static <T> T runInterruptedTaskWithLock(Lock lock, Callable<T> task)
        throws Exception {
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

}
