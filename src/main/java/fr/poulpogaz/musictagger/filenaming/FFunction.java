package fr.poulpogaz.musictagger.filenaming;

import fr.poulpogaz.musictagger.filenaming.types.FunctionType;

public abstract class FFunction {

    protected final String name;
    protected final FunctionType signature;

    public FFunction(String name, FunctionType signature) {
        this.name = name;
        this.signature = signature;
    }

    public String name() {
        return name;
    }

    public FunctionType signature() {
        return signature;
    }

    public abstract Object call(Object[] args, int argCount);
}
