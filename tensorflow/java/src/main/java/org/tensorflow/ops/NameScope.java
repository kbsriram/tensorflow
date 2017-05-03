package org.tensorflow.ops;

import java.util.HashMap;
import java.util.Map;

public final class NameScope {

  public NameScope withSubscope(String ns) {
    ns = makeUnique(ns);
    return new NameScope(ns);
  }

  public String makeOpName(String op) {
    return makeUnique(op);
  }

  static NameScope create() {
    return new NameScope(null);
  }

  private NameScope(String ns) {
    this.ns = ns;
  }

  private String makeUnique(String candidate) {
    String result;

    if (!ids.containsKey(candidate)) {
      ids.put(candidate, 1);
      result = candidate;
    } else {
      int cur = ids.get(candidate);
      result = String.format("%s_%d", candidate, cur);
      ids.put(candidate, cur + 1);
    }
    if (ns == null) {
      return result;
    } else {
      return String.format("%s/%s", ns, result);
    }
  }

  private final Map<String,Integer> ids = new HashMap<>();
  private final String ns;
}
