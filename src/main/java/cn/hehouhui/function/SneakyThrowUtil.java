package cn.hehouhui.function;

/**
 * 隐式抛异常
 *
 * @author HeHui
 * @date 2025-01-23 10:12
 */
final class SneakyThrowUtil {

    private SneakyThrowUtil() {}

    /**
     * 使用泛型和类型转换抛出异常，规避编译时类型检查
     * 此方法允许在泛型上下文中抛出任何类型的异常，而不会被Java的编译时类型检查机制捕获
     * 应谨慎使用此方法，因为它会绕过编译时的类型安全检查，可能导致运行时类型错误
     *
     * @param t 要抛出的异常对象
     * @param <T> 泛型参数，表示可能抛出的异常类型
     * @param <R> 泛型参数，表示此方法应返回的类型，但由于抛出异常，实际上不会返回任何值
     * @throws T 通过泛型参数指定的异常类型，实际上此异常会被转换后抛出
     */
    static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
        throw (T) t;
    }
}
