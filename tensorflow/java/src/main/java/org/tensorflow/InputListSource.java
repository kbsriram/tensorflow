package org.tensorflow;

public interface InputListSource {

  Output[] inputList();

  static InputListSource of(final Output[] outputs) {
    return new InputListSource() {
      public Output[] inputList() {
        return outputs;
      }
    };
  }
}
