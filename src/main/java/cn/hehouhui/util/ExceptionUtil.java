package cn.hehouhui.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * 异常工具
 *
 * @author HeHui
 * @date 2024-11-28 13:28
 */
public class ExceptionUtil {

    /**
     * 打印异常的异常栈到字符串
     *
     * @param throwable 异常
     *
     * @return 异常栈
     */
    public static String printStack(Throwable throwable) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        throwable.printStackTrace(new PrintStream(bos, true, Charset.defaultCharset()));
        return bos.toString(Charset.defaultCharset());
    }

    /**
     * 通过递归调用getCause来获取最底层的异常
     *
     * @param throwable 上层异常，不能为null
     *
     * @return 最底层异常
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable.getCause() == null) {
            return throwable;
        } else {
            return getRootCause(throwable.getCause());
        }
    }
}
