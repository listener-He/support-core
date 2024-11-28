package cn.hehouhui.shandard;

import java.util.List;

/**
 * 树节点
 *
 * @author HEHH
 * @date 2024/11/18
 */
public interface TreeNode<ID, S extends Comparable<S>, E> {

    /**
     * 获取id
     *
     * @return {@link ID }
     */
    ID getId();

    /**
     * 获取父id
     *
     * @return {@link ID }
     */
    ID getParentId();

    /**
     * 获取排序
     *
     * @return {@link S }
     */
    S getSort();

    /**
     * 获取子节点
     *
     * @return {@link List }<{@link E }>
     */
    List<E> getChildren();

    /**
     * 设置子节点
     *
     * @param children 子节点
     */
    void setChildren(List<E> children);
}
