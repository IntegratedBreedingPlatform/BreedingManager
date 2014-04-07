package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
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
		ICON = iCON;
		this.label = label;
	}
	
	public ThemeResource getICON() {
		return ICON;
	}
	public void setICON(ThemeResource iCON) {
		ICON = iCON;
	}
	public Label getLabel() {
		return label;
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
		Embedded icon = new Embedded("",ICON);
		
		setHeight("30px");
		setSpacing(true);
		addComponent(icon);
		addComponent(label);
		
		this.addStyleName("headerLabelLayout");
		this.setComponentAlignment(icon, Alignment.TOP_LEFT);
		this.setComponentAlignment(label, Alignment.TOP_LEFT);
	}
	
	
}
