
package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.constants.AppConstants;

import com.vaadin.ui.Label;

public class MandatoryMarkLabel extends Label {

	private static final long serialVersionUID = -3455033564724774241L;

	public MandatoryMarkLabel() {
		super("* ");
		this.setWidth("8px");
		this.addStyleName(AppConstants.CssStyles.MARKED_MANDATORY);
	}

}
