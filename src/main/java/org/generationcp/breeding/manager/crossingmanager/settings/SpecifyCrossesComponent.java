package org.generationcp.breeding.manager.crossingmanager.settings;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;

@Configurable
public class SpecifyCrossesComponent extends AbsoluteLayout implements BreedingManagerLayout,
		InitializingBean, InternationalizableComponent {
	
	private static final long serialVersionUID = 7012558460476168852L;


	public enum SpecifyCrossesOption {
		SPECIFY_MANUALLY, UPLOAD_CROSSES
	}
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	private Label specifyCrossesLabel;
	private Label howToSpecifyCrossesLabel;
	private OptionGroup specifyCrossesOptionGroup;
	private Label fileSelectedLabel;
	private Button browseButton;

	@Override
	public void attach() {
		super.attach();
		updateLabels();
	}
	
	@Override
	public void updateLabels() {
		howToSpecifyCrossesLabel.setValue(messageSource.getMessage(Message.HOW_WOULD_YOU_LIKE_TO_SPECIFY_CROSSES));
		fileSelectedLabel.setValue(messageSource.getMessage(Message.NO_FILE_SELECTED));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();

	}

	@Override
	public void instantiateComponents() {
		specifyCrossesLabel =  new Label("<b>" +messageSource.getMessage(Message.SPECIFYING_CROSSES) 
				+ "</b>", Label.CONTENT_XHTML);
		specifyCrossesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		howToSpecifyCrossesLabel = new Label();
		
		specifyCrossesOptionGroup = new OptionGroup();
		specifyCrossesOptionGroup.setImmediate(true);
		specifyCrossesOptionGroup.addStyleName(AppConstants.CssStyles.HORIZONTAL_GROUP);
		
		browseButton = new Button(messageSource.getMessage(Message.BROWSE));
		
		fileSelectedLabel = new Label();
	}

	@Override
	public void initializeValues() {
		specifyCrossesOptionGroup.addItem(SpecifyCrossesOption.SPECIFY_MANUALLY);
		specifyCrossesOptionGroup.setItemCaption(SpecifyCrossesOption.SPECIFY_MANUALLY, messageSource.getMessage(Message.SPECIFY_CROSSES_MANUALLY));
		specifyCrossesOptionGroup.addItem(SpecifyCrossesOption.UPLOAD_CROSSES);
		specifyCrossesOptionGroup.setItemCaption(SpecifyCrossesOption.UPLOAD_CROSSES, messageSource.getMessage(Message.UPLOAD_LIST_OF_CROSSES));
		specifyCrossesOptionGroup.select(SpecifyCrossesOption.SPECIFY_MANUALLY);
	}

	@Override
	public void addListeners() {
	}
	

	@Override
	public void layoutComponents() {
		addComponent(specifyCrossesLabel, "top:0px; left:0px");
		
		addComponent(howToSpecifyCrossesLabel, "top:30px; left:0px");
		
		addComponent(specifyCrossesOptionGroup, "top:52px; left:0px");
		
		addComponent(browseButton, "top:52px; left:340px");
		addComponent(fileSelectedLabel, "top:55px; left:425px");
	}

}
