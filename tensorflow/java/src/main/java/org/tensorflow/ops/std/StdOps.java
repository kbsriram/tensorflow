package org.tensorflow.ops.std;

import org.tensorflow.Tensor;
import org.tensorflow.ops.Scope;

// Note: This class would NOT be generated at build time.
public final class StdOps {

  public static Const constant(Scope scope, Object value) {
    try (Tensor t = Tensor.create(value)) {
      return new Const(scope, t);
    }
  }

  public static Const constant(Scope scope, String name, Object value) {
    try (Tensor t = Tensor.create(value)) {
      return new Const(scope, name, t);
    }
  }

  private StdOps() {
    super();
  }
}
