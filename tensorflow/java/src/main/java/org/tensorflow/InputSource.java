package org.tensorflow;

public interface InputSource {

  Output input();

  static InputSource of(final Output output) {
    return new InputSource() {
      public Output input() {
        return output;
      }
    };
  }
}
