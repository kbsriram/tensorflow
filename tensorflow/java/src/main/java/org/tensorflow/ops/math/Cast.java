package org.tensorflow.ops.math;

import org.tensorflow.DataType;
import org.tensorflow.InputSource;
import org.tensorflow.ops.Node;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public class Cast extends Node {

  Cast(Scope scope, InputSource x, DataType y) {
    this(scope, OP_TYPE, x, y);
  }

  Cast(Scope scope, String name, InputSource x, DataType y) {
    super(scope, OP_TYPE, name, x);
    opBuilder().setAttr("DstT", y); // Note: code-generator needs to detect that the output type must be explicitly set
  }

  private static final String OP_TYPE = "Cast";
}
