package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@Configurable
public class ListManagerSidebysideMain extends VerticalLayout implements InitializingBean{

    private static final long serialVersionUID = 5976245899964745758L;
    
    private Button toggleButton;
	private HorizontalSplitPanel splitPanel;

	private static Float EXPANDED_SPLIT_POSITION = Float.valueOf("50");
	private static Float COLLAPSED_SPLIT_POSITION = Float.valueOf("96");
	
    @Override
    public void afterPropertiesSet() throws Exception {

    	setSizeFull();
    	
    	setMargin(false);
        setSpacing(false);
    	
		Label content = new Label("Content");

		toggleButton = new Button();
		toggleButton.addListener(new ClickListener(){
			public void buttonClick(ClickEvent event) {
				if(splitPanel.getSplitPosition() == EXPANDED_SPLIT_POSITION){
					collapse();
				} else {
					expand();
				}
			}
		});
		
		splitPanel = new HorizontalSplitPanel();
		splitPanel.setSizeFull();
		splitPanel.setMargin(false);
		
		collapse();
		
		//Attach browse/search lists tabsheet here
		splitPanel.setFirstComponent(content);
		
		//Attach build new list here
		splitPanel.setSecondComponent(toggleButton);
        
		addComponent(splitPanel);

    }
    
    private void expand(){
    	splitPanel.setSplitPosition(EXPANDED_SPLIT_POSITION);
    	toggleButton.setCaption(">>");
    }

    private void collapse(){
    	splitPanel.setSplitPosition(COLLAPSED_SPLIT_POSITION);
    	toggleButton.setCaption("<<");
    }
}
