package com.in3rovert_so.securedoc.function;

/*
Custom Consumer that takes three values.
 */

@FunctionalInterface
public interface TriConsumer <T, U, V> {
    void accept (T t, U u, V v);

//    default Consumer<T, U, V> andThen (TriConsumer<? super T> after) {
//        Objects.requireNonNull(after);
//        return (T t, U u, V v) -> {accept(t, u, v); after.accept(t,u,v );};
//    }
}

