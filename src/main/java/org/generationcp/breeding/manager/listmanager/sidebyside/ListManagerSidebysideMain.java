package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@Configurable
public class ListManagerSidebysideMain extends VerticalLayout implements InitializingBean{

    private static final long serialVersionUID = 5976245899964745758L;
	private VerticalSplitPanel vsp;
	private HorizontalSplitPanel hsp;

    @Override
    public void afterPropertiesSet() throws Exception {

		Label a = new Label("A");
		Label b = new Label("B");
		Label c = new Label("C");

		Button A = new Button();
		A.addListener(new ClickListener(){
			public void buttonClick(ClickEvent event) {
				expandTop();
			}
		});
		Button B = new Button();
		B.addListener(new ClickListener(){
			public void buttonClick(ClickEvent event) {
				expandBottom();
			}
		});
		
		System.out.println(this.getHeight());
		
		hsp = new HorizontalSplitPanel();
		hsp.setHeight("700px");
		hsp.setSplitPosition(28);
		
		vsp = new VerticalSplitPanel();
		vsp.setHeight("700px");
		expandTop();

		vsp.setFirstComponent(A);
		vsp.setSecondComponent(B);
		
		hsp.setFirstComponent(vsp);
		hsp.setSecondComponent(c);
        
        this.addComponent(hsp);
    }
    
    private void expandTop(){
    	vsp.setSplitPosition(vsp.getHeight()-33, Sizeable.UNITS_PIXELS);
    }

    private void expandBottom(){
    	vsp.setSplitPosition(33, Sizeable.UNITS_PIXELS);
    }
}
