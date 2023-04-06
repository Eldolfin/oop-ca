package interpreteur.rouille.java;

import java.util.List;
import java.util.Optional;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitPrintStmt(Print stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitAssignStmt(Assign stmt);
    R visitWhileStmt(While stmt);

  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
    
    final Expr expression;
  }

  static class Function extends Stmt {
    Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }
    
    final Token name;
    final List<Token> params;
    final List<Stmt> body;
  }

  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
    
    final Expr expression;
  }

  static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }
    
    final Token keyword;
    final Expr value;
  }

  static class Var extends Stmt {
    Var(Token name, Expr initializer, boolean mutable, Optional<Type> type) {
      this.name = name;
      this.initializer = initializer;
      this.mutable = mutable;
      this.type = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
    
    final Token name;
    final Expr initializer;
    final boolean mutable;
    final Optional<Type> type;
  }

  static class Assign extends Stmt {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignStmt(this);
    }
    
    final Token name;
    final Expr value;
  }

  static class While extends Stmt {
    While(Token whileToken, Expr condition, Expr.Block body) {
      this.whileToken = whileToken;
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }
    
    final Token whileToken;
    final Expr condition;
    final Expr.Block body;
  }

  abstract <R> R accept(Visitor<R> visitor);

}