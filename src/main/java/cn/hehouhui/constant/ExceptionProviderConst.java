package cn.hehouhui.constant;

import cn.hehouhui.exception.CodeErrorException;
import cn.hehouhui.function.ExceptionProvider;

/**
 * 异常常量
 *
 * @author HeHui
 * @date 2024-11-28 13:35
 */
public final class ExceptionProviderConst {

    /**
     * RuntimeException
     */
    public static final ExceptionProvider RuntimeExceptionProvider = ((cause, msg) -> new RuntimeException(msg, cause));

    /**
     * IllegalArgumentException
     */
    public static final ExceptionProvider IllegalArgumentExceptionProvider =
        ((cause, msg) -> new IllegalArgumentException(msg, cause));

    /**
     * IllegalStateException
     */
    public static final ExceptionProvider IllegalStateExceptionProvider =
        ((cause, msg) -> new IllegalStateException(msg, cause));

    /**
     * UnsupportedOperationException
     */
    public static final ExceptionProvider UnsupportedOperationExceptionProvider =
        ((cause, msg) -> new UnsupportedOperationException(msg, cause));


    /**
     * 编码异常
     */
    public static final ExceptionProvider CodeErrorExceptionProvider =
        ((cause, msg) -> new CodeErrorException(msg, cause));
}
