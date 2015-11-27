
package org.generationcp.breeding.manager.listimport.actions;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.generationcp.breeding.manager.data.initializer.ImportedGermplasmListDataInitializer;
import org.generationcp.breeding.manager.listimport.GermplasmFieldsComponent;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.ComboBox;

@RunWith(MockitoJUnitRunner.class)
public class ProcessImportedGermplasmActionTest {

	@Mock
	private SpecifyGermplasmDetailsComponent germplasmDetailsComponent;

	@Mock
	private GermplasmFieldsComponent germplasmFieldsComponent;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	private ProcessImportedGermplasmAction processImportedGermplasmAction;

	final static Integer IBDB_USER_ID = 1;
	final static Integer DATE_INT_VALUE = 20151105;

	@Before
	public void setUp() {
		Mockito.doReturn(this.germplasmFieldsComponent).when(this.germplasmDetailsComponent).getGermplasmFieldsComponent();
		final ComboBox locationComboBox = new ComboBox();
		locationComboBox.addItem("1");
		Mockito.doReturn(locationComboBox).when(this.germplasmFieldsComponent).getLocationComboBox();

		final ComboBox methodComboBox = new ComboBox();
		methodComboBox.addItem("1");
		Mockito.doReturn(methodComboBox).when(this.germplasmFieldsComponent).getBreedingMethodComboBox();

		this.processImportedGermplasmAction.setGermplasmDataManager(this.germplasmDataManager);
	}

	@Test
	public void testUpdateGidWhenGermplasmIdIsExisting() throws MiddlewareQueryException {
		final int gid = 100;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 1;
		final boolean searchByNameOrNewGermplasmIsNeeded = true;
		Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(0);

		Mockito.doReturn(true).when(this.germplasmDetailsComponent).automaticallyAcceptSingleMatchesCheckbox();

		final List<Germplasm> germplasms = new ArrayList<Germplasm>();
		germplasms.add(GermplasmTestDataInitializer.createGermplasm(gid));

		Mockito.doReturn(germplasms).when(this.germplasmDataManager)
				.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);

		germplasm =
				this.processImportedGermplasmAction.updateGidForSingleMatch(IBDB_USER_ID, this.DATE_INT_VALUE, importedGermplasm,
						germplasmMatchesCount, germplasm, searchByNameOrNewGermplasmIsNeeded);

		Assert.assertEquals("Expecting that the gid set is from the existing germplasm.", gid, germplasm.getGid().intValue());
	}

	@Test
	public void testUpdateGidWhenNoGermplasmIdIsExisting() throws MiddlewareQueryException {
		final int gid = 0;
		final ImportedGermplasm importedGermplasm = ImportedGermplasmListDataInitializer.createImportedGermplasm(gid);
		importedGermplasm.setDesig("Name" + gid);

		final int germplasmMatchesCount = 0;
		final boolean searchByNameOrNewGermplasmIsNeeded = true;
		Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(0);

		
		try {
			germplasm =
					this.processImportedGermplasmAction.updateGidForSingleMatch(IBDB_USER_ID, this.DATE_INT_VALUE, importedGermplasm,
							germplasmMatchesCount, germplasm, searchByNameOrNewGermplasmIsNeeded);

			Mockito.verify(this.germplasmDetailsComponent, Mockito.times(0)).automaticallyAcceptSingleMatchesCheckbox();
			Mockito.verify(this.germplasmDataManager, Mockito.times(0)).getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
		} catch (Exception e) {
			Assert.fail();
		}
		Assert.assertEquals("Expecting that the gid is set to 0 when there is no existing germplasm.", 0, germplasm.getGid().intValue());
	}
}