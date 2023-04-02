package interpreteur.rouille.java;

import java.util.List;

class Natives {
  public static void load(Environment globals) {
    globals.define("clock", new Clock());
  }
}

class Clock implements Callable {
  @Override
  public int arity() {
    return 0;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    return (double) System.nanoTime() / 1e9;
  }
}
