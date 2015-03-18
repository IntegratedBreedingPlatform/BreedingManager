package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.vaadin.ui.Table;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class GermplasmSearchResultsComponentTest {

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
		
		doReturn("MENU").when(messageSource).getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST);
		doReturn("SELECT ALL").when(messageSource).getMessage(Message.SELECT_ALL);
		
		TableWithSelectAllLayout table = new TableWithSelectAllLayout(10, GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID);
		table.instantiateComponents();
		
		doReturn(table).when(germplasmSearchResultsComponent).getTableWithSelectAllLayout();
		
	}
	
	@Test
	public void testInstantiateComponentsHeaderNameExistsFromOntology() throws MiddlewareQueryException{
		
		doReturn(createTerm("PARENTAGE")).when(ontologyDataManager).getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		doReturn(createTerm("GID")).when(ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		doReturn(createTerm("LOCATIONS")).when(ontologyDataManager).getTermById(ColumnLabels.GERMPLASM_LOCATION.getTermId().getId());
		doReturn(createTerm("METHOD_NAME")).when(ontologyDataManager).getTermById(ColumnLabels.BREEDING_METHOD_NAME.getTermId().getId());
		
		germplasmSearchResultsComponent.instantiateComponents();
		
		Table table = germplasmSearchResultsComponent.getMatchingGermplasmsTableWithSelectAll().getTable();
		
		assertEquals("Tag All Column", table.getColumnHeader(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID));
		assertEquals("NAMES", table.getColumnHeader(GermplasmSearchResultsComponent.NAMES));
		assertEquals("PARENTAGE", table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals("GID", table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals("LOCATIONS", table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		assertEquals("METHOD_NAME", table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		
	}
	
	@Test
	public void testInstantiateComponentsHeaderNameDoesntExistFromOntology() throws MiddlewareQueryException{
		
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.GERMPLASM_LOCATION.getTermId().getId());
		doReturn(null).when(ontologyDataManager).getTermById(ColumnLabels.BREEDING_METHOD_NAME.getTermId().getId());
		
		germplasmSearchResultsComponent.instantiateComponents();
		
		Table table = germplasmSearchResultsComponent.getMatchingGermplasmsTableWithSelectAll().getTable();
		
		assertEquals("Tag All Column", table.getColumnHeader(GermplasmSearchResultsComponent.CHECKBOX_COLUMN_ID));
		assertEquals("NAMES", table.getColumnHeader(GermplasmSearchResultsComponent.NAMES));
		assertEquals(ColumnLabels.PARENTAGE.getName(), table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals(ColumnLabels.GID.getName(), table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals(ColumnLabels.GERMPLASM_LOCATION.getName(), table.getColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName()));
		assertEquals(ColumnLabels.BREEDING_METHOD_NAME.getName(), table.getColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName()));
		
	}
	
	private Term createTerm(String name){
		Term term = new Term();
		term.setName(name);
		term.setId(0);
		return(term);
	}
	
}
