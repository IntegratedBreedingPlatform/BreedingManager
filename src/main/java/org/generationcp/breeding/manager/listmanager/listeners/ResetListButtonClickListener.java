package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListBuilderComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

public class ResetListButtonClickListener implements Button.ClickListener{

	private static final long serialVersionUID = -2641642996209640461L;
	
	private ListBuilderComponent source;
	
	private SimpleResourceBundleMessageSource messageSource;
	
	public ResetListButtonClickListener(ListBuilderComponent source, SimpleResourceBundleMessageSource messageSource){
		this.source = source;
		this.messageSource = messageSource;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {

		ConfirmDialog.show(source.getWindow(), "Reset List Builder", 
            messageSource.getMessage(Message.CONFIRM_RESET_LIST_BUILDER_FIELDS), 
            messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL), 
            new ConfirmDialog.Listener() {
				private static final long serialVersionUID = 1L;
				public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                    	resetListBuilder();
                    }
                }
            }
        );
	}

	public void resetListBuilder(){
		source.resetList();
	}
    
}
