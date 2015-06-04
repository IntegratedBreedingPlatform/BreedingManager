
package org.generationcp.breeding.manager.listimport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.Window;

public class GermplasmImportFileComponentTest {

	private GermplasmImportFileComponent importFileComponent;
	private GermplasmImportMain importMain;
	private Window importWindow;

	@Before
	public void setUp() {
		this.importMain = Mockito.mock(GermplasmImportMain.class);
		this.importFileComponent = new GermplasmImportFileComponent(this.importMain);

		this.importWindow = new Window();
		Mockito.doReturn(this.importWindow).when(this.importMain).getWindow();
	}

	@Test
	public void testCancelActionFromMainImportTool() {
		this.importFileComponent.cancelButtonAction();
		Mockito.verify(this.importMain).reset();
	}

	@Test
	public void testCancelActionFromListManager() {
		GermplasmImportPopupSource popupSource = Mockito.mock(GermplasmImportPopupSource.class);
		Window listManagerWindow = new Window();
		listManagerWindow.addWindow(this.importWindow);
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("List Manager Window has Germplasm Import sub-window", !listManagerWindow.getChildWindows().isEmpty());

		Mockito.doReturn(popupSource).when(this.importMain).getGermplasmImportPopupSource();
		Mockito.doReturn(this.importWindow).when(this.importMain).getComponentContainer();
		Mockito.doReturn(listManagerWindow).when(popupSource).getParentWindow();

		this.importFileComponent.cancelButtonAction();
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("Germplasm Import sub-window should have been closed", listManagerWindow.getChildWindows().isEmpty());

	}

	@Test
	public void testCancelActionFromFieldbook() {
		this.importWindow = Mockito.mock(Window.class);
		Mockito.doReturn(this.importWindow).when(this.importMain).getWindow();

		Mockito.doReturn(true).when(this.importMain).isViaPopup();

		this.importFileComponent.cancelButtonAction();
		Mockito.verify(this.importMain).reset();
		Mockito.verify(this.importWindow).executeJavaScript(GermplasmImportFileComponent.FB_CLOSE_WINDOW_JS_CALL);
	}

}
