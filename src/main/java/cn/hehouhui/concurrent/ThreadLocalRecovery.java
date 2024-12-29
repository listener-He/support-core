package cn.hehouhui.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 线程本地恢复 子实例尽量为静态常量 减少类的构建次数
 * 
 * @author HEHH
 * @date 2024/05/30
 */
public abstract class ThreadLocalRecovery<T> {

    // 所有实例
    private static final List<ThreadLocalRecovery<?>> instances = new ArrayList<>(64);

    public final ThreadLocal<T> threadLocal = new InheritableThreadLocal<>();

    public ThreadLocalRecovery() {
        instances.add(this);
    }

    /**
     * 重置
     */
    public static void reset() {
        instances.stream().filter(Objects::nonNull).forEach(ThreadLocalRecovery::clean);
    }

    /**
     * 清除线程变量
     */
    void clean() {
        threadLocal.remove();
    }

    /**
     * 被垃圾回收器 回收时调用
     *
     * @throws Throwable
     *             可丢弃
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            instances.remove(this);
        } catch (Exception ignored) {
        }

        super.finalize();
    }
}
