/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/** Unit tests for {@link org.tensorflow.Operation}. */
@RunWith(JUnit4.class)
public class OperationTest {

  @Test
  public void  outputListLengthFailsOnInvalidName() {
      try {
        op.outputListLength("unknown");
        fail("Did not catch bad name");
      } catch (IllegalArgumentException iae) {
        // expected
      }
    }
    return builder.build();
  }

  private Output variable(Graph g, String name, Output value) {
    return g.opBuilder("VariableV2", name)
        .setAttr("shape", value.shape())
        .setAttr("dtype", value.dataType())
        .build()
        .output(0);
  }

  private Output assign(Graph g, String name, Output ref, Output value) {
    return g.opBuilder("Assign", name)
        .addInput(ref)
        .addInput(value)
        .build()
        .output(0);
  }

  @Test
  public void outputListLength() {
    assertEquals(1, split(new int[]{0, 1}, 1));
    assertEquals(2, split(new int[]{0, 1}, 2));
    assertEquals(3, split(new int[]{0, 1, 2}, 3));
  }

  private int split(int[] values, int num_split) {
    try (Graph g = new Graph()) {
      return g.opBuilder("Split", "Split")
          .addInput(TestUtil.constant(g, "split_dim", 0))
          .addInput(TestUtil.constant(g, "values", values))
          .setAttr("num_split", num_split)
          .build()
          .outputListLength("output");
    }
  }
}
