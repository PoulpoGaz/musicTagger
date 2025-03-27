package fr.poulpogaz.musictagger.filenaming.types;

public final class IntType implements FType {

    public static final IntType INSTANCE = new IntType();
    private IntType() {}

    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}
