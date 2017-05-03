package org.tensorflow.ops;

import org.tensorflow.InputListSource;
import org.tensorflow.Operation;
import org.tensorflow.Output;

public abstract class Node {

  protected static Output[] makeOutputs(
      Operation op, int start, String name) {
    int len = op.outputListLength(name);
    final Output[] array = new Output[len];

    int end = start + len;
    for (int i = start; i < end; i++) {
      array[i - start] = op.output(i);
    }
    return array;
  }

}
