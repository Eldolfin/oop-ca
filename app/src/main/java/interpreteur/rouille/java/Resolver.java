package interpreteur.rouille.java;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import interpreteur.rouille.java.Expr.Binary;
import interpreteur.rouille.java.Expr.Block;
import interpreteur.rouille.java.Expr.Call;
import interpreteur.rouille.java.Expr.Grouping;
import interpreteur.rouille.java.Expr.If;
import interpreteur.rouille.java.Expr.Literal;
import interpreteur.rouille.java.Expr.Logical;
import interpreteur.rouille.java.Expr.Tuple;
import interpreteur.rouille.java.Expr.Unary;
import interpreteur.rouille.java.Expr.Variable;
import interpreteur.rouille.java.Stmt.Assign;
import interpreteur.rouille.java.Stmt.Expression;
import interpreteur.rouille.java.Stmt.Function;
import interpreteur.rouille.java.Stmt.Print;
import interpreteur.rouille.java.Stmt.Return;
import interpreteur.rouille.java.Stmt.Var;
import interpreteur.rouille.java.Stmt.While;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  private enum FunctionType {
    NONE,
    FUNCTION,
  }

  @Override
  public Void visitExpressionStmt(Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(Function stmt) {
    define(stmt.name);

    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  @Override
  public Void visitPrintStmt(Print stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitReturnStmt(Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      App.error(stmt.keyword, "`renvoie` ne peut pas être utilisé hors d'une fonction.");
    }

    resolve(stmt.value);
    return null;
  }

  @Override
  public Void visitVarStmt(Var stmt) {
    declare(stmt.name);
    resolve(stmt.initializer);
    define(stmt.name);
    return null;
  }

  @Override
  public Void visitAssignStmt(Assign stmt) {
    resolve(stmt.value);
    resolveLocal(stmt, stmt.name);
    return null;
  }

  @Override
  public Void visitWhileStmt(While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitCallExpr(Call expr) {
    resolve(expr.callee);

    for (var argument : expr.arguments) {
      resolve(argument);
    }

    return null;
  }

  @Override
  public Void visitGroupingExpr(Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Literal expr) {
    return null;
  }

  @Override
  public Void visitTupleExpr(Tuple expr) {
    for (var e : expr.expressions) {
      resolve(e);
    }
    return null;
  }

  @Override
  public Void visitLogicalExpr(Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitVariableExpr(Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
      App.error(expr.name, "Une variable ne peut pas être définie par elle-même");
    }

    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visitBlockExpr(Block expr) {
    beginScope();
    resolve(expr.statements);
    if (expr.returnedValue.isPresent()) {
      resolve(expr.returnedValue.get());
    }
    endScope();
    return null;
  }

  @Override
  public Void visitIfExpr(If expr) {
    resolve(expr.condition);
    resolve(expr.thenBranch);
    if (expr.elseBranch.isPresent())
      resolve(expr.elseBranch.get());
    return null;
  }

  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty())
      return;

    var scope = scopes.peek();
    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty())
      return;

    var scope = scopes.peek();
    scope.put(name.lexeme, true);
  }

  private void resolveLocal(Object exprOrStmt, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(exprOrStmt, scopes.size() - 1 - i);
        return;
      }
    }
  }

  private void resolveFunction(Stmt.Function function, FunctionType type) {
    var enclosingFunctionType = currentFunction;
    currentFunction = type;

    beginScope();
    for (var param : function.params)
      define(param);

    resolve(function.body);
    endScope();

    currentFunction = enclosingFunctionType;
  }

  void resolve(List<Stmt> statements) {
    for (var statement : statements) {
      resolve(statement);
    }
  }

  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }
}
