package cn.hehouhui.util;

import cn.hehouhui.constant.BasicConstant;
import cn.hehouhui.constant.ExceptionProviderConst;
import cn.hehouhui.shandard.TreeNode;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 集合工具类
 *
 * @author HeHui
 * @date 2024-11-26 23:44
 */
public class CollUtil {

    private CollUtil() {
        throw new AssertionError();
    }

    /**
     * map转换，将map的value按照指定方式转换为新的value然后返回转换后的map
     *
     * @param map
     *            要转换的map
     * @param function
     *            转换函数，用于将老的value转换为新的value
     * @param <K>
     *            KEY类型
     * @param <NEW>
     *            转换后的value类型
     * @param <OLD>
     *            转换前的value类型
     *
     * @return 转换后的map
     */
    public static <K, NEW, OLD> Map<K, NEW> convert(Map<K, OLD> map, Function<OLD, NEW> function) {
        Map<K, NEW> newMap = new HashMap<>();
        map.forEach((k, v) -> newMap.put(k, function.apply(v)));
        return newMap;
    }

    /**
     * 将数组长度加1并且将指定数据添加到数组末尾
     *
     * @param target
     *            要添加的数据
     * @param array
     *            目标数组
     *
     * @return 新数组
     */
    public static <T> T[] append(T target, T[] array) {
        T[] newArray = createArray(target.getClass(), array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = target;
        return newArray;
    }

    /**
     * 根据指定的开始值、结束值、下一个值的生成函数、映射函数和已存在的值集合，填充一个集合
     *
     * @param start
     *            范围的开始值，不能为空
     * @param end
     *            范围的结束值，不能为空
     * @param next
     *            用于生成下一个值的函数，不能为空
     * @param mapper
     *            用于将集合中的元素映射到可比较类型的函数
     * @param existingValue
     *            已存在的值集合
     * @param value
     *            要填充的值
     *
     * @return {@link List }<{@link T }> 填充后的集合
     */
    public static <T, E extends Comparable<E>> List<T> fill(E start, E end, Function<E, E> next, Function<T, E> mapper,
        Collection<T> existingValue, T value) {
        // 确保开始值、结束值和下一个值的生成函数不为空
        Assert.notNull(start, "开始值不能为空");
        Assert.notNull(end, "结束值不能为空");
        Assert.notNull(next, "next不能为空");

        // 初始化范围列表，用于存储从开始值到结束值的所有值
        List<E> range = new ArrayList<>();
        range.add(start);

        // 使用原子引用来存储当前值，以便在多线程环境下安全地更新当前值
        AtomicReference<E> startRef = new AtomicReference<>(start);

        // 生成范围列表，直到达到或超过结束值
        while (true) {
            E nextValue = next.apply(startRef.get());
            if (nextValue.compareTo(end) >= 0) {
                range.add(end);
                break;
            }
            if (nextValue.compareTo(startRef.get()) <= 0) {
                throw ExceptionProviderConst.RuntimeExceptionProvider
                    .newRuntimeException("Next " + nextValue + " ge Pre " + startRef.get());
            }
            range.add(start);
            startRef.set(nextValue);
        }

        // 调用fill方法，根据生成的范围列表填充集合
        return fill(range, mapper, existingValue, value);
    }

    /**
     * 根据给定的范围和映射函数，用指定的值填充一个集合
     * <p>
     * 如果范围为空，则返回一个空列表 否则，将现有值转换为Map，以便快速查找
     * <p>
     * 对于范围中的每个元素，使用映射函数在现有值中查找对应的值，如果找不到，则使用指定的值
     *
     * @param range
     *            范围集合，用于确定填充的大小和顺序
     * @param mapper
     *            映射函数，用于将T类型转换为E类型
     * @param existingValue
     *            现有值集合，用于查找已存在的值
     * @param value
     *            默认值，当范围中的元素在现有值中找不到时使用
     *
     * @return {@link List }<{@link T }> 填充后的集合
     */
    public static <T, E> List<T> fill(Collection<E> range, Function<T, E> mapper, Collection<T> existingValue,
        T value) {
        // 检查范围是否为空，如果为空，则直接返回一个空列表
        if (EmptyUtil.isEmpty(range)) {
            return EmptyUtil.emptyList();
        }
        // 将现有值转换为Map，键为通过映射函数转换后的值，值为原始值
        // 这样做是为了在填充时能够快速查找现有值中是否存在对应的值
        Map<E, T> map = toMap(existingValue, mapper, Function.identity());

        // 对范围中的每个元素，使用映射函数在现有值中查找对应的值
        // 如果找不到，则使用指定的值
        // 最后将结果收集到一个新的列表中并返回
        return range.stream().map(e -> {
            T t = map.get(e);
            return t == null ? value : t;
        }).collect(Collectors.toList());
    }

    /**
     * 去重
     *
     * @param collection
     *            数据
     * @param keyFunction
     *            唯一标识
     *
     * @return {@link List }<{@link T }>
     */
    public static <T, K> List<T> distinct(Collection<T> collection, Function<T, K> keyFunction) {
        if (EmptyUtil.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(collection.stream().filter(e -> keyFunction.apply(e) != null)
            .collect(Collectors.toMap(keyFunction, Function.identity(), FunctionUtil::mergeFirst)).values());
    }

    /**
     * 排序
     *
     * @param collection
     *            集合
     * @param sortField
     *            排序字段
     *
     * @return {@link List }<{@link T }>
     */
    public static <T> List<T> sort(Collection<T> collection, Function<T, ? extends Comparable<Object>> sortField) {
        return sort(collection, false, sortField);
    }

    /**
     * 排序
     *
     * @param collection
     *            集合
     * @param reverse
     *            是否反转
     * @param sortFields
     *            排序字段
     *
     * @return {@link List }<{@link T }>
     */
    @SafeVarargs
    public static <T> List<T> sort(Collection<T> collection, boolean reverse,
        Function<T, ? extends Comparable<Object>>... sortFields) {
        if (EmptyUtil.isEmpty(collection)) {
            return Collections.emptyList();
        }
        List<T> sortedList = collection instanceof List ? (List<T>)collection : new ArrayList<>(collection);
        if (EmptyUtil.isEmpty(sortedList)) {
            return sortedList;
        }
        Comparator<T> comparator = comparator(sortFields);
        if (reverse) {
            comparator = comparator.reversed();
        }
        sortedList.sort(comparator);
        return sortedList;
    }

    /**
     * 比较器
     *
     * @param sortFields
     *            排序字段
     *
     * @return {@link Comparator }<{@link T }>
     */
    @SafeVarargs
    public static <T> Comparator<T> comparator(Function<T, ? extends Comparable<Object>>... sortFields) {
        if (EmptyUtil.isEmpty(sortFields)) {
            return (o1, o2) -> 0; // 默认比较器
        }
        return Arrays.stream(sortFields).map(Comparator::comparing).reduce(Comparator::thenComparing)
            .orElse((o1, o2) -> 0); // 默认比较器
    }

    /**
     * 计算集合中某一个属性值
     *
     * @param collection
     *            集合
     * @param getValue
     *            获取集合属性值
     * @param accumulator
     *            属性值计算
     * @param other
     *            其他
     *
     * @return {@link R }
     */
    public static <T, R> R reduce(Collection<T> collection, Function<T, R> getValue, BinaryOperator<R> accumulator,
        Supplier<? extends R> other) {
        if (EmptyUtil.isEmpty(collection)) {
            return other.get();
        }
        return collection.stream().map(getValue).reduce(accumulator).orElseGet(other);
    }

    /**
     * 获取首个元素
     *
     * @param collection
     *            集合
     *
     * @return {@link Optional }<{@link T }>
     */
    public static <T> Optional<T> first(Collection<T> collection) {
        if (EmptyUtil.isEmpty(collection)) {
            return Optional.empty();
        }
        return collection.stream().findFirst();
    }

    /**
     * 根据逗号分隔
     *
     * @param str
     *            str
     *
     * @return {@link List }<{@link String }>
     */
    public static List<String> split(String str) {
        return split(str, String::trim);
    }

    /**
     * 根据逗号分隔
     *
     * @param str
     *            str
     *
     * @return {@link List }<{@link Long }>
     */
    public static <R> List<R> split(String str, Function<String, R> mapper) {
        return split(str, mapper, BasicConstant.SEPARATOR);
    }

    /**
     * 根据指定分隔符分隔
     *
     * @param str
     *            str
     * @param mapper
     *            映射器
     * @param separator
     *            分离器
     *
     * @return {@link List }<{@link R }>
     */
    public static <R> List<R> split(String str, Function<String, R> mapper, String separator) {
        if (EmptyUtil.isEmpty(str)) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(separator)).map(mapper).collect(Collectors.toList());
    }

    /**
     * 英文逗号拼接
     *
     * @param collection
     *            收集
     * @param stringFunction
     *            字符串函数
     *
     * @return {@link String }
     */
    public static <T> String joining(Collection<T> collection, Function<T, String> stringFunction) {
        return joining(collection, stringFunction, BasicConstant.SEPARATOR);
    }

    /**
     * 将集合中的元素通过指定的函数转换为字符串，并用指定的分隔符连接起来。
     *
     * @param collection
     *            需要被处理的集合，不能为null且不可为空。
     * @param stringFunction
     *            一个函数，用于将集合中的元素转换为字符串。
     * @param separator
     *            用于连接转换后的字符串的分隔符。
     *
     * @return 由集合中元素转换后的字符串组成的字符串，元素之间以分隔符分隔。如果集合为空，则返回空字符串。
     */
    public static <T> String joining(Collection<T> collection, Function<T, String> stringFunction, String separator) {
        // 如果集合为null或空，直接返回空字符串
        if (EmptyUtil.isEmpty(collection)) {
            return BasicConstant.EMPTY;
        }
        // 使用stream流将集合中的元素转换为字符串，并用指定的分隔符连接
        return collection.stream().map(stringFunction).collect(Collectors.joining(separator));
    }

    /**
     * 将列表元素转换为映射，其中键是元素在列表中的索引 此方法的目的是为了快速通过索引访问列表元素，提高查询效率
     *
     * @param list
     *            列表，其中包含要转换为映射的元素
     *
     * @return 返回一个映射，键是元素在列表中的索引，值是列表元素
     */
    public static <T> Map<Integer, T> indexToMap(List<T> list) {
        // 检查列表是否为空或为null，如果是，则返回一个空映射
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        // 初始化一个HashMap，大小为列表大小的0.75倍，以减少重新散列的可能性
        Map<Integer, T> map = new HashMap<>((int)(list.size() * 0.75));
        // 遍历列表，从索引1开始，将元素及其索引放入映射中
        for (int i = 1; i < list.size(); i++) {
            map.put(i, list.get(i));
        }
        // 返回构建的映射
        return map;
    }

    /**
     * 将集合转换为映射，其中映射的键和值由提供的函数生成 此方法允许从一个集合创建一个映射，通过指定的函数生成键和值 如果集合中的元素生成了相同的键，将使用指定的合并函数进行合并
     *
     * @param collection
     *            要转换的集合
     * @param keyFunction
     *            用于生成映射键的函数
     * @param valueFunction
     *            用于生成映射值的函数
     *
     * @return {@link Map }<{@link K }, {@link V }>
     */
    public static <K, V, T> Map<K, V> toMap(Collection<T> collection, Function<T, K> keyFunction,
        Function<T, V> valueFunction) {
        // 使用流处理集合，通过提供的函数收集键和值，并使用指定的合并策略处理键冲突
        return toMap(collection, keyFunction, valueFunction, (v1, v2) -> v2, HashMap::new);
    }

    /**
     * 将给定的集合转换成一个Map，通过提供的keyMapper和valueMapper函数来提取键和值，当遇到重复的键时，使用mergeFunction来合并值。 该方法使用提供的Map供应商来创建新的Map实例。
     *
     * @param items
     *            集合对象，将被转换为Map。如果为null或空集合，将返回一个空的Map。
     * @param keyMapper
     *            一个函数，用于根据输入项生成Map的键。
     * @param valueMapper
     *            一个函数，用于根据输入项生成Map的值。
     * @param mergeFunction
     *            当遇到重复键时应用的合并函数，用于合并值。
     * @param supplier
     *            一个供应函数，用于提供一个新的空Map实例。
     * @param <T>
     *            输入项的类型。
     * @param <K>
     *            Map键的类型。
     * @param <V>
     *            Map值的类型。
     *
     * @return 转换后的Map，其键值对由输入集合及提供的函数确定。
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> items, Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends V> valueMapper, BinaryOperator<V> mergeFunction,
        Supplier<? extends Map<K, V>> supplier) {
        // 如果输入项为null或空集合，直接返回一个空的Map。
        if (items == null || items.isEmpty()) {
            return EmptyUtil.emptyMap();
        }
        // 使用提供的函数和策略将集合流转换为Map。
        return items.stream()
            .filter(item -> item != null && keyMapper.apply(item) != null && valueMapper.apply(item) != null)
            .collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction, supplier));
    }

    /**
     * 合并
     *
     * @param elements
     *            元素
     *
     * @return {@link List}<{@link T}>
     */
    @SafeVarargs
    public static <T> List<T> merge(List<T>... elements) {
        if (elements == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(elements).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * 计算一个列表的平均值
     *
     * @param list
     *            整型列表
     *
     * @return 平均数
     */
    public static <T, E extends Number> double average(List<T> list, Function<T, E> getElement,
        BinaryOperator<E> accumulator, Function<E, Double> toDouble) {
        if (EmptyUtil.isEmpty(list)) {
            return 0;
        }
        AtomicReference<E> sum = new AtomicReference<>();
        if (list instanceof RandomAccess) {
            sum.set(getElement.apply(list.get(0)));
            if (list.size() > 1) {
                // 对于可以随机存取的，采用下标访问
                for (int i = 1; i <= list.size(); i++) {
                    E value = getElement.apply(list.get(i));
                    sum.set(accumulator.apply(sum.get(), value));
                }
            }
        } else {
            // 对于顺序存取的，采用迭代访问
            list.stream().map(getElement).reduce(accumulator)
                .orElseThrow(() -> new IllegalArgumentException("list is empty"));
        }
        return 1.0 * toDouble.apply(sum.get()) / list.size();
    }

    /**
     * 安全的获取数组的长度
     *
     * @param array
     *            数组
     *
     * @return 数组的长度，为null时返回0
     */
    public static <T> int size(T[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * 安全的获取数组的长度
     *
     * @param array
     *            数组
     *
     * @return 数组的长度，为null时返回0
     */
    public static int size(byte[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * 安全的获取集合的长度
     *
     * @param array
     *            集合
     *
     * @return 集合的长度，为null时返回0
     */
    public static int size(Collection<?> array) {
        return array == null ? 0 : array.size();
    }

    /**
     * 安全的获取map的长度
     *
     * @param array
     *            map
     *
     * @return map的长度，为null时返回0
     */
    public static int size(Map<?, ?> array) {
        return array == null ? 0 : array.size();
    }

    /**
     * 生成指定类型指定长度的一维数组
     *
     * @param arrayType
     *            数组类型
     * @param len
     *            长度
     *
     * @return 指定类型指定长度的一维数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(Class<?> arrayType, int len) {
        return (T[])Array.newInstance(arrayType, len);
    }

    /**
     * 判断数组长度是否相同
     *
     * @param arr0
     *            数组1
     * @param arr1
     *            数组2
     * @param <T>
     *            数组实际类型
     * @param <F>
     *            数组实际类型
     *
     * @return 返回true表示数组长度一致
     */
    public static <T, F> boolean sizeEquals(T[] arr0, F[] arr1) {
        return size(arr0) == size(arr1);
    }

    /**
     * 根据给定的节点列表和起始节点ID构建树形结构 此方法用于从一个扁平的节点列表中构建出树形结构，便于层次展示或处理
     *
     * @param list
     *            节点列表，每个节点都是一个TreeNode的实现
     * @param startId
     *            起始节点的ID，用于定位树的根节点
     *
     * @return 返回构建好的树形结构列表
     */
    public static <R extends TreeNode<I, Integer, R>, I> List<R> tree(List<R> list, I startId) {
        // 检查列表是否为空，如果为空则直接返回空列表
        if (EmptyUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 初始化每个节点的子节点列表，如果节点的子节点列表为null，则为其创建一个新的空子节点列表
        // 这一步确保在后续构建树形结构时，每个节点都有一个可以添加子节点的列表
        list.stream().filter(l -> l.getChildren() == null).forEach(l -> l.setChildren(new ArrayList<>()));

        // 递归调用tree方法，构建树形结构
        // 这里通过方法句柄传递父节点ID获取方法、节点ID获取方法和子节点列表获取方法，以支持不同类型的TreeNode实现
        return tree(list, TreeNode::getParentId, TreeNode::getId, TreeNode::getChildren, startId);
    }

    /**
     * 将列表转换为树结构
     *
     * @param list
     *            列表，待转换为树结构的源数据集合。
     * @param parentGetter
     *            父ID的获取函数，用于从列表元素中获取父节点的ID。
     * @param idGetter
     *            当前节点ID的获取函数，用于从列表元素中获取当前节点的ID。
     * @param childGetter
     *            子节点列表的获取函数，用于从列表元素中获取子节点的集合。
     * @param startId
     *            开始节点ID，指定树结构的根节点ID。
     * @param <R>
     *            节点类型，列表元素的类型。
     * @param <I>
     *            ID类型，节点ID的类型。
     *
     * @return 转换后的树结构列表，返回根节点的集合。
     */
    public static <R, I> List<R> tree(List<R> list, Function<? super R, ? extends I> parentGetter,
        Function<? super R, ? extends I> idGetter, Function<? super R, List<R>> childGetter, I startId) {
        // 验证输入参数是否为空
        if (list == null || parentGetter == null || idGetter == null || childGetter == null) {
            throw new IllegalArgumentException("Input parameters must not be null.");
        }

        // 如果源数据集为空，直接返回空列表
        if (EmptyUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 根据指定的开始节点ID，筛选出根节点列表
        List<R> trees = list.stream().filter(node -> Objects.equals(parentGetter.apply(node), startId))
            .collect(Collectors.toList());

        // 如果没有找到根节点，直接返回空列表
        if (EmptyUtil.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 使用Map来缓存节点及其子节点列表，以提高后续构建树结构的效率
        Map<I, List<R>> map = list.stream().collect(Collectors.groupingBy(parentGetter));

        // 递归构建树结构
        trees.forEach(node -> buildTree(map, node, idGetter, childGetter));

        return trees;
    }

    /**
     * 基于给定的映射、当前节点及其getter函数，构建树结构。
     *
     * @param map
     *            映射表，用于存储节点标识符到节点对象的映射。
     * @param currentNode
     *            当前处理的节点对象，将以此节点作为树结构的构建起点。
     * @param idGetter
     *            一个函数，用于从节点对象中获取节点的标识符。
     * @param childGetter
     *            一个函数，用于从节点对象中获取该节点的子节点列表。
     *            <p>
     *            通过递归方式遍历映射表，根据当前节点及其getter函数，构建从当前节点开始的树结构。 首先，获取当前节点的标识符和其对应的子节点列表。然后，将找到的子节点添加到当前节点的子节点列表中。
     *            最后，对每个子节点递归调用此方法，以构建整棵树。
     */
    private static <R, I> void buildTree(Map<I, List<R>> map, R currentNode, Function<? super R, ? extends I> idGetter,
        Function<? super R, List<R>> childGetter) {
        // 获取当前节点的标识符
        I currentId = idGetter.apply(currentNode);

        // 寻找当前节点的所有子节点，若不存在则直接返回
        List<R> children = map.getOrDefault(currentId, Collections.emptyList());
        if (EmptyUtil.isEmpty(children)) {
            return;
        }

        // 将找到的子节点添加到当前节点的子节点列表中
        childGetter.apply(currentNode).addAll(children);

        // 递归处理每个子节点，构建子树
        for (R child : children) {
            buildTree(map, child, idGetter, childGetter);
        }
    }

    /**
     * 比较繁琐 复杂的比较器?? 但是相对安全 创建一个比较器，该比较器基于提供的函数对对象进行比较 这些函数提取出对象中用于比较的字段，字段必须是可比较的
     *
     * @param <T>
     *            要比较的对象类型
     * @param fields
     *            一个或多个函数，用于提取出对象中用于比较的字段
     *
     * @return 一个比较器，用于比较类型为T的对象
     */
    @SafeVarargs
    public static <T> Comparator<T> complexComparator(Function<T, ? extends Comparable<Object>>... fields) {
        if (EmptyUtil.isEmpty(fields)) {
            return (o1, o2) -> 0;
        }
        return (o1, o2) -> {
            // 如果两个对象都为null，则视为相等
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                // 如果只有第一个对象为null，则第一个对象小于第二个对象
                return -1;
            } else if (o2 == null) {
                // 如果只有第二个对象为null，则第二个对象小于第一个对象
                return 1;
            }
            // 遍历每个字段提取函数，对对象的对应字段进行比较
            for (Function<T, ? extends Comparable<Object>> field : fields) {
                Comparable<Object> value1 = field.apply(o1);
                Comparable<Object> value2 = field.apply(o2);
                // Class<?> clazz1 = ClassUtil.getGenericParameter(value1);
                // Class<?> clazz2 = ClassUtil.getGenericParameter(value2);
                // if (!ClassUtil.compatible(clazz1, clazz2)) {
                // return 0;
                // }
                // 比较两个字段值
                int result = value1.compareTo(value2);
                if (result != 0) {
                    // 如果比较结果不为0，则返回该结果
                    return result;
                }
            }
            // 如果所有字段比较结果都为0，则视为相等
            return 0;
        };
    }

    /**
     * 比较值
     *
     * @param value1
     *            值1
     * @param value2
     *            值2
     *
     * @return int
     */
    private static int compareValues(Comparable<Object> value1, Comparable<Object> value2) {
        if (value1 == null || value2 == null) {
            return value1 == null ? (value2 == null ? 0 : -1) : 1;
        } else {
            return value1.compareTo(value2);
        }
    }
}
