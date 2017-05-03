package org.tensorflow.example;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.InputSource;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Shape;
import org.tensorflow.Tensor;
import org.tensorflow.ops.GraphBuilder;
import java.util.Arrays;
import java.util.List;

public final class HelloWorld {

  private static InputSource buildInference(
      GraphBuilder gb, InputSource images, int nHidden1, int nHidden2) {

    GraphBuilder gbh = gb.withName("hidden1");
    InputSource weights = gbh.withName("weights").variable(
        gbh.random.truncatedNormal(
            gbh.constant(new int[] {28, nHidden1})).withDtype(DataType.FLOAT));

    InputSource biases = gbh.withName("biases").variable
        (gbh.constant(new int[nHidden1]));
    return biases;
  }

  public static void main(String args[]) {
    try (Graph g = new Graph()) {
      GraphBuilder gb = GraphBuilder.create(g);

      InputSource x = gb.array.placeholder().withDtype(DataType.FLOAT);
      InputSource w = gb.variable(gb.constant(new float[784][10]));
      InputSource b = gb.variable(gb.constant(new float[10]));
      InputSource y = gb.nn.softmax(gb.math.add(gb.math.matMul(x, w), b));

      InputSource y_ = gb.array.placeholder().withDtype(DataType.FLOAT);
      InputSource reduce0 = gb.constant(new int[] { 0 });
      InputSource reduce1 = gb.constant(new int[] { 1 });
      InputSource cross_entropy = gb.math.mean(
          gb.math.neg(gb.math.sum(gb.math.mul(
              y_, gb.math.log(y)), reduce1)), reduce0);

      InputSource target = buildInference(gb, cross_entropy, 10, 20);

      try (Session s = new Session(g)) {
        // Initialize variables
        s.runner()
            .addTarget(gb.operationContext().globalInitializersTarget())
            .run();

        try (Tensor feedx = Tensor.create(new float[1][784]);
            Tensor feedy_ = Tensor.create(new float[1][10])) {
          run("result",
              s.runner()
              .feed(x.input(), feedx)
              .feed(y_.input(), feedy_),
              target);
        }
      }
    }
  }

  private static void run(String msg, Session.Runner runner, InputSource target) {
    List<Tensor> results = runner.fetch(target.input()).run();
    for (Tensor result: results) {
      System.out.print(msg + ": ");
      dumpTensor(result);
      result.close();
    }
  }

  private static void dumpTensor(Tensor t) {
    if (t.numDimensions() == 1 &&
        t.dataType() == DataType.INT32) {
      int[] values = new int[(int) t.shape()[0]];
      t.copyTo(values);
      System.out.println(Arrays.toString(values));
    } else {
      System.out.println(t);
    }
  }
}
