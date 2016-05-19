
package org.generationcp.breeding.manager.cross.study.commons.trait.filter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.breeding.manager.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.breeding.manager.cross.study.adapted.main.listeners.AdaptedGermplasmValueChangeListener;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CategoricalTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.validators.CategoricalTraitLimitsValidator;
import org.generationcp.breeding.manager.cross.study.constants.CategoricalVariatesCondition;
import org.generationcp.breeding.manager.cross.study.constants.TraitWeight;
import org.generationcp.breeding.manager.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.CategoricalTraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class CategoricalVariatesSection extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 9099796930978032454L;

	private static final Logger LOG = LoggerFactory.getLogger(CategoricalVariatesSection.class);

	private static final String TRAIT_COLUMN_ID = "Trait Column";
	private static final String NUM_LOCATIONS_COLUMN_ID = "Number of Locations";
	private static final String NUM_LINES_COLUMN_ID = "Number of Lines";
	private static final String NUM_OBSERVATIONS_COLUMN_ID = "Number of Observations";
	private static final String CLASS_COLUMN_ID = "Class";
	private static final String CONDITION_COLUMN_ID = "Condition";
	private static final String LIMITS_COLUMN_ID = "Limits";
	private static final String PRIORITY_COLUMN_ID = "Priority";
	public static final String TRAIT_BUTTON_ID = "CharacterTraitsSection Trait Button ID";

	private List<Integer> environmentIds = null;
	private List<Integer> selectedTraits = null;
	private final List<Field> fieldsToValidate = new ArrayList<Field>();

	private final Window parentWindow;
	private Label lblSectionTitle;
	private Table traitsTable;

	private List<CategoricalTraitInfo> categoricalValueObjects;
	private int categoricalTraitCount;
	private boolean emptyMessageShown = false;
	private List<CategoricalTraitFilter> filters;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	public CategoricalVariatesSection(List<Integer> environmentIds, Window parentWindow) {
		super();
		this.environmentIds = environmentIds;
		this.parentWindow = parentWindow;
	}

	public CategoricalVariatesSection(List<Integer> environmentIds, List<Integer> selectedTraits, Window parentWindow) {
		super();
		this.environmentIds = environmentIds;
		this.selectedTraits = selectedTraits;
		this.parentWindow = parentWindow;
	}

	private void initializeComponents() {

		if (this.environmentIds != null && !this.environmentIds.isEmpty()) {
			try {
				this.categoricalValueObjects = this.crossStudyDataManager.getTraitsForCategoricalVariates(this.environmentIds);
				if (this.selectedTraits != null) {
					this.categoricalValueObjects = this.filterUnwantedTraitsFromResults(this.categoricalValueObjects, this.selectedTraits);
				}
			} catch (MiddlewareQueryException ex) {
				CategoricalVariatesSection.LOG.error("Error with getting categorical variate info given environment ids: "
						+ this.environmentIds.toString(), ex);
				MessageNotifier.showError(
						this.parentWindow,
						"Database Error!",
						"Error with getting categorical variate info given environment ids. "
								+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			}
		}

		this.lblSectionTitle = new Label(this.messageSource.getMessage(Message.CATEGORICAL_TRAITS_SECTION_TITLE));

		this.traitsTable = new Table();
		this.traitsTable.setImmediate(true);
		this.traitsTable.setColumnCollapsingAllowed(true);
		this.traitsTable.setColumnReorderingAllowed(true);

		this.traitsTable.addContainerProperty(CategoricalVariatesSection.TRAIT_COLUMN_ID, Button.class, null);
		this.traitsTable.addContainerProperty(CategoricalVariatesSection.NUM_LOCATIONS_COLUMN_ID, Integer.class, null);
		this.traitsTable.addContainerProperty(CategoricalVariatesSection.NUM_LINES_COLUMN_ID, Integer.class, null);
		this.traitsTable.addContainerProperty(CategoricalVariatesSection.NUM_OBSERVATIONS_COLUMN_ID, Integer.class, null);
		if (this.categoricalValueObjects != null) {
			for (int i = 0; i < this.getMaxCategoryValueCount(this.categoricalValueObjects); i++) {
				this.traitsTable.addContainerProperty(CategoricalVariatesSection.CLASS_COLUMN_ID + i, String.class, null);
			}
		}
		this.traitsTable.addContainerProperty(CategoricalVariatesSection.CONDITION_COLUMN_ID, ComboBox.class, null);
		this.traitsTable.addContainerProperty(CategoricalVariatesSection.LIMITS_COLUMN_ID, TextField.class, null);
		this.traitsTable.addContainerProperty(CategoricalVariatesSection.PRIORITY_COLUMN_ID, ComboBox.class, null);

		this.traitsTable.setColumnHeader(CategoricalVariatesSection.TRAIT_COLUMN_ID,
				this.messageSource.getMessage(Message.HEAD_TO_HEAD_TRAIT)); // Trait
		this.traitsTable.setColumnHeader(CategoricalVariatesSection.NUM_LOCATIONS_COLUMN_ID,
				this.messageSource.getMessage(Message.NUMBER_OF_LOCATIONS)); // # of Locations
		this.traitsTable.setColumnHeader(CategoricalVariatesSection.NUM_LINES_COLUMN_ID,
				this.messageSource.getMessage(Message.NUMBER_OF_LINES)); // # of Lines
		this.traitsTable.setColumnHeader(CategoricalVariatesSection.NUM_OBSERVATIONS_COLUMN_ID,
				this.messageSource.getMessage(Message.NUMBER_OF_OBSERVATIONS)); // # of Observations
		if (this.categoricalValueObjects != null) {
			for (int i = 0; i < this.getMaxCategoryValueCount(this.categoricalValueObjects); i++) {
				this.traitsTable.setColumnHeader(CategoricalVariatesSection.CLASS_COLUMN_ID + i,
						this.messageSource.getMessage(Message.CLASS) + " " + (i + 1));
			}
		}
		this.traitsTable.setColumnHeader(CategoricalVariatesSection.CONDITION_COLUMN_ID,
				this.messageSource.getMessage(Message.CONDITION_HEADER)); // Condition
		this.traitsTable.setColumnHeader(CategoricalVariatesSection.LIMITS_COLUMN_ID, this.messageSource.getMessage(Message.LIMITS)); // Limits
		this.traitsTable.setColumnHeader(CategoricalVariatesSection.PRIORITY_COLUMN_ID, this.messageSource.getMessage(Message.PRIORITY)); // Priority

		this.traitsTable.setColumnWidth(CategoricalVariatesSection.TRAIT_COLUMN_ID, 120);
		this.traitsTable.setColumnWidth(CategoricalVariatesSection.NUM_LOCATIONS_COLUMN_ID, 120);
		this.traitsTable.setColumnWidth(CategoricalVariatesSection.NUM_LINES_COLUMN_ID, 120);
		this.traitsTable.setColumnWidth(CategoricalVariatesSection.NUM_OBSERVATIONS_COLUMN_ID, 120);
		if (this.categoricalValueObjects != null) {
			for (int i = 0; i < this.getMaxCategoryValueCount(this.categoricalValueObjects); i++) {
				this.traitsTable.setColumnWidth(CategoricalVariatesSection.CLASS_COLUMN_ID + i, 100);
			}
		}
		this.traitsTable.setColumnWidth(CategoricalVariatesSection.CONDITION_COLUMN_ID, 200);
		this.traitsTable.setColumnWidth(CategoricalVariatesSection.LIMITS_COLUMN_ID, 200);
		this.traitsTable.setColumnWidth(CategoricalVariatesSection.PRIORITY_COLUMN_ID, 200);

		this.setWidth(1200 + this.getMaxCategoryValueCount(this.categoricalValueObjects) * 120 + "px");
	}

	private void initializeLayout() {
		this.setMargin(true);
		this.setSpacing(true);
		this.setWidth("995px");
		this.addComponent(this.lblSectionTitle);
		this.addComponent(this.traitsTable);
		this.traitsTable.setHeight("360px");
		this.traitsTable.setWidth("960px");
	}

	private int getMaxCategoryValueCount(List<CategoricalTraitInfo> categoricalValueObjects) {
		int max = 0;
		if (categoricalValueObjects != null) {
			for (int i = 0; i < categoricalValueObjects.size(); i++) {
				int count = categoricalValueObjects.get(i).getValues().size();
				if (count > max) {
					max = count;
				}
			}
		}
		return max;
	}

	private void populateTraitsTable() {
		String limitsRequiredMessage =
				MessageFormat.format(this.messageSource.getMessage(Message.FIELD_IS_REQUIRED),
						this.messageSource.getMessage(Message.LIMITS));

		if (this.environmentIds != null && !this.environmentIds.isEmpty()) {

			if (this.categoricalValueObjects != null) {
				this.categoricalTraitCount = this.categoricalValueObjects.size();
				if (this.categoricalValueObjects.isEmpty()) {
					return;
				}

				for (CategoricalTraitInfo traitInfo : this.categoricalValueObjects) {

					Button traitNameLink = new Button(traitInfo.getName());
					traitNameLink.setImmediate(true);
					traitNameLink.setStyleName(BaseTheme.BUTTON_LINK);
					traitNameLink.setData(CategoricalVariatesSection.TRAIT_BUTTON_ID);
					traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this, traitInfo.getId(), traitInfo.getName(),
							"Categorical Variate", this.environmentIds));

					ComboBox conditionComboBox = CrossStudyUtil.getCategoricalVariatesComboBox();
					conditionComboBox.setEnabled(true);

					ComboBox priorityComboBox = CrossStudyUtil.getTraitWeightsComboBox();
					TextField txtLimits = new TextField();
					txtLimits.setEnabled(false);
					txtLimits.setImmediate(true);
					txtLimits.setRequired(true);
					txtLimits.setRequiredError(limitsRequiredMessage);
					txtLimits.addValidator(new CategoricalTraitLimitsValidator(conditionComboBox, traitInfo.getValues()));
					this.fieldsToValidate.add(txtLimits);
					txtLimits.setEnabled(false);

					Object[] itemObj = new Object[this.traitsTable.getColumnHeaders().length];

					itemObj[0] = traitNameLink;
					itemObj[1] = traitInfo.getLocationCount();
					itemObj[2] = traitInfo.getGermplasmCount();
					itemObj[3] = traitInfo.getObservationCount();

					int currentColumn = 4;
					for (int currentValueIndex = 0; currentValueIndex < traitInfo.getValues().size(); currentColumn++, currentValueIndex++) {
						itemObj[currentColumn] =
								traitInfo.getValues().get(currentValueIndex).getName() + " ("
										+ traitInfo.getValues().get(currentValueIndex).getCount() + ")";
					}

					// for cases wherein not all column count is equal
					for (; currentColumn < this.traitsTable.getColumnHeaders().length - 3; currentColumn++) {
						itemObj[currentColumn] = "";
					}

					itemObj[this.traitsTable.getColumnHeaders().length - 3] = conditionComboBox;
					itemObj[this.traitsTable.getColumnHeaders().length - 2] = txtLimits;
					itemObj[this.traitsTable.getColumnHeaders().length - 1] = priorityComboBox;

					this.traitsTable.addItem(itemObj, traitInfo);

					conditionComboBox.addListener(new AdaptedGermplasmValueChangeListener(this, txtLimits, priorityComboBox));
				}
			}
		}
	}

	// TODO : Rebecca is not happy with public/private method ordering
	// TODO : warning - On2 may need to revisit for performance
	private List<CategoricalTraitInfo> filterUnwantedTraitsFromResults(List<CategoricalTraitInfo> categoricalTraitInfos,
			List<Integer> desiredTraits) {
		List<CategoricalTraitInfo> filteredTraits = new ArrayList<CategoricalTraitInfo>();
		for (CategoricalTraitInfo cto : categoricalTraitInfos) {
			for (Integer traitId : desiredTraits) {
				if (cto.getId() == traitId.intValue()) {
					filteredTraits.add(cto);
				}
			}
		}
		return filteredTraits;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setSpacing(true);
		this.setMargin(true);

		this.initializeComponents();
		this.initializeLayout();
		this.populateTraitsTable();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.lblSectionTitle, Message.CATEGORICAL_TRAITS_SECTION_TITLE);
	}

	public void showEmptyTraitsMessage() {
		if (!this.emptyMessageShown && this.categoricalTraitCount == 0) {
			MessageNotifier.showMessage(this.parentWindow, "Information",
					"There were no categorical traits observed in the environments you have selected.", 3000);
			this.emptyMessageShown = true;
		}
	}

	public void showTraitObservationClickAction(Integer traitId, String variateType, String traitName, List<Integer> envIds) {
		Window parentWindow = this.getWindow();
		ViewTraitObservationsDialog viewTraitDialog =
				new ViewTraitObservationsDialog(this, parentWindow, variateType, traitId, traitName, envIds);
		viewTraitDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		parentWindow.addWindow(viewTraitDialog);
	}

	// perform validation on limits textfields
	public boolean allFieldsValid() {
		try {
			for (Field field : this.fieldsToValidate) {
				if (field.isEnabled()) {
					field.validate();
				}
			}

			return true;

		} catch (InvalidValueException e) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.INCORRECT_LIMITS_VALUE), e.getMessage());
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public List<CategoricalTraitFilter> getFilters() {
		this.filters = new ArrayList<CategoricalTraitFilter>();

		Collection<CategoricalTraitInfo> traitInfoObjects = (Collection<CategoricalTraitInfo>) this.traitsTable.getItemIds();
		for (CategoricalTraitInfo traitInfo : traitInfoObjects) {
			Item tableRow = this.traitsTable.getItem(traitInfo);

			ComboBox conditionComboBox = (ComboBox) tableRow.getItemProperty(CategoricalVariatesSection.CONDITION_COLUMN_ID).getValue();
			CategoricalVariatesCondition condition = (CategoricalVariatesCondition) conditionComboBox.getValue();

			ComboBox priorityComboBox = (ComboBox) tableRow.getItemProperty(CategoricalVariatesSection.PRIORITY_COLUMN_ID).getValue();
			TraitWeight priority = (TraitWeight) priorityComboBox.getValue();

			TextField limitsField = (TextField) tableRow.getItemProperty(CategoricalVariatesSection.LIMITS_COLUMN_ID).getValue();
			String limitsString = limitsField.getValue().toString();

			if (condition != CategoricalVariatesCondition.DROP_TRAIT && priority != TraitWeight.IGNORED) {
				if (condition == CategoricalVariatesCondition.KEEP_ALL) {
					CategoricalTraitFilter filter = new CategoricalTraitFilter(traitInfo, condition, new ArrayList<String>(), priority);
					this.filters.add(filter);
				} else {
					if (limitsString != null && limitsString.length() > 0) {
						StringTokenizer tokenizer = new StringTokenizer(limitsString, ",");
						List<String> givenLimits = new ArrayList<String>();

						while (tokenizer.hasMoreTokens()) {
							String limit = tokenizer.nextToken().trim();
							givenLimits.add(limit);
						}

						CategoricalTraitFilter filter = new CategoricalTraitFilter(traitInfo, condition, givenLimits, priority);
						this.filters.add(filter);
					}
				}
			}
		}

		return this.filters;
	}

	/*
	 * If at least one trait is NOT dropped, allow to proceed
	 */
	public boolean allTraitsDropped() {
		if (this.filters == null) {
			this.filters = this.getFilters();
		}
		if (!this.filters.isEmpty()) {
			for (CategoricalTraitFilter filter : this.filters) {
				if (!CategoricalVariatesCondition.DROP_TRAIT.equals(filter.getCondition())) {
					return false;
				}
			}
		}
		return true;
	}

}
