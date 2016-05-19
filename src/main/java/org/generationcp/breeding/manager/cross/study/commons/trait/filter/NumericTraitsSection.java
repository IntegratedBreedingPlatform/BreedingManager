
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
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.validators.NumericTraitLimitsValidator;
import org.generationcp.breeding.manager.cross.study.constants.NumericTraitCriteria;
import org.generationcp.breeding.manager.cross.study.constants.TraitWeight;
import org.generationcp.breeding.manager.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.NumericTraitInfo;
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
public class NumericTraitsSection extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	public static final String TRAIT_BUTTON_ID = "NumericTraitsSection Trait Button ID";

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(NumericTraitsSection.class);

	private final Window parentWindow;
	private Label lblSectionTitle;
	private Table traitsTable;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	private List<Integer> environmentIds = null;
	private List<Integer> selectedTraits = null;

	private final List<Field> fieldsToValidate = new ArrayList<Field>();
	private List<NumericTraitFilter> filters;

	private int numericTraitCount;
	private boolean emptyMessageShown = false;

	public NumericTraitsSection(List<Integer> environmentIds, Window parentWindow) {
		super();
		this.parentWindow = parentWindow;
		this.environmentIds = environmentIds;
	}

	public NumericTraitsSection(List<Integer> environmentIds, List<Integer> selectedTraits, Window parentWindow) {
		super();
		this.parentWindow = parentWindow;
		this.environmentIds = environmentIds;
		this.selectedTraits = selectedTraits;
	}

	@Override
	public void updateLabels() {
		// not implemented
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setSpacing(true);
		this.setMargin(true);

		this.initializeComponents();
		this.populateTable();
	}

	private void populateTable() {
		String limitsRequiredMessage =
				MessageFormat.format(this.messageSource.getMessage(Message.FIELD_IS_REQUIRED),
						this.messageSource.getMessage(Message.LIMITS));

		List<NumericTraitInfo> numericTraits = null;

		try {
			numericTraits = this.crossStudyDataManager.getTraitsForNumericVariates(this.environmentIds);
			if (this.selectedTraits != null) {
				numericTraits = this.filterUnwantedTraitsFromResults(numericTraits, this.selectedTraits);
			}
		} catch (MiddlewareQueryException e) {
			NumericTraitsSection.LOG.error("Database error!", e);
			MessageNotifier.showError(this.parentWindow, "Database Error!", "Error with getting numeric trait info given environment ids. "
					+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

		if (numericTraits != null) {
			this.numericTraitCount = numericTraits.size();
			if (numericTraits.isEmpty()) {
				return;
			}
			for (NumericTraitInfo trait : numericTraits) {
				double minValue = trait.getMinValue();
				double maxValue = trait.getMaxValue();

				Button traitNameLink = new Button(trait.getName());
				traitNameLink.setImmediate(true);
				traitNameLink.setStyleName(BaseTheme.BUTTON_LINK);
				traitNameLink.setData(NumericTraitsSection.TRAIT_BUTTON_ID);

				traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this, trait.getId(), trait.getName(), "Numeric Variate",
						this.environmentIds));

				TextField limitsField = new TextField();
				limitsField.setWidth("80px");
				limitsField.setEnabled(false);
				limitsField.setImmediate(true);
				limitsField.setRequired(true);
				limitsField.setRequiredError(limitsRequiredMessage);

				ComboBox conditionBox = CrossStudyUtil.getNumericTraitCombobox();
				conditionBox.setWidth("100px");
				ComboBox weightBox = CrossStudyUtil.getTraitWeightsComboBox();
				weightBox.setWidth("100px");

				conditionBox.addListener(new AdaptedGermplasmValueChangeListener(this, limitsField, weightBox));
				limitsField.addValidator(new NumericTraitLimitsValidator(conditionBox, minValue, maxValue));
				this.fieldsToValidate.add(limitsField);

				Object[] itemObj =
						new Object[] {traitNameLink, trait.getLocationCount(), trait.getGermplasmCount(), trait.getObservationCount(),
								minValue, trait.getMedianValue(), maxValue, conditionBox, limitsField, weightBox};

				this.traitsTable.addItem(itemObj, trait);
			}
		}
	}

	private void initializeComponents() {
		this.lblSectionTitle = new Label(this.messageSource.getMessage(Message.GET_NUMERIC_VARIATES));

		this.traitsTable = new Table();
		this.traitsTable.setImmediate(true);
		this.traitsTable.setColumnCollapsingAllowed(true);
		this.traitsTable.setColumnReorderingAllowed(true);
		this.traitsTable.setPageLength(10);

		for (TableColumn column : TableColumn.values()) {
			this.traitsTable.addContainerProperty(column, column.getColumnClass(), null);
			this.traitsTable.setColumnHeader(column, this.messageSource.getMessage(column.getMessage()));
			this.traitsTable.setColumnAlignment(column, Table.ALIGN_CENTER);
			this.traitsTable.setColumnWidth(column, column.getWidth());
		}

		this.traitsTable.setHeight("360px");
		this.traitsTable.setWidth("950px");

		this.addComponent(this.lblSectionTitle);
		this.addComponent(this.traitsTable);

	}

	// TODO : Rebecca is not happy with public/private method ordering
	// TODO : warning - On2 may need to revisit for performance
	private List<NumericTraitInfo> filterUnwantedTraitsFromResults(List<NumericTraitInfo> numericTraitInfos, List<Integer> desiredTraits) {
		List<NumericTraitInfo> filteredTraits = new ArrayList<NumericTraitInfo>();
		for (NumericTraitInfo cto : numericTraitInfos) {
			for (Integer traitId : desiredTraits) {
				if (cto.getId() == traitId.intValue()) {
					filteredTraits.add(cto);
				}
			}
		}
		return filteredTraits;
	}

	public void showEmptyTraitsMessage() {
		if (!this.emptyMessageShown && this.numericTraitCount == 0) {
			MessageNotifier.showMessage(this.parentWindow, "Information",
					"There were no numeric traits observed in the environments you have selected.", 3000);
			this.emptyMessageShown = true;
		}
	}

	public void showNumericVariateClickAction(Integer traitId, String traitName, List<Integer> envIds) {
		Window subParentWindow = this.getWindow();
		ViewTraitObservationsDialog viewTraitDialog =
				new ViewTraitObservationsDialog(this, subParentWindow, "Numeric Variate", traitId, traitName, envIds);
		viewTraitDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		subParentWindow.addWindow(viewTraitDialog);
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
			LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.INCORRECT_LIMITS_VALUE), e.getMessage());
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public List<NumericTraitFilter> getFilters() {
		this.filters = new ArrayList<NumericTraitFilter>();

		Collection<NumericTraitInfo> traitInfoObjects =
				(Collection<NumericTraitInfo>) this.traitsTable.getContainerDataSource().getItemIds();
		for (NumericTraitInfo traitInfo : traitInfoObjects) {
			Item tableRow = this.traitsTable.getItem(traitInfo);

			ComboBox conditionComboBox = (ComboBox) tableRow.getItemProperty(TableColumn.NUM_CONDITION_COL_ID).getValue();
			NumericTraitCriteria condition = (NumericTraitCriteria) conditionComboBox.getValue();

			ComboBox priorityComboBox = (ComboBox) tableRow.getItemProperty(TableColumn.NUM_PRIORITY_COL_ID).getValue();
			TraitWeight priority = (TraitWeight) priorityComboBox.getValue();

			TextField limitsField = (TextField) tableRow.getItemProperty(TableColumn.NUM_LIMITS_COL_ID).getValue();
			String limitsString = limitsField.getValue().toString();

			if (condition != NumericTraitCriteria.DROP_TRAIT && priority != TraitWeight.IGNORED) {
				if (condition == NumericTraitCriteria.KEEP_ALL) {
					NumericTraitFilter filter = new NumericTraitFilter(traitInfo, condition, new ArrayList<String>(), priority);
					this.filters.add(filter);
				} else {
					if (limitsString != null && limitsString.length() > 0) {
						StringTokenizer tokenizer = new StringTokenizer(limitsString, ",");
						List<String> givenLimits = new ArrayList<String>();

						while (tokenizer.hasMoreTokens()) {
							String limit = tokenizer.nextToken().trim();
							givenLimits.add(limit);
						}

						NumericTraitFilter filter = new NumericTraitFilter(traitInfo, condition, givenLimits, priority);
						this.filters.add(filter);
					}
				}

			}
		}
		return this.filters;
	}

	private enum TableColumn {
		NUM_TRAIT_COL_ID(Message.HEAD_TO_HEAD_TRAIT, Button.class, 80), NUM_NUMBER_OF_ENVTS_COL_ID(Message.NUMBER_OF_LOCATIONS,
				Integer.class, 97), NUM_NUMBER_OF_LINES_COL_ID(Message.NUMBER_OF_LINES, Integer.class, 65), NUM_NUMBER_OF_OBS_COL_ID(
				Message.NUMBER_OF_OBSERVATIONS, Integer.class, 123), NUM_MIN_COL_ID(Message.MIN, Double.class, 40), NUM_MEDIAN_COL_ID(
				Message.MEDIAN, Double.class, 50), NUM_MAX_COL_ID(Message.MAX, Double.class, 40), NUM_CONDITION_COL_ID(
				Message.CONDITION_HEADER, ComboBox.class, 100), NUM_LIMITS_COL_ID(Message.LIMITS, TextField.class, 85), NUM_PRIORITY_COL_ID(
				Message.PRIORITY, ComboBox.class, 105);

		private Message message;
		private Class<?> columnClass;
		private int width;

		private TableColumn(Message message, Class<?> columnClass, int width) {
			this.message = message;
			this.columnClass = columnClass;
			this.width = width;
		}

		public Message getMessage() {
			return this.message;
		}

		public Class<?> getColumnClass() {
			return this.columnClass;
		}

		public int getWidth() {
			return this.width;
		}

	}

	/*
	 * If at least one trait is NOT dropped, allow to proceed
	 */
	public boolean allTraitsDropped() {
		if (this.filters == null) {
			this.filters = this.getFilters();
		}
		if (!this.filters.isEmpty()) {
			for (NumericTraitFilter filter : this.filters) {
				if (!NumericTraitCriteria.DROP_TRAIT.equals(filter.getCondition())) {
					return false;
				}
			}
		}
		return true;
	}

}
