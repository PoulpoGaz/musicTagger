package fr.poulpogaz.musictagger.filenaming;

import java.util.LinkedList;
import java.util.List;

public interface FTree {


    record Variable(String name) implements FTree {}

    static Variable variable(String name) {
        return new Variable(name);
    }



    record Literal(Object value) implements FTree {}
    Literal NULL_LITERAL = new Literal(null);

    static Literal literal(Object value) {
        return value == null ? NULL_LITERAL : new Literal(value);
    }



    record FunctionCall(String function, List<ExpressionList> args) implements FTree {}

    static FunctionCall functionCall(String name, List<ExpressionList> arguments) {
        return new FunctionCall(name, arguments);
    }



    record ExpressionList(List<FTree> list) implements FTree {

        public ExpressionList() {
            this(new LinkedList<>());
        }

        ExpressionList add(FTree tree) {
            if (tree instanceof ExpressionList) {
                throw new IllegalStateException();
            }

            list.addFirst(tree);
            return this;
        }
    }

    static ExpressionList expList() {
        return new ExpressionList();
    }




    record If(FTree cond, ExpressionList ifTrue, ExpressionList ifFalse) implements FTree {}

    static If ifExp(FTree condition, ExpressionList ifTrue, ExpressionList ifFalse) {
        return new If(condition, ifTrue, ifFalse);
    }


    record UnaryExpression(int op, FTree cond) implements FTree {}
    record BinaryExpression(FTree left, int op, FTree right) implements FTree {}

    static UnaryExpression unExp(int op, FTree exp) {
        return new UnaryExpression(op, exp);
    }

    static BinaryExpression binExp(FTree left, int op, FTree right) {
        return new BinaryExpression(left, op, right);
    }
}
