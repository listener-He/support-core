package cn.hehouhui.util;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;

/**
 * 线性计算工具
 *
 * @author HEHH
 * @date 2024/11/01
 */
public class LinearComputeUtil {

    /**
     * 根据给定的列表和两个函数，生成一个映射，该映射的键是一个谓词，用于判断列表中的元素是否在某个区间内， 值是列表中的元素本身。
     *
     * @param list
     *            包含T类型元素的列表。
     * @param left
     *            一个函数，用于确定区间的左端点。
     * @param right
     *            一个函数，用于确定区间的右端点。
     *
     * @return {@link Map }<{@link Predicate }<{@link E }>, {@link T }> 返回一个映射，键是谓词，用于判断列表中的元素是否在指定的区间内，值是列表中的元素。
     */
    public static <E extends Comparable<E>, T> Map<Predicate<E>, T> section(List<T> list,
        Function<? super T, ? extends E> left, Function<? super T, ? extends E> right) {
        if (EmptyUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        // 使用Stream API收集列表中的元素到一个映射中
        // 映射的键是一个谓词，用于判断元素是否在区间内
        // 映射的值是列表中的元素本身
        return list.stream().collect(Collectors.toMap(entry -> {
            // 生成一个谓词，用于判断给定的值是否在当前元素确定的区间内
            return value -> value.compareTo(left.apply(entry)) >= 0 && value.compareTo(right.apply(entry)) <= 0;
        }, Function.identity()));
    }

    /**
     * 连续区间分段线性函数，传入区间终点和区间内权重，返回此分段线性函数(区间连续，所以区间结尾是否取值不影响结果)，权重为空返回原值,超出最后一个区间取值为最后一个区间权重 求一个 跳跃（可去）间断点函数的积分函数是连续（可导）的
     * f(x)= a (x < interval1) 或 f(x)= a (x <= interval1) f(x)= b (x < interval2) 或 f(x)= b (x <= interval2) 的不定积分 F（x）=
     * a*x (x <= interval1) F（x）= a*interval1 + b*(x-interval1) = b*x - (b-a)*interval1 (interval1 <=x < interval2)
     * F(interval1+0) = F(interval1-0)
     *
     * @param intervalEndWeightMap
     *            区间终点和权重的映射，用于定义分段线性函数的各个区间和对应的斜率
     *
     * @return {@link UnaryOperator }<{@link BigDecimal }> 返回一个分段函数，它接受一个BigDecimal并应用分段线性函数计算结果
     */
    public static UnaryOperator<BigDecimal> piecewiseLinear(Map<BigDecimal, BigDecimal> intervalEndWeightMap) {
        // 检查输入的映射是否为空或无效，如果是，则记录错误并返回身份操作符（不做任何操作）
        if (intervalEndWeightMap == null || intervalEndWeightMap.isEmpty()) {
            return UnaryOperator.identity();
        }
        // 将输入的映射转换为不可变映射，以确保线程安全和防止修改
        Map<BigDecimal, BigDecimal> weightMap = Collections.unmodifiableMap(intervalEndWeightMap);
        // 将区间终点排序并转换为数组，以便后续处理
        BigDecimal[] intervalEnds =
            weightMap.keySet().stream().sorted(BigDecimal::compareTo).toArray(BigDecimal[]::new);
        // 初始化快速扣除数组，用于存储每个区间内的快速扣除值
        BigDecimal[] fastDeduction = new BigDecimal[intervalEnds.length];
        // 第一个区间的快速扣除值为0
        fastDeduction[0] = ZERO;
        // 计算每个区间的快速扣除值，这是通过计算每个区间内的线性函数的积分实现的
        for (int i = 1; i < intervalEnds.length; i++) {
            fastDeduction[i] = weightMap.get(intervalEnds[i]).multiply(intervalEnds[i - 1]).subtract(
                weightMap.get(intervalEnds[i - 1]).multiply(intervalEnds[i - 1]).subtract(fastDeduction[i - 1]));
        }
        // 返回一个函数，该函数接受一个BigDecimal x，并根据分段线性函数计算结果
        return x -> {
            // 使用二分查找确定x所属的区间
            int indexOf = Arrays.binarySearch(intervalEnds, x);
            // 如果找到确切的匹配项或x落在某个区间内，则计算相应的分段线性函数值
            indexOf = indexOf >= 0 ? indexOf : Math.min(intervalEnds.length - 1, -indexOf - 1);
            // 根据找到的区间计算分段线性函数的值，并返回结果
            return weightMap.get(intervalEnds[indexOf]).multiply(x).subtract(fastDeduction[indexOf]);
        };
    }

    /**
     * 连续区间分段线性函数，传入区间终点和区间内权重，返回此分段线性函数(区间连续，所以区间结尾是否取值不影响结果)，权重为空返回原值,负数结果全为0 求一个 跳跃（可去）间断点函数的积分函数是连续（可导）的 f(x)= a (x
     * < interval1) 或 f(x)= a (x <= interval1) f(x)= b (x < interval2) 或 f(x)= b (x <= interval2) 的不定积分 F（x）= a*x (x <=
     * interval1) F（x）= a*interval1 + b*(x-interval1) = b*x - (b-a)*interval1 (interval1 <=x < interval2) F(interval1+0)
     * = F(interval1-0)
     *
     * @param intervalEndWeightMap
     *            区间和权重映射，仅传入区间终点（默认不包含），区间必须连续
     *
     * @return {@link BiFunction }<{@link BigDecimal }, {@link BigDecimal }, {@link BigDecimal }> 分段函数(区间计算)
     */
    public static BiFunction<BigDecimal, BigDecimal, BigDecimal>
        piecewiseIntervalPositive(Map<BigDecimal, BigDecimal> intervalEndWeightMap) {

        Map<BigDecimal,
            BigDecimal> positiveInterval = Optional.ofNullable(intervalEndWeightMap).orElse(Collections.emptyMap())
                .entrySet().stream().filter(e -> e.getKey().compareTo(ZERO) > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<BigDecimal, BigDecimal> weightMap = new HashMap<>(positiveInterval);
        weightMap.put(ZERO, ZERO);

        UnaryOperator<BigDecimal> function = piecewiseLinear(weightMap);
        return (x1, x2) -> function.apply(x2).subtract(function.apply(x1));
    }

    /**
     * 线性区间
     *
     * @param weight
     *            重量
     *
     * @return {@link BiFunction }<{@link BigDecimal }, {@link BigDecimal }, {@link BigDecimal }>
     */
    public static BiFunction<BigDecimal, BigDecimal, BigDecimal> linearInterval(BigDecimal weight) {
        return (x1, x2) -> weight.multiply(x2.subtract(x1));
    }

    /**
     * 线性区间
     *
     * @param intervalEndWeightMap
     *            区间末端权重
     *
     * @return {@link BiFunction }<{@link BigDecimal }, {@link BigDecimal }, {@link BigDecimal }>
     */
    public static BiFunction<BigDecimal, BigDecimal, BigDecimal>
        linearInterval(Map<BigDecimal, BigDecimal> intervalEndWeightMap) {
        BigDecimal weight = intervalEndWeightMap.values().stream().findAny().orElse(ZERO);
        return linearInterval(weight);
    }
}
