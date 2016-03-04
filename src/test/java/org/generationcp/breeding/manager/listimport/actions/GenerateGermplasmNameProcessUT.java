package org.generationcp.breeding.manager.listimport.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenerateGermplasmNameProcessUT {

	public static final int DUMMY_GID = 3;
	public static final String DUMMY_DESIG = "DUMMY_DESIG";

	public static final String GERMPLASM_NAME = "GERMPLASM_NAME";
	public static final int ENTRY_ID = 1;
	private static final Integer DUPLICATED_ENTRY_ID = 2;

	@Mock
	NameBuilder nameBuilderMock;
	@Mock
	GermplasmBuilder germplasmBuilderMock;
	@Mock
	NameDataProvider nameDataProvider;
	@Mock
	GermplasmDataProvider germplasmDataProvider;

	GenerateGermplasmNameProcess process;

	GermplasmRegistrationContext context;

	private List<ImportedGermplasm> list;
	private ImportedGermplasm importedDuplicateGermplasm;
	private ImportedGermplasm importedGermplasm;
	Map<String, Germplasm> map = new HashMap<>();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		process = new GenerateGermplasmNameProcess(nameBuilderMock, germplasmBuilderMock);
		importedGermplasm = new ImportedGermplasm(ENTRY_ID, GERMPLASM_NAME);
		//		importedDuplicateGermplasm = new ImportedGermplasm(DUPLICATED_ENTRY_ID, GERMPLASM_NAME);
		//		list = Lists.newArrayList(importedGermplasm);
		context = new GermplasmRegistrationContext();
		context.setImportedGermplasm(importedGermplasm);

		context.setCreatedGermplasmMap(map);

	}

	@Test
	public void generateGermplasmNameWithGermplasmAndNameWhenImportedGermplasmExist() throws Exception {
		Germplasm germplasm = new Germplasm(DUMMY_GID);
		Name name = new Name();
		name.setNval(DUMMY_DESIG);
		GermplasmName expectedGermplasmName = new GermplasmName(germplasm, name);
		when(germplasmBuilderMock.build(context)).thenReturn(germplasm);
		when(nameBuilderMock.build(context)).thenReturn(name);

		GermplasmRegistrationContext resultContext = process.execute(context);

		assertThat(resultContext.getGermplasmNameObjects()).containsExactly(expectedGermplasmName);
	}

	@Test
	public void generateGermplasmNameWhenImportedGermplasmAlreadyImportedShouldNotCreateNewGermplasm() throws Exception {

		Germplasm germplasm = new Germplasm(DUMMY_GID);
		Name name = new Name();
		name.setNval(DUMMY_DESIG);
		GermplasmName expectedGermplasmName = new GermplasmName(germplasm, name);

		when(germplasmBuilderMock.build(context)).thenReturn(germplasm);
		when(nameBuilderMock.build(context)).thenReturn(name);
		map.put(DUMMY_DESIG, germplasm);
		GermplasmRegistrationContext resultContext = process.execute(context);

		assertThat(resultContext.getGermplasmNameObjects()).containsExactly(expectedGermplasmName);
		verify(nameBuilderMock).build(context.getNameDataProvider());
		verify(germplasmBuilderMock, never()).build(context.getGermplasmDataProvider());

	}

}
