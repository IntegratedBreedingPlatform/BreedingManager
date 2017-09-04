
package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.breeding.manager.data.initializer.GermplasmListDataTestDataInitializer;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class ListComponentAddEntryDialogSourceTest {

	private static final String[] STANDARD_COLUMNS =
			{ColumnLabels.GID.getName(), ColumnLabels.DESIGNATION.getName(), ColumnLabels.SEED_SOURCE.getName(),
					ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.GROUP_ID.getName(), ColumnLabels.STOCKID.getName()};

	private static final int GERMPLASM_LIST_ID = 25;

	private static final int GID = 100;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private InventoryDataManager inventoryDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@Mock
	private NewGermplasmEntriesFillColumnSource newEntriesSource;

	@Mock
	private AddedColumnsMapper addedColumnsMapper;

	@Mock
	private ListComponent listComponent;

	@Mock
	private Table table;

	@InjectMocks
	private ListComponentAddEntryDialogSource addEntrySource;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.addEntrySource.setGermplasmDataManager(this.germplasmDataManager);
		this.addEntrySource.setGermplasmListManager(this.germplasmListManager);
		this.addEntrySource.setPedigreeService(this.pedigreeService);
		this.addEntrySource.setInventoryDataManager(this.inventoryDataManager);
		this.addEntrySource.setNewEntriesSource(this.newEntriesSource);
		this.addEntrySource.setAddedColumnsMapper(this.addedColumnsMapper);

		Mockito.doReturn(GermplasmTestDataInitializer.createGermplasm(ListComponentAddEntryDialogSourceTest.GID))
				.when(this.germplasmDataManager).getGermplasmWithPrefName(Matchers.eq(ListComponentAddEntryDialogSourceTest.GID));
		final GermplasmList germplasmList =
				GermplasmListTestDataInitializer.createGermplasmList(ListComponentAddEntryDialogSourceTest.GERMPLASM_LIST_ID, true);
		Mockito.doReturn(germplasmList).when(this.listComponent).getGermplasmList();
		Mockito.doReturn(Arrays.asList(GermplasmListDataTestDataInitializer.getGermplasmListData(germplasmList,
				ListComponentAddEntryDialogSourceTest.GERMPLASM_LIST_ID, ListComponentAddEntryDialogSourceTest.GID, 4)))
				.when(this.inventoryDataManager).getLotCountsForListEntries(
						Matchers.eq(ListComponentAddEntryDialogSourceTest.GERMPLASM_LIST_ID), Matchers.anyListOf(Integer.class));
		Mockito.doReturn(ListComponentAddEntryDialogSourceTest.STANDARD_COLUMNS).when(this.table).getVisibleColumns();
	}

	@Test
	public void testFinishAddingEntry() {
		this.addEntrySource.finishAddingEntry(ListComponentAddEntryDialogSourceTest.GID, false);

		Mockito.verify(this.germplasmListManager).addGermplasmListData(Matchers.any(GermplasmListData.class));
		Mockito.verify(this.listComponent).addListEntryToTable(Matchers.any(GermplasmListData.class));
		Mockito.verifyZeroInteractions(this.newEntriesSource);
		Mockito.verifyZeroInteractions(this.addedColumnsMapper);
		Mockito.verify(this.listComponent).saveChangesAction(this.listComponent.getWindow(), false);
		Mockito.verify(this.table).refreshRowCache();
		Mockito.verify(this.table).setImmediate(true);
		Mockito.verify(this.table).setEditable(true);
	}

	@Test
	public void testFinishAddingEntryWhenThereAreAddedColumns() {
		final List<String> columns = new ArrayList<>(Arrays.asList(ListComponentAddEntryDialogSourceTest.STANDARD_COLUMNS));
		columns.add(ColumnLabels.PREFERRED_NAME.getName());
		columns.add(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		columns.add(ColumnLabels.CROSS_MALE_GID.getName());
		Mockito.doReturn(columns.toArray()).when(this.table).getVisibleColumns();
		this.addEntrySource.finishAddingEntry(ListComponentAddEntryDialogSourceTest.GID, false);

		Mockito.verify(this.germplasmListManager).addGermplasmListData(Matchers.any(GermplasmListData.class));
		Mockito.verify(this.listComponent).addListEntryToTable(Matchers.any(GermplasmListData.class));
		Mockito.verify(this.newEntriesSource).setAddedItemIds(Matchers.anyListOf(Integer.class));
		Mockito.verify(this.newEntriesSource).setAddedGids(Matchers.anyListOf(Integer.class));
		Mockito.verify(this.addedColumnsMapper).generateValuesForAddedColumns(columns.toArray(), false);
		;
		Mockito.verify(this.listComponent).saveChangesAction(this.listComponent.getWindow(), false);
		Mockito.verify(this.table).refreshRowCache();
		Mockito.verify(this.table).setImmediate(true);
		Mockito.verify(this.table).setEditable(true);
	}

}
