package cn.hehouhui.util;

import cn.hehouhui.function.complete.SetGet;
import lombok.Getter;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 快捷函数操作工具
 *
 * @author HEHH
 * @date 2024/03/06
 */
public class FunctionUtil {

    private FunctionUtil() {
        throw new AssertionError();
    }

    /**
     * 创建一个缓存函数
     * 该函数接收一个功能函数作为参数，该功能函数应返回一个从键列表到值映射的函数
     * 缓存函数的作用是将功能函数的结果缓存起来，以提高后续相同输入的处理效率
     *
     * @param function 功能函数，接受一个键列表并返回一个键值映射
     * @param <K>      键的类型
     * @param <V>      值的类型
     *
     * @return 一个经过缓存优化的函数，它会使用内部缓存来提高效率
     */
    public static <K, V> Function<List<K>, Map<K, V>> cacheFunction(Function<List<K>, Map<K, V>> function) {
        // 创建一个缓存映射，用于存储键值对
        Map<K, V> cacheMap = new HashMap<>();
        // 返回一个新的函数，该函数会对输入的键列表进行缓存处理
        return ids -> {
            if (EmptyUtil.isEmpty(ids)) {
                return Collections.emptyMap();
            }
            // 过滤出未缓存的键
            List<K> notCacheIds = ids.stream().filter(id -> !cacheMap.containsKey(id)).collect(Collectors.toList());
            // 如果有未缓存的键，则调用原始函数进行处理
            if (EmptyUtil.isNotEmpty(notCacheIds)) {
                Map<K, V> map = function.apply(notCacheIds);
                // 如果处理结果不为空，则将结果添加到缓存中
                if (EmptyUtil.isNotEmpty(map)) {
                    cacheMap.putAll(map);
                }
            }
            // 返回缓存映射
            return ids.stream().collect(Collectors.toMap(Function.identity(), cacheMap::get));
        };
    }

    /**
     * 创建并返回一个 {@link BinaryOperator}，它将两个输入元素中的第一个作为结果返回。 这个操作符主要用于合并或联合两个同类型元素时，选择第一个元素作为结果。
     *
     * @param <U>
     *            指定二元操作符操作的元素类型。
     *
     * @return 返回一个 {@link BinaryOperator} 实例，它接收两个 {@link U} 类型的参数，并返回第一个参数作为结果。
     */
    public static <U> U mergeFirst(U v1, U v2) {
        // 返回一个Lambda表达式，接收两个参数v1和v2，返回v1。
        return v1;
    }

    /**
     * id 查询出map 然后join
     *
     * @param collection
     *            id
     * @param nameMapCreator
     *            名称映射创建者
     * @param mapper
     *            映射
     * @param collector
     *            收藏家
     *
     * @return {@link R}
     */
    public static <I, N, E, R> R mapThenJoin(Collection<? extends I> collection,
        Function<? super List<I>, Map<? super I, ? extends N>> nameMapCreator,
        BiFunction<? super I, ? super N, ? extends E> mapper, Collector<? super E, ?, R> collector) {

        List<I> filtered = collection.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (EmptyUtil.isEmpty(filtered)) {
            return Stream.empty().map(id -> (E)null).collect(collector);
        }
        Map<? super I, ? extends N> nameMap = nameMapCreator.apply(filtered);
        return filtered.stream().map(id -> mapper.apply(id, nameMap.get(id))).filter(Objects::nonNull)
            .collect(collector);
    }

    /**
     * 为集合中的每个对象补全名称信息 该方法通过提供的ID到名称的映射函数和设置/获取函数对，为集合中每个对象的名称字段进行补全
     *
     * @param <I>
     *            对象ID的类型
     * @param <N>
     *            名称的类型
     * @param <E>
     *            集合元素的类型
     * @param collection
     *            需要处理的集合
     * @param nameMapCreator
     *            根据ID列表创建ID到名称的映射的函数
     * @param setGetFunctions
     *            一组包含ID获取器和名称设置器的函数对
     */
    @SafeVarargs
    public static <I, N, E> void complete(Collection<? extends E> collection,
        Function<? super List<I>, ? extends Map<? super I, ? extends N>> nameMapCreator,
        SetGet<E, I, N>... setGetFunctions) {

        // 如果设置/获取函数对或集合为空，则不进行任何操作
        if (setGetFunctions == null || EmptyUtil.isEmpty(collection)) {
            return;
        }
        // 将设置/获取函数对转换为列表形式，便于后续处理
        List<SetGet<E, I, N>> collect = Arrays.stream(setGetFunctions).toList();
        // 从集合中的每个对象中提取ID，并去重，用于后续查询名称信息
        List<I> idSet = collection.stream().flatMap(item -> collect.stream().map(f -> f.get(item)))
            .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        // 如果没有提取到任何ID，则不进行任何操作
        if (EmptyUtil.isEmpty(idSet)) {
            return;
        }
        // 根据ID查询
        Map<? super I, ? extends N> nameMap = nameMapCreator.apply(idSet);
        // 如果查询结果为空，则不进行任何操作
        if (nameMap == null || nameMap.isEmpty()) {
            return;
        }
        // 遍历集合，为非空对象补全名称
        collection.stream().filter(Objects::nonNull).forEach(e -> {
            collect.forEach(func -> {
                // 提取对象的ID
                I id = func.get(e);
                if (id != null) {
                    // 从映射表中获取对应的名称
                    N value = nameMap.get(id);
                    if (value != null) {
                        // 设置名称到对象上
                        func.set(e, value);
                    }
                }
            });

        });
    }

    /**
     * 补全 collection 中的名称 通过从集合中提取的ID集合查询出对应的name
     *
     * @param collection
     *            数据源
     * @param idGetter
     *            id 提取
     * @param nameSetter
     *            名称设置
     * @param nameMapCreator
     *            id与名称 Map数据函数
     */
    public static <I, N, E> void complete(Collection<? extends E> collection, Function<? super E, ? extends I> idGetter,
        BiConsumer<? super E, ? super N> nameSetter,
        Function<? super List<I>, ? extends Map<? super I, ? extends N>> nameMapCreator) {

        if (EmptyUtil.isEmpty(collection)) {
            return;
        }
        // 提取id集合
        List<I> idSet = collection.stream().filter(Objects::nonNull).map(idGetter).filter(Objects::nonNull).distinct()
            .collect(Collectors.toList());
        if (EmptyUtil.isEmpty(idSet)) {
            return;
        }
        // 根据ID查询
        Map<? super I, ? extends N> nameMap = nameMapCreator.apply(idSet);
        if (nameMap == null || nameMap.isEmpty()) {
            return;
        }
        // collection.forEach(e -> nameSetter.accept(e, nameMap.get(idGetter.apply(e))));
        complete(collection, idGetter, nameSetter, () -> nameMap);
    }

    /**
     * 补全集合中的对象名称。通过从提供的ID-名称映射中获取名称，设置到指定的集合对象上。
     *
     * @param collection
     *            数据源集合，需要补全名称的对象集合。
     * @param idGetter
     *            从集合对象中提取ID的函数接口。
     * @param nameSetter
     *            设置集合对象名称的双参数消费者接口。
     * @param nameMapCreator
     *            提供ID-名称映射的供应者接口。
     * @param <I>
     *            ID的类型。
     * @param <N>
     *            名称的类型。
     * @param <E>
     *            集合对象的类型。
     */
    public static <I, N, E> void complete(Collection<? extends E> collection, Function<? super E, ? extends I> idGetter,
        BiConsumer<? super E, ? super N> nameSetter, Supplier<? extends Map<? super I, ? extends N>> nameMapCreator) {
        // 如果集合为空，则直接返回，无需任何操作
        if (EmptyUtil.isEmpty(collection)) {
            return;
        }
        // 获取ID-名称映射表
        Map<? super I, ? extends N> nameMap = nameMapCreator.get();
        if (EmptyUtil.isEmpty(nameMap)) {
            return;
        }
        // 遍历集合，为非空对象补全名称
        collection.stream().filter(Objects::nonNull).forEach(e -> {
            // 提取对象的ID
            I id = idGetter.apply(e);
            if (id != null) {
                // 从映射表中获取对应的名称
                N value = nameMap.get(id);
                if (value != null) {
                    // 设置名称到对象上
                    nameSetter.accept(e, value);
                }
            }
        });
    }

    /**
     * 每次检查
     *
     * @param list
     *            列表
     * @param predicate
     *            谓语
     *
     * @return {@link Optional }<{@link String }> 是否检查通过
     */
    public static <T> Optional<String> forEachCheck(List<T> list, Function<Iterator<T>, String> predicate) {
        if (null == list || list.isEmpty()) {
            return Optional.empty();
        }
        T previous = list.get(0);
        String result = predicate.apply(new Iterator<>(null, previous, list.size() > 1));
        if (result != null) {
            return Optional.of(result);
        }

        for (int i = 1; i < list.size(); i++) {
            T value = list.get(i);
            result = predicate.apply(new Iterator<>(previous, value, i < list.size() - 1));
            if (result != null) {
                return Optional.of(result);
            }
            previous = value;
        }
        return Optional.empty();
    }

    /**
     * 迭代器
     *
     * @author HEHH
     * @date 2024/11/24
     */
    @Getter
    public static class Iterator<T> {

        private final T previous;

        private final T current;

        private final boolean next;

        public Iterator(T previous, T current, boolean next) {
            this.previous = previous;
            this.current = current;
            this.next = next;
        }

        /**
         * 获取上一个
         *
         * @return {@link Optional }<{@link T }>
         */
        public Optional<T> getPrevious() {
            return Optional.ofNullable(previous);
        }

    }


}
