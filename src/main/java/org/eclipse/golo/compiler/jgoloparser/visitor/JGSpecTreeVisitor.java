package org.eclipse.golo.compiler.jgoloparser.visitor;

import org.eclipse.golo.compiler.ir.GoloFunction;
import org.eclipse.golo.compiler.ir.LocalReference;
import org.eclipse.golo.compiler.ir.ReferenceTable;
import org.eclipse.golo.compiler.jgoloparser.*;

import java.util.*;

import static org.eclipse.golo.compiler.jgoloparser.JGVariableContainer.Type;
import static java.util.Map.Entry;

public class JGSpecTreeVisitor implements SpecTreeVisitor {

  private ReferenceTable originalTable;

  private TypedReferenceTable referenceTable;

  @Override
  public void verify(GoloFunction function) {
    JGSpecs specs = function.getSpecification();
    if (specs != null) {
      originalTable = function.getBlock().getReferenceTable();
      referenceTable = new TypedReferenceTable();
      verify(specs);
      assignTypesToReferences();
    }
  }

  @Override
  public void verify(JGSpecs node) {
    node.getSpecList().forEach(this::verify);
  }

  @Override
  public void verify(JGSpec node) {
    node.getFormula().accept(this);
  }

  @Override
  public Type verify(JGVariableContainer node) {
    return node.accept(this);
  }

  @Override
  public void assign(JGTerm term, JGFormula operator, Type type) {
    referenceTable.assign(term, operator, type);
  }

  private void assignTypesToReferences() {
    computedAll: while (true) {
      for (Entry<JGTerm, Map<Type, Set<JGFormula>>> termInfo : referenceTable) {
        JGTerm term = termInfo.getKey();
        Set<Type> assignedTypes = termInfo.getValue().keySet();
        if (assignedTypes.contains(Type.NUMERIC_OR_BOOLEAN) && assignedTypes.contains(Type.NUMERIC)) {
          updateTypes(termInfo.getValue(), Type.NUMERIC);
          continue computedAll;
        } else if (assignedTypes.contains(Type.NUMERIC_OR_BOOLEAN) && assignedTypes.contains(Type.BOOLEAN)) {
          updateTypes(termInfo.getValue(), Type.BOOLEAN);
          continue computedAll;
        } else if (assignedTypes.contains(Type.NUMERIC) && assignedTypes.contains(Type.BOOLEAN)) {
          System.err.println("Variable '" + term + "' used as boolean and numeric at the same time:");
          printFormulas(Type.NUMERIC, termInfo.getValue().get(Type.NUMERIC));
          printFormulas(Type.BOOLEAN, termInfo.getValue().get(Type.BOOLEAN));
          System.err.println("");
        }
      }
      break;
    }
    for (Entry<JGTerm, Map<Type, Set<JGFormula>>> termInfo : referenceTable) {
      Set<Type> assignedTypes = termInfo.getValue().keySet();
      assignType(termInfo.getKey(), assignedTypes, Type.NUMERIC);
      assignType(termInfo.getKey(), assignedTypes, Type.BOOLEAN);
    }
  }

  private void assignType(JGTerm term, Set<Type> assignedTypes, Type expected) {
    if (assignedTypes.size() == 1 && assignedTypes.contains(expected)) {
      LocalReference reference = originalTable.get(term.getName());
      if (reference != null) {
        reference.setType(expected);
      }
    }
  }

  private void printFormulas(Type type, Set<JGFormula> formulas) {
    System.err.println("\t" + type + ":");
    for (JGFormula formula : formulas) {
      System.err.println("\t\t" + formula);
    }
  }

  private void updateTypes(Map<Type, Set<JGFormula>> formulasByTypes, Type expected) {
    Set<JGFormula> formulas = formulasByTypes.get(Type.NUMERIC_OR_BOOLEAN);
    for (JGFormula formula : formulas) {
      JGBinary operator = JGBinary.class.cast(formula);
      assign(JGTerm.class.cast(operator.getLeft()), operator, expected);
      assign(JGTerm.class.cast(operator.getRight()), operator, expected);
    }
    formulasByTypes.remove(Type.NUMERIC_OR_BOOLEAN);
  }

  private class TypedReferenceTable implements Iterable<Entry<JGTerm, Map<Type, Set<JGFormula>>>> {

    private Map<JGTerm, Map<Type, Set<JGFormula>>> table = new HashMap<>();

    public void assign(JGTerm term, JGFormula operator, Type type) {
      Map<Type, Set<JGFormula>> record = table.computeIfAbsent(term, k -> new HashMap<>());
      Set<JGFormula> formulas = record.computeIfAbsent(type, k -> new HashSet<>());
      formulas.add(operator);
    }

    @Override
    public Iterator<Entry<JGTerm, Map<Type, Set<JGFormula>>>> iterator() {
      return table.entrySet().iterator();
    }
  }
}
