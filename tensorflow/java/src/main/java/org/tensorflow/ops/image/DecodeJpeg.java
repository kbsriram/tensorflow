package org.tensorflow.ops.image;

import org.tensorflow.InputSource;
import org.tensorflow.ops.Node;
import org.tensorflow.ops.Scope;

// Note: This class would be generated at build time.
public class DecodeJpeg extends Node {

  DecodeJpeg(Scope scope, InputSource contents) {
    this(scope, OP_TYPE, contents);
  }

  DecodeJpeg(Scope scope, String name, InputSource contents) {
    super(scope, OP_TYPE, name, contents);
  }

  public DecodeJpeg withChannels(long value) {
    opBuilder().setAttr("channels", value);
    return this;
  }

  public DecodeJpeg withRatio(long value) {
    opBuilder().setAttr("ratio", value);
    return this;
  }

  public DecodeJpeg withFancyUpscaling(boolean value) {
    opBuilder().setAttr("fancy_upscaling", value);
    return this;
  }

  public DecodeJpeg withTryRecoverTruncated(boolean value) {
    opBuilder().setAttr("try_recover_truncated", value);
    return this;
  }

  public DecodeJpeg withAcceptableFraction(float value) {
    opBuilder().setAttr("acceptable_fraction", value);
    return this;
  }

  public DecodeJpeg withDctMethod(String value) {
    opBuilder().setAttr("dct_method", value);
    return this;
  }

  private static final String OP_TYPE = "DecodeJpeg";
}
