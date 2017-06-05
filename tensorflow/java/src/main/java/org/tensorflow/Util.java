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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Package-private helper utilities for the core Tensorflow API. */
final class Util {

  /**
   * Copies an iterable's elements into an array.
   *
   * @param iterable the iterable to copy
   * @param type the type of the elements
   * @return a newly-allocated array into which all the elements of the iterable have been copied
   */
  static <T> T[] toArray(Iterable<? extends T> iterable, Class<T> type) {
    if (iterable == null) {
      return null;
    }

    // More efficient for the common case where an Iterable is also a Collection
    Collection<? extends T> collection = castOrCopyToCollection(iterable);
    return collection.toArray(newArray(type, collection.size()));
  }

  /**
   * Returns a new array of the given length with the specified component type.
   *
   * @param type the component type
   * @param length the length of the new array
   */
  @SuppressWarnings("unchecked")
  static <T> T[] newArray(Class<T> type, int length) {
    return (T[]) Array.newInstance(type, length);
  }

  private static <T> Collection<T> castOrCopyToCollection(Iterable<T> iterable) {

    if (iterable instanceof Collection) {
      return (Collection<T>) iterable;
    }

    List<T> list = new ArrayList<T>();
    for (T item : iterable) {
      list.add(item);
    }
    return list;
  }

  private Util() {}
}
