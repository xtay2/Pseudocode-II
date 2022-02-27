package expressions.main.functions;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import datatypes.Value;
import exceptions.parsing.IllegalCodeFormatException;
import exceptions.runtime.IllegalCallException;
import exceptions.runtime.IllegalReturnException;
import expressions.abstractions.Expression;
import expressions.abstractions.interfaces.ValueHolder;
import expressions.normal.brackets.OpenScope;
import expressions.normal.containers.Name;
import expressions.normal.containers.Variable;
import expressions.possible.Call;
import modules.interpreter.Interpreter;
import types.specific.data.DataType;
import types.specific.data.ExpectedType;

/**
 * This is the class for a Function-Declaration.
 * 
 * If a {@link Function} gets called, this happens through the {@link Call}-Class and
 * {@link Interpreter#call}.
 */
public class Function extends Returnable {

	/** All expected parameters. */
	private final LinkedHashMap<Name, ExpectedType> paramBlueprint = new LinkedHashMap<>();

	/** Extends the constructor from {@link Returnable}. */
	public Function(int lineID) {
		super(lineID);
	}

	/** [NAME] (?[?TYPE] [NAME]) ([EXPECTED_RETURN_TYPE]) [OPEN_SCOPE] */
	@Override
	public void merge(Expression... e) {
		name = (Name) e[0];
		// Names and Types of parameters.
		for (int i = 1; i < e.length - 2; i++) {
			if (e[i].type instanceof ExpectedType t) {
				if (e[i + 1] instanceof Name) {
					paramBlueprint.put((Name) e[i + 1], t);
					i += 2;
					continue;
				}
				throw new IllegalCodeFormatException(getOriginalLine(),
						"Expected a name after the type " + e[i] + ". Got " + e[i + 1] + " instead.");
			}
			if (e[i] instanceof Name) {
				paramBlueprint.put((Name) e[i], DataType.VAR);
				i++;
				continue;
			}
			throw new AssertionError("Unexpected token: " + e[i]);
		}
		// Expected ReturnValue
		returnType = e[e.length - 2] == null ? null : (ExpectedType) e[e.length - 2].type;
		initScope((OpenScope) e[e.length - 1]);
	}

	@Override
	public boolean execute(ValueHolder... params) {
		if (expectedParams() != params.length)
			throw new IllegalCallException(getOriginalLine(), "This function is called with the wrong amount of params.");
		finalCheck();
		// Init Params
		int i = 0;
		for (Entry<Name, ExpectedType> param : paramBlueprint.entrySet()) {
			Value v = params[i++].getValue();
			Variable.quickCreate(lineIdentifier, getScope(), param.getValue(), param.getKey(), v);
		}
		callFirstLine();
		// The return-value is now set.
		if (returnType != null && returnVal == null) {
			throw new IllegalReturnException(getOriginalLine(),
					getNameString() + " was defined to return a value of type: " + returnType + ", but returned nothing.");
		}
		getScope().clear();
		return true;
	}

	@Override
	public int expectedParams() {
		return paramBlueprint.size();
	}
}
