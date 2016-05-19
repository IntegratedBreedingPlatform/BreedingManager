
package org.generationcp.breeding.manager.cross.study.commons.trait.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.breeding.manager.cross.study.adapted.main.listeners.AdaptedGermplasmButtonClickListener;
import org.generationcp.breeding.manager.cross.study.adapted.main.listeners.AdaptedGermplasmValueChangeListener;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CharacterTraitFilter;
import org.generationcp.breeding.manager.cross.study.commons.trait.filter.listeners.CharacterTraitLimitsValueChangeListener;
import org.generationcp.breeding.manager.cross.study.constants.CharacterTraitCondition;
import org.generationcp.breeding.manager.cross.study.constants.TraitWeight;
import org.generationcp.breeding.manager.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.CharacterTraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * The component class for the Character Traits Section of the Trait Filter
 * 
 * @author Kevin Manansala (kevin@efficio.us.com)
 *
 */
@Configurable
public class CharacterTraitsSection extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 9099796930978032454L;

	private static final Logger LOG = LoggerFactory.getLogger(CharacterTraitsSection.class);

	private static final String TRAIT_COLUMN_ID = "Trait Column";
	private static final String NUM_LOCATIONS_COLUMN_ID = "Number of Locations";
	private static final String NUM_LINES_COLUMN_ID = "Number of Lines";
	private static final String NUM_OBSERVATIONS_COLUMN_ID = "Number of Observations";
	private static final String DISTINCT_OBSERVED_VALUES_COLUMN_ID = "Distinct Observed Values";
	private static final String CONDITION_COLUMN_ID = "Condition";
	private static final String LIMITS_COLUMN_ID = "Limits";
	private static final String PRIORITY_COLUMN_ID = "Priority";
	public static final String TRAIT_BUTTON_ID = "CharacterTraitsSection Trait Button ID";

	private List<Integer> environmentIds = null;
	private List<Integer> selectedTraits = null;

	private final Window parentWindow;
	private Label lblSectionTitle;
	private Table traitsTable;

	private int characterTraitCount;
	private boolean emptyMessageShown = false;
	private List<CharacterTraitFilter> filters;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	public CharacterTraitsSection(List<Integer> environmentIds, Window parentWindow) {
		super();
		this.environmentIds = environmentIds;
		this.parentWindow = parentWindow;
	}

	public CharacterTraitsSection(List<Integer> environmentIds, List<Integer> selectedTraits, Window parentWindow) {
		super();
		this.environmentIds = environmentIds;
		this.selectedTraits = selectedTraits;
		this.parentWindow = parentWindow;
	}

	private void initializeComponents() {
		this.lblSectionTitle = new Label(this.messageSource.getMessage(Message.CHARACTER_TRAITS_SECTION_TITLE));

		this.traitsTable = new Table();
		this.traitsTable.setImmediate(true);
		this.traitsTable.setColumnCollapsingAllowed(true);
		this.traitsTable.setColumnReorderingAllowed(true);

		this.traitsTable.addContainerProperty(CharacterTraitsSection.TRAIT_COLUMN_ID, Button.class, null);
		this.traitsTable.addContainerProperty(CharacterTraitsSection.NUM_LOCATIONS_COLUMN_ID, Integer.class, null);
		this.traitsTable.addContainerProperty(CharacterTraitsSection.NUM_LINES_COLUMN_ID, Integer.class, null);
		this.traitsTable.addContainerProperty(CharacterTraitsSection.NUM_OBSERVATIONS_COLUMN_ID, Integer.class, null);
		this.traitsTable.addContainerProperty(CharacterTraitsSection.DISTINCT_OBSERVED_VALUES_COLUMN_ID, String.class, null);
		this.traitsTable.addContainerProperty(CharacterTraitsSection.CONDITION_COLUMN_ID, ComboBox.class, null);
		this.traitsTable.addContainerProperty(CharacterTraitsSection.LIMITS_COLUMN_ID, TextField.class, null);
		this.traitsTable.addContainerProperty(CharacterTraitsSection.PRIORITY_COLUMN_ID, ComboBox.class, null);

		this.traitsTable.setColumnHeader(CharacterTraitsSection.TRAIT_COLUMN_ID, this.messageSource.getMessage(Message.HEAD_TO_HEAD_TRAIT)); // Trait
		this.traitsTable.setColumnHeader(CharacterTraitsSection.NUM_LOCATIONS_COLUMN_ID,
				this.messageSource.getMessage(Message.NUMBER_OF_LOCATIONS)); // # of Locations
		this.traitsTable
				.setColumnHeader(CharacterTraitsSection.NUM_LINES_COLUMN_ID, this.messageSource.getMessage(Message.NUMBER_OF_LINES)); // #
																																		// of
																																		// Lines
		this.traitsTable.setColumnHeader(CharacterTraitsSection.NUM_OBSERVATIONS_COLUMN_ID,
				this.messageSource.getMessage(Message.NUMBER_OF_OBSERVATIONS)); // # of Observations
		this.traitsTable.setColumnHeader(CharacterTraitsSection.DISTINCT_OBSERVED_VALUES_COLUMN_ID,
				this.messageSource.getMessage(Message.DISTINCT_OBSERVED_VALUES)); // Distinct Observed Values
		this.traitsTable.setColumnHeader(CharacterTraitsSection.CONDITION_COLUMN_ID,
				this.messageSource.getMessage(Message.CONDITION_HEADER)); // Condition
		this.traitsTable.setColumnHeader(CharacterTraitsSection.LIMITS_COLUMN_ID, this.messageSource.getMessage(Message.LIMITS)); // Limits
		this.traitsTable.setColumnHeader(CharacterTraitsSection.PRIORITY_COLUMN_ID, this.messageSource.getMessage(Message.PRIORITY)); // Priority

		this.traitsTable.setColumnWidth(CharacterTraitsSection.DISTINCT_OBSERVED_VALUES_COLUMN_ID, 250);

		this.traitsTable.setItemDescriptionGenerator(new ItemDescriptionGenerator() {

			private static final long serialVersionUID = -3207714818504151649L;

			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				if (propertyId != null && propertyId == CharacterTraitsSection.DISTINCT_OBSERVED_VALUES_COLUMN_ID) {
					Table theTraitsTable = (Table) source;
					Item item = theTraitsTable.getItem(itemId);
					String distinctValues =
							(String) item.getItemProperty(CharacterTraitsSection.DISTINCT_OBSERVED_VALUES_COLUMN_ID).getValue();
					return "<b>Distinct values:</b>  " + distinctValues;
				}
				return null;
			}
		});
	}

	private void initializeLayout() {
		this.setMargin(true);
		this.setSpacing(true);
		this.setWidth("995px");
		this.addComponent(this.lblSectionTitle);

		this.traitsTable.setHeight("360px");
		this.traitsTable.setWidth("960px");
		this.addComponent(this.traitsTable);
	}

	private void populateTraitsTable() {
		if (this.environmentIds != null && !this.environmentIds.isEmpty()) {
			List<CharacterTraitInfo> traitInfoObjects = null;
			try {
				traitInfoObjects = this.crossStudyDataManager.getTraitsForCharacterVariates(this.environmentIds);
				if (this.selectedTraits != null) {
					traitInfoObjects = this.filterUnwantedTraitsFromResults(traitInfoObjects, this.selectedTraits);
				}

			} catch (MiddlewareQueryException ex) {
				CharacterTraitsSection.LOG.error(
						"Error with getting character trait info given environment ids: " + this.environmentIds.toString(), ex);
				MessageNotifier.showError(
						this.parentWindow,
						"Database Error!",
						"Error with getting character trait info given environment ids. "
								+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				return;
			}

			if (traitInfoObjects != null) {
				this.characterTraitCount = traitInfoObjects.size();
				if (traitInfoObjects.isEmpty()) {
					return;
				}

				for (CharacterTraitInfo traitInfo : traitInfoObjects) {
					StringBuffer distinctValuesObserved = new StringBuffer();
					for (String value : traitInfo.getValues()) {
						distinctValuesObserved.append(value);
						distinctValuesObserved.append(", ");
					}

					Button traitNameLink = new Button(traitInfo.getName());
					traitNameLink.setImmediate(true);
					traitNameLink.setStyleName(BaseTheme.BUTTON_LINK);
					traitNameLink.setData(CharacterTraitsSection.TRAIT_BUTTON_ID);
					traitNameLink.addListener(new AdaptedGermplasmButtonClickListener(this, traitInfo.getId(), traitInfo.getName(),
							"Character Variate", this.environmentIds));

					ComboBox conditionComboBox = CrossStudyUtil.getCharacterTraitConditionsComboBox();
					ComboBox priorityComboBox = CrossStudyUtil.getTraitWeightsComboBox();
					TextField txtLimits = new TextField();
					txtLimits.setImmediate(true);
					txtLimits.setEnabled(false);

					conditionComboBox.addListener(new AdaptedGermplasmValueChangeListener(this, txtLimits, priorityComboBox));
					priorityComboBox.addListener(new AdaptedGermplasmValueChangeListener(this, conditionComboBox, null, txtLimits));
					txtLimits.addListener(new CharacterTraitLimitsValueChangeListener(this.parentWindow, traitInfo.getValues()));

					Object[] itemObj =
							new Object[] {traitNameLink, traitInfo.getLocationCount(), traitInfo.getGermplasmCount(),
									traitInfo.getObservationCount(), distinctValuesObserved.toString(), conditionComboBox, txtLimits,
									priorityComboBox};
					this.traitsTable.addItem(itemObj, traitInfo);
				}
			}
		}
	}

	// TODO : Rebecca is not happy with public/private method ordering
	// TODO : warning - On2 may need to revisit for performance
	private List<CharacterTraitInfo> filterUnwantedTraitsFromResults(List<CharacterTraitInfo> characterTraitInfos,
			List<Integer> desiredTraits) {
		List<CharacterTraitInfo> filteredTraits = new ArrayList<CharacterTraitInfo>();
		for (CharacterTraitInfo cto : characterTraitInfos) {
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
		this.initializeComponents();
		this.initializeLayout();
		this.populateTraitsTable();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.lblSectionTitle, Message.CHARACTER_TRAITS_SECTION_TITLE);
	}

	public void showEmptyTraitsMessage() {
		if (!this.emptyMessageShown && this.characterTraitCount == 0) {
			MessageNotifier.showMessage(this.parentWindow, "Information",
					"There were no character traits observed in the environments you have selected.", 3000);
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

	@SuppressWarnings("unchecked")
	public List<CharacterTraitFilter> getFilters() {
		this.filters = new ArrayList<CharacterTraitFilter>();

		Collection<CharacterTraitInfo> traitInfoObjects = (Collection<CharacterTraitInfo>) this.traitsTable.getItemIds();
		for (CharacterTraitInfo traitInfo : traitInfoObjects) {
			Item tableRow = this.traitsTable.getItem(traitInfo);

			ComboBox conditionComboBox = (ComboBox) tableRow.getItemProperty(CharacterTraitsSection.CONDITION_COLUMN_ID).getValue();
			CharacterTraitCondition condition = (CharacterTraitCondition) conditionComboBox.getValue();

			ComboBox priorityComboBox = (ComboBox) tableRow.getItemProperty(CharacterTraitsSection.PRIORITY_COLUMN_ID).getValue();
			TraitWeight priority = (TraitWeight) priorityComboBox.getValue();

			TextField limitsField = (TextField) tableRow.getItemProperty(CharacterTraitsSection.LIMITS_COLUMN_ID).getValue();
			String limitsString = limitsField.getValue().toString();

			if (condition != CharacterTraitCondition.DROP_TRAIT && priority != TraitWeight.IGNORED) {
				if (condition == CharacterTraitCondition.KEEP_ALL) {
					CharacterTraitFilter filter = new CharacterTraitFilter(traitInfo, condition, new ArrayList<String>(), priority);
					this.filters.add(filter);
				} else {
					if (limitsString != null && limitsString.length() > 0) {
						StringTokenizer tokenizer = new StringTokenizer(limitsString, ",");
						List<String> givenLimits = new ArrayList<String>();

						while (tokenizer.hasMoreTokens()) {
							String limit = tokenizer.nextToken().trim();
							givenLimits.add(limit);
						}

						CharacterTraitFilter filter = new CharacterTraitFilter(traitInfo, condition, givenLimits, priority);
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
			for (CharacterTraitFilter filter : this.filters) {
				if (!CharacterTraitCondition.DROP_TRAIT.equals(filter.getCondition())) {
					return false;
				}
			}
		}
		return true;
	}

}
