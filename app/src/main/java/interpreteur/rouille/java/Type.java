package interpreteur.rouille.java;

public class Type {
  boolean reference = false;
  Token name;

  void setByName(Token name) {
    // TODO: match name by known types in an enum and use them??
    this.name = name;
  }
}
