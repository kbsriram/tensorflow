package org.tensorflow.ops.image;

import org.tensorflow.InputSource;
import org.tensorflow.ops.Node;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public class ResizeBilinear extends Node {

  ResizeBilinear(Scope scope, InputSource images, InputSource size) {
    this(scope, OP_TYPE, images, size);
  }

  ResizeBilinear(Scope scope, String name, InputSource images, InputSource size) {
    super(scope, OP_TYPE, name, images, size);
  }

  public ResizeBilinear withAlignCorners(boolean value) {
    opBuilder().setAttr("align_corners", value);
    return this;
  }

  private static final String OP_TYPE = "ResizeBilinear";
}
