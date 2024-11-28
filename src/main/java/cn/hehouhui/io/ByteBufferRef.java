package cn.hehouhui.io;

/**
 * byte buffer引用
 *
 * @author HeHui
 * @date 2024-11-28 13:32
 */
public class ByteBufferRef {

    /** 空 */
    public static final ByteBufferRef EMPTY = new ByteBufferRef(new byte[0], 0, 0);

    private byte[] data;

    /**
     * data中的可用数据起始位置
     */
    private int offset;

    /**
     * data中的可用数据长度
     */
    private int len;

    public ByteBufferRef() {
    }

    public ByteBufferRef(final byte[] data, final int offset, final int len) {
        this.data = data;
        this.offset = offset;
        this.len = len;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    public int getLen() {
        return len;
    }

    public void setLen(final int len) {
        this.len = len;
    }
}
