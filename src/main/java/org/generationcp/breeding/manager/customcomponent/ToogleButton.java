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
		setIcon(AppConstants.Icons.ICON_TOOGLE);
		setDescription(description);
		setStyleName(Reindeer.BUTTON_LINK);
		setWidth("28px");
	}
	
}
