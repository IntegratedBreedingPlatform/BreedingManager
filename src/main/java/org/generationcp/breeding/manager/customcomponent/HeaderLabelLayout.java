
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class HeaderLabelLayout extends HorizontalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private ThemeResource ICON;
	private Label label;

	public HeaderLabelLayout(ThemeResource iCON, Label label) {
		super();
		this.ICON = iCON;
		this.label = label;
	}

	public ThemeResource getICON() {
		return this.ICON;
	}

	public void setICON(ThemeResource iCON) {
		this.ICON = iCON;
	}

	public Label getLabel() {
		return this.label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Embedded icon = new Embedded("", this.ICON);
		icon.setDebugId("icon");

		this.setHeight("25px");
		this.setSpacing(false);
		this.addComponent(icon);
		this.addComponent(this.label);

		this.addStyleName("no-caption");
	}

}
