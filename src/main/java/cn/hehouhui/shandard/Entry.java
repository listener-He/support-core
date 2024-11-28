package cn.hehouhui.shandard;

/**
 * 键值对
 *
 * @author HeHui
 * @date 2024-11-24 23:06
 */
public class Entry<K, V> {

    private K key;

    private V value;

    public Entry(){}

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
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
}
