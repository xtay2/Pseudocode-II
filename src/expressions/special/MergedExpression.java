package expressions.special;

import java.util.Arrays;
import java.util.List;

import expressions.normal.Expression;
import parsing.program.ValueMerger;

/**
 * Interface for all Expressions that aren't directly build from the text, but
 * rather constructed from multiple pure Expressions by the {@link ValueMerger}.
 */
public interface MergedExpression {

	// ONLY IMPLEMENT ONE OF THE FOLLOWING TWO!!!

	/**
	 * Construct a MergedExpression from multiple Expressions.
	 * 
	 * @param e are optional Expressions.
	 */
	default void merge(Expression... e) {
		merge(Arrays.asList(e));
	}

	/**
	 * Construct a MergedExpression from multiple Expressions.
	 * 
	 * @param e are optional Expressions.
	 */
	default void merge(List<Expression> e) {
		merge(e.toArray(new Expression[e.size()]));
	}

}
