package cn.hehouhui.exception;

import java.io.Serial;

/**
 * 检查异常
 *
 * @author HeHui
 * @date 2025/01/23
 */
public class CheckedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -75228092901311538L;

  public CheckedException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
