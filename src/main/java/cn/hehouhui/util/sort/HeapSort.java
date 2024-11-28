package cn.hehouhui.util.sort;

import cn.hehouhui.util.EmptyUtil;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 堆排序
 *
 * @author HeHui
 * @date 2024-11-27 09:43
 */
class HeapSort<T, E extends Comparable<E>> implements Sortable<T, E> {


    /**
     * 对集合进行排序
     *
     * @param collection 待排序集合
     * @param sortField  排序字段
     *
     * @return {@link List }<{@link T }> 已排序集合
     */
    @Override
    public List<T> sort(final Collection<T> collection, final Function<T, E> sortField) {
        if (EmptyUtil.isEmpty(collection)) {
            return Collections.emptyList();
        }
        T[] array = (T[])collection.toArray();
        MaxHeap<T, E> maxHeap = new MaxHeap<>(array, sortField);

        for (int i = 0; i < array.length; i++) {
            maxHeap.removeAndFixit();
        }
        System.arraycopy(maxHeap.queue, 1, array, 0, collection.size());
        return List.of(array);
    }

    /**
     * <p>
     * 最大堆
     * </p>
     *
     * @see <a href="http://blog.csdn.net/lemon_tree12138">http://blog.csdn.net/lemon_tree12138</a>
     */
    private static class MaxHeap<T, E extends Comparable<E>> {

        private int size = 0;
        private final T[] queue;
        private final Function<T, E> sortField;

        public MaxHeap(Collection<T> data, Function<T, E> sortField) {
            this((T[]) data.toArray(), sortField);
        }

        public MaxHeap(T[] data, Function<T, E> sortField) {
            this.sortField = sortField;
            this.queue = (T[]) Array.newInstance(data.getClass().getComponentType(), data.length + 1);
            for (int i = 0; i < data.length; i++) {
                queue[++size] = data[i];
                fixUp(size);
            }
        }

        // 这里对可以确定有序的元素进行移除，再对剩下无序的序列进行排序
        private void removeAndFixit() {
            swap(queue, 1, size--);
            fixDown(1);
        }

        /**
         * 从给定的父节点开始，向下调整堆，以确保堆的性质被维护
         * 主要用于删除堆顶元素或插入新元素后，重新调整堆结构
         *
         * @param fatherIndex 调整开始的父节点索引
         */
        private void fixDown(int fatherIndex) {
            int size = this.size; // 提前获取堆的大小，避免多次访问成员变量
            while (true) {
                int leftChild = fatherIndex * 2;
                int rightChild = leftChild + 1;
                int largest = fatherIndex;

                // 检查左子节点是否存在且大于父节点
                if (leftChild <= size && sortField.apply(queue[leftChild]).compareTo(sortField.apply(queue[largest])) > 0) {
                    largest = leftChild;
                }

                // 检查右子节点是否存在且大于当前最大节点
                if (rightChild <= size && sortField.apply(queue[rightChild]).compareTo(sortField.apply(queue[largest])) > 0) {
                    largest = rightChild;
                }

                // 如果父节点已经是最大的，不需要交换
                if (largest == fatherIndex) {
                    break;
                }

                // 交换父节点和最大子节点
                swap(queue, fatherIndex, largest);
                fatherIndex = largest;
            }
        }


        /**
         * 调整指定索引处的元素位置以维持堆属性。
         * 用于处理指定索引处元素的堆属性违规情况。 在初始化阶段，需要构建所有元素满足大顶堆
         *
         * @param currentIndex 需要调整的元素的索引。
         */
        private void fixUp(int currentIndex) {
            // 检查队列是否为空，如果为空则抛出异常。
            if (queue == null) {
                throw new IllegalArgumentException("队列为空");
            }
            // 如果当前索引小于等于1或大于等于数组长度，则无需操作。
            if (currentIndex <= 1 || currentIndex >= queue.length) {
                return;
            }
            // 计算当前索引的父节点索引。
            int parentIndex = currentIndex >> 1;
            // 继续循环直到当前索引为1或堆属性恢复。
            while (currentIndex > 1) {
                // 如果父节点值大于当前节点值，则堆属性已恢复，退出循环。
                if (sortField.apply(queue[parentIndex]).compareTo(sortField.apply(queue[currentIndex])) > 0) {
                    break;
                }
                // 交换父节点和当前节点的位置。
                swap(queue, parentIndex, currentIndex);
                // 更新当前索引和父节点索引。
                currentIndex = parentIndex;
                parentIndex = currentIndex >> 1;
            }
        }

        /**
         * 交换数组中两个指定位置的元素
         * 用于在数组中交换两个位置的元素，以实现元素的重新排列
         *
         * @param array 要进行元素交换的数组，类型为泛型E，表示可以是任何类型的数组
         * @param i     第一个要交换的元素的索引位置
         * @param j     第二个要交换的元素的索引位置
         *
         * @throws IllegalArgumentException  如果传入的数组为null，则抛出此异常，表示不允许操作空数组
         * @throws IndexOutOfBoundsException 如果传入的索引i或j不在数组的有效索引范围内，则抛出此异常，表示索引越界
         */
        private void swap(T[] array, int i, int j) {
            if (array == null) {
                throw new IllegalArgumentException("数组不能为空");
            }
            if (i < 0 || i >= array.length || j < 0 || j >= array.length) {
                throw new IndexOutOfBoundsException("索引超出数组范围");
            }
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

    }


}
