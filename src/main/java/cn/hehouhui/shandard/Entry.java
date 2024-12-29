package cn.hehouhui.shandard;

import cn.hehouhui.util.EmptyUtil;

import java.util.List;
import java.util.Map;

/**
 * 键值对
 *
 * @author HeHui
 * @date 2024-11-24 23:06
 */
public class Entry<K, V> {

    private K key;

    private V value;

    public Entry() {}

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Entry(Map.Entry<K, V> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    public K getKey() {
        return key;
    }

    public void setKey(final K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(final V value) {
        this.value = value;
    }

    /**
     * 根据map转集合
     *
     * @param map
     *            map
     *
     * @return {@link List }<{@link Entry }<{@link K }, {@link V }>>
     */
    public static <K, V> List<Entry<K, V>> create(Map<K, V> map) {
        if (EmptyUtil.isEmpty(map)) {
            return EmptyUtil.emptyList();
        }
        return map.entrySet().stream().map(Entry::new).toList();
    }
}
