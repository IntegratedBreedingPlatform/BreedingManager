
package org.generationcp.breeding.manager.listimport;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.util.VaadinFileDownloadResource;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import com.vaadin.Application;
import com.vaadin.ui.Component;
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
		doReturn(this.application).when(this.exportDialog).getCurrentApplication();
		doReturn(this.request).when(this.exportDialog).getCurrentRequest();
		doReturn(mock(VaadinFileDownloadResource.class)).when(this.exportDialog)
				.getTemplateAsDownloadResource(any(File.class));

		when(this.application.getMainWindow()).thenReturn(this.window);
	}

	@Test
	@Ignore(
			value = "This test runs fine in IDE but fails on mvn commandline due to classpath issues in loading the xls file from commons. Team Manila to fix and enable soon.")
	public void testExportGermplasmTemplate() throws Exception {
		Component component = mock(Component.class);
		when(component.getWindow()).thenReturn(this.window);
		this.exportDialog.exportGermplasmTemplate(component);

		verify(this.window).open(any(VaadinFileDownloadResource.class));
	}

	@Test
	@Ignore(
			value = "This test runs fine in IDE but fails on mvn commandline due to classpath issues in loading the xls file from commons. Team Manila to fix and enable soon.")
	public void testGermplasmTemplateExists() throws Exception {
		ClassPathResource cpr = new ClassPathResource("templates/" + GermplasmListTemplateDownloader.EXPANDED_TEMPLATE_FILE);
		File templateFile = cpr.getFile();
		assert templateFile != null;

		assert templateFile.exists();

	}
}
