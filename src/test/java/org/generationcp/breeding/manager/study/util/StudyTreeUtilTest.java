
package org.generationcp.breeding.manager.study.util;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.exception.GermplasmStudyBrowserException;
import org.generationcp.breeding.manager.study.StudyTreeComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

public class StudyTreeUtilTest {

	private static final String ROOT_FOLDER_NAME = "Nurseries and Trials";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private StudyTreeComponent component;

	@Mock
	private StudyDataManager studyDataManager;

	private StudyTreeUtil util;

	private static final String PROGRAM_UUID = "123456789";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.component.getWindow()).thenReturn(Mockito.mock(Window.class));

		this.util = Mockito.spy(new StudyTreeUtil(Mockito.mock(Tree.class), this.component));
		this.util.setMessageSource(this.messageSource);
		this.util.setStudyDataManager(this.studyDataManager);
	}

	@Test
	public void testSetParentIfSourceIsRootFolderShouldReturnFalse() {
		boolean response = this.util.setParent(StudyTreeComponent.STUDY_ROOT_NODE, null, true);
		Assert.assertFalse("Should return false since the folder being move is the main root folder", response);
	}

	@Test
	public void testSetParentIfSourceIsNotRootFolderButHaveChildShouldReturnFalse() {
		Mockito.when(this.component.hasChildStudy(Matchers.anyInt())).thenReturn(true);
		boolean response = this.util.setParent(new Integer(2), null, true);
		Assert.assertFalse("Should return false since the folder being move has child folder", response);
	}

	@Test
	public void testSetParentIfSourceIsNotRootFolderAndValidTargetFolderReturnTrue() {
		Mockito.when(this.component.hasChildStudy(Matchers.anyInt())).thenReturn(false);
		boolean response = this.util.setParent(new Integer(2), new Integer(3), true);
		Assert.assertTrue("Should return true since the folder being move and target folder are both valid", response);
	}

	@Test
	public void testValidateForDeleteNurseryListIsNotExistingShouldThrowError() throws MiddlewareQueryException {
		Mockito.when(this.studyDataManager.getProject(Matchers.anyInt())).thenReturn(null);
		Integer id = 1;
		boolean throwsException = false;
		try {
			this.util.validateForDeleteNurseryList(id);
		} catch (GermplasmStudyBrowserException e) {
			throwsException = true;
		}
		Assert.assertTrue("Should throw exception since project is not existing", throwsException);
	}

	@Test
	public void testValidateForDeleteNurseryListIsExistingAndIsNotFolderShouldThrowError() throws MiddlewareQueryException {
		Mockito.when(this.studyDataManager.getProject(Matchers.anyInt())).thenReturn(Mockito.mock(DmsProject.class));
		Mockito.when(this.component.isFolder(Matchers.anyInt())).thenReturn(false);
		Integer id = 1;
		boolean throwsException = false;
		try {
			this.util.validateForDeleteNurseryList(id);
		} catch (GermplasmStudyBrowserException e) {
			throwsException = true;
		}
		Assert.assertTrue("Should throw exception since project is not folder", throwsException);
	}

	@Test
	public void testValidateForDeleteNurseryListIsExistingAndIsFolderShouldNotThrowError() throws MiddlewareQueryException {
		Mockito.when(this.studyDataManager.getProject(Matchers.anyInt())).thenReturn(Mockito.mock(DmsProject.class));
		Mockito.when(this.component.isFolder(Matchers.anyInt())).thenReturn(true);
		Mockito.when(this.component.hasChildStudy(Matchers.anyInt())).thenReturn(false);
		Integer id = 1;
		boolean throwsException = false;
		try {
			this.util.validateForDeleteNurseryList(id);
		} catch (GermplasmStudyBrowserException e) {
			throwsException = true;
		}
		Assert.assertFalse("Should not throw exception since project is a folder", throwsException);
	}

	@Test
	public void testValidateForDeleteNurseryListIsExistingAndIsFolderAndHasStudyShouldThrowError() throws MiddlewareQueryException {
		Mockito.when(this.studyDataManager.getProject(Matchers.anyInt())).thenReturn(Mockito.mock(DmsProject.class));
		Mockito.when(this.component.isFolder(Matchers.anyInt())).thenReturn(true);
		Mockito.when(this.component.hasChildStudy(Matchers.anyInt())).thenReturn(true);
		Integer id = 1;
		boolean throwsException = false;
		try {
			this.util.validateForDeleteNurseryList(id);
		} catch (GermplasmStudyBrowserException e) {
			throwsException = true;
		}
		Assert.assertTrue("Should throw exception since project is a folder with child studies", throwsException);
	}

	@Test
	public void testIsValidNameInputReturnsFalseforEmptyString() throws MiddlewareQueryException {
		Assert.assertFalse("Expected to return false for empty string.", this.util.isValidNameInput("", StudyTreeUtilTest.PROGRAM_UUID));
	}

	@Test
	public void testIsValidNameInputReturnsFalseforStudyNameWithLongNames() throws MiddlewareQueryException {
		String studyName =
				"Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium,"
						+ " totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. "
						+ "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos "
						+ "qui ratione voluptatem sequi nesciunt.";
		Assert.assertFalse("Expected to return false for study name length > 255.",
				this.util.isValidNameInput(studyName, StudyTreeUtilTest.PROGRAM_UUID));
	}

	@Test
	public void testIsValidNameInputReturnsFalseforExistingStudyName() throws MiddlewareQueryException {
		String itemName = "Sample Folder Name";
		Mockito.when(this.studyDataManager.checkIfProjectNameIsExistingInProgram(itemName, StudyTreeUtilTest.PROGRAM_UUID))
				.thenReturn(true);
		Assert.assertFalse("Expected to return false for existing study name",
				this.util.isValidNameInput(itemName, StudyTreeUtilTest.PROGRAM_UUID));
	}

	@Test
	public void testIsValidNameInputReturnsFalseforUsingRootFolderName() throws MiddlewareQueryException {
		String itemName = StudyTreeUtilTest.ROOT_FOLDER_NAME;
		Mockito.when(this.messageSource.getMessage(Message.NURSERIES_AND_TRIALS)).thenReturn(StudyTreeUtilTest.ROOT_FOLDER_NAME);
		Assert.assertFalse("Expected to return false for using root folder name.",
				this.util.isValidNameInput(itemName, StudyTreeUtilTest.PROGRAM_UUID));
	}
}
