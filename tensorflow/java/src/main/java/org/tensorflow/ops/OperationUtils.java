package org.tensorflow.ops;

import org.tensorflow.InputListSource;
import org.tensorflow.Operation;
import org.tensorflow.Output;

public final class OperationUtils {

  // Create an InputListSource from a named output for an operation.
  public static InputListSource makeInputList(Operation op, int start, String name) {
    int len = op.outputListLength(name);
    final Output[] array = new Output[len];

    int end = start + len;
    for (int i = start; i < end; i++) {
      array[i - start] = op.output(i);
    }
    return InputListSource.of(array);
  }

  private OperationUtils() {
  }
}
