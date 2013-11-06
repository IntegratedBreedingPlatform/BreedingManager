package org.generationcp.breeding.manager.listmanager;

import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.generationcp.breeding.manager.application.Message;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListManagerBrowseListsComponent extends AbsoluteLayout implements
		InternationalizableComponent, InitializingBean {

	private static final long serialVersionUID = -224052511814636864L;
	private Label heading;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		heading = new Label();
		heading.setValue(messageSource.getMessage(Message.BROWSE_LISTS));
		heading.addStyleName("gcp-content-title");
		
		ListManagerTreeComponent listManagerTreeComponent = new ListManagerTreeComponent();
		
		addComponent(heading,"top:30px; left:20px;");
		addComponent(listManagerTreeComponent, "top:55px; left:20px");
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}

}
