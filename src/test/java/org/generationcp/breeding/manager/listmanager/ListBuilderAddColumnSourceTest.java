
package org.generationcp.breeding.manager.listmanager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Table;

public class ListBuilderAddColumnSourceTest {

	private static final String GID_PROPERTY_ID = "GID_BUTTON";

	@Mock
	private ListBuilderComponent listBuilderComponent;

	@Mock
	private Table targetTable;

	private ListBuilderAddColumnSource addColumnSource;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.addColumnSource =
				new ListBuilderAddColumnSource(this.listBuilderComponent, this.targetTable, ListBuilderAddColumnSourceTest.GID_PROPERTY_ID);
	}

	@Test
	public void testPropagateUIChangesForEditableTable() {
		Mockito.doReturn(true).when(this.targetTable).isEditable();
		this.addColumnSource.propagateUIChanges();

		Mockito.verify(this.listBuilderComponent).setHasUnsavedChanges(true);
		Mockito.verify(this.targetTable).setEditable(false);
		Mockito.verify(this.targetTable).setEditable(true);
	}

	@Test
	public void testPropagateUIChangesForNonEditableTable() {
		Mockito.doReturn(false).when(this.targetTable).isEditable();
		this.addColumnSource.propagateUIChanges();

		Mockito.verify(this.listBuilderComponent).setHasUnsavedChanges(true);
		Mockito.verify(this.targetTable, Mockito.never()).setEditable(Matchers.anyBoolean());
	}

}
