package org.tensorflow.ops;

import org.tensorflow.InputSource;
import org.tensorflow.Operation;
import org.tensorflow.OperationBuilder;
import org.tensorflow.Output;

public abstract class Node implements InputSource {

  protected Node(Scope scope, String type, String name, InputSource... inputs) {
    super();
    opBuilder = scope.beginNode(this, type, name);
    for (InputSource input : inputs) {
      opBuilder.addInput(input);
    }
  }

  @Override
  public Output input() {
    return output(0);
  }

  public Output output(int idx) {
    return op().output(idx);
  }

  public Operation op() {
    if (op == null) {
      op = opBuilder.build();
    }
    return op;
  }

  protected OperationBuilder opBuilder() {
    return opBuilder;
  }

  private final OperationBuilder opBuilder;
  private Operation op;
}
