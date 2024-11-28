package cn.hehouhui.util;

import cn.hehouhui.constant.ExceptionProviderConst;
import cn.hehouhui.exception.PluginException;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Java class 反射相关 工具
 *
 * @author HeHui
 * @date 2024-11-28 10:13
 */
public class ClassUtil {

    private ClassUtil() {
        throw new AssertionError();
    }

    /**
     * 常用的集合类，注意，有先后顺序，先加入的优先级较高
     */
    private static final List<Class<?>> IMPL_CLASSES = new ArrayList<>();

    /**
     * 到object后默认深度+100
     */
    private static final int OBJECT_DEEP = 100;

    /**
     * lambda表达式类名中的标识符
     */
    private static final String LAMBDA_ID = "$$Lambda$";

    /** The package separator character: '.' */
    private static final char PACKAGE_SEPARATOR = '.';

    /** The ".class" file suffix */
    private static final String CLASS_FILE_SUFFIX = ".class";

    static {
        // list的几个常用实现
        IMPL_CLASSES.add(ArrayList.class);
        IMPL_CLASSES.add(CopyOnWriteArrayList.class);
        // set的几个常用实现
        IMPL_CLASSES.add(HashSet.class);
        IMPL_CLASSES.add(TreeSet.class);
        IMPL_CLASSES.add(ConcurrentSkipListSet.class);
        // map的几个常用实现
        IMPL_CLASSES.add(HashMap.class);
        IMPL_CLASSES.add(TreeMap.class);
        IMPL_CLASSES.add(ConcurrentHashMap.class);
    }


    /**
     * 是否原始类
     *
     * @param clazz clazz
     *
     * @return boolean
     */
    public static boolean isPrimitive(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isPrimitive() || clazz.isEnum() || clazz.isAnnotation() || clazz.isSynthetic();
    }

    /**
     * is数组
     *
     * @param clazz clazz
     *
     * @return boolean
     */
    public static boolean isArray(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isArray() || clazz.isInterface();
    }


    /**
     * 比较两个对象是否兼容
     * <p>
     * 主要用于判断两个对象是否属于同一类或相互兼容的类
     * 它首先检查两个对象是否为null，然后比较它们的类是否相同，
     * 或者一个类是否可以赋值给另一个类，从而确定它们是否相等
     *
     * @param bean1 第一个对象，用于比较
     * @param bean2 第二个对象，用于比较
     *
     * @return boolean 如果两个对象相等或属于相互兼容的类
     */
    public static boolean compatible(Object bean1, Object bean2) {
        // 检查对象是否为null，如果任一对象为null，则返回false
        if (bean1 == null || bean2 == null) {
            return false;
        }
        return compatible(bean1.getClass(), bean2.getClass());
    }


    /**
     * 比较两个类型是否兼容
     * <p>
     * 主要用于判断两个类型是否属于同一类或相互兼容的类
     * 它首先检查两个类型是否为null，然后比较它们的类是否相同，
     * 或者一个类是否可以赋值给另一个类，从而确定它们是否相等
     *
     * @param class1 第一个类型，用于比较
     * @param class2 第二个类型，用于比较
     *
     * @return boolean 如果两个对象相等或属于相互兼容的类
     */
    public static boolean compatible(Class<?> class1, Class<?> class2) {
        if (class1 == null || class2 == null) {
            return false;
        }
        // 检查两个对象的类是否相同，如果相同，则返回true
        if (class1.equals(class2)) {
            return true;
        }
        // 检查第一个对象的类是否可以由第二个对象的类赋值，如果是，则返回true
        else if (class1.isAssignableFrom(class2)) {
            return true;
        }
        // 检查第二个对象的类是否可以由第一个对象的类赋值，如果是，则返回false
        else if (class2.isAssignableFrom(class1)) {
            return false;
        }
        // 如果以上条件都不满足，则返回false
        return false;
    }


    /**
     * 是否具有范型参数
     *
     * @param clazz clazz
     *
     * @return boolean
     */
    public static boolean hasGenericParameters(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        // 检查类本身是否有泛型参数
        if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
            return true;
        }

        // 检查类实现的接口是否有泛型参数
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取泛型参数
     *
     * @param bean 豆
     *
     * @return {@link Class }<{@link ? }>
     */
    public static Class<?> getGenericParameter(Object bean) {
        if (bean == null) {
            return null;
        }
        Class<?> clazz = bean.getClass();
        if (isPrimitive(clazz)) {
            return null;
        }
        return getGenericParameter(clazz);
    }

    /**
     * 获取泛型参数
     *
     * @param clazz clazz
     *
     * @return {@link Class }<{@link ? }>
     */
    public static Class<?> getGenericParameter(Class<?> clazz) {
        return getGenericParameter(clazz, 0);
    }

    /**
     * 获取泛型类型
     *
     * @param clazz clazz
     * @param index 指数
     *
     * @return {@link Class }<{@link ? }>
     */
    public static Class<?> getGenericParameter(Class<?> clazz, int index) {
        if (clazz == null) {
            return null;
        }
        // 判断本身对象是否包含范型参数
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof final ParameterizedType parameterizedType) {
            return getParameterizedTypeClass(parameterizedType, index);
        }
        // 判断父类接口是否包含范型参数
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof final ParameterizedType parameterizedType) {
                return getParameterizedTypeClass(parameterizedType, index);
            }
        }
        return null;
    }

    /**
     * 获取范型类型
     *
     * @param parameterizedType 参数类型
     * @param index             索引
     *
     * @return {@link Class }<{@link ? }>
     */
    public static Class<?> getParameterizedTypeClass(ParameterizedType parameterizedType, int index) {
        if (parameterizedType == null) {
            return null;
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (index < actualTypeArguments.length && actualTypeArguments[index] instanceof Class) {
            return (Class<?>) actualTypeArguments[index];
        }
        return null;
    }


    /**
     * 构建指定子类到其父类的继承链
     *
     * @param child  子类
     * @param parent 父类
     * @param chain  存放继承链的list
     * @param <T>    当前继承链的实际类型
     *
     * @return 构造好的继承链，第一个是子类，最后一个是父类
     */
    public static <T extends List<Class<?>>> T buildChain(Class<?> child, Class<?> parent, T chain) {
        chain.add(child);
        if (child.equals(parent)) {
            return chain;
        } else if (child.equals(Object.class)) {
            throw new PluginException(
                StrUtil.format("当前类型[{}]不是类型[{}]的子类", child, parent));
        } else {
            return buildChain(child.getSuperclass(), parent, chain);
        }
    }

    /**
     * 获取指定类的路径，如果同一个class两个jar包都提供了，该方法可以返回class以及所在的jar包的路径，主要用于分析一些极端问题
     *
     * @param cls 类
     *
     * @return 该类的路径URL，获取失败返回null，注意，该URL可能不能调用{@link URL#openStream()}，例如如果类是lambda表达式的情况
     */
    public static URL where(final Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null input: cls");
        }

        URL result = null;
        final String clsAsResource = cls.getName().replace('.', '/').concat(".class");
        final ProtectionDomain pd = cls.getProtectionDomain();
        if (pd != null) {
            final CodeSource cs = pd.getCodeSource();
            if (cs != null) {
                result = cs.getLocation();
            }

            if (result != null) {
                if ("file".equals(result.getProtocol())) {
                    try {
                        if (result.toExternalForm().endsWith(".jar") || result.toExternalForm().endsWith(".zip")) {
                            result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
                        } else if (new File(result.getFile()).isDirectory()) {
                            result = new URL(result, clsAsResource);
                        }
                    } catch (MalformedURLException ignore) {
                    }
                }
            }
        }
        if (result == null) {
            final ClassLoader clsLoader = cls.getClassLoader();
            result =
                clsLoader != null ? clsLoader.getResource(clsAsResource) : ClassLoader.getSystemResource(clsAsResource);
        }

        return result;
    }

    /**
     * 获取指定class的class文件的输入流
     *
     * @param clazz class
     *
     * @return 对应的输入流
     */
    public static InputStream getClassAsStream(Class<?> clazz) {
        Assert.argNotNull(clazz, "clazz");
        return clazz.getResourceAsStream(getClassFileName(clazz));
    }

    /**
     * 获取class的class文件名（不包含包名，例如：String.class）
     *
     * @param clazz the class
     *
     * @return .class文件名
     */
    public static String getClassFileName(Class<?> clazz) {
        Assert.argNotNull(clazz, "clazz");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

    /**
     * 获取默认classloader
     *
     * @return 当前默认classloader（先从当前线程上下文获取，获取不到获取加载该类的ClassLoader，还获取不到就获取系统classloader）
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassUtil.class.getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
        }
        return loader;
    }


    /**
     * 计算两个类之间的距离；两个类之间的距离定义：如果类A与类B相同，则距离为0，如果类A的父类是类B，则距离是1，如果类A的父类的父类是类B，则距离是2
     *
     * @param targetType 目标类
     * @param srcType    要计算的类，必须与目标类相同或者是目标类的子类
     *
     * @return srcType到targetType的距离
     */
    public static int calcGap(Class<?> targetType, Class<?> srcType) {
        Assert.assertTrue(targetType.isAssignableFrom(srcType),
            StrUtil.format("类型 [{}] 不是类型 [{}] 的子类", srcType, targetType),
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        return calcGap(targetType, srcType, 0, new HashMap<>());
    }

    /**
     * 计算两个类之间的距离；两个类之间的距离定义：如果类A与类B相同，则距离为0，如果类A的父类是类B，则距离是1，如果类A的父类的父类是类B，则距离是2
     *
     * @param targetType 目标类
     * @param srcType    源类
     * @param nowGap     当前计算的距离
     *
     * @return srcType到targetType的距离
     */
    private static int calcGap(Class<?> targetType, Class<?> srcType, int nowGap, Map<String, Integer> cache) {
        String key = srcType.getName() + "->" + targetType.getName();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        if (srcType == Object.class) {
            return nowGap + OBJECT_DEEP;
        }

        if (srcType == targetType) {
            cache.put(key, nowGap);
            return nowGap;
        }

        if (targetType.isInterface()) {
            int min = Integer.MAX_VALUE;
            for (Class<?> anInterface : srcType.getInterfaces()) {
                if (anInterface == targetType) {
                    min = 1;
                    break;
                } else if (targetType.isAssignableFrom(anInterface)) {
                    min = Math.min(min, calcGap(targetType, anInterface, nowGap, cache));
                }
            }
            if (min != Integer.MAX_VALUE) {
                cache.put(key, min + nowGap);
                return min + nowGap;
            }
        }

        Class<?> superClass = srcType.getSuperclass();
        if (superClass != null) {
            int result = calcGap(targetType, superClass, nowGap + 1, cache);
            cache.put(key, result);
            return result;
        }

        return nowGap;
    }


    /**
     * 断言类不是lambda表达式
     *
     * @param clazz 类
     */
    private static void checkNotLambda(Class<?> clazz) {
        if (clazz.getName().contains(LAMBDA_ID)) {
            throw new PluginException(String.format("目前不支持lambda表达式[%s]", clazz));
        }
    }

}
