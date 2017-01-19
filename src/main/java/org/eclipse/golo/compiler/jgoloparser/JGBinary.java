/* Copyright 2016 INSA Lyon
 * Inspired from "FirstOrderParser" by Dominic Scheurer.
 *
 * HardenedGolo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FirstOrderParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FirstOrderParser.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclipse.golo.compiler.jgoloparser;

import org.eclipse.golo.compiler.jgoloparser.visitor.SpecTreeVisitor;

public class JGBinary implements JGFormula {

  private JGFormula left;

  private Operator operator;

  private JGFormula right;

  public JGBinary(JGFormula left, String operator, JGFormula right) {
    this.operator = Operator.parse(operator);
    this.left = left;
    this.right = right;
  }

  public JGFormula getLeft() {
    return left;
  }

  public JGFormula getRight() {
    return right;
  }

  @Override
  public Type accept(SpecTreeVisitor visitor) {
    return operator.checkType(this, visitor);
  }

  @Override
  public String toString() {
    return "(" + left + " " + operator + " " + right + ")";
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    JGBinary another = (JGBinary) object;
    return operator == another.operator
        && left.equals(another.left)
        && right.equals(another.right);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  private enum Operator {
    PLUS("+", Type.NUMERIC),
    LESS("<", Type.BOOLEAN, Type.NUMERIC),
    MINUS("-", Type.NUMERIC),
    DIVIDE("/", Type.NUMERIC),
    MODULO("%", Type.NUMERIC),
    GREATER(">", Type.BOOLEAN, Type.NUMERIC),
    MULTIPLICATION("*", Type.NUMERIC),
    LESS_OR_EQUALS("<=", Type.BOOLEAN, Type.NUMERIC),
    GREATER_OR_EQUALS(">=", Type.BOOLEAN, Type.NUMERIC),

    IMPLICATIVE("->", Type.BOOLEAN),
    DISJUNCTIVE("\\/", Type.BOOLEAN),
    CONJUNCTIVE("/\\", Type.BOOLEAN),

    EQUALS("=", Type.NUMERIC_OR_BOOLEAN) {
      @Override
      public Type checkType(JGBinary operator, SpecTreeVisitor visitor) {
        return checkNumericOrBoolean(operator, visitor);
      }
    },
    NOT_EQUALS("<>", Type.NUMERIC_OR_BOOLEAN) {
      @Override
      public Type checkType(JGBinary operator, SpecTreeVisitor visitor) {
        return checkNumericOrBoolean(operator, visitor);
      }
    };

    private final String symbol;

    private final Type operandsType;

    private final Type operatorType;

    Operator(String symbol, Type operatorType) {
      this(symbol, operatorType, operatorType);
    }

    Operator(String symbol, Type operatorType, Type operandsType) {
      this.symbol = symbol;
      this.operatorType = operatorType;
      this.operandsType = operandsType;
    }

    public Type checkType(JGBinary operator, SpecTreeVisitor visitor) {
        Type typeLeft = check(operator, operator.left, visitor);
        Type typeRight = check(operator, operator.right, visitor);
        return typeLeft == operandsType && typeRight == operandsType ? operatorType : Type.OTHER;
    }

    private Type check(JGBinary operator, JGVariableContainer operand, SpecTreeVisitor visitor) {
      Type type = visitor.verify(operand);
      if (type == null) {
        visitor.assign(JGTerm.class.cast(operand), operator, operandsType);
      } else if (type != operandsType) {
        System.err.println("Operation " + this.name() + " is performed with non-" + operandsType + " operand: " + operand);
        return Type.OTHER;
      }
      return operandsType;
    }

    protected Type checkNumericOrBoolean(JGBinary operator, SpecTreeVisitor visitor) {
      JGVariableContainer left = operator.left;
      JGVariableContainer right = operator.right;
      Type typeLeft = visitor.verify(left);
      Type typeRight = visitor.verify(right);
      if (typeLeft == null && typeRight == null) {
        visitor.assign(JGTerm.class.cast(left), operator, Type.NUMERIC_OR_BOOLEAN);
        visitor.assign(JGTerm.class.cast(right), operator, Type.NUMERIC_OR_BOOLEAN);
        return Type.BOOLEAN;
      } else if (typeLeft == null && typeRight == Type.NUMERIC) {
        visitor.assign(JGTerm.class.cast(left), operator, Type.NUMERIC);
        return Type.BOOLEAN;
      } else if (typeLeft == Type.NUMERIC && typeRight == null) {
        visitor.assign(JGTerm.class.cast(right), operator, Type.NUMERIC);
        return Type.BOOLEAN;
      } else if (typeLeft == null && typeRight == Type.BOOLEAN) {
        visitor.assign(JGTerm.class.cast(left), operator, Type.BOOLEAN);
        return Type.BOOLEAN;
      } else if (typeLeft == Type.BOOLEAN && typeRight == null) {
        visitor.assign(JGTerm.class.cast(right), operator, Type.BOOLEAN);
        return Type.BOOLEAN;
      } else if (typeLeft == Type.BOOLEAN && typeRight == Type.BOOLEAN || typeLeft == Type.NUMERIC && typeRight == Type.NUMERIC) {
        return Type.BOOLEAN;
      }
      System.err.println("Operation " + this.name() + " is performed with wrong operands: " + operator.left + " " + operator.right);
      return Type.OTHER;
    }

    @Override
    public String toString() {
      return symbol;
    }

    static Operator parse(String operator) {
      for (Operator current : values()) {
        if (current.symbol.equals(operator)) {
          return current;
        }
      }
      throw new RuntimeException("Found unknown binary operator: " + operator);
    }
  }
}
