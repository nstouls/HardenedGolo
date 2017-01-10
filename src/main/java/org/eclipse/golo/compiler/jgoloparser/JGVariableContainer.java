package org.eclipse.golo.compiler.jgoloparser;

import org.eclipse.golo.compiler.jgoloparser.visitor.SpecTreeVisitor;

import java.util.Set;

public interface JGVariableContainer {

  void accept(SpecTreeVisitor visitor);

  Type getType();

  enum Type {
    OTHER,
    BOOLEAN,
    NUMERIC
  }
}
