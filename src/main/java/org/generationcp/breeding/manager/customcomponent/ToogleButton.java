package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ToogleButton  extends Button implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
	
	String description;
	
	public ToogleButton(String description){
		this.description = description;
	}
		
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setCaption("<span class='fa fa-bars' style='left: 2px; color: #717171;font-size: 18px; font-weight: bold;'></span>");
        setHtmlContentAllowed(true);
		setDescription(description);
		setStyleName(Reindeer.BUTTON_LINK);
		setWidth("25px");
		setHeight("30px");
	}
	
}
