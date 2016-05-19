
package org.generationcp.breeding.manager.cross.study.h2h.main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmEntryDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmListDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.TablesEntries;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SpecifyGermplasmsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final String SEARCH_BUTTON_WIDTH = "130px";

	private static final long serialVersionUID = -7925696669478799303L;
	private static final Logger LOG = LoggerFactory.getLogger(SpecifyGermplasmsComponent.class);

	public static final String NEXT_BUTTON_ID = "SpecifyGermplasmsComponent Next Button ID";
	public static final String SELECT_TEST_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Test Entry Button ID";
	public static final String SELECT_STANDARD_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Standard Entry Button ID";

	public static final String SELECT_TEST_SEARCH_GERMPLASM_BUTTON_ID = "SpecifyGermplasmsComponent Test Search Germplasm Button ID";
	public static final String SELECT_STANDARD_SEARCH_GERMPLASM_BUTTON_ID =
			"SpecifyGermplasmsComponent Standard Search Germplasm Button ID";
	public static final String SELECT_TEST_SEARCH_GERMPLASM_LIST_BUTTON_ID =
			"SpecifyGermplasmsComponent Test Search Germplasm List Button ID";
	public static final String SELECT_STANDARD_SEARCH_GERMPLASM_LIST_BUTTON_ID =
			"SpecifyGermplasmsComponent Standard Search Germplasm List Button ID";

	// maximum # of pairs can be rendered
	private static final BigInteger MAX_NUM_OF_PAIRS = new BigInteger("2000");

	private Panel testPanel;
	private Panel standardPanel;

	private Button nextButton;

	private Button testSearchGermplasm;
	private Button standardSearchGermplasm;

	private Button testSearchGermplasmList;
	private Button standardSearchGermplasmList;

	private Label testSearchGermplasmLabel;
	private Label standardSearchGermplasmLabel;

	private Label testSearchGermplasmListLabel;
	private Label standardSearchGermplasmListLabel;

	private Label headerLabel;

	private final HeadToHeadCrossStudyMain mainScreen;
	private final TraitsAvailableComponent nextScreen;

	private Table entriesTable;

	private static final String TEST_ENTRY_COLUMN_ID = "SpecifyGermplasmsComponent Test Entry Column Id";
	private static final String STANDARD_ENTRY_COLUMN_ID = "SpecifyGermplasmsComponent Standard Entry Column Id";

	private static final Action ACTION_SELECT_ALL = new Action("Select All");
	private static final Action ACTION_DELETE = new Action("Delete selected");
	private static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] {SpecifyGermplasmsComponent.ACTION_SELECT_ALL,
			SpecifyGermplasmsComponent.ACTION_DELETE};

	@Autowired
	private GermplasmListManager germplasmListManager;

	private final Set<String> tableEntriesId = new HashSet<String>();
	private Set<String> singleEntriesSet = new HashSet<String>();

	private static final String TEST_ENTRY = "TEST";
	private static final String STANDARD_ENTRY = "STANDARD";

	private Map<String, String> germplasmIdNameMap;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public SpecifyGermplasmsComponent(HeadToHeadCrossStudyMain mainScreen, TraitsAvailableComponent nextScreen) {
		this.mainScreen = mainScreen;
		this.nextScreen = nextScreen;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("600px");
		this.setWidth("1000px");

		this.testPanel = new Panel("TEST");
		this.testPanel.setWidth("400px");
		this.testPanel.setSizeUndefined();

		this.standardPanel = new Panel("STANDARD");
		this.standardPanel.setWidth("470px");
		this.testPanel.setSizeUndefined();

		AbsoluteLayout absLayout = new AbsoluteLayout();
		absLayout.setWidth("400px");
		absLayout.setHeight("90px");

		this.testSearchGermplasmLabel = new Label(this.messageSource.getMessage(Message.SPECIFY_SINGLE_TEST_ENTRY));
		this.testSearchGermplasmLabel.setImmediate(true);

		this.testSearchGermplasmListLabel = new Label(this.messageSource.getMessage(Message.SPECIFY_TEST_GERMPLASM_LIST_ENTRY));
		this.testSearchGermplasmListLabel.setImmediate(true);

		this.testSearchGermplasm = new Button(this.messageSource.getMessage(Message.HEAD_TO_HEAD_SEARCH_GERMPLASM));
		this.testSearchGermplasm.setData(SpecifyGermplasmsComponent.SELECT_TEST_SEARCH_GERMPLASM_BUTTON_ID);
		this.testSearchGermplasm.setWidth(SpecifyGermplasmsComponent.SEARCH_BUTTON_WIDTH);
		this.testSearchGermplasm.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));

		this.testSearchGermplasmList = new Button(this.messageSource.getMessage(Message.HEAD_TO_HEAD_BROWSE_LIST));
		this.testSearchGermplasmList.setData(SpecifyGermplasmsComponent.SELECT_TEST_SEARCH_GERMPLASM_LIST_BUTTON_ID);
		this.testSearchGermplasmList.setWidth(SpecifyGermplasmsComponent.SEARCH_BUTTON_WIDTH);
		this.testSearchGermplasmList.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));

		absLayout.addComponent(this.testSearchGermplasmLabel, "top:13px;left:2px");
		absLayout.addComponent(this.testSearchGermplasmListLabel, "top:53px;left:2px");
		absLayout.addComponent(this.testSearchGermplasm, "top:10px;left:260px");
		absLayout.addComponent(this.testSearchGermplasmList, "top:50px;left:260px");
		this.testPanel.addComponent(absLayout);

		AbsoluteLayout absLayoutStandard = new AbsoluteLayout();
		absLayoutStandard.setWidth("450px");
		absLayoutStandard.setHeight("90px");

		this.standardSearchGermplasmLabel = new Label(this.messageSource.getMessage(Message.SPECIFY_SINGLE_STANDARD_ENTRY));
		this.standardSearchGermplasmLabel.setImmediate(true);

		this.standardSearchGermplasmListLabel = new Label(this.messageSource.getMessage(Message.SPECIFY_STANDARD_GERMPLASM_LIST_ENTRY));
		this.standardSearchGermplasmListLabel.setImmediate(true);

		this.standardSearchGermplasm = new Button(this.messageSource.getMessage(Message.HEAD_TO_HEAD_SEARCH_GERMPLASM));
		this.standardSearchGermplasm.setData(SpecifyGermplasmsComponent.SELECT_STANDARD_SEARCH_GERMPLASM_BUTTON_ID);
		this.standardSearchGermplasm.setWidth(SpecifyGermplasmsComponent.SEARCH_BUTTON_WIDTH);
		this.standardSearchGermplasm.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));

		this.standardSearchGermplasmList = new Button(this.messageSource.getMessage(Message.HEAD_TO_HEAD_BROWSE_LIST));
		this.standardSearchGermplasmList.setData(SpecifyGermplasmsComponent.SELECT_STANDARD_SEARCH_GERMPLASM_LIST_BUTTON_ID);
		this.standardSearchGermplasmList.setWidth(SpecifyGermplasmsComponent.SEARCH_BUTTON_WIDTH);
		this.standardSearchGermplasmList.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));

		absLayoutStandard.addComponent(this.standardSearchGermplasmLabel, "top:13px;left:2px");
		absLayoutStandard.addComponent(this.standardSearchGermplasmListLabel, "top:53px;left:2px");
		absLayoutStandard.addComponent(this.standardSearchGermplasm, "top:10px;left:290px");
		absLayoutStandard.addComponent(this.standardSearchGermplasmList, "top:50px;left:290px");
		this.standardPanel.addComponent(absLayoutStandard);

		this.headerLabel = new Label(this.messageSource.getMessage(Message.SELECT_TEST_STANDARD_COMPARE));
		this.headerLabel.setImmediate(true);

		this.addComponent(this.headerLabel, "top:10px;left:20px");
		this.addComponent(this.testPanel, "top:30px;left:20px");
		this.addComponent(this.standardPanel, "top:30px;left:470px");

		this.entriesTable = new Table();
		this.entriesTable.setWidth("920px");
		this.entriesTable.setHeight("330px");
		this.entriesTable.setImmediate(true);
		this.entriesTable.setPageLength(0);

		this.entriesTable.addContainerProperty(SpecifyGermplasmsComponent.TEST_ENTRY_COLUMN_ID, String.class, null);
		this.entriesTable.addContainerProperty(SpecifyGermplasmsComponent.STANDARD_ENTRY_COLUMN_ID, String.class, null);

		this.entriesTable.setColumnHeader(SpecifyGermplasmsComponent.TEST_ENTRY_COLUMN_ID, "Test Entry");
		this.entriesTable.setColumnHeader(SpecifyGermplasmsComponent.STANDARD_ENTRY_COLUMN_ID, "Standard Entry");

		this.entriesTable.setSelectable(true);
		this.entriesTable.setMultiSelect(true);
		this.entriesTable.setNullSelectionAllowed(false);

		this.entriesTable.addActionHandler(new Action.Handler() {

			private static final long serialVersionUID = 3972058734324800774L;

			@Override
			public Action[] getActions(Object target, Object sender) {
				return SpecifyGermplasmsComponent.ACTIONS_TABLE_CONTEXT_MENU;
			}

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (SpecifyGermplasmsComponent.ACTION_DELETE == action) {
					SpecifyGermplasmsComponent.this.deleteEntriesAction();
					SpecifyGermplasmsComponent.this.entriesTable.setPageLength(0);
				} else if (SpecifyGermplasmsComponent.ACTION_SELECT_ALL == action) {
					SpecifyGermplasmsComponent.this.entriesTable.setValue(SpecifyGermplasmsComponent.this.entriesTable.getItemIds());
					SpecifyGermplasmsComponent.this.entriesTable.setPageLength(0);
				}
			}
		});

		this.nextButton = new Button("Next");
		this.nextButton.setData(SpecifyGermplasmsComponent.NEXT_BUTTON_ID);
		this.nextButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
		this.nextButton.setEnabled(false);
		this.nextButton.setWidth("80px");
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addComponent(this.nextButton, "top:550px;left:460px");

		this.addComponent(this.entriesTable, "top:200px;left:20px");
	}

	private void deleteEntriesAction() {
		final Collection<?> selectedIds = (Collection<?>) this.entriesTable.getValue();
		if (!selectedIds.isEmpty()) {
			for (Object itemId : selectedIds) {
				this.entriesTable.removeItem(itemId);
				this.tableEntriesId.remove(itemId);
			}
		}

		if (this.isTableEntriesEmpty()) {
			// we set the new set since we already cleared it
			this.singleEntriesSet = new HashSet<String>();
			this.germplasmIdNameMap = new HashMap<String, String>();
		}

		if (this.isEitherTableEntriesEmpty()) {
			this.nextButton.setEnabled(false);
		} else {
			this.nextButton.setEnabled(true);
		}

	}

	public void selectTestEntryButtonClickAction() {
		Window parentWindow = this.getWindow();
		SelectGermplasmEntryDialog selectAGermplasmDialog = new SelectGermplasmEntryDialog(this, parentWindow, true);
		selectAGermplasmDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		parentWindow.addWindow(selectAGermplasmDialog);
	}

	public void selectStandardEntryButtonClickAction() {
		Window parentWindow = this.getWindow();
		SelectGermplasmEntryDialog selectAGermplasmDialog = new SelectGermplasmEntryDialog(this, parentWindow, false);
		selectAGermplasmDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		parentWindow.addWindow(selectAGermplasmDialog);
	}

	public void selectTestGermplasmListButtonClickAction() {
		Window parentWindow = this.getWindow();
		SelectGermplasmListDialog selectAGermplasmDialog = new SelectGermplasmListDialog(this, parentWindow, true);
		selectAGermplasmDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		parentWindow.addWindow(selectAGermplasmDialog);
	}

	public void selectStandardGermplasmListButtonClickAction() {
		Window parentWindow = this.getWindow();
		SelectGermplasmListDialog selectAGermplasmDialog = new SelectGermplasmListDialog(this, parentWindow, false);
		selectAGermplasmDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		parentWindow.addWindow(selectAGermplasmDialog);
	}

	public void nextButtonClickAction() {
		if (this.nextScreen != null) {
			this.nextScreen.populateTraitsAvailableTable(this.getGermplasmPairs(), this.germplasmIdNameMap);
			this.mainScreen.selectSecondTab();

		}
	}

	@SuppressWarnings("rawtypes")
	private List<GermplasmPair> getGermplasmPairs() {
		List<GermplasmPair> pairList = new ArrayList<GermplasmPair>();

		Iterator iter = this.entriesTable.getItemIds().iterator();
		while (iter.hasNext()) {
			GermplasmPair germplasmPair = new GermplasmPair();
			// we iterate and permutate against the list
			String id = (String) iter.next();
			String leftId = "";
			String rightId = "";
			StringTokenizer tokenizer = new StringTokenizer(id, ":");
			if (tokenizer.countTokens() == 2) {
				leftId = tokenizer.nextToken().trim();
				rightId = tokenizer.nextToken().trim();
			}

			germplasmPair.setGid1(Integer.valueOf(leftId));
			germplasmPair.setGid2(Integer.valueOf(rightId));
			pairList.add(germplasmPair);

		}

		return pairList;
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	private boolean permutationsWillExceedMax(Integer listSize) {
		if (this.entriesTable != null) {
			Integer tableSize = this.entriesTable.size();
			// if table empty, use 1 as divisor
			tableSize = tableSize == 0 ? 1 : tableSize;

			BigInteger selectedListSize = new BigInteger(listSize.toString());
			BigInteger currentTableSize = new BigInteger(Integer.valueOf(tableSize).toString());
			BigInteger maxAllowableSize = SpecifyGermplasmsComponent.MAX_NUM_OF_PAIRS.divide(currentTableSize);

			return maxAllowableSize.compareTo(selectedListSize) < 0;
		}

		return true;
	}

	public boolean addGermplasmList(Integer germplasmListId, Integer listSize, boolean isTestEntry) {
		// do not allow to select list if resulting pairs will exceed max
		if (!this.permutationsWillExceedMax(listSize)) {
			try {

				List<GermplasmListData> germplasmListData =
						this.germplasmListManager.getGermplasmListDataByListId(germplasmListId);

				if (germplasmListData != null && !germplasmListData.isEmpty()) {
					this.doGermplasmPermutationOnTable(isTestEntry, false, germplasmListData, null);
					return true;
				}

			} catch (Exception e) {
				SpecifyGermplasmsComponent.LOG.error(e.getMessage(), e);
			}

		} else {
			MessageNotifier.showWarning(this.getWindow(), "Warning", "The list selected will create too "
					+ "many germplasm pairs and may cause the tool to crash. Please select another list.");
		}

		return false;
	}

	public void addTestGermplasm(Germplasm germplasm) {
		if (germplasm != null) {
			this.doGermplasmPermutationOnTable(true, true, null, germplasm);
		}
	}

	public void addStandardGermplasm(Germplasm germplasm) {
		if (germplasm != null) {
			this.doGermplasmPermutationOnTable(false, true, null, germplasm);
		}
	}

	private void doGermplasmPermutationOnTable(boolean isTestEntry, boolean isGermplasm, List<GermplasmListData> germplasmListData,
			Germplasm germplasm) {

		Map<String, Map<String, String>> map = this.getBothMapEntries();
		Map<String, String> testMap = map.get(SpecifyGermplasmsComponent.TEST_ENTRY);
		Map<String, String> standardMap = map.get(SpecifyGermplasmsComponent.STANDARD_ENTRY);
		List<TablesEntries> tableEntriesList = new ArrayList<TablesEntries>();

		if (this.isTableEntriesEmpty()) {
			this.germplasmIdNameMap = new HashMap<String, String>();
		}

		// create a germplasm list with the germplasm as the sole list item
		if (isGermplasm) {
			germplasmListData = new ArrayList<GermplasmListData>();

			GermplasmListData germplasmData = new GermplasmListData();
			// GID and Designation are fields that will be checked/used
			germplasmData.setGid(germplasm.getGid());
			germplasmData.setDesignation(germplasm.getPreferredName().getNval());

			germplasmListData.add(germplasmData);
		}

		this.permutateGermplasmListToPartnerEntries(isTestEntry, testMap, standardMap, tableEntriesList, germplasmListData);

		this.addToTable(tableEntriesList);

		if (this.isEitherTableEntriesEmpty()) {
			this.nextButton.setEnabled(false);
		} else {
			this.nextButton.setEnabled(true);
		}

	}

	private void permutateGermplasmListToPartnerEntries(boolean isTestEntry, Map<String, String> testMap, Map<String, String> standardMap,
			List<TablesEntries> tableEntriesList, List<GermplasmListData> germplasmListData) {

		Map<String, String> ownMap = testMap;
		Map<String, String> partnerMap = standardMap;

		if (!isTestEntry) {
			ownMap = standardMap;
			partnerMap = testMap;
		}

		if (ownMap.keySet().isEmpty() && !partnerMap.keySet().isEmpty()) {
			// we need to remove all
			this.deleteAllSingleEntriesInTableListData(isTestEntry, germplasmListData, testMap.keySet().size(), standardMap.keySet().size());
		}

		if (partnerMap.keySet().isEmpty()) {
			// just add on one side
			for (GermplasmListData listData : germplasmListData) {
				String gid = listData.getGid().toString();
				String germplasmName = listData.getDesignation() != null ? listData.getDesignation() : gid;

				String testEntryName = germplasmName;
				String standardEntryName = "";
				String newId = gid + ": ";
				if (!isTestEntry) {
					standardEntryName = germplasmName;
					testEntryName = "";
					newId = " :" + gid;
				}

				TablesEntries entry = this.createTableEntries(testEntryName, standardEntryName, newId);
				tableEntriesList.add(entry);
				this.singleEntriesSet.add(newId);
				this.germplasmIdNameMap.put(gid, germplasmName);

			}

		} else {
			// we iterate
			for (GermplasmListData listData : germplasmListData) {
				Iterator<String> partnerIterator = partnerMap.keySet().iterator();
				String gid = listData.getGid().toString();
				String germplasmName = listData.getDesignation() != null ? listData.getDesignation() : gid;

				while (partnerIterator.hasNext()) {
					String partnerId = partnerIterator.next();
					String partnerName = partnerMap.get(partnerId);

					String testEntryName = germplasmName;
					String standardEntryName = partnerName;
					String newId = gid + ":" + partnerId;

					if (!isTestEntry) {
						testEntryName = partnerName;
						standardEntryName = germplasmName;
						newId = partnerId + ":" + gid;
					}

					if (!gid.equalsIgnoreCase(partnerId)) {
						TablesEntries entry = this.createTableEntries(testEntryName, standardEntryName, newId);
						tableEntriesList.add(entry);
					}
					this.germplasmIdNameMap.put(partnerId, partnerName);
				}

				this.germplasmIdNameMap.put(gid, germplasmName);
			}

		}
	}

	private TablesEntries createTableEntries(String testName, String standardName, String id) {
		TablesEntries tableEntry = new TablesEntries();
		tableEntry.setTestEntryName(testName);
		tableEntry.setStandardEntryName(standardName);
		tableEntry.setTestStandardEntry(id);
		return tableEntry;
	}

	private void deleteAllSingleEntriesInTableListData(boolean isTestEntry, List<GermplasmListData> germplasmListData, int leftSize,
			int rightSize) {
		// we delete the single entrie
		for (String idToDelete : this.singleEntriesSet) {
			for (GermplasmListData listData : germplasmListData) {

				String tempId = "";
				if (isTestEntry) {
					tempId = " :" + listData.getGid().toString();
				} else {
					tempId = listData.getGid().toString() + ": ";
				}

				if (idToDelete.equalsIgnoreCase(tempId)) {
					if (isTestEntry) {
						if (rightSize != 1) {
							this.entriesTable.removeItem(idToDelete);
							this.tableEntriesId.remove(idToDelete);
						}
					} else {
						if (leftSize != 1) {
							this.entriesTable.removeItem(idToDelete);
							this.tableEntriesId.remove(idToDelete);
						}
					}
				}

				if (!idToDelete.equalsIgnoreCase(tempId)) {
					this.entriesTable.removeItem(idToDelete);
					this.tableEntriesId.remove(idToDelete);
				}
			}
		}

		this.entriesTable.setPageLength(0);

		// we set the new set since we already cleared it
		if (this.isTableEntriesEmpty()) {
			this.singleEntriesSet = new HashSet<String>();
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Map<String, String>> getBothMapEntries() {
		Map<String, String> testMap = new HashMap<String, String>();
		Map<String, String> standardMap = new HashMap<String, String>();
		Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
		Iterator iter = this.entriesTable.getItemIds().iterator();
		while (iter.hasNext()) {
			String id = (String) iter.next();
			StringTokenizer tokenizer = new StringTokenizer(id, ":");
			String leftId = "";
			String rightId = "";
			Item item = this.entriesTable.getItem(id);

			if (tokenizer.countTokens() == 2) {
				leftId = tokenizer.nextToken().trim();
				rightId = tokenizer.nextToken().trim();
			}
			String testEntryName = (String) item.getItemProperty(SpecifyGermplasmsComponent.TEST_ENTRY_COLUMN_ID).getValue();
			String standardEntryName = (String) item.getItemProperty(SpecifyGermplasmsComponent.STANDARD_ENTRY_COLUMN_ID).getValue();

			if (leftId != null && !"".equalsIgnoreCase(leftId)) {
				testMap.put(leftId, testEntryName);
			}
			if (rightId != null && !"".equalsIgnoreCase(rightId)) {
				standardMap.put(rightId, standardEntryName);
			}
		}
		resultMap.put(SpecifyGermplasmsComponent.TEST_ENTRY, testMap);
		resultMap.put(SpecifyGermplasmsComponent.STANDARD_ENTRY, standardMap);
		return resultMap;
	}

	private void addToTable(List<TablesEntries> tableEntryList) {
		for (TablesEntries tableEntry : tableEntryList) {
			String newId = tableEntry.getTestStandardEntry();
			// if not in map, add it in the table

			if (!this.tableEntriesId.contains(newId)) {
				this.entriesTable.addItem(new Object[] {tableEntry.getTestEntryName(), tableEntry.getStandardEntryName()},
						tableEntry.getTestStandardEntry());
				this.tableEntriesId.add(newId);
			}

			this.entriesTable.setPageLength(0);
		}
	}

	private boolean isTableEntriesEmpty() {
		Map<String, Map<String, String>> map = this.getBothMapEntries();
		Map<String, String> testMap = map.get(SpecifyGermplasmsComponent.TEST_ENTRY);
		Map<String, String> standardMap = map.get(SpecifyGermplasmsComponent.STANDARD_ENTRY);
		if (testMap.keySet().isEmpty() && standardMap.keySet().isEmpty()) {
			return true;
		}
		return false;
	}

	private boolean isEitherTableEntriesEmpty() {
		Map<String, Map<String, String>> map = this.getBothMapEntries();
		Map<String, String> testMap = map.get(SpecifyGermplasmsComponent.TEST_ENTRY);
		Map<String, String> standardMap = map.get(SpecifyGermplasmsComponent.STANDARD_ENTRY);
		if (testMap.keySet().isEmpty() || standardMap.keySet().isEmpty()) {
			return true;
		}
		return false;
	}
}
