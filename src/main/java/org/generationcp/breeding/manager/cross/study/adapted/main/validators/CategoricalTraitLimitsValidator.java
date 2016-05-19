
package org.generationcp.breeding.manager.cross.study.adapted.main.validators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.breeding.manager.cross.study.constants.CategoricalVariatesCondition;
import org.generationcp.middleware.domain.h2h.CategoricalValue;

import com.vaadin.data.Validator;
import com.vaadin.ui.ComboBox;

/**
 * The value must be present to the existing categorical values of the fields The value must not be blank
 * */

public class CategoricalTraitLimitsValidator implements Validator {

	private static final long serialVersionUID = 1L;

	public static final String LIST_REGEX = "^(\\w+(\\.\\w+)*)(\\s*,\\s*\\w+(\\.\\w+)*)*";
	public static final String DOUBLE_REGEX = "^(\\w+(\\.\\w+)*)$";
	public static final String LIST_DELIMITER = ",";

	private static final String DEFAULT_ERROR = "Limit Value is not a valid categorical value for the current trait.";
	private static final String INVALID_FORMAT = "Invalid Format for chosen condition.";

	private final ComboBox conditionBox;
	private final List<CategoricalValue> distinctValues;
	private String errorDetails;
	private String delimiter;

	public CategoricalTraitLimitsValidator(ComboBox conditionBox, List<CategoricalValue> distinctValues) {
		super();
		this.conditionBox = conditionBox;
		this.distinctValues = distinctValues;
	}

	@Override
	public void validate(Object value) throws InvalidValueException {

		if (!this.isValid(value)) {
			throw new InvalidValueException(this.errorDetails);
		}

	}

	@Override
	public boolean isValid(Object value) {
		String stringValue = ((String) value).trim();

		if (this.isValidFormat(stringValue)) {
			List<String> values = this.parseValues(stringValue);
			boolean allValid = true;

			if (values != null && !values.isEmpty()) {
				this.errorDetails = CategoricalTraitLimitsValidator.DEFAULT_ERROR;

				for (String val : values) {
					if (!this.isAPossibleValue(val)) {
						allValid = false;
						break;
					}
				}

				return allValid;
			}
		}

		return false;
	}

	public boolean isAPossibleValue(String val) {

		Iterator<CategoricalValue> valueIterator = this.distinctValues.iterator();

		while (valueIterator.hasNext()) {
			CategoricalValue categoricalValue = valueIterator.next();

			if (categoricalValue.getName().equals(val)) {
				return true;
			}
		}

		return false;
	}

	public boolean isValidFormat(String value) {
		CategoricalVariatesCondition criteria = (CategoricalVariatesCondition) this.conditionBox.getValue();
		this.errorDetails = CategoricalTraitLimitsValidator.INVALID_FORMAT;

		if (CategoricalVariatesCondition.IN.equals(criteria) || CategoricalVariatesCondition.NOT_IN.equals(criteria)) {
			this.delimiter = CategoricalTraitLimitsValidator.LIST_DELIMITER;
			return value.matches(CategoricalTraitLimitsValidator.LIST_REGEX);
		} else {
			this.delimiter = "";
			return value.matches(CategoricalTraitLimitsValidator.DOUBLE_REGEX);
		}

	}

	private List<String> parseValues(String valueString) {
		List<String> values = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(valueString, this.delimiter);

		while (st.hasMoreTokens()) {
			values.add(st.nextToken().toString());
		}
		return values;
	}

}
