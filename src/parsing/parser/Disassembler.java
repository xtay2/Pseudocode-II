package parsing.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.parsing.IllegalCodeFormatException;
import helper.Helper;

import static helper.Output.*;

public class Disassembler {

	static List<String> program = null;

	static final List<Declaration> declarations = new ArrayList<>();

	public static List<String> disassemble(List<String> file) {
		program = file;
		splitOneLiners();
		// Remove whitespaces
		for (int i = 0; i < program.size(); i++)
			program.set(i, program.get(i).strip());
		// Find called used declarations and list them.
		analyse();
		program.forEach(e -> print(e));
		// Whipe out unused lines
		collapse();
		// Remove all remaining empty or fully commented lines.
		clearUnwantedLines();
		print(LINE_BREAK + "Compressed program: " + LINE_BREAK);
		program.forEach(e -> print(e));
		return program;
	}

	private static void splitOneLiners() {
		for (int i = 0; i < program.size(); i++) {
			String line = program.get(i).strip();
			int lineBreak = line.indexOf(":");
			if (lineBreak != -1 && Helper.isNotInString(lineBreak, line)) {
				if (lineBreak == line.stripTrailing().length() - 1)
					throw new IllegalCodeFormatException("This one-line statement has to end with a semicolon.");
				// Ersetze Semikolon
				if (line.charAt(line.length() - 1) == ';')
					program.add(i + 1, "}");
				program.add(i + 1, line.substring(lineBreak + 1)); // Teil nach :
				line = line.substring(0, lineBreak) + " {";
				program.set(i, line);
			}
		}
	}

	private static void clearUnwantedLines() {
		for (int i = program.size() - 1; i >= 0; i--) {
			String line = program.get(i);
			if (line.isBlank() || line.startsWith("#"))
				program.remove(i);
		}
	}

	private static void analyse() {
		Declaration main = findMain();
		for (int i = 0; i < program.size(); i++) {
			String line = program.get(i);
			if (Helper.isNotInString(line.indexOf("func"), line)) {
				int end = line.endsWith("{") ? findEndOfScope(i) : i;
				String name = line.substring(line.indexOf("func") + "func".length() + 1, line.indexOf('('));
				declarations.add(new Declaration(name, i, end, countParamsInDeclaration(line), findCallsBetween(i, end)));
			}
		}
		recursive(main);
		print(LINE_BREAK);
		print("All detected Functions: ");
		print(declarations.toString());
		print("Called Functions: ");
		List<Declaration> filtered = declarations.stream().filter(e -> e.getsCalled).toList();
		print(filtered.toString());
	}

	private static void collapse() {
		for (Declaration d : declarations) {
			if (!d.getsCalled) {
				for (int i = d.start; i <= d.end; i++) {
					program.set(i, "");
				}
			}
		}
	}

	private static void recursive(Declaration current) {
		current.getsCalled = true;
		for (Call call : current.calls) {
			try {
				Declaration d = declarations.stream().filter(e -> (e.name + e.arguments)//
						.equals(call.name + call.arguments))//
						.findFirst()//
						.get();
				if (!d.getsCalled) {
					print(current + " \tis calling " + d);
					recursive(d);
				}
			} catch (NoSuchElementException e) {
				throw new AssertionError("Trying to call a function \"" + call.name + "\" that doesn't get defined or imported.");
			}
		}
	}

	private static Declaration findMain() {
		for (int i = 0; i < program.size(); i++) {
			if (program.get(i).stripLeading().startsWith("main")) {
				int end = findEndOfScope(i);
				return new Declaration("main", i, end, 0, findCallsBetween(i, end));
			}
		}
		throw new AssertionError("Program has to contain a main.");
	}

	private static int findEndOfScope(int line) {
		int brack = 0;
		for (int i = line; i < program.size(); i++) {
			if (program.get(i).endsWith("{"))
				brack++;
			if (program.get(i).startsWith("}"))
				brack--;
			if (brack == 0)
				return i;
		}
		throw new AssertionError("Has to be called on a valid scope. Was " + program.get(line));
	}

	private static List<Call> findCallsBetween(int start, int end) {
		List<Call> calls = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			String line = program.get(i).stripLeading();
			int funcKeyword = line.indexOf("func");
			if (line.matches(".*\\w+\\(.*\\);?.*") && (funcKeyword == -1 || !Helper.isNotInString(funcKeyword, line)))
				calls.addAll(findCalls(line));
		}
		return calls;
	}

	private static List<Call> findCalls(String line) {
		Matcher m = Pattern.compile("\\w+\\(").matcher(line);
		List<String> textCalls = new ArrayList<>();
		List<Call> calls = new ArrayList<>();
		m.results().forEach((e) -> textCalls.add(e.group()));
		textCalls.forEach((e) -> {
			int brack = 1, args = 0, arr = 0;
			for (int i = line.indexOf(e) + e.length(); i < line.length(); i++) {
				if (Helper.isNotInString(i, line)) {
					if (line.charAt(i) == '[')
						arr++;
					if (line.charAt(i) == ']')
						arr--;
					if (brack == 1 && arr == 0 && line.charAt(i) == ',')
						args++;
					if (line.charAt(i) == '(')
						brack++;
					if (line.charAt(i) == ')')
						brack--;
					if (brack == 0) {
						if (line.charAt(line.indexOf(e) + e.length()) != ')')
							args++;
						break;
					}
				}
			}
			calls.add(new Call(e.substring(0, e.length() - 1), args));
		});
		return calls;
	}

	private static int countParamsInDeclaration(String call) {
		int params = 0;
		for (int i = 0; i < call.length(); i++) {
			if (call.charAt(i) == ',' && Helper.isNotInString(i, call))
				params++;
		}
		return params == 0 ? (call.charAt(call.indexOf('(') + 1) == ')' ? 0 : 1) : params + 1;
	}
}
