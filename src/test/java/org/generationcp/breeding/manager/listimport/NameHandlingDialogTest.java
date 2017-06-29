
package org.generationcp.breeding.manager.listimport;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.beust.jcommander.internal.Lists;
import com.vaadin.ui.ComboBox;

import junit.framework.Assert;

public class NameHandlingDialogTest {

	public static final List<String> NAME_FACTORS = Lists.newArrayList("DRVNM", "IBP_ALIAS", "LEAFNODE-NAME");

	@Mock
	private NameHandlingDialogSource source;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private NameHandlingDialog nameHandlingDialog;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.nameHandlingDialog = new NameHandlingDialog(this.source, NameHandlingDialogTest.NAME_FACTORS);
		this.nameHandlingDialog.setMessageSource(this.messageSource);

		this.nameHandlingDialog.instantiateComponents();
	}

	@Test
	public void testPopulateNameTypesComboBox() {
		this.nameHandlingDialog.populateNameTypesComboBox();

		final ComboBox nameTypesComboBox = this.nameHandlingDialog.getNameTypesComboBox();
		Assert.assertEquals("Expecting " + NameHandlingDialogTest.NAME_FACTORS.size() + " name factors to be displayed.",
				NameHandlingDialogTest.NAME_FACTORS.size(), nameTypesComboBox.size());
		Assert.assertEquals("Expecting first name factor to be chosen.", NameHandlingDialogTest.NAME_FACTORS.get(0),
				nameTypesComboBox.getValue().toString());
	}

	@Test
	public void testPopulateNameTypesComboBoxNoNameFactors() {
		this.nameHandlingDialog.setImportedNameFactors(new ArrayList<String>());
		this.nameHandlingDialog.populateNameTypesComboBox();

		final ComboBox nameTypesComboBox = this.nameHandlingDialog.getNameTypesComboBox();
		Assert.assertEquals("Expecting no name factors to be displayed.", 0, nameTypesComboBox.size());
	}

}
