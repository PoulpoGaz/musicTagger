package fr.poulpogaz.musicdl;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public abstract class BasicObjectPool<E> {

    public static <E> BasicObjectPool<E> supplierPool(Supplier<E> supplier) {
        return new BasicObjectPool<>() {
            @Override
            public E newObject() {
                return supplier.get();
            }
        };
    }



    private final ConcurrentLinkedQueue<E> objects = new ConcurrentLinkedQueue<>();

    public BasicObjectPool() {

    }

    public abstract E newObject();

    public E get() {
        E object = objects.poll();

        if (object == null) {
            object = newObject();
        }

        return object;
    }

    public void free(E object) {
        objects.offer(object);
    }
}
