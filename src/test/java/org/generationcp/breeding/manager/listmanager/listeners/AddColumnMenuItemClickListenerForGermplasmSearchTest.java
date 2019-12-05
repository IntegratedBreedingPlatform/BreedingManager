
package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.constant.ColumnLabels;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

@RunWith(MockitoJUnitRunner.class)
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
		this.addColumnClickListener.setMessageSource(this.messageSource);

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
		Mockito.doReturn(AddColumnMenuItemClickListenerForGermplasmSearchTest.FILL_WITH_ATTRIBUTE).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GROUP_SOURCE_PREFERRED_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_IMMEDIATE_SOURCE_PREFERRED_NAME.getMessageKey());
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

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.FGID);

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

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.MGID);

	}

	@Test
	public void testFillWithCrossMaleNameItemClick() {
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);

	}
	
	@Test
	public void testFillWithGroupSourceGIDItemClick() {
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GROUP_SOURCE_GID);
	}
	
	@Test
	public void testFillWithGroupSourcePreferredNameItemClick() {
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME);
	}
	
	@Test
	public void testFillWithImmediateSourceGIDItemClick() {
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.IMMEDIATE_SOURCE_GID);
	}
	
	@Test
	public void testFillWithImmediateSourcePreferredNameItemClick() {
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource).addColumn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME);
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
		Mockito.doReturn(true).when(this.addColumnSource)
				.columnExists(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName());
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
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.FGID.getName());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.FGID);
	}

	@Test
	public void testFillWithCrossFemaleNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource)
				.columnExists(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME);
	}

	@Test
	public void testFillWithCrossMaleGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.MGID.getName());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.MGID);
	}

	@Test
	public void testFillWithCrossMaleNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource)
				.columnExists(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.CROSS_MALE_PREFERRED_NAME);
	}
	
	@Test
	public void testFillWithGroupSourceGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GROUP_SOURCE_GID.getName());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GROUP_SOURCE_GID);
	}
	
	@Test
	public void testFillWithGroupSourcePreferredNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.GROUP_SOURCE_PREFERRED_NAME);
	}
	
	@Test
	public void testFillWithImmediateSourceGIDItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.IMMEDIATE_SOURCE_GID.getName());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_GID.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.IMMEDIATE_SOURCE_GID);
	}
	
	@Test
	public void testFillWithImmediateSourcePreferredNameItemClickAndColumnExists() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName());
		Mockito.doReturn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName()).when(this.contextMenuItem).getName();
		this.addColumnClickListener.contextItemClick(this.clickEvent);

		Mockito.verify(this.addColumnSource, Mockito.never()).addColumn(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME);
	}
	
	@Test
	public void testIsFromGermplasmSearchWindow() {
		Assert.assertTrue(this.addColumnClickListener.isFromGermplasmSearchWindow());
	}

}
