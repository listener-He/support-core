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
package cn.hehouhui.reflect.bean.converter;

import cn.hehouhui.constant.ExceptionProviderConst;
import cn.hehouhui.reflect.bean.TypeConverter;
import cn.hehouhui.util.Assert;
import cn.hehouhui.util.StrUtil;

import java.lang.annotation.Annotation;

/**
 * 抽象类型转换器
 *
 * @author HEHH
 * @date 2024/12/02
 */
public abstract class AbstractTypeConverter implements TypeConverter {

    @SuppressWarnings("unchecked")
    @Override
    public <S, T> T convert(S src, Annotation[] annotations, Class<T> targetType) {
        Assert.argNotNull(targetType, "targetType");

        if (src == null) {
            return null;
        }

        Class<S> srcType = (Class<S>)src.getClass();
        Assert.assertTrue(test(srcType, targetType),
            StrUtil.format("类型[{}]不能使用类型转换器[{}]转换为[{}]", srcType, this.getClass(), targetType),
            ExceptionProviderConst.IllegalStateExceptionProvider);
        return convert(srcType, targetType, src, annotations);
    }

    /**
     * 将类型S的src转换为类型T的结果
     * 
     * @param srcType
     *            源class
     * @param targetType
     *            目标class
     * @param src
     *            源数据
     * @param <S>
     *            源类型
     * @param <T>
     *            目标类型
     * @return 结果
     */
    protected abstract <S, T> T convert(Class<S> srcType, Class<T> targetType, S src, Annotation[] annotations);
}
