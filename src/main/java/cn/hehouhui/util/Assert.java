package cn.hehouhui.util;

import cn.hehouhui.constant.ExceptionProviderConst;
import cn.hehouhui.function.ExceptionProvider;

import java.util.function.Supplier;

/**
 * 断言
 *
 * @author HeHui
 * @date 2024-11-28 13:24
 */
public class Assert {

    private Assert() {
        throw new AssertionError();
    }

    /**
     * 断言参数不能为空
     *
     * @param arg 参数
     */
    public static void argNotNull(Object arg) {
        if (arg == null) {
            throw new IllegalArgumentException("must not be null");
        }
    }

    /**
     * 断言参数不能为空
     *
     * @param arg     参数
     * @param argName 参数名
     */
    public static void argNotNull(Object arg, String argName) {
        if (arg == null) {
            throw new IllegalArgumentException(String.format("%s must not be null", argName));
        }
    }

    /**
     * 断言参数不能为空
     *
     * @param arg      参数
     * @param provider 异常提供器
     */
    public static void notNull(Object arg, ExceptionProvider provider) {
        notNull(arg, (String) null, provider);
    }

    /**
     * 断言参数不能为空
     *
     * @param arg 参数
     * @param msg 异常消息
     */
    public static void notNull(Object arg, String msg) {
        notNull(arg, msg, ExceptionProviderConst.IllegalArgumentExceptionProvider);
    }

    /**
     * 断言参数不能为空
     *
     * @param arg      参数
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void notNull(Object arg, String msg, ExceptionProvider provider) {
        if (arg == null) {
            throw provider.newRuntimeException(msg);
        }
    }

    /**
     * 断言参数不能为空
     *
     * @param arg      参数
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void notNull(Object arg, Supplier<String> msg, ExceptionProvider provider) {
        if (arg == null) {
            throw provider.newRuntimeException(msg.get());
        }
    }

    /**
     * 断言参数为空
     *
     * @param arg      参数
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void isNull(Object arg, String msg, ExceptionProvider provider) {
        if (arg != null) {
            throw provider.newRuntimeException(msg);
        }
    }

    /**
     * 断言参数为空
     *
     * @param arg      参数
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void isNull(Object arg, Supplier<String> msg, ExceptionProvider provider) {
        if (arg != null) {
            throw provider.newRuntimeException(msg.get());
        }
    }

    /**
     * 断言指定字符串不为空
     *
     * @param str     字符串
     * @param argName 参数名
     */
    public static void argNotBlank(String str, String argName) {
        if (EmptyUtil.isEmpty(str)) {
            throw new IllegalArgumentException(String.format("arg [%s] must not be blank", argName));
        }
    }

    /**
     * 断言指定字符串不为空
     *
     * @param str      字符串
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void notBlank(String str, String msg, ExceptionProvider provider) {
        if (EmptyUtil.isEmpty(str)) {
            throw provider.newRuntimeException(msg);
        }
    }

    /**
     * 断言指定字符串不为空
     *
     * @param str      字符串
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void notBlank(String str, Supplier<String> msg, ExceptionProvider provider) {
        if (EmptyUtil.isEmpty(str)) {
            throw provider.newRuntimeException(msg.get());
        }
    }

    /**
     * 断言指定字符串为空
     *
     * @param str      字符串
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void isBlank(String str, String msg, ExceptionProvider provider) {
        if (EmptyUtil.isNotEmpty(str)) {
            throw provider.newRuntimeException(msg);
        }
    }

    /**
     * 断言指定字符串为空
     *
     * @param str      字符串
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void isBlank(String str, Supplier<String> msg, ExceptionProvider provider) {
        if (EmptyUtil.isEmpty(str)) {
            throw provider.newRuntimeException(msg.get());
        }
    }

    /**
     * 断言数组不能为空
     *
     * @param array   数组
     * @param argName 参数名
     * @param <T>     数组实际类型
     */
    public static <T> void argIsEmpty(T[] array, String argName) {
        if (EmptyUtil.isEmpty(array)) {
            throw new IllegalArgumentException(String.format("arg [%s] must not be empty", argName));
        }
    }

    /**
     * 断言数组不能为空
     *
     * @param array    数组
     * @param provider 异常提供器
     * @param <T>      数组实际类型
     */
    public static <T> void isEmpty(T[] array, ExceptionProvider provider) {
        isEmpty(array, (String) null, provider);
    }

    /**
     * 断言数组不能为空
     *
     * @param array    数组
     * @param msg      数组为空时的消息
     * @param provider 异常提供器
     * @param <T>      数组实际类型
     */
    public static <T> void isEmpty(T[] array, String msg, ExceptionProvider provider) {
        if (EmptyUtil.isNotEmpty(array)) {
            throw provider.newRuntimeException(msg);
        }
    }

    /**
     * 断言数组不能为空
     *
     * @param array    数组
     * @param msg      数组为空时的消息
     * @param provider 异常提供器
     * @param <T>      数组实际类型
     */
    public static <T> void isEmpty(T[] array, Supplier<String> msg, ExceptionProvider provider) {
        if (EmptyUtil.isEmpty(array)) {
            throw provider.newRuntimeException(msg.get());
        }
    }

    /**
     * 断言boolean值为true
     *
     * @param flag     指定boolean
     * @param provider 异常提供器
     */
    public static void assertTrue(boolean flag, ExceptionProvider provider) {
        assertTrue(flag, (String) null, provider);
    }

    /**
     * 断言boolean值为true
     *
     * @param flag     指定boolean
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void assertTrue(boolean flag, String msg, ExceptionProvider provider) {
        if (!flag) {
            throw provider.newRuntimeException(msg);
        }
    }

    /**
     * 断言boolean值为true
     *
     * @param flag     指定boolean
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void assertTrue(boolean flag, Supplier<String> msg, ExceptionProvider provider) {
        if (!flag) {
            throw provider.newRuntimeException(msg.get());
        }
    }

    /**
     * 断言boolean值为false
     *
     * @param flag     指定boolean
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void assertFalse(boolean flag, String msg, ExceptionProvider provider) {
        assertTrue(!flag, msg, provider);
    }

    /**
     * 断言boolean值为false
     *
     * @param flag     指定boolean
     * @param msg      异常消息
     * @param provider 异常提供器
     */
    public static void assertFalse(boolean flag, Supplier<String> msg, ExceptionProvider provider) {
        assertTrue(!flag, msg, provider);
    }
}
