package expressions.possible;

import static parsing.program.ExpressionType.ARRAY_START;
import static parsing.program.ExpressionType.LITERAL;
import static parsing.program.ExpressionType.NAME;
import static parsing.program.ExpressionType.OPEN_BRACKET;

import datatypes.Value;
import expressions.normal.Expression;
import expressions.normal.Name;
import expressions.normal.Variable;
import expressions.special.MergedExpression;
import expressions.special.ValueHolder;
import interpreter.VarManager;

public class Assignment extends PossibleMainExpression implements ValueHolder, MergedExpression {

	private Name target;
	private ValueHolder value;

	public Assignment(int line) {
		super(line);
		setExpectedExpressions(NAME, OPEN_BRACKET, LITERAL, ARRAY_START);
	}

	/** Modifies the Value in the {@link VarManager} and returns the result. */
	@Override
	public Value getValue() {
		Variable v = VarManager.get(target.getName(), getOriginalLine());
		Value newVal = value.getValue();
		v.setValue(newVal);
		return newVal;
	}

	@Override
	public void merge(Expression... e) {
		if (e.length != 2)
			throw new AssertionError("");
		target = (Name) e[0];
		value = (ValueHolder) e[1];
	}

	@Override
	public boolean execute(ValueHolder... params) {
		getValue();
		return callNextLine();
	}
}
