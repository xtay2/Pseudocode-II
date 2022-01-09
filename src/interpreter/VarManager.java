package interpreter;

import static helper.Output.print;

import java.util.ArrayList;
import java.util.HashMap;

import datatypes.NumberValue;
import exceptions.runtime.DeclarationException;
import exceptions.runtime.IllegalCallException;
import expressions.normal.Name;
import expressions.normal.Variable;
import expressions.special.Scope;
import expressions.special.Type;
import parsing.finder.KeywordFinder;

public final class VarManager {

	private static Stack stack = new Stack();
	private static final byte LOOP_VAR_COUNT = 8;
	private static final char FIRST_COUNTER_NAME = 'i';
	private static char counterName = FIRST_COUNTER_NAME;

	static {
		print("Initialising " + Scope.GLOBAL_SCOPE.getScopeName() + "-scope.");
		stack.appendScope(Scope.GLOBAL_SCOPE.getScopeName());
	}

	public static void registerScope(Scope scope) {
		stack.appendScope(scope.getScopeName());
		print("-- Registered " + scope.getScopeName() + " --");
	}

	public static void registerVar(Variable var) {
		stack.registerVar(var);
	}

	public static Variable get(String name) {
		return stack.findVar(name);
	}

	public static void deleteScope(Scope scope) {
		ScopeMemory deleted = stack.popScope(scope.getScopeName());
		if (deleted.getScope().containsKey(String.valueOf((char) (counterName - 1))))
			counterName--;
		print("-- Deleted " + scope.getScopeName() + " --");
	}

	public static int countOfScopes() {
		return stack.height();
	}

	public static void initCounter(Scope scope, long value) {
		Variable cnt = new Variable(scope.getStart(), Type.NUMBER);
		cnt.initialise(new Name(String.valueOf(counterName), scope.getStart()), new NumberValue(value));
		if (counterName > 'p') {
			System.err.println("DISCOURAGED BEHAVIOUR! USING MORE THAN 8 NESTED LOOPS.");
			return;
		}
		counterName++;
	}

	public static void nameCheck(String name) {
		for (byte b = 0; b < LOOP_VAR_COUNT; b++)
			if (String.valueOf((char) (FIRST_COUNTER_NAME + b)).equals(name))
				throw new DeclarationException("Variable cannot be manually declared with a counter-name. (" + FIRST_COUNTER_NAME + "-"
						+ (char) (FIRST_COUNTER_NAME + LOOP_VAR_COUNT) + ") was " + name);
		if (KeywordFinder.isKeyword(name) || Type.isType(name))
			throw new IllegalArgumentException("A Variable cannot be named after a keyword or a type.");
	}

}

final class Stack {

	private final ArrayList<ScopeMemory> scopes = new ArrayList<>();

	public void appendScope(String scopeName) {
		scopes.add(new ScopeMemory(scopeName));
	}

	public String peekScopeName() {
		return scopes.get(height() - 1).getName();
	}

	private HashMap<String, Variable> peekScope() {
		return scopes.get(height() - 1).getScope();
	}

	public ScopeMemory popScope(String name) {
		if (!name.equals(scopes.get(height() - 1).getName()))
			throw new IllegalArgumentException(
					"Trying to delete non-top-scope " + name + "\ntop-scope was " + scopes.get(height() - 1).getName() + "\n" + this);
		return scopes.remove(height() - 1);
	}

	public void registerVar(Variable var) {
		if (peekScope().containsValue(var))
			throw new IllegalArgumentException("Var \"" + var.getName() + "\" couldn't be registered in scope \""
					+ scopes.get(height() - 1).getScope() + "\", as it exists here already.");
		peekScope().put(var.getName(), var);
	}

	public Variable findVar(String varName) {
		for (int i = scopes.size() - 1; i >= 0; i--) {
			HashMap<String, Variable> scope = scopes.get(i).getScope();
			Variable var = scope.get(varName);
			if (var != null)
				return var;
		}
		throw new IllegalCallException("Var " + varName + " doesn't exist. \nScopes: " + scopes);
	}

	public int height() {
		return scopes.size();
	}

	@Override
	public String toString() {
		return scopes.toString();
	}
}

class ScopeMemory {

	private final HashMap<String, Variable> scope = new HashMap<>();

	private final String scopeName;

	public ScopeMemory(String scopeName) {
		this.scopeName = scopeName;
	}

	public String getName() {
		return scopeName;
	}

	public HashMap<String, Variable> getScope() {
		return scope;
	}

	@Override
	public String toString() {
		return "[Scope " + scopeName + ": " + scope + "]";
	}

}
