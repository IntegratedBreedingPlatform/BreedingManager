package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.Reindeer;

public class IconButton extends Button {
	
	private static final long serialVersionUID = 1L;
	
	String description;
	String icon;
	
	public IconButton(String icon, String description){
		this.icon = icon;
		this.description = description;
		
		setHtmlContentAllowed(true);
		setCaption(icon);
		setDescription(description);
		setStyleName(Reindeer.BUTTON_LINK);
		setWidth("25px");
		setHeight("25px");
	}

}
