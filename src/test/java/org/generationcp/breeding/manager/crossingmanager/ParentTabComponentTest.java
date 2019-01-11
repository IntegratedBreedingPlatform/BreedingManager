package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.action.SaveGermplasmListAction;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.data.initializer.GermplasmListDataTestDataInitializer;
import org.generationcp.breeding.manager.data.initializer.GermplasmListEntryTestDataInitializer;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.data.Item;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

@RunWith(MockitoJUnitRunner.class)
public class ParentTabComponentTest {

	private static final int GERMPLASM_LIST_ID = 1;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ManageCrossingSettingsMain makeCrossesSettingsMain;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private Component parent;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private CrossingManagerListTreeComponent crossingManagerListTreeComponent;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	private ParentTabComponent parentTabComponent;
	private CrossingManagerMakeCrossesComponent makeCrossesMain;

	@Before
	public void setUp() {
		ManagerFactory.getCurrentManagerFactoryThreadLocal().set(Mockito.mock(ManagerFactory.class));

		final Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.doReturn(fromOntology).when(this.ontologyDataManager).getTermById(Matchers.anyInt());
		this.makeCrossesMain = new CrossingManagerMakeCrossesComponent(this.makeCrossesSettingsMain);
		this.makeCrossesMain.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);

		final SelectParentsComponent selectParentsComponent = new SelectParentsComponent(this.makeCrossesMain);
		selectParentsComponent.setMessageSource(this.messageSource);
		selectParentsComponent.instantiateComponents();
		selectParentsComponent.setListTreeComponent(this.crossingManagerListTreeComponent);

		this.makeCrossesMain.instantiateComponents();

		final MakeCrossesParentsComponent source = Mockito.spy(new MakeCrossesParentsComponent(this.makeCrossesMain));
		final String parentLabel = "Female Parents";
		final Integer rowCount = 10;

		this.makeCrossesMain.setParentsComponent(source);
		final MakeCrossesTableComponent crossesTableComponent = new MakeCrossesTableComponent(this.makeCrossesMain);
		crossesTableComponent.setMessageSource(this.messageSource);
		crossesTableComponent.setOntologyDataManager(this.ontologyDataManager);
		this.makeCrossesMain.setCrossesTableComponent(crossesTableComponent);
		this.makeCrossesMain.setSelectParentsComponent(selectParentsComponent);

		this.parentTabComponent = new ParentTabComponent(this.makeCrossesMain, source, parentLabel, rowCount);
		source.setMaleParentTab(this.parentTabComponent);
		source.setFemaleParentTab(this.parentTabComponent);
		Mockito.doReturn("TestString").when(this.messageSource).getMessage(Matchers.any(Message.class));
		this.parentTabComponent.setMessageSource(this.messageSource);
		this.parentTabComponent.setOntologyDataManager(this.ontologyDataManager);
		this.parentTabComponent.setInventoryDataManager(this.inventoryDataManager);
		this.parentTabComponent.setParent(this.parent);

		final SaveGermplasmListAction saveGermplasmListAction =
				new SaveGermplasmListAction(this.parentTabComponent,
						GermplasmListTestDataInitializer.createGermplasmList(ParentTabComponentTest.GERMPLASM_LIST_ID),
						GermplasmListEntryTestDataInitializer.getGermplasmListEntries());
		saveGermplasmListAction.setContextUtil(this.contextUtil);
		saveGermplasmListAction.setGermplasmListManager(this.germplasmListManager);
		saveGermplasmListAction.setPedigreeService(this.pedigreeService);
		saveGermplasmListAction.setInventoryDataManager(this.inventoryDataManager);

		final User user = new User();
		user.setUserid(12);
		user.setPersonid(123);

	}

	@Test
	public void testInitializeParentTable_returnsTheValueFromColumLabelDefaultName() {
		final Integer rowCount = 10;
		this.parentTabComponent.setRowCount(rowCount);

		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		final Term fromOntologyDesignation = new Term();
		fromOntologyDesignation.setName("DESIGNATION");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntologyDesignation);
		final Term fromOntologySeedReservation = new Term();
		fromOntologySeedReservation.setName("CROSS");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntologySeedReservation);
		final Term fromOntologyStockId = new Term();
		fromOntologyStockId.setName("STOCKID");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntologyStockId);

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.parentTabComponent.initializeParentTable(tableWithSelectAll);

		final Table table = tableWithSelectAll.getTable();

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("CROSS", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
	}

	@Test
	public void testInitializeParentTable_returnsTheValueFromOntologyManager() throws MiddlewareQueryException {
		final Integer rowCount = 10;
		this.parentTabComponent.setRowCount(rowCount);

		final Term fromOntologyDesignation = new Term();
		fromOntologyDesignation.setName("DESIGNATION");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntologyDesignation);
		final Term fromOntologySeedReservation = new Term();
		fromOntologySeedReservation.setName("CROSS");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntologySeedReservation);
		final Term fromOntologyStockId = new Term();
		fromOntologyStockId.setName("STOCKID");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(fromOntologyStockId);

		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn("TAG");
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn("HASHTAG");

		final TableWithSelectAllLayout tableWithSelectAll = new TableWithSelectAllLayout(ColumnLabels.TAG.getName());
		tableWithSelectAll.instantiateComponents();

		this.parentTabComponent.initializeParentTable(tableWithSelectAll);

		final Table table = tableWithSelectAll.getTable();

		Assert.assertEquals("TAG", table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals("HASHTAG", table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals("DESIGNATION", table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals("CROSS", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateListDataTableWithPreservedSelectedEntriesAndCheckedSelectAll() {
		final TableWithSelectAllLayout tableWithSelectAll = this.initializeTable();
		// setup data
		final List<GermplasmListData> savedListEntries =
				GermplasmListDataTestDataInitializer.getGermplasmListDataList(ParentTabComponentTest.GERMPLASM_LIST_ID);
		final Collection<GermplasmListEntry> listEntries = this.createListEntries(savedListEntries);
		final List<Integer> selectedEntryIds =
				this.selectEntryIdsFromListEntries(listEntries, GermplasmListDataTestDataInitializer.NUM_OF_ENTRIES);
		final Table table = tableWithSelectAll.getTable();
		this.addEntriesToTable(listEntries, table, selectedEntryIds);
		// test
		this.parentTabComponent.updateListDataTable(ParentTabComponentTest.GERMPLASM_LIST_ID, savedListEntries);

		final Collection<GermplasmListEntry> newSelectedListEntries = (Collection<GermplasmListEntry>) table.getValue();
		Assert.assertEquals("The selected entries should be preserved", selectedEntryIds.size(), newSelectedListEntries.size());
		Assert.assertTrue("The select all should be selected", tableWithSelectAll.getCheckBox().booleanValue());
	}

	private TableWithSelectAllLayout initializeTable() {
		final TableWithSelectAllLayout tableWithSelectAll =
				new TableWithSelectAllLayout(GermplasmListDataTestDataInitializer.NUM_OF_ENTRIES, ParentTabComponent.TAG_COLUMN_ID);
		tableWithSelectAll.instantiateComponents();
		tableWithSelectAll.addListeners();
		this.parentTabComponent.initializeMainComponents();
		this.parentTabComponent.initializeParentTable(tableWithSelectAll);
		this.parentTabComponent.addListeners();
		return tableWithSelectAll;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateListDataTableWithPreservedSelectedEntriesAndUncheckedSelectAll() {
		final TableWithSelectAllLayout tableWithSelectAll = this.initializeTable();
		// setup data
		final List<GermplasmListData> savedListEntries =
				GermplasmListDataTestDataInitializer.getGermplasmListDataList(ParentTabComponentTest.GERMPLASM_LIST_ID);
		final Collection<GermplasmListEntry> listEntries = this.createListEntries(savedListEntries);
		final List<Integer> selectedEntryIds = this.selectEntryIdsFromListEntries(listEntries, 1);
		final Table table = tableWithSelectAll.getTable();
		this.addEntriesToTable(listEntries, table, selectedEntryIds);
		// test
		this.parentTabComponent.updateListDataTable(ParentTabComponentTest.GERMPLASM_LIST_ID, savedListEntries);

		final Collection<GermplasmListEntry> newSelectedListEntries = (Collection<GermplasmListEntry>) table.getValue();
		Assert.assertEquals("The selected entries should be preserved", selectedEntryIds.size(), newSelectedListEntries.size());
		Assert.assertFalse("The select all should not be selected", tableWithSelectAll.getCheckBox().booleanValue());
	}

	private List<Integer> selectEntryIdsFromListEntries(final Collection<GermplasmListEntry> listEntries, final int numberOfSelectedItems) {
		final List<Integer> selectedEntryIds = new ArrayList<>();
		int counterOfSelectedItems = 0;
		for (final GermplasmListEntry germplasmListEntry : listEntries) {
			if (counterOfSelectedItems < numberOfSelectedItems) {
				selectedEntryIds.add(germplasmListEntry.getEntryId());
			}
			counterOfSelectedItems++;
		}
		return selectedEntryIds;
	}

	private void addEntriesToTable(final Collection<GermplasmListEntry> selectedListEntries, final Table table,
			final List<Integer> selectedEntryIds) {
		for (final GermplasmListEntry germplasmListEntry : selectedListEntries) {
			final Item newItem = table.getContainerDataSource().addItem(germplasmListEntry);

			final CheckBox tag = new CheckBox();
			newItem.getItemProperty(ParentTabComponent.TAG_COLUMN_ID).setValue(tag);

			if (selectedEntryIds.contains(germplasmListEntry.getEntryId())) {
				table.select(germplasmListEntry);
			}
		}
	}

	private List<GermplasmListEntry> createListEntries(final List<GermplasmListData> savedListEntries) {
		final List<GermplasmListEntry> listEntries = new ArrayList<>();
		int listDataId = 100;
		for (final GermplasmListData germplasmListData : savedListEntries) {
			final GermplasmListEntry entry =
					new GermplasmListEntry(listDataId++, germplasmListData.getGid(), germplasmListData.getEntryId(),
							germplasmListData.getDesignation(), germplasmListData.getSeedSource());
			listEntries.add(entry);
		}
		return listEntries;
	}
}
