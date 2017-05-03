package org.tensorflow.ops;

import org.tensorflow.Graph;
import org.tensorflow.InputSource;
import org.tensorflow.OperationBuilder;
import org.tensorflow.Operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OperationContext {

  public final OperationContext withName(String name) {
    return new OperationContext(
        graph, nameScope.withSubscope(name), initializers);
  }

  public final NameScope nameScope() {
    return nameScope;
  }

  public final Graph graph() {
    return graph;
  }

  public final OperationContext addGlobalInitializer(InputSource init) {
    initializers.add(init);
    return this;
  }

  public final Operation globalInitializersTarget() {
    OperationBuilder builder = graph.opBuilder(
        "NoOp", nameScope.withSubscope("init").makeOpName("NoOp"));
    for (InputSource input: initializers) {
      builder.addControlInput(input.input().op());
    }
    return builder.build();
  }

  static OperationContext create(Graph g) {
    return new OperationContext(g, NameScope.create(), new ArrayList<>());
  }

  private OperationContext(Graph g, NameScope ns, List<InputSource> inits) {
    graph = g;
    nameScope = ns;
    initializers = inits;
  }

  private final Graph graph;
  private final NameScope nameScope;
  private final List<InputSource> initializers;
}
