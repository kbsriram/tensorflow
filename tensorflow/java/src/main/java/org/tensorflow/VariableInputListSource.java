package org.tensorflow;

public interface VariableInputListSource extends InputListSource {
  static VariableInputListSource of(final Output[] outputs) {
    return new VariableInputListSource() {
      public Output[] inputList() {
        return outputs;
      }
    };
  }
}
