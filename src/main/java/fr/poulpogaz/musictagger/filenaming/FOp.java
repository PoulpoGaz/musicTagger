package fr.poulpogaz.musictagger.filenaming;

import java.util.List;
import java.util.function.BiPredicate;

public interface FOp {

    void execute(FProgram program);

    record Variable(String name) implements FOp {
        @Override
        public void execute(FProgram program) {
            program.push(program.file.getFirst(name));
        }
    }

    record Literal(Object value) implements FOp {
        @Override
        public void execute(FProgram program) {
            program.push(value);
        }
    }

    record Call(String function, int numArgs) implements FOp {
        @Override
        public void execute(FProgram program) {
            program.prepareCall(numArgs);
            FFunction func = program.getFunction(function);
            Object o = func.call(program.funcArgs, numArgs);
            program.push(o);
        }
    }

    record Concat(int count) implements FOp {
        @Override
        public void execute(FProgram program) {
            program.prepareCall(count);
            program.sb.setLength(0);
            for (int i = 0; i < count; i++) {
                program.sb.append(program.castToString(program.funcArgs[i]));
            }
            program.push(program.sb.toString());
        }
    }

    record Jump(int dest) implements FOp {

        @Override
        public void execute(FProgram program) {
            program.instructionPointer = dest - 1;
        }
    }

    record JumpIf(int dest) implements FOp {

        @Override
        public void execute(FProgram program) {
            Object obj = program.pop();
            boolean jump;
            if (obj instanceof List<?> l) {
                jump = !l.isEmpty();
            } else if (obj instanceof Boolean b) {
                jump = b;
            } else {
                throw new FException("Can't cast " + obj + " to boolean");
            }

            if (jump) {
                program.instructionPointer = dest - 1;
            }
        }
    }

    BoolBinaryOp AND = new BoolBinaryOp((a, b) -> a && b);
    BoolBinaryOp OR = new BoolBinaryOp((a, b) -> a || b);

    record BoolBinaryOp(BiPredicate<Boolean, Boolean> func) implements FOp {

        @Override
        public void execute(FProgram program) {
            boolean b1 = (Boolean) program.pop();
            boolean b2 = (Boolean) program.pop();

            program.push(func.test(b1, b2));
        }
    }

    final class BoolNegationOp implements FOp {

        public static final BoolNegationOp INSTANCE = new BoolNegationOp();
        private BoolNegationOp() {}

        @Override
        public void execute(FProgram program) {
            boolean b = (Boolean) program.pop();
            program.push(!b);
        }

        @Override
        public String toString() {
            return "BoolNegationOp[]";
        }
    }

    record CompOp(BiPredicate<Object, Object> func) implements FOp {

        @Override
        public void execute(FProgram program) {
            boolean b1 = (Boolean) program.pop();
            boolean b2 = (Boolean) program.pop();

            program.push(func.test(b1, b2));
        }
    }
}
