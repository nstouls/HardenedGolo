package org.eclipse.golo.compiler.jgoloparser;

import org.eclipse.golo.compiler.jgoloparser.visitor.SpecTreeVisitor;

public interface JGVariableContainer {

  Type accept(SpecTreeVisitor visitor);

  enum Type {
    OTHER,
    BOOLEAN,
    NUMERIC,
    NUMERIC_OR_BOOLEAN
  }
}
