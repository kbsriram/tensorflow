package org.tensorflow.operator.processor;

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.lang.model.element.TypeElement;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import org.tensorflow.annotation.Operator;
import java.util.Set;

public class OperatorProcessor extends BasicAnnotationProcessor {

  private OperatorProcessingStep operatorStep;
  
  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  protected Iterable<? extends ProcessingStep> initSteps() {
    operatorStep = new OperatorProcessingStep(
        processingEnv.getMessager(),
        processingEnv.getElementUtils(),
        processingEnv.getFiler());

    return ImmutableList.of(operatorStep);
  }

  private void note(String msg) {
    processingEnv.getMessager().printMessage(Kind.NOTE, msg);
  }

}
