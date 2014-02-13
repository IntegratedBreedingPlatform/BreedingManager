package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListManagerSidebysideMain extends VerticalLayout implements InitializingBean{

    private static final long serialVersionUID = 5976245899964745758L;

    @Override
    public void afterPropertiesSet() throws Exception {
        HorizontalSplitPanel horiSplitPanel = new HorizontalSplitPanel();
        horiSplitPanel.setSplitPosition(50); // percent
        
        // left component:
        horiSplitPanel.addComponent(new Label("Left side"));

        // right component:
        horiSplitPanel.addComponent(new Label("Right side"));
        
        this.addComponent(horiSplitPanel);
    }

}
