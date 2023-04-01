package interpreteur.rouille.java;

import interpreteur.rouille.java.Expr.Binary;
import interpreteur.rouille.java.Expr.Grouping;
import interpreteur.rouille.java.Expr.Literal;
import interpreteur.rouille.java.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {

  void interpret(Expr expression) {
    try {
      Object value = evaluate(expression);
      System.out.println(stringify(value));
    } catch (RuntimeError e) {
      App.runtimeError(e);
    }
  }

  private String stringify(Object o) {
    if (o == null)
      return "Null 🤨 🤬";
    else
      return o.toString();
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
      default:
        break;
    }

    // Unreachable
    return null;
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
        // Unreachable
        return null;
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

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }
}
