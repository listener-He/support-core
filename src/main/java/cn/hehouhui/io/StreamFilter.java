package cn.hehouhui.io;

/**
 * 流过滤器
 *
 * @author HEHH
 * @date 2024/11/28
 */
@FunctionalInterface
public interface StreamFilter {

    /**
     * 过滤数据
     *
     * @param ref
     *            过滤前的数据
     * @return 过滤后的数据
     */
    ByteBufferRef filter(ByteBufferRef ref);

    /**
     * 数据结束
     *
     * @return 最后还要补充的数据，返回null表示没有了
     */
    default ByteBufferRef finish() {
        return null;
    }
}
