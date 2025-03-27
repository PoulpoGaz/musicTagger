package fr.poulpogaz.musictagger.filenaming.types;

public sealed interface FType permits
        BoolType, IntType, StringType, StringListType,
        FunctionType, NullType, ObjectType, VoidType {

    boolean canConvertTo(FType other);

    Object convertTo(FType other, Object object);


    static BoolType boolT() {
        return BoolType.INSTANCE;
    }

    static IntType intT() {
        return IntType.INSTANCE;
    }

    static StringType stringT() {
        return StringType.INSTANCE;
    }

    static StringListType stringListT() {
        return StringListType.INSTANCE;
    }


    static NullType nullT() {
        return NullType.INSTANCE;
    }

    static ObjectType objectT() {
        return ObjectType.INSTANCE;
    }

    static VoidType voidT() {
        return VoidType.INSTANCE;
    }

}
