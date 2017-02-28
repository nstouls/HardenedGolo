package org.eclipse.golo.compiler.jgoloparser;

public class JGSpec {

  private JGFormula formula;

  private Type type;

  public  JGSpec(String type, JGFormula form) {
    this.type = Type.parse(type);
    this.formula = form;
  }

  public JGFormula getFormula() {
    return formula;
  }

  @Override
  public String toString() {
    return type + " { " + formula + " } ";
  }

  private enum Type {
    ENSURES("ensures"),
    REQUIRES("requires"),
    INVARIANT("invariant"),
    VARIANT("variant");

    private final String symbol;

    Type(String symbol) {
      this.symbol = symbol;
    }

    @Override
    public String toString() {
      return symbol;
    }

    static Type parse(String spec) {
      for (Type current : values()) {
        if (current.symbol.equalsIgnoreCase(spec)) {
          return current;
        }
      }
      throw new RuntimeException("Found unknown type of spec: " + spec);
    }
  }
}
