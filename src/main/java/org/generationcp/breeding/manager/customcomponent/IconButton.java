
package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

public class IconButton extends Button {

	private static final long serialVersionUID = 1L;

	String description;
	String icon;

	public IconButton(String icon, String description) {
		this.icon = icon;
		this.description = description;

		this.setHtmlContentAllowed(true);
		this.setCaption(icon);
		this.setDescription(description);
		this.setStyleName(BaseTheme.BUTTON_LINK);
		this.setWidth("25px");
		this.setHeight("25px");
	}

}
