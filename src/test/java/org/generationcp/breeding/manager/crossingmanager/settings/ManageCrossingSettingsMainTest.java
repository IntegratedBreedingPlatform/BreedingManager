package org.generationcp.breeding.manager.crossingmanager.settings;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.VerticalLayout;

public class ManageCrossingSettingsMainTest {
	
	private VerticalLayout parent;
	private ManageCrossingSettingsMain crossingSettingsMain;
	
	@Before
	public void setUp(){
		parent = new VerticalLayout();
		
		crossingSettingsMain = new ManageCrossingSettingsMain(parent);
		parent.addComponent(crossingSettingsMain);
	}
	
	@Test
	public void testReset(){
		int index;
		
		index = parent.getComponentIndex(crossingSettingsMain);
		ManageCrossingSettingsMain crossingSettingsMainBeforeReset = (ManageCrossingSettingsMain) parent.getComponent(index);
		
		crossingSettingsMain.reset();
		
		Assert.assertTrue("Expecting the parent layout contains only 1 component, ManagerCrossingSettingsMain", parent.getComponentCount() == 1);
		
		index = parent.getComponentIndex(parent.getComponent(0));
		ManageCrossingSettingsMain crossingSettingsMainAfterReset = (ManageCrossingSettingsMain) parent.getComponent(index);
		
		Assert.assertNotSame("Expected two different ManagerCrossingSettingsMain instances here but didn't", crossingSettingsMainBeforeReset,crossingSettingsMainAfterReset);
	}
	
}
