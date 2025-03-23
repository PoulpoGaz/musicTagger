package fr.poulpogaz.musictagger.filenaming;

public interface FFunction {

    String name();

    int arity();

    Class<?> returnClass();

    Object call(Object[] args, int argCount);
}
