package cn.hehouhui.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 字符串工具
 *
 * @author HeHui
 * @date 2024-11-27 10:38
 */
public class StrUtil {

    public static final StringMatch stringMatch = new KMP();

    private StrUtil() {
        throw new AssertionError();
    }

    /**
     * 将首字母大写
     *
     * @param arg
     *            指定字符串
     * @return 首字母大写后的字符串
     */
    public static String toFirstUpperCase(String arg) {
        return arg.substring(0, 1).toUpperCase() + arg.substring(1);
    }

    /**
     * 是否为数字
     *
     * @param str
     *            str
     *
     * @return boolean
     */
    public static boolean isNumber(final String str) {
        if (EmptyUtil.isEmpty(str)) {
            return false;
        }
        String numberStr = str.trim();
        if (str.length() > 1 && str.indexOf("-") == 0) {
            numberStr = str.substring(1);
        }
        return numberStr.matches("[0-9]+");
    }

    /**
     * 判断一个字符是否是数字
     *
     * @param c
     *            待判断的是字符
     *
     * @return 是否是数字字符
     */
    public static boolean isNumberChar(char c) {
        return ('0' <= c && c <= '9');
    }

    /**
     * 判断一个字符是否是大写字母
     *
     * @param c
     *            待判断的是字符
     *
     * @return 是否是大写字母字符
     */
    public static boolean isCapital(char c) {
        return ('A' <= c && c <= 'Z');
    }

    /**
     * 判断一个字符是否是小写字母
     *
     * @param c
     *            待判断的是字符
     *
     * @return 是否是小写字母字符
     */
    public static boolean isLowercase(char c) {
        return ('a' <= c && c <= 'z');
    }

    /**
     * 判断一个字符是否是字母
     *
     * @param c
     *            待判断的是字符
     *
     * @return 是否是字母字符
     */
    public static boolean isLetter(char c) {
        return (isLowercase(c) || isCapital(c));
    }

    /**
     * 把byte类型的数据转换成十六进制ASCII字符表示
     *
     * @param in
     *            待转化字节
     *
     * @return 十六进制ASCII字符表示
     */
    public static String hexStr(byte in) {
        char[] DigitStr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        char[] out = new char[2];
        out[0] = DigitStr[(in >> 4) & 0x0F]; // 取高4位
        out[1] = DigitStr[in & 0x0F]; // 取低4位

        return new String(out);
    }

    /**
     * 反转一个字符串
     *
     * @param label
     *            待反转字符串
     *
     * @return 字符串的反转序列
     */
    public static String reverse(String label) {
        if (EmptyUtil.isEmpty(label)) {
            return label;
        }
        return new StringBuffer(label).reverse().toString();
    }

    /**
     * 重复一个字符串n遍
     *
     * @param label
     *            待重复的字符串
     * @param n
     *            重复次数
     *
     * @return 重复后的字符串
     */
    public static String repeat(String label, int n) {
        if (EmptyUtil.isEmpty(label)) {
            return label;
        }
        if (n < 1) {
            return label;
        }
        return label.repeat(n);
    }

    /**
     * 判断一个字符串是可以由另一个字符串经过N次拼接得到
     *
     * @param label
     *            待判断的字符串
     * @param pattern
     *            模式字符串
     *
     * @return 结果
     */
    public static boolean splicing(String label, String pattern) {
        if (EmptyUtil.isEmpty(label) || EmptyUtil.isEmpty(pattern)) {
            return false;
        }
        int patternLength = pattern.length();
        int labelLength = label.length();
        if (labelLength % patternLength != 0) {
            return false;
        }
        for (int i = 0; i < labelLength; i += patternLength) {
            if (!label.substring(i, i + patternLength).equals(pattern)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算一个字符串的最小正周期
     *
     * @param label
     *            待处理字符串
     *
     * @return 最小正周期
     */
    public static int minCycle(String label) {
        Objects.requireNonNull(label, "label cannot be null");
        if (label.isEmpty()) {
            return 1;
        }
        int length = label.length();
        if (length == 1) {
            return 1;
        }

        List<Integer> factors = getFactors(length);
        return findMinCycle(label, factors);
    }

    /**
     * 获取字符串长度的所有因子
     *
     * @param length
     *            字符串长度
     *
     * @return 因子列表
     */
    public static List<Integer> getFactors(int length) {
        Set<Integer> result = new TreeSet<>();
        if (length == 1) {
            result.add(1);
            return new ArrayList<>(result);
        }
        for (int i = 1; i <= Math.sqrt(length); i++) {
            if (length % i == 0) {
                result.add(i);
                if (i != length / i) {
                    result.add(length / i);
                }
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 根据因子列表找到最小正周期
     *
     * @param label
     *            待处理字符串
     * @param factors
     *            因子列表
     *
     * @return 最小正周期
     */
    public static int findMinCycle(String label, List<Integer> factors) {
        int length = label.length();
        for (int factor : factors) {
            String pattern = label.substring(0, factor);
            if (splicing(label, pattern)) {
                return length / factor;
            }
        }
        return 1;
    }

    /**
     * 将一个字符串循环左移n位
     *
     * @param label
     *            待移动的字符串
     * @param n
     *            移动位数
     *
     * @return {@link String }
     */
    public static String leftShift(String label, int n) {
        // 检查输入字符串是否为空
        if (label == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        // 处理空字符串或单字符字符串的情况
        if (label.isEmpty() || label.length() == 1) {
            return label;
        }
        int length = label.length();
        // 处理负数情况，并确保 n 在 [0, length) 范围内
        n = (n % length + length) % length;
        // 当 n 为 0 或者 n 是字符串长度的倍数时，直接返回原字符串
        if (n == 0) {
            return label;
        }
        // 执行字符串循环左移
        return label.substring(n, length) + label.substring(0, n);
    }

    /**
     * 将一个字符串循环右移n位
     *
     * @param label
     *            待移动的字符串
     * @param n
     *            移动位数
     *
     * @return {@link String } 结果字符串
     */
    public static String rightShift(String label, int n) {
        if (EmptyUtil.isEmpty(label) || label.length() == 1) {
            return label;
        }

        return leftShift(label, label.length() - n);
    }

    /**
     * 统计匹配串中模式串的个数
     *
     * @param match
     *            匹配串
     * @param pattern
     *            模式串
     *
     * @return int 个数
     */
    public static int countMatches(String match, String pattern) {
        return stringMatch.indexOfMatch(match, pattern).size();
    }

    /**
     * 将指定字符集的数据转换为另外一个字符集
     *
     * @param data
     *            数据
     * @param charset
     *            当前数据字符集
     * @param resultCharset
     *            结果数据集
     *
     * @return utf8字符集的数据
     */
    public static byte[] convert(byte[] data, Charset charset, Charset resultCharset) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (charset == null || resultCharset == null) {
            throw new IllegalArgumentException("Charsets cannot be null");
        }
        if (resultCharset.equals(charset)) {
            return data;
        }
        return convertCharset(data, charset, resultCharset);
    }

    private static byte[] convertCharset(byte[] data, Charset charset, Charset resultCharset) {
        CharBuffer decode = charset.decode(ByteBuffer.wrap(data));
        ByteBuffer encodeBuffer = resultCharset.encode(decode);
        return Arrays.copyOf(encodeBuffer.array(), encodeBuffer.limit());
    }

    /**
     * 查找第count个指定code在str中的位置
     * <p>
     * 例如str是123132412，code是3，count是2，那么返回的index是4；
     *
     * @param str
     *            字符串
     * @param code
     *            要搜寻的代码
     * @param count
     *            需要搜寻第几个
     *
     * @return int 索引
     */
    public static int indexOf(String str, String code, int count) {
        if (str == null || code == null) {
            throw new IllegalArgumentException("str and code cannot be null");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        int foundCount = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.substring(i, i + 1).equals(code)) {
                foundCount++;
                if (foundCount == count) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 将目标字符串重复count次返回
     *
     * @param str
     *            目标字符串
     * @param count
     *            次数
     *
     * @return 目标字符串重复count次结果，例如目标字符串是test，count是2，则返回testtest，如果count是3则返回testtesttest
     */
    public static String copy(String str, int count) {
        if (str == null) {
            throw new NullPointerException("原始字符串不能为null");
        }

        if (count <= 0) {
            throw new IllegalArgumentException("次数必须大于0");
        }
        if (count == 1) {
            return str;
        }
        if (count == 2) {
            return str + str;
        }

        return str.repeat(count);
    }

    /**
     * 如果源数据为空，则返回默认数据
     *
     * @param src
     *            源数据
     * @param defaultStr
     *            默认字符串
     *
     * @return 如果源数据为空，则返回默认数据
     */
    public static String getOrDefault(String src, String defaultStr) {
        return EmptyUtil.isEmpty(src) ? defaultStr : src;
    }

    /**
     * 格式化字符串，使用{}作为占位符
     *
     * @param msg
     *            字符串模板
     * @param args
     *            模板参数
     *
     * @return 格式化后的字符串
     */
    public static String format(String msg, Object... args) {
        if (EmptyUtil.isEmpty(msg)) {
            return "";
        }
        return String.format(msg, args);
    }

    /**
     * 将字符串前后的指定数据去除
     *
     * @param data
     *            字符串
     * @param trim
     *            要去除的数据
     *
     * @return 处理后的数据，例如数据是123abc123，trim是123，那么处理完毕后返回abc
     */
    public static String trim(String data, String trim) {
        Assert.argNotBlank(data, "data");
        Assert.argNotBlank(trim, "trim");

        String result = data;
        while (result.startsWith(trim)) {
            result = result.substring(trim.length());
        }

        while (result.endsWith(trim)) {
            result = result.substring(0, result.length() - trim.length());
        }

        return result;
    }

    /**
     * 将字符串前后的指定数据去除
     *
     * @param data
     *            字符串
     * @param trim
     *            要去除的数据
     *
     * @return 处理后的数据，例如数据是123abc123，trim是123，那么处理完毕后返回abc
     */
    public static String trim(String data, char trim) {
        return trim(data, Character.toString(trim));
    }

    /**
     * 如果字符串长度小于指定长度，则在左边补上指定字符
     *
     * @param str
     *            字符串
     * @param len
     *            目标长度
     * @param pad
     *            补上的字符
     *
     * @return 补齐后的字符串
     */
    public static String leftPadding(String str, int len, char pad) {
        int strLen = str.length();
        if (strLen >= len) {
            return str;
        }
        char[] chars = new char[len - strLen];
        Arrays.fill(chars, pad);
        return new String(chars) + str;
    }

    /**
     * 如果字符串长度小于指定长度，则在右边补上指定字符
     *
     * @param str
     *            字符串
     * @param len
     *            目标长度
     * @param pad
     *            补上的字符
     *
     * @return 补齐后的字符串
     */
    public static String rightPadding(String str, int len, char pad) {
        int strLen = str.length();
        if (strLen >= len) {
            return str;
        }
        char[] chars = new char[len - strLen];
        Arrays.fill(chars, pad);
        return str + new String(chars);
    }

    /**
     * 字符串匹配
     *
     * @author HEHH
     * @date 2024/11/27
     */
    public interface StringMatch {

        /**
         * 获得模式串在匹配串中所有位置
         *
         * @param match
         *            匹配串
         * @param pattern
         *            模式串
         *
         * @return {@link List }<{@link Integer }> 所有存在的位置
         */
        List<Integer> indexOfMatch(String match, String pattern);
    }

    /**
     * 基于KMP算法的字符串匹配实现
     *
     * @author HEHH
     * @date 2024/11/27
     */
    private static class KMP implements StringMatch {

        /**
         * 获得模式串在匹配串中所有位置
         *
         * @param match
         *            匹配串
         * @param pattern
         *            模式串
         *
         * @return {@link List }<{@link Integer }> 所有存在的位置
         */
        @Override
        public List<Integer> indexOfMatch(final String match, final String pattern) {
            try {
                if (EmptyUtil.isEmpty(match) || EmptyUtil.isEmpty(pattern)) {
                    return Collections.emptyList();
                }

                List<Integer> indexList = new ArrayList<>();
                char[] t = match.toCharArray();
                char[] p = pattern.toCharArray();
                int[] next = getNext(pattern);

                if (next == null) {
                    return indexList; // 如果 getNext 返回 null，直接返回空列表
                }

                int indexT = 0;
                int indexP = 0;
                while (indexT < t.length) {
                    if (t[indexT] == p[indexP]) {
                        indexP++;
                        indexT++;
                    } else {
                        if (indexP > 0) {
                            indexP = next[indexP - 1];
                        } else {
                            indexT++;
                        }
                    }

                    if (indexP == p.length) {
                        indexList.add(indexT - indexP);
                        indexP = 0;
                    }
                }

                return indexList;
            } catch (Exception e) {
                // 记录日志或进行其他处理
                return new ArrayList<>();
            }
        }

        /**
         * 获得字符串中的每个最优前缀子字符串中的 最长的最优前缀等于最优后缀的长度
         *
         * @param text
         *            待计算的字符串
         *
         * @return {@link int[] } 返回最长的最优前缀等于最优后缀的长度数组
         */
        private int[] getNext(String text) {
            if (EmptyUtil.isEmpty(text)) {
                return null;
            }

            int textLength = text.length();
            int[] lengths = new int[textLength];

            computePrefixLengths(text, lengths);

            return lengths;
        }

        /**
         * 计算字符串的前缀长度数组 该方法用于计算给定字符串的每个子串的最长相同前后缀的长度，用于KMP算法的预处理阶段
         *
         * @param text
         *            输入的字符串，用于计算前缀长度
         * @param lengths
         *            一个整型数组，用于存储每个子串的最长相同前后缀的长度
         */
        private void computePrefixLengths(String text, int[] lengths) {
            // 初始化当前最长相同前后缀的长度为0
            int len = 0;
            // 从字符串的第二个字符开始遍历
            for (int i = 1; i < text.length();) {
                // 如果当前字符与当前最长相同前后缀的下一个字符匹配
                if (text.charAt(i) == text.charAt(len)) {
                    // 增加最长相同前后缀的长度
                    len++;
                    // 更新当前位置的最长相同前后缀的长度
                    lengths[i] = len;
                    // 移动到下一个字符
                    i++;
                } else {
                    // 如果当前没有匹配的字符
                    if (len != 0) {
                        // 回退到当前最长相同前后缀的下一个字符开始的位置
                        len = lengths[len - 1];
                    } else {
                        // 当前位置没有相同前后缀，设置长度为0，并移动到下一个字符
                        lengths[i] = 0;
                        i++;
                    }
                }
            }
        }

    }

    /**
     * 基于朴素字符串匹配算法的字符串匹配实现
     *
     * @author HEHH
     * @date 2024/11/27
     */
    private static class NativeMatch implements StringMatch {

        /**
         * 查找字符串中所有匹配模式的索引 该方法通过遍历字符串来寻找与给定模式匹配的所有子字符串，并返回这些子字符串的起始索引
         *
         * @param match
         *            待搜索的字符串
         * @param pattern
         *            需要查找的模式字符串
         *
         * @return 包含所有匹配模式的子字符串起始索引的列表如果输入为空，则返回空列表
         */
        @Override
        public List<Integer> indexOfMatch(String match, String pattern) {
            // 检查输入字符串和模式是否为空，如果任一为空，则直接返回空列表
            if (EmptyUtil.isEmpty(match) || EmptyUtil.isEmpty(pattern)) {
                return Collections.emptyList();
            }

            // 初始化列表，用于存储所有匹配模式的子字符串的起始索引
            List<Integer> indexList = new ArrayList<>();
            // 将输入字符串和模式转换为字符数组，便于逐字符比较
            char[] t = match.toCharArray();
            char[] p = pattern.toCharArray();
            // 获取输入字符串和模式的长度
            int tLength = t.length;
            int pLength = p.length;

            // 遍历输入字符串，直到剩余的字符串长度小于模式长度时停止
            for (int i = 0; i <= tLength - pLength; i++) {
                // 遍历模式，逐字符与输入字符串中的子字符串进行比较
                for (int j = 0; j < pLength; j++) {
                    // 如果当前字符匹配，则继续比较后续字符
                    if (t[i + j] == p[j]) {
                        // 如果到达模式末尾，说明找到一个完全匹配的子字符串，将其起始索引添加到列表中
                        if (j == pLength - 1) {
                            indexList.add(i);
                        }
                        continue;
                    }

                    // 如果任何字符不匹配，则跳出内层循环，继续搜索下一个子字符串
                    break;
                }
            }

            // 返回所有匹配模式的子字符串起始索引的列表
            return indexList;
        }
    }
}
