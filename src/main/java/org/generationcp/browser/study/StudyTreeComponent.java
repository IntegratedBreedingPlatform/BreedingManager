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
import java.util.List;
import java.util.Map;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.listeners.StudyButtonClickListener;
import org.generationcp.browser.study.listeners.StudyItemClickListener;
import org.generationcp.browser.study.listeners.StudyTreeExpandListener;
import org.generationcp.browser.study.util.StudyTreeUtil;
import org.generationcp.browser.util.SelectedTabCloseHandler;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class StudyTreeComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -3481988646509402160L;

    private final static Logger LOG = LoggerFactory.getLogger(StudyTreeComponent.class);
    
    public final static String REFRESH_BUTTON_ID = "StudyTreeComponent Refresh Button";
    public static final String LOCAL = "LOCAL";
    public static final String CENTRAL = "CENTRAL";
    
    @Autowired
    private StudyDataManagerImpl studyDataManager;
    
    private VerticalLayout treeContainer;
    private Tree studyTree;
    private static TabSheet tabSheetStudy;
    private HorizontalLayout studyBrowserMainLayout;
    
    private Label controlButtonsHeading;
    private HorizontalLayout controlButtonsLayout;
    private Button addFolderBtn;
    private Button deleteFolderBtn;
    private Button renameFolderBtn;
    
    private Button refreshButton;
    
    private Database database;
    
    private Integer rootNodeProjectId;
    private Map<Integer, Integer> parentChildItemIdMap;
    private Object selectedStudyTreeNodeId;
    private StudyTreeUtil studyTreeUtil;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    static{
        tabSheetStudy = new TabSheet();
    }
    
    public StudyTreeComponent(HorizontalLayout studyBrowserMainLayout, Database database) {
        this.studyBrowserMainLayout = studyBrowserMainLayout;
        this.database = database;
    }

    public StudyTreeComponent(HorizontalLayout studyBrowserMainLayout) {
        this.studyBrowserMainLayout = studyBrowserMainLayout;
    }
    
    // Called by StudyButtonClickListener
    public void createTree() {
        treeContainer.removeComponent(studyTree);
        studyTree.removeAllItems();
        
        if(database!=null){
        	studyTree = createStudyTree(Database.LOCAL);
        } else { 
        	studyTree = createCombinedStudyTree();
        }
                
        treeContainer.addComponent(studyTree);
    }

    private Tree createStudyTree(Database database) {
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

        final Tree studyTree = new Tree();

        
        
        for (FolderReference ps : rootFolders) {
            studyTree.addItem(ps.getId());
            studyTree.setItemCaption(ps.getId(), ps.getName());
        }
                
        studyTree.addListener(new StudyTreeExpandListener(this));
        studyTree.addListener(new StudyItemClickListener(this));

        studyTree.setItemStyleGenerator(new ItemStyleGenerator() {
        	private static final long serialVersionUID = -5690995097357568121L;

			@Override
            public String getStyle(Object itemId) {
				Study currentStudy = null;
				try {
					currentStudy = studyDataManager.getStudy(Integer.valueOf(itemId.toString()));
				} catch (NumberFormatException e) {
					currentStudy = null;
				} catch (MiddlewareQueryException e) {
					LOG.error("Error with getting study by id: " + itemId, e);
					currentStudy = null;
		        } catch (Exception e) {
		        	//e.printStackTrace();
				} 
				
            	//if(itemId.equals(LOCAL) || itemId.equals(CENTRAL)){
            	//	return "listManagerTreeRootNode"; 
				
				if(currentStudy!=null && isFolder(currentStudy.getId())){
					return "listManagerTreeRegularParentNode";
				} else if(currentStudy!=null && isFolderType(currentStudy.getType())){
            		return "listManagerTreeRegularParentNode";
            	} else {
            		return "listManagerTreeRegularChildNode";
            	}
            }
        });

        studyTree.setImmediate(true);
        
        return studyTree;
    }

    private Tree createCombinedStudyTree() {
    	
    	final Tree studyTree = new Tree();
    	
    	studyTree.addItem(LOCAL);
        studyTree.setItemCaption(LOCAL, messageSource.getMessage(Message.PROGRAM_STUDIES));
    	
        studyTree.addItem(CENTRAL);
        studyTree.setItemCaption(CENTRAL, messageSource.getMessage(Message.PUBLIC_STUDIES));
        
        populateRootNode(studyTree, LOCAL, Database.LOCAL);
        populateRootNode(studyTree, CENTRAL, Database.CENTRAL);
                
        studyTree.addListener(new StudyTreeExpandListener(this));
        studyTree.addListener(new StudyItemClickListener(this));

        studyTree.setItemStyleGenerator(new ItemStyleGenerator() {
        	private static final long serialVersionUID = -5690995097357568121L;

			@Override
            public String getStyle(Object itemId) {
				Study currentStudy = null;
				try {
					currentStudy = studyDataManager.getStudy(Integer.valueOf(itemId.toString()));
				} catch (NumberFormatException e) {
					currentStudy = null;
				} catch (MiddlewareQueryException e) {
					LOG.error("Error with getting study by id: " + itemId, e);
					currentStudy = null;
		        } catch (Exception e) {
		        	//e.printStackTrace();
				} 
				
				if(itemId.toString().equals(LOCAL) || itemId.toString().equals(CENTRAL)){
					return "listManagerTreeRegularParentNode";
				} else if(currentStudy!=null && isFolder(currentStudy.getId())){
					return "listManagerTreeRegularParentNode";
				} else if(currentStudy!=null && isFolderType(currentStudy.getType())){
            		return "listManagerTreeRegularParentNode";
            	} else {
            		return "listManagerTreeRegularChildNode";
            	}
            }
        });

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
        	
        	if(database==null){
        		if(studyId>0){
        			studyTree.expandItem(CENTRAL);
        		} else {
        			studyTree.expandItem(LOCAL);
        		}
        	}
        	
            Study study = this.studyDataManager.getStudy(studyId);
            //don't show study details if study record is a Folder ("F")
            String studyType = study.getType();
            if (!hasChildStudy(studyId) && !isFolderType(studyType)){
                createStudyInfoTab(studyId);
            }
            
            
        } catch (NumberFormatException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
        } finally{
        	updateButtons(itemId);
        	selectedStudyTreeNodeId = itemId;
        }
    }

    public Boolean studyExists(int studyId) throws InternationalizableException {
        try {
            Study study = this.studyDataManager.getStudy(Integer.valueOf(studyId));
            if(study==null) {
            	return false;
        	} else {
        		String studyType = study.getType();
                if (!hasChildStudy(studyId) && !isFolderType(studyType)){
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

            studyBrowserMainLayout.addComponent(tabSheetStudy);
            studyBrowserMainLayout.setExpandRatio(tabSheetStudy, 1.0f);
            tabSheetStudy.setSelectedTab(layout);
            tabSheetStudy.setCloseHandler(new SelectedTabCloseHandler());
            
        } else {
            Tab tab = Util.getTabAlreadyExist(tabSheetStudy, getStudyName(studyId));
            tabSheetStudy.setSelectedTab(tab.getComponent());
        }
    }

    private String getStudyName(int studyId) throws InternationalizableException {
        try {
            Study studyDetails = this.studyDataManager.getStudy(Integer.valueOf(studyId));
            if(studyDetails != null){
                return studyDetails.getName();
            } else {
                return null;
            }
            
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID);
        }
    }

    private boolean hasChildStudy(int studyId) {

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
        
        if (database != null){
        	studyTree = createStudyTree(database);
        } else {
        	studyTree = createCombinedStudyTree();
        	initializeButtonPanel();
        	addComponent(controlButtonsLayout);
        }

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
        refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        if (database == Database.LOCAL || database == null) {
            refreshButton.addListener(new StudyButtonClickListener(this));
            addComponent(refreshButton);
        }

        studyTreeUtil = new StudyTreeUtil(studyTree, this);
    }
    
    private void initializeButtonPanel() {
    	controlButtonsHeading = new Label();
		controlButtonsHeading.setValue(messageSource.getMessage(Message.PROJECT_STUDIES));
		controlButtonsHeading.setStyleName(Bootstrap.Typography.H4.styleName());
		
		renameFolderBtn =new Button("<span class='glyphicon glyphicon-pencil' style='right: 2px;'></span>");
        renameFolderBtn.setHtmlContentAllowed(true);
        renameFolderBtn.setDescription("Rename Folder");
        renameFolderBtn.setStyleName(Bootstrap.Buttons.INFO.styleName());
        renameFolderBtn.setWidth("40px");
        renameFolderBtn.setEnabled(false);
        renameFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				//TODO
            }
        });
        
        addFolderBtn = new Button("<span class='glyphicon glyphicon-plus' style='right: 2px'></span>");
        addFolderBtn.setHtmlContentAllowed(true);
        addFolderBtn.setDescription("Add New Folder");
        addFolderBtn.setStyleName(Bootstrap.Buttons.INFO.styleName());
        addFolderBtn.setWidth("40px");
        addFolderBtn.setEnabled(false);
        addFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				studyTreeUtil.addFolder(selectedStudyTreeNodeId);
            }
        });
        
        deleteFolderBtn = new Button("<span class='glyphicon glyphicon-trash' style='right: 2px'></span>");
        deleteFolderBtn.setHtmlContentAllowed(true);
        deleteFolderBtn.setDescription("Delete Selected Folder");
        deleteFolderBtn.setStyleName(Bootstrap.Buttons.DANGER.styleName());
        deleteFolderBtn.setWidth("40px");
        deleteFolderBtn.setEnabled(false);
        deleteFolderBtn.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
            public void buttonClick(Button.ClickEvent event) {
				//TODO
            }
        });
        
        controlButtonsLayout = new HorizontalLayout();
        //controlButtonsLayout.addComponent(controlButtonsHeading);
        controlButtonsLayout.addComponent(new Label("&nbsp;&nbsp;",Label.CONTENT_XHTML));
        controlButtonsLayout.addComponent(renameFolderBtn);
        controlButtonsLayout.addComponent(addFolderBtn);
        controlButtonsLayout.addComponent(deleteFolderBtn);
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

    
    public static TabSheet getTabSheetStudy() {
        return tabSheetStudy;
    }

    private boolean isFolderType(String type){
        if(type != null){
            type = type.toLowerCase();
            if(type.equals("f") || type.equals("folder")){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    
    public void showChild(Integer childItemId){
    	buildChildMap(childItemId,true);
    	Integer rootItemId = rootNodeProjectId;
    	
    	System.out.println("Root: "+rootItemId);
    	System.out.println("Parent Child Map: "+parentChildItemIdMap);
    	
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
}
