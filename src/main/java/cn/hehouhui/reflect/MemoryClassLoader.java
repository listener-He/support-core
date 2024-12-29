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
package cn.hehouhui.reflect;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 内存类加载器
 *
 * @author HEHH
 * @date 2024/12/02
 */
public class MemoryClassLoader extends URLClassLoader {

    private final Map<String, byte[]> definitions = new HashMap<>();

    public MemoryClassLoader() {
        super(new URL[0]);
    }

    public MemoryClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public MemoryClassLoader(URL[] urls) {
        super(urls);
    }

    public MemoryClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * @param name
     *            class名字
     * @param bytes
     *            class数据
     */
    public void addDefinition(final String name, final byte[] bytes) {
        definitions.put(name, bytes);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        final byte[] bytes = definitions.remove(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.loadClass(name, resolve);
    }

}
