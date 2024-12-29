package cn.hehouhui.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP 工具
 *
 * @author HeHui
 * @date 2024-11-27 11:31
 */
public class IPUtil {

    private IPUtil() {
        throw new AssertionError();
    }

    /**
     * 将127.0.0.1形式的IP地址转换成十进制整数，这里没有进行任何错误处理
     *
     * @param strIp
     *            127.0.0.1形式的IP地址
     *
     * @return 127.0.0.1形式的IP地址所对应的整型IP
     */
    public static long ipToLong(String strIp) {
        if (EmptyUtil.isEmpty(strIp)) {
            return 0;
        }
        long[] ip = new long[4];

        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);

        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));

        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    /**
     * 将十进制整数形式转换成127.0.0.1形式的ip地址
     *
     * @param longIp
     *            整型IP
     *
     * @return 整型IP所对应的127.0.0.1形式的IP地址
     */
    public static String longToIP(long longIp) {
        StringBuilder sb = new StringBuilder();

        // 直接右移24位
        sb.append(longIp >>> 24);
        sb.append(".");

        // 将高8位置0，然后右移16位
        sb.append((longIp & 0x00FFFFFF) >>> 16);
        sb.append(".");

        // 将高16位置0，然后右移8位
        sb.append((longIp & 0x0000FFFF) >>> 8);
        sb.append(".");

        // 将高24位置0
        sb.append(longIp & 0x000000FF);

        return sb.toString();
    }

    /**
     * 根据域名获得对应的IP地址
     *
     * @param domainName
     *            域名地址
     *
     * @return 域名所对应的IP地址
     */
    public static String getIP(String domainName) {
        try {
            return InetAddress.getByName(domainName).getHostAddress();
        } catch (UnknownHostException ignored) {
        }
        return null;
    }

}
