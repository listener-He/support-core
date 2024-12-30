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

import lombok.*;

import java.lang.reflect.GenericDeclaration;

/**
 * 表示一个唯一的泛型
 *
 * @author HEHH
 * @date 2024/12/02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenericDefinition {

    /**
     * 该泛型的名字
     */
    private String name;

    /**
     * 该泛型定义的位置
     */
    private GenericDeclaration declaration;

}