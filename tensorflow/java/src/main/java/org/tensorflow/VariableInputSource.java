package org.tensorflow;

public interface VariableInputSource extends InputSource {
  static VariableInputSource of(final Output output) {
    return new VariableInputSource() {
      public Output input() {
        return output;
      }
    };
  }
}
