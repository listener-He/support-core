package cn.hehouhui.function;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * 分批查询
 *
 * @author HeHui
 * @date 2022-11-14 14:51
 */
public class BatchQuery<ID, T> {

    private int batch;

    private final AtomicReference<ID> idx;

    private final BiFunction<Integer, ID, List<T>> queryFunc;

    private final Function<T, ID> mapper;

    /**
     * 结果消费者
     */
    private Consumer<List<T>> resultConsumer;

    /** 异常消费者 */
    private Consumer<Throwable> throwableConsumer;

    /** 锁 */
    private final ReentrantLock lock;

    public BatchQuery(BiFunction<Integer, ID, List<T>> queryFunc, Function<T, ID> mapper) {
        this(1000, queryFunc, mapper);
    }


    public BatchQuery(int batch, BiFunction<Integer, ID, List<T>> queryFunc, Function<T, ID> mapper) {
        this.batch = batch;
        this.queryFunc = queryFunc;
        this.idx = new AtomicReference<>();
        this.mapper = mapper;
        this.lock = new ReentrantLock();
    }


    /**
     * 获取最后一个id
     *
     * @param rows 排
     *
     * @return {@link ID }
     */
    private ID getLastId(List<T> rows) {
        return mapper.apply(rows.get(rows.size() - 1));
    }


    /**
     * 结果消费者
     *
     * @param consumer 消费者
     *
     * @return {@link BatchQuery}<{@link T}>
     */
    public BatchQuery<ID, T> consumer(Consumer<List<T>> consumer) {
        return concurrent(() -> {
            this.resultConsumer = consumer;
            return this;
        });
    }


    /**
     * 异常消费者
     *
     * @param consumer 消费者
     *
     * @return {@link BatchQuery }<{@link ID },{@link T }>
     */
    public BatchQuery<ID, T> onError(Consumer<Throwable> consumer) {
        return concurrent(() -> {
            this.throwableConsumer = consumer;
            return this;
        });
    }


    /**
     * 获取查询结果 （无限查询直到查不到为止）
     *
     * @param first 起始ID
     *
     * @return {@link List }<{@link T }>
     */
    public List<T> get(ID first) {
        return get(first, c -> true);
    }


    /**
     * 获取查询结果
     *
     * @param first    起始ID
     * @param maxCount 最大查询条数
     *
     * @return {@link List }<{@link T }>
     */
    public List<T> get(ID first, int maxCount) {
        return get(first, count -> {
            // 判断当前计数是否达到最大限制数
            if (count >= maxCount) {
                // 如果达到或超过最大限制数，则停止查询
                return false;
            }
            // 动态调整批次大小，如果剩余需要查询的数量小于最大限制数，则将批次大小设置为剩余的数量
            if (count + batch < maxCount) {
                batch = maxCount - count;
            }
            // 如果未达到最大限制数，则继续查询
            return true;
        });
    }


    /**
     * 获取
     *
     * @param first          起始ID
     * @param countPredicate 最大总数的判断
     *
     * @return {@link List}<{@link T}>
     */
    public List<T> get(ID first, Predicate<Integer> countPredicate) {
        return concurrent(() -> {
            List<List<T>> list = new ArrayList<>();
            Consumer<List<T>> results = list::add;
            if (resultConsumer == null) {
                resultConsumer = results;
            } else {
                resultConsumer = resultConsumer.andThen(results);
            }
            run(first, countPredicate);
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            return list.stream().flatMap(List::stream).collect(Collectors.toList());
        });

    }


    /**
     * 无限制的查询
     *
     * @param first 第一个元素
     *
     * @return long
     */
    public long run(ID first) {
        return run(first, count -> true);
    }

    /**
     * 无限制的查询
     *
     * @param first 第一个元素
     * @param max   最大限制数
     *
     * @return long
     */
    public long run(ID first, int max) {
        return run(first, count -> {
            // 判断当前计数是否达到最大限制数
            if (count >= max) {
                // 如果达到或超过最大限制数，则停止查询
                return false;
            }
            // 动态调整批次大小，如果剩余需要查询的数量小于最大限制数，则将批次大小设置为剩余的数量
            if (count + batch < max) {
                batch = max - count;
            }
            // 如果未达到最大限制数，则继续查询
            return true;
        });
    }


    /**
     * 运行
     *
     * @param first     起始ID，用于开始查询
     * @param predicate 用于决定何时停止查询的条件
     */
    public long run(ID first, Predicate<Integer> predicate) {
        return concurrent(() -> {
            // 设置初始查询ID
            idx.set(first);
            // 初始化是否有下一个元素的标志为true
            boolean hasNext = true;
            // 初始化计数器
            int count = 0;
            // 循环进行查询操作
            do {
                try {
                    // 执行查询操作并获取结果列表
                    List<T> rows = this.queryFunc.apply(batch, idx.get());
                    boolean notEmpty = rows != null && !rows.isEmpty();
                    // 如果查询结果不为空
                    if (notEmpty) {
                        // 更新查询ID为结果列表中的最后一个ID
                        idx.set(getLastId(rows));
                        // 如果结果消费者不为空，则使用结果消费者处理结果
                        if (resultConsumer != null) {
                            resultConsumer.accept(rows);
                        }
                        // 累加计数器
                        count += rows.size();
                    }
                    // 如果查询结果为空或者不再满足继续查询的条件或者结果数量小于批量大小，则停止查询
                    if (!notEmpty || !predicate.test(count) || rows.size() < batch) {
                        hasNext = false;
                    }
                } catch (Throwable e) {
                    // 如果异常消费者不为空，则使用异常消费者处理异常
                    if (throwableConsumer != null) {
                        throwableConsumer.accept(e);
                    } else {
                        // 重新抛出异常
                        throw new RuntimeException(e);
                    }
                }
            } while (hasNext);

            return count;
        });

    }


    /**
     * 同时发生
     *
     * @param supplier 执行函数
     *
     * @return {@link R }
     */
    private <R> R concurrent(Supplier<R> supplier) {
        if (lock.tryLock()) {
            try {
                return supplier.get();
            } finally {
                lock.unlock();
            }
        }
        throw new RuntimeException("查询冲突");

    }


}
