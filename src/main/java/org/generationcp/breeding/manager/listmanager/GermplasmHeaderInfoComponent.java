package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

@Configurable
public class GermplasmHeaderInfoComponent extends GridLayout implements
		InitializingBean, InternationalizableComponent {

	@SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmHeaderInfoComponent.class);
	private static final long serialVersionUID = -4916820997495310249L;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private Label gid;
	private Label creationMethod;
	private Label location;
	private Label prefName;
	private Label creationDate;
	private Label reference;

	private GermplasmDetailModel gDetailModel;
	
	public GermplasmHeaderInfoComponent(GermplasmDetailModel germplasmDetailModel){
		this.gDetailModel = germplasmDetailModel;
	}
	
	@Override
	public void updateLabels() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		addStyleName("overflow_x_auto");

		setRows(2);
        setColumns(3);
        setSpacing(true);
        setMargin(true);
        
		initializeLabels();

        //1st row
		addComponent(prefName, 0, 0);
		addComponent(creationDate, 1, 0);
        addComponent(gid, 2, 0);
        
        //2nd row
        addComponent(creationMethod, 0, 1);
        addComponent(location, 1, 1);
        addComponent(reference, 2, 1);
    }

	private void initializeLabels() {
        gid = new Label("<b>" + messageSource.getMessage(Message.LISTDATA_GID_HEADER) + ":</b> "
                + String.valueOf(gDetailModel.getGid()), Label.CONTENT_XHTML);
        
        prefName = new Label("<b>" + messageSource.getMessage(Message.PREFERRED_NAME) + ":</b> "
                + gDetailModel.getGermplasmPreferredName(), Label.CONTENT_XHTML);
        
        location = new Label("<b>" + messageSource.getMessage(Message.LOCATION) + ":</b> " 
                + gDetailModel.getGermplasmLocation(), Label.CONTENT_XHTML);
        
        creationMethod = new Label("<b>" + messageSource.getMessage(Message.CREATION_METHOD) + ":</b> "
                + gDetailModel.getGermplasmMethod(), Label.CONTENT_XHTML);
        
        creationDate = new Label("<b>" + messageSource.getMessage(Message.CREATION_DATE_LABEL) + ":</b> "
                + (!gDetailModel.getGermplasmCreationDate().equals("0") ? gDetailModel.getGermplasmCreationDate() : "-") 
                , Label.CONTENT_XHTML);
        
        reference = new Label("<b>" + messageSource.getMessage(Message.REFERENCE) + ":</b> "
                + String.valueOf( gDetailModel.getReference()), Label.CONTENT_XHTML);
	}

}