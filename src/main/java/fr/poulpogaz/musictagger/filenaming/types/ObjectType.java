package fr.poulpogaz.musictagger.filenaming.types;

public final class ObjectType implements FType {

    public static final ObjectType INSTANCE = new ObjectType();
    private ObjectType() {}

    @Override
    public boolean canConvertTo(FType other) {
        return false;
    }

    @Override
    public Object convertTo(FType other, Object object) {
        return null;
    }
}