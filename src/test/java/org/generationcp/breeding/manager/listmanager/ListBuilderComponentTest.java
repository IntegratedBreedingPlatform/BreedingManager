
package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.Collection;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.data.Container;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class ListBuilderComponentTest {

	private static final String SEED_RES = "SEED_RES";
	private static final String AVAIL_INV = "AVAIL_INV";
	private static final String HASH = "#";
	private static final String CHECK = "CHECK";
	private static final String SEED_SOURCE = "SEED_SOURCE";
	private static final String CROSS = "CROSS";
	private static final String DESIG = "DESIG";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	private static final String GID = "GID";
	private static final String STOCKID = "STOCKID";
	private static String DELETE_GERMPLASM_ENTRIES = "Delete Germplasm Entries";
	private static String DELETE_SELECTED_ENTRIES_CONFIRM = "Delete selected germplasm entries?";
	private static String YES = "YES";
	private static String NO = "NO";

	@Mock
	private OntologyDataManager ontologyDataManager;
	
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	@Mock
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	
	@InjectMocks
	private ListBuilderComponent listBuilderComponent;

	@Before
	public void setUp() {
		this.listBuilderComponent.setOntologyDataManager(this.ontologyDataManager);
		this.listBuilderComponent.setMessageSource(this.messageSource);
		
		Mockito.when(this.messageSource.getMessage(Message.CHECK_ICON)).thenReturn(ListBuilderComponentTest.CHECK);
		Mockito.when(this.messageSource.getMessage(Message.HASHTAG)).thenReturn(ListBuilderComponentTest.HASH);
		Mockito.when(this.messageSource.getMessage(Message.DELETE_GERMPLASM_ENTRIES)).thenReturn(ListBuilderComponentTest.DELETE_GERMPLASM_ENTRIES);
		Mockito.when(this.messageSource.getMessage(Message.DELETE_SELECTED_ENTRIES_CONFIRM)).thenReturn(ListBuilderComponentTest.DELETE_SELECTED_ENTRIES_CONFIRM);
		Mockito.when(this.messageSource.getMessage(Message.YES)).thenReturn(ListBuilderComponentTest.YES);
		Mockito.when(this.messageSource.getMessage(Message.NO)).thenReturn(ListBuilderComponentTest.NO);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AVAILABLE_INVENTORY.getId())).thenReturn(new Term(1, ListBuilderComponentTest.AVAIL_INV, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_RESERVATION.getId())).thenReturn(new Term(1, ListBuilderComponentTest.SEED_RES, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(new Term(1, ListBuilderComponentTest.GID, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(new Term(1, ListBuilderComponentTest.ENTRY_CODE, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(new Term(1, ListBuilderComponentTest.DESIG, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(new Term(1, ListBuilderComponentTest.CROSS, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(new Term(1, ListBuilderComponentTest.SEED_SOURCE, ""));
		Mockito.when(this.ontologyDataManager.getTermById(TermId.STOCKID.getId())).thenReturn(new Term(1, ListBuilderComponentTest.STOCKID, ""));
	}

	@Test
	public void testAddBasicTableColumns() {

		Table table = new Table();
		this.listBuilderComponent.addBasicTableColumns(table);

		Assert.assertEquals(ListBuilderComponentTest.CHECK, table.getColumnHeader(ColumnLabels.TAG.getName()));
		Assert.assertEquals(ListBuilderComponentTest.HASH, table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.AVAIL_INV, table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ListBuilderComponentTest.SEED_RES, table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals(ListBuilderComponentTest.GID, table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ListBuilderComponentTest.ENTRY_CODE, table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.DESIG, table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		Assert.assertEquals(ListBuilderComponentTest.CROSS, table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.SEED_SOURCE, table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		Assert.assertEquals(ListBuilderComponentTest.STOCKID, table.getColumnHeader(ColumnLabels.STOCKID.getName()));

	}
	
	@Test
	public void testDeleteSelectedEntriesWithNoSelectedEntries(){
		ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getWindow()).thenReturn(new Window());
		this.listBuilderComponent.setSource(source);
		
		BreedingManagerTable breedingManagerTable = Mockito.mock(BreedingManagerTable.class);
		Mockito.when(this.tableWithSelectAllLayout.getTable()).thenReturn(breedingManagerTable);
		Collection<? extends Integer> selectedItems =  Arrays.asList(new Integer[]{});
		Mockito.when(breedingManagerTable.getValue()).thenReturn(selectedItems);
		
		this.listBuilderComponent.setTableWithSelectAllLayout(tableWithSelectAllLayout);
		
		this.listBuilderComponent.deleteSelectedEntries();
		try{
			Mockito.verify(source, Mockito.times(1)).getWindow();
		} catch (WantedButNotInvoked e) {
			Assert.fail("Expecting to show 'no selected germplasm entry' error but didn't.");
		}
	}
	
	@Test
	public void testDeleteSelectedEntriesWithSelectedEntries(){
		BreedingManagerTable breedingManagerTable = Mockito.mock(BreedingManagerTable.class);
		Mockito.when(this.tableWithSelectAllLayout.getTable()).thenReturn(breedingManagerTable);
		
		Container container = Mockito.mock(Container.class);
		Mockito.when(breedingManagerTable.getContainerDataSource()).thenReturn(container);
		
		Collection<? extends Integer> selectedItems =  Arrays.asList(new Integer[]{1});
		Mockito.when(breedingManagerTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setTableWithSelectAllLayout(tableWithSelectAllLayout);
		
		Table listDataTable = Mockito.mock(Table.class);
		listDataTable.addItem(1);
		listDataTable.addItem(2);
		Mockito.when(listDataTable.getValue()).thenReturn(selectedItems);
		this.listBuilderComponent.setListDataTable(listDataTable);
		
		ListManagerMain source = Mockito.mock(ListManagerMain.class);
		Mockito.when(source.getModeView()).thenReturn(ModeView.LIST_VIEW);
		this.listBuilderComponent.setSource(source);
		
		this.listBuilderComponent.setTotalListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.setTotalSelectedListEntriesLabel(Mockito.mock(Label.class));
		this.listBuilderComponent.doDeleteSelectedEntries();
		
		try{
			Mockito.verify(container, Mockito.times(1)).removeItem(Matchers.any());
		} catch (WantedButNotInvoked e) {
			Assert.fail("Expecting to show 'no selected germplasm entry' error but didn't.");
		}
	}
}
