package fr.poulpogaz.musictagger.filenaming;

public interface FOp {

    void execute(FProgram program);

    record Variable(String name) implements FOp {
        @Override
        public void execute(FProgram program) {
            program.stack.push(program.file.getFirst(name));
        }
    }

    record Literal(Object value) implements FOp {
        @Override
        public void execute(FProgram program) {
            program.stack.push(value);
        }
    }

    record Call(String function, int numArgs) implements FOp {
        @Override
        public void execute(FProgram program) {
            program.prepareCall(numArgs);
            FFunction func = program.getFunction(function);
            Object o = func.call(program.funcArgs, numArgs);
            program.stack.push(o);
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
            program.stack.push(program.sb.toString());
        }
    }
}
