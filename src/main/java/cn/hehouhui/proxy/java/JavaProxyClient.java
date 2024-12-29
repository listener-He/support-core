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
package cn.hehouhui.proxy.java;

import cn.hehouhui.proxy.Interception;
import cn.hehouhui.proxy.ProxyClient;
import cn.hehouhui.proxy.ProxyParent;
import cn.hehouhui.util.CollUtil;

import java.lang.reflect.Proxy;

/**
 * 需要注意的是java原生代理客户端只支持对接口的代理，不支持对普通类或者抽象类代理，同时不支持设置代理生成的类的名字
 *
 * @author HEHH
 * @date 2024/12/03
 */
public class JavaProxyClient implements ProxyClient {

    @Override
    public ClientType getClientType() {
        return ClientType.JAVA;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name, Interception interception,
        Class<?>[] paramTypes, Object[] params) {
        if (!CollUtil.sizeEquals(params, paramTypes)) {
            throw new IllegalArgumentException("构造器参数列表paramTypes长度和实际参数params长度不一致");
        }

        return (T)Proxy.newProxyInstance(loader, new Class[] {parent, ProxyParent.class},
            new MethodInterceptorAdapter(proxy, parent, interception));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<T> createClass(Class<T> parent, T proxy, ClassLoader loader, String name,
        Interception interception) {
        // java代理返回class对象没有必要，而且构造器是一个特殊构造器，详情参照Proxy#newProxyInstance方法实现
        throw new UnsupportedOperationException("java 代理不支持对对象进行代理");
    }
}
