package interpreteur.rouille.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import interpreteur.rouille.java.Expr.Binary;
import interpreteur.rouille.java.Expr.Call;
import interpreteur.rouille.java.Expr.Grouping;
import interpreteur.rouille.java.Expr.If;
import interpreteur.rouille.java.Expr.Literal;
import interpreteur.rouille.java.Expr.Unary;
import interpreteur.rouille.java.Expr.Variable;
import interpreteur.rouille.java.Stmt.Assign;
import interpreteur.rouille.java.Stmt.Expression;
import interpreteur.rouille.java.Stmt.Print;
import interpreteur.rouille.java.Stmt.Var;
import interpreteur.rouille.java.Stmt.While;

public class Interpreter implements
    Expr.Visitor<Object>,
    Stmt.Visitor<Void> {

  final Environment globals = new Environment();
  private Environment environment = globals;
  public boolean repl_mode = false;
  private Map<Object, Integer> locals = new HashMap<>();

  Interpreter() {
    Natives.load(globals);
  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        // TODO: check that top level statements are only function definition (or
        // constants?) (only in !repl_mode)
        execute(statement);
      }
      // this is a hacky way of executing the main function
      if (!repl_mode) {
        var main = new Expr.Call(new Expr.Variable(new Token("principale")), null, new ArrayList<>());
        evaluate(main);
      }
    } catch (RuntimeError e) {
      App.runtimeError(e);
    }
  }

  @Override
  public Void visitExpressionStmt(Expression stmt) {
    var value = evaluate(stmt.expression);
    if (repl_mode) {
      System.out.println(stringify(value));
    }
    return null;
  }

  @Override
  public Void visitPrintStmt(Print stmt) {
    var value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitVarStmt(Var stmt) {
    var value = evaluate(stmt.initializer);
    environment.define(stmt.name.lexeme, value, stmt.mutable);
    return null;
  }

  @Override
  public Void visitAssignStmt(Assign stmt) {
    var value = evaluate(stmt.value);

    var distance = locals.get(stmt);
    if (distance != null) {
      environment.assignAt(distance, stmt.name, value);
    } else {
      globals.assign(stmt.name, value);
    }

    return null;
  }

  @Override
  public Void visitWhileStmt(While stmt) {
    if (stmt.body.returnedValue.isPresent()) {
      throw new RuntimeError(stmt.whileToken,
          "Le block d'un `tant` ne peut pas contenir de valeur de retour implicite.");
    }
    var firstLoop = evaluate(stmt.condition);
    if (!(firstLoop instanceof Boolean)) {
      throw new RuntimeError(stmt.whileToken, "La condition d'un `tant` doit être un booléen.");
    }

    if ((boolean) firstLoop) {
      evaluate(stmt.body);
      while ((boolean) evaluate(stmt.condition)) {
        evaluate(stmt.body);
      }
    }

    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    var function = new Function(stmt, environment);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    throw new Return(evaluate(stmt.value));
  }

  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    var left = evaluate(expr.left);
    if (!(left instanceof Boolean)) {
      throw new RuntimeError(expr.operator,
          "L'opérande gauche de l'operateur logique `" + expr.operator.lexeme + "` doit être un booléen");
    }
    switch (expr.operator.type) {
      case OR:
        if ((boolean) left)
          return true;
        break;
      case AND:
        if (!(boolean) left)
          return false;
      default:
        break;
    }

    var right = evaluate(expr.right);
    if (!(right instanceof Boolean)) {
      throw new RuntimeError(expr.operator,
          "L'opérande droite de l'operateur logique `" + expr.operator.lexeme + "` doit être un booléen");
    }
    return right;
  }

  @Override
  public Object visitBlockExpr(Expr.Block expr) {
    return executeBlock(expr.statements, expr.returnedValue, new Environment(this.environment));
  }

  @Override
  public Object visitIfExpr(If expr) {
    var condition = evaluate(expr.condition);
    if (!(condition instanceof Boolean)) {
      throw new RuntimeError(expr.ifToken, "La condition d'un `si` doit être un booléen.");
    }

    if ((boolean) condition) {
      return evaluate(expr.thenBranch);
    } else if (expr.elseBranch.isPresent()) {
      return evaluate(expr.elseBranch.get());
    } else {
      return new Tuple();
    }
  }

  @Override
  public Object visitVariableExpr(Variable expr) {
    return lookUpVariable(expr.name, expr);
  }

  @Override
  public Object visitBinaryExpr(Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case MINUS:
        if (right instanceof Double && left instanceof Double)
          return (double) left - (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left - (int) right;
        else
          // TODO: Implement binary - for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `-` doivent être des nombres du même type.");
      case SLASH:
        if (right instanceof Double && left instanceof Double)
          return (double) left / (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left / (int) right;
        else
          // TODO: Implement binary / for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `/` doivent être des nombres du même type.");
      case STAR:
        if (right instanceof Double && left instanceof Double)
          return (double) left * (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left * (int) right;
        else
          // TODO: Implement binary * for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `*` doivent être des nombres du même type.");
      case PERCENT:
        if (right instanceof Double && left instanceof Double)
          return (double) left % (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left % (int) right;
        else
          // TODO: Implement binary % for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `%` doivent être des nombres du même type.");
      case PLUS:
        if (right instanceof Double && left instanceof Double)
          return (double) left + (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left + (int) right;
        else if (right instanceof String && left instanceof String)
          return (String) left + (String) right;
        else
          // TODO: Implement binary + for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `+` doivent être des nombres du même type (ou des chaines...).");
      case GREATER:
        if (right instanceof Double && left instanceof Double)
          return (double) left > (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left > (int) right;
        else
          // TODO: Implement binary > for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `>` doivent être des nombres du même type.");
      case GREATER_EQUAL:
        if (right instanceof Double && left instanceof Double)
          return (double) left >= (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left >= (int) right;
        else
          // TODO: Implement binary >= for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `>=` doivent être des nombres du même type.");
      case LESS:
        if (right instanceof Double && left instanceof Double)
          return (double) left < (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left < (int) right;
        else
          // TODO: Implement binary < for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `<` doivent être des nombres du même type.");
      case LESS_EQUAL:
        if (right instanceof Double && left instanceof Double)
          return (double) left <= (double) right;
        else if (right instanceof Integer && left instanceof Integer)
          return (int) left <= (int) right;
        else
          // TODO: Implement binary <= for all rust's number types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `<=` doivent être des nombres du même type.");
      case EQUAL_EQUAL:
        return left.equals(right);
      case BANG_EQUAL:
        return !left.equals(right);
      case AMPERSAND:
        if (right instanceof Integer && left instanceof Integer)
          return (int) left & (int) right;
        else
          // TODO: Implement binary & for all rust's integer types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `&` doivent être des nombres entiers.");
      case BITWISE_OR:
        if (right instanceof Integer && left instanceof Integer)
          return (int) left | (int) right;
        else
          // TODO: Implement binary | for all rust's integer types
          throw new RuntimeError(expr.operator,
              "Les deux operandes de l'operateur binarire `|` doivent être des nombres entiers.");
      default:
        break;
    }

    throw new RuntimeError(null, "Unreachable");
  }

  @Override
  public Object visitCallExpr(Call expr) {
    var callee = evaluate(expr.callee);

    var arguments = new ArrayList<>();
    for (var arg : expr.arguments) {
      arguments.add(evaluate(arg));
    }

    if (!(callee instanceof Callable)) {
      throw new RuntimeError(expr.paren, "Seules les fonctions peuvent être appelées");
    }

    var function = (Callable) callee;

    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren,
          "Mauvais nombre d'arguments passé en paramètre (" +
              function.arity() + " attendus mais " + arguments.size() + " reçus)");
    }

    return function.call(this, arguments);
  }

  @Override
  public Object visitUnaryExpr(Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case MINUS:
        if (right instanceof Double)
          return -(double) right;
        else if (right instanceof Integer)
          return -(int) right;
        else
          // TODO: Implement unary - for all rust's number types
          throw new RuntimeError(expr.operator, "Les Operandes de l'operateur unaire `-` doivent être des nombres.");
      case BANG:
        if (right instanceof Boolean)
          return !(boolean) right;
        else if (right instanceof Integer)
          // in rust, i_ and u_ implements unary not, and it's bitwise
          // but java doesn't, so we XOR it with ones
          return (int) right ^ Integer.MAX_VALUE;
        else
          // TODO: Implement unary ! for all rust's integer types
          throw new RuntimeError(expr.operator,
              "Les Operandes de l'operateur unaire `!` doivent être des entiers (u_ ou i_).");
      default:
        throw new RuntimeError(null, "Unreachable");
    }
  }

  @Override
  public Object visitGroupingExpr(Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitTupleExpr(Expr.Tuple expr) {
    var items = new ArrayList<Object>();
    for (var e : expr.expressions) {
      items.add(evaluate(e));
    }

    return new Tuple(items);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt statement) {
    statement.accept(this);
  }

  Object executeBlock(List<Stmt> statements, Environment environment) {
    return executeBlock(statements, Optional.empty(), environment);
  }

  Object executeBlock(List<Stmt> statements, Optional<Expr> returnedValue, Environment environment) {
    var previousEnv = this.environment;
    Object result = new Tuple();
    try {
      this.environment = environment;

      for (var statement : statements) {
        execute(statement);
      }
      if (returnedValue.isPresent())
        result = evaluate(returnedValue.get());
    } finally {
      this.environment = previousEnv;
    }

    return result;
  }

  public void resolve(Object exprOrStmt, int depth) {
    locals.put(exprOrStmt, depth);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }

  private String stringify(Object o) {
    if (o == null)
      return "Null 🤨 🤬";
    else if (o instanceof Boolean)
      return ((boolean) o) ? "vrai" : "faux";
    else
      return o.toString();
  }
}
