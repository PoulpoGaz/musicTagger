package fr.poulpogaz.musictagger.filenaming;

import fr.poulpogaz.musictagger.filenaming.types.FType;
import fr.poulpogaz.musictagger.filenaming.types.FunctionType;

import java.util.List;

public class FFunctions {

    public static final FFunction FIRST_NON_NULL = new FFunction("?", new FunctionType(FType.objectT(), List.of(), FType.objectT())) {

        @Override
        public Object call(Object[] args, int argCount) {
            for (int i = 0; i < argCount; i++) {
                if (args[i] != null) {
                    return args[i];
                }
            }

            return null;
        }
    };

    public static final FFunction EQ = new FFunction("eq", new FunctionType(FType.objectT(), FType.objectT(), FType.objectT())) {
        @Override
        public Object call(Object[] args, int argCount) {
            return null;
        }
    };
}
