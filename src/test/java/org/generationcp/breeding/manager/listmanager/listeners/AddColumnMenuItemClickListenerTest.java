
package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.FillWithAttributeWindow;
import org.generationcp.breeding.manager.listmanager.FillWithGermplasmNameWindow;
import org.generationcp.breeding.manager.listmanager.GermplasmColumnValuesGenerator;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.AddColumnMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class AddColumnMenuItemClickListenerTest {

	private static final String FILL_WITH_ATTRIBUTE = "Fill With Attribute";

	private static final String FILL_WITH_GERMPLASM_NAME = "Fill With Germplasm Name";

	@Mock
	private ClickEvent clickEvent;

	@Mock
	private ContextMenuItem contextMenuItem;

	@Mock
	private AddColumnSource addColumnSource;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmColumnValuesGenerator valuesGenerator;

	@InjectMocks
	private AddColumnMenuItemClickListener addColumnClickListener;

	@Before
	public void setup() {
		this.addColumnClickListener.setMessageSource(this.messageSource);
		this.addColumnClickListener.setValuesGenerator(this.valuesGenerator);

		Mockito.doReturn(this.contextMenuItem).when(this.clickEvent).getClickedItem();
		Mockito.doReturn(false).when(this.addColumnSource).columnExists(Matchers.anyString());

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
		Mockito.doReturn(AddColumnMenuItemClickListenerTest.FILL_WITH_ATTRIBUTE).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_PREFERRED_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_PREFERRED_NAME.getMessageKey());
		Mockito.doReturn(AddColumnMenuItemClickListenerTest.FILL_WITH_GERMPLASM_NAME).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GERMPLASM_NAME.getMessageKey());
	}

	@Test
	public void testFillWithPreferredIDItemClick() {
		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.PREFERRED_ID);
		Mockito.verify(this.valuesGenerator).setPreferredIdColumnValues(ColumnLabels.PREFERRED_ID.getName());
	}

	@Test
	public void testFillWithPreferredNameItemClick() {
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator).setPreferredNameColumnValues(ColumnLabels.PREFERRED_NAME.getName());
	}

	@Test
	public void testFillWithLocationItemClick() {
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GERMPLASM_LOCATION);
		Mockito.verify(this.valuesGenerator).setLocationNameColumnValues(ColumnLabels.GERMPLASM_LOCATION.getName());
	}

	@Test
	public void testFillWithGermplasmDateItemClick() {
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GERMPLASM_DATE);
		Mockito.verify(this.valuesGenerator).setGermplasmDateColumnValues(ColumnLabels.GERMPLASM_DATE.getName());
	}

	@Test
	public void testFillWithBreedingMethodNameItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_NAME);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
	}

	@Test
	public void testFillWithBreedingMethodAbbreviationItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
	}

	@Test
	public void testFillWithBreedingMethodNumberItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
	}

	@Test
	public void testFillWithBreedingMethodGroupItemClick() {
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.BREEDING_METHOD_GROUP);
		Mockito.verify(this.valuesGenerator).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
	}

	@Test
	public void testFillWithCrossFemaleGIDItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.FGID);
		Mockito.verify(this.valuesGenerator).setCrossFemaleInfoColumnValues(ColumnLabels.FGID.getName(),
				FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
	}

	@Test
	public void testFillWithCrossFemaleNameItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator).setCrossFemaleInfoColumnValues(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(),
				FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
	}

	@Test
	public void testFillWithCrossMaleGIDItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.MGID);
		Mockito.verify(this.valuesGenerator).setCrossMaleGIDColumnValues(ColumnLabels.MGID.getName());
	}

	@Test
	public void testFillWithCrossMaleNameItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator).setCrossMalePrefNameColumnValues(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
	}

	@Test
	public void testFillWithGroupSourceGIDItemClick() {
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GROUP_SOURCE_GID);
		Mockito.verify(this.valuesGenerator).setGroupSourceGidColumnValues(ColumnLabels.GROUP_SOURCE_GID.getName());
	}

	@Test
	public void testFillWithGroupSourcePreferredNameItemClick() {
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator).setGroupSourcePreferredNameColumnValues(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName());
	}

	@Test
	public void testFillWithImmediateSourceGIDItemClick() {
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.IMMEDIATE_SOURCE_GID);
		Mockito.verify(this.valuesGenerator).setImmediateSourceGidColumnValues(ColumnLabels.IMMEDIATE_SOURCE_GID.getName());
	}

	@Test
	public void testFillWithImmediateSourcePreferredNameItemClick() {
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator)
				.setImmediateSourcePreferredNameColumnValues(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName());
	}

	@Test
	public void testFillWithAttributeItemClick() {
		Mockito.doReturn(AddColumnMenuItemClickListenerTest.FILL_WITH_ATTRIBUTE).when(this.contextMenuItem).getName();
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
	public void testFillWithGermplasmNameItemClick() {
		Mockito.doReturn(AddColumnMenuItemClickListenerTest.FILL_WITH_GERMPLASM_NAME).when(this.contextMenuItem).getName();
		final Window parentWindow = Mockito.mock(Window.class);
		Mockito.doReturn(parentWindow).when(this.addColumnSource).getWindow();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		final ArgumentCaptor<Window> subWindowCaptor = ArgumentCaptor.forClass(Window.class);
		Mockito.verify(parentWindow).addWindow(subWindowCaptor.capture());
		Assert.assertTrue(subWindowCaptor.getValue() instanceof FillWithGermplasmNameWindow);

		final FillWithGermplasmNameWindow nameTypeWindow = (FillWithGermplasmNameWindow) subWindowCaptor.getValue();
		Assert.assertEquals(this.addColumnSource, nameTypeWindow.getAddColumnSource());
	}

	@Test
	public void testFillWithPreferredIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.PREFERRED_ID.getName());
		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.PREFERRED_ID);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setPreferredIdColumnValues(ColumnLabels.PREFERRED_ID.getName());
	}

	@Test
	public void testFillWithPreferredNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setPreferredNameColumnValues(ColumnLabels.PREFERRED_NAME.getName());
	}

	@Test
	public void testFillWithLocationItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GERMPLASM_LOCATION.getName());
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GERMPLASM_LOCATION);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setLocationNameColumnValues(ColumnLabels.GERMPLASM_LOCATION.getName());
	}

	@Test
	public void testFillWithGermplasmDateItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GERMPLASM_DATE.getName());
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GERMPLASM_DATE);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setGermplasmDateColumnValues(ColumnLabels.GERMPLASM_DATE.getName());
	}

	@Test
	public void testFillWithBreedingMethodNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_NAME.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_NAME);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NAME.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NAME);
	}

	@Test
	public void testFillWithBreedingMethodAbbreviationItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_ABBREVIATION);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV);
	}

	@Test
	public void testFillWithBreedingMethodNumberItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_NUMBER.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_NUMBER);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_NUMBER.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER);
	}

	@Test
	public void testFillWithBreedingMethodGroupItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.BREEDING_METHOD_GROUP.getName());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.BREEDING_METHOD_GROUP);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setMethodInfoColumnValues(ColumnLabels.BREEDING_METHOD_GROUP.getName(),
				FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP);
	}

	@Test
	public void testFillWithCrossFemaleGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.FGID.getName());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.FGID);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setCrossFemaleInfoColumnValues(ColumnLabels.FGID.getName(),
				FillWithOption.FILL_WITH_CROSS_FEMALE_GID);
	}

	@Test
	public void testFillWithCrossFemaleNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setCrossFemaleInfoColumnValues(
				ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName(), FillWithOption.FILL_WITH_CROSS_FEMALE_NAME);
	}

	@Test
	public void testFillWithCrossMaleGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.MGID.getName());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.MGID);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setCrossMaleGIDColumnValues(ColumnLabels.MGID.getName());
	}

	@Test
	public void testFillWithCrossMaleNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator, Mockito.never())
				.setCrossMalePrefNameColumnValues(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
	}

	@Test
	public void testFillWithGroupSourceGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GROUP_SOURCE_GID.getName());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GROUP_SOURCE_GID);
		Mockito.verify(this.valuesGenerator, Mockito.never()).setGroupSourceGidColumnValues(ColumnLabels.GROUP_SOURCE_GID.getName());
	}

	@Test
	public void testFillWithGroupSourcePreferredNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator, Mockito.never())
				.setGroupSourcePreferredNameColumnValues(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName());
	}

	@Test
	public void testFillWithImmediateSourceGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.IMMEDIATE_SOURCE_GID.getName());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.IMMEDIATE_SOURCE_GID);
		Mockito.verify(this.valuesGenerator, Mockito.never())
				.setImmediateSourceGidColumnValues(ColumnLabels.IMMEDIATE_SOURCE_GID.getName());
	}

	@Test
	public void testFillWithImmediateSourcePreferredNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME);
		Mockito.verify(this.valuesGenerator, Mockito.never())
				.setImmediateSourcePreferredNameColumnValues(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName());
	}
	
	@Test
	public void testIsFromGermplasmSearchWindow() {
		Assert.assertFalse(this.addColumnClickListener.isFromGermplasmSearchWindow());
	}

}
