package org.tensorflow.ops.math;

import org.tensorflow.InputSource;
import org.tensorflow.ops.Node;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public class Sub extends Node {

  Sub(Scope scope, InputSource x, InputSource y) {
    this(scope, OP_TYPE, x, y);
  }

  Sub(Scope scope, String name, InputSource x, InputSource y) {
    super(scope, OP_TYPE, name, x, y);
  }

  private static final String OP_TYPE = "Sub";
}
