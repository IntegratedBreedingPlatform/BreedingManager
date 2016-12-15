
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.PagedTableWithSelectAllLayout;
import org.generationcp.breeding.manager.customfields.PagedBreedingManagerTable;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.vaadin.ui.Table;

public class GermplasmSearchResultsComponentTest {

	private static final String GERMPLASM_NAMES_WITH_MORE_THAN_20_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final String GERMPLASM_NAMES_WITH_20_CHARS = "ABCDEFGHIJKLMNOPQRST";

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	@Spy
	private GermplasmSearchResultsComponent germplasmSearchResultsComponent;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		// menu item
		Mockito.doReturn("MENU").when(this.messageSource).getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST);
		Mockito.doReturn("SELECT ALL").when(this.messageSource).getMessage(Message.SELECT_ALL);

		Mockito.doReturn("DUMMY MESSAGE").when(this.messageSource).getMessage("VALIDATION_INTEGER_FORMAT");
		final PagedBreedingManagerTable pagedTable = new PagedBreedingManagerTable(1, 50);

		final PagedTableWithSelectAllLayout table =
				new PagedTableWithSelectAllLayout(10, GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID);
		table.setTable(pagedTable);

		Mockito.doReturn(table).when(this.germplasmSearchResultsComponent).getTableWithSelectAllLayout();

	}

	@Test
	public void testInstantiateComponentsHeaderNameExistsFromOntology() throws MiddlewareQueryException {

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

		this.germplasmSearchResultsComponent.instantiateComponents();

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
	public void testInstantiateComponentsHeaderNameDoesntExistFromOntology() throws MiddlewareQueryException {

		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.AVAILABLE_INVENTORY.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.SEED_RESERVATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.GERMPLASM_LOCATION.getTermId().getId());
		Mockito.doReturn(null).when(this.ontologyDataManager).getTermById(ColumnLabels.BREEDING_METHOD_NAME.getTermId().getId());

		this.germplasmSearchResultsComponent.instantiateComponents();

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

	private Term createTerm(final String name) {
		final Term term = new Term();
		term.setName(name);
		term.setId(0);
		return term;
	}

}
