package cn.hehouhui.util;

import java.util.*;

/**
 * 判断空工具
 *
 * @author HeHui
 * @date 2024-11-26 09:38
 */
public class EmptyUtil {

    private EmptyUtil() {
        throw new AssertionError();
    }

    /**
     * 集合是否为空
     *
     * @param collection 集合
     *
     * @return boolean
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 集合不为空
     *
     * @param collection 集合
     *
     * @return boolean
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 为空
     *
     * @param arrays 集合
     *
     * @return boolean
     */
    public static boolean isEmpty(Object[] arrays) {
        return arrays == null || arrays.length == 0;
    }


    /**
     * 检查给定的数组中是否至少有一个为空
     * <p>
     * 此方法主要用于在多个数组参数的场景下，判断是否存在空数组参数
     * 它首先检查传入的数组参数本身是否为空，然后检查数组中的元素是否存在空
     *
     * @param arrays 可变参数数组，包含待检查的数组
     *
     * @return boolean 如果传入的数组参数为空，或者数组中的任一元素为null，则返回true；否则返回false
     */
    public static boolean isAnyEmpty(Object... arrays) {
        // 检查传入的数组参数本身是否为空
        if (isEmpty(arrays)) {
            return true;
        }
        // 检查数组中的元素是否存在空
        return Arrays.stream(arrays).anyMatch(Objects::isNull);
    }

    /**
     * 对象不为空
     *
     * @param arrays 集合
     *
     * @return boolean
     */
    public static boolean isNotEmpty(Object[] arrays) {
        return !isEmpty(arrays);
    }


    /**
     * 集合是否为空
     *
     * @param map map集合
     *
     * @return boolean
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 检查给定的多个Map中是否至少有一个为空
     *
     * @param maps 可变参数，代表多个Map对象
     * @return 如果至少有一个Map为空，则返回true；否则返回false
     */
    public static boolean isAnyEmpty(Map<?, ?>... maps) {
        // 检查传入的Map数组是否为空
        if (isEmpty(maps)) {
            return true;
        }
        // 使用流处理，检查每个Map是否为空
        return Arrays.stream(maps).anyMatch(EmptyUtil::isEmpty);
    }


    /**
     * 集合不为空
     *
     * @param map map集合
     *
     * @return boolean
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 字符串是否为空
     *
     * @param str 字符串
     *
     * @return boolean
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查给定的字符串数组中是否有任何字符串为空
     * 此方法用于验证一个或多个字符串是否为空，如果至少有一个字符串为空，则返回true
     * 主要用于在执行某些操作前，验证数据的有效性，避免空指针异常等问题
     *
     * @param strings 可变参数字符串数组，表示待检查的字符串集合
     * @return 如果任何字符串为空，则返回true；否则返回false
     */
    public static boolean isAnyEmpty(String... strings) {
        // 检查传入的数组是否为空
        if (isEmpty(strings)) {
            return true;
        }
        // 使用流处理，检查每个字符串是否为空
        return Arrays.stream(strings).anyMatch(EmptyUtil::isEmpty);
    }


    /**
     * 集合不为空
     *
     * @param str 字符串
     *
     * @return boolean
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 为空
     *
     * @param bean 对象
     *
     * @return boolean
     */
    public static boolean isEmpty(Object bean) {
        return Objects.isNull(bean);
    }

    /**
     * 对象不为空
     *
     * @param bean 豆
     *
     * @return boolean
     */
    public static boolean isNotEmpty(Object bean) {
        return !isEmpty(bean);
    }

    /**
     * 空集合
     *
     * @return {@link List }<{@link T }>
     */
    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    /**
     * 空集合
     *
     * @return {@link Set }<{@link T }>
     */
    public static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    /**
     * 空map
     *
     * @return {@link Map }<{@link K },{@link V }>
     */
    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    ;
}
