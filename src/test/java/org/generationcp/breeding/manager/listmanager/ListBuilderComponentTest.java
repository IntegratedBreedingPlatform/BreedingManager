package org.generationcp.breeding.manager.listmanager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

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
	
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	private ListBuilderComponent listBuilderComponent;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		listBuilderComponent = spy(new ListBuilderComponent());
		listBuilderComponent.setMessageSource(messageSource);
		
		doReturn(CHECK).when(messageSource).getMessage(Message.CHECK_ICON);
		doReturn(HASH).when(messageSource).getMessage(Message.HASHTAG);
		doReturn(AVAIL_INV).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.AVAILABLE_INVENTORY);
		doReturn(SEED_RES).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.SEED_RESERVATION);
		doReturn(GID).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.GID);
		doReturn(ENTRY_CODE).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.ENTRY_CODE);
		doReturn(DESIG).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.DESIGNATION);
		doReturn(CROSS).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.PARENTAGE);
		doReturn(SEED_SOURCE).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.SEED_SOURCE);
		doReturn(STOCKID).when(listBuilderComponent).getTermNameFromOntology(ColumnLabels.STOCKID);
		
	}
	
	@Test
	public void testAddBasicTableColumns(){
		
		Table table =  new Table();
		listBuilderComponent.addBasicTableColumns(table);
		
		assertEquals(CHECK ,table.getColumnHeader(ColumnLabels.TAG.getName()));
		assertEquals(HASH ,table.getColumnHeader(ColumnLabels.ENTRY_ID.getName()));
		assertEquals(AVAIL_INV ,table.getColumnHeader(ColumnLabels.AVAILABLE_INVENTORY.getName()));
		assertEquals(SEED_RES ,table.getColumnHeader(ColumnLabels.SEED_RESERVATION.getName()));
		assertEquals(GID ,table.getColumnHeader(ColumnLabels.GID.getName()));
		assertEquals(ENTRY_CODE ,table.getColumnHeader(ColumnLabels.ENTRY_CODE.getName()));
		assertEquals(DESIG ,table.getColumnHeader(ColumnLabels.DESIGNATION.getName()));
		assertEquals(CROSS ,table.getColumnHeader(ColumnLabels.PARENTAGE.getName()));
		assertEquals(SEED_SOURCE ,table.getColumnHeader(ColumnLabels.SEED_SOURCE.getName()));
		assertEquals(STOCKID ,table.getColumnHeader(ColumnLabels.STOCKID.getName()));
		
	}

}
