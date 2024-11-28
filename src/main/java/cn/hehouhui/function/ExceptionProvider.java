package cn.hehouhui.function;

/**
 * 异常提供者
 *
 * @author HEHH
 * @date 2024/11/28
 */
@FunctionalInterface
public interface ExceptionProvider {

    /**
     * 创建一个新的异常
     *
     * @param cause
     *            cause，可能为空
     * @param msg
     *            msg，不能为空
     * @return 异常
     */
    RuntimeException newRuntimeException(Throwable cause, String msg);

    /**
     * 创建一个新的异常
     *
     * @param msg
     *            异常消息，不能为空
     * @return 异常
     */
    default RuntimeException newRuntimeException(String msg) {
        return newRuntimeException(null, msg);
    }
}
