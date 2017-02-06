package org.tensorflow.ops;

import java.util.HashMap;
import java.util.Map;

import org.tensorflow.Graph;
import org.tensorflow.OperationBuilder;

public class Scope {

  public Scope(Graph graph) {
    this(graph, "");
  }

  public Scope(Graph graph, String prefix) {
    super();
    this.graph = graph;
    this.prefix = prefix;
    nodes = new HashMap<>();
  }

  public Node node(String name) {
    return nodes.get(name);
  }

  OperationBuilder beginNode(Node node, String type, String name) {
    if (currentNode != null) {
      currentNode.op();
    }
    currentNode = node;
    nodes.put(name, node);
    return graph.opBuilder(type, prefix + name);
  }

  private final Graph graph;
  private final String prefix;
  private final Map<String, Node> nodes;
  private Node currentNode;
}
