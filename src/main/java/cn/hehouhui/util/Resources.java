package cn.hehouhui.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * 资源加载器
 *  通过类加载器，获取资源
 * @author HeHui
 * @date 2024-11-26 22:47
 */
public class Resources {

    private static ClassLoader defaultClassLoader;

    private static Charset charset;

    private Resources() {
    }

    /**
     * 获取默认类加载器
     *
     * @return {@link ClassLoader }
     */
    public static ClassLoader getDefaultClassLoader() {
        return defaultClassLoader;
    }

    /**
     * 设置默认类加载器
     *
     * @param defaultClassLoader 默认类加载器
     */
    public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        Resources.defaultClassLoader = defaultClassLoader;
    }

    /**
     * 获取资源url
     *
     * @param resource 资源
     *
     * @return {@link URL }
     *
     * @throws IOException IOException
     */
    public static URL getResourceURL(String resource) throws IOException {
        return getResourceURL(getClassLoader(), resource);
    }

    /**
     * 获取资源url
     *
     * @param loader   装载机
     * @param resource 资源
     *
     * @return {@link URL }
     *
     * @throws IOException IOException
     */
    public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
        URL url = null;
        if (loader != null) {
            url = loader.getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        if (url == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return url;
    }

    /**
     * 以流形式获取资源
     *
     * @param resource 资源
     *
     * @return {@link InputStream }
     *
     * @throws IOException IOException
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(getClassLoader(), resource);
    }

    /**
     * 以流形式获取资源
     *
     * @param loader   装载机
     * @param resource 资源
     *
     * @return {@link InputStream }
     *
     * @throws IOException IOException
     */
    public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = null;
        if (loader != null) {
            in = loader.getResourceAsStream(resource);
        }
        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }

    /**
     * 获取资源作为属性
     *
     * @param resource 资源
     *
     * @return {@link Properties }
     *
     * @throws IOException IOException
     */
    public static Properties getResourceAsProperties(String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = resource;
        in = getResourceAsStream(propfile);
        props.load(in);
        in.close();
        return props;
    }


    /**
     * 获取资源作为属性
     *
     * @param loader   装载机
     * @param resource 资源
     *
     * @return {@link Properties }
     *
     * @throws IOException IOException
     */
    public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = resource;
        in = getResourceAsStream(loader, propfile);
        props.load(in);
        in.close();
        return props;
    }


    /**
     * 以读者身份获取资源
     *
     * @param resource 资源
     *
     * @return {@link Reader }
     *
     * @throws IOException IOException
     */
    public static Reader getResourceAsReader(String resource) throws IOException {
        return (charset == null) ? new InputStreamReader(getResourceAsStream(resource)) : new InputStreamReader(getResourceAsStream(resource), charset);
    }


    /**
     * 以读者身份获取资源
     *
     * @param loader   装载机
     * @param resource 资源
     *
     * @return {@link Reader }
     *
     * @throws IOException IOException
     */
    public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
        Reader reader;
        if (charset == null) {
            reader = new InputStreamReader(getResourceAsStream(loader, resource));
        } else {
            reader = new InputStreamReader(getResourceAsStream(loader, resource), charset);
        }

        return reader;
    }


    /**
     * 以文件形式获取资源
     *
     * @param resource 资源
     *
     * @return {@link File }
     *
     * @throws IOException IOException
     */
    public static File getResourceAsFile(String resource) throws IOException {
        return new File(getResourceURL(resource).getFile());
    }


    /**
     * 以文件形式获取资源
     *
     * @param loader   装载机
     * @param resource 资源
     *
     * @return {@link File }
     *
     * @throws IOException IOException
     */
    public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
        return new File(getResourceURL(loader, resource).getFile());
    }


    /**
     * 获取url作为流
     *
     * @param urlString url字符串
     *
     * @return {@link InputStream }
     *
     * @throws IOException IOException
     */
    public static InputStream getUrlAsStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }


    /**
     * 以阅读器身份获取url
     *
     * @param urlString url字符串
     *
     * @return {@link Reader }
     *
     * @throws IOException IOException
     */
    public static Reader getUrlAsReader(String urlString) throws IOException {
        return new InputStreamReader(getUrlAsStream(urlString));
    }


    /**
     * 获取url作为属性
     *
     * @param urlString url字符串
     *
     * @return {@link Properties }
     *
     * @throws IOException IOException
     */
    public static Properties getUrlAsProperties(String urlString) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = urlString;
        in = getUrlAsStream(propfile);
        props.load(in);
        in.close();
        return props;
    }


    /**
     * 类名称
     *
     * @param className 类名
     *
     * @return {@link Class }<{@link ? }>
     *
     * @throws ClassNotFoundException 类未找到异常
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            clazz = getClassLoader().loadClass(className);
        } catch (Exception e) {
            // Ignore. Failsafe below.
        }
        if (clazz == null) {
            clazz = Class.forName(className);
        }
        return clazz;
    }


    /**
     * 实例化
     *
     * @param className 类名
     *
     * @return {@link Object }
     *
     * @throws ClassNotFoundException 类未找到异常
     * @throws InstantiationException 实例化异常
     * @throws IllegalAccessException 非法访问异常
     */
    public static Object instantiate(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return instantiate(classForName(className));
    }


    /**
     * 实例化
     *
     * @param clazz clazz
     *
     * @return {@link Object }
     *
     * @throws InstantiationException 实例化异常
     * @throws IllegalAccessException 非法访问异常
     */
    public static Object instantiate(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }

    /**
     * 获取类加载器
     *
     * @return {@link ClassLoader }
     */
    private static ClassLoader getClassLoader() {
        if (defaultClassLoader != null) {
            return defaultClassLoader;
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    /**
     * 获取字符集
     *
     * @return {@link Charset }
     */
    public static Charset getCharset() {
        return charset;
    }

    /**
     * 设置字符集
     *
     * @param charset 字符集
     */
    public static void setCharset(Charset charset) {
        Resources.charset = charset;
    }

}
