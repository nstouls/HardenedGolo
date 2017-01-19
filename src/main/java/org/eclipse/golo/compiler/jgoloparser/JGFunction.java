package org.eclipse.golo.compiler.jgoloparser;

import java.util.ArrayList;

public class JGFunction extends JGTerm {

  private ArrayList<JGFormula> innerTerms;

  public JGFunction(String name, ArrayList<JGFormula> innerTerms) {
    super(name);
    this.innerTerms = innerTerms;
  }

  public int arity() {
    return innerTerms.size();
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(super.getName()).append(" (");

    for (JGFormula innerTerm : innerTerms) {
      result.append(innerTerm).append(" ");
    }
    result.append(")");
    return result.toString();
  }
}
