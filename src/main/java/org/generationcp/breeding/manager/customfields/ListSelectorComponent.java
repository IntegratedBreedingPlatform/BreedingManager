package org.generationcp.breeding.manager.customfields;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.GermplasmListTree;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.ToggleButton;
import org.generationcp.breeding.manager.customcomponent.generator.GermplasmListSourceItemDescriptionGenerator;
import org.generationcp.breeding.manager.customcomponent.generator.GermplasmListSourceItemStyleGenerator;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeCollapseListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeExpandListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@Configurable
public abstract class ListSelectorComponent extends CssLayout implements
        InitializingBean, BreedingManagerLayout {

	private static final long serialVersionUID = 6042782367848192853L;

	private static final Logger LOG = LoggerFactory.getLogger(ListSelectorComponent.class);

    public static final int BATCH_SIZE = 50;
    public static final String REFRESH_BUTTON_ID = "ListManagerTreeComponent Refresh Button";
    public static final String LISTS = "Lists";

    protected enum FolderSaveMode {
        ADD, RENAME
    }

    protected GermplasmListSource germplasmListSource;

    @Autowired
    protected GermplasmListManager germplasmListManager;
    @Autowired
    private UserDataManager userDataManager;

    @Autowired
    protected SimpleResourceBundleMessageSource messageSource;

    protected HorizontalLayout controlButtonsLayout;
    protected HorizontalLayout ctrlBtnsLeftSubLayout;
    protected HorizontalLayout ctrlBtnsRightSubLayout;
    protected CssLayout treeContainerLayout;

    protected Integer listId;
    protected GermplasmListTreeUtil germplasmListTreeUtil;

    protected Button addFolderBtn;
    protected Button deleteFolderBtn;
    protected Button renameFolderBtn;

    protected HeaderLabelLayout treeHeadingLayout;
    protected Label heading;
    protected Button refreshButton;

    protected HorizontalLayout addRenameFolderLayout;
    protected Label folderLabel;
    protected TextField folderTextField;
    protected Button saveFolderButton;
    protected Button cancelFolderButton;

    protected Boolean selectListsFolderByDefault;

    protected Object selectedListId;
    protected GermplasmList germplasmList;

    protected ToggleButton toggleListTreeButton;

    protected Map<Integer, GermplasmList> germplasmListsMap;

    protected FolderSaveMode folderSaveMode;

    protected ListNameValidator listNameValidator;

    protected ListTreeActionsListener treeActionsListener;

    protected abstract boolean doIncludeActionsButtons();
    protected abstract boolean doIncludeRefreshButton();
    protected abstract boolean isTreeItemsDraggable();
    protected abstract boolean doShowFoldersOnly();
    protected abstract String getTreeStyleName();
    public abstract String getMainTreeStyleName();
    public abstract Object[] generateCellInfo(String name, String owner, String description, String listType, String numberOfEntries);
    public abstract void setNodeItemIcon(Object id, boolean isFolder);
    public abstract void instantiateGermplasmListSourceComponent();

    public GermplasmListSource getGermplasmListSource(){
        return germplasmListSource;
    }

    public void setGermplasmListSource(GermplasmListSource newGermplasmListSource){
        this.germplasmListSource = newGermplasmListSource;
    }

    protected boolean doSaveNewFolder() {
        return FolderSaveMode.ADD.equals(folderSaveMode);
    }

    public boolean usedInSubWindow(){
        return true;
    }

    protected boolean doIncludeTreeHeadingIcon(){
        return true;
    }

    protected boolean doIncludeToggleButton(){
        return false;
    }

    public void initializeRefreshButton(){
        refreshButton = new Button();
        refreshButton.setData(REFRESH_BUTTON_ID);
        refreshButton.setCaption(messageSource.getMessage(Message.REFRESH_LABEL));
        refreshButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
    }

    protected void initializeButtonPanel() {
        renameFolderBtn = new IconButton("<span class='bms-edit' style='left: 2px; color: #0083c0;font-size: 18px; font-weight: bold;'></span>","Rename Item");
        renameFolderBtn.setEnabled(false);

        addFolderBtn = new IconButton("<span class='bms-add' style='left: 2px; color: #00a950;font-size: 18px; font-weight: bold;'></span>","Add New Folder");
        addFolderBtn.setEnabled(false);

        deleteFolderBtn = new IconButton("<span class='bms-delete' style='left: 2px; color: #f4a41c;font-size: 18px; font-weight: bold;'></span>","Delete Item");
        deleteFolderBtn.setEnabled(false);
        deleteFolderBtn.setData(this);

        ctrlBtnsRightSubLayout = new HorizontalLayout();
        ctrlBtnsRightSubLayout.setHeight("30px");
        ctrlBtnsRightSubLayout.addComponent(addFolderBtn);
        ctrlBtnsRightSubLayout.addComponent(renameFolderBtn);
        ctrlBtnsRightSubLayout.addComponent(deleteFolderBtn);
        ctrlBtnsRightSubLayout.setComponentAlignment(addFolderBtn, Alignment.BOTTOM_RIGHT);
        ctrlBtnsRightSubLayout.setComponentAlignment(renameFolderBtn, Alignment.BOTTOM_RIGHT);
        ctrlBtnsRightSubLayout.setComponentAlignment(deleteFolderBtn, Alignment.BOTTOM_RIGHT);

        ctrlBtnsLeftSubLayout = new HorizontalLayout();
        ctrlBtnsLeftSubLayout.setHeight("30px");

        if(doIncludeToggleButton()){
            ctrlBtnsLeftSubLayout.addComponent(toggleListTreeButton);
            ctrlBtnsLeftSubLayout.setComponentAlignment(toggleListTreeButton, Alignment.BOTTOM_LEFT);
        }

        if (doIncludeTreeHeadingIcon()){
            ctrlBtnsLeftSubLayout.addComponent(treeHeadingLayout);
            heading.setWidth("80px");
        } else {
            ctrlBtnsLeftSubLayout.addComponent(heading);
            heading.setWidth("140px");
        }

        controlButtonsLayout = new HorizontalLayout();
        controlButtonsLayout.setWidth("100%");
        controlButtonsLayout.setHeight("30px");
        controlButtonsLayout.setSpacing(true);

        controlButtonsLayout.addComponent(ctrlBtnsLeftSubLayout);
        controlButtonsLayout.addComponent(ctrlBtnsRightSubLayout);
        controlButtonsLayout.setComponentAlignment(ctrlBtnsLeftSubLayout, Alignment.BOTTOM_LEFT);
        controlButtonsLayout.setComponentAlignment(ctrlBtnsRightSubLayout, Alignment.BOTTOM_RIGHT);

    }

    protected void initializeAddRenameFolderPanel() {
        folderLabel = new Label("Folder");
        folderLabel.addStyleName(AppConstants.CssStyles.BOLD);
        Label mandatoryMarkLabel = new MandatoryMarkLabel();

        folderTextField = new TextField();
        folderTextField.setMaxLength(50);
        folderTextField.setValidationVisible(false);

        folderTextField.setRequired(true);
        folderTextField.setRequiredError("Please specify item name.");
        listNameValidator = new ListNameValidator();
        folderTextField.addValidator(listNameValidator);

        saveFolderButton =new Button("<span class='glyphicon glyphicon-ok' style='right: 2px;'></span>");
        saveFolderButton.setHtmlContentAllowed(true);
        saveFolderButton.setDescription(messageSource.getMessage(Message.SAVE_LABEL));
        saveFolderButton.setStyleName(Bootstrap.Buttons.SUCCESS.styleName());

        cancelFolderButton =new Button("<span class='glyphicon glyphicon-remove' style='right: 2px;'></span>");
        cancelFolderButton.setHtmlContentAllowed(true);
        cancelFolderButton.setDescription(messageSource.getMessage(Message.CANCEL));
        cancelFolderButton.setStyleName(Bootstrap.Buttons.DANGER.styleName());

        addRenameFolderLayout = new HorizontalLayout();
        addRenameFolderLayout.setSpacing(true);

        HorizontalLayout rightPanelLayout = new HorizontalLayout();
        rightPanelLayout.addComponent(folderTextField);
        rightPanelLayout.addComponent(saveFolderButton);
        rightPanelLayout.addComponent(cancelFolderButton);

        addRenameFolderLayout.addComponent(folderLabel);
        addRenameFolderLayout.addComponent(mandatoryMarkLabel);
        addRenameFolderLayout.addComponent(rightPanelLayout);


        addRenameFolderLayout.setVisible(false);
    }

    public void updateButtons(Object itemId){
        setSelectedListId(itemId);
        
    	//If any of the lists/folders is selected
    	if(NumberUtils.isNumber(itemId.toString())){
            addFolderBtn.setEnabled(true);
            renameFolderBtn.setEnabled(true);
            deleteFolderBtn.setEnabled(true);        		
    	} else if(itemId.toString().equals(LISTS)) {
            addFolderBtn.setEnabled(true);
            renameFolderBtn.setEnabled(false);
            deleteFolderBtn.setEnabled(false);
        }
    }

    public void showAddRenameFolderSection(boolean showFolderSection){
        addRenameFolderLayout.setVisible(showFolderSection);

        if(showFolderSection && folderSaveMode != null){
            folderLabel.setValue(doSaveNewFolder()? "Add Folder" : "Rename Item");

            if(doSaveNewFolder()){
                folderTextField.setValue("");

                //If rename, set existing name
            } else if (selectedListId != null){
                String itemCaption = getSelectedItemCaption();
                if (itemCaption != null){
                    listNameValidator.setCurrentListName(itemCaption);
                    folderTextField.setValue(itemCaption);
                }
            }
            folderTextField.focus();

        }
    }

    protected boolean isEmptyFolder(GermplasmList list) throws MiddlewareQueryException {
        boolean isFolder = list.getType().equalsIgnoreCase(AppConstants.DB.FOLDER);
        return isFolder && !hasChildList(list.getId());
    }

    protected boolean hasChildList(int listId) {

        List<GermplasmList> listChildren = new ArrayList<GermplasmList>();

        try {
            listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, 0, 1);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting germplasm lists by parent id.", e);
            MessageNotifier.showWarning(getWindow(),
                    messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
            listChildren = new ArrayList<GermplasmList>();
        }

        return !listChildren.isEmpty();
    }


    public void addGermplasmListNode(int parentGermplasmListId) {
        List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

        try {
            germplasmListChildren = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, BATCH_SIZE);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting germplasm lists by parent id.", e);
            MessageNotifier.showWarning(getWindow(),
                    messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
            germplasmListChildren = new ArrayList<GermplasmList>();
        }
        addGermplasmListNodeToComponent(germplasmListChildren, parentGermplasmListId);

    }

    public boolean doAddItem(GermplasmList list){
        return !doShowFoldersOnly() || isFolder(list.getId());
    }

    public boolean isFolder(Object itemId){
        try {
            int currentListId = Integer.valueOf(itemId.toString());
            GermplasmList currentGermplasmList = germplasmListManager.getGermplasmListById(currentListId);
            if(currentGermplasmList==null) {
                return false;
            }
            return currentGermplasmList.getType().equalsIgnoreCase(AppConstants.DB.FOLDER);
        } catch (MiddlewareQueryException e){
            LOG.debug("Checking is folder, cause the MW exception");
            LOG.error(e.getMessage(),e);
            return false;
        } catch (NumberFormatException e){
            boolean returnVal = false;
            if(listId!=null && (listId.toString().equals(LISTS))){
                returnVal = true;
            }
            return returnVal;
        }
    }

    public Object getSelectedListId(){
        return selectedListId;
    }


    public void setListId(Integer listId){
        this.listId = listId;
    }

    public void assignNewNameToGermplasmListMap(String key, String newName){
        GermplasmList germplasmListFromMap = germplasmListsMap.get(Integer.valueOf(key.toString()));
        if(germplasmListFromMap!=null){
            germplasmListFromMap.setName(newName);
        }
    }

    @Override
    public void addListeners() {
        if (doIncludeRefreshButton()){
            refreshButton.addListener(new Button.ClickListener() {
                private static final long serialVersionUID = 1L;
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    refreshComponent();
                }
            });
        }

        if (doIncludeActionsButtons()){
            addFolderActionsListener();
        }
    }

    @SuppressWarnings("serial")
    protected void addFolderActionsListener() {
        renameFolderBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                folderSaveMode = FolderSaveMode.RENAME;
                showAddRenameFolderSection(true);
            }
        });

        addFolderBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                folderSaveMode = FolderSaveMode.ADD;
                showAddRenameFolderSection(true);
            }
        });

        deleteFolderBtn.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Object data = event.getButton().getData();
                if (data instanceof ListSelectorComponent){
                    germplasmListTreeUtil.deleteFolderOrList((ListSelectorComponent) data,
                            Integer.valueOf(selectedListId.toString()), treeActionsListener);
                }
            }
        });

        folderTextField.addShortcutListener(new ShortcutListener("ENTER", ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                addRenameItemAction();
            }
        });

        saveFolderButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                addRenameItemAction();
            }
        });

        cancelFolderButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showAddRenameFolderSection(false);
            }
        });
    }

    public void refreshRemoteTree(){
    }

    public void studyClickedAction(GermplasmList germplasmList){
        if (treeActionsListener != null && germplasmList != null){
            treeActionsListener.studyClicked(germplasmList);
        }
    }

    public void folderClickedAction(GermplasmList germplasmList){
        if (treeActionsListener != null && germplasmList != null){
            treeActionsListener.folderClicked(germplasmList);
        }
    }

    public void toggleFolderSectionForItemSelected(){
        if (addRenameFolderLayout != null 	&& addRenameFolderLayout.isVisible()){
            Integer currentListId = null;
            if (selectedListId instanceof Integer){
                currentListId = Integer.valueOf(selectedListId.toString());
            }

            if (!doSaveNewFolder()){
                if (currentListId != null && currentListId < 0){
                    folderTextField.setValue(getSelectedItemCaption());
                    folderTextField.focus();
                } else if (LISTS.equals(selectedListId)){
                    showAddRenameFolderSection(false);
                }

            }
        }
    }

    @Override
    public void layoutComponents() {
        setWidth("100%");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setWidth("100%");

        if (doIncludeActionsButtons()){
            layout.addComponent(controlButtonsLayout);
            layout.addComponent(addRenameFolderLayout);
        }

        treeContainerLayout.addComponent(getGermplasmListSource().getUIComponent());
        layout.addComponent(treeContainerLayout);

        if(doIncludeRefreshButton()){
            layout.addComponent(refreshButton);
        }

        addComponent(layout);
    }


    protected String getTreeHeading(){
        return messageSource.getMessage(Message.LISTS);
    }

    protected String getTreeHeadingStyleName(){
        return Bootstrap.Typography.H4.styleName();
    }

    @Override
    public void instantiateComponents() {
        setHeight("580px");
        setWidth("880px");

        heading = new Label();
        heading.setValue(getTreeHeading());
        heading.addStyleName(getTreeHeadingStyleName());
        heading.addStyleName(AppConstants.CssStyles.BOLD);

        treeHeadingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_BUILD_NEW_LIST, heading);

        // if tree will include the toggle button to hide itself
        if (doIncludeToggleButton()){
            toggleListTreeButton = new ToggleButton("Toggle Build New List Pane");
        }

        // assumes that all tree will display control buttons
        if (doIncludeActionsButtons()){
            initializeButtonPanel();
            initializeAddRenameFolderPanel();
        }

        treeContainerLayout = new CssLayout();
        treeContainerLayout.setWidth("100%");

        if (doIncludeRefreshButton()){
            initializeRefreshButton();
        }

        instantiateListComponent();
    }

    public ListTreeActionsListener getTreeActionsListener(){
        return treeActionsListener;
    }

    public void createTree() {
        if (treeContainerLayout != null && treeContainerLayout.getComponentCount() > 0){
            treeContainerLayout.removeComponent(getGermplasmListSource().getUIComponent());
        }
        getGermplasmListSource().removeAllItems();

        createGermplasmList();
        getGermplasmListSource().setStyleName(getMainTreeStyleName());
        getGermplasmListSource().addStyleName(getTreeStyleName());

        getGermplasmListSource().setItemStyleGenerator(new GermplasmListSourceItemStyleGenerator());

        germplasmListsMap = Util.getAllGermplasmLists(germplasmListManager);
        addListTreeItemDescription();

        getGermplasmListSource().setImmediate(true);
        if (doIncludeActionsButtons()){
            germplasmListTreeUtil = new GermplasmListTreeUtil(this, getGermplasmListSource());
        }
        treeContainerLayout.addComponent(getGermplasmListSource().getUIComponent());
        getGermplasmListSource().requestRepaint();

    }

    public void addListTreeItemDescription(){
        getGermplasmListSource().setItemDescriptionGenerator(new GermplasmListSourceItemDescriptionGenerator(this));
    }

    public void setSelectedListId(Object listId){
        this.selectedListId = listId;
        selectListSourceDetails(listId, false);
    }
    public String getSelectedItemCaption(){
        return getGermplasmListSource().getItemCaption(selectedListId);
    }

    public void removeListFromTree(GermplasmList germplasmList){
        Integer currentListId = germplasmList.getId();
        Item item = getGermplasmListSource().getItem(currentListId);
        if (item != null){
            getGermplasmListSource().removeItem(currentListId);
        }
        GermplasmList parent = germplasmList.getParent();
        if (parent == null) {
            getGermplasmListSource().select(LISTS);
            setSelectedListId(LISTS);
        } else {
            getGermplasmListSource().select(parent.getId());
            getGermplasmListSource().expandItem(parent.getId());
            setSelectedListId(parent.getId());
        }
        updateButtons(this.selectedListId);
    }

    public void treeItemClickAction(int germplasmListId) {

        try {

            germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
            selectedListId = germplasmListId;

            boolean isEmptyFolder = isEmptyFolder(germplasmList);
            if (!isEmptyFolder){
                boolean hasChildList = hasChildList(germplasmListId);

                if (!hasChildList){
                    studyClickedAction(germplasmList);
                    //toggle folder
                } else if(hasChildList){
                    folderClickedAction(germplasmList);
                    expandOrCollapseListTreeNode(Integer.valueOf(germplasmListId));
                }

                selectListSourceDetails(germplasmListId, false);
            }else{
                //when an empty folder is clicked
                folderClickedAction(germplasmList);
            }

        } catch (NumberFormatException e) {

            LOG.error("Error clicking of list.", e);
            MessageNotifier.showWarning(getWindow(),
                    messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                    messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
        }catch (MiddlewareQueryException e){
            LOG.error("Error in displaying germplasm list details.", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
    }

    public void expandOrCollapseListTreeNode(Object nodeId){

        if(!this.getGermplasmListSource().isExpanded(nodeId)){
            this.getGermplasmListSource().expandItem(nodeId);
        } else{
            this.getGermplasmListSource().collapseItem(nodeId);
        }

        selectListSourceDetails(nodeId, false);
    }

    public void expandNode(Object itemId){
        getGermplasmListSource().expandItem(itemId);
    }

    public void addGermplasmListNodeToComponent(List<GermplasmList> germplasmListChildren, int parentGermplasmListId){
        for (GermplasmList listChild : germplasmListChildren) {
            if(doAddItem(listChild)){
                String size = "";
                if(!listChild.isFolder()) {
                    try {
    					long numberOfEntries = this.germplasmListManager.countGermplasmListDataByListId(listChild.getId());
    					size = Long.toString(numberOfEntries);
                    } catch (MiddlewareQueryException e) {
    					LOG.error("Error in getting number of entries for list id "+listChild.getId(), e);
    		            size = "0";
    				}
                }
                getGermplasmListSource().addItem(generateCellInfo(listChild.getName(), BreedingManagerUtil.getOwnerListName(listChild.getUserId(), userDataManager),BreedingManagerUtil.getDescriptionForDisplay(listChild), BreedingManagerUtil.getTypeString(listChild.getType(), germplasmListManager), size), listChild.getId());
                setNodeItemIcon(listChild.getId(), listChild.isFolder());
                getGermplasmListSource().setItemCaption(listChild.getId(), listChild.getName());
                getGermplasmListSource().setParent(listChild.getId(), parentGermplasmListId);
                // allow children if list has sub-lists
                getGermplasmListSource().setChildrenAllowed(listChild.getId(), hasChildList(listChild.getId()));
            }
        }
        selectListSourceDetails(parentGermplasmListId, false);
    }    

    private void selectListSourceDetails(Object itemId, boolean nullSelectAllowed){
        getGermplasmListSource().setNullSelectionAllowed(nullSelectAllowed);
        getGermplasmListSource().select(itemId);
        getGermplasmListSource().setValue(itemId);
    }

    public String addRenameItemAction() {
        if (doSaveNewFolder()){
            germplasmListTreeUtil.addFolder(selectedListId, folderTextField);
        } else{

            String oldName = getGermplasmListSource().getItemCaption(selectedListId);
            germplasmListTreeUtil.renameFolderOrList(Integer.valueOf(selectedListId.toString()),
                    treeActionsListener, folderTextField, oldName);
        }
        return folderTextField.getValue().toString().trim();
    }
    public void refreshComponent(){
        this.listId = null;
        createTree();
    }
    public void instantiateListComponent(){
        setGermplasmListSource(new GermplasmListTree());
        createTree();
        germplasmListTreeUtil = new GermplasmListTreeUtil(this, getGermplasmListSource());
    }
    public void reloadTreeItemDescription(){
        germplasmListsMap = Util.getAllGermplasmLists(germplasmListManager);
        addListTreeItemDescription();
    }

    public Map<Integer, GermplasmList> getGermplasmListsMap(){
        return germplasmListsMap;
    }

    @Override
    public void initializeValues() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instantiateComponents();
        initializeValues();
        addListeners();
        layoutComponents();
    }

    public void createGermplasmList() {

        instantiateGermplasmListSourceComponent();

        if (isTreeItemsDraggable()){
            getGermplasmListSource().setDragMode(Tree.TreeDragMode.NODE, Table.TableDragMode.ROW);
        }
        addGermplasmsToTheList();
        addGermplasmListSourceListeners();
        initializeGermplasmList();
    }

    private void initializeGermplasmList(){
        try{
            if(listId != null){
                GermplasmList list = germplasmListManager.getGermplasmListById(listId);

                if(list != null){
                    Deque<GermplasmList> parents = new ArrayDeque<GermplasmList>();
                    GermplasmListTreeUtil.traverseParentsOfList(germplasmListManager, list, parents);

                    getGermplasmListSource().expandItem(LISTS);

                    while(!parents.isEmpty()){
                        GermplasmList parent = parents.pop();
                        getGermplasmListSource().setChildrenAllowed(parent.getId(), true);
                        addGermplasmListNode(parent.getId().intValue());
                        getGermplasmListSource().expandItem(parent.getId());
                    }

                    getGermplasmListSource().setNullSelectionAllowed(false);
                    getGermplasmListSource().select(listId);
                    getGermplasmListSource().setValue(listId);
                    setSelectedListId(listId);
                    updateButtons(listId);
                }

            } else if(selectListsFolderByDefault) {
                getGermplasmListSource().select(LISTS);
                getGermplasmListSource().setValue(LISTS);
                updateButtons(LISTS);
            }
        } catch(MiddlewareQueryException ex){
            LOG.error("Error with getting parents for hierarchy of list id: " + listId, ex);
        }
    }

    private void addGermplasmsToTheList(){
        List<GermplasmList> germplasmListParent = new ArrayList<GermplasmList>();
        try {
            germplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(BATCH_SIZE);
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting top level lists.", e);
            if (getWindow() != null){
                MessageNotifier.showWarning(getWindow(),
                        messageSource.getMessage(Message.ERROR_DATABASE),
                        messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
            }
            germplasmListParent = new ArrayList<GermplasmList>();
        }

        getGermplasmListSource().addItem(generateCellInfo(LISTS, "", "", "", ""), LISTS);
        setNodeItemIcon(LISTS, true);
        getGermplasmListSource().setItemCaption(LISTS, LISTS);

        for (GermplasmList parentList : germplasmListParent) {
            if(doAddItem(parentList)){
            	long size;
				try {
					size = this.germplasmListManager.countGermplasmListDataByListId(parentList.getId());
				} catch (MiddlewareQueryException e) {
					LOG.error("Error in getting top level lists.", e);
		            if (getWindow() != null){
		                MessageNotifier.showWarning(getWindow(),
		                        messageSource.getMessage(Message.ERROR_DATABASE),
		                        messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
		            }
		            return;
				}
                getGermplasmListSource().addItem(generateCellInfo(parentList.getName(), BreedingManagerUtil.getOwnerListName(parentList.getUserId(), userDataManager), BreedingManagerUtil.getDescriptionForDisplay(parentList), BreedingManagerUtil.getTypeString(parentList.getType(), germplasmListManager), parentList.isFolder() ? "" : Long.toString(size)), parentList.getId());
                setNodeItemIcon(parentList.getId(), parentList.isFolder());
                getGermplasmListSource().setItemCaption(parentList.getId(), parentList.getName());
                getGermplasmListSource().setChildrenAllowed(parentList.getId(), hasChildList(parentList.getId()));
                getGermplasmListSource().setParent(parentList.getId(), LISTS);
            }
        }

    }

    private void addGermplasmListSourceListeners(){
        getGermplasmListSource().addListener(new GermplasmListTreeExpandListener(this));
        getGermplasmListSource().addListener(new GermplasmListItemClickListener(this));
        getGermplasmListSource().addListener(new GermplasmListTreeCollapseListener(this));
    }

    public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
        this.germplasmListManager = germplasmListManager;
    }
    
	public void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}
	public void setFolderTextField(TextField folderTextField) {
		this.folderTextField = folderTextField;
	}
	public void setFolderSaveMode(FolderSaveMode folderSaveMode) {
		this.folderSaveMode = folderSaveMode;
	}
	public GermplasmListTreeUtil getGermplasmListTreeUtil() {
		return germplasmListTreeUtil;
	}
	public SimpleResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}
	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public Button getAddFolderBtn() {
		return addFolderBtn;
	}
	public Button getDeleteFolderBtn() {
		return deleteFolderBtn;
	}
	public Button getRenameFolderBtn() {
		return renameFolderBtn;
	}
}
