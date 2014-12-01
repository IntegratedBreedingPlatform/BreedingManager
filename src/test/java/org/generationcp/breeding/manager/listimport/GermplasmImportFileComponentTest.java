package org.generationcp.breeding.manager.listimport;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Window;

public class GermplasmImportFileComponentTest {
	
	private GermplasmImportFileComponent importFileComponent;
	private GermplasmImportMain importMain;
	private Window importWindow;
	
	@Before
	public void setUp(){
		importMain = mock(GermplasmImportMain.class);
		importFileComponent = new GermplasmImportFileComponent(importMain);
		
		importWindow = new Window();
		doReturn(importWindow).when(importMain).getWindow();
	}

	@Test
	public void testCancelActionFromMainImportTool(){
		importFileComponent.cancelButtonAction();
		verify(importMain).reset();
	}
	
	@Test
	public void testCancelActionFromListManager(){
		GermplasmImportPopupSource popupSource = mock(GermplasmImportPopupSource.class);
		Window listManagerWindow = new Window();
		listManagerWindow.addWindow(importWindow);
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("List Manager Window has Germplasm Import sub-window", !listManagerWindow.getChildWindows().isEmpty());

		doReturn(popupSource).when(importMain).getGermplasmImportPopupSource();
		doReturn(importWindow).when(importMain).getComponentContainer();
		doReturn(listManagerWindow).when(popupSource).getParentWindow();
		
		importFileComponent.cancelButtonAction();
		Assert.assertNotNull(listManagerWindow.getChildWindows());
		Assert.assertTrue("Germplasm Import sub-window should have been closed", listManagerWindow.getChildWindows().isEmpty());

	}
	
	@Test
	public void testCancelActionFromFieldbook(){
		importWindow = mock(Window.class);
		doReturn(importWindow).when(importMain).getWindow();
		
		doReturn(true).when(importMain).isViaPopup();
		
		importFileComponent.cancelButtonAction();
		verify(importMain).reset();
		verify(importWindow).executeJavaScript(GermplasmImportFileComponent.FB_CLOSE_WINDOW_JS_CALL);
	}
	

}
