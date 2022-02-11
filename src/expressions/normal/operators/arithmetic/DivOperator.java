package expressions.normal.operators.arithmetic;

import datatypes.NumberValue;
import datatypes.Value;
import expressions.normal.operators.Operator;
import expressions.normal.operators.OperatorTypes.InfixOperator;
import expressions.special.ValueHolder;

public class DivOperator extends Operator {

	public DivOperator(int line, InfixOperator div) {
		super(line, div);
	}

	@Override
	public Associativity getAssociativity() {
		return Associativity.LEFT;
	}

	@Override
	public Value perform(ValueHolder a, ValueHolder b) {
		return NumberValue.div(a.getValue().asNumber(), b.getValue().asNumber());
	}

}
