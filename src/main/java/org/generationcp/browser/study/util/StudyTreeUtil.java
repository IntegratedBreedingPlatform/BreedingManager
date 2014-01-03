package org.generationcp.browser.study.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.StudyTreeComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class StudyTreeUtil implements Serializable {
	private static final long serialVersionUID = -4427723835290060592L;
	
	private final static Logger LOG = LoggerFactory.getLogger(StudyTreeUtil.class);
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private StudyDataManager studyDataManager;
	
	private Tree targetTree;
	private StudyTreeComponent source;
	
	public StudyTreeUtil(Tree targetTree, StudyTreeComponent source){
		this.targetTree = targetTree;
		this.source = source;
	}

	public void addFolder(final Object parentItemId){
    	
        final Window w = new Window("Add new folder");
        w.setWidth("300px");
        w.setHeight("150px");
        w.setModal(true);
        w.setResizable(false);
        w.setStyleName(Reindeer.WINDOW_LIGHT);

        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setMargin(true);

        HorizontalLayout formContainer = new HorizontalLayout();
        formContainer.setSpacing(true);

        Label l = new Label("Folder Name");
        final TextField name = new TextField();
        name.setMaxLength(50);

        formContainer.addComponent(l);
        formContainer.addComponent(name);

        HorizontalLayout btnContainer = new HorizontalLayout();
        btnContainer.setSpacing(true);
        btnContainer.setWidth("100%");

        Label spacer = new Label("");
        btnContainer.addComponent(spacer);
        btnContainer.setExpandRatio(spacer, 1.0F);

        Button ok = new Button("Ok");
        ok.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        ok.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -6313787074401316900L;

			@Override
            public void buttonClick(Button.ClickEvent event) {
                Integer newFolderId = null;
                String newFolderName = name.getValue().toString();
                int parentFolderId = 1;	//1 by default because root study folder has id = 1
                
                if(newFolderName.replace(" ","").equals("")){
                	MessageNotifier.showError(source.getWindow(),
                            messageSource.getMessage(Message.INVALID_INPUT), 
                            messageSource.getMessage(Message.INVALID_BLANK_STUDY_FOLDER_NAME),
                            Notification.POSITION_CENTERED);
                	return;
                } else if(newFolderName.length() > 255){
                	MessageNotifier.showError(source.getWindow(),
                            messageSource.getMessage(Message.INVALID_INPUT), 
                            messageSource.getMessage(Message.INVALID_LONG_STUDY_FOLDER_NAME),
                            Notification.POSITION_CENTERED);
                	return;
                }

                try{
                	if(studyDataManager.checkIfProjectNameIsExisting(newFolderName)){
                		MessageNotifier.showError(source.getWindow(),
                                messageSource.getMessage(Message.INVALID_INPUT), 
                                messageSource.getMessage(Message.EXISTING_STUDY_ERROR_MESSAGE),
                                Notification.POSITION_CENTERED);
                    	return;
                	}
                	
                	if(parentItemId != null && parentItemId instanceof Integer){
                		if(source.isFolder((Integer) parentItemId)){
                			parentFolderId = ((Integer) parentItemId).intValue();
                		} else{
                			int selectItemId = ((Integer) parentItemId).intValue();
                			DmsProject parentFolder = studyDataManager.getParentFolder(selectItemId);
                			parentFolderId = parentFolder.getProjectId().intValue();
                		}
                	}
                	
                	newFolderId = Integer.valueOf(studyDataManager.addSubFolder(parentFolderId, newFolderName, newFolderName));
                } catch(MiddlewareQueryException ex){
                	LOG.error("Error with adding a study folder.", ex);
                	MessageNotifier.showError(source.getWindow(),
                            messageSource.getMessage(Message.ERROR_DATABASE), 
                            messageSource.getMessage(Message.PLEASE_SEE_ERROR_LOG),
                            Notification.POSITION_CENTERED);
                	return;
                }
                
                //update UI
                if (newFolderId != null) {
                    targetTree.addItem(newFolderId);
                    targetTree.setItemCaption(newFolderId, newFolderName);
                    targetTree.setChildrenAllowed(newFolderId, true);
                    
                    source.setSelectedStudyTreeNodeId(newFolderId);
    		        
                    if(parentFolderId == 1){
                    	targetTree.setChildrenAllowed(StudyTreeComponent.LOCAL, true);
    		        	targetTree.setParent(newFolderId, StudyTreeComponent.LOCAL);
                    } else{
                    	targetTree.setChildrenAllowed(Integer.valueOf(parentFolderId), true);
    		        	targetTree.setParent(newFolderId, Integer.valueOf(parentFolderId));
                    }
                    
                    if (targetTree.getValue() != null) {
                        if (!targetTree.isExpanded(targetTree.getValue()))
                            targetTree.expandItem(parentItemId);
                    } else {
                    	targetTree.expandItem(StudyTreeComponent.LOCAL);
					}
                    targetTree.select(newFolderId);
                }

                // close popup
                source.getWindow().removeWindow(event.getComponent().getWindow());
            }
        });

        Button cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -6542741100092010158L;

			@Override
            public void buttonClick(Button.ClickEvent event) {
            	source.getWindow().removeWindow(w);
            }
        });

        btnContainer.addComponent(ok);
        btnContainer.addComponent(cancel);

        container.addComponent(formContainer);
        container.addComponent(btnContainer);

        w.setContent(container);

        // show window
        source.getWindow().addWindow(w);    	
    }
}
