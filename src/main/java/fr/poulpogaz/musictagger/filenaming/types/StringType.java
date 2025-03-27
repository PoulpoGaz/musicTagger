package fr.poulpogaz.musictagger.filenaming.types;

public final class StringType implements FType {

    public static final StringType INSTANCE = new StringType();
    private StringType() {}

    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}