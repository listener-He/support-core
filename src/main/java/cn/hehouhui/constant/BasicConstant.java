package cn.hehouhui.constant;

import java.io.Serial;
import java.io.Serializable;

/**
 * 常量
 *
 * @author HeHui
 * @date 2024-11-26 09:31
 */
public final class BasicConstant {

    // =============================================================
    // 数组常量
    // =============================================================

    private BasicConstant() {
        throw new AssertionError();
    }

    // =============================================================
    // 对象常量
    // =============================================================

    // 0-valued primitive wrappers
    public static final Byte ZERO = 0;
    public static final Float FLOAT_ZERO = 0F;
    public static final Double DOUBLE_ZERO = 0D;
    public static final Character CHAR_NULL = '\0';

    /**
     * 代表null值的占位对象。
     */
    public static final Object NULL_PLACEHOLDER = new NullPlaceholder();

    private final static class NullPlaceholder implements Serializable {
        @Serial
        private static final long serialVersionUID = 7092611880189329093L;

        public String toString() {
            return "null";
        }

        @Serial
        private Object readResolve() {
            return NULL_PLACEHOLDER;
        }
    }

    /**
     * 空字符串。
     */
    public static final String EMPTY = "";

    /** 默认分隔符 */
    public static final String SEPARATOR = ",";
}
