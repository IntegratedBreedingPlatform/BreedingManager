/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.util.StudyBrowserTabCloseHandler;
import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.browser.study.listeners.StudyItemClickListener;
import org.generationcp.browser.study.listeners.StudyTreeCollapseListener;
import org.generationcp.browser.study.listeners.StudyTreeExpandListener;
import org.generationcp.browser.study.util.StudyTreeUtil;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class StudyTreeComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3481988646509402160L;

    private final static Logger LOG = LoggerFactory.getLogger(StudyTreeComponent.class);
    
    public final static String REFRESH_BUTTON_ID = "StudyTreeComponent Refresh Button";
    public static final String LOCAL = "LOCAL";
    public static final String CENTRAL = "CENTRAL";
    
    @Autowired
    private StudyDataManager studyDataManager;
    
    private VerticalLayout treeContainer;
    private Tree studyTree;
    private TabSheet tabSheetStudy;
    
    private StudyBrowserMain studyBrowserMain;
    private StudyBrowserMainLayout studyBrowserMainLayout;
    
    private Label controlButtonsHeading;
    private HorizontalLayout controlButtonsLayout;
    private HorizontalLayout controlButtonsSubLayout;
    private Button addFolderBtn;
    private Button deleteFolderBtn;
    private Button renameFolderBtn;
    
    private Button refreshButton;
    
    private Integer rootNodeProjectId;
    private Map<Integer, Integer> parentChildItemIdMap;
    private Object selectedStudyTreeNodeId;
    private StudyTreeUtil studyTreeUtil;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public StudyTreeComponent(StudyBrowserMain studyBrowserMain) {
        this.studyBrowserMain = studyBrowserMain;
        this.studyBrowserMainLayout = studyBrowserMain.getMainLayout();
    }
    
    // Called by StudyButtonClickListener
    public void createTree() {
        treeContainer.removeComponent(studyTree);
        studyTree.removeAllItems();
        
    	studyTree = createCombinedStudyTree();
    	studyTree.setNullSelectionAllowed(false);
        studyTreeUtil = new StudyTreeUtil(studyTree, this);
        
        treeContainer.addComponent(studyTree);
    }


    private Tree createCombinedStudyTree() {
    	
    	final Tree studyTree = new Tree();
    	studyTree.setDragMode(TreeDragMode.NODE);
    	
    	studyTree.addItem(LOCAL);
        studyTree.setItemCaption(LOCAL, messageSource.getMessage(Message.PROGRAM_STUDIES));
    	
        studyTree.addItem(CENTRAL);
        studyTree.setItemCaption(CENTRAL, messageSource.getMessage(Message.PUBLIC_STUDIES));
        
        populateRootNode(studyTree, LOCAL, Database.LOCAL);
        populateRootNode(studyTree, CENTRAL, Database.CENTRAL);
                
        studyTree.addListener(new StudyTreeExpandListener(this));
        studyTree.addListener(new StudyItemClickListener(this));
        studyTree.addListener(new StudyTreeCollapseListener(this));

        studyTree.setItemStyleGenerator(new ItemStyleGenerator() {
        	private static final long serialVersionUID = -5690995097357568121L;

			@Override
            public String getStyle(Object itemId) {
				
				if(itemId.toString().equals(LOCAL) || itemId.toString().equals(CENTRAL)){
					return "listManagerTreeRegularParentNode";
				} else if(itemId!=null && itemId instanceof Integer && isFolder((Integer)itemId)){
					return "listManagerTreeRegularParentNode";
            	} else {
            		return "listManagerTreeRegularChildNode";
            	}
            }
        });

        studyTree.addStyleName("studyBrowserTree");
        studyTree.setImmediate(true);
        
        return studyTree;
    }
    
    
    public void populateRootNode(Tree studyTree, String rootNodeId, Database database){
    	List<FolderReference> rootFolders = new ArrayList<FolderReference>();
        try {
        	rootFolders = this.studyDataManager.getRootFolders(database);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            if (getWindow() != null){
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_STUDIES));
            }
            rootFolders = new ArrayList<FolderReference>();
        }

        for (FolderReference ps : rootFolders) {
            studyTree.addItem(ps.getId());
            studyTree.setItemCaption(ps.getId(), ps.getName());
            studyTree.setParent(ps.getId(), rootNodeId);
            if (!hasChildStudy(ps.getId())){
            	studyTree.setChildrenAllowed(ps.getId(), false);
            }
        }
    }
    
    public Boolean isFolder(Integer studyId) {
        try {
            boolean isStudy = studyDataManager.isStudy(studyId);
            return !isStudy;
        } catch (MiddlewareQueryException e) {
        	return false;
        }
    }
    
    // Called by StudyItemClickListener
    public void studyTreeItemClickAction(Object itemId) throws InternationalizableException{
    	
        try {
        	expandOrCollapseStudyTreeNode(itemId);
        	int studyId = Integer.valueOf(itemId.toString());
        	
            if (!hasChildStudy(studyId) && !isFolder(studyId)){
                createStudyInfoTab(studyId);
            }
            
            
        } catch (NumberFormatException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
        } finally{
        	updateButtons(itemId);
        	selectedStudyTreeNodeId = itemId;
        }
    }

    public Boolean studyExists(int studyId) throws InternationalizableException {
        try {
            DmsProject study = this.studyDataManager.getProject(studyId);
            if(study==null) {
            	return false;
        	} else {
                if (!hasChildStudy(studyId) && !isFolder(studyId)){
                    return true;
                }
            	return false;
        	}
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
            return false;
        }
    }    
    
    public void addStudyNode(int parentStudyId) throws InternationalizableException{
    	
        List<Reference> studyChildren = new ArrayList<Reference>();
        try {
            studyChildren = this.studyDataManager.getChildrenOfFolder(Integer.valueOf(parentStudyId));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
            studyChildren = new ArrayList<Reference>();
        }

        for (Reference sc : studyChildren) {
            studyTree.addItem(sc.getId());
            studyTree.setItemCaption(sc.getId(), sc.getName());
            studyTree.setParent(sc.getId(), parentStudyId);
            
            // check if the study has sub study
            if (hasChildStudy(sc.getId())) {
                studyTree.setChildrenAllowed(sc.getId(), true);
            } else {
                studyTree.setChildrenAllowed(sc.getId(), false);
            }
            
        }
    }

    
    
    private void createStudyInfoTab(int studyId) throws InternationalizableException {
        VerticalLayout layout = new VerticalLayout();

        if (!Util.isTabExist(tabSheetStudy, getStudyName(studyId))) {
            layout.addComponent(new StudyAccordionMenu(studyId, new StudyDetailComponent(this.studyDataManager, studyId),
                    studyDataManager, false,false));
            Tab tab = tabSheetStudy.addTab(layout, getStudyName(studyId), null);
            tab.setClosable(true);

            studyBrowserMainLayout.addStudyInfoTabSheet(tabSheetStudy);
            studyBrowserMainLayout.showDetailsLayout();
            tabSheetStudy.setSelectedTab(layout);
            tabSheetStudy.setCloseHandler(new StudyBrowserTabCloseHandler(studyBrowserMainLayout));
        } else {
            Tab tab = Util.getTabAlreadyExist(tabSheetStudy, getStudyName(studyId));
            tabSheetStudy.setSelectedTab(tab.getComponent());
        }
    }

    private String getStudyName(int studyId) throws InternationalizableException {
        try {
            DmsProject studyDetails = this.studyDataManager.getProject(studyId);
            if(studyDetails != null){
                return studyDetails.getName();
            } else {
                return null;
            }
            
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID);
        }
    }

    public boolean hasChildStudy(int studyId) {

        List<Reference> studyChildren = new ArrayList<Reference>();

        try {
            studyChildren = this.studyDataManager.getChildrenOfFolder(new Integer(studyId));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
            studyChildren = new ArrayList<Reference>();
        }
        if (!studyChildren.isEmpty()) {
            return true;
        }
        return false;
    }
    
    @Override
    public void afterPropertiesSet() {
    	
        setSpacing(true);
        setMargin(true);
        
        tabSheetStudy = new TabSheet();
        
    	studyTree = createCombinedStudyTree();
    	initializeButtonPanel();
    	addComponent(controlButtonsLayout);

        // add tooltip
        studyTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = -2669417630841097077L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.STUDY_DETAILS_LABEL); // "Click to view study details"
            }
        });
        
        treeContainer = new VerticalLayout();
        treeContainer.addComponent(studyTree);
        addComponent(treeContainer);
        
        refreshButton = new Button(); // "Refresh"
        refreshButton.setData(REFRESH_BUTTON_ID);
        refreshButton.addStyleName(Bootstrap.Buttons.INFO.styleName());

        
        refreshButton.addListener(new StudyButtonClickListener(this));
        addComponent(refreshButton);

        studyTreeUtil = new StudyTreeUtil(studyTree, this);
    }
    
    private void initializeButtonPanel() {
    	controlButtonsHeading = new Label();
		controlButtonsHeading.setValue(messageSource.getMessage(Message.PROJECT_STUDIES));
		controlButtonsHeading.setStyleName(Bootstrap.Typography.H4.styleName());
		controlButtonsHeading.setWidth("177px");
		
		renameFolderBtn =new Button("<span class='bms-edit' style='left: 2px; color: #0083c0;font-size: 18px; font-weight: bold;'></span>");
        renameFolderBtn.setHtmlContentAllowed(true);
        renameFolderBtn.setDescription(messageSource.getMessage(Message.RENAME_ITEM));
        renameFolderBtn.setStyleName(Reindeer.BUTTON_LINK);
        renameFolderBtn.setWidth("25px");
        renameFolderBtn.setHeight("30px");
        renameFolderBtn.setEnabled(false);
        renameFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				int studyId = Integer.valueOf(selectedStudyTreeNodeId.toString());
				String name = studyTree.getItemCaption(selectedStudyTreeNodeId);
				studyTreeUtil.renameFolder(studyId, name);
            }
        });
        
        addFolderBtn = new Button("<span class='bms-add' style='left: 2px; color: #00a950;font-size: 18px; font-weight: bold;'></span>");
        addFolderBtn.setHtmlContentAllowed(true);
        addFolderBtn.setDescription(messageSource.getMessage(Message.ADD_NEW_FOLDER));
        addFolderBtn.setStyleName(Reindeer.BUTTON_LINK);
        addFolderBtn.setWidth("25px");
        addFolderBtn.setHeight("30px");
        addFolderBtn.setEnabled(false);
        addFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				studyTreeUtil.addFolder(selectedStudyTreeNodeId);
            }
        });
        
        deleteFolderBtn = new Button("<span class='bms-delete' style='left: 2px; color: #f4a41c;font-size: 18px; font-weight: bold;'></span>");
        deleteFolderBtn.setHtmlContentAllowed(true);
        deleteFolderBtn.setDescription(messageSource.getMessage(Message.DELETE_ITEM));
        deleteFolderBtn.setStyleName(Reindeer.BUTTON_LINK);
        deleteFolderBtn.setWidth("25px");
        deleteFolderBtn.setHeight("30px");
        deleteFolderBtn.setEnabled(false);
        deleteFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				int studyId = Integer.valueOf(selectedStudyTreeNodeId.toString());
				studyTreeUtil.deleteFolder(studyId);
            }
        });
        
        controlButtonsSubLayout = new HorizontalLayout();
        controlButtonsSubLayout.addComponent(addFolderBtn);
        controlButtonsSubLayout.addComponent(renameFolderBtn);
        controlButtonsSubLayout.addComponent(deleteFolderBtn);
        controlButtonsSubLayout.setComponentAlignment(addFolderBtn, Alignment.BOTTOM_RIGHT);
        controlButtonsSubLayout.setComponentAlignment(renameFolderBtn, Alignment.BOTTOM_RIGHT);
        controlButtonsSubLayout.setComponentAlignment(deleteFolderBtn, Alignment.BOTTOM_RIGHT);
        
        controlButtonsLayout = new HorizontalLayout();
        controlButtonsLayout.setWidth("304px");
        controlButtonsLayout.setSpacing(true);
        
        controlButtonsLayout.addComponent(controlButtonsHeading);
        controlButtonsLayout.addComponent(controlButtonsSubLayout);
        controlButtonsLayout.setComponentAlignment(controlButtonsHeading, Alignment.BOTTOM_LEFT);
        controlButtonsLayout.setComponentAlignment(controlButtonsSubLayout, Alignment.BOTTOM_RIGHT);
        
	}
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(refreshButton, Message.REFRESH_LABEL);
    }

    
    public TabSheet getTabSheetStudy() {
        return tabSheetStudy;
    }
    
    public void showChild(Integer childItemId){
    	buildChildMap(childItemId,true);
    	Integer rootItemId = rootNodeProjectId;
    	    	
    	if(childItemId>0){
    		studyTree.expandItem(CENTRAL);
    	} else {
    		studyTree.expandItem(LOCAL);
    	}
    	
    	if(rootItemId!=null){
    		addStudyNode(rootItemId);
    		studyTree.expandItem(rootItemId);
    	}
    	
    	Integer currentItemId = parentChildItemIdMap.get(rootItemId);
    	if(currentItemId!=null){
    		addStudyNode(currentItemId);
    		studyTree.expandItem(currentItemId);
    	}
    	
    	while(parentChildItemIdMap.get(currentItemId)!=childItemId && currentItemId!=null){
    		currentItemId = parentChildItemIdMap.get(currentItemId);
    		if(currentItemId!=null){
    			addStudyNode(currentItemId);
    			studyTree.expandItem(currentItemId);
    		}
    	}
    	studyTree.select(childItemId);
    	studyTree.setNullSelectionAllowed(false);
    	studyTree.select(childItemId);
    	
    }

    private void buildChildMap(Integer studyId, Boolean endNode){
    	if(endNode==true){
    		parentChildItemIdMap = new HashMap<Integer, Integer>();
    	}
        try {
            DmsProject studyParent = this.studyDataManager.getParentFolder(studyId);
            if(studyParent!=null && ((studyId<0 && studyParent.getProjectId()!=1) || studyId>0)){
            	int parentProjectId = studyParent.getProjectId();
                parentChildItemIdMap.put(parentProjectId, studyId);
            	buildChildMap(studyParent.getProjectId(),false);
            } else {
            	rootNodeProjectId = studyId;
            }
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
        }
    }

    
    public void expandOrCollapseStudyTreeNode(Object itemId){
    	if(!this.studyTree.isExpanded(itemId)){
    		this.studyTree.expandItem(itemId);
    	} else{
    		this.studyTree.collapseItem(itemId);
    	}
    }
    
    public void setSelectedStudyTreeNodeId(Object id){
    	this.selectedStudyTreeNodeId = id;
    }
    
    public void updateButtons(Object itemId){
    	setSelectedStudyTreeNodeId(itemId);
    	try {
    		//If any of the central lists/folders is selected
			if(Integer.valueOf(itemId.toString())>0){
				addFolderBtn.setEnabled(false);
				renameFolderBtn.setEnabled(false);
				deleteFolderBtn.setEnabled(false);
    		//If any of the local folders is selected
			} else if(Integer.valueOf(itemId.toString())<=0 && isFolder((Integer) itemId)){
				addFolderBtn.setEnabled(true);
				renameFolderBtn.setEnabled(true);
				deleteFolderBtn.setEnabled(true);
			//The rest of the local lists
			} else {
				addFolderBtn.setEnabled(true);
				renameFolderBtn.setEnabled(true);
				deleteFolderBtn.setEnabled(false);
			}
    	} catch(NumberFormatException e) {
    		//If selected item is "Shared Lists"
    		if(itemId.toString().equals("CENTRAL")) {
				addFolderBtn.setEnabled(false);
				renameFolderBtn.setEnabled(false);
				deleteFolderBtn.setEnabled(false);
			//If selected item is "Program Lists"
    		} else if(itemId.toString().equals(LOCAL)) {
				addFolderBtn.setEnabled(true);
				renameFolderBtn.setEnabled(false);
				deleteFolderBtn.setEnabled(false);
			//Any non-numeric itemID (nothing goes here as of the moment)
    		} else {
				addFolderBtn.setEnabled(false);
				renameFolderBtn.setEnabled(false);
				deleteFolderBtn.setEnabled(false);
    		}
    	}
    }
    
    /*
     * Update the tab header and displayed study name with new name.
     * This is called by rename function in study tree
     */
    public void renameStudyTab(String oldName, String newName){
    	Tab studyTab = Util.getTabAlreadyExist(tabSheetStudy, oldName);
    	if (studyTab != null){
    		studyTab.setCaption(newName);
    	}
    	Component component = studyTab.getComponent();
    	
    	if (component instanceof VerticalLayout){
    		VerticalLayout layout = (VerticalLayout) component;
    		Iterator<Component> componentIterator = layout.getComponentIterator();
    		while (componentIterator.hasNext()){
    			Component child = componentIterator.next();
    			if (child instanceof StudyAccordionMenu){
    				StudyAccordionMenu accordion = (StudyAccordionMenu) child;
    				accordion.updateStudyName(newName);
    			}
    		}
    	}
    }
    
    
    public Tree getStudyTree(){
    	return studyTree;
    }
}
