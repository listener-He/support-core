package cn.hehouhui.function.complete;

import cn.hehouhui.util.EmptyUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 补充函数
 * <p>
 * 这应该是 complete函数 一年多来的究极演化体了。可同时支持多组补充函数
 * <p>
 * v1.0：{@link cn.hehouhui.util.FunctionUtil#complete(Collection, Function, BiConsumer, Function)}
 * <p>
 * v1.1: @link FunctionUtil#complete(Collection, Function, FunctionUtil.SetGet[])
 * <p>
 * 例子： <blockquote>
 * 
 * <pre>
 * Complete.start(userList)
 *     // 补充用户名称
 *     .build(userService::getUsernameMap).filter(user -> user.getUserId() > 0).add(User::getUserId, User::setUsername)
 *     .then().over();
 * </pre>
 * 
 * </blockquote>
 *
 * @author HeHui
 * @date 2024-12-29 02:08
 */
public class Complete<E> {

    private final List<Prepare<?, ?, E>> actuator = new ArrayList<>();
    private final Collection<E> collection;

    private Complete(final Collection<E> collection) {
        this.collection = collection;
    }

    /**
     * 启动一个完成对象，用于处理集合中的元素 此方法的主要作用是初始化一个Complete对象，以便对给定集合进行后续的操作 它提供了一种通用的方式来开始对集合的处理，而不暴露底层实现细节
     *
     * @param collection
     *            一个包含E类型元素的集合，用于初始化Complete对象
     * @param <E>
     *            集合中元素的类型，使用泛型来允许处理不同类型的集合
     *
     * @return 返回一个初始化后的Complete对象，用于执行后续的完成操作
     */
    public static <E> Complete<E> start(Collection<E> collection) {
        return new Complete<>(collection);
    }

    /**
     * 构建一个Prepare对象，并将其添加到执行器中 此方法允许用户提供一个自定义的映射创建函数，用于将项目列表转换为带有名称的映射
     *
     * @param nameMapCreator
     *            一个函数，接受一个项目的列表，返回一个映射，其中包含每个项目的名称和对应的节点
     *
     * @return 返回构建的Prepare对象，它已经准备好被执行
     */
    public <I, N> Prepare<I, N, E>
        build(final Function<? super List<I>, ? extends Map<? super I, ? extends N>> nameMapCreator) {
        // 创建一个新的Prepare对象，传入自定义的映射创建函数和当前配置
        Prepare<I, N, E> prepare = new Prepare<>(nameMapCreator, this);
        // 将创建的Prepare对象添加到执行器中，以便后续执行
        actuator.add(prepare);
        // 返回创建的Prepare对象
        return prepare;
    }

    /**
     * 构建一个Prepare对象，用于处理元素的ID和名称映射
     *
     * @param <I>
     *            元素ID的类型
     * @param <N>
     *            元素名称的类型
     * @param idGetter
     *            一个函数，用于获取元素的ID
     * @param nameSetter
     *            一个消费者，用于设置元素的名称
     * @param nameMapCreator
     *            一个函数，用于创建从ID列表到名称映射的Map
     *
     * @return 返回构建的Prepare对象
     */
    public <I, N> Prepare<I, N, E> build(Function<? super E, ? extends I> idGetter,
        BiConsumer<? super E, ? super N> nameSetter,
        final Function<? super List<I>, ? extends Map<? super I, ? extends N>> nameMapCreator) {
        // 创建一个新的Prepare对象，传入自定义的映射创建函数和当前配置
        Prepare<I, N, E> prepare = new Prepare<>(nameMapCreator, this);
        // 向Prepare对象中添加idGetter和nameSetter，以便处理元素的ID和名称
        prepare.add(idGetter, nameSetter);
        // 将创建的Prepare对象添加到执行器中，以便后续执行
        actuator.add(prepare);
        // 返回创建的Prepare对象
        return prepare;
    }

    /**
     * 执行当前存量任务
     * <p>
     * 此方法用于在当前流程或任务完成时，执行定义在{@link #over()}中的操作 它提供了一种机制，确保在流程的特定阶段执行某些操作，例如资源释放、通知或其他清理工作 然后返回当前对象，以支持链式调用和进一步操作
     *
     * @return 返回Complete对象，允许进行链式调用或进一步操作
     */
    public Complete<E> doThen() {
        over();
        return this;
    }

    /**
     * 执行当前存量任务然后准备继续添加其他任务
     *
     * @param executor
     *            调度器
     *
     * @return 返回Complete对象，使得可以链式调用其他方法
     */
    public Complete<E> doThen(Executor executor) {
        over(executor);
        return this;
    }

    /**
     * 调用当前实例的over方法处理其内部的collection 此方法的存在是为了提供一个便捷的方式，使得当前实例可以直接调用其内部的collection进行处理 而不需要显式地传递collection参数
     * 方法首先检查集合是否为空，如果为空，则不执行任何操作 然后，它遍历集合中的每个元素，对每个元素执行初始化操作 最后，再次遍历集合中的每个元素，对每个元素执行完成操作
     */
    public void over() {
        over(null);
    }

    /**
     * 执行初始化和完成操作于集合中的每个元素 如果集合或执行器为空，则仅清除actuator 此方法首先对集合中的每个元素执行初始化操作，然后对每个元素执行完成操作
     * 使用CompletableFuture来并行执行完成操作，可以选择性地使用提供的执行器
     *
     * @param executor
     *            用于执行完成操作的线程池执行器，可以为空
     */
    public void over(Executor executor) {
        // 检查集合是否为空，如果为空，则不执行任何操作
        if (EmptyUtil.isEmpty(collection) || EmptyUtil.isEmpty(actuator)) {
            actuator.clear();
            return;
        }

        // 遍历集合中的每个元素，对每个元素执行初始化操作
        collection.forEach(item -> actuator.forEach(prepare -> prepare.init(item)));

        // 再次遍历集合中的每个元素，对每个元素执行完成操作
        if (executor == null) {
            actuator.stream().map(Prepare::finish).reduce(Consumer::andThen).ifPresent(collection::forEach);
        } else {
            // 使用CompletableFuture来并行执行这些操作，以提高效率
            List<CompletableFuture<Consumer<E>>> futures =
                actuator.stream().map(prepare -> CompletableFuture.supplyAsync(prepare::finish, executor)).toList();
            futures.stream().map(CompletableFuture::join).reduce(Consumer::andThen).ifPresent(collection::forEach);
        }

        // 清空actuator列表，以便下一次使用
        actuator.clear();
    }
}
