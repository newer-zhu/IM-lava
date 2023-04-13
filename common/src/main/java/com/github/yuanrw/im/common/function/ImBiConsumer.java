package com.github.yuanrw.im.common.function;


@FunctionalInterface
public interface ImBiConsumer<T, U> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws Exception
     */
    void accept(T t, U u) throws Exception;
}
