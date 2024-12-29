package cn.hehouhui.util;

/**
 * 人民币工具
 *
 * @author HeHui
 * @date 2024-11-26 22:38
 */
public class RmbUtil {

    static String[] HanDigiStr = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    static String[] HanDiviStr = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿",
        "拾", "佰", "仟", "万", "拾", "佰", "仟"};

    private RmbUtil() {
        throw new AssertionError();
    }

    /**
     * 数字转中文人民币字符
     *
     * @param val
     *            数字
     *
     * @return {@link String }
     */
    public static String numToRMBStr(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return "非法输入!";
        }
        String SignStr = val < 0.0D ? "负" : "";
        val = Math.abs(val);
        if (val > 100000000000000.0D) {
            return "数值位数过大!";
        }
        long temp = Math.round(val * 100.0D);
        long integer = temp / 100L;
        long fraction = temp % 100L;
        int jiao = (int)fraction / 10;
        int fen = (int)fraction % 10;

        String tailStr = "";
        if (jiao == 0 && fen == 0) {
            tailStr = "整";
        } else {
            if (jiao != 0) {
                tailStr += HanDigiStr[jiao] + "角";
            }
            if (fen != 0) {
                tailStr += HanDigiStr[fen] + "分";
            }
        }

        try {
            return SignStr + toHanStr(String.valueOf(integer)) + "元" + tailStr;
        } catch (Exception e) {
            return "转换失败: " + e.getMessage();
        }
    }

    /**
     * 数字转字符串
     *
     * @param digitStr
     *            数字str
     *
     * @return {@link String }
     */
    public static String digitToString(String digitStr) {
        if (EmptyUtil.isEmpty(digitStr)) {
            return digitStr;
        }
        String[] digit = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[] weight = {"", "", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟"};
        StringBuilder retDigit = new StringBuilder();
        char[] chars = digitStr.toCharArray();
        int dot = -1;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '.') {
                dot = i;
                break;
            }
        }

        // 检查输入是否为空或无效
        if (digitStr.isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }

        String zs;
        String xs = "";
        if (dot > 0) {
            zs = new String(chars, 0, dot).replaceFirst("^0+", "");
            xs = new String(chars, dot + 1, chars.length - dot - 1);
        } else {
            zs = new String(chars).replaceFirst("^0+", "");
        }

        appendDigits(retDigit, zs, digit, weight);
        if (dot > 0) {
            retDigit.append("点");
            appendDigits(retDigit, xs, digit, new String[0]);
        }
        return retDigit.toString();
    }

    /**
     * 追加数字
     *
     * @param retDigit
     *            字符缓存流
     * @param part
     *            零部位
     * @param digit
     *            数字
     * @param weight
     *            重量
     */
    private static void appendDigits(StringBuilder retDigit, String part, String[] digit, String[] weight) {
        int w;
        boolean flag = false;
        for (int i = 0; i < part.length(); i++) {
            w = Character.getNumericValue(part.charAt(i));
            if (w == 0) {
                if (part.length() - i == 5 && weight.length > 4) {
                    retDigit.append("万");
                }
                if (part.length() - i == 9 && weight.length > 8) {
                    retDigit.append("亿");
                }
                flag = true;
            } else {
                if (flag) {
                    retDigit.append("零");
                    flag = false;
                }
                retDigit.append(digit[w]);
                if (weight.length > 0) {
                    retDigit.append(weight[Math.min(part.length() - i, weight.length - 1)]);
                }
            }
        }
    }

    /**
     * 将正整数转换为汉字表示
     *
     * @param numberStr
     *            数字字符串
     *
     * @return 转换后的汉字字符串
     */
    private static String toHanStr(String numberStr) {
        if (EmptyUtil.isEmpty(numberStr)) {
            return numberStr;
        }
        try {
            // 去除首尾空格
            numberStr = numberStr.trim();

            // 验证输入是否只包含数字字符
            if (!numberStr.matches("\\d+")) {
                return "输入含非数字字符!";
            }

            StringBuilder result = new StringBuilder();
            boolean lastZero = false;
            boolean hasValue = false;

            int length = numberStr.length();
            if (length > 15) {
                return "数值过大!";
            }

            for (int i = length - 1; i >= 0; i--) {
                int digit = numberStr.charAt(length - i - 1) - '0';

                if (digit != 0) {
                    if (lastZero) {
                        result.append(HanDigiStr[0]); // 添加零
                    }
                    if ((digit != 1) || (i % 4 != 1) || (i != length - 1)) {
                        result.append(HanDigiStr[digit]); // 添加数字对应的汉字
                    }
                    result.append(HanDiviStr[i]); // 添加单位
                    hasValue = true;
                } else if ((i % 8 == 0) || ((i % 8 == 4) && (hasValue))) {
                    result.append(HanDiviStr[i]); // 添加单位
                }
                if (i % 8 == 0) {
                    hasValue = false;
                }
                lastZero = (digit == 0) && (i % 4 != 0);
            }

            if (result.isEmpty()) {
                return HanDigiStr[0]; // 返回零
            }

            return result.toString();
        } catch (Exception e) {
            return "处理过程中发生错误: " + e.getMessage();
        }
    }

}
