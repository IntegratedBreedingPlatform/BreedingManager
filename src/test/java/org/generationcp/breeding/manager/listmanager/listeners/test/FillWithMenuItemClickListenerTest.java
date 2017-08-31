
package org.generationcp.breeding.manager.listmanager.listeners.test;

import org.generationcp.breeding.manager.crossingmanager.AdditionalDetailsCrossNameComponent;
import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Window;

public class FillWithMenuItemClickListenerTest {

	private static final String FILL_WITH_ATTRIBUTE = "Fill with Attribute";

	private static final String FILL_WITH_EMPTY = "Fill With Empty";

	private static final String FILL_WITH_SEQUENCE = "Fill With Sequence";

	private static final String FILL_WITH_CROSS_EXPANSION = "Fill With Cross Expansion";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private ClickEvent clickEvent;

	@Mock
	private ContextMenuItem contextMenuItem;

	@Mock
	private FillWith fillWith;

	@Mock
	private AddColumnSource addColumnSource;

	@Mock
	private ContextMenu fillWithMenu;

	@Mock
	private GermplasmColumnValuesGenerator valuesGenerator;

	@InjectMocks
	private FillWithMenuItemClickListener menuClickListener;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.menuClickListener.setMessageSource(this.messageSource);

		Mockito.doReturn(this.contextMenuItem).when(this.clickEvent).getClickedItem();
		Mockito.doReturn(ColumnLabels.ENTRY_CODE.getName()).when(this.fillWithMenu).getData();

		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey());
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey());
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey());
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_ATTRIBUTE).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_EMPTY).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_EMPTY.getMessageKey());
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_CROSS_EXPANSION).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_EXPANSION.getMessageKey());
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_SEQUENCE).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_SEQUENCE_NUMBMER.getMessageKey());
	}

	@Test
	public void testFillWithPreferredIDItemClick() {
		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setPreferredIdColumnValues(ColumnLabels.ENTRY_CODE.getName());
	}

	@Test
	public void testFillWithPreferredNameItemClick() {
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setPreferredNameColumnValues(ColumnLabels.ENTRY_CODE.getName());
	}

	@Test
	public void testFillWithEmptyItemClick() {
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_EMPTY).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).fillWithEmpty(ColumnLabels.ENTRY_CODE.getName());
	}

	@Test
	public void testFillWithGermplasmDateItemClick() {
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setGermplasmDateColumnValues(ColumnLabels.ENTRY_CODE.getName());
	}

	@Test
	public void testFillWithLocationItemClick() {
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setLocationNameColumnValues(ColumnLabels.ENTRY_CODE.getName());
	}

	@Test
	public void testFillWithBreedingMethodNameItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.ENTRY_CODE.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
	}

	@Test
	public void testFillWithBreedingMethodAbbreviationItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.ENTRY_CODE.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
	}

	@Test
	public void testFillWithBreedingMethodNumberItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.ENTRY_CODE.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
	}

	@Test
	public void testFillWithBreedingMethodGroupItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.ENTRY_CODE.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
	}

	@Test
	public void testFillWithCrossFemaleGIDItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setCrossFemaleInfoColumnValues(ColumnLabels.ENTRY_CODE.getName(),
				FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
	}

	@Test
	public void testFillWithCrossFemaleNameItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setCrossFemaleInfoColumnValues(ColumnLabels.ENTRY_CODE.getName(),
				FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
	}

	@Test
	public void testFillWithCrossMaleGIDItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setCrossMaleGIDColumnValues(ColumnLabels.ENTRY_CODE.getName());
	}

	@Test
	public void testFillWithCrossMaleNameItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.menuClickListener.contextItemClick(this.clickEvent);
		Mockito.verify(this.valuesGenerator).setCrossMalePrefNameColumnValues(ColumnLabels.ENTRY_CODE.getName());
	}

	@Test
	public void testFillWithAttributeItemClick() {
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_ATTRIBUTE).when(this.contextMenuItem).getName();
		final Window parentWindow = Mockito.mock(Window.class);
		Mockito.doReturn(parentWindow).when(this.addColumnSource).getWindow();

		this.menuClickListener.contextItemClick(this.clickEvent);

		final ArgumentCaptor<Window> subWindowCaptor = ArgumentCaptor.forClass(Window.class);
		Mockito.verify(parentWindow).addWindow(subWindowCaptor.capture());
		Assert.assertTrue(subWindowCaptor.getValue() instanceof FillWithAttributeWindow);

		final FillWithAttributeWindow attributeWindow = (FillWithAttributeWindow) subWindowCaptor.getValue();
		Assert.assertEquals(this.addColumnSource, attributeWindow.getAddColumnSource());
	}

	@Test
	public void testFillWithSequenceItemClick() {
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_SEQUENCE).when(this.contextMenuItem).getName();
		final Window parentWindow = Mockito.mock(Window.class);
		Mockito.doReturn(parentWindow).when(this.addColumnSource).getWindow();

		this.menuClickListener.contextItemClick(this.clickEvent);

		final ArgumentCaptor<Window> subWindowCaptor = ArgumentCaptor.forClass(Window.class);
		Mockito.verify(parentWindow).addWindow(subWindowCaptor.capture());
		final Window window = subWindowCaptor.getValue();
		Assert.assertEquals(FillWithMenuItemClickListener.SPECIFY_SEQUENCE_NUMBER, window.getCaption());

		final AdditionalDetailsCrossNameComponent nameWindow = (AdditionalDetailsCrossNameComponent) window.getContent();
		Assert.assertEquals(this.fillWith, nameWindow.getFillWithSource());
		Assert.assertEquals(ColumnLabels.ENTRY_CODE.getName(), nameWindow.getPropertyIdToFill());
	}

	@Test
	public void testFillWithCrossExpansionItemClick() {
		Mockito.doReturn(FillWithMenuItemClickListenerTest.FILL_WITH_CROSS_EXPANSION).when(this.contextMenuItem).getName();
		final Window parentWindow = Mockito.mock(Window.class);
		Mockito.doReturn(parentWindow).when(this.addColumnSource).getWindow();

		this.menuClickListener.contextItemClick(this.clickEvent);

		final ArgumentCaptor<Window> subWindowCaptor = ArgumentCaptor.forClass(Window.class);
		Mockito.verify(parentWindow).addWindow(subWindowCaptor.capture());
		final Window window = subWindowCaptor.getValue();
		Assert.assertEquals(FillWithMenuItemClickListener.SPECIFY_EXPANSION_LEVEL, window.getCaption());
	}

}
