package org.generationcp.breeding.manager.crossingmanager.settings;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@Configurable
public class CrossingSettingsNameComponent extends AbsoluteLayout implements
		BreedingManagerLayout, InternationalizableComponent,
		InitializingBean {

	public static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsNameComponent.class);
	private static final long serialVersionUID = 1887628092049615806L;
	private static final Integer MAX_LEADING_ZEROS = 10;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
	
    private Label crossNameLabel;
    private Label specifyPrefixLabel;
    private Label specifySuffixLabel;
    private Label howManyDigitsLabel; 
    private Label addSpaceLabel;
   
    private TextField prefixTextField;
    private TextField suffixTextField;
    private CheckBox sequenceNumCheckBox;
    private OptionGroup addSpaceOptionGroup;
    private Select leadingZerosSelect;
    
    private AbstractComponent[] digitsToggableComponents = new AbstractComponent[2];
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

	@Override
	public void updateLabels() {
		specifyPrefixLabel.setValue(messageSource.getMessage(Message.SPECIFY_PREFIX) + ": *");
		specifySuffixLabel.setValue(messageSource.getMessage(Message.SPECIFY_SUFFIX) + ":");
		addSpaceLabel.setValue(messageSource.getMessage(Message.ADD_SPACE_BETWEEN_PREFIX_AND_CODE) + "?");
		howManyDigitsLabel.setValue(messageSource.getMessage(Message.DIGITS));
		
        messageSource.setCaption(sequenceNumCheckBox, Message.SEQUENCE_NUMBER_SHOULD_HAVE);
	}

	@SuppressWarnings("serial")
	@Override
	public void instantiateComponents() {  
		crossNameLabel = new Label("<b>" +messageSource.getMessage(Message.CROSS_NAME).toUpperCase() 
				+ "</b>", Label.CONTENT_XHTML);
		
        sequenceNumCheckBox = new CheckBox();
        sequenceNumCheckBox.setImmediate(true);
        sequenceNumCheckBox.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                enableSpecifyLeadingZerosComponents(sequenceNumCheckBox.booleanValue());
            }
        });

        addSpaceLabel = new Label();
        addSpaceOptionGroup = new OptionGroup();
        addSpaceOptionGroup.setImmediate(true);
        addSpaceOptionGroup.addStyleName(AppConstants.CssStyles.HORIZONTAL_GROUP);
        
        specifyPrefixLabel = new Label();
        prefixTextField = new TextField();
        prefixTextField.setWidth("200px");
        
        howManyDigitsLabel = new Label();
        leadingZerosSelect = new Select();

        leadingZerosSelect.setNullSelectionAllowed(false);
        leadingZerosSelect.select(Integer.valueOf(1));
        leadingZerosSelect.setWidth("50px");
        
        specifySuffixLabel = new Label();
        suffixTextField = new TextField();
        suffixTextField.setWidth("200px");
        
	}

	@Override
	public void initializeValues() {
        digitsToggableComponents[0] = howManyDigitsLabel;
        digitsToggableComponents[1] = leadingZerosSelect;
        enableSpecifyLeadingZerosComponents(false);
        
        for (int i = 1; i <= MAX_LEADING_ZEROS; i++){
            leadingZerosSelect.addItem(Integer.valueOf(i));
        }

        // Add space option group 
        String yes = messageSource.getMessage(Message.YES);
        String no = messageSource.getMessage(Message.NO);
        addSpaceOptionGroup.addItem(yes);
        addSpaceOptionGroup.setItemCaption(yes, yes);
        addSpaceOptionGroup.addItem(no);
        addSpaceOptionGroup.setItemCaption(no, no);        
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void layoutComponents() {
		addComponent(crossNameLabel, "top:0px; left:0px");

		addComponent(specifyPrefixLabel, "top:26px;left:0px");
        addComponent(prefixTextField, "top:26px;left:145px");
        addComponent(addSpaceLabel, "top:26px;left:385px");
        addComponent(addSpaceOptionGroup, "top:26px;left:615px");
        
        addComponent(sequenceNumCheckBox, "top:57px;left:140px");
        addComponent(leadingZerosSelect, "top:55px;left:380px");
        
        addComponent(specifySuffixLabel, "top:83px;left:0px");
        addComponent(suffixTextField, "top:83px;left:145px");
	}
	
	public boolean validateCrossNameFields(){
        Window window = getWindow();
        String prefix = ((String) prefixTextField.getValue()).trim();
        
        if (StringUtils.isEmpty(prefix)){
            MessageNotifier.showError(window, messageSource.getMessage(Message.ERROR_WITH_CROSS_CODE), 
            		messageSource.getMessage(Message.ERROR_ENTER_PREFIX_FIRST), Notification.POSITION_CENTERED);
            return false;
        }
        
        return true;
    }
	
    
    private void enableSpecifyLeadingZerosComponents(boolean enabled){
        for (AbstractComponent component : digitsToggableComponents){
            component.setEnabled(enabled);
        }
    }

}
