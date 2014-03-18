package org.generationcp.breeding.manager.listmanager.dialog;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListNameField;
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
import com.vaadin.ui.HorizontalLayout;
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
	
	private String windowLabel;
	private String selectBtnCaption;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	public SelectLocationFolderDialog(SelectLocationFolderDialogSource source, Integer folderId){
		this.source = source;
		this.folderId = folderId;
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

	@Override
	public void instantiateComponents() {
		
		this.windowLabel = messageSource.getMessage(Message.SELECT_LOCATION_FOLDER);
		this.selectBtnCaption = messageSource.getMessage(Message.SELECT_LOCATION);
		
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
						
					Window window = event.getButton().getWindow();
			        window.getParent().removeWindow(window);
				} catch(MiddlewareQueryException ex){
					LOG.error("Error with retrieving list with id: " + folderId, ex);
				}
			}
		});

	}

	@Override
	public void layoutComponents() {

		setHeight("380px");
		setWidth("250px");

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
		mainLayout.addComponent(buttonLayout);
		
		setContent(mainLayout);
	}
	
}
