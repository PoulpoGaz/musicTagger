package fr.poulpogaz.musictagger.filenaming;

import fr.poulpogaz.musictagger.opus.OpusFile;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Compiler {

    public Compiler() {

    }

    public FProgram compile(Reader reader) throws Exception {
        FLexer lexer = new FLexer(reader);
        FParser parser = new FParser(lexer);

        FTree root = (FTree) parser.parse().value;

        // transform tree into reverse polish notation
        List<FOp> program = new ArrayList<>();

        // contains either FTree or FOp
        Stack<Object> stack = new Stack<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Object e = stack.pop();

            switch (e) {
                case FTree.Variable(String name) -> program.add(new FOp.Variable(name));
                case FTree.Literal(Object value) -> program.add(new FOp.Literal(value));
                case FTree.ExpressionList(List<FTree> list) -> {
                    if (list.size() > 1) {
                        stack.push(new FOp.Concat(list.size()));
                    }

                    for (int i = list.size() - 1; i >= 0; i--) {
                        stack.push(list.get(i));
                    }
                }
                case FTree.FunctionCall(String func, List<FTree.ExpressionList> args) -> {
                    stack.push(new FOp.Call(func, args.size()));

                    for (int i = args.size() - 1; i >= 0; i--) {
                        stack.push(args.get(i));
                    }
                }
                case FOp.Concat concat -> program.add(concat);
                case FOp.Call call -> program.add(call);
                default -> throw new IllegalStateException("Unexpected value: " + e);
            }
        }

        return new FProgram(program);
    }

    public static void main(String[] args) throws Exception {
        String program = """
                ?(albumartist, artist)"/"
                if (albumartist,
                    album "num"
                )
                /*
                    Multiline comment
                */
                if (eq(cd, 2),
                    "two cds", // comment
                    $cd count$
                )
                """;

        Compiler compiler = new Compiler();
        FProgram p = compiler.compile(new StringReader(program));
        System.out.println(p);
        OpusFile file = new OpusFile();
        file.put("albumartist", "Moi");
        file.put("album", "MonAlbum");
        file.put("cd", "5");
        file.put("cd count", "couuunt");

        System.out.println(p.execute(file));
    }
}
