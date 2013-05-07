package org.generationcp.breeding.manager.crossingmanager;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@Configurable
public class CrossingManagerDetailsComponent extends AbsoluteLayout 
		implements InitializingBean, InternationalizableComponent, CrossesMadeContainer {
    
    private static final long serialVersionUID = 9097810121003895303L;
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerDetailsComponent.class);
    
    private CrossingManagerMain source;
    private Accordion accordion;
    
    private Label germplasmListNameLabel;
    private Label germplasmListDescriptionLabel;
    private Label germplasmListTypeLabel;
    private Label germplasmListDateLabel;
    private TextField germplasmListName;
    private TextField germplasmListDescription;
    private ComboBox germplasmListType;
    private DateField germplasmListDate;
    private Button backButton;
    private Button doneButton;

    public static final String DONE_BUTTON_ID = "done button";
    public static final String BACK_BUTTON_ID = "back button";
    public static final String DEFAULT_GERMPLASM_LIST_TYPE = "F1";

    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private CrossesMade crossesMade;
        
    
    public CrossingManagerDetailsComponent(CrossingManagerMain source, Accordion accordion){
    	this.source = source;
        this.accordion = accordion;
        
    }

	@Override
	public CrossesMade getCrossesMade() {
		return this.crossesMade;
	}


	@Override
	public void setCrossesMade(CrossesMade crossesMade) {
		this.crossesMade = crossesMade;
		
	}
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("300px");
        setWidth("800px");

        germplasmListNameLabel = new Label();
        germplasmListDescriptionLabel = new Label();
        germplasmListTypeLabel = new Label();
        germplasmListDateLabel = new Label();
        germplasmListName = new TextField();
        germplasmListDescription = new TextField();
        germplasmListType = new ComboBox();
        germplasmListDate = new DateField();
        
        backButton = new Button();
        backButton.setData(BACK_BUTTON_ID);
        doneButton = new Button();
        
        germplasmListName.setWidth("450px");
        germplasmListDescription.setWidth("450px");
        germplasmListType.setWidth("450px");
        
        addComponent(germplasmListNameLabel, "top:50px; left:30px;");
        addComponent(germplasmListDescriptionLabel, "top:80px; left:30px;");
        addComponent(germplasmListTypeLabel, "top:110px; left:30px;");
        addComponent(germplasmListDateLabel, "top:140px; left:30px;");
        
        addComponent(germplasmListName, "top:30px; left:200px;");
        addComponent(germplasmListDescription, "top:60px; left:200px;");
        addComponent(germplasmListType, "top:90px; left:200px;");
        addComponent(germplasmListDate, "top:120px; left:200px;");
        
        addComponent(backButton, "top:260px; left: 625px;");
        addComponent(doneButton, "top:260px; left: 700px;");
        
        germplasmListDate.setResolution(DateField.RESOLUTION_DAY);
		germplasmListDate.setDateFormat(CrossingManagerMain.DATE_FORMAT);

        //start DJ: GCP-3799
        //germplasmListName.setRequired(true);
        //germplasmListDescription.setRequired(true);
        doneButton.setData(DONE_BUTTON_ID);
        List<UserDefinedField> germplasmListTypes = germplasmListManager.getGermplasmListTypes();
        for(int i = 0 ; i < germplasmListTypes.size() ; i++){
            UserDefinedField userDefinedField = germplasmListTypes.get(i);
            germplasmListType.addItem(userDefinedField.getFcode());
            germplasmListType.setItemCaption(userDefinedField.getFcode(), userDefinedField.getFname());
            if(DEFAULT_GERMPLASM_LIST_TYPE.equalsIgnoreCase(userDefinedField.getFcode())){
                germplasmListType.setValue(userDefinedField.getFcode());
            }
        }
        germplasmListDate.setDateFormat("yyyy-MM-dd");
        germplasmListDate.setResolution(DateField.RESOLUTION_DAY);
        germplasmListDate.setValue(new Date());

        CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
		doneButton.addListener(listener);
		backButton.addListener(listener);
        //end DJ: GCP-3799
    }

    public void doneButtonClickAction() throws InternationalizableException{
            String nGermplasmListName = (String) germplasmListName.getValue();
            String nGermplasmListDescription= (String) germplasmListDescription.getValue();
            Date date = (Date)germplasmListDate.getValue();
            String nGermplasmListType = (String)germplasmListType.getValue();
        	if(nGermplasmListName==null || nGermplasmListName.trim().equalsIgnoreCase("")){
        		//getSource().getApplication().getMainWindow().showNotification(, Window.Notification.TYPE_WARNING_MESSAGE);
                //MessageNotifier.showWarning(this.getWindow(), "Germplasm List Name is required.", "");
                MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_NAME_REQUIRED), "");

        	} else if(nGermplasmListDescription==null || nGermplasmListDescription.trim().equalsIgnoreCase("")) {
                //getSource().getApplication().getMainWindow().showNotification("Germplasm List Description is required.", Window.Notification.TYPE_WARNING_MESSAGE);
                MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_DESCRIPTION_REQUIRED), "");
        	} else if(nGermplasmListType == null || nGermplasmListType.equalsIgnoreCase("")){
                //getSource().getApplication().getMainWindow().showNotification(, Window.Notification.TYPE_WARNING_MESSAGE);
                MessageNotifier.showWarning(this.getWindow(),  messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_TYPE_REQUIRED), "");
            } else if(date==null) {
                //getSource().getApplication().getMainWindow().showNotification("Please choose a correct date", Window.Notification.TYPE_WARNING_MESSAGE);
                MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.ERROR_GERMPLASM_LIST_DATE_REQUIRED), "");
        	}
        }

        public Accordion getAccordion() {
        	return accordion;
        }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
    	messageSource.setCaption(germplasmListNameLabel, Message.GERMPLASM_LIST_NAME);
    	messageSource.setCaption(germplasmListDescriptionLabel, Message.GERMPLASM_LIST_DESCRIPTION);
    	messageSource.setCaption(germplasmListTypeLabel, Message.GERMPLASM_LIST_TYPE);
    	messageSource.setCaption(germplasmListDateLabel, Message.GERMPLASM_LIST_DATE);
    	messageSource.setCaption(backButton, Message.BACK);
    	messageSource.setCaption(doneButton, Message.DONE);
    }

    public CrossingManagerMain getSource() {
    	return source;
    }
	
	//TODO replace with actual back button logic. 
    // For now, just displays CrossesMade information
    public void backButtonClickAction(){
    	displayCrossesMadeInformation();
    }

	private void displayCrossesMadeInformation() {
		if (crossesMade != null){
    		Map<Germplasm,Name> crossesMap = crossesMade.getCrossesMap();
    		if (crossesMap != null){
    			for (Entry<Germplasm, Name> entry : crossesMap.entrySet()){
    				System.out.println(entry.getKey() + " >>> " + entry.getValue());
    				if (crossesMade.getOldCrossNames() != null){
    					System.out.println(crossesMade.getOldCrossNames().get(entry.getKey()));
    				}
    			}
    		}

    	}
	}
}
