
package org.generationcp.breeding.manager.cross.study.adapted.main.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.breeding.manager.cross.study.constants.NumericTraitCriteria;

import com.vaadin.data.Validator;
import com.vaadin.ui.ComboBox;

public class NumericTraitLimitsValidator implements Validator {

	public static final String LIST_REGEX = "^(\\d+(\\.\\d+)*)(\\s*,\\s*\\d+(\\.\\d+)*)*";
	public static final String RANGE_REGEX = "^(\\d+(\\.\\d+)*)\\s*-{1}\\s*(\\d+(\\.\\d+)*)$";
	public static final String DOUBLE_REGEX = "^(\\d+(\\.\\d+)*)$";
	public static final String LIST_DELIMITER = ",";
	public static final String RANGE_DELIMITER = "-";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ERROR = "Value exceeded min / max limits.";
	private static final String INVALID_FORMAT = "Invalid value / format for chosen condition.";

	private final ComboBox conditionBox;
	private final Double minValue;
	private final Double maxValue;
	private String errorDetails;
	private String delimiter;

	public NumericTraitLimitsValidator(ComboBox conditionBox, double minValue, double maxValue) {
		super();
		this.conditionBox = conditionBox;
		this.minValue = minValue;
		this.maxValue = maxValue;
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
			List<Double> values = this.parseValues(stringValue);
			boolean allValid = true;

			if (values != null && !values.isEmpty()) {
				this.errorDetails = NumericTraitLimitsValidator.DEFAULT_ERROR;

				for (Double aDouble : values) {
					if (this.exceedLowerLimits(aDouble) || this.exceedUpperLimits(aDouble)) {
						allValid = false;
						break;
					}
				}

				return allValid;
			}
		}

		return false;
	}

	public boolean isValidFormat(String value) {
		NumericTraitCriteria criteria = (NumericTraitCriteria) this.conditionBox.getValue();
		this.errorDetails = NumericTraitLimitsValidator.INVALID_FORMAT;

		if (NumericTraitCriteria.BETWEEN.equals(criteria)) {
			this.delimiter = NumericTraitLimitsValidator.RANGE_DELIMITER;
			return value.matches(NumericTraitLimitsValidator.RANGE_REGEX);

		} else if (NumericTraitCriteria.IN.equals(criteria) || NumericTraitCriteria.NOT_IN.equals(criteria)) {
			this.delimiter = NumericTraitLimitsValidator.LIST_DELIMITER;
			return value.matches(NumericTraitLimitsValidator.LIST_REGEX);

		} else {
			this.delimiter = "";
			return value.matches(NumericTraitLimitsValidator.DOUBLE_REGEX);
		}

	}

	private boolean exceedUpperLimits(Double value) {
		return value.compareTo(this.maxValue) > 0;
	}

	private boolean exceedLowerLimits(Double value) {
		return value.compareTo(this.minValue) < 0;
	}

	private List<Double> parseValues(String valueString) {
		List<Double> values = new ArrayList<Double>();
		StringTokenizer st = new StringTokenizer(valueString, this.delimiter);
		try {

			while (st.hasMoreTokens()) {
				values.add(Double.parseDouble(st.nextToken()));
			}
			return values;

		} catch (NumberFormatException e) {
			this.errorDetails = "Value must be a number.";
		}

		return null;
	}

}
