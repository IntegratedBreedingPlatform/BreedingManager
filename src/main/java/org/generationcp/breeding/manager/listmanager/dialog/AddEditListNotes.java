package org.generationcp.breeding.manager.listmanager.dialog;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListDetailComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
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

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;

@Configurable 
public class AddEditListNotes extends Window implements InitializingBean, InternationalizableComponent {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(AddEditListNotes.class);

	private Label lblNotes;
	private TextArea txtNotes;
	
	private Button saveButton;
	private Button cancelButton;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
    private GermplasmListManager germplasmListManager;
	
	private String title;
	private ListDetailComponent source;
	private Integer germplasmListId;
	private boolean isViewMode;
	
	public AddEditListNotes(ListDetailComponent source, GermplasmListManager germplasmListManager, Integer germplasmListId, String title){
		this.source = source;
		this.germplasmListManager = germplasmListManager;
		this.germplasmListId = germplasmListId;
		this.title = title;
	}
	
	public String getNotes(){
		return txtNotes.getValue().toString();
	}
	
	public String getNotesFromDB(){
		String notes = "";

		try{
			GermplasmList germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
			notes = germplasmList.getNotes();
			
			if(notes == null){
				notes = "";
			}
		}
		catch(Exception e){
			
		}
		return notes;
	}
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setModal(true);
		setWidth("620px");
        setHeight("470px");
        setResizable(false);
        setCaption(title);
        center();
        
        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("600px");
        mainLayout.setHeight("420px");
        
        this.isViewMode = messageSource.getMessage(Message.VIEW_NOTES).equals(this.title);
                
        lblNotes = new Label("<b>Notes: </b>",Label.CONTENT_XHTML);
        lblNotes.setWidth("100px");
        mainLayout.addComponent(lblNotes,"top:10px;left:10px");
        
        txtNotes = new TextArea();
        txtNotes.setWidth("560px");
        txtNotes.setHeight("320px");
        txtNotes.setValue(getNotesFromDB());
        txtNotes.setReadOnly(isViewMode);
        mainLayout.addComponent(txtNotes,"top:30px;left:10px");
        
        // if view-mode, do not show buttons
        if (!isViewMode){
        	renderEditableMode(mainLayout);
        	
        }
        
        addComponent(mainLayout);
	}

	private void renderEditableMode(AbsoluteLayout mainLayout) {
		setHeight("500px");

		saveButton = new Button(messageSource.getMessage(Message.SAVE_LABEL));
		saveButton.setWidth("80px");
		saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		saveButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				saveListNotes();
				
			}
		});
		saveButton.addListener(new CloseWindowAction());
		mainLayout.addComponent(saveButton,"top:360px; right:130px");
		
		cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
		cancelButton.setWidth("80px");
		cancelButton.addListener(new CloseWindowAction());
		mainLayout.addComponent(cancelButton,"top:360px; right:40px");
	}
	
	public void saveListNotes(){
		try{
			GermplasmList listFromDB = this.germplasmListManager.getGermplasmListById(germplasmListId);
			listFromDB.setNotes(txtNotes.getValue().toString());
			
			Integer listId = this.germplasmListManager.updateGermplasmList(listFromDB);
			
			source.setNotesCaption(source.getNotes(listFromDB.getNotes()));
			source.setAddEditNotesCaption();
			source.requestRepaintRequests();
			source.germplasmList.setNotes(txtNotes.getValue().toString());
			
			if(listId == null){
				MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE)
						, messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
				return;
			} 
		} catch(MiddlewareQueryException ex){
			LOG.error("Error in updating the notes of germplasm list: " + germplasmListId, ex);
			MessageNotifier.showError(this.source.getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_SAVING_GERMPLASM_LIST));
			return;
		}
	}
	

}
