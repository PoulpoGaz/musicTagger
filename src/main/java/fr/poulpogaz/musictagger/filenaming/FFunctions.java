package fr.poulpogaz.musictagger.filenaming;

import java.util.Objects;

public class FFunctions {



    public static final FFunction FIRST_NON_NULL = new FFunction() {
        @Override
        public String name() {
            return "?";
        }

        @Override
        public int arity() {
            return -1;
        }

        @Override
        public Class<?> returnClass() {
            return String.class;
        }

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


    public static final FFunction IF = new FFunction() {
        @Override
        public String name() {
            return "if";
        }

        @Override
        public int arity() {
            return 3;
        }

        @Override
        public Class<?> returnClass() {
            return String.class;
        }

        @Override
        public Object call(Object[] args, int argCount) {
            Object condition = args[0];

            Object ret = null;
            if (condition instanceof Integer i && i != 0 || condition != null) {
                ret = args[1];
            } else if (argCount == 3){
                ret = args[2];
            }

            return ret;
        }
    };

    public static final FFunction EQ = new FFunction() {
        @Override
        public String name() {
            return "eq";
        }

        @Override
        public int arity() {
            return 2;
        }

        @Override
        public Class<?> returnClass() {
            return Integer.class;
        }

        @Override
        public Object call(Object[] args, int argCount) {
            return Objects.equals(args[0].toString(), args[1].toString()) ? 1 : 0;
        }
    };
}
