package fr.poulpogaz.musictagger.filenaming;

import java.util.LinkedList;
import java.util.List;

public interface FTree {

    record Variable(String name) implements FTree {}
    record Literal(Object value) implements FTree {}
    record FunctionCall(String function, List<ExpressionList> args) implements FTree {
        public FunctionCall(String function) {
            this(function, null);
        }
    }
    record ExpressionList(List<FTree> list) implements FTree {

        public ExpressionList() {
            this(new LinkedList<>());
        }

        ExpressionList add(FTree tree) {
            list.addFirst(tree);
            return this;
        }
    }
}
