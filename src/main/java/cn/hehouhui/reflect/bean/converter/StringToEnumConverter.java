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

import cn.hehouhui.shandard.BaseEnum;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * String转换enum
 *
 * @author HEHH
 * @date 2024/12/02
 */
public class StringToEnumConverter extends AbstractTypeConverter {

    @Override
    public boolean test(Class<?> srcType, Class<?> targetType, Annotation[] annotations) {
        return String.class.equals(srcType) && Enum.class.isAssignableFrom(targetType);
    }

    @Override
    @SuppressWarnings("all")
    protected <S, T> T convert(Class<S> srcType, Class<T> targetType, S src, Annotation[] annotations) {
        if (BaseEnum.class.isAssignableFrom(targetType)) {
            return (T)BaseEnum.codeIf((String)src, targetType).orElse(null);
        }

        T[] enumConstants = targetType.getEnumConstants();
        for (T enumConstant : enumConstants) {
            if (Objects.equals(((Enum)enumConstant).name(), src)) {
                return enumConstant;
            }
        }

        throw new IllegalArgumentException("No enum constant " + targetType.getCanonicalName() + "." + src);
    }
}
