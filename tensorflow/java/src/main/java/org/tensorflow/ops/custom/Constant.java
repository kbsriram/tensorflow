package org.tensorflow.ops.custom;

import org.tensorflow.InputSource;
import org.tensorflow.Operation;
import org.tensorflow.Output;
import org.tensorflow.Tensor;
import org.tensorflow.annotation.Operator;
import org.tensorflow.ops.OperationContext;
import org.tensorflow.ops.array.Const;

@Operator
public final class Constant implements InputSource {

  @Override
  public String toString() {
    return "Constant: " + output.op().name();
  }

  @Override
  public Output input() {
    return output;
  }

  /**
   * Generate constants from Java objects.
   * @param ctx is the operation context
   * @param v is one of certain objects Object
   */
  public Constant(OperationContext ctx, Object v) {
    try (Tensor value = Tensor.create(v)) {
      output = new Const(ctx)
          .withValue(value)
          .withDtype(value.dataType())
          .input();
    }
  }
  private final Output output;
}
