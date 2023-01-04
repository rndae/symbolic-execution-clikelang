import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class StaticAnalyser {
  public static void main(String[] args) throws Exception {
    String inputFile = null;
    if (args.length > 0) inputFile = args[0];
    InputStream is = System.in;
    if (inputFile != null) is = new FileInputStream(inputFile);
    ANTLRInputStream input = new ANTLRInputStream(is);

    SimpleCLexer lexer = new SimpleCLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    SimpleCParser parser = new SimpleCParser(tokens);

    ParseTree tree = parser.program();

    SETGen setGen = new SETGen();
    setGen.visit(tree);
    System.out.println(setGen.instructionsConsidered);

    int index = 0;
    for (Node leaf: setGen.conditionsTree.getLeaves()) {
      index++;
      System.out.println("Path N" + index + ": ");
      for (Node nodeInPath:setGen.conditionsTree.findTheRootOfAllEvil(leaf)) {
        System.out.println(nodeInPath.key);
      }
      System.out.println("");
    }
  }
}
