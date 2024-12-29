package cn.hehouhui.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象引用工具
 *
 * @author HeHui
 * @date 2024-11-28 13:45
 */
public class ReferenceUtil {

    /**
     * 销毁引用队列
     */
    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();

    /**
     * 回调MAP
     */
    private static final Map<Reference<?>, Runnable> CALLBACK_MAP = new ConcurrentHashMap<>();

    static {
        Thread thread = new Thread(() -> {
            Reference<?> lastRef = null;
            while (true) {
                try {
                    if (lastRef != null) {
                        Reference<?> ref = lastRef;
                        lastRef = null;

                        Runnable callback = CALLBACK_MAP.get(ref);
                        if (callback != null) {
                            try {
                                callback.run();
                            } catch (Throwable ignored) {
                            }
                        }
                    } else {
                        lastRef = QUEUE.remove(1000);
                    }
                } catch (Throwable ignored) {
                }
            }

        }, "对象销毁监听线程");
        thread.setDaemon(true);
        thread.setContextClassLoader(null);
        thread.start();
    }

    /**
     * 监听指定对象的销毁
     *
     * @param obj
     *            要监听的对象
     * @param callback
     *            对象销毁后的回调，注意，请勿在回调中直接或者间接引用监听的对象！！！另外需要注意内部会缓存callback直到callback执行完毕， 这个可能会导致外部无法正确回收callback的class对象；
     */
    public static <T> void listenDestroy(T obj, Runnable callback) {
        // 注意，ListenerWeakReference不能丢，不然就触发不了回调了
        Assert.argNotNull(callback, "callback");

        CALLBACK_MAP.put(new PhantomReference<>(obj, QUEUE), callback);
    }
}
