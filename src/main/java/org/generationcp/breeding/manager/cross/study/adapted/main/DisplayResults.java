
package org.generationcp.breeding.manager.cross.study.adapted.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.generationcp.breeding.manager.application.GermplasmStudyBrowserLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.adapted.dialogs.SaveToListDialog;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CategoricalTraitEvaluator;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CategoricalTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CharacterTraitEvaluator;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.CharacterTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.NumericTraitEvaluator;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.ObservationList;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.TableResultRow;
import org.generationcp.breeding.manager.cross.study.adapted.main.pojos.TraitObservationScore;
import org.generationcp.breeding.manager.cross.study.constants.EnvironmentWeight;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.breeding.manager.study.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.ObservationKey;
import org.generationcp.middleware.domain.h2h.TraitType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class DisplayResults extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, GermplasmStudyBrowserLayout {

	private static final String GERMPLASM_COL_TABLE_HEIGHT = "445px";

	private static final String GERMPLASM_COL_TABLE_WIDTH = "340px";

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(DisplayResults.class);

	private static final String TAG_COLUMN_ID = "DisplayResults Tag Column Id";
	private static final String LINE_NO = "DisplayResults Line No";
	private static final String LINE_GID = "DisplayResults Line GID";
	private static final String LINE_DESIGNATION = "DisplayResults Line Designation";
	private static final String COMBINED_SCORE_COLUMN_ID = "DisplayResults Combined Score";

	public static final String SAVE_BUTTON_ID = "DisplayResults Save Button ID";
	public static final String BACK_BUTTON_ID = "DisplayResults Back Button ID";
	public static final String NEXT_ENTRY_BUTTON_ID = "DisplayResults Next Entry Button ID";
	public static final String PREV_ENTRY_BUTTON_ID = "DisplayResults Prev Entry Button ID";

	private final QueryForAdaptedGermplasmMain mainScreen;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private Table germplasmColTable;
	private Table traitsColTable;
	private Table combinedScoreTagColTable;

	private Integer noOfTraitColumns;
	private List<String> columnHeaders;

	List<TableResultRow> tableRows;
	List<TableResultRow> tableRowsSelected;
	Integer currentLineIndex;

	private Button backButton;
	private Button saveButton;
	private Button nextEntryBtn;
	private Button prevEntryBtn;

	private List<Integer> environmentIds;
	List<EnvironmentForComparison> environments;

	private List<Integer> traitIds;
	private Map<Integer, String> germplasmIdNameMap;
	private Map<String, Integer> germplasmNameIdMap;

	private List<NumericTraitFilter> numericTraitFilter;
	private List<CharacterTraitFilter> characterTraitFilter;
	private List<CategoricalTraitFilter> categoricalTraitFilter;

	private List<Observation> observations;
	private Map<ObservationKey, ObservationList> observationsMap;

	private SaveToListDialog saveGermplasmListDialog;
	private Map<Integer, String> selectedGermplasmMap;
	private Map<Object, Boolean> columnOrdering;

	private CheckBox tagAllCheckBoxOnCombinedScoreTagColTable;

	public DisplayResults(QueryForAdaptedGermplasmMain mainScreen) {
		this.mainScreen = mainScreen;
	}

	@Override
	public void updateLabels() {
		// not implemented
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {

		// initialize tables
		this.germplasmColTable = new Table();
		this.germplasmColTable.setWidth(GERMPLASM_COL_TABLE_WIDTH);
		this.germplasmColTable.setHeight(GERMPLASM_COL_TABLE_HEIGHT);
		this.germplasmColTable.setImmediate(true);
		this.germplasmColTable.setPageLength(15);
		this.germplasmColTable.setColumnCollapsingAllowed(true);
		this.germplasmColTable.setColumnReorderingAllowed(false);

		this.traitsColTable = new Table();
		this.traitsColTable.setWidth("490px");
		this.traitsColTable.setHeight(GERMPLASM_COL_TABLE_HEIGHT);
		this.traitsColTable.setImmediate(true);
		this.traitsColTable.setPageLength(15);
		this.traitsColTable.setColumnCollapsingAllowed(true);
		this.traitsColTable.setColumnReorderingAllowed(false);

		this.combinedScoreTagColTable = new Table();
		this.combinedScoreTagColTable.setWidth("160px");
		this.combinedScoreTagColTable.setHeight(GERMPLASM_COL_TABLE_HEIGHT);
		this.combinedScoreTagColTable.setImmediate(true);
		this.combinedScoreTagColTable.setPageLength(15);
		this.combinedScoreTagColTable.setColumnCollapsingAllowed(true);
		this.combinedScoreTagColTable.setColumnReorderingAllowed(false);

		this.tagAllCheckBoxOnCombinedScoreTagColTable = new CheckBox();
		this.tagAllCheckBoxOnCombinedScoreTagColTable.setImmediate(true);

		// initialize buttons
		this.prevEntryBtn = new Button(this.messageSource.getMessage(Message.PREV_ARROW));
		this.prevEntryBtn.setData(DisplayResults.NEXT_ENTRY_BUTTON_ID);
		this.prevEntryBtn.setWidth("50px");
		this.prevEntryBtn.setEnabled(true);
		this.prevEntryBtn.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.nextEntryBtn = new Button(this.messageSource.getMessage(Message.NEXT_ARROW));
		this.nextEntryBtn.setData(DisplayResults.NEXT_ENTRY_BUTTON_ID);
		this.nextEntryBtn.setWidth("50px");
		this.nextEntryBtn.setEnabled(true);
		this.nextEntryBtn.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.backButton = new Button(this.messageSource.getMessage(Message.BACK));
		this.backButton.setData(DisplayResults.BACK_BUTTON_ID);
		this.backButton.setWidth("100px");
		this.backButton.setEnabled(true);

		this.saveButton = new Button(this.messageSource.getMessage(Message.SAVE_GERMPLASMS_TO_NEW_LIST_LABEL));
		this.saveButton.setData(DisplayResults.SAVE_BUTTON_ID);
		this.saveButton.setWidth("100px");
		this.saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.saveButton.setEnabled(false);

	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {
		this.prevEntryBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7083618946346280184L;

			@Override
			public void buttonClick(ClickEvent event) {
				DisplayResults.this.prevEntryButtonClickAction();
			}
		});

		this.nextEntryBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4837144379158727020L;

			@Override
			public void buttonClick(ClickEvent event) {
				DisplayResults.this.nextEntryButtonClickAction();
			}
		});

		this.backButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8767137627847480579L;

			@Override
			public void buttonClick(ClickEvent event) {
				DisplayResults.this.backButtonClickAction();
			}
		});

		this.saveButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4170202465915624787L;

			@Override
			public void buttonClick(ClickEvent event) {
				DisplayResults.this.saveButtonClickAction();
			}
		});

		this.tagAllCheckBoxOnCombinedScoreTagColTable.addListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((Boolean) DisplayResults.this.tagAllCheckBoxOnCombinedScoreTagColTable.getValue()) {
					DisplayResults.this.tagAllEnvironmentsOnCombinedScoreTagColTable();
				} else {
					DisplayResults.this.untagAllEnvironmentsOnCombinedScoreTagColTable();
				}
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setHeight("550px");
		this.setWidth("1000px");

		AbsoluteLayout resultTable = new AbsoluteLayout();
		resultTable.setHeight("470px");
		resultTable.setWidth("1000px");
		resultTable.addComponent(this.germplasmColTable, "top:20px;left:20px");
		resultTable.addComponent(this.traitsColTable, "top:20px;left:345px");
		resultTable.addComponent(this.combinedScoreTagColTable, "top:20px;left:819px");

		this.addComponent(new Label("<style> .v-table-column-selector { width:0; height:0; overflow:hidden; }"
				+ ".v-table-row, .v-table-row-odd { height: 25px; } " + ".v-table-header { height: auto; background-color: #dcdee0;} "
				+ ".v-table-header-wrap { height: auto; background-color: #dcdee0; } "
				+ ".v-table-caption-container { height: auto; background-color: #dcdee0; } " + ".v-table { border-radius: 0px; } "
				+ " </style>", Label.CONTENT_XHTML));
		this.addComponent(resultTable, "top:0px;left:0px");

		this.addComponent(this.tagAllCheckBoxOnCombinedScoreTagColTable,
				"top:30px; left:" + (817 + this.combinedScoreTagColTable.getWidth() - 27) + "px;");

		this.addComponent(this.prevEntryBtn, "top:470px;left:455px");
		this.addComponent(this.nextEntryBtn, "top:470px;left:515x");
		this.addComponent(this.backButton, "top:510px;left:405px");
		this.addComponent(this.saveButton, "top:510px;left:515px");

	}

	public void populateResultsTable(List<EnvironmentForComparison> environments, List<NumericTraitFilter> numericTraitFilter,
			List<CharacterTraitFilter> characterTraitFilter, List<CategoricalTraitFilter> categoricalTraitFilter) {
		this.environments = environments;
		this.environmentIds = this.getEnvironmentIds(environments);
		this.numericTraitFilter = numericTraitFilter;
		this.characterTraitFilter = characterTraitFilter;
		this.categoricalTraitFilter = categoricalTraitFilter;

		this.traitIds = this.getTraitIds(numericTraitFilter, characterTraitFilter, categoricalTraitFilter);

		this.germplasmIdNameMap = this.getGermplasm(this.traitIds, this.environmentIds);
		this.germplasmNameIdMap = this.getSortedGermplasmList(this.germplasmIdNameMap);
		this.selectedGermplasmMap = new HashMap<Integer, String>();

		// TODO we might want to optimize the creation of the 3 tables so that no duplication of data is done
		// also we must think of a more optimal solution for sorting so that we don't have to replicate all columns in the 3 tables
		// perhaps we can keep one common column which will be the basis for sorting in the 3 tables
		this.germplasmColTable = this.createResultsTable(this.germplasmColTable);
		this.traitsColTable = this.createResultsTable(this.traitsColTable);
		this.combinedScoreTagColTable = this.createResultsTable(this.combinedScoreTagColTable);

		for (Object propertyId : this.germplasmColTable.getContainerPropertyIds()) {
			if (propertyId.toString().equals(DisplayResults.LINE_NO)) {
				this.germplasmColTable.setColumnCollapsed(propertyId, false);
			} else if (propertyId.toString().equals(DisplayResults.LINE_GID)) {
				this.germplasmColTable.setColumnCollapsed(propertyId, false);
			} else if (propertyId.toString().equals(DisplayResults.LINE_DESIGNATION)) {
				this.germplasmColTable.setColumnCollapsed(propertyId, false);
			} else {
				this.germplasmColTable.setColumnCollapsed(propertyId, true);
			}
		}

		for (Object propertyId : this.traitsColTable.getContainerPropertyIds()) {
			if (propertyId.toString().equals(DisplayResults.LINE_NO)) {
				this.traitsColTable.setColumnCollapsed(propertyId, true);
			} else if (propertyId.toString().equals(DisplayResults.LINE_GID)) {
				this.traitsColTable.setColumnCollapsed(propertyId, true);
			} else if (propertyId.toString().equals(DisplayResults.LINE_DESIGNATION)) {
				this.traitsColTable.setColumnCollapsed(propertyId, true);
			} else if (propertyId.toString().equals(DisplayResults.TAG_COLUMN_ID)) {
				this.traitsColTable.setColumnCollapsed(propertyId, true);
			} else if (propertyId.toString().equals(DisplayResults.COMBINED_SCORE_COLUMN_ID)) {
				this.traitsColTable.setColumnCollapsed(propertyId, true);
			} else {
				this.traitsColTable.setColumnCollapsed(propertyId, false);
			}
		}

		for (Object propertyId : this.combinedScoreTagColTable.getContainerPropertyIds()) {
			if (propertyId.toString().equals(DisplayResults.TAG_COLUMN_ID)) {
				this.combinedScoreTagColTable.setColumnCollapsed(propertyId, false);
			} else if (propertyId.toString().equals(DisplayResults.COMBINED_SCORE_COLUMN_ID)) {
				this.combinedScoreTagColTable.setColumnCollapsed(propertyId, false);
			} else {
				this.combinedScoreTagColTable.setColumnCollapsed(propertyId, true);
			}
		}

		// header column listener
		this.initializeColumnOrdering();

		this.germplasmColTable.addListener(new Table.HeaderClickListener() {

			private static final long serialVersionUID = -9165077040691158639L;

			@Override
			public void headerClick(HeaderClickEvent event) {
				Object property = event.getPropertyId();
				Object[] properties = new Object[] {property};

				boolean order = DisplayResults.this.columnOrdering.get(property);
				order = order ? false : true;

				DisplayResults.this.columnOrdering.put(property, order);

				boolean[] ordering = new boolean[] {order};

				DisplayResults.this.traitsColTable.sort(properties, ordering);
				DisplayResults.this.combinedScoreTagColTable.sort(properties, ordering);
			}
		});

		this.traitsColTable.addListener(new Table.HeaderClickListener() {

			private static final long serialVersionUID = -6923284105485115775L;

			@Override
			public void headerClick(HeaderClickEvent event) {
				Object property = event.getPropertyId();
				Object[] properties = new Object[] {property};

				boolean order = DisplayResults.this.columnOrdering.get(property);
				order = order ? false : true;

				DisplayResults.this.columnOrdering.put(property, order);

				boolean[] ordering = new boolean[] {order};

				DisplayResults.this.germplasmColTable.sort(properties, ordering);
				DisplayResults.this.combinedScoreTagColTable.sort(properties, ordering);
			}
		});

		this.combinedScoreTagColTable.addListener(new Table.HeaderClickListener() {

			private static final long serialVersionUID = 9161532217269536655L;

			@Override
			public void headerClick(HeaderClickEvent event) {
				Object property = event.getPropertyId();
				Object[] properties = new Object[] {property};

				boolean order = DisplayResults.this.columnOrdering.get(property);
				order = order ? false : true;

				DisplayResults.this.columnOrdering.put(property, order);

				boolean[] ordering = new boolean[] {order};

				DisplayResults.this.traitsColTable.sort(properties, ordering);
				DisplayResults.this.germplasmColTable.sort(properties, ordering);
			}
		});

	}

	public Table createResultsTable(Table resultTable) {

		List<Object> propertyIds = new ArrayList<Object>();
		for (Object propertyId : resultTable.getContainerPropertyIds()) {
			propertyIds.add(propertyId);
		}
		for (Object propertyId : propertyIds) {
			resultTable.removeContainerProperty(propertyId);
			resultTable.removeGeneratedColumn(propertyId);
		}

		resultTable.removeAllItems();

		resultTable.addContainerProperty(DisplayResults.LINE_NO, Integer.class, null);
		resultTable.addContainerProperty(DisplayResults.LINE_GID, Button.class, null);
		resultTable.addContainerProperty(DisplayResults.LINE_DESIGNATION, String.class, null);

		resultTable.setColumnHeader(DisplayResults.LINE_NO, "Line<br/> No");
		resultTable.setColumnHeader(DisplayResults.LINE_GID, "Line<br/> GID");
		resultTable.setColumnHeader(DisplayResults.LINE_DESIGNATION, "Line<br/> Designation");

		Integer noOfColumns = 3;
		this.noOfTraitColumns = 0;
		for (NumericTraitFilter trait : this.numericTraitFilter) {
			String name = this.getNameLabel(trait.getTraitInfo().getName().trim());
			String weight = this.getWeightLabel(trait.getPriority().getWeight());
			Integer traitId = trait.getTraitInfo().getId();

			resultTable.addContainerProperty(this.getContainerPropertyName(name, traitId, TraitType.NUMERIC), Integer.class, null);
			resultTable.addContainerProperty(this.getContainerPropertyName(weight, traitId, TraitType.NUMERIC), Double.class, null);

			resultTable.setColumnHeader(this.getContainerPropertyName(name, traitId, TraitType.NUMERIC), name);
			resultTable.setColumnHeader(this.getContainerPropertyName(weight, traitId, TraitType.NUMERIC), weight);

			noOfColumns += 2;
			this.noOfTraitColumns += 2;
		}

		for (CharacterTraitFilter trait : this.characterTraitFilter) {
			String name = this.getNameLabel(trait.getTraitInfo().getName().trim());
			String weight = this.getWeightLabel(trait.getPriority().getWeight());
			Integer traitId = trait.getTraitInfo().getId();

			resultTable.addContainerProperty(this.getContainerPropertyName(name, traitId, TraitType.CHARACTER), Integer.class, null);
			resultTable.addContainerProperty(this.getContainerPropertyName(weight, traitId, TraitType.CHARACTER), Double.class, null);

			resultTable.setColumnHeader(this.getContainerPropertyName(name, traitId, TraitType.CHARACTER), name);
			resultTable.setColumnHeader(this.getContainerPropertyName(weight, traitId, TraitType.CHARACTER), weight);

			noOfColumns += 2;
			this.noOfTraitColumns += 2;
		}

		for (CategoricalTraitFilter trait : this.categoricalTraitFilter) {
			String name = this.getNameLabel(trait.getTraitInfo().getName().trim());
			String weight = this.getWeightLabel(trait.getPriority().getWeight());
			Integer traitId = trait.getTraitInfo().getId();

			resultTable.addContainerProperty(this.getContainerPropertyName(name, traitId, TraitType.CATEGORICAL), Integer.class, null);
			resultTable.addContainerProperty(this.getContainerPropertyName(weight, traitId, TraitType.CATEGORICAL), Double.class, null);

			resultTable.setColumnHeader(this.getContainerPropertyName(name, traitId, TraitType.CATEGORICAL), name);
			resultTable.setColumnHeader(this.getContainerPropertyName(weight, traitId, TraitType.CATEGORICAL), weight);

			noOfColumns += 2;
			this.noOfTraitColumns += 2;
		}

		resultTable.addContainerProperty(DisplayResults.COMBINED_SCORE_COLUMN_ID, Double.class, null);
		resultTable.setColumnHeader(DisplayResults.COMBINED_SCORE_COLUMN_ID, "Combined<br/> Score");
		noOfColumns++;

		resultTable.addContainerProperty(DisplayResults.TAG_COLUMN_ID, CheckBox.class, null);
		resultTable.setColumnHeader(DisplayResults.TAG_COLUMN_ID, "Tag<br/>\n");
		noOfColumns++;

		this.tableRows = this.getTableRowsResults();
		this.currentLineIndex = 0;
		this.populateRowsResultsTable(resultTable, noOfColumns);

		return resultTable;
	}

	private String getNameLabel(String name) {
		return name + "<br/> No of Obs";
	}

	private String getWeightLabel(int weight) {
		return "Wt = " + weight + "<br/> Score";
	}

	private String getContainerPropertyName(String name, Integer traitId, TraitType traitType) {
		return "DisplayResults " + name + traitId + " " + traitType.toString().toLowerCase();
	}

	public void populateRowsResultsTable(Table resultTable, Integer noOfColumns) {
		int lineNo = this.currentLineIndex + 1;
		int endOfListIndex = this.currentLineIndex + 15;

		if (endOfListIndex > this.tableRows.size()) {
			endOfListIndex = this.tableRows.size();
		}

		for (TableResultRow row : this.tableRows.subList(this.currentLineIndex, endOfListIndex)) {
			int gid = row.getGermplasmId();
			String germplasmName = this.germplasmIdNameMap.get(gid);

			Object[] itemObj = new Object[noOfColumns];

			itemObj[0] = lineNo;

			// make GID as link
			String gidString = String.valueOf(gid);
			Button gidButton = new Button(gidString, new GidLinkButtonClickListener(gidString));
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			gidButton.setDescription("Click to view Germplasm information");
			itemObj[1] = gidButton;
			itemObj[2] = germplasmName == null ? "" : germplasmName;

			this.columnHeaders = this.getColumnProperties(resultTable.getContainerPropertyIds());

			Map<NumericTraitFilter, TraitObservationScore> numericTOSMap = row.getNumericTOSMap();
			for (Map.Entry<NumericTraitFilter, TraitObservationScore> numericTOS : numericTOSMap.entrySet()) {
				String traitName = numericTOS.getKey().getTraitInfo().getName().trim();
				Integer traitId = numericTOS.getKey().getTraitInfo().getId();

				String name = this.getNameLabel(traitName);

				int index = this.columnHeaders.indexOf(this.getContainerPropertyName(name, traitId, TraitType.NUMERIC));

				itemObj[index] = numericTOS.getValue().getNoOfObservation();
				itemObj[index + 1] = numericTOS.getValue().getWtScore();

			}

			Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap = row.getCharacterTOSMap();
			for (Map.Entry<CharacterTraitFilter, TraitObservationScore> characterTOS : characterTOSMap.entrySet()) {
				String traitName = characterTOS.getKey().getTraitInfo().getName().trim();
				Integer traitId = characterTOS.getKey().getTraitInfo().getId();

				String name = this.getNameLabel(traitName);

				int index = this.columnHeaders.indexOf(this.getContainerPropertyName(name, traitId, TraitType.CHARACTER));

				itemObj[index] = characterTOS.getValue().getNoOfObservation();
				itemObj[index + 1] = characterTOS.getValue().getWtScore();

			}

			Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap = row.getCategoricalTOSMap();
			for (Map.Entry<CategoricalTraitFilter, TraitObservationScore> categoricalTOS : categoricalTOSMap.entrySet()) {
				String traitName = categoricalTOS.getKey().getTraitInfo().getName().trim();
				Integer traitId = categoricalTOS.getKey().getTraitInfo().getId();

				String name = this.getNameLabel(traitName);

				int index = this.columnHeaders.indexOf(this.getContainerPropertyName(name, traitId, TraitType.CATEGORICAL));

				itemObj[index] = categoricalTOS.getValue().getNoOfObservation();
				itemObj[index + 1] = categoricalTOS.getValue().getWtScore();

			}

			itemObj[noOfColumns - 2] = row.getCombinedScore();

			CheckBox box = new CheckBox();
			box.setImmediate(true);
			box.setData(row);
			if (this.selectedGermplasmMap.containsKey(gid)) {
				box.setValue(true);
			}

			box.addListener(new ClickListener() {

				private static final long serialVersionUID = -3482228761993860979L;

				@Override
				public void buttonClick(ClickEvent event) {
					CheckBox box = (CheckBox) event.getSource();
					TableResultRow row = (TableResultRow) box.getData();

					if (box.booleanValue()) {
						box.setValue(true);
					} else {
						box.setValue(false);
					}

					DisplayResults.this.addItemForSelectedGermplasm(box, row);
				}
			});

			itemObj[noOfColumns - 1] = box;

			resultTable.addItem(itemObj, row);

			lineNo++;
		}
	}

	@SuppressWarnings("rawtypes")
	public List<String> getColumnProperties(Collection properties) {
		List<String> headers = new ArrayList<String>();

		for (Object prop : properties) {
			headers.add(prop.toString());
		}

		return headers;
	}

	public void initializeColumnOrdering() {
		this.columnOrdering = new HashMap<Object, Boolean>();

		for (Object column : this.germplasmColTable.getContainerPropertyIds()) {
			if (column.equals(DisplayResults.LINE_DESIGNATION)) {
				this.columnOrdering.put(column, false);
			} else {
				this.columnOrdering.put(column, true);
			}

		}
	}

	private Double getTotalEnvWeightForTrait(Integer traitId, Integer gid) {
		Double totalEnvWeight = 0.0;
		for (EnvironmentForComparison env : this.environments) {
			ObservationKey key = new ObservationKey(traitId, gid, env.getEnvironmentNumber());
			ObservationList obsList = this.observationsMap.get(key);

			if (obsList != null) {
				ComboBox weightComboBox = env.getWeightComboBox();
				EnvironmentWeight weight = (EnvironmentWeight) weightComboBox.getValue();
				totalEnvWeight = totalEnvWeight + Double.valueOf(weight.getWeight());
			}
		}
		return totalEnvWeight;
	}

	private Double roundOffDoubleToTwoDecimalPlaces(Double toRoundOff) {
		double roundedOff = Math.round(toRoundOff.doubleValue() * 100.0) / 100.0;
		return Double.valueOf(roundedOff);
	}

	public List<TableResultRow> getTableRowsResults() {
		List<TableResultRow> rows = new ArrayList<TableResultRow>();

		try {
			// TODO must reuse the observations class Object and not have multiple calls of getObservationForTraits
			this.observations = this.crossStudyDataManager.getObservationsForTraits(this.traitIds, this.environmentIds);
			this.observationsMap = this.getObservationsMap(this.observations);

			List<Integer> germplasmIds = new ArrayList<Integer>();
			germplasmIds.addAll(this.germplasmIdNameMap.keySet());

			for (Map.Entry<String, Integer> germplasm : this.germplasmNameIdMap.entrySet()) {
				DisplayResults.LOG.debug("Processing : " + germplasm.getValue() + ":" + germplasm.getKey());
				int germplasmId = germplasm.getValue();

				Map<NumericTraitFilter, TraitObservationScore> numericTOSMap = new HashMap<NumericTraitFilter, TraitObservationScore>();
				Map<CharacterTraitFilter, TraitObservationScore> characterTOSMap =
						new HashMap<CharacterTraitFilter, TraitObservationScore>();
				Map<CategoricalTraitFilter, TraitObservationScore> categoricalTOSMap =
						new HashMap<CategoricalTraitFilter, TraitObservationScore>();

				// NUMERIC TRAIT
				for (NumericTraitFilter trait : this.numericTraitFilter) {
					Double envWt = 0.0;
					Integer noOfObservation = 0;
					Integer noObsForAllEnvs = 0;
					Double scorePerTrait = 0.0;

					Double totalEnvWeight = this.getTotalEnvWeightForTrait(trait.getTraitInfo().getId(), germplasmId);

					DisplayResults.LOG.info("Processing Trait : " + trait.getTraitInfo() + ":weight:" + totalEnvWeight);

					for (EnvironmentForComparison env : this.environments) {
						ObservationKey key = new ObservationKey(trait.getTraitInfo().getId(), germplasmId, env.getEnvironmentNumber());
						ObservationList obsList = this.observationsMap.get(key);

						// if the observation exist
						if (obsList != null) {
							DisplayResults.LOG.trace("Processing : " + key + "Found Observations : " + obsList.getObservationList());
							ComboBox weightComboBox = env.getWeightComboBox();
							EnvironmentWeight weight = (EnvironmentWeight) weightComboBox.getValue();
							envWt = Double.valueOf(weight.getWeight()) / totalEnvWeight;

							noOfObservation = obsList.getObservationList().size();
							noObsForAllEnvs += noOfObservation;

							Double scorePerEnv = 0.0;
							for (Observation obs : obsList.getObservationList()) {
								if (this.testNumericTraitVal(trait, obs)) {
									DisplayResults.LOG.trace("Complies - Adding : " + obs.getValue());
									scorePerEnv = scorePerEnv + Double.valueOf(1);
								}
							}

							// for an environment with 3 values in range out of 5, we get a score of 3/5=0.6
							scorePerEnv = envWt * (scorePerEnv / Double.valueOf(noOfObservation));

							DisplayResults.LOG.info("Score for env : " + env.getLocationName() + ":" + scorePerEnv);

							scorePerTrait += scorePerEnv;
						}
					}

					// No Of Observation and Wt Score Per Trait
					scorePerTrait = this.roundOffDoubleToTwoDecimalPlaces(scorePerTrait);
					DisplayResults.LOG.info("Score for trait : " + trait.getTraitInfo().getName());
					TraitObservationScore tos = new TraitObservationScore(germplasmId, noObsForAllEnvs, scorePerTrait);
					DisplayResults.LOG.info("Trait Map Add : " + trait.getTraitInfo() + tos);
					numericTOSMap.put(trait, tos);
				}

				// CHARACTER TRAIT
				for (CharacterTraitFilter trait : this.characterTraitFilter) {
					Double envWt = 0.0;
					Integer noOfObservation = 0;
					Integer noObsForAllEnvs = 0;
					Double scorePerTrait = 0.0;

					Double totalEnvWeight = this.getTotalEnvWeightForTrait(trait.getTraitInfo().getId(), germplasmId);

					for (EnvironmentForComparison env : this.environments) {
						ObservationKey key = new ObservationKey(trait.getTraitInfo().getId(), germplasmId, env.getEnvironmentNumber());
						ObservationList obsList = this.observationsMap.get(key);

						// if the observation exist
						if (obsList != null) {
							ComboBox weightComboBox = env.getWeightComboBox();
							EnvironmentWeight weight = (EnvironmentWeight) weightComboBox.getValue();
							envWt = Double.valueOf(weight.getWeight()) / totalEnvWeight;

							noOfObservation = obsList.getObservationList().size();
							noObsForAllEnvs += noOfObservation;

							Double scorePerEnv = 0.0;
							for (Observation obs : obsList.getObservationList()) {
								if (this.testCharacterTraitVal(trait, obs)) {
									scorePerEnv = scorePerEnv + Double.valueOf(1);
								}
							}

							scorePerEnv = envWt * (scorePerEnv / Double.valueOf(noOfObservation));

							scorePerTrait += scorePerEnv;
						}
					}

					// No Of Observation and Wt Score Per Trait
					scorePerTrait = this.roundOffDoubleToTwoDecimalPlaces(scorePerTrait);
					TraitObservationScore tos = new TraitObservationScore(germplasmId, noObsForAllEnvs, scorePerTrait);
					characterTOSMap.put(trait, tos);
				}

				// CATEGORICAL TRAIT
				for (CategoricalTraitFilter trait : this.categoricalTraitFilter) {
					Double envWt = 0.0;
					Integer noOfObservation = 0;
					Integer noObsForAllEnvs = 0;
					Double scorePerTrait = 0.0;

					Double totalEnvWeight = this.getTotalEnvWeightForTrait(trait.getTraitInfo().getId(), germplasmId);

					for (EnvironmentForComparison env : this.environments) {
						ObservationKey key = new ObservationKey(trait.getTraitInfo().getId(), germplasmId, env.getEnvironmentNumber());
						ObservationList obsList = this.observationsMap.get(key);

						// if the observation exist
						if (obsList != null) {
							ComboBox weightComboBox = env.getWeightComboBox();
							EnvironmentWeight weight = (EnvironmentWeight) weightComboBox.getValue();
							envWt = Double.valueOf(weight.getWeight()) / totalEnvWeight;

							noOfObservation = obsList.getObservationList().size();
							noObsForAllEnvs += noOfObservation;

							Double scorePerEnv = 0.0;
							for (Observation obs : obsList.getObservationList()) {
								if (this.testCategoricalTraitVal(trait, obs)) {
									scorePerEnv = scorePerEnv + Double.valueOf(1);
								}
							}

							scorePerEnv = envWt * (scorePerEnv / Double.valueOf(noOfObservation));

							scorePerTrait += scorePerEnv;
						}
					}

					// No Of Observation and Wt Score Per Trait
					scorePerTrait = this.roundOffDoubleToTwoDecimalPlaces(scorePerTrait);
					TraitObservationScore tos = new TraitObservationScore(germplasmId, noObsForAllEnvs, scorePerTrait);
					categoricalTOSMap.put(trait, tos);
				}

				rows.add(new TableResultRow(germplasmId, numericTOSMap, characterTOSMap, categoricalTOSMap));
			}

		} catch (MiddlewareQueryException e) {
			DisplayResults.LOG.error(e.getMessage(), e);
		}

		// Sort the TableResultRow instances by Combined Score, highest to lowest. This alleviates the
		// Vaadin behaviour whereby sorting only occurs in the visible page
		Collections.sort(rows, new Comparator<TableResultRow>() {

			@Override
			public int compare(TableResultRow o1, TableResultRow o2) {
				return o2.getCombinedScore().compareTo(o1.getCombinedScore());
			}

		});
		return rows;
	}

	public boolean testNumericTraitVal(NumericTraitFilter trait, Observation observation) {
		// skip testing traits with "missing" value
		if ("missing".equalsIgnoreCase(observation.getValue())) {
			return true;
		} else {
			NumericTraitEvaluator eval =
					new NumericTraitEvaluator(trait.getCondition(), trait.getLimits(), Double.valueOf(observation.getValue()));
			return eval.evaluate();
		}
	}

	public boolean testCharacterTraitVal(CharacterTraitFilter trait, Observation obsevation) {
		CharacterTraitEvaluator eval = new CharacterTraitEvaluator(trait.getCondition(), trait.getLimits(), obsevation.getValue());
		return eval.evaluate();
	}

	public boolean testCategoricalTraitVal(CategoricalTraitFilter trait, Observation observation) {
		// skip testing traits with "missing" value
		if ("missing".equalsIgnoreCase(observation.getValue())) {
			return true;
		} else {
			CategoricalTraitEvaluator eval = new CategoricalTraitEvaluator(trait.getCondition(), trait.getLimits(), observation.getValue());
			return eval.evaluate();
		}
	}

	public Map<ObservationKey, ObservationList> getObservationsMap(List<Observation> observations) {
		Map<ObservationKey, ObservationList> keyObservationsMap = new HashMap<ObservationKey, ObservationList>();

		for (Observation obs : observations) {
			ObservationKey key = obs.getId();

			if (!keyObservationsMap.containsKey(key)) {
				ObservationList list = new ObservationList(key);
				list.add(obs);
				keyObservationsMap.put(key, list);
			} else {
				ObservationList obslist = keyObservationsMap.get(key);
				List<Observation> list = obslist.getObservationList();
				list.add(obs);
				obslist.setObservationList(list);

				keyObservationsMap.put(key, obslist);
			}
		}

		return keyObservationsMap;
	}

	public List<Integer> getTraitIds(List<NumericTraitFilter> numericTraitFilter, List<CharacterTraitFilter> characterTraitFilter,
			List<CategoricalTraitFilter> categoricalTraitFilter) {
		List<Integer> ids = new ArrayList<Integer>();

		for (NumericTraitFilter trait : numericTraitFilter) {
			ids.add(trait.getTraitInfo().getId());
		}

		for (CharacterTraitFilter trait : characterTraitFilter) {
			ids.add(trait.getTraitInfo().getId());
		}

		for (CategoricalTraitFilter trait : categoricalTraitFilter) {
			ids.add(trait.getTraitInfo().getId());
		}

		return ids;
	}

	public List<Integer> getEnvironmentIds(List<EnvironmentForComparison> environments) {
		List<Integer> envIds = new ArrayList<Integer>();

		for (EnvironmentForComparison env : environments) {
			envIds.add(env.getEnvironmentNumber());
		}

		return envIds;
	}

	public Map<Integer, String> getGermplasm(List<Integer> traitIds, List<Integer> environmentIds) {
		Map<Integer, String> gidNameMap = new HashMap<Integer, String>();

		List<Integer> germplasmIds = new ArrayList<Integer>();
		List<Integer> traitIdList = new ArrayList<Integer>();
		traitIdList.addAll(traitIds);

		try {
			// TODO must reuse this observations Object and not have multiple calls of getObservationForTraits
			this.observations = this.crossStudyDataManager.getObservationsForTraits(traitIdList, environmentIds);

			Iterator<Observation> obsIter = this.observations.iterator();
			while (obsIter.hasNext()) {
				Observation observation = obsIter.next();
				int id = observation.getId().getGermplasmId();
				if (!germplasmIds.contains(id)) {
					germplasmIds.add(id);
				}
			}

			gidNameMap = this.germplasmDataManager.getPreferredNamesByGids(germplasmIds);
		} catch (MiddlewareQueryException ex) {
			DisplayResults.LOG.error("Database error!", ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!", this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

		return gidNameMap;
	}

	public Map<String, Integer> getSortedGermplasmList(Map<Integer, String> germplasmList) {
		Map<String, Integer> sorted = new TreeMap<String, Integer>();

		for (Map.Entry<Integer, String> entry : germplasmList.entrySet()) {
			String name = entry.getValue();
			if (name == null) {
				name = "";
			}

			Integer id = entry.getKey();
			sorted.put(name, id);
		}

		return sorted;
	}

	public void nextEntryButtonClickAction() {
		if (!(this.currentLineIndex + 15 > this.tableRows.size())) {
			this.germplasmColTable.removeAllItems();
			this.traitsColTable.removeAllItems();
			this.combinedScoreTagColTable.removeAllItems();

			this.currentLineIndex += 15;

			int noOfColumns = this.noOfTraitColumns + 5;
			this.populateRowsResultsTable(this.germplasmColTable, noOfColumns);
			this.populateRowsResultsTable(this.traitsColTable, noOfColumns);
			this.populateRowsResultsTable(this.combinedScoreTagColTable, noOfColumns);
		} else {
			MessageNotifier.showWarning(this.getWindow(), "Notification", "No More Rows to display.");
		}
	}

	public void prevEntryButtonClickAction() {
		this.currentLineIndex -= 15;
		if (!(this.currentLineIndex < 0)) {
			this.germplasmColTable.removeAllItems();
			this.traitsColTable.removeAllItems();
			this.combinedScoreTagColTable.removeAllItems();

			int noOfColumns = this.noOfTraitColumns + 5;
			this.populateRowsResultsTable(this.germplasmColTable, noOfColumns);
			this.populateRowsResultsTable(this.traitsColTable, noOfColumns);
			this.populateRowsResultsTable(this.combinedScoreTagColTable, noOfColumns);
		} else {
			this.currentLineIndex = 0;
			MessageNotifier.showWarning(this.getWindow(), "Notification", "No More Rows to preview.");
		}
	}

	public void backButtonClickAction() {
		this.mainScreen.selectSecondTab();
	}

	public void saveButtonClickAction() {
		this.openDialogSaveList();
	}

	public void addItemForSelectedGermplasm(CheckBox box, TableResultRow row) {
		Integer gid = row.getGermplasmId();
		String preferredName = this.germplasmIdNameMap.get(gid);

		if (this.selectedGermplasmMap.isEmpty()) {
			this.selectedGermplasmMap.put(gid, preferredName);
		} else {
			if (this.selectedGermplasmMap.containsKey(gid)) {
				this.selectedGermplasmMap.remove(gid);
			} else {
				this.selectedGermplasmMap.put(gid, preferredName);
			}
		}

		this.toggleSaveButton();

	}

	public void toggleSaveButton() {
		if (!this.selectedGermplasmMap.isEmpty()) {
			this.saveButton.setEnabled(true);
		} else if (!this.selectedGermplasmMap.isEmpty()) {
			this.saveButton.setEnabled(false);
		}
	}

	private void openDialogSaveList() {
		Window parentWindow = this.getWindow();

		this.saveGermplasmListDialog = new SaveToListDialog(this.mainScreen, this, parentWindow, this.selectedGermplasmMap);
		this.saveGermplasmListDialog.addStyleName(Reindeer.WINDOW_LIGHT);

		parentWindow.addWindow(this.saveGermplasmListDialog);
	}

	private void tagAllEnvironmentsOnCombinedScoreTagColTable() {
		Object[] tableItemIds = this.combinedScoreTagColTable.getItemIds().toArray();
		for (int i = 0; i < tableItemIds.length; i++) {
			if (this.combinedScoreTagColTable.getItem(tableItemIds[i]).getItemProperty(DisplayResults.TAG_COLUMN_ID).getValue() instanceof CheckBox) {
				((CheckBox) this.combinedScoreTagColTable.getItem(tableItemIds[i]).getItemProperty(DisplayResults.TAG_COLUMN_ID).getValue())
						.setValue(true);
			}
		}
		this.selectedGermplasmMap.clear();
		for (int i = 0; i < this.tableRows.size(); i++) {
			String preferredName = this.germplasmIdNameMap.get(this.tableRows.get(i).getGermplasmId());
			this.selectedGermplasmMap.put(this.tableRows.get(i).getGermplasmId(), preferredName);
		}
		this.toggleSaveButton();
	}

	private void untagAllEnvironmentsOnCombinedScoreTagColTable() {
		Object[] tableItemIds = this.combinedScoreTagColTable.getItemIds().toArray();
		for (int i = 0; i < tableItemIds.length; i++) {
			if (this.combinedScoreTagColTable.getItem(tableItemIds[i]).getItemProperty(DisplayResults.TAG_COLUMN_ID).getValue() instanceof CheckBox) {
				((CheckBox) this.combinedScoreTagColTable.getItem(tableItemIds[i]).getItemProperty(DisplayResults.TAG_COLUMN_ID).getValue())
						.setValue(false);
			}
		}
		this.selectedGermplasmMap.clear();
		this.toggleSaveButton();
	}

}
