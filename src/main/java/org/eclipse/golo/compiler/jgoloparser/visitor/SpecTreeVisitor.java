package org.eclipse.golo.compiler.jgoloparser.visitor;

import org.eclipse.golo.compiler.ir.GoloFunction;
import org.eclipse.golo.compiler.jgoloparser.*;

import static org.eclipse.golo.compiler.jgoloparser.JGVariableContainer.Type;

public interface SpecTreeVisitor {

  void verify(GoloFunction function);

  void verify(JGSpecs specification);

  void verify(JGSpec specification);

  Type verify(JGVariableContainer node);

  void assign(JGTerm node, JGFormula formula, Type type);
}
