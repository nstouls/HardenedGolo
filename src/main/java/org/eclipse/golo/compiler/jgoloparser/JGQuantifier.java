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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JGQuantifier implements JGFormula {

  private static final Set<String> WHYML_TYPES = new HashSet<>(Arrays.asList(
    "char",
    "byte",
    "short",
    "int",
    "long",
    "float",
    "double"
  ));

  private Operator operator;

  private JGTerm typeQuantifier;

  private JGTerm quantifiedBy;

  private JGFormula formula;

  public JGQuantifier(String operator, JGTerm quantifiedBy, JGTerm typeQuantifier, JGFormula formula) {
    this.operator = Operator.parse(operator);
    this.quantifiedBy = quantifiedBy;
    this.formula = formula;
    this.typeQuantifier = typeQuantifier;
  }

  @Override
  public Type accept(SpecTreeVisitor visitor) {
    if (!isAllowableWhyMLType(typeQuantifier)) {
      System.err.println("Type '" + typeQuantifier + "' isn't allowed in WHYML!");
    }
    if (visitor.verify(formula) != Type.BOOLEAN) {
      System.err.println("Operation " + operator.name() + " is performed with non-boolean operand: " + formula);
      return Type.OTHER;
    }
    return Type.BOOLEAN;
  }

  private boolean isAllowableWhyMLType(JGTerm type) {
    return WHYML_TYPES.contains(type.toString());
  }

  @Override
  public String toString() {
    return operator + " " + quantifiedBy + ":" + typeQuantifier + ". ( " + formula + " )";
  }

  private enum Operator {
    EXISTS("exists"),
    FORALL("forall");

    private final String symbol;

    Operator(String symbol) {
      this.symbol = symbol;
    }

    @Override
    public String toString() {
      return symbol;
    }

    static JGQuantifier.Operator parse(String operator) {
      for (JGQuantifier.Operator current : values()) {
        if (current.symbol.equals(operator)) {
          return current;
        }
      }
      throw new RuntimeException("Found unknown quantifier: " + operator);
    }
  }
}
