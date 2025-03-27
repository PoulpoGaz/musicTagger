package fr.poulpogaz.musictagger.filenaming.types;

public final class NullType implements FType {

    public static final NullType INSTANCE = new NullType();
    private NullType() {}

    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}