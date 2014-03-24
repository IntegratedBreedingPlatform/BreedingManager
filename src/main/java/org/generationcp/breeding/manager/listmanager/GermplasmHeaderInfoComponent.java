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
	
	private Label lblGid;
	private Label lblCreationMethod;
	private Label lblLocation;
	private Label lblPreferredName;
	private Label lblCreationDate;
	private Label lblReference;
	
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

		setRows(3);
        setColumns(4);
        setColumnExpandRatio(1, 2);
        setColumnExpandRatio(3, 2);
        setSpacing(true);
        setMargin(true);
        
		initializeLabels();

        //1st row
        addComponent(lblGid, 0, 0);
        addComponent(gid, 1, 0);
        addComponent(lblPreferredName, 2, 0);
        addComponent(prefName, 3, 0);
        
        //2nd row
        addComponent(lblCreationMethod, 0, 1);
        addComponent(creationMethod, 1, 1);
        addComponent(lblCreationDate, 2, 1);
        addComponent(creationDate, 3, 1);
        
        //3rd row
        addComponent(lblLocation, 0, 2);
        addComponent(location, 1, 2);
        addComponent(lblReference, 2, 2);
        addComponent(reference, 3, 2);
    }

	private void initializeLabels() {
		lblGid = new Label( "<b>" + messageSource.getMessage(Message.LISTDATA_GID_HEADER) + ":</b> ", Label.CONTENT_XHTML);
		lblCreationMethod = new Label( "<b>" + messageSource.getMessage(Message.CREATION_METHOD) + ":</b> ", Label.CONTENT_XHTML); 
		lblLocation = new Label( "<b>" + messageSource.getMessage(Message.LOCATION) + ":</b> ", Label.CONTENT_XHTML); 
		lblPreferredName = new Label( "<b>" + messageSource.getMessage(Message.PREFERRED_NAME) + ":</b> ", Label.CONTENT_XHTML); 
		lblCreationDate = new Label( "<b>" + messageSource.getMessage(Message.CREATION_DATE_LABEL) + ":</b> ", Label.CONTENT_XHTML); 
		lblReference = new Label( "<b>" + messageSource.getMessage(Message.REFERENCE) + ":</b> ", Label.CONTENT_XHTML); 
		
		gid = new Label(String.valueOf(gDetailModel.getGid()));
        prefName = new Label(gDetailModel.getGermplasmPreferredName());
        location = new Label( gDetailModel.getGermplasmLocation());
        creationMethod = new Label(gDetailModel.getGermplasmMethod());
        creationDate = new Label(!gDetailModel.getGermplasmCreationDate().equals("0") ? gDetailModel.getGermplasmCreationDate() : "-" );
        reference = new Label(String.valueOf( gDetailModel.getReference()));
	}

}
