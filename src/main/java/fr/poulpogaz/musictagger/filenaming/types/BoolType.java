package fr.poulpogaz.musictagger.filenaming.types;

public final class BoolType implements FType {

    public static final BoolType INSTANCE = new BoolType();
    private BoolType() {}

    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}