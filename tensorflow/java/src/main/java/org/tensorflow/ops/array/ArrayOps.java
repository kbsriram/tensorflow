package org.tensorflow.ops.array;

import org.tensorflow.InputSource;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public final class ArrayOps {

  public static ExpandDims expandDims(Scope scope, InputSource input, InputSource size) {
    return new ExpandDims(scope, input, size);
  }

  public static ExpandDims expandDims(Scope scope, String name, InputSource input, InputSource size) {
    return new ExpandDims(scope, name, input, size);
  }

  private ArrayOps() {
    super();
  }
}
