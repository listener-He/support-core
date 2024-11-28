package cn.hehouhui.shandard;

import java.io.Serializable;

/**
 * 枚举基础接口 为反序列化做支持
 *
 * @author HEHH
 * @date 2024/11/19
 */
public interface BaseEnum<T extends Serializable> {

    /**
     * 枚举值
     *
     * @return {@link T } 枚举值
     */
    T getValue();

    /**
     * 枚举描述
     *
     * @return {@link String } 枚举描述
     */
    String getDescription();

}
