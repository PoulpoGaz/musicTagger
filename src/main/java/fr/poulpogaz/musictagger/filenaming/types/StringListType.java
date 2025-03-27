package fr.poulpogaz.musictagger.filenaming.types;

public final class StringListType implements FType {

    public static final StringListType INSTANCE = new StringListType();
    private StringListType() {}

    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}