
package org.generationcp.breeding.manager.listmanager.listeners.test;

import org.generationcp.breeding.manager.listmanager.listeners.FillWithMenuTableHeaderClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Table.HeaderClickEvent;

public class FillWithMenuTableHeaderClickListenerTest {

	private static final int Y_COORDINATE = 100;

	private static final int X_COORDINATE = 500;

	@Mock
	private FillWith fillWith;

	@Mock
	private ContextMenu fillWithMenu;

	@Mock
	private ContextMenuItem menuFillWithLocationName;

	@Mock
	private ContextMenuItem menuFillWithCrossExpansion;

	@Mock
	private HeaderClickEvent headerClickEvent;

	@InjectMocks
	private FillWithMenuTableHeaderClickListener tableHeaderClickListener;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.tableHeaderClickListener.setMenuFillWithCrossExpansion(this.menuFillWithCrossExpansion);
		this.tableHeaderClickListener.setMenuFillWithLocationName(this.menuFillWithLocationName);

		Mockito.doReturn(com.vaadin.event.MouseEvents.ClickEvent.BUTTON_RIGHT).when(this.headerClickEvent).getButton();
		Mockito.doReturn(FillWithMenuTableHeaderClickListenerTest.X_COORDINATE).when(this.headerClickEvent).getClientX();
		Mockito.doReturn(FillWithMenuTableHeaderClickListenerTest.Y_COORDINATE).when(this.headerClickEvent).getClientY();
	}

	@Test
	public void testTableHeaderLeftClick() {
		Mockito.doReturn(com.vaadin.event.MouseEvents.ClickEvent.BUTTON_LEFT).when(this.headerClickEvent).getButton();
		this.tableHeaderClickListener.headerClick(this.headerClickEvent);

		Mockito.verifyZeroInteractions(this.fillWith);
		Mockito.verifyZeroInteractions(this.fillWithMenu);
		Mockito.verifyZeroInteractions(this.menuFillWithCrossExpansion);
		Mockito.verifyZeroInteractions(this.menuFillWithLocationName);
	}

	@Test
	public void testTableHeaderRightClickOnEntryCodeColumn() {
		final String columnName = ColumnLabels.ENTRY_CODE.getName();
		Mockito.doReturn(columnName).when(this.headerClickEvent).getPropertyId();
		this.tableHeaderClickListener.headerClick(this.headerClickEvent);

		Mockito.verify(this.fillWithMenu).setData(columnName);
		Mockito.verify(this.menuFillWithLocationName).setVisible(false);
		Mockito.verify(this.menuFillWithCrossExpansion).setVisible(false);
		Mockito.verify(this.fillWith).setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
		Mockito.verify(this.fillWithMenu).show(Matchers.eq(FillWithMenuTableHeaderClickListenerTest.X_COORDINATE),
				Matchers.eq(FillWithMenuTableHeaderClickListenerTest.Y_COORDINATE));
	}

	@Test
	public void testTableHeaderRightClickOnSeedSourceColumn() {
		final String columnName = ColumnLabels.SEED_SOURCE.getName();
		Mockito.doReturn(columnName).when(this.headerClickEvent).getPropertyId();
		this.tableHeaderClickListener.headerClick(this.headerClickEvent);

		Mockito.verify(this.fillWithMenu).setData(columnName);
		Mockito.verify(this.menuFillWithLocationName).setVisible(true);
		Mockito.verify(this.menuFillWithCrossExpansion).setVisible(false);
		Mockito.verify(this.fillWith).setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(true);
		Mockito.verify(this.fillWithMenu).show(Matchers.eq(FillWithMenuTableHeaderClickListenerTest.X_COORDINATE),
				Matchers.eq(FillWithMenuTableHeaderClickListenerTest.Y_COORDINATE));
	}

	@Test
	public void testTableHeaderRightClickOnParentageColumn() {
		final String columnName = ColumnLabels.PARENTAGE.getName();
		Mockito.doReturn(columnName).when(this.headerClickEvent).getPropertyId();
		this.tableHeaderClickListener.headerClick(this.headerClickEvent);

		Mockito.verify(this.fillWithMenu).setData(columnName);
		Mockito.verify(this.menuFillWithLocationName).setVisible(false);
		Mockito.verify(this.menuFillWithCrossExpansion).setVisible(true);
		Mockito.verify(this.fillWith).setCommonOptionsForEntryCodeAndSeedSourceToBeVisible(false);
		Mockito.verify(this.fillWithMenu).show(Matchers.eq(FillWithMenuTableHeaderClickListenerTest.X_COORDINATE),
				Matchers.eq(FillWithMenuTableHeaderClickListenerTest.Y_COORDINATE));
	}

	@Test
	public void testTableHeaderRightClickOnNonSupportedColumns() {
		final String[] otherColumns = {ColumnLabels.GID.getName(), ColumnLabels.DESIGNATION.getName(),
				ColumnLabels.AVAILABLE_INVENTORY.getName(), ColumnLabels.GROUP_ID.getName(), ColumnLabels.STOCKID.getName()};
		for (final String column : otherColumns) {
			Mockito.doReturn(column).when(this.headerClickEvent).getPropertyId();
			this.tableHeaderClickListener.headerClick(this.headerClickEvent);

			Mockito.verify(this.fillWithMenu).setData(column);
			;
			Mockito.verifyZeroInteractions(this.fillWith);
			Mockito.verifyZeroInteractions(this.menuFillWithCrossExpansion);
			Mockito.verifyZeroInteractions(this.menuFillWithLocationName);
			Mockito.verifyNoMoreInteractions(this.fillWithMenu);
		}
	}

}
