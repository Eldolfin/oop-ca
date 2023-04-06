package interpreteur.rouille.java;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Tuple {
  List<Object> items;

  Tuple(Object... objects) {
    items = Arrays.asList(objects);
  }

  boolean isEmpty() {
    return items.isEmpty();
  }

  void add(Object o) {
    items.add(o);
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "()";
    }
    var res = items.stream()
        .map(Object::toString)
        .collect(Collectors.joining(", "));
    res = res.substring(1, res.length() - 1);
    return "(" + res + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Tuple)) {
      return false;
    }
    var otherItems = ((Tuple) obj).items;

    if (items.size() != otherItems.size()) {
      return false;
    }
    for (int i = 0; i < items.size(); i++) {
      if (!items.get(i).equals(otherItems.get(i))) {
        return false;
      }
    }

    return true;
  }
}
