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
package cn.hehouhui.proxy.bytebuddy;

import cn.hehouhui.proxy.Interception;
import cn.hehouhui.proxy.ProxyClassLoader;
import cn.hehouhui.proxy.ProxyClient;
import cn.hehouhui.proxy.ProxyParent;
import cn.hehouhui.reflect.ClassUtil;
import cn.hehouhui.util.CollUtil;
import cn.hehouhui.util.EmptyUtil;
import lombok.CustomLog;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 代理客户端bytebuddy实现
 *
 * @author HEHH
 * @date 2024/12/03
 */
@CustomLog
public class ByteBuddyProxyClient implements ProxyClient {
    private static final AnyMethodElementMatcher MATCHER = new AnyMethodElementMatcher();

    @Override
    public <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name, Interception interception,
        Class<?>[] paramTypes, Object[] params) {
        if (!CollUtil.sizeEquals(params, paramTypes)) {
            throw new IllegalArgumentException("构造器参数列表paramTypes长度和实际参数params长度不一致");
        }
        return ClassUtil.getInstance(createClass(parent, proxy, loader, name, interception), paramTypes, params);
    }

    @Override
    public <T> Class<? extends T> createClass(Class<T> parent, T proxy, ClassLoader loader, String name,
        Interception interception) {
        DynamicType.Builder<T> builder = new ByteBuddy().subclass(parent).implement(ProxyParent.class);

        if (EmptyUtil.isNotEmpty(name)) {
            builder = builder.name(name);
        }

        ClassLoader usedLoader = loader == null ? ProxyClient.DEFAULT_LOADER : loader;

        builder =
            builder.method(MATCHER).intercept(MethodDelegation.to(new GeneralInterceptor(interception, parent, proxy)));
        ProxyClassLoader realLoader;
        if (usedLoader instanceof ProxyClassLoader) {
            realLoader = (ProxyClassLoader)usedLoader;
        } else {
            realLoader = new ProxyClassLoader(usedLoader);
        }
        return builder.make().load(realLoader, (classLoader, types) -> CollUtil.convert(types, classLoader::buildClass))
            .getLoaded();
    }

    @Override
    public ClientType getClientType() {
        return ClientType.BYTE_BUDDY;
    }

    /**
     * 匹配任何方法
     */
    private static class AnyMethodElementMatcher implements ElementMatcher<MethodDescription> {

        @Override
        public boolean matches(MethodDescription target) {
            return true;
        }
    }
}
