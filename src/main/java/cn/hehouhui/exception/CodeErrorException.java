package cn.hehouhui.exception;

import java.io.Serial;

/**
 * 代码异常
 *
 * @author HeHui
 * @date 2024-11-28 13:36
 */
public class CodeErrorException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6698595244845107834L;

    public CodeErrorException() {}

    public CodeErrorException(final String message) {
        super(message);
    }

    public CodeErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CodeErrorException(final Throwable cause) {
        super(cause);
    }

}
