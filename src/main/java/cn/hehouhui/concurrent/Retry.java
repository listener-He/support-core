/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cn.hehouhui.concurrent;

import cn.hehouhui.constant.ExceptionProviderConst;
import cn.hehouhui.util.Assert;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 重试工具
 *
 * @author HEHH
 * @date 2024/12/02
 */
public class Retry {

    /**
     * 执行指定函数，最多重试指定次数，发生异常时根据参数判断是抛出异常还是什么都不做
     *
     * @param runnable     要执行的函数
     * @param retry        最大重试次数
     * @param throwIfError 发生异常时是否抛出，true 表示需要抛出
     */
    public static void run(Runnable runnable, int retry, boolean throwIfError) {
        runWithRetry(() -> {
            runnable.run();
            return null;
        }, null, t -> true, retry, throwIfError);
    }

    /**
     * 执行指定函数，最多重试指定次数，发生异常时返回默认结果
     *
     * @param supplier      指定函数
     * @param defaultResult 默认结果
     * @param retry         最大重试次数
     * @param <T>           结果类型
     *
     * @return 结果
     */
    public static <T> T runWithRetry(Supplier<T> supplier, Supplier<T> defaultResult, int retry) {
        return runWithRetry(supplier, defaultResult, Objects::nonNull, retry, false);
    }

    /**
     * 执行指定函数，最多重试指定次数，如果仍然失败将会抛出异常
     *
     * @param supplier 指定函数
     * @param retry    最大重试次数
     * @param <T>      结果类型
     *
     * @return 结果
     */
    public static <T> T runWithRetry(Supplier<T> supplier, int retry) {
        return runWithRetry(supplier, null, Objects::nonNull, retry, true);
    }

    /**
     * 执行指定函数，最多重试指定次数，发生异常时根据参数确认是返回默认结果还是抛出异常
     *
     * @param supplier      指定函数
     * @param defaultResult 默认结果
     * @param isSuccess     判断结果是否成功
     * @param retry         最大重试次数
     * @param throwIfError  重试超限后异常是否抛出，true 表示需要抛出
     *
     * @return {@link T } 结果
     */
    public static <T> T runWithRetry(Supplier<T> supplier, Supplier<T> defaultResult, Predicate<T> isSuccess, int retry, boolean throwIfError) {
        // 确保 supplier 不为空
        Assert.argNotNull(supplier, "supplier");
        // 确保 retry 大于 0
        Assert.assertTrue(retry > 0, "retry 必须大于0", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        int i = 0;
        Throwable throwable = null;
        while (i < retry) {
            i++;
            try {
                // 尝试执行 supplier
                T t = supplier.get();
                if (isSuccess.test(t)) {
                    // 如果执行成功，返回结果
                    return t;
                }
            } catch (Throwable e) {
                // 记录异常信息
                System.err.println("Exception occurred: " + e.getMessage());
                // 保存异常，以便后续处理
                throwable = e;
            }
        }

        // 当 retry 为 1 时，只会执行一次
        if (throwIfError) {
            // 如果需要抛出异常，重新抛出捕获的异常
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        } else {
            if (defaultResult == null) {
                return null;
            }
            // 返回默认结果
            return defaultResult.get();
        }
    }


}
