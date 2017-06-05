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

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link org.tensorflow.Util}. */
@RunWith(JUnit4.class)
public class UtilTest {
  @Test
  public void toArraySize0() {
    Iterable<String> iterable = Collections.emptySet();
    assertArrayEquals(new String[] {}, Util.toArray(iterable, String.class));
  }

  @Test
  public void toArraySize1() {
    Iterable<String> iterable = Collections.singleton("a");
    assertArrayEquals(new String[] {"a"}, Util.toArray(iterable, String.class));
  }

  @Test
  public void toArraySize2NonCollection() {
    Iterable<Integer> iterable =
        new Iterable<Integer>() {
          @Override
          public Iterator<Integer> iterator() {
            return Arrays.asList(0, 1).iterator();
          }
        };
    assertArrayEquals(new Integer[] {0, 1}, Util.toArray(iterable, Integer.class));
  }

  @Test
  public void toArrayCollectionDoesntIterate() {
    List<Integer> nums = Arrays.asList(1, 2, 3);
    List<Integer> collection =
        new ArrayList<Integer>(nums) {
          @Override
          public Iterator<Integer> iterator() {
            throw new IllegalStateException("Don't iterate me!");
          }
        };
    assertArrayEquals(new Integer[] {1, 2, 3}, Util.toArray(collection, Integer.class));
  }
}
