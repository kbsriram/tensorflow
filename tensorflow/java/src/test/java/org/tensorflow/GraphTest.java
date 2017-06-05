/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link org.tensorflow.Graph}. */
@RunWith(JUnit4.class)
public class GraphTest {

  @Test
  public void graphDefRoundTrip() {
    byte[] graphDef;
    // Create a graph for A * X + B
    try (Graph g = new Graph()) {
      TestUtil.transpose_A_times_X(g, new int[2][2]);
      graphDef = g.toGraphDef();
    }
    // Import the GraphDef and find all the nodes.
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef);
      validateImportedGraph(g, "");
    }
    try (Graph g = new Graph()) {
      g.importGraphDef(graphDef, "BugsBunny");
      validateImportedGraph(g, "BugsBunny/");
    }
  }

  // Helper function whose implementation is based on knowledge of how
  // TestUtil.transpose_A_times_X is implemented.
  private static void validateImportedGraph(Graph g, String prefix) {
    Operation op = g.operation(prefix + "A");
    assertNotNull(op);
    assertEquals(prefix + "A", op.name());
    assertEquals("Const", op.type());
    assertEquals(1, op.numOutputs());
    assertEquals(op, op.output(0).op());

    op = g.operation(prefix + "X");
    assertNotNull(op);
    assertEquals(prefix + "X", op.name());
    assertEquals("Placeholder", op.type());
    assertEquals(1, op.numOutputs());
    assertEquals(op, op.output(0).op());

    op = g.operation(prefix + "Y");
    assertNotNull(op);
    assertEquals(prefix + "Y", op.name());
    assertEquals("MatMul", op.type());
    assertEquals(1, op.numOutputs());
    assertEquals(op, op.output(0).op());
  }

  @Test
  public void failImportOnInvalidGraphDefs() {
    try (Graph g = new Graph()) {
      try {
        g.importGraphDef(null);
        fail("Did not catch invalid graph def");
      } catch (IllegalArgumentException e) {
        // expected exception.
      }

      try {
        g.importGraphDef(new byte[] {1});
        fail("Did not catch invalid graph def");
      } catch (IllegalArgumentException e) {
        // expected exception.
      }
    }
  }

  @Test
  public void failOnUseAfterClose() {
    Graph g = new Graph();
    g.close();
    try {
      g.toGraphDef();
      fail("Did not catch closed graph");
    } catch (IllegalStateException e) {
      // expected exception.
    }
  }

  @Test
  public void testGradients() {
    try (Graph g = new Graph();
        Session s = new Session(g)) {
      Output x = TestUtil.placeholder(g, "x", DataType.FLOAT);

      Output y = inverseSquare(g, x);

      List<Output> dy = g.addGradients(Arrays.asList(y), Arrays.asList(x), null);
      assertEquals(1, dy.size());

      float[] xvals = new float[] {3f, 0.5f, 8f, 0.001f};
      try (Tensor xt = Tensor.create(xvals);
          TestUtil.AutoCloseableList<Tensor> out =
              new TestUtil.AutoCloseableList<Tensor>(
                  s.runner().feed(x, xt).fetch(dy.get(0)).run())) {
        assertEquals(1, out.size());
        verifyGradients(out.get(0), xvals, null);
      }
    }
  }

  @Test
  public void testGradientsWithInitialValue() {
    try (Graph g = new Graph();
        Session s = new Session(g)) {
      Output x = TestUtil.placeholder(g, "x", DataType.FLOAT);

      Output y = inverseSquare(g, x);

      Output dx = TestUtil.placeholder(g, "dx", DataType.FLOAT);

      List<Output> dy = g.addGradients(Arrays.asList(y), Arrays.asList(x), Arrays.asList(dx));
      assertEquals(1, dy.size());

      float[] xvals = new float[] {1f, 3f, -5f, 0.1f};
      float[] dxvals = new float[] {2f, -3f, -0.7f, 0.3f};
      try (Tensor xt = Tensor.create(xvals);
          Tensor dxt = Tensor.create(dxvals);
          TestUtil.AutoCloseableList<Tensor> out =
              new TestUtil.AutoCloseableList<Tensor>(
                  s.runner().feed(x, xt).feed(dx, dxt).fetch(dy.get(0)).run())) {
        assertEquals(1, out.size());
        verifyGradients(out.get(0), xvals, dxvals);
      }
    }
  }

  @Test
  public void failOnGradientUseAfterClose() {
    Graph g = new Graph();
    Output x = TestUtil.placeholder(g, "x", DataType.FLOAT);
    Output y = inverseSquare(g, x);
    g.close();
    try {
      g.addGradients(Arrays.asList(y), Arrays.asList(x), null);
      fail("Did not catch closed graph");
    } catch (IllegalStateException e) {
      // expected exception.
    }
  }

  @Test
  public void failOnGradientWithInvalidInitialValues() {
    try (Graph g = new Graph()) {
      Output x = TestUtil.placeholder(g, "x", DataType.FLOAT);
      Output y = inverseSquare(g, x);
      try {
        g.addGradients(Arrays.asList(y), Arrays.asList(x), Arrays.asList());
        fail("Did not catch incorrect dx count");
      } catch (IllegalArgumentException e) {
        // expected exception.
      }
    }
  }

  // Verify gradients are correctly calculated for y = 1/x^2
  private static void verifyGradients(Tensor t, float[] xvals, float[] dxvals) {
    float[] results = new float[xvals.length];
    t.copyTo(results);

    for (int i = 0; i < xvals.length; i++) {
      float xval = xvals[i];
      // y = 1/x^2
      // dy = -2/x^3 * dx
      float expected = -2f / (xval * xval * xval);
      if (dxvals != null) {
        expected *= dxvals[i];
      }
      assertEquals(expected, results[i], 0.001f);
    }
  }

  // return 1/x^2
  private static Output inverseSquare(Graph g, Output x) {
    Output x_inv = g.opBuilder("Reciprocal", "x_inv").addInput(x).build().output(0);
    return g.opBuilder("Square", "x_inv_squared").addInput(x_inv).build().output(0);
  }
}
