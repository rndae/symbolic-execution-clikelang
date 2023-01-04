import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.v4.runtime.tree.ParseTree;


public class SETGen extends SimpleCBaseVisitor<Void> {
  Map<String, SimpleCParser.FunctionContext> functions = new HashMap<String, SimpleCParser.FunctionContext>();
  HashMap<String, String> variablesGeneral; //variable to constant value

  HashMap<String, String> symbolsToVariableGeneral;

  LinkedList<Node> autumnLeaves;

  StringBuilder instructionsConsidered = new StringBuilder();

  ExprVisitor exprVisitor = new ExprVisitor();

  LinkedList<ConditionPath> paths = new LinkedList<ConditionPath>();

  int programCounter = 0;
  int symVarCount = 0;

  BinaryTree conditionsTree;

  int ifCurrently = 0;

  protected class ExprVisitor extends SimpleCBaseVisitor<String> {
    @Override
    public String visitCall(SimpleCParser.CallContext ctx) {
      String functionName = ctx.ID().getText();
      symbolsToVariableGeneral.put(functionName, "symbol" + symVarCount);
      symVarCount++;

      programCounter++;
      instructionsConsidered.append(programCounter);
      instructionsConsidered.append(": input ").append(functionName).append("; (").append(symbolsToVariableGeneral.get(functionName)).append(")\n");
      return symbolsToVariableGeneral.get(functionName);
    }

    @Override
    public String visitNegate(SimpleCParser.NegateContext ctx) {
      String exprNegated = visit(ctx.expr());
      return "-" +"(" +exprNegated+")";
    }

    @Override
    public String visitNot(SimpleCParser.NotContext ctx) {
      String exprComplement = visit(ctx.expr());
      //all boolean and numeric operations either return a literal value, e.g. true, or a expression in terms of the symbolic variables
      return "bool";
    }

    @Override
    public String visitMultDiv(SimpleCParser.MultDivContext ctx) {
      String left = visit(ctx.expr(0));
      String right = visit(ctx.expr(1));
      boolean leftIsInt = left.equals("int");
      boolean rightIsInt = right.equals("int");
      return "int";
    }

    @Override
    public String visitAddSub(SimpleCParser.AddSubContext ctx) {
      String left = visit(ctx.expr(0));
      String right = visit(ctx.expr(1));
      boolean leftIsInt = left.equals("int");
      boolean rightIsInt = right.equals("int");
      return "int";
    }

    @Override
    public String visitRelational(SimpleCParser.RelationalContext ctx) {
      String left = visit(ctx.expr(0));
      String right = visit(ctx.expr(1));
      boolean leftIsInt = left.equals("int");
      boolean rightIsInt = right.equals("int");
      return "bool";
    }

    @Override
    public String visitEqNeq(SimpleCParser.EqNeqContext ctx) {
      String left = visit(ctx.expr(0));
      String right = visit(ctx.expr(1));
      return "bool";
    }

    @Override
    public String visitAndOr(SimpleCParser.AndOrContext ctx) {
      String left = visit(ctx.expr(0));
      String right = visit(ctx.expr(1));
      boolean leftIsBool = left.equals("bool");
      boolean rightIsBool = right.equals("bool");
      return "bool";
    }

    @Override
    public String visitVar(SimpleCParser.VarContext ctx) {
      String nameVar = ctx.ID().getText();
      return /*variables.get(*/nameVar;
    }
    
    @Override
    public String visitNum(SimpleCParser.NumContext ctx) {
      //System.out.println("Num:" + ctx.getText());
      return ctx.getText();
      //return "int";
    }
    
    @Override
    public String visitParens(SimpleCParser.ParensContext ctx) {
      return visit(ctx.expr());
    }
  }
  
  @Override
  public Void visitProgram(SimpleCParser.ProgramContext ctx) {
    visitChildren(ctx);
    return null;
  }

  @Override
  public Void visitFunction(SimpleCParser.FunctionContext ctx) {
    String name = ctx.ID().getText();
    if (! functions.containsKey(name)) {
      functions.put(name, ctx);
    }

    this.variablesGeneral = new HashMap<String, String>();
    this.variablesGeneral.put("true", "true");
    this.variablesGeneral.put("false", "false");

    this.symbolsToVariableGeneral = new HashMap<String, String>();

    this.paths = new LinkedList<>();
    this.conditionsTree = new BinaryTree(name);
    this.autumnLeaves = new LinkedList<>();

    /*if (ctx.formalParams() != null) {
      for (TerminalNode tn : ctx.formalParams().ID()) this.variables.put(tn.getText(), "int");
    }*/
    for (SimpleCParser.DeclContext dctx : ctx.decl()) visit(dctx);
    for (SimpleCParser.StmtContext sctx : ctx.stmt()) visit(sctx);
    return null;
  }

  @Override
  public Void visitDecl(SimpleCParser.DeclContext ctx) {
    programCounter++;
    instructionsConsidered.append(programCounter);
    instructionsConsidered.append(": ");
    instructionsConsidered.append(ctx.getText());
    instructionsConsidered.append("\n");
    return null;
  }

  @Override
  public Void visitAssignment(SimpleCParser.AssignmentContext ctx) {
    //System.out.println("PC: "+programCounter);
    String nameVar = ctx.ID().getText();
    //String exprVisit = exprVisitor.visit(ctx.expr());
    StringBuilder exprOut = new StringBuilder();
    //check if id is in symbtoVar
    boolean containsSymb = false;

    //check if it needs a symbol or depends on a symbol
    for (ParseTree element: ctx.expr().children) {
      String exprexper = element.getText();
      //for all symbolsToVariable1...n (all paths)

      //
      if(symbolsToVariableGeneral.containsKey(exprexper)){
        exprOut.append(symbolsToVariableGeneral.get(exprexper));
        containsSymb = true;
      } else exprOut.append(variablesGeneral.getOrDefault(exprexper, exprexper));

    }
    //for all symbolsToVariable1...n (all paths)

    //
    if(symbolsToVariableGeneral.containsKey(nameVar)) {
      if(!containsSymb) {
        symbolsToVariableGeneral.remove(nameVar);
        variablesGeneral.put(nameVar, exprOut.toString());
      } else {
        symbolsToVariableGeneral.replace(nameVar, exprOut.toString());
      }
    } else {
      if(variablesGeneral.containsKey(nameVar)) {
        variablesGeneral.replace(nameVar, exprOut.toString());
      } else {
        variablesGeneral.put(nameVar, exprOut.toString());
      }
    }
    programCounter++;
    instructionsConsidered.append(programCounter).append(": ");
    //instructionsConsidered.append(ctx.getText());
    instructionsConsidered.append(nameVar).append("=").append(exprOut).append("\n");
    //System.out.println("variable  "+nameVar + " :" +variablesGeneral.get(nameVar));
    return null;
  }

  @Override
  public Void visitWhile(SimpleCParser.WhileContext ctx) {
    instructionsConsidered.append(programCounter);
    instructionsConsidered.append(": ");
    instructionsConsidered.append(ctx.getText());
    instructionsConsidered.append("\n");
    programCounter++;
    //exprVisitor.visit(ctx.expr());
    visit(ctx.stmt());
    return null;
  }

  @Override
  public Void visitIfThenElse(SimpleCParser.IfThenElseContext ctx) {
    if (ifCurrently == 0){
      autumnLeaves = conditionsTree.getLeaves();
    }
    ifCurrently++;
    StringBuilder symCondition = new StringBuilder();
    //++
    for (ParseTree element: ctx.expr().children) {
      String exprexper = element.getText();
      //System.out.println(exprexper);
      //check in the mapping (variable to symbol) if exprexpr
      //check if expr needs a symbol
      if(symbolsToVariableGeneral.containsKey(exprexper)){
        symCondition.append(symbolsToVariableGeneral.get(exprexper));
      } else symCondition.append(variablesGeneral.getOrDefault(exprexper, exprexper));

    }
    //String exprLiteral = ctx.expr().getText();
    //System.out.println(exprLiteral);
    //
    programCounter++;
    int localPC = programCounter;
    instructionsConsidered.append(programCounter);
    instructionsConsidered.append(": if ");
    instructionsConsidered.append(symCondition);
    instructionsConsidered.append(" {\n");

    ConditionPath pathSaveState = new ConditionPath(variablesGeneral, symbolsToVariableGeneral);
    ConditionPath pathTrueCondition;
    ConditionPath pathFalseCondition;
    /* backupppppp
    conditionsTree.current.left = new Node(symCondition.toString(), conditionsTree.current);
    conditionsTree.current.right = new Node("!(" + symCondition+ ")", conditionsTree.current);
    Node saveRightNodeRef = conditionsTree.current.right;

    conditionsTree.current = conditionsTree.current.left;

     */
    //++
    LinkedList<Node> saveLeftNodeRefs = new LinkedList<>();
    LinkedList<Node> saveRightNodeRefs = new LinkedList<>();

    int i = 0;
    for (Node leaf : autumnLeaves) {
      leaf.left = new Node(symCondition.toString(), leaf);
      leaf.right = new Node("!(" + symCondition + ")", leaf);
      saveLeftNodeRefs.add(leaf.left);
      saveRightNodeRefs.add(leaf.right);
    }

    autumnLeaves = saveLeftNodeRefs;

    /* else {
      conditionsTree.current.left = new Node(symCondition.toString(), conditionsTree.current);
      conditionsTree.current.right = new Node("!(" + symCondition+ ")", conditionsTree.current);
      saveRightNodeRef = conditionsTree.current.right;

      conditionsTree.current = conditionsTree.current.left;
    }*/
    //++

    visit(ctx.stmt(0));
    programCounter++;
    instructionsConsidered.append("} //").append(localPC).append("\n").append(programCounter).append(": else { \n");
    localPC = programCounter;

    //save values before going in to elsebody, saveafterif
    pathTrueCondition = new ConditionPath(variablesGeneral, symbolsToVariableGeneral);
    //restore to savebeforeif
    variablesGeneral = (HashMap<String, String>) pathSaveState.variables.clone();
    symbolsToVariableGeneral = (HashMap<String, String>) pathSaveState.symbolsToVariable.clone();

    /* backupppppp
    conditionsTree.current = saveRightNodeRef;

     */
    //++
    autumnLeaves = saveRightNodeRefs;
    //++

    visit(ctx.stmt(1));

    //save modified values in new path. in paths
    pathFalseCondition = new ConditionPath(variablesGeneral, symbolsToVariableGeneral);

    //save condition
    /*pathTrueCondition.conditions.add(symCondition.toString());
    pathFalseCondition.conditions.add("!(" + symCondition.toString() + ")");*/

    //add condition to all paths
    if(!paths.isEmpty()) {
      for (ConditionPath pe : paths) {
        pe.symbolsToVariable = (HashMap<String, String>) symbolsToVariableGeneral.clone();
        pe.variables = (HashMap<String, String>) variablesGeneral.clone();
      }
    }

    paths.add(pathTrueCondition);
    paths.add(pathFalseCondition);
    //restore to savebeforeif before going in to else
    variablesGeneral = (HashMap<String, String>) pathSaveState.variables.clone();
    symbolsToVariableGeneral = (HashMap<String, String>) pathSaveState.symbolsToVariable.clone();
    instructionsConsidered.append("} //").append(localPC).append("\n");
    ifCurrently--;
    return null;
  }

  @Override
  public Void visitReturn(SimpleCParser.ReturnContext ctx) {
    programCounter++;
    instructionsConsidered.append(programCounter);
    instructionsConsidered.append(": ");
    instructionsConsidered.append(ctx.getText());
    instructionsConsidered.append("\n");
    return null;
  }

  @Override
  public Void visitCompound(SimpleCParser.CompoundContext ctx) {
    for (SimpleCParser.StmtContext sctx : ctx.stmt()) {
      visit(sctx);
    }
    return null;
  }

  @Override
  public Void visitInput(SimpleCParser.InputContext ctx) {
    String variable = ctx.ID().getText();
    symbolsToVariableGeneral.put(variable, "symbol" + symVarCount);
    symVarCount++;

    programCounter++;
    instructionsConsidered.append(programCounter);
    instructionsConsidered.append(": input ").append(variable).append("; (").append(symbolsToVariableGeneral.get(variable)).append(")\n");
    return null;
  }

}
