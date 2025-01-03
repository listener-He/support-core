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
package cn.hehouhui.proxy.cglib;

import cn.hehouhui.proxy.Interception;
import cn.hehouhui.proxy.ProxyClient;
import cn.hehouhui.proxy.ProxyParent;
import cn.hehouhui.util.CollUtil;
import cn.hehouhui.util.EmptyUtil;
import net.sf.cglib.proxy.Enhancer;

/**
 * cglib实现的代理客户端
 *
 * @author HEHH
 * @date 2024/12/03
 */
public class CglibProxyClient implements ProxyClient {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name, Interception interception,
        Class<?>[] paramTypes, Object[] params) {
        if (!CollUtil.sizeEquals(params, paramTypes)) {
            throw new IllegalArgumentException("构造器参数列表paramTypes长度和实际参数params长度不一致");
        }

        Enhancer enhancer = new Enhancer();
        if (parent.isInterface()) {
            enhancer.setInterfaces(new Class[] {ProxyParent.class, parent});
        } else {
            enhancer.setSuperclass(parent);
            enhancer.setInterfaces(new Class[] {ProxyParent.class});
        }
        enhancer.setClassLoader(loader);
        enhancer.setCallback(new MethodInterceptorAdapter(interception, proxy, parent));
        if (EmptyUtil.isEmpty(paramTypes)) {
            return (T)enhancer.create();
        } else {
            return (T)enhancer.create(paramTypes, params);
        }
    }

    /**
     * 构建指定对象的代理Class，稍后可以通过反射构建该class的实例，对象的类必须是公共的，同时代理方法也必须是公共的
     * <p>
     * 注意：生成的class通过反射调用构造器创建对象的时候，构造器中调用的方法不会被拦截！！！
     * </p>
     *
     * @param parent
     *            指定接口
     * @param proxy
     *            被代理的对象
     * @param loader
     *            加载生成的对象的class的classloader
     * @param name
     *            生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理class
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> createClass(Class<T> parent, T proxy, ClassLoader loader, String name,
        Interception interception) {
        Enhancer enhancer = new Enhancer();
        if (parent.isInterface()) {
            enhancer.setInterfaces(new Class[] {ProxyParent.class, parent});
        } else {
            enhancer.setSuperclass(parent);
            enhancer.setInterfaces(new Class[] {ProxyParent.class});
        }
        enhancer.setClassLoader(loader);
        enhancer.setCallback(new MethodInterceptorAdapter(interception, proxy, parent));
        return (Class<? extends T>)enhancer.createClass();
    }

    @Override
    public ClientType getClientType() {
        return ClientType.CGLIB;
    }
}
