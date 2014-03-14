package org.generationcp.breeding.manager.listmanager.dialog;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectLocationFolderDialog extends Window implements InitializingBean, InternationalizableComponent, BreedingManagerLayout{
	private static final long serialVersionUID = -5502264917037916149L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SelectLocationFolderDialog.class);

	private SelectLocationFolderDialogSource source;
	private ListManagerTreeComponent germplasmListTree;
	
	private Button cancelButton;
	private Button selectLocationButton;
	
	private Integer folderId;
	
	private Label listNameLabel;
	private TextField listNameTxtField;
	
	private String windowLabel;
	private String selectBtnCaption;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	private Boolean fromMakeCrosses;
	
	public SelectLocationFolderDialog(SelectLocationFolderDialogSource source, Integer folderId){
		this.source = source;
		this.folderId = folderId;
	}
	
	public SelectLocationFolderDialog(SelectLocationFolderDialogSource source, Integer folderId, Boolean fromMakeCrosses){
		this.source = source;
		this.folderId = folderId;
		this.fromMakeCrosses = fromMakeCrosses;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}

	public TextField getListNameTxtField() {
		return listNameTxtField;
	}

	public void setListNameTxtField(String value) {
		this.listNameTxtField.setValue(value);
	}

	@Override
	public void instantiateComponents() {
		if(fromMakeCrosses){
			this.windowLabel = messageSource.getMessage(Message.SAVE_LIST_AS);
			this.selectBtnCaption = messageSource.getMessage(Message.SELECT);
			
			listNameLabel = new Label(messageSource.getMessage(Message.LIST_NAME) + ":*");
			listNameTxtField = new TextField();
			listNameTxtField.setImmediate(true);
			listNameTxtField.setRequired(true);
			listNameTxtField.setRequiredError("Please specify the name of the list.");
			listNameTxtField.addValidator(new StringLengthValidator(
	                "List Description must not exceed 255 characters.", 1, 100, false));
			listNameTxtField.addValidator(new ListNameValidator());
		}
		else{
			this.windowLabel = messageSource.getMessage(Message.SELECT_LOCATION_FOLDER);
			this.selectBtnCaption = messageSource.getMessage(Message.SELECT_LOCATION);
		}
		
		setCaption(windowLabel);
		addStyleName(Reindeer.WINDOW_LIGHT);
		setResizable(false);
		setModal(true);
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		
		selectLocationButton = new Button(selectBtnCaption);
		selectLocationButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
				
		germplasmListTree = new ListManagerTreeComponent(true, folderId); 
				
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		cancelButton.addListener(new CloseWindowAction());
		
		selectLocationButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -4029658156820141206L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				if(validateListName()){
					Integer folderId = null;
					if(germplasmListTree.getSelectedListId() instanceof Integer){
						folderId = (Integer) germplasmListTree.getSelectedListId();
					}
					try{
						if(folderId != null){
							GermplasmList folder = germplasmListManager.getGermplasmListById(folderId);
							source.setSelectedFolder(folder);
						} else{
							source.setSelectedFolder(null);
						}
						
						if(fromMakeCrosses){
							source.setListName(getListNameTxtField().getValue().toString());
						}
							
						Window window = event.getButton().getWindow();
				        window.getParent().removeWindow(window);
					} catch(MiddlewareQueryException ex){
						LOG.error("Error with retrieving list with id: " + folderId, ex);
					}

				}
				
			}
		});

	}

	@Override
	public void layoutComponents() {
		if(fromMakeCrosses){
			setHeight("480px");
			setWidth("280px");
		}
		else{
			setHeight("380px");
			setWidth("250px");
		}
		
		HorizontalLayout listNameLayout = new HorizontalLayout();
		listNameLayout.addComponent(listNameLabel);
		listNameLayout.addComponent(listNameTxtField);
		
		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setSpacing(true);
		buttonBar.setMargin(true);
		buttonBar.addComponent(cancelButton);
		buttonBar.addComponent(selectLocationButton);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(buttonBar);
		buttonLayout.setComponentAlignment(buttonBar, Alignment.MIDDLE_CENTER);

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainLayout.addComponent(germplasmListTree);
		if(fromMakeCrosses){ mainLayout.addComponent(listNameLayout); }
		mainLayout.addComponent(buttonLayout);
		
		setContent(mainLayout);
	}
	
	public boolean validateListName(){
		if(listNameTxtField != null){
			try {
				
				listNameTxtField.validate();
				return true;
				
			} catch (InvalidValueException e) {
				MessageNotifier.showError(getWindow(), 
						this.messageSource.getMessage(Message.INVALID_INPUT), 
						e.getMessage(), Notification.POSITION_CENTERED);
				return false;
			}
		}
		
		return true;
	}
	
}
