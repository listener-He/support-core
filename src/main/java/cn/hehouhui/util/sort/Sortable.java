package cn.hehouhui.util.sort;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 排序
 *
 * @author HEHH
 * @date 2024/11/27
 */
public interface Sortable<T, E extends Comparable<E>> {


    /**
     * 对集合进行排序
     *
     * @param collection 待排序集合
     * @param sortField  排序字段
     *
     * @return {@link List }<{@link T }> 已排序集合
     */
    List<T> sort(Collection<T> collection, Function<T, E> sortField);

}
