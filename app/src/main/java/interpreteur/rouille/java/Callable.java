package interpreteur.rouille.java;

import java.util.List;

public interface Callable {
  int arity();

  Object call(Interpreter interpreter, List<Object> arguments);
}
