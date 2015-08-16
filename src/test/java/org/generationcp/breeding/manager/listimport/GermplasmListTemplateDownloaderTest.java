package org.generationcp.breeding.manager.listimport;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import com.vaadin.Application;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmListTemplateDownloaderTest {

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private Application application;

	@Mock
	private Window window;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private GermplasmListTemplateDownloader exportDialog = spy(new GermplasmListTemplateDownloader());

	@Before
	public void setUp() throws Exception {
		doReturn(application).when(exportDialog).getCurrentApplication() ;
		doReturn(request).when(exportDialog).getCurrentRequest();
		doReturn(mock(FileDownloadResource.class)).when(exportDialog).getTemplateAsDownloadResource(any(File.class));

		when(application.getMainWindow()).thenReturn(window);
	}

	@Test
	public void testExportGermplasmTemplate() throws Exception {
		exportDialog.exportGermplasmTemplate();

		verify(window).open(any(FileDownloadResource.class));
	}

	@Test
	public void testGermplasmTemplateExists() throws Exception {
		ClassPathResource cpr = new ClassPathResource("templates/" + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE);
		File templateFile = cpr.getFile();
		assert templateFile != null;

		assert templateFile.exists();

	}
}
