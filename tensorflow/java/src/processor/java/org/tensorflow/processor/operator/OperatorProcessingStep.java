package org.tensorflow.operator.processor;

import static javax.lang.model.util.ElementFilter.constructorsIn;

import java.io.IOException;
import com.google.auto.common.AnnotationMirrors;
import com.google.common.base.CaseFormat;
import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.tensorflow.annotation.Operator;

class OperatorProcessingStep implements ProcessingStep {

  private final Messager messager;
  private final Elements elements;
  private final Filer filer;
  private boolean hasRun = false;
  private final List<MethodSpec> topMethods = new ArrayList<>();
  private final Map<String, List<MethodSpec>> groups = new HashMap<>();

  OperatorProcessingStep(Messager messager, Elements elements, Filer filer) {
    this.messager = messager;
    this.elements = elements;
    this.filer = filer;
  }
  @Override
  public Set<? extends Class<? extends Annotation>> annotations() {
    return ImmutableSet.of(Operator.class);
  }

  @Override
  public Set<Element> process(
      SetMultimap<Class<? extends Annotation>, Element> multimap) {

    for (Element element: multimap.values()) {
      if (hasRun) {
        error("Cannot handle generated @Operators", element);
      } else {
        generateMethod(element);
      }
    }

    if (!hasRun) {
      generateClasses();
      hasRun = true;
    }

    return ImmutableSet.of();
  }

  private void generateClasses() {
    Map<String, ClassName> groupToClass = new HashMap<>();
    for (Map.Entry<String, List<MethodSpec>> entry : groups.entrySet()) {
      ClassName generated = generateGroupClass(entry.getKey(), entry.getValue());
      groupToClass.put(entry.getKey(), generated);
    }
    generateTopClass(groupToClass);
  }

  private void generateTopClass(Map<String, ClassName> groupToClass) {

    MethodSpec.Builder ctrBuilder =
        MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(T_OPERATION_CONTEXT, "ctx")
        .addStatement("operationContext = ctx");

    for (Map.Entry<String, ClassName> entry: groupToClass.entrySet()) {
      ctrBuilder.addStatement("$L = new $T(ctx)", entry.getKey(), entry.getValue());
    }

    TypeSpec.Builder builder =
        TypeSpec.classBuilder("GraphBuilder")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addMethods(topMethods)
        .addMethod(ctrBuilder.build());

    builder.addMethod(
        MethodSpec.methodBuilder("withName")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(T_STRING, "name")
        .returns(T_GRAPH_BUILDER)
        .addStatement("return new GraphBuilder(operationContext.withName(name))")
        .build());

    builder.addMethod(
        MethodSpec.methodBuilder("create")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(T_GRAPH, "graph")
        .returns(T_GRAPH_BUILDER)
        .addStatement("return new GraphBuilder(OperationContext.create(graph))")
        .build());

    builder.addField(
        FieldSpec.builder(T_OPERATION_CONTEXT, "operationContext")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .build());

    builder.addMethod(
        MethodSpec.methodBuilder("operationContext")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(T_OPERATION_CONTEXT)
        .addStatement("return operationContext")
        .build());

    for (Map.Entry<String, ClassName> entry: groupToClass.entrySet()) {
      builder.addField(
          FieldSpec.builder(entry.getValue(), entry.getKey())
          .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
          .build());
    }

    write(builder.build());
  }

  private ClassName generateGroupClass(String group, List<MethodSpec> methods) {

    MethodSpec.Builder ctrBuilder =
        MethodSpec.constructorBuilder()
        .addParameter(T_OPERATION_CONTEXT, "ctx")
        .addStatement("operationContext = ctx");

    TypeSpec.Builder builder =
        TypeSpec.classBuilder(classFormatFromGroup(group) + "Ops")
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addMethods(methods)
        .addMethod(ctrBuilder.build());

    builder.addField(
        FieldSpec.builder(T_OPERATION_CONTEXT, "operationContext")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .build());

    TypeSpec spec = builder.build();
    write(spec);
    return ClassName.get("org.tensorflow.ops", spec.name);
  }

  private void write(TypeSpec spec) {
    try {
      JavaFile.builder("org.tensorflow.ops", spec)
          .skipJavaLangImports(true)
          .build()
          .writeTo(filer);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private void generateMethod(Element element) {
    TypeElement tn = MoreElements.asType(element);
    AnnotationMirror am = MoreElements.getAnnotationMirror(element, Operator.class).get();
    final ClassName className = ClassName.get(tn);
    
    final String methodName = makeMethodName(className, am);
    final String groupName = makeGroupName(am);

    for (ExecutableElement ctr : constructorsIn(element.getEnclosedElements())) {
      if (canInstantiate(ctr)) {
        MethodSpec method = buildMethod(methodName, className, ctr);
        List<MethodSpec> methods;
        if (groupName == null) {
          // add directly to the GraphBuilder
          methods = topMethods;
        } else {
          methods = groups.get(groupName);
          if (methods == null) {
            methods = new ArrayList<>();
            groups.put(groupName, methods);
          }
        }
        methods.add(method);
      }
    }
  }


  private void note(String msg) {
    messager.printMessage(Kind.NOTE, msg);
  }

  private void error(String msg, Element e) {
    messager.printMessage(Kind.NOTE, msg, e);
  }

  private String makeMethodName(ClassName className, AnnotationMirror am) {
    String name = (String) AnnotationMirrors.getAnnotationValue(am, "name").getValue();
    if ("".equals(name)) {
      return methodFormatFromClass(className.simpleName());
    } else {
      return name;
    }
  }

  private String makeGroupName(AnnotationMirror am) {
    String name = (String) AnnotationMirrors.getAnnotationValue(am, "group").getValue();
    if ("".equals(name)) {
      return null;
    } else {
      return name;
    }
  }

  private static String methodFormatFromClass(String s) {
    String result = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, s);
    if (KEYWORDS.contains(result)) {
      return result + "_";
    } else {
      return result;
    }
  }

  private static String classFormatFromGroup(String s) {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s);
  }

  private MethodSpec buildMethod(String mname, ClassName cname, ExecutableElement ctr) {
    MethodSpec.Builder builder =
        MethodSpec.methodBuilder(mname)
        .addModifiers(Modifier.PUBLIC)
        .returns(cname)
        .varargs(ctr.isVarArgs());

    for (TypeParameterElement tp: ctr.getTypeParameters()) {
      TypeVariableName tvn = TypeVariableName.get((TypeVariable) tp.asType());
      builder.addTypeVariable(tvn);
    }

    StringBuilder call = new StringBuilder("return new $T(operationContext");
    boolean first = true;
    for (VariableElement param : ctr.getParameters()) {
      ParameterSpec p = ParameterSpec.get(param);
      if (first) {
        first = false;
        continue;
      }
      call.append(", ");
      call.append(p.name);
      builder.addParameter(p);
    }
    call.append(")");

    for (TypeMirror thrownType: ctr.getThrownTypes()) {
      builder.addException(TypeName.get(thrownType));
    }

    builder.addStatement(call.toString(), cname);

    String javadoc = elements.getDocComment(ctr);
    if (javadoc != null) {
      builder.addJavadoc("$L", javadoc);
    }
    return builder.build();
  }

  private static boolean canInstantiate(Element e) {
    Set<Modifier> mods = e.getModifiers();
    return !mods.contains(Modifier.ABSTRACT)
        && !mods.contains(Modifier.PRIVATE);
  }


  private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of(
      "assert",
      "const",
      "switch");

  private final static TypeName T_GRAPH_BUILDER =
      ClassName.get("org.tensorflow.ops", "GraphBuilder");
  private final static TypeName T_OPERATION_CONTEXT =
      ClassName.get("org.tensorflow.ops", "OperationContext");
  private final static TypeName T_GRAPH =
      ClassName.get("org.tensorflow", "Graph");
  private final static TypeName T_NAME_SCOPE =
      ClassName.get("org.tensorflow.ops", "NameScope");
  private final static TypeName T_STRING = ClassName.get(String.class);

}
