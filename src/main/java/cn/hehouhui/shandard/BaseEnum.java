package cn.hehouhui.shandard;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    String getDesc();

    /**
     * 根据code获取指定枚举
     *
     * @param code
     *            枚举code
     * @param clazz
     *            枚举类型
     * @param <M>
     *            枚举实际类型
     * @return 对应的枚举
     */
    @SuppressWarnings({"all"})
    static <M extends BaseEnum<? extends Serializable>> Optional<M> codeIf(Object code, Class<?> clazz) {
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException(String.format("类型[%s]不是枚举", clazz.getName()));
        }

        if (code == null) {
            return Optional.empty();
        }

        M[] enumInterfaces = (M[])clazz.getEnumConstants();

        for (M enumInterface : enumInterfaces) {
            if (Objects.equals(enumInterface.getValue(), code)) {
                return Optional.of(enumInterface);
            }
        }
        if (code instanceof String) {
            // 兜底，使用系统自带方法转换
            return Optional.ofNullable((M)Enum.valueOf((Class<? extends Enum>)clazz, code.toString()));
        }
        return Optional.empty();
    }



}
