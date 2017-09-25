
package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithAttributeButtonClickListener;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;

import junit.framework.Assert;

public class FillWithAttributeWindowTest {

	private static final int ATTRIBUTE_TYPE_ID2 = 2;
	private static final int ATTRIBUTE_TYPE_ID1 = 1;
	private static final String ATTRIBUTE_TYPE_CODE2 = "NEW_PAZZPORT";
	private static final String ATTRIBUTE_TYPE_CODE1 = "Ipstat";
	private static final String ATTRIBUTE_TYPE_NAME2 = "New Passport Type";
	private static final String ATTRIBUTE_TYPE_NAME1 = "Ip Status";
	private static final List<Integer> GID_LIST = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmColumnValuesGenerator valuesGenerator;

	@Mock
	private AddColumnSource addColumnSource;

	@InjectMocks
	private FillWithAttributeWindow fillWithAttributeWindow;

	private List<UserDefinedField> attributeTypes;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.fillWithAttributeWindow.setGermplasmDataManager(this.germplasmDataManager);

		Mockito.doReturn(GID_LIST).when(this.addColumnSource).getAllGids();
		this.attributeTypes = this.getAttributeTypes();
		Mockito.doReturn(this.attributeTypes).when(this.germplasmDataManager).getAttributeTypesByGIDList(Matchers.eq(GID_LIST));
	}

	@Test
	public void testPopulateAttributeTypes() {
		this.fillWithAttributeWindow.instantiateComponents();
		this.fillWithAttributeWindow.initializeValues();

		Mockito.verify(this.addColumnSource).getAllGids();
		Mockito.verify(this.germplasmDataManager).getAttributeTypesByGIDList(Matchers.eq(GID_LIST));
		final ComboBox attributeTypesComboBox = this.fillWithAttributeWindow.getAttributeBox();
		Assert.assertNotNull(attributeTypesComboBox);
		Assert.assertEquals(3, attributeTypesComboBox.size());
		for (final UserDefinedField attributeType : this.attributeTypes) {
			final Integer id = attributeType.getFldno();
			Assert.assertNotNull(attributeTypesComboBox.getItem(id));
			Assert.assertEquals(attributeType.getFcode(), attributeTypesComboBox.getItemCaption(id));
		}
	}

	@Test
	public void testAddListeners() {
		this.fillWithAttributeWindow.instantiateComponents();
		this.fillWithAttributeWindow.addListeners();

		final Collection<?> clickListeners = this.fillWithAttributeWindow.getOkButton().getListeners(ClickEvent.class);
		Assert.assertNotNull(clickListeners);
		Assert.assertEquals(1, clickListeners.size());
		Assert.assertTrue(clickListeners.iterator().next() instanceof FillWithAttributeButtonClickListener);
	}

	private List<UserDefinedField> getAttributeTypes() {
		final UserDefinedField attributeType1 = new UserDefinedField(FillWithAttributeWindowTest.ATTRIBUTE_TYPE_ID1);
		attributeType1.setFname(FillWithAttributeWindowTest.ATTRIBUTE_TYPE_NAME1);
		attributeType1.setFcode(FillWithAttributeWindowTest.ATTRIBUTE_TYPE_CODE1);
		final UserDefinedField attributeType2 = new UserDefinedField(FillWithAttributeWindowTest.ATTRIBUTE_TYPE_ID2);
		attributeType2.setFname(FillWithAttributeWindowTest.ATTRIBUTE_TYPE_NAME2);
		attributeType2.setFcode(FillWithAttributeWindowTest.ATTRIBUTE_TYPE_CODE2);
		final UserDefinedField attributeType3 = new UserDefinedField(3);
		attributeType3.setFname("Grower");
		attributeType3.setFcode("Grow");
		return Arrays.asList(attributeType1, attributeType2, attributeType3);
	}

}
