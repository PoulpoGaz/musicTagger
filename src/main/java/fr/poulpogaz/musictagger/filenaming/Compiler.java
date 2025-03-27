package fr.poulpogaz.musictagger.filenaming;

import fr.poulpogaz.musictagger.opus.OpusFile;
import java_cup.runtime.ComplexSymbolFactory;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Compiler {

    private boolean printAST;

    public Compiler() {

    }

    public FProgram compile(Reader reader) throws Exception {
        ErrorHandler errors = new ErrorHandler(128);

        ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();
        FLexer lexer = new FLexer(reader, symbolFactory, errors);
        FParser parser = new FParser(lexer, symbolFactory);

        FTree root = (FTree) parser.parse().value;
        if (printAST) {
            prettyPrint(root);
        }

        // transform tree into reverse polish notation
        List<FOp> program = new ArrayList<>();
        write(program, root);

        return new FProgram(program);
    }

    private void write(List<FOp> program, FTree ftree) {
        switch (ftree) {
            case FTree.Variable(String name) -> program.add(new FOp.Variable(name));
            case FTree.Literal(Object value) -> program.add(new FOp.Literal(value));
            case FTree.ExpressionList(List<FTree> list) -> {
                for (FTree fTree : list) {
                    write(program, fTree);
                }
                if (list.isEmpty()) {
                    program.add(new FOp.Literal(""));
                }
                if (list.size() > 1) {
                    program.add(new FOp.Concat(list.size()));
                }
            }
            case FTree.FunctionCall(String func, List<FTree.ExpressionList> args) -> {
                for (FTree.ExpressionList arg : args) {
                    write(program, arg);
                }

                program.add(new FOp.Call(func, args.size()));
            }
            case FTree.If(FTree condition, FTree.ExpressionList ifTrue, FTree.ExpressionList ifFalse) -> {
                // cond
                // jumpif <JUMPIF>
                // else
                // jump <JUMP>
                // <JUMPIF>
                // if
                // <JUMP>

                write(program, condition);
                int jumpIfIndex = program.size();
                program.add(null); // jump if

                if (ifFalse != null) { // dumb but for testing
                    write(program, ifFalse);
                }
                int jumpIndex = program.size();
                program.add(null);

                write(program, ifTrue);

                program.set(jumpIfIndex, new FOp.JumpIf(jumpIndex + 1));
                program.set(jumpIndex, new FOp.Jump(program.size()));
            }
            case FTree.UnaryExpression(int op, FTree cond) -> {
                if (op == FParserSym.NOT) {
                    write(program, cond);
                    program.add(FOp.BoolNegationOp.INSTANCE);
                } else {
                    throw new IllegalStateException();
                }
            }
            case FTree.BinaryExpression(FTree left, int op, FTree right) -> {
                write(program, left);
                write(program, right);

                switch (op) {
                    case FParserSym.EQ -> program.add(new FOp.CompOp((a, b) -> a == b));
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + ftree);
        }
    }


    public boolean isPrintAST() {
        return printAST;
    }

    public void setPrintAST(boolean printAST) {
        this.printAST = printAST;
    }

    public static void main(String[] args) throws Exception {
        String program = """
                ?(albumartist, artist)"/"
                if albumartist {
                    album "num"
                }
                /*
                    Multiline comment
                */
                if cd == 2 {
                    "two cds" // comment
                    $cd count$
                }
                /*if cd == 2 {
                    "two cs" $cd count$
                } else if cd == 3 && abc == func(if cd == 3 { hello } else { world }) {
                
                } else {
                
                }
                if a == 1 {} else if a == 2 {} else {}*/
                """;


        program = """
                if true {
                    "hello"
                } else {
                    "ERROR"
                }
                
                if false {
                    "GRRR"
                } else {
                    if false {
                        "GRRRRRRRRRR"
                    } else {

                    }
                }
                " "
                
                """;



        Compiler compiler = new Compiler();


        long time = System.currentTimeMillis();
        FProgram p = compiler.compile(new StringReader(program));
        long time2 = System.currentTimeMillis();
        System.out.println("Compilation done in " + (time2 - time) + "ms");
        // System.out.println(p);
        System.out.println("Size: " + p.size());

        OpusFile file = new OpusFile();
        file.put("albumartist", "Moi");
        file.put("album", "MonAlbum");
        file.put("cd", "5");
        file.put("cd count", "couuunt");

        time = System.currentTimeMillis();
        String output = p.execute(file);
        time2 = System.currentTimeMillis();
        System.out.println("Execution time: " + (time2 - time) + "ms");
        System.out.println(output);
    }

    private static void prettyPrint(FTree value) {
        StringBuilder sb = new StringBuilder();
        prettyPrint(sb, value, 0, true);
        System.out.println(sb);
    }

    private static void prettyPrint(StringBuilder sb, FTree value, int indent, boolean allowNewLine) {
        switch (value) {
            case FTree.Variable(String name) -> sb.append("var {").append(name).append("}");
            case FTree.Literal(Object lit) -> sb.append("lit {").append(lit).append("}");
            case FTree.FunctionCall(String name, List<FTree.ExpressionList> args) -> {
                sb.append("func ").append(name);

                if (args != null) {
                    sb.append("{");
                    for (int i = 0; i < args.size(); i++) {
                        FTree.ExpressionList arg = args.get(i);
                        prettyPrint(sb, arg, indent + 1, false);

                        if (i + 1 < args.size()) {
                            sb.append("; ");
                        }
                    }
                    sb.append("}");
                }
            }
            case FTree.ExpressionList(List<FTree> list) -> {
                sb.append("list {");
                indent++;
                newline(sb, indent, allowNewLine);

                for (int i = 0; i < list.size(); i++) {
                    FTree tree = list.get(i);
                    prettyPrint(sb, tree, indent + 1, allowNewLine);

                    if (i + 1 < list.size()) {
                        sb.append(":");
                        newline(sb, indent, allowNewLine);
                    }
                }

                indent--;
                newline(sb, indent, allowNewLine);
                sb.append("}");
            }
            case FTree.If(FTree cond, FTree.ExpressionList expList, FTree.ExpressionList elseExp) -> {
                sb.append("if {");
                prettyPrint(sb, cond, indent, false);
                sb.append("} then {");
                newline(sb, indent, allowNewLine);
                prettyPrint(sb, expList, indent, allowNewLine);
                newline(sb, indent - 1, allowNewLine);
                sb.append("}");

                if (elseExp != null) {
                    sb.append(" else {");
                    newline(sb, indent, allowNewLine);
                    prettyPrint(sb, elseExp, indent + 1, allowNewLine);
                    newline(sb, indent - 1, allowNewLine);
                    sb.append("}");
                }
            }
            case FTree.UnaryExpression(int op, FTree cond) -> {
                sb.append(FParserSym.terminalNames[op]).append("{");
                prettyPrint(sb, cond, indent, false);
                sb.append("}");
            }
            case FTree.BinaryExpression(FTree left, int op, FTree right) -> {
                sb.append("{");
                prettyPrint(sb, left, indent, false);
                sb.append("} ").append(FParserSym.terminalNames[op]).append(" {");
                prettyPrint(sb, right, indent, false);
                sb.append("}");
            }
            case null, default -> {}
        }
    }

    private static void newline(StringBuilder sb, int indent, boolean allowNewLine) {
        if (allowNewLine) {
            sb.append(System.lineSeparator());
            sb.append(" ".repeat(4 * indent));
        }
    }
}
