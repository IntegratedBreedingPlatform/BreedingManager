
package org.generationcp.breeding.manager.listmanager.listeners.test;

import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithGermplasmNameButtonClickListener;
import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

public class FillWithGermplasmNameButtonClickListenerTest {

	private static final int NAME_TYPE_ID = 2;
	private static final String NAME_TYPE_NAME = "New Name Type";

	@Mock
	private AddColumnSource addColumnSource;

	@Mock
	private GermplasmColumnValuesGenerator valuesGenerator;

	@Mock
	private ComboBox nameTypeBox;

	@Mock
	private ClickEvent clickEvent;

	@Mock
	private Button button;

	@Mock
	private Window nameWindow;

	@Mock
	private Window parentWindow;

	@InjectMocks
	private FillWithGermplasmNameButtonClickListener clickListener;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.clickListener.setValuesGenerator(this.valuesGenerator);

		Mockito.doReturn(this.button).when(this.clickEvent).getSource();
		Mockito.doReturn(this.nameWindow).when(this.button).getWindow();
		Mockito.doReturn(this.parentWindow).when(this.nameWindow).getParent();

		Mockito.doReturn(FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_ID).when(this.nameTypeBox).getValue();
		Mockito.doReturn(FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_NAME).when(this.nameTypeBox)
				.getItemCaption(FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_ID);
	}

	@Test
	public void testButtonClickWithNullTargetPropertyId() {
		this.clickListener.setIsFromGermplasmSearchWindow(false);
		this.clickListener.buttonClick(this.clickEvent);

		// Expecting column to be added for source since target property id
		// (column) is not specified
		Mockito.verify(this.addColumnSource)
				.addColumn(FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_NAME.toUpperCase());
		// Check that chosen attribute type name was capitalized
		Mockito.verify(this.valuesGenerator).fillWithGermplasmName(
				FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_ID,
				FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_NAME.toUpperCase());
		Mockito.verify(this.parentWindow).removeWindow(this.nameWindow);
	}

	@Test
	public void testButtonClickWithTargetPropertyIdSpecified() {
		// Specify that ENTRY_CODE column will be filled up
		final String columnName = ColumnLabels.ENTRY_CODE.getName();
		this.clickListener.setTargetPropertyId(columnName);
		this.clickListener.setIsFromGermplasmSearchWindow(false);
		this.clickListener.buttonClick(this.clickEvent);

		// Expecting no column to be added for source since target column was
		// specified
		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(Matchers.anyString());
		Mockito.verify(this.valuesGenerator)
				.fillWithGermplasmName(FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_ID, columnName);
		Mockito.verify(this.parentWindow).removeWindow(this.nameWindow);
	}

	@Test
	public void testButtonClickWithNoNameTypeSelected() {
		this.clickListener.setIsFromGermplasmSearchWindow(false);
		Mockito.doReturn(null).when(this.nameTypeBox).getValue();
		this.clickListener.buttonClick(this.clickEvent);

		// Expecting no column to be added and filled up
		Mockito.verifyZeroInteractions(this.addColumnSource);
		Mockito.verifyZeroInteractions(this.valuesGenerator);
		Mockito.verify(this.parentWindow).removeWindow(this.nameWindow);
	}

	@Test
	public void testButtonWhereIsFromGermplasmSearchWindowTrue() {
		this.clickListener.setIsFromGermplasmSearchWindow(true);
		// Specify that ENTRY_CODE column will be filled up
		final String columnName = ColumnLabels.ENTRY_CODE.getName();
		this.clickListener.setTargetPropertyId(columnName);
		this.clickListener.buttonClick(this.clickEvent);

		// Expecting no column to be added for source since target column was
		// specified
		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(Matchers.anyString());
		Mockito.verify(this.valuesGenerator, Mockito.never())
				.fillWithGermplasmName(FillWithGermplasmNameButtonClickListenerTest.NAME_TYPE_ID, columnName);
		Mockito.verify(this.parentWindow).removeWindow(this.nameWindow);
	}

}
