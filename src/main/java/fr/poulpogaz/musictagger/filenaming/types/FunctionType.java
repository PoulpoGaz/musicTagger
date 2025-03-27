package fr.poulpogaz.musictagger.filenaming.types;

import java.util.List;

public record FunctionType(FType returnT, List<FType> types, FType variadicType) implements FType {

    public FunctionType(FType returnT) {
        this(returnT, List.of(), null);
    }

    public FunctionType(FType returnT, FType type1) {
        this(returnT, List.of(type1), null);
    }

    public FunctionType(FType returnT, FType type1, FType type2) {
        this(returnT, List.of(type1, type2), null);
    }

    public FunctionType(FType returnT, FType type1, FType type2, FType type3) {
        this(returnT, List.of(type1, type2, type3), null);
    }

    public FunctionType(FType returnT, FType type1, FType type2, FType type3, FType type4) {
        this(returnT, List.of(type1, type2, type3, type4), null);
    }


    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}
