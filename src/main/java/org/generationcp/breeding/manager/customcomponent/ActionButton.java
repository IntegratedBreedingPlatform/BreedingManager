
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;

@SuppressWarnings("unchecked")
@Configurable
public class ActionButton extends Button implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	public ActionButton() {
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setCaption("<span class='glyphicon glyphicon-cog' style='right: 8px'></span><span style='position:relative; right: 4px;'>ACTIONS</span>");
		this.setHtmlContentAllowed(true);
		this.addStyleName(Bootstrap.Buttons.INFO.styleName());
		this.setWidth("90px");

	}

}
