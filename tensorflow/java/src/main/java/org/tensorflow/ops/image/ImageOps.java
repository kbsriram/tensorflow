package org.tensorflow.ops.image;

import org.tensorflow.InputSource;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public final class ImageOps {
	
  public static DecodeJpeg decodeJpeg(Scope scope, InputSource contents) {
    return new DecodeJpeg(scope, contents);
  }

  public static DecodeJpeg decodeJpeg(Scope scope, String name, InputSource contents) {
    return new DecodeJpeg(scope, name, contents);
  }

  public static ResizeBilinear resizeBilinear(Scope scope, InputSource images, InputSource size) {
    return new ResizeBilinear(scope, images, size);
  }

  public static ResizeBilinear resizeBilinear(Scope scope, String name, InputSource images, InputSource size) {
    return new ResizeBilinear(scope, name, images, size);
  }

  private ImageOps() {
    super();
  }
}
