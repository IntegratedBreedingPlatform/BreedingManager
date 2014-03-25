package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SaveListAsDialog extends Window implements InitializingBean, InternationalizableComponent, BreedingManagerLayout{

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SaveListAsDialog.class);
	
	private VerticalLayout mainLayout;
	private HorizontalLayout contentLayout;
	private HorizontalLayout buttonLayout;
	
	private SaveListAsDialogSource source;
	
	private Label listLocationLabel;
	private ListManagerTreeComponent germplasmListTree;
	private Integer folderId;
	
	private BreedingManagerListDetailsComponent listDetailsComponent;

	private Button cancelButton;
	private Button saveButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	private GermplasmList germplasmList;
	
	public SaveListAsDialog(SaveListAsDialogSource source, GermplasmList germplasmList){
		this.source = source;
		this.germplasmList = germplasmList;
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
		setCaption(messageSource.getMessage(Message.SAVE_LIST_AS));
		addStyleName(Reindeer.WINDOW_LIGHT);
		setResizable(false);
		setModal(true);

		germplasmListTree = new ListManagerTreeComponent(true, folderId);
		listLocationLabel = germplasmListTree.getHeading();
		listLocationLabel.setValue(messageSource.getMessage(Message.LIST_LOCATION));
		listLocationLabel.setStyleName(Bootstrap.Typography.H6.styleName());
		germplasmListTree.setHeading(listLocationLabel);
		
		listDetailsComponent = new BreedingManagerListDetailsComponent(germplasmList);
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL_LABEL));
		cancelButton.setWidth("80px");
		
		saveButton = new Button(messageSource.getMessage(Message.SAVE_LABEL));
		saveButton.setWidth("80px");
		saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		if(germplasmList != null){
			
			GermplasmList parent = germplasmList.getParent();
			if(parent != null){ // if not "Program Lists"
				germplasmListTree.setListId(parent.getId());
				germplasmListTree.setSelectedListId(parent.getId());
			}
			
			germplasmListTree.createTree();
			
			listDetailsComponent.setGermplasmListDetails(germplasmList);

		}
	}

	@Override
	public void addListeners() {
		cancelButton.addListener(new CloseWindowAction());
		saveButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 993268331611479850L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(validateAllFields()){
					source.saveList(getGermplasmListToSave());
					
					Window window = event.getButton().getWindow();
			        window.getParent().removeWindow(window);
				}
			}
			
		});
	}

	@Override
	public void layoutComponents() {
		setWidth("625px");
		setHeight("461px");
		
		contentLayout = new HorizontalLayout();
		contentLayout.setSpacing(true);
		contentLayout.addComponent(germplasmListTree);
		contentLayout.addComponent(listDetailsComponent);
		
		buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponent(cancelButton);
		buttonLayout.addComponent(saveButton);
		
		HorizontalLayout buttonLayoutMain = new HorizontalLayout();
		buttonLayoutMain.setWidth("100%");
		buttonLayoutMain.addComponent(buttonLayout);
		buttonLayoutMain.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
		
		mainLayout = new VerticalLayout();
		mainLayout.setHeight("364px");
		mainLayout.addComponent(contentLayout);
		mainLayout.addComponent(buttonLayoutMain);
		
		addComponent(mainLayout);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	public GermplasmList getParentList(){
		Integer folderId = null;
		if(germplasmListTree.getSelectedListId() instanceof Integer){
			folderId = (Integer) germplasmListTree.getSelectedListId();
		}
		
		GermplasmList folder = null;
		if(folderId != null){
			try {
				folder = germplasmListManager.getGermplasmListById(folderId);
			} catch (MiddlewareQueryException e) {
				LOG.error("Error with retrieving list with id: " + folderId, e);
				e.printStackTrace();
			}
		}
		
		return folder;
	}
	
	public GermplasmList getGermplasmListToSave(){
		Integer currentId = null;
		if(germplasmList != null){
			currentId = germplasmList.getId();
		}
		germplasmList = listDetailsComponent.getGermplasmList();
		germplasmList.setId(currentId);
		germplasmList.setParent(getParentList());         
		germplasmList.setStatus(0);
		
        return germplasmList;
	}

	protected boolean validateAllFields() {
		
		if(!listDetailsComponent.validate()){
			return false;
		}
		
		return true;
	}
}
