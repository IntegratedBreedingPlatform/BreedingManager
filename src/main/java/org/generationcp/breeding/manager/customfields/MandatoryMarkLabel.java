package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.constants.AppConstants;

import com.vaadin.ui.Label;

public class MandatoryMarkLabel extends Label {

	private static final long serialVersionUID = -3455033564724774241L;
	
	public MandatoryMarkLabel(){
		super("* ");
		setWidth("8px");
		addStyleName(AppConstants.CssStyles.MARKED_MANDATORY);
	}

}
