package cn.hehouhui.shandard;

/**
 * 范围值
 *
 * @author HeHui
 * @date 2024-12-17 18:54
 */
public class Range<T> {

    /** 开始 */
    private T start;

    /** 结束 */
    private T end;

    public Range() {}

    public Range(final T start, final T end) {
        this.start = start;
        this.end = end;
    }


    public T getStart() {
        return start;
    }

    public void setStart(final T start) {
        this.start = start;
    }

    public T getEnd() {
        return end;
    }

    public void setEnd(final T end) {
        this.end = end;
    }
}
