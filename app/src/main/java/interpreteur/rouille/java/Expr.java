package interpreteur.rouille.java;

import java.util.List;
import java.util.Optional;

abstract class Expr {
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);
    R visitCallExpr(Call expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitTupleExpr(Tuple expr);
    R visitLogicalExpr(Logical expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    R visitBlockExpr(Block expr);
    R visitIfExpr(If expr);

  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
    
    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
    
    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }

  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
    
    final Expr expression;
  }

  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
    
    final Object value;
  }

  static class Tuple extends Expr {
    Tuple(List<Expr> expressions) {
      this.expressions = expressions;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTupleExpr(this);
    }
    
    final List<Expr> expressions;
  }

  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }
    
    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
    
    final Token operator;
    final Expr right;
  }

  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
    
    final Token name;
  }

  static class Block extends Expr {
    Block(List<Stmt> statements, Optional<Expr> returnedValue) {
      this.statements = statements;
      this.returnedValue = returnedValue;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockExpr(this);
    }
    
    final List<Stmt> statements;
    final Optional<Expr> returnedValue;
  }

  static class If extends Expr {
    If(Token ifToken, Expr condition, Expr thenBranch, Optional<Expr> elseBranch) {
      this.ifToken = ifToken;
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfExpr(this);
    }
    
    final Token ifToken;
    final Expr condition;
    final Expr thenBranch;
    final Optional<Expr> elseBranch;
  }

  abstract <R> R accept(Visitor<R> visitor);

}