package cn.hehouhui.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 获取Bean属性名称
 * 
 * @author HEHH
 * @date 2024/03/08
 */
@FunctionalInterface
public interface BeanFieldNameFunction<T, R> extends Function<T, R>, Serializable {}
