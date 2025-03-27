package fr.poulpogaz.musictagger.filenaming;

import fr.poulpogaz.musictagger.MTException;
import fr.poulpogaz.musictagger.opus.OpusFile;

import java.util.*;

public class FProgram {

    private final List<FOp> ops;
    private final Map<String, FFunction> functions = new HashMap<>();

    OpusFile file;
    Object[] funcArgs;
    StringBuilder sb;
    Stack<Object> stack;

    int instructionPointer;

    FProgram(List<FOp> ops) {
        this.ops = Collections.unmodifiableList(ops);
        addFunction(FFunctions.EQ);
        addFunction(FFunctions.FIRST_NON_NULL);
    }

    private void addFunction(FFunction func) {
        functions.put(func.name(), func);
    }

    public String execute(OpusFile file) {
        try {
            this.file = file;
            funcArgs = new Object[16];
            sb = new StringBuilder();
            stack = new Stack<>();

            while (instructionPointer >= 0 && instructionPointer < ops.size()) {
                FOp op = ops.get(instructionPointer);
                // System.out.println(instructionPointer + " - " + stack + " - " + op);
                op.execute(this);
                instructionPointer++;
            }

            if (stack.size() != 1) {
                throw new MTException("Runtime error, expected stack size is one but got " + stack.size());
            }

            return stack.pop().toString();
        } finally {
            clean();
        }
    }

    void prepareCall(int argCount) {
        funcArgs = growIfNeeded(funcArgs, argCount);

        for (int i = 0; i < argCount; i++) {
            funcArgs[argCount - i - 1] = pop();
        }
    }

    private Object[] growIfNeeded(Object[] funcArgs, int argCount) {
        if (funcArgs.length < argCount) {
            return new Object[argCount];
        }
        return funcArgs;
    }

    FFunction getFunction(String name) {
        return functions.get(name);
    }


    Object pop() {
        return stack.pop();
    }

    void push(Object object) {
        stack.push(object);
    }


    String castToString(Object pop) {
        if (pop instanceof List<?> list) {
            if (list.isEmpty()) {
                return "";
            }

            return castToString(list.getFirst());
        } else if (pop != null) {
            return pop.toString();
        } else {
            return "";
        }
    }


    private void clean() {
        Arrays.fill(funcArgs, null);
        funcArgs = null;
        sb = null;
        stack.clear();
        stack = null;
        file = null;
    }

    public int size() {
        return ops.size();
    }

    @Override
    public String toString() {
        return ops.toString();
    }
}
