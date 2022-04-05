package formatter.basic;

import static misc.helper.ProgramHelper.containsRunnable;
import static misc.supporting.Output.print;

import java.util.List;
import java.util.stream.Collectors;

import building.types.specific.BuilderType;
import launching.Main;

public sealed abstract class Formatter permits FormattingPreChecks,FormatterLvl1,FormatterLvl2,FormatterLvl3,FormatterLvl4,FormatterLvl5 {

	static List<String> program;

	/**
	 * Formats a program dependent on the strength-level.
	 * 
	 * @param rawProgram is the unformatted program.
	 * @param level      is the strength of the formatter.
	 * @param isMain     tells, if the formatted file is the Main.pc-file
	 * @return the formatted program.
	 */
	public static final List<String> format(List<String> rawProgram, boolean isMain) {
		int level = Main.getFormattingLvl();
		if (level < 1 && level > 5)
			throw new IllegalArgumentException("The level of the formatter has to be between 1 and 5. Was: " + level);
		program = rawProgram.stream().map(l -> l.strip()).collect(Collectors.toList()); // Stripping has to occur first
		print("Executing formatting-pre-checks.");
		FormattingPreChecks.check();
		print("Formatting the program on level " + level + ".");
		/////////////////////////////////////////
		FormatterLvl1.preFormatting();
		// Padding
		if (level >= 2)
			FormatterLvl2.format();
		// Necessary
		FormatterLvl1.format(isMain);
		/////////////////////////////////////////
		// Shortening
		if (level >= 5)
			FormatterLvl5.format();
		// Styling
		if (level >= 4)
			FormatterLvl4.format();
		/////////////////////////////////////////
		// Redundancy
		if (level >= 3)
			FormatterLvl3.format();
		/////////////////////////////////////////
		indent(); // Indentation comes last
		return program;
	}

	/** Add correct tabwise indentation. */
	static void indent() {
		int brack = 0;
		for (int i = 0; i < program.size(); i++) {
			String s = program.get(i);
			if (containsRunnable(s, CBR))
				brack--;
			System.out.println(brack + " " + program.get(i));
			program.set(i, "\t".repeat(brack) + s.stripIndent());
			if (containsRunnable(s, OBR))
				brack++;
		}
	}

	// STATIC INHERITED

	//@formatter:off
	public static final char
	
	/** The symbol of a one-line-start : */
	OLS = ':';
	
    public static final String

  	/** The symbol of a OpenBlock. { Has to be ecaped in a regex.*/
  	OB = BuilderType.OPEN_BLOCK.toString(),
  	
  	/** The symbol of a CloseBlock. } Has to be ecaped in a regex.*/
  	CB = BuilderType.CLOSE_BLOCK.toString(),
  		
	/** Open Block-Regex: "{" */
  	OBR = "\\" + OB,
  	
	/** Close Block-Regex: "}" */
  	CBR = "\\" + CB,
  	
  	/** 
  	 * The Regex for an open scope, either " {" or ": "
  	 * 
  	 * THIS IS ONLY A LOOK-AHEAD-ATTACHMENT AND SHOULDN'T BE USED ALONE!
  	 */
  	OSR = "((?=" + OLS + "\\s)|(?=\\s" + OBR +"))",
  	
  	/** The symbol of a multi-close-scope ; */
  	MCS = BuilderType.MULTI_CLOSE_SCOPE.toString(),
  			
  	/** The symbol of a single-line-comment # */
  	SLC = BuilderType.SINGLE_LINE_COMMENT.toString();
    
    //@formatter:on

	/**
	 * Every formatting-function that only edits one line is a {@link LineFormatterFunc}. They all get
	 * called in {@link Formatter#forEachLine(List)}.
	 */
	@FunctionalInterface
	interface LineFormatterFunc {
		/**
		 * A method that only edits one line.
		 * 
		 * @param line            the unedited line.
		 * @param isFullyRunnable true if the line contains comments or string-literals.
		 * @return the edited line.
		 */
		String formatLine(String line, boolean isFullyRunnable);
	}

	/**
	 * Executes the formatting that can be done linewise, i.e is not dependent on other lines.
	 */
	static void forEachLine(List<LineFormatterFunc> functions) {
		for (int i = 0; i < program.size(); i++) {
			String line = program.get(i);
			if (line.isBlank() || line.startsWith(SLC))
				continue;
			// If the line contains no comments/strings, repeated checking can be avoided.
			final boolean isFullyRunnable = isFullyRunnable(line);
			for (LineFormatterFunc func : functions)
				line = func.formatLine(line, isFullyRunnable);
			program.set(i, line);
		}
	}

	/** Returns true if the line doesn't contain comments or strings. */
	public static boolean isFullyRunnable(String line) {
		return !line.contains(SLC) && !line.contains("\"");
	}

	/**
	 * Comment out all uncommented lines between two indices in the {@link Formatter#program}.
	 * 
	 * @param start is the start-index (inclusive)
	 * @param end   is the end index (inclusive)
	 */
	static void commentRange(int start, int end) {
		for (int i = start; i <= end; i++)
			comment(i);
	}

	/**
	 * Comments out the line at the specified index in {@link Formatter#program}, if its not already
	 * done.
	 */
	static void comment(int line) {
		program.set(line, comment(program.get(line)));
	}

	/** Returns the same {@link String} with a prepended SLC if it doesn't already start with one. */
	static String comment(String line) {
		if (!line.startsWith(SLC))
			return SLC + " " + line.stripLeading();
		return line;
	}
}
