package org.generationcp.breeding.manager.customfields;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListTypeField extends HorizontalLayout
implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 4506866031376540836L;
	private final static Logger LOG = LoggerFactory.getLogger(ListTypeField.class);

	private Label captionLabel;
	private String caption;
	private ComboBox listTypeComboBox;
	private boolean isMandatory;
	private Label mandatoryMark;
	private final String DEFAULT_LIST_TYPE = "LST"; 
	private boolean changed;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	public ListTypeField(String caption, boolean isMandatory){
		this.caption = caption + ": ";
		this.isMandatory = isMandatory;
		this.changed = false;
	}
	@Override
	public void instantiateComponents() {
		captionLabel = new Label(caption);
		captionLabel.addStyleName("bold");
		
		listTypeComboBox = new ComboBox();
		listTypeComboBox.setWidth("180px");
		listTypeComboBox.setImmediate(true);
		
		if(isMandatory){
			mandatoryMark = new MandatoryMarkLabel();
			
			listTypeComboBox.setNullSelectionAllowed(false);
			listTypeComboBox.setRequired(true);
			listTypeComboBox.setRequiredError("Please specify the type of the list.");
		}
	}

	@Override
	public void initializeValues() {
		try {
			// initialize List Type ComboBox
			populateListType(listTypeComboBox);
		} catch (MiddlewareQueryException e) {
			LOG.error("Error in retrieving List Type", e);
			e.printStackTrace();
		}
	}
	
	private void populateListType(ComboBox selectType) throws MiddlewareQueryException {
        List<UserDefinedField> listTypes = this.germplasmListManager.getGermplasmListTypes();
        
        for (UserDefinedField listType : listTypes) {
            String typeCode = listType.getFcode();
            if (!AppConstants.DB.FOLDER.equals(typeCode)){
            	selectType.addItem(typeCode);
            	selectType.setItemCaption(typeCode, listType.getFname());
            	//set "Germplasm List" as the default value
            	if (DEFAULT_LIST_TYPE.equals(typeCode)) {
            		selectType.setValue(typeCode);
            	}
            }
        }
    }

	@Override
	public void addListeners() {
		listTypeComboBox.addListener(new Property.ValueChangeListener(){
            
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
		
		addComponent(listTypeComboBox);
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
	
	public ComboBox getListTypeComboBox() {
		return listTypeComboBox;
	}
	
	public void setListTypeComboBox(ComboBox listTypeComboBox) {
		this.listTypeComboBox = listTypeComboBox;
	}
	
	public void setValue(String value){
		listTypeComboBox.select(value);
	}
	
	public String getValue(){
		return (String)listTypeComboBox.getValue();
	}
	
	public String getDEFAULT_LIST_TYPE() {
		return DEFAULT_LIST_TYPE;
	}
	
	public void validate() throws InvalidValueException {
		listTypeComboBox.validate();
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
}
