
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.PagedTableWithSelectAllLayout;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.breeding.manager.service.BreedingManagerSearchException;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Container.Indexed;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

public class GermplasmSearchResultsComponentTest {

	private static final String GERMPLASM_NAMES_WITH_MORE_THAN_20_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final String GERMPLASM_NAMES_WITH_20_CHARS = "ABCDEFGHIJKLMNOPQRST";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	@Mock
	private PagedTableWithSelectAllLayout tableWithSelectAllLayout;
	
	@Mock
	private PagedBreedingManagerTable pagedTable;
	
	@Mock
	private Indexed dataSource;
	
	@Captor
	private ArgumentCaptor<Object[]> captor;

	@InjectMocks
	private GermplasmSearchResultsComponent germplasmSearchResultsComponent;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		// menu item
		Mockito.doReturn("MENU").when(this.messageSource).getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST);
		Mockito.doReturn("SELECT ALL").when(this.messageSource).getMessage(Message.SELECT_ALL);
		Mockito.doReturn("DUMMY MESSAGE").when(this.messageSource).getMessage("VALIDATION_INTEGER_FORMAT");
		
		Mockito.doReturn(this.pagedTable).when(this.tableWithSelectAllLayout).getTable();
		Mockito.doReturn(this.dataSource).when(this.pagedTable).getContainerDataSource();
	}

	@Test
	public void testInitMatchingGermplasmTableHeaderNameExistsFromOntology() throws MiddlewareQueryException {
		final PagedBreedingManagerTable actualTable = new PagedBreedingManagerTable(1, 20);
		Mockito.doReturn(actualTable).when(this.tableWithSelectAllLayout).getTable();
		
		Mockito.doReturn(this.createTerm("PARENTAGE")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		Mockito.doReturn(this.createTerm("GID")).when(this.ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		Mockito.doReturn(this.createTerm("STOCKID")).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(this.createTerm("LOCATIONS")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.GERMPLASM_LOCATION.getTermId().getId());
		Mockito.doReturn(this.createTerm("METHOD_NAME")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.BREEDING_METHOD_NAME.getTermId().getId());
		Mockito.doReturn(this.createTerm("AVAILABLE")).when(this.ontologyDataManager)
				.getTermById(ColumnLabels.TOTAL.getTermId().getId());

		this.germplasmSearchResultsComponent.initMatchingGermplasmTable();

		final Table table = this.germplasmSearchResultsComponent.getMatchingGermplasmsTableWithSelectAll().getTable();

		Assert.assertEquals(10, table.getColumnHeaders().length);

		Assert.assertEquals("Tag All Column", table.getColumnHeader(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID));
		Assert.assertEquals("NAMES", table.getColumnHeader(GermplasmSearchResultsComponent.NAMES));
		Assert.assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals("STOCKID", table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals("LOCATIONS", table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertEquals("METHOD_NAME", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		Assert.assertEquals("AVAILABLE", table.getColumnHeader(ColumnLabels.TOTAL.getName()));

	}

	@Test
	public void testInitMatchingGermplasmTableHeaderNameDoesntExistFromOntology() throws MiddlewareQueryException {
		final PagedBreedingManagerTable actualTable = new PagedBreedingManagerTable(1, 20);
		Mockito.doReturn(actualTable).when(this.tableWithSelectAllLayout).getTable();
		
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_RESERVATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.GERMPLASM_LOCATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.BREEDING_METHOD_NAME.getTermId().getId());

		this.germplasmSearchResultsComponent.initMatchingGermplasmTable();

		final Table table = this.germplasmSearchResultsComponent.getMatchingGermplasmsTableWithSelectAll().getTable();

		Assert.assertEquals("Tag All Column", table.getColumnHeader(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID));
		Assert.assertEquals("NAMES", table.getColumnHeader(GermplasmSearchResultsComponent.NAMES));
		Assert.assertEquals(ColumnLabels.PARENTAGE.getName(), table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		Assert.assertEquals(ColumnLabels.AVAILABLE_INVENTORY.getName(), table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		Assert.assertEquals(ColumnLabels.SEED_RESERVATION.getName(), table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		Assert.assertEquals(ColumnLabels.STOCKID.getName(), table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		Assert.assertEquals(ColumnLabels.GID.getName(), table.getColumnHeader(ColumnLabels.GID.getName()));
		Assert.assertEquals(ColumnLabels.GERMPLASM_LOCATION.getName(), table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		Assert.assertEquals(ColumnLabels.BREEDING_METHOD_NAME.getName(), table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));

	}

	@Test
	public void testGetShortenedNamesIfNameLengthIsAtLeast20() {
		final String shortenedNames = this.germplasmSearchResultsComponent.getShortenedNames(GERMPLASM_NAMES_WITH_MORE_THAN_20_CHARS);

		Assert.assertEquals("Expecting to return string with only 20 characters with ellipsis(...) at the end.",
				GERMPLASM_NAMES_WITH_MORE_THAN_20_CHARS.substring(0, 20).concat("..."), shortenedNames);
	}

	@Test
	public void testGetShortenedNamesIfNameLengthIsAtMost20() {
		final String shortenedNames = this.germplasmSearchResultsComponent.getShortenedNames(GERMPLASM_NAMES_WITH_20_CHARS);
		Assert.assertEquals("Expecting to return the same name.", GERMPLASM_NAMES_WITH_20_CHARS, shortenedNames);
	}
	
	@Test
	public void testApplyGermplasmResultsNoMatch() {
		// instantiate other needed components in the page
		createDummyComponents();
		
		GermplasmSearchParameter searchParameter = new GermplasmSearchParameter("ABC", Operation.EQUAL);
		try {
			this.germplasmSearchResultsComponent.applyGermplasmResults(searchParameter);
			Assert.fail("Expecting BreedingManagerSearchException to be thrown for empty search but wasn't.");
			
		} catch (BreedingManagerSearchException e) {
			Assert.assertEquals(Message.NO_SEARCH_RESULTS, e.getErrorMessage());
			// Verify key actions done, prior to throwing exception
			Mockito.verify(this.pagedTable, Mockito.times(1)).setCurrentPage(1);
			
			// Setting of visible columns set twice
			// 1) during table initialization and 2) in applyGermplasmResults method
			Mockito.verify(this.pagedTable, Mockito.times(2)).setVisibleColumns(captor.capture());
			
			// Check that GID_REF property is not visible
			Assert.assertEquals(10, captor.getValue().length);
			for (Object property : captor.getValue()){
				final String gidRefColumn = ColumnLabels.GID.getName() + "_REF";
				if (gidRefColumn.equals(property)){
					Assert.fail("Expecting GID_REF column to be hidden on table but was not.");
				}
			}
			
			// Check that Select All checkboxes states were updated
			Mockito.verify(this.tableWithSelectAllLayout, Mockito.times(1)).updateSelectAllCheckboxesCaption();
		}
	}

	private void createDummyComponents() {
		this.germplasmSearchResultsComponent.initMatchingGermplasmTable();
		final Label totalEntriesLabel = new Label();
		this.germplasmSearchResultsComponent.setTotalEntriesLabel(totalEntriesLabel);
		final Label selectedEntriesLabel = new Label();
		this.germplasmSearchResultsComponent.setTotalSelectedEntriesLabel(selectedEntriesLabel);
	}
	
	

	private Term createTerm(final String name) {
		final Term term = new Term();
		term.setName(name);
		term.setId(0);
		return term;
	}

}
