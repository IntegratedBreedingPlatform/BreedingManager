package org.generationcp.breeding.manager.listmanager.dialog;

import org.generationcp.breeding.manager.application.Message;
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

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SelectLocationFolderDialog extends Window implements InitializingBean, InternationalizableComponent{
	private static final long serialVersionUID = -5502264917037916149L;
	
	private static final Logger LOG = LoggerFactory.getLogger(SelectLocationFolderDialog.class);

	private SelectLocationFolderDialogSource source;
	private ListManagerTreeComponent germplasmListTree;
	
	private Button cancelButton;
	private Button selectLocationButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	public SelectLocationFolderDialog(SelectLocationFolderDialogSource source){
		this.source = source;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeComponents();
		initializeLayout();
	}
	
	private void initializeComponents(){
		setCaption("Select Location Folder");
		addStyleName(Reindeer.WINDOW_LIGHT);
		setResizable(false);
		setModal(true);
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		cancelButton.addListener(new CloseWindowAction());
		
		selectLocationButton = new Button("Select Location");
		selectLocationButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		selectLocationButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -4029658156820141206L;

			@Override
			public void buttonClick(ClickEvent event) {
				Integer folderId = (Integer) germplasmListTree.getTree().getValue();
				try{
					GermplasmList folder = germplasmListManager.getGermplasmListById(folderId);
					source.setSelectedFolder(folder);
				} catch(MiddlewareQueryException ex){
					LOG.error("Error with retrieving list with id: " + folderId, ex);
				}
			}
		});
		
		germplasmListTree = new ListManagerTreeComponent(true); 
	}
	
	private void initializeLayout(){
		setHeight("380px");
		setWidth("250px");
		AbsoluteLayout mainLayout = new AbsoluteLayout();
		
		mainLayout.addComponent(germplasmListTree, "top:5px;left:15px");
		mainLayout.addComponent(cancelButton, "top:287px;left:25px");
		mainLayout.addComponent(selectLocationButton, "top:287px;left:100px");
		
		setContent(mainLayout);
	}
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}
	
}
