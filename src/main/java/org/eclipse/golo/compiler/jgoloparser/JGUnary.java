package org.eclipse.golo.compiler.jgoloparser;

import org.eclipse.golo.compiler.jgoloparser.visitor.SpecTreeVisitor;

public class JGUnary implements JGFormula {

  private Operator operator;

  private JGFormula formula;

  public JGUnary(String operator, JGFormula formula) {
    this.operator = Operator.parse(operator);
    this.formula = formula;
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || !(object == null || getClass() != object.getClass())
        && formula.equals(JGUnary.class.cast(object).formula);
  }

  @Override
  public int hashCode() {
    return formula.hashCode();
  }

  @Override
  public String toString() {
    return operator + "(" + formula.toString() + ")";
  }

  @Override
  public Type accept(SpecTreeVisitor visitor) {
    return operator.verify(this, visitor);
  }

  private enum Operator {
    MINUS("-", Type.NUMERIC),
    NEGATIVE("!", Type.BOOLEAN);

    private final String symbol;

    private final Type type;

    Operator(String symbol, Type type) {
      this.symbol = symbol;
      this.type = type;
    }

    public Type verify(JGUnary operator, SpecTreeVisitor visitor) {
      JGVariableContainer formula = operator.formula;
      Type computed = visitor.verify(formula);
      if (computed == null) {
        visitor.assign(JGTerm.class.cast(formula), operator, type);
      } else if (computed != type) {
        System.err.println("Operation " + this.name() + " is performed with non-" + type.name().toLowerCase() + " operand: " + formula);
        return Type.OTHER;
      }
      return type;
    }


    @Override
    public String toString() {
      return symbol;
    }

    static JGUnary.Operator parse(String operator) {
      for (JGUnary.Operator current : values()) {
        if (current.symbol.equals(operator)) {
          return current;
        }
      }
      throw new RuntimeException("Found unknown unary operator: " + operator);
    }
  }
}
