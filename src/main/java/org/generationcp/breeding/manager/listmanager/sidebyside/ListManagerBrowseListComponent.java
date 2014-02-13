package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListManagerBrowseListComponent extends AbsoluteLayout implements
	InternationalizableComponent, InitializingBean {

	private static final long serialVersionUID = 1L;

	@Override
	public void afterPropertiesSet() throws Exception {
		addComponent(new Label("This is the Browse List Component Pane"));
		
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}
