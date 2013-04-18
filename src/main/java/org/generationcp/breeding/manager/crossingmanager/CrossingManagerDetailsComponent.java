package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;

@Configurable
public class CrossingManagerDetailsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerDetailsComponent.class);
    
    private CrossingManagerMain source;
    private Accordion accordion;
    
    public CrossingManagerDetailsComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
    }
    
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("300px");
        setWidth("800px");
        
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {

    }

    public CrossingManagerMain getSource() {
    	return source;
    }
}
