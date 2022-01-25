package parsing.program;

import java.math.BigDecimal;

import datatypes.BoolValue;
import datatypes.NumberValue;
import datatypes.TextValue;
import datatypes.Value;
import exceptions.runtime.UnexpectedTypeError;

public final class ValueBuilder {

	/**
	 * Replace escaped characters with the real ascii values.
	 */
	private static TextValue excapeText(String arg) {
		for (int i = 0; i < arg.length() - 1; i++) {
			if (arg.charAt(i) == '\\') {
				char c = switch (arg.charAt(i + 1)) {
				case 't' -> '\t';
				case 'r' -> '\r';
				case 'n' -> '\n';
				case 'f' -> '\f';
				case '\\' -> '\\';
				case '"' -> '"';
				default -> throw new IllegalArgumentException("Unexpected value: " + arg.charAt(i + 1));
				};
				arg = arg.substring(0, i) + c + arg.substring(i + 2);
			}
		}
		return new TextValue(arg);
	}
	
	public static Value stringToLiteral(String arg) {
		// Is Single Value
		if (Value.isBoolean(arg))
			return new BoolValue("true".equals(arg));
		if (Value.isNumber(arg))
			return new NumberValue(new BigDecimal(arg));
		if (Value.isString(arg))
			return excapeText(arg.substring(1, arg.length() - 1));
		throw new UnexpectedTypeError("Type must be known by now!");
	}
}