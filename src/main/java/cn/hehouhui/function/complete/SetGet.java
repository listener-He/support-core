package cn.hehouhui.function.complete;

import lombok.Getter;
import org.springframework.util.Assert;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * set get函数
 *
 * @author HeHui
 * @date 2024/11/19
 */
@Getter
public class SetGet<E, I, N> {

    private final Function<? super E, ? extends I> idGetter;

    private final BiConsumer<? super E, ? super N> nameSetter;

    public SetGet(final Function<? super E, ? extends I> idGetter, final BiConsumer<? super E, ? super N> nameSetter) {
        Assert.notNull(idGetter, "idGetter must not be null");
        Assert.notNull(nameSetter, "nameSetter must not be null");
        this.idGetter = idGetter;
        this.nameSetter = nameSetter;
    }


    public I get(E target) {
        return idGetter.apply(target);
    }

    public void set(E target, N value) {
        nameSetter.accept(target, value);
    }
}
