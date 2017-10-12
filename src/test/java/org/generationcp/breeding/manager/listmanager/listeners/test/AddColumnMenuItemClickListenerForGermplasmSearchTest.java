
package org.generationcp.breeding.manager.listmanager.listeners.test;

import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.AddColumnMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.AddColumnMenuItemClickListenerForGermplasmSearch;
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
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

public class AddColumnMenuItemClickListenerForGermplasmSearchTest {

	private static final String FILL_WITH_ATTRIBUTE = "Fill With Attribute";
	
	@Mock
	private ClickEvent clickEvent;

	@Mock
	private ContextMenuItem contextMenuItem;

	@Mock
	private AddColumnSource addColumnSource;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private AddColumnMenuItemClickListenerForGermplasmSearch addColumnClickListener;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.addColumnClickListener.setMessageSource(this.messageSource);

		Mockito.doReturn(this.contextMenuItem).when(this.clickEvent).getClickedItem();
		Mockito.doReturn(false).when(this.addColumnSource).columnExists(Mockito.anyString());

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
		Mockito.doReturn(FILL_WITH_ATTRIBUTE).when(this.messageSource).getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
	}

	@Test
	public void testFillWithPreferredIDItemClick() {
		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.PREFERRED_ID);

	}

	@Test
	public void testFillWithPreferredNameItemClick() {
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.PREFERRED_NAME);

	}

	@Test
	public void testFillWithLocationItemClick() {
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GERMPLASM_LOCATION);

	}

	@Test
	public void testFillWithGermplasmDateItemClick() {
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GERMPLASM_DATE);

	}

	@Test
	public void testFillWithBreedingMethodNameItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_NAME);

	}

	@Test
	public void testFillWithBreedingMethodAbbreviationItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);

	}

	@Test
	public void testFillWithBreedingMethodNumberItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);

	}

	@Test
	public void testFillWithBreedingMethodGroupItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_GROUP);

	}

	@Test
	public void testFillWithCrossFemaleGIDItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.CROSS_FEMALE_GID);

	}

	@Test
	public void testFillWithCrossFemaleNameItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);

	}

	@Test
	public void testFillWithCrossMaleGIDItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.CROSS_MALE_GID);

	}

	@Test
	public void testFillWithCrossMaleNameItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);

	}

	@Test
	public void testFillWithAttributeItemClick() {
		Mockito.doReturn(FILL_WITH_ATTRIBUTE).when(this.contextMenuItem).getName();
		final Window parentWindow = Mockito.mock(Window.class);
		Mockito.doReturn(parentWindow).when(this.addColumnSource).getWindow();

		this.addColumnClickListener.contextItemClick(this.clickEvent);

		final ArgumentCaptor<Window> subWindowCaptor = ArgumentCaptor.forClass(Window.class);
		Mockito.verify(parentWindow).addWindow(subWindowCaptor.capture());
		Assert.assertTrue(subWindowCaptor.getValue() instanceof FillWithAttributeWindow);

		final FillWithAttributeWindow attributeWindow = (FillWithAttributeWindow) subWindowCaptor.getValue();
		Assert.assertEquals(this.addColumnSource, attributeWindow.getAddColumnSource());
	}

	@Test
	public void testFillWithPreferredIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.PREFERRED_ID.getName());
		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.PREFERRED_ID);
	}

	@Test
	public void testFillWithPreferredNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.PREFERRED_NAME);
	}

	@Test
	public void testFillWithLocationItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GERMPLASM_LOCATION);

	}

	@Test
	public void testFillWithGermplasmDateItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GERMPLASM_DATE);
	}

	@Test
	public void testFillWithBreedingMethodNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_NAME.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_NAME);
	}

	@Test
	public void testFillWithBreedingMethodAbbreviationItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);
	}

	@Test
	public void testFillWithBreedingMethodNumberItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);
	}

	@Test
	public void testFillWithBreedingMethodGroupItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_GROUP.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_GROUP);

	}

	@Test
	public void testFillWithCrossFemaleGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.CROSS_FEMALE_GID.getName());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_FEMALE_GID);
	}

	@Test
	public void testFillWithCrossFemaleNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
	}

	@Test
	public void testFillWithCrossMaleGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.CROSS_MALE_GID.getName());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_MALE_GID);
	}

	@Test
	public void testFillWithCrossMaleNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
	}

}
