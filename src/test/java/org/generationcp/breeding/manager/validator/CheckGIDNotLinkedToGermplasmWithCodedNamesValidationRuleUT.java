package org.generationcp.breeding.manager.validator;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.components.CodeNamesLocator;
import org.generationcp.middleware.manager.api.NamesDataManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by Mildo on 4/6/16.
 */
@Ignore
public class CheckGIDNotLinkedToGermplasmWithCodedNamesValidationRuleUT {

	CheckGIDNotLinkedToGermplasmWithCodedNamesValidationRule rule;
	@Mock
	private CodeNamesLocator codedNamesLocator;
	@Mock
	private NamesDataManager manager;
	@Mock
	private ContextUtil context;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		rule = new CheckGIDNotLinkedToGermplasmWithCodedNamesValidationRule(codedNamesLocator,manager,context);
//		codedCodes = Lists.newArrayList(CODE_1);
//		Project project = new Project();
//		CropType cropType = new CropType();
//		cropType.setCropName(CROP_NAME);
//		project.setCropType(cropType);
//		when(contextUtil.getProjectInContext()).thenReturn(project);
//		when(codedNameLocator.locateCodeNamesForCrop(CROP_NAME)).thenReturn(codedCodes);
	}

	@Test
	public void validationRuleFailsWhenExistARegisteredGermplasmWithACodedName() throws Exception {

	}
}
