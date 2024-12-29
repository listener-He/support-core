/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cn.hehouhui.util;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 身份证工具类
 *
 * @author JoeKerouac
 * @date 2022-10-17 19:27
 * @since 2.0.0
 */
public class IDCardUtil {

    private IDCardUtil() {
        throw new AssertionError();
    }

    /**
     * 加权表
     */
    private static int[] POWER = new int[] {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 加权因子
     */
    private static char[] DIVISOR = new char[] {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 身份证正则
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("[0-9]{17}[0-9xX]");

    /**
     * 检查身份证号是否符合格式
     *
     * @param idCard
     *            身份证号
     *
     * @return 如果身份证号符合身份证格式则返回<code>true</code>
     */
    public static boolean check(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }

        Matcher matcher = ID_CARD_PATTERN.matcher(idCard);
        if (!matcher.matches()) {
            // 格式不对
            return false;
        }

        int mod = calcMod(idCard);
        int calcLast = DIVISOR[mod];
        char last = idCard.charAt(17);
        if (last == 'x' || last == 'X') {
            last = 'X';
        }
        if (last != calcLast) {
            // 格式不对 加权码错误
            return false;
        }
        return true;
    }

    /**
     * 获取用于生日
     *
     * @param idCard
     *            身份证号
     *
     * @return 生日，格式yyyyMMdd
     */
    public static String getBirthday(String idCard) {
        if (idCard == null || idCard.length() < 14) {
            return null;
        }
        return idCard.substring(6, 14);
    }

    /**
     * 获取身份证的区域编码
     *
     * @param idCard
     *            身份证
     *
     * @return 区域编码
     */
    public static String getAreaCode(String idCard) {
        if (idCard == null || idCard.length() < 6) {
            return null;
        }
        return idCard.substring(0, 6);
    }

    /**
     * 获取用户性别，0是女，1是男
     *
     * @param idCard
     *            用户身份证号
     *
     * @return 用户性别
     */
    public static int getSex(String idCard) {
        if (idCard == null || idCard.length() < 17) {
            return -1;
        }
        // 判断身份证用户性别
        int sexInt = Integer.parseInt(idCard.substring(16, 17));
        return sexInt % 2;
    }

    /**
     * 获取用户年龄，如果2020.01.01出生，那么到2021.01.01都返回1岁，到2021.01.02就返回2岁了
     *
     * @param idCard
     *            用户身份证号
     *
     * @return int 用户年龄
     */
    public static int getAge(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new IllegalArgumentException("Invalid ID card format");
        }

        try {
            String birthDateString = idCard.substring(6, 14);
            LocalDate birthDate = LocalDate.parse(birthDateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate currentDate = LocalDate.now();

            int age = currentDate.getYear() - birthDate.getYear();
            if (currentDate.isBefore(birthDate.plusYears(age))) {
                age -= 1;
            }

            if (age < 0) {
                throw new IllegalArgumentException("Invalid ID card date");
            }

            return age;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ID card format", e);
        }
    }

    /**
     * 计算校验和
     *
     * @param card
     *            身份证号，长度不得低于17位，使用前17位计算校验和
     *
     * @return 校验和
     */
    private static int calcMod(String card) {
        // 生成最后一位校验码
        byte[] idCardByte = card.getBytes(Charset.defaultCharset());
        int sum = 0;
        for (int j = 0; j < 17; j++) {
            sum += (((int)idCardByte[j]) - 48) * POWER[j];
        }
        return sum % 11;
    }

}
