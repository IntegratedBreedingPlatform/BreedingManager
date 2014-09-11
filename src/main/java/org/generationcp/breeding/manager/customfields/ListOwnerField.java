package org.generationcp.breeding.manager.customfields;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListOwnerField extends HorizontalLayout
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ListOwnerField.class);
	
	private Label captionLabel;
	private String caption;
	private Label listOwnerLabel;
	private boolean isMandatory;
	private Label mandatoryMark;
	private ListNameValidator listNameValidator;
	private boolean changed;
	
	@Autowired
    private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
    private UserDataManager userDataManager;
	
	public ListOwnerField(String caption, boolean isMandatory){
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}
	
	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		listOwnerLabel = new Label();
		listOwnerLabel.setWidth("180px");
		
		if(isMandatory){
			mandatoryMark = new MandatoryMarkLabel();
		}
	}

	@Override
	public void initializeValues() {
		setDefaultValue();
	}

	@Override
	public void addListeners() {
		listOwnerLabel.addListener(new Property.ValueChangeListener(){
            
            private static final long serialVersionUID = 2323698194362809907L;

            public void valueChange(ValueChangeEvent event) {
                changed = true;
            }
            
        });
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		
		addComponent(captionLabel);
		
		if(isMandatory){
			addComponent(mandatoryMark);
		}
		
		addComponent(listOwnerLabel);
	}
	
	public String getOwnerListName(Integer userId) {
		String username = "";
		
		try{
			User user = null;
			
			if(userId != null){
				user=userDataManager.getUserById(userId);
	        }
			else{
				int currentUser = Util.getCurrentUserLocalId(workbenchDataManager);
				user=userDataManager.getUserById(currentUser);
			}
			
			if(user != null){
				int personId=user.getPersonid();
				Person p = userDataManager.getPersonById(personId);
				
				if(p!=null){
					username = p.getFirstName() + " " + p.getMiddleName() + " " + p.getLastName();
				}else{
					username = user.getName();
				}
			}
			
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting list owner name of user with id: " + userId, ex);
		}
		
		return username;
    }

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	public Label getListOwnerLabel() {
		return listOwnerLabel;
	}

	public void setListOwnerLabel(Label listOwnerLabel) {
		this.listOwnerLabel = listOwnerLabel;
	}

	public void setValue(String listOwnerName){
		listOwnerLabel.setValue(listOwnerName);
	}
	
	public void setDefaultValue(){
		listOwnerLabel.setValue(getOwnerListName(null));
	}
	
	public void setValue(Integer userId){
		listOwnerLabel.setValue(getOwnerListName(userId));
	}

	public Object getValue(){
		return listOwnerLabel.getValue();
	}
	
	public ListNameValidator getListNameValidator() {
		return listNameValidator;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

}
