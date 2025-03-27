package fr.poulpogaz.musictagger.filenaming.types;

public final class VoidType implements FType {

    public static final VoidType INSTANCE = new VoidType();
    private VoidType() {}

    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}
