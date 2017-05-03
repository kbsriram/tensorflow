package org.tensorflow.ops.custom;

import org.tensorflow.VariableInputSource;
import org.tensorflow.InputSource;
import org.tensorflow.Operation;
import org.tensorflow.Output;
import org.tensorflow.annotation.Operator;
import org.tensorflow.ops.OperationContext;
import org.tensorflow.ops.state.Assign;
import org.tensorflow.ops.state.VariableV2;

@Operator
public final class Variable implements VariableInputSource {

  @Override
  public Output input() {
    return ref.input();
  }

  public Variable(OperationContext ctx, InputSource init) {
    ref = new VariableV2(ctx)
        .withShape(init.input().shape())
        .withDtype(init.input().dataType());
    ctx.addGlobalInitializer(new Assign(ctx.withName("gen_"), ref, init));
  }

  private final VariableV2 ref;
}
