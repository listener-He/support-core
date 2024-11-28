package cn.hehouhui.shandard;

import java.lang.annotation.*;

/**
 * API签名注解，用于标记需要进行签名验证的方法。
 *
 * @author HeHui
 * @date 2020-09-14 22:34
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented // 文档中包含此注解
public @interface Signature {

    /**
     * 需要忽略的表单参数名数组。
     * 这些表单参数不会被用于签名计算。
     *
     * @return 忽略的表单参数名数组，默认为空数组。
     */
    String[] ignore() default {};

    /**
     * 请求的过期时间，单位为毫秒。
     * 如果请求超过此时间，则认为请求过期。
     *
     * @return 过期时间，0表示使用默认过期时间。
     */
    long overdue() default 0;
}

