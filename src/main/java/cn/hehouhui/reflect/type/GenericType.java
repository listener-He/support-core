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
package cn.hehouhui.reflect.type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * 表示一个泛型，例如List&lt;T&gt;的T
 *
 * @author HEHH
 * @date 2024/12/02
 */
@Getter
@Setter
@ToString(callSuper = true)
public class GenericType extends JavaType {

    /**
     * 该类型的父类型，当泛型为（T extends JavaType）这种形式时存在该值
     * 
     * 注意，如果用户既没有指定T extends，也没有指定T super，那么默认是T extends Object
     */
    @ToString.Exclude
    private JavaType parent;

    /**
     * 该类型的子类型，当泛型为（T super JavaType）这种形式时存在该值
     */
    @ToString.Exclude
    private JavaType child;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GenericType that = (GenericType)o;
        return Objects.equals(parent, that.parent) && Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parent, child);
    }
}
