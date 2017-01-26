
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class HeaderLabelLayout extends HorizontalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private ThemeResource ICON;
	private Label label;
	private Button button;

	public HeaderLabelLayout(ThemeResource iCON, Label label) {
		super();
		this.ICON = iCON;
		this.label = label;
	}

	public HeaderLabelLayout(ThemeResource iCON, Label label, Button button) {
		super();
		this.button = button;
		this.label = label;
		this.ICON = iCON;
		this.button.setIcon(this.ICON);
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
		
		if (this.button != null) {
			this.button.setWidth("20px");
			this.button.setHeight("25px");
			this.button.setStyleName(BaseTheme.BUTTON_LINK);
			this.addComponent(button);
		} else {
			this.addComponent(icon);
		}
		
		this.addComponent(this.label);

		this.addStyleName("no-caption");
	}


}
