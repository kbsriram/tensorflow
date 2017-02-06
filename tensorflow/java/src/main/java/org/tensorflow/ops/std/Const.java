package org.tensorflow.ops.std;

import org.tensorflow.Tensor;
import org.tensorflow.ops.Node;
import org.tensorflow.ops.Scope;

// Note: This class would NOT be generated at build time.
public class Const extends Node {

  Const(Scope scope, Tensor t) {
    this(scope, OP_TYPE, t);
  }

  Const(Scope scope, String name, Tensor t) {
    super(scope, OP_TYPE, name);
    opBuilder().setAttr("dtype", t.dataType()).setAttr("value", t);
  }

  private static final String OP_TYPE = "Const";
}
