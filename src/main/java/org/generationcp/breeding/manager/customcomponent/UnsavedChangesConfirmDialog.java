package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class UnsavedChangesConfirmDialog extends BaseSubWindow implements InitializingBean,
								InternationalizableComponent, BreedingManagerLayout {
	private static final long serialVersionUID = -7800270790767272974L;
	
	private Label descriptionLabel; 
	private Button cancelButton;
	private Button discardButton;
	private Button saveButton;
	
	private String description;
	
	private VerticalLayout mainLayout;
	private UnsavedChangesConfirmDialogSource source;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	public UnsavedChangesConfirmDialog(UnsavedChangesConfirmDialogSource source, String description) {
		super();
		this.source = source;
		this.description = description;
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
		setModal(true);
		setCaption(messageSource.getMessage(Message.UNSAVED_CHANGES));
		setStyleName(Reindeer.WINDOW_LIGHT);
		addStyleName("unsaved-changes-dialog");
		// define window size, set as not resizable
        setWidth("544px");
        setHeight("180px");
        setResizable(false);
        // center window within the browser
		center();
		
		//content variables
		descriptionLabel = new Label("<center>" + description + "</center>",Label.CONTENT_XHTML);
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		discardButton = new Button(messageSource.getMessage(Message.DISCARD_CHANGES));
		
		saveButton = new Button(messageSource.getMessage(Message.SAVE_CHANGES));
		saveButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		cancelButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 2688256898854358066L;

			@Override
			public void buttonClick(ClickEvent event) {
				cancelAction();
			}
		});
		
		saveButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -941792327552845606L;

			@Override
			public void buttonClick(ClickEvent event) {
				saveAction();
			}
		});
		
		discardButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -5985668025701325303L;

			@Override
			public void buttonClick(ClickEvent event) {
				discardAction();
			}
		});
	}

	public void discardAction() {
		source.discardAllListChangesAction();
	}

	public void saveAction() {
		source.saveAllListChangesAction();
	}

	public void cancelAction() {
		source.cancelAllListChangesAction();
	}

	@Override
	public void layoutComponents() {
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		mainLayout.addComponent(descriptionLabel);
		
		Label forSpaceLabel = new Label();
		mainLayout.addComponent(forSpaceLabel);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(discardButton);
		buttonLayout.addComponent(saveButton);
		
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		
		addComponent(mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
}
