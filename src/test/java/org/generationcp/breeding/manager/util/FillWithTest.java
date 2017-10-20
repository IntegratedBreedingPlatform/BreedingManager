
package org.generationcp.breeding.manager.util;

import java.util.Collection;

import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithMenuItemClickListener;
import org.generationcp.breeding.manager.listmanager.util.FillWith;
import org.generationcp.breeding.manager.listmanager.util.FillWithOption;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Table;

import junit.framework.Assert;

public class FillWithTest {

	private static final String FILL_WITH_ATTRIBUTE = "Fill with Attribute";

	private static final String FILL_WITH_EMPTY = "Fill With Empty";

	private static final String FILL_WITH_SEQUENCE = "Fill With Sequence";

	private static final String FILL_WITH_CROSS_EXPANSION = "Fill With Cross Expansion";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private AddColumnSource addColumnSource;

	@Mock
	private AbsoluteLayout parentLayout;
	
	@Mock
	private Table table;

	private FillWith fillWith;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		Mockito.doReturn(ColumnLabels.PREFERRED_ID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey());
		Mockito.doReturn(ColumnLabels.PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.GERMPLASM_DATE.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey());
		Mockito.doReturn(ColumnLabels.GERMPLASM_LOCATION.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey());
		Mockito.doReturn(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.name()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NAME.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_ABBREVIATION.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_NUMBER.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey());
		Mockito.doReturn(ColumnLabels.BREEDING_METHOD_GROUP.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey());
		Mockito.doReturn(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.name()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_FEMALE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey());
		Mockito.doReturn(FillWithOption.FILL_WITH_CROSS_MALE_INFO.name()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_INFO.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_GID.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey());
		Mockito.doReturn(ColumnLabels.CROSS_MALE_PREFERRED_NAME.getName()).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey());
		Mockito.doReturn(FillWithTest.FILL_WITH_ATTRIBUTE).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
		Mockito.doReturn(FillWithTest.FILL_WITH_EMPTY).when(this.messageSource).getMessage(FillWithOption.FILL_WITH_EMPTY.getMessageKey());
		Mockito.doReturn(FillWithTest.FILL_WITH_CROSS_EXPANSION).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_CROSS_EXPANSION.getMessageKey());
		Mockito.doReturn(FillWithTest.FILL_WITH_SEQUENCE).when(this.messageSource)
				.getMessage(FillWithOption.FILL_WITH_SEQUENCE_NUMBMER.getMessageKey());
		
		this.fillWith = new FillWith(this.addColumnSource, this.parentLayout, this.messageSource);
	}

	@Test
	public void testSetupContextMenu() {
		final ContextMenu fillWithmenu = this.fillWith.getFillWithMenu();
		Assert.assertNotNull(fillWithmenu);
		final Collection<?> menuListeners = fillWithmenu.getListeners(ClickEvent.class);
		Assert.assertNotNull(menuListeners);
		Assert.assertEquals(1, menuListeners.size());
		Assert.assertTrue(menuListeners.iterator().next() instanceof FillWithMenuItemClickListener);
		Assert.assertNotNull(this.fillWith.getHeaderClickListener());
		
		Mockito.verify(this.parentLayout).addComponent(fillWithmenu);
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_EMPTY.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_PREFERRED_ID.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_PREFERRED_NAME.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_GERMPLASM_DATE.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_LOCATION.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_INFO.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_ABBREV.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_NUMBER.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_BREEDING_METHOD_GROUP.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_INFO.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_GID.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_FEMALE_NAME.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_MALE_INFO.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_MALE_GID.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_MALE_NAME.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_ATTRIBUTE.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_CROSS_EXPANSION.getMessageKey());
		Mockito.verify(this.messageSource).getMessage(FillWithOption.FILL_WITH_SEQUENCE_NUMBMER.getMessageKey());
	}
	
	@Test
	public void testSetContextMenuEnabled() {
		this.fillWith.setContextMenuEnabled(this.table, true);
		Mockito.verify(this.table).removeListener(this.fillWith.getHeaderClickListener());
		Mockito.verify(this.table).addListener(this.fillWith.getHeaderClickListener());
	}
	
	@Test
	public void testSetContextMenuDisabled() {
		this.fillWith.setContextMenuEnabled(this.table, false);
		Mockito.verify(this.table).removeListener(this.fillWith.getHeaderClickListener());
		Mockito.verify(this.table, Mockito.never()).addListener(this.fillWith.getHeaderClickListener());
	}

}
