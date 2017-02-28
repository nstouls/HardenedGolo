/* Copyright 2014 Dominic Scheurer
 *
 * This file is part of FirstOrderParser.
 *
 * FirstOrderParser is free software: you can redistribute it and/or modify
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

public class JGTerm implements JGFormula {

  private String name;

  private Type type;

  public JGTerm(JGLiteral lit) {
    this(lit.toString());
    Object value = lit.getValue();
    if (Number.class.isInstance(value)) {
      type = Type.NUMERIC;
    } else if (Boolean.class.isInstance(value)) {
      type = Type.BOOLEAN;
    }
  }

  public JGTerm(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isConstant() {
    return Character.isUpperCase(name.charAt(0));
  }

  public boolean isVariable() {
    return Character.isLowerCase(name.charAt(0));
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Type accept(SpecTreeVisitor visitor) {
    return type;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    JGTerm another = (JGTerm) object;
    return name.equals(another.name);
  }
}
