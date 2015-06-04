
package org.generationcp.breeding.manager.crossingmanager.actions;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Utility class for generating next cross name / number given CrossNameSetting object
 *
 * @author Darla Ani
 *
 */
@Configurable
public class GenerateCrossNameAction {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private CrossNameSetting setting;

	private Integer nextNumberInSequence = 1;

	/**
	 * Returns the generated next name in sequence with the given CrossNameSetting parameters
	 *
	 * @param setting
	 * @return
	 * @throws MiddlewareQueryException
	 */
	public String getNextNameInSequence(CrossNameSetting setting) throws MiddlewareQueryException {
		this.setting = setting;
		this.nextNumberInSequence = this.getNextNumberInSequence(setting);

		return this.buildNextNameInSequence(this.nextNumberInSequence);
	}

	/**
	 * Returns the generated next number in sequence with the given CrossNameSetting parameters
	 *
	 * @param setting
	 * @return
	 * @throws MiddlewareQueryException
	 */
	public Integer getNextNumberInSequence(CrossNameSetting setting) throws MiddlewareQueryException {
		this.setting = setting;
		String lastPrefixUsed = this.buildPrefixString();
		this.nextNumberInSequence = 1;

		Integer startNumber = setting.getStartNumber();
		if (startNumber != null && startNumber > 0) {
			this.nextNumberInSequence = startNumber;
		} else {
			String nextSequenceNumberString =
					this.germplasmDataManager.getNextSequenceNumberForCrossName(lastPrefixUsed.toUpperCase().trim());
			this.nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
		}

		return this.nextNumberInSequence;

	}

	private String buildPrefixString() {
		String prefix = this.setting.getPrefix().trim();
		if (this.setting.isAddSpaceBetweenPrefixAndCode()) {
			return prefix + " ";
		}
		return prefix;
	}

	private String buildSuffixString() {
		String suffix = this.setting.getSuffix().trim();
		if (this.setting.isAddSpaceBetweenSuffixAndCode()) {
			return " " + suffix;
		}
		return suffix;
	}

	public String buildNextNameInSequence(Integer number) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.buildPrefixString());
		sb.append(this.getNumberWithLeadingZeroesAsString(number));
		if (!StringUtils.isEmpty(this.setting.getSuffix())) {
			sb.append(this.buildSuffixString());
		}
		return sb.toString();
	}

	private String getNumberWithLeadingZeroesAsString(Integer number) {
		StringBuilder sb = new StringBuilder();
		String numberString = number.toString();
		Integer numOfDigits = this.setting.getNumOfDigits();

		if (numOfDigits != null && numOfDigits > 0) {
			int numOfZerosNeeded = numOfDigits - numberString.length();
			if (numOfZerosNeeded > 0) {
				for (int i = 0; i < numOfZerosNeeded; i++) {
					sb.append("0");
				}
			}

		}
		sb.append(number);
		return sb.toString();
	}

}
