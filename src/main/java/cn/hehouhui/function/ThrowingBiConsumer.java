package cn.hehouhui.function;


import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * 可检测异常消费者
 *
 * @author HeHui
 * @date 2025-01-23 10:10
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T1, T2, EX extends Exception> {

    /**
     * 接受两个参数的函数
     * <p>
     * 该函数的设计目的是为了处理或接受两种不同类型的参数，进行特定的操作或逻辑处理
     * 它允许异常抛出，这可能是因为在执行过程中可能会遇到一些异常情况，需要调用者进行处理
     *
     * @param t  第一个参数，类型为T1，代表该函数需要处理的第一个对象或数据
     * @param t2 第二个参数，类型为T2，代表该函数需要处理的第二个对象或数据
     *
     * @throws EX 表明该函数可能会抛出类型为EX的异常，调用者应该注意处理这些异常情况
     */
    void accept(T1 t, T2 t2) throws EX;


    /**
     * 将一个可能抛出异常的二元消费者转换为不会抛出异常的二元消费者.
     * 此方法的目的是简化在使用Java 8函数式接口时的异常处理,通过忽略或转换异常,
     * 从而允许在lambda表达式中使用可能抛出受检异常的方法.
     *
     * @param consumer 一个可能抛出异常的二元消费者函数.
     * @param <T>      第一个参数的类型.
     * @param <U>      第二个参数的类型.
     *
     * @return 一个不会抛出异常的二元消费者, 它会在内部处理异常.
     */
    static <T, U> BiConsumer<T, U> unchecked(ThrowingBiConsumer<? super T, ? super U, ?> consumer) {
        requireNonNull(consumer);
        return (t, u) -> {
            try {
                consumer.accept(t, u);
            } catch (Exception e) {
                // 异常处理逻辑,例如日志记录或转换为运行时异常
                // 这里选择将异常转换为运行时异常,由调用者决定是否处理
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 创建一个BiConsumer，它可以在消费两个参数时抛出任何类型的异常
     * 这个方法的目的是包装一个可能抛出异常的二元消费者，使其能够适应不支持抛出异常的场景
     *
     * @param <T>      第一个参数的类型
     * @param <U>      第二个参数的类型
     * @param consumer 一个可能抛出异常的二元消费者
     *
     * @return 一个可以悄无声息地处理异常的BiConsumer
     */
    static <T, U> BiConsumer<T, U> sneaky(ThrowingBiConsumer<? super T, ? super U, ?> consumer) {
        requireNonNull(consumer);
        return (arg1, arg2) -> {
            try {
                consumer.accept(arg1, arg2);
            } catch (final Exception e) {
                // 在这里悄无声息地处理异常
                SneakyThrowUtil.sneakyThrow(e);
            }
        };
    }
}
