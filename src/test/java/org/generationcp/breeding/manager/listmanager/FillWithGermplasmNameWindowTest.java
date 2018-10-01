
package org.generationcp.breeding.manager.listmanager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.containers.GermplasmQuery;
import org.generationcp.breeding.manager.listmanager.api.AddColumnSource;
import org.generationcp.breeding.manager.listmanager.listeners.FillWithGermplasmNameButtonClickListener;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class FillWithGermplasmNameWindowTest {

	private static final String NAMETYPE_NAME3 = "Line Accession Name";
	private static final String NAMETYPE_CODE3 = "LACCNM";
	private static final int NAMETYPE_ID3 = 3;
	private static final int NAME_TYPE_ID2 = 2;
	private static final int NAME_TYPE_ID1 = 1;
	private static final String NAME_TYPE_CODE2 = "COOL_NAME";
	private static final String NAME_TYPE_CODE1 = "COOLER_NAME";
	private static final String NAME_TYPE_NAME2 = "Some Cool Name";
	private static final String NAME_TYPE_NAME1 = "Some Cooler Name";
	private static final List<Integer> GID_LIST = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private GermplasmColumnValuesGenerator valuesGenerator;

	@Mock
	private AddColumnSource addColumnSource;

	@InjectMocks
	private final FillWithGermplasmNameWindow fillWithGermplasmNameWindow = new FillWithGermplasmNameWindow(this.addColumnSource,
			GermplasmQuery.GID_REF_PROPERTY, false);

	private List<UserDefinedField> nameTypes;

	@Before
	public void setup() {
		this.fillWithGermplasmNameWindow.setGermplasmDataManager(this.germplasmDataManager);

		Mockito.doReturn(FillWithGermplasmNameWindowTest.GID_LIST).when(this.addColumnSource).getAllGids();
		this.nameTypes = this.getNameTypes();
		Mockito.doReturn(this.nameTypes).when(this.germplasmDataManager)
				.getNameTypesByGIDList(Matchers.eq(FillWithGermplasmNameWindowTest.GID_LIST));
	}

	@Test
	public void testPopulateNameTypes() {
		this.fillWithGermplasmNameWindow.instantiateComponents();
		this.fillWithGermplasmNameWindow.initializeValues();

		Mockito.verify(this.addColumnSource).getAllGids();
		Mockito.verify(this.germplasmDataManager)
				.getNameTypesByGIDList(Matchers.eq(FillWithGermplasmNameWindowTest.GID_LIST));
		final ComboBox nameTypesComboBox = this.fillWithGermplasmNameWindow.getNamesTypeBox();
		Assert.assertNotNull(nameTypesComboBox);
		Assert.assertEquals(3, nameTypesComboBox.size());
		for (final UserDefinedField nameType : this.nameTypes) {
			final Integer id = nameType.getFldno();
			Assert.assertNotNull(nameTypesComboBox.getItem(id));
			Assert.assertEquals(nameType.getFname().toUpperCase(), nameTypesComboBox.getItemCaption(id));
		}
	}
	
	@Test
	public void testPopulateNameTypesWithAddedColumnAlready() {
		Mockito.doReturn(true).when(this.addColumnSource).columnExists(NAMETYPE_NAME3);
		this.fillWithGermplasmNameWindow.instantiateComponents();
		this.fillWithGermplasmNameWindow.initializeValues();

		Mockito.verify(this.addColumnSource).getAllGids();
		Mockito.verify(this.germplasmDataManager)
				.getNameTypesByGIDList(Matchers.eq(FillWithGermplasmNameWindowTest.GID_LIST));
		final ComboBox nameTypesComboBox = this.fillWithGermplasmNameWindow.getNamesTypeBox();
		Assert.assertNotNull(nameTypesComboBox);
		Assert.assertEquals(2, nameTypesComboBox.size());
		final List<UserDefinedField> subList = this.nameTypes.subList(0, 2);
		for (final UserDefinedField nameType : subList) {
			final Integer id = nameType.getFldno();
			Assert.assertNotNull(nameTypesComboBox.getItem(id));
			Assert.assertEquals(nameType.getFname().toUpperCase(), nameTypesComboBox.getItemCaption(id));
		}
	}

	@Test
	public void testAddListeners() {
		this.fillWithGermplasmNameWindow.instantiateComponents();
		this.fillWithGermplasmNameWindow.addListeners();

		final Collection<?> clickListeners = this.fillWithGermplasmNameWindow.getOkButton().getListeners(ClickEvent.class);
		Assert.assertNotNull(clickListeners);
		Assert.assertEquals(1, clickListeners.size());
		Assert.assertTrue(clickListeners.iterator().next() instanceof FillWithGermplasmNameButtonClickListener);
	}

	private List<UserDefinedField> getNameTypes() {
		final UserDefinedField type1 = new UserDefinedField(FillWithGermplasmNameWindowTest.NAME_TYPE_ID1);
		type1.setFname(FillWithGermplasmNameWindowTest.NAME_TYPE_NAME1);
		type1.setFcode(FillWithGermplasmNameWindowTest.NAME_TYPE_CODE1);
		final UserDefinedField type2 = new UserDefinedField(FillWithGermplasmNameWindowTest.NAME_TYPE_ID2);
		type2.setFname(FillWithGermplasmNameWindowTest.NAME_TYPE_NAME2);
		type2.setFcode(FillWithGermplasmNameWindowTest.NAME_TYPE_CODE2);
		final UserDefinedField type3 = new UserDefinedField(NAMETYPE_ID3);
		type3.setFname(NAMETYPE_NAME3);
		type3.setFcode(NAMETYPE_CODE3);
		return Arrays.asList(type1, type2, type3);
	}

}
