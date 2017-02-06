package org.tensorflow.ops.array;

import org.tensorflow.InputSource;
import org.tensorflow.ops.Node;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public class ExpandDims extends Node {

  ExpandDims(Scope scope, InputSource input, InputSource dim) {
    this(scope, OP_TYPE, input, dim);
  }

  ExpandDims(Scope scope, String name, InputSource input, InputSource dim) {
    super(scope, OP_TYPE, name, input, dim);
  }

  private static final String OP_TYPE = "ExpandDims";
}
