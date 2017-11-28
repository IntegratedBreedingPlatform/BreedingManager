
package org.generationcp.breeding.manager.crossingmanager.actions;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.ruleengine.ProcessCodeOrderedRule;
import org.generationcp.commons.ruleengine.ProcessCodeRuleFactory;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.cross.CrossingRuleExecutionContext;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.commons.util.ExpressionHelper;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for generating next cross name / number given CrossNameSetting object
 * 
 * @author Darla Ani
 * 
 */
@Configurable
public class GenerateCrossNameAction {

	private static final Logger LOG = LoggerFactory.getLogger(GenerateCrossNameAction.class);

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private ProcessCodeRuleFactory processCodeRuleFactory;

	@Resource
	private PedigreeDataManager pedigreeDataManager;

	private CrossNameSetting setting;

	private Integer nextNumberInSequence = 1;

	/**
	 * Returns the generated next name in sequence with the given CrossNameSetting parameters
	 * 
	 * @param setting
	 * @return @
	 */
	public String getNextNameInSequence(CrossNameSetting setting) {
		this.setting = setting;
		this.nextNumberInSequence = this.getNextNumberInSequence(setting);

		return this.buildNextNameInSequence(this.nextNumberInSequence);
	}

	/**
	 * Returns the generated next number in sequence with the given CrossNameSetting parameters
	 * 
	 * @param setting
	 * @return @
	 */
	public Integer getNextNumberInSequence(CrossNameSetting setting) {
		this.setting = setting;
		String lastPrefixUsed = this.buildPrefixString();
		this.nextNumberInSequence = 1;

		Integer startNumber = setting.getStartNumber();
		if (startNumber != null && startNumber > 0) {
			this.nextNumberInSequence = startNumber;
		} else {
			String nextSequenceNumberString =
					this.germplasmDataManager.getNextSequenceNumberForCrossName(lastPrefixUsed.toUpperCase().trim(), setting.getSuffix());
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

	/**
	 * Returns the generated next name in sequence with the given methodId and germplasm.
	 * The suffix will come from the specified method if it's available in the database setting.
	 *
	 * @param methodId
	 * @param germplasm
	 * @param number
	 * @return
	 */
	public String buildNextNameInSequence(final Integer methodId, final Germplasm germplasm, final Integer number) {

		String suffix = "";

		StringBuilder sb = new StringBuilder();
		sb.append(this.buildPrefixString());
		sb.append(this.getNumberWithLeadingZeroesAsString(number));

		// Get the suffix from method's settings.
		if (methodId != null) {
			final Method method = this.germplasmDataManager.getMethodByID(methodId);
			if (!StringUtils.isEmpty(method.getSuffix())) {
				suffix = method.getSuffix().trim();
			}
		}

		if (!StringUtils.isEmpty(suffix)) {

			final Pattern processCodePattern = Pattern.compile(ExpressionHelper.PROCESS_CODE_PATTERN);
			final Matcher matcherProcessCode = processCodePattern.matcher(suffix);

			String processCode = "";
			String processCodeValue = "";

			// Process the suffix process code if it is available.
			if (matcherProcessCode.find()) {
				processCode = matcherProcessCode.group();
				processCodeValue = this.evaluateSuffixProcessCode(germplasm, processCode);
			}

			sb.append(replaceExpressionWithValue(new StringBuilder(suffix), processCode, processCodeValue));

		}

		return sb.toString();
	}

	protected String evaluateSuffixProcessCode(final Germplasm germplasm, final String processCode) {
		final ProcessCodeOrderedRule rule = this.processCodeRuleFactory.getRuleByProcessCode(processCode);

		final CrossingRuleExecutionContext crossingRuleExecutionContext =
				new CrossingRuleExecutionContext(new ArrayList<String>(), null, germplasm.getGpid2() != null ? Integer.valueOf(germplasm
						.getGpid2()) : 0, germplasm.getGpid1() != null ? Integer.valueOf(germplasm.getGpid1()) : 0,
						this.germplasmDataManager, this.pedigreeDataManager);

		if (rule != null) {
			try {
				return (String) rule.runRule(crossingRuleExecutionContext);
			} catch (final RuleException e) {
				LOG.error(e.getMessage(), e);
				return "";
			}
		}

		return "";

	}

	protected String replaceExpressionWithValue(StringBuilder container, String processCode, String value) {
		int startIndex = container.toString().toUpperCase().indexOf(processCode);
		int endIndex = startIndex + processCode.length();

		String replaceValue = value == null ? "" : value;
		container.replace(startIndex, endIndex, replaceValue);
		return container.toString();
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

	public void setSetting(final CrossNameSetting setting) {
		this.setting = setting;
	}

}
