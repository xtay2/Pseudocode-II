package expressions.main.functions;

import static helper.Output.print;

import java.util.Arrays;

import datatypes.Value;
import exceptions.parsing.IllegalCodeFormatException;
import exceptions.runtime.CastingException;
import exceptions.runtime.DeclarationException;
import exceptions.runtime.IllegalReturnException;
import expressions.normal.ExpectedReturnType;
import expressions.normal.ExpectedType;
import expressions.normal.Keyword;
import expressions.normal.Name;
import expressions.normal.Variable;
import expressions.normal.brackets.OpenBlock;
import expressions.special.Expression;
import expressions.special.Scope;
import expressions.special.Type;
import expressions.special.ValueHolder;
import extensions.datastructures.Dictionary;
import extensions.datastructures.DictionaryEntry;
import helper.Output;
import interpreter.Interpreter;
import interpreter.VarManager;
import interpreter.system.SystemFunctions;
import parsing.finder.KeywordFinder;
import parsing.program.ExpressionType;
import parsing.program.KeywordType;

public class Function extends Scope implements ValueHolder {

	private final Dictionary<Name, ExpectedType> paramBlueprint = new Dictionary<>();
	protected Name name = null;
	private Value returnVal = null;
	private Type returnType = null;

	// Keyword flags
	boolean isNative = false;

	public Function(int line) {
		super(line);
		setExpectedExpressions(ExpressionType.NAME);
	}

	@Override
	public void build(Expression... args) {
		int funcKeywordPos = 0;
		// Finde alle Keyword flags heraus.
		while (args[funcKeywordPos]instanceof Keyword k) {
			if (k.getKeyword() == KeywordType.NATIVE)
				isNative = true;
			funcKeywordPos++;
		}

		// Finde den Namen der Funktion heraus.
		if (args[funcKeywordPos + 1]instanceof Name n) {
			nameCheck(n.getName());
			name = n;
		} else
			throw new DeclarationException("Every function must have a name!" + Arrays.toString(args));

		// Finde die Namen und Typen der Parameter heraus.
		for (int i = funcKeywordPos + 2; i < args.length; i++) {
			if (args[i] instanceof ExpectedType) {
				if (args[i + 1] instanceof Name) {
					paramBlueprint.add((Name) args[i + 1], ((ExpectedType) args[i]));
					i += 2;
					continue;
				}
			} else if (args[i] instanceof Name) {
				paramBlueprint.add((Name) args[i], null);
				i++;
				continue;
			}
			// Finde heraus ob es einen R�ckgabetypen gibt, und wenn ja, welchen.
			if (args[i] instanceof ExpectedReturnType) {
				if (!(args[i + 1] instanceof ExpectedType))
					throw new IllegalCodeFormatException("No type declaration after \"->\" in " + name);
				returnType = ((ExpectedType) args[i + 1]).type;
				break;
			}
		}
		Expression last = args[args.length - 1];

		if (!isNative) {
			if (last instanceof OpenBlock ob)
				block = ob;
			else
				throw new IllegalCodeFormatException(
						name + ": A function-declaration must end with a valid block. Expected ':' or '{', was: '" + last + "'");
		}
	}

	/**
	 * A function cannot be named after a keyword, a type, or the global-scope.
	 */
	private void nameCheck(String s) {
		if (KeywordFinder.isKeyword(s) || Type.isType(s))
			throw new DeclarationException("A function cannot be named after a keyword or a type.");
	}

	/**
	 * This method gets called by the ReturnStatement. If a returntype is specified,
	 * the value gets implicitly casted.
	 */
	public void setReturnVal(Value val) {
		if (returnVal != null && val != null)
			throw new AssertionError("Function " + name + " already has a return value.");
		if (returnType != null && val != null && val.getType() != returnType)
			returnVal = val.as(returnType);
		else
			returnVal = val;
	}

	@Override
	public Value getValue() {
		return returnVal;
	}

	/** Returns the amount of expected parameters. */
	public int expectedParams() {
		return paramBlueprint.size();
	}
	
	public boolean isNative() {
		return isNative;
	}

	public String getName() {
		return name.getName();
	}

	/** Register all temporary function-vars */
	private void registerParameters(ValueHolder... params) {
		VarManager.registerScope(this);
		try {
			int paramCount = paramBlueprint.size();
			if (paramCount != params.length)
				throw new DeclarationException(name + " takes " + paramCount + " parameters. Please call it accordingly.");
			for (int i = 0; i < paramCount; i++) {
				DictionaryEntry<Name, ExpectedType> param = paramBlueprint.get(i);
				Value v = params[i].getValue();
				Variable p = new Variable(line, param.getValue() == null ? v.getType() : param.getValue().type);
				p.initialise(param.getKey(), v);
			}
		} catch (CastingException e) {
			throw new DeclarationException("Passed a value with an unwanted type to " + name + ".");
		}
	}
	
	@Override
	public boolean execute(boolean doExecuteNext, ValueHolder... params) {
		if (isNative) {
			returnVal = SystemFunctions.callSystemFunc(getName(), params);
		} else {
			print("Executing " + name + (params.length == 0 ? "" : " with " + Arrays.toString(params)));
			registerParameters(params);
			if (doExecuteNext)
				Interpreter.execute(line + 1, true);
			if (returnType != null && returnVal == null)
				throw new IllegalReturnException(
						"func " + name + " was defined to return a value of type: " + returnType.getName() + ", but returned nothing.");
			VarManager.deleteScope(this);
		}
		return true;

	}

	@Override
	public int getEnd() {
		return isNative ? getStart() : super.getEnd();
	}

	@Override
	public String getScopeName() {
		return "func" + name.getName() + getStart() + "-" + getEnd();
	}

	@Override
	public String toString() {
		return Output.DEBUG ? this.getClass().getSimpleName() : (name == null ? "func" : name.toString());
	}


}
