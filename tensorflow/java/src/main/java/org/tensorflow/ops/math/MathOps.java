package org.tensorflow.ops.math;

import org.tensorflow.DataType;
import org.tensorflow.InputSource;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public final class MathOps {

  public static Cast cast(Scope scope, InputSource x, DataType y) {
    return new Cast(scope, x, y);
  }

  public static Cast cast(Scope scope, String name, InputSource x, DataType y) {
    return new Cast(scope, name, x, y);
  }

  public static Div div(Scope scope, InputSource x, InputSource y) {
    return new Div(scope, x, y);
  }

  public static Div div(Scope scope, String name, InputSource x, InputSource y) {
    return new Div(scope, name, x, y);
  }

  public static Sub sub(Scope scope, InputSource x, InputSource y) {
    return new Sub(scope, x, y);
  }

  public static Sub sub(Scope scope, String name, InputSource x, InputSource y) {
    return new Sub(scope, name, x, y);
  }

  private MathOps() {
    super();
  }
}
