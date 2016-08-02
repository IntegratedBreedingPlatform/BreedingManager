
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.GermplasmListTree;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.IconButton;
import org.generationcp.breeding.manager.customcomponent.ToggleButton;
import org.generationcp.breeding.manager.customcomponent.generator.GermplasmListSourceItemStyleGenerator;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.listimport.util.ToolTipGenerator;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListTreeCollapseListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.validator.ListNameValidator;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.service.UserTreeStateService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListMetadata;
import org.generationcp.middleware.pojos.UserDefinedField;
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
public abstract class ListSelectorComponent extends CssLayout implements InitializingBean, BreedingManagerLayout, Tree.ExpandListener {

	private static final long serialVersionUID = 6042782367848192853L;

	private static final Logger LOG = LoggerFactory.getLogger(ListSelectorComponent.class);

	public static final int BATCH_SIZE = 500;
	public static final String REFRESH_BUTTON_ID = "ListManagerTreeComponent Refresh Button";
	public static final String LISTS = "Lists";

	protected enum FolderSaveMode {
		ADD, RENAME
	}

	protected GermplasmListSource germplasmListSource;

	@Autowired
	protected GermplasmListManager germplasmListManager;

	@Autowired
	private ContextUtil util;

	@Autowired
	private UserTreeStateService userTreeStateService;

	@Autowired
	protected SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private UserDataManager userDataManager;

	protected HorizontalLayout controlButtonsLayout;
	protected HorizontalLayout ctrlBtnsLeftSubLayout;
	protected HorizontalLayout ctrlBtnsRightSubLayout;
	protected CssLayout treeContainerLayout;

	protected Integer listId;
	protected GermplasmListTreeUtil germplasmListTreeUtil;
	
	protected ToolTipGenerator tooltipGenerator;

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

	public GermplasmListSource getGermplasmListSource() {
		return this.germplasmListSource;
	}

	public void setGermplasmListSource(final GermplasmListSource newGermplasmListSource) {
		this.germplasmListSource = newGermplasmListSource;
	}

	protected boolean doSaveNewFolder() {
		return FolderSaveMode.ADD.equals(this.folderSaveMode);
	}

	public boolean usedInSubWindow() {
		return true;
	}

	protected boolean doIncludeTreeHeadingIcon() {
		return true;
	}

	protected boolean doIncludeToggleButton() {
		return false;
	}

	public void initializeRefreshButton() {
		this.refreshButton = new Button();
		this.refreshButton.setData(ListSelectorComponent.REFRESH_BUTTON_ID);
		this.refreshButton.setCaption(this.messageSource.getMessage(Message.REFRESH_LABEL));
		this.refreshButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
	}

	protected void initializeButtonPanel() {
		this.renameFolderBtn =
				new IconButton("<span class='bms-edit' style='left: 2px; color: #0083c0;font-size: 18px; font-weight: bold;'></span>",
						"Rename Item");
		this.renameFolderBtn.setEnabled(false);

		this.addFolderBtn =
				new IconButton("<span class='bms-add' style='left: 2px; color: #00a950;font-size: 18px; font-weight: bold;'></span>",
						"Add New Folder");
		this.addFolderBtn.setEnabled(false);

		this.deleteFolderBtn =
				new IconButton("<span class='bms-delete' style='left: 2px; color: #f4a41c;font-size: 18px; font-weight: bold;'></span>",
						"Delete Item");
		this.deleteFolderBtn.setEnabled(false);
		this.deleteFolderBtn.setData(this);

		this.ctrlBtnsRightSubLayout = new HorizontalLayout();
		this.ctrlBtnsRightSubLayout.setHeight("30px");
		this.ctrlBtnsRightSubLayout.addComponent(this.addFolderBtn);
		this.ctrlBtnsRightSubLayout.addComponent(this.renameFolderBtn);
		this.ctrlBtnsRightSubLayout.addComponent(this.deleteFolderBtn);
		this.ctrlBtnsRightSubLayout.setComponentAlignment(this.addFolderBtn, Alignment.BOTTOM_RIGHT);
		this.ctrlBtnsRightSubLayout.setComponentAlignment(this.renameFolderBtn, Alignment.BOTTOM_RIGHT);
		this.ctrlBtnsRightSubLayout.setComponentAlignment(this.deleteFolderBtn, Alignment.BOTTOM_RIGHT);

		this.ctrlBtnsLeftSubLayout = new HorizontalLayout();
		this.ctrlBtnsLeftSubLayout.setHeight("30px");

		if (this.doIncludeToggleButton()) {
			this.ctrlBtnsLeftSubLayout.addComponent(this.toggleListTreeButton);
			this.ctrlBtnsLeftSubLayout.setComponentAlignment(this.toggleListTreeButton, Alignment.BOTTOM_LEFT);
		}

		if (this.doIncludeTreeHeadingIcon()) {
			this.ctrlBtnsLeftSubLayout.addComponent(this.treeHeadingLayout);
			this.heading.setWidth("80px");
		} else {
			this.ctrlBtnsLeftSubLayout.addComponent(this.heading);
			this.heading.setWidth("140px");
		}

		this.controlButtonsLayout = new HorizontalLayout();
		this.controlButtonsLayout.setWidth("100%");
		this.controlButtonsLayout.setHeight("30px");
		this.controlButtonsLayout.setSpacing(true);

		this.controlButtonsLayout.addComponent(this.ctrlBtnsLeftSubLayout);
		this.controlButtonsLayout.addComponent(this.ctrlBtnsRightSubLayout);
		this.controlButtonsLayout.setComponentAlignment(this.ctrlBtnsLeftSubLayout, Alignment.BOTTOM_LEFT);
		this.controlButtonsLayout.setComponentAlignment(this.ctrlBtnsRightSubLayout, Alignment.BOTTOM_RIGHT);

	}

	protected void initializeAddRenameFolderPanel() {
		this.folderLabel = new Label("Folder");
		this.folderLabel.addStyleName(AppConstants.CssStyles.BOLD);
		final Label mandatoryMarkLabel = new MandatoryMarkLabel();

		this.folderTextField = new TextField();
		this.folderTextField.setMaxLength(50);
		this.folderTextField.setValidationVisible(false);

		this.folderTextField.setRequired(true);
		this.folderTextField.setRequiredError("Please specify item name.");
		this.listNameValidator = new ListNameValidator();
		this.folderTextField.addValidator(this.listNameValidator);

		this.saveFolderButton = new Button("<span class='glyphicon glyphicon-ok' style='right: 2px;'></span>");
		this.saveFolderButton.setHtmlContentAllowed(true);
		this.saveFolderButton.setDescription(this.messageSource.getMessage(Message.SAVE_LABEL));
		this.saveFolderButton.setStyleName(Bootstrap.Buttons.SUCCESS.styleName());

		this.cancelFolderButton = new Button("<span class='glyphicon glyphicon-remove' style='right: 2px;'></span>");
		this.cancelFolderButton.setHtmlContentAllowed(true);
		this.cancelFolderButton.setDescription(this.messageSource.getMessage(Message.CANCEL));
		this.cancelFolderButton.setStyleName(Bootstrap.Buttons.DANGER.styleName());

		this.addRenameFolderLayout = new HorizontalLayout();
		this.addRenameFolderLayout.setSpacing(true);

		final HorizontalLayout rightPanelLayout = new HorizontalLayout();
		rightPanelLayout.addComponent(this.folderTextField);
		rightPanelLayout.addComponent(this.saveFolderButton);
		rightPanelLayout.addComponent(this.cancelFolderButton);

		this.addRenameFolderLayout.addComponent(this.folderLabel);
		this.addRenameFolderLayout.addComponent(mandatoryMarkLabel);
		this.addRenameFolderLayout.addComponent(rightPanelLayout);

		this.addRenameFolderLayout.setVisible(false);
	}

	public void updateButtons(final Object itemId) {
		this.setSelectedListId(itemId);

		// If any of the lists/folders is selected
		if (NumberUtils.isNumber(itemId.toString())) {
			this.addFolderBtn.setEnabled(true);
			this.renameFolderBtn.setEnabled(true);
			this.deleteFolderBtn.setEnabled(true);
		} else if (itemId.toString().equals(ListSelectorComponent.LISTS)) {
			this.addFolderBtn.setEnabled(true);
			this.renameFolderBtn.setEnabled(false);
			this.deleteFolderBtn.setEnabled(false);
		}
	}

	public void showAddRenameFolderSection(final boolean showFolderSection) {
		this.addRenameFolderLayout.setVisible(showFolderSection);

		if (showFolderSection && this.folderSaveMode != null) {
			this.folderLabel.setValue(this.doSaveNewFolder() ? "Add Folder" : "Rename Item");

			if (this.doSaveNewFolder()) {
				this.folderTextField.setValue("");

				// If rename, set existing name
			} else if (this.selectedListId != null) {
				final String itemCaption = this.getSelectedItemCaption();
				if (itemCaption != null) {
					this.listNameValidator.setCurrentListName(itemCaption);
					this.folderTextField.setValue(itemCaption);
				}
			}
			this.folderTextField.focus();

		}
	}

	protected boolean isEmptyFolder(final GermplasmList list) throws MiddlewareQueryException {
		final boolean isFolder = list.getType().equalsIgnoreCase(AppConstants.DB.FOLDER);
		return isFolder && !this.hasChildList(list.getId());
	}

	protected boolean hasChildList(final int listId) {

		List<GermplasmList> listChildren = new ArrayList<GermplasmList>();

		try {
			listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, this.getCurrentProgramUUID(), 0, 1);
		} catch (final MiddlewareQueryException e) {
			ListSelectorComponent.LOG.error("Error in getting germplasm lists by parent id.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			listChildren = new ArrayList<>();
		}

		return !listChildren.isEmpty();
	}

	public void reinitializeTree(final boolean isSaveList) {
		List<String> parsedState = null;

		try {
			final Integer userID = this.util.getCurrentUserLocalId();
			final String programUUID = this.util.getCurrentProgramUUID();

			if (isSaveList) {
				parsedState = this.userTreeStateService.getUserProgramTreeStateForSaveList(userID, programUUID);
			} else {
				parsedState =
						this.userTreeStateService.getUserProgramTreeStateByUserIdProgramUuidAndType(userID, programUUID,
								ListTreeState.GERMPLASM_LIST.name());
			}

			if (parsedState.isEmpty()) {
				this.getGermplasmListSource().collapseItem(ListSelectorComponent.LISTS);
				return;
			}

			this.getGermplasmListSource().expandItem(ListSelectorComponent.LISTS);

			for (final String s : parsedState) {
				final String trimmed = s.trim();
				if (!StringUtils.isNumeric(trimmed)) {
					continue;
				}

				final int itemId = Integer.parseInt(trimmed);
				this.getGermplasmListSource().expandItem(itemId);
			}

            if (isSaveList) {
                // the tree state returned for save list navigation has, as its last item, the folder previously used to save
                String previousSavedFolder = parsedState.get(parsedState.size() - 1);
                this.getGermplasmListSource().select(previousSavedFolder);
            } else {
                this.getGermplasmListSource().clearSelection();
            }
		} catch (final MiddlewareQueryException e) {
			ListSelectorComponent.LOG.error(e.getMessage(), e);
		}
	}

	public void addGermplasmListNode(final int parentGermplasmListId) {
		List<GermplasmList> germplasmListChildren;

		try {
			germplasmListChildren =
					this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, this.getCurrentProgramUUID(),
							ListSelectorComponent.BATCH_SIZE);
		} catch (final MiddlewareQueryException e) {
			ListSelectorComponent.LOG.error("Error in getting germplasm lists by parent id.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			germplasmListChildren = new ArrayList<>();
		}
		this.addGermplasmListNodeToComponent(germplasmListChildren, parentGermplasmListId);

	}

	protected String getCurrentProgramUUID() {
		return this.util.getCurrentProgramUUID();
	}

	public boolean doAddItem(final GermplasmList list) {
		return !this.doShowFoldersOnly() || this.isFolder(list.getId());
	}

	public boolean isFolder(final Object itemId) {
		try {
			final int currentListId = Integer.valueOf(itemId.toString());
			final GermplasmList currentGermplasmList = this.germplasmListManager.getGermplasmListById(currentListId);
			if (currentGermplasmList == null) {
				return false;
			}
			return currentGermplasmList.getType().equalsIgnoreCase(AppConstants.DB.FOLDER);
		} catch (final MiddlewareQueryException e) {
			ListSelectorComponent.LOG.debug("Checking is folder, cause the MW exception");
			ListSelectorComponent.LOG.error(e.getMessage(), e);
			return false;
		} catch (final NumberFormatException e) {
			boolean returnVal = false;
			if (this.listId != null && this.listId.toString().equals(ListSelectorComponent.LISTS)) {
				returnVal = true;
			}
			return returnVal;
		}
	}

	public Object getSelectedListId() {
		return this.selectedListId;
	}

	public void setListId(final Integer listId) {
		this.listId = listId;
	}

	@Override
	public void addListeners() {
		if (this.doIncludeRefreshButton()) {
			this.refreshButton.addListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(final Button.ClickEvent event) {
					ListSelectorComponent.this.refreshComponent();
				}
			});
		}

		if (this.doIncludeActionsButtons()) {
			this.addFolderActionsListener();
		}
	}

	@SuppressWarnings("serial")
	protected void addFolderActionsListener() {
		this.renameFolderBtn.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 4606520616351364666L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				ListSelectorComponent.this.folderSaveMode = FolderSaveMode.RENAME;
				ListSelectorComponent.this.showAddRenameFolderSection(true);
			}
		});

		this.addFolderBtn.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = -7317775128679479757L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				ListSelectorComponent.this.folderSaveMode = FolderSaveMode.ADD;
				ListSelectorComponent.this.showAddRenameFolderSection(true);
			}
		});

		this.deleteFolderBtn.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 3963269144924095369L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final Object data = event.getButton().getData();
				if (data instanceof ListSelectorComponent) {
					ListSelectorComponent.this.germplasmListTreeUtil.deleteFolderOrList((ListSelectorComponent) data,
							Integer.valueOf(ListSelectorComponent.this.selectedListId.toString()),
							ListSelectorComponent.this.treeActionsListener);
				}
			}
		});

		this.folderTextField.addShortcutListener(new ShortcutListener("ENTER", ShortcutAction.KeyCode.ENTER, null) {

			/**
			 *
			 */
			private static final long serialVersionUID = 3453562703942122213L;

			@Override
			public void handleAction(final Object sender, final Object target) {
				ListSelectorComponent.this.addRenameItemAction();
			}
		});

		this.saveFolderButton.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 8280338644831541745L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				ListSelectorComponent.this.addRenameItemAction();
			}
		});

		this.cancelFolderButton.addListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 2812915644280474197L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				ListSelectorComponent.this.showAddRenameFolderSection(false);
			}
		});
	}

	public void refreshRemoteTree() {
	}

	public void studyClickedAction(final GermplasmList germplasmList) {
		if (this.treeActionsListener != null && germplasmList != null) {
			this.treeActionsListener.studyClicked(germplasmList);
		}
	}

	public void folderClickedAction(final GermplasmList germplasmList) {
		if (this.treeActionsListener != null && germplasmList != null) {
			this.treeActionsListener.folderClicked(germplasmList);
		}
	}

	public void toggleFolderSectionForItemSelected() {
		if (this.addRenameFolderLayout != null && this.addRenameFolderLayout.isVisible()) {
			Integer currentListId = null;
			if (this.selectedListId instanceof Integer) {
				currentListId = Integer.valueOf(this.selectedListId.toString());
			}

			if (!this.doSaveNewFolder()) {
				if (currentListId != null) {
					this.folderTextField.setValue(this.getSelectedItemCaption());
					this.folderTextField.focus();
				} else if (ListSelectorComponent.LISTS.equals(this.selectedListId)) {
					this.showAddRenameFolderSection(false);
				}

			}
		}
	}

	@Override
	public void layoutComponents() {
		this.setWidth("100%");

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setWidth("100%");

		if (this.doIncludeActionsButtons()) {
			layout.addComponent(this.controlButtonsLayout);
			layout.addComponent(this.addRenameFolderLayout);
		}

		this.treeContainerLayout.addComponent(this.getGermplasmListSource().getUIComponent());
		layout.addComponent(this.treeContainerLayout);

		if (this.doIncludeRefreshButton()) {
			layout.addComponent(this.refreshButton);
		}

		this.addComponent(layout);
	}

	protected String getTreeHeading() {
		return this.messageSource.getMessage(Message.LISTS);
	}

	protected String getTreeHeadingStyleName() {
		return Bootstrap.Typography.H4.styleName();
	}

	@Override
	public void instantiateComponents() {
		this.setHeight("450px");
		this.setWidth("880px");

		this.heading = new Label();
		this.heading.setValue(this.getTreeHeading());
		this.heading.addStyleName(this.getTreeHeadingStyleName());
		this.heading.addStyleName(AppConstants.CssStyles.BOLD);

		this.treeHeadingLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_BUILD_NEW_LIST, this.heading);

		// if tree will include the toggle button to hide itself
		if (this.doIncludeToggleButton()) {
			this.toggleListTreeButton = new ToggleButton("Toggle Build New List Pane");
		}

		// assumes that all tree will display control buttons
		if (this.doIncludeActionsButtons()) {
			this.initializeButtonPanel();
			this.initializeAddRenameFolderPanel();
		}

		this.treeContainerLayout = new CssLayout();
		this.treeContainerLayout.setWidth("100%");

		if (this.doIncludeRefreshButton()) {
			this.initializeRefreshButton();
		}

		this.instantiateListComponent();
	}

	public ListTreeActionsListener getTreeActionsListener() {
		return this.treeActionsListener;
	}

	public void createTree() {
		if (this.treeContainerLayout != null && this.treeContainerLayout.getComponentCount() > 0) {
			this.treeContainerLayout.removeComponent(this.getGermplasmListSource().getUIComponent());
		}
		this.getGermplasmListSource().removeAllItems();

		this.createGermplasmList();
		this.getGermplasmListSource().setStyleName(this.getMainTreeStyleName());
		this.getGermplasmListSource().addStyleName(this.getTreeStyleName());

		this.getGermplasmListSource().setItemStyleGenerator(new GermplasmListSourceItemStyleGenerator());

		this.getGermplasmListSource().setImmediate(true);
		if (this.doIncludeActionsButtons()) {
			this.germplasmListTreeUtil = new GermplasmListTreeUtil(this, this.getGermplasmListSource());
		}
		this.treeContainerLayout.addComponent(this.getGermplasmListSource().getUIComponent());
		this.getGermplasmListSource().requestRepaint();

	}

	public void setSelectedListId(final Object listId) {
		this.selectedListId = listId;
		this.selectListSourceDetails(listId, false);
	}

	public String getSelectedItemCaption() {
		return this.getGermplasmListSource().getItemCaption(this.selectedListId);
	}

	public void removeListFromTree(final GermplasmList germplasmList) {
		final Integer currentListId = germplasmList.getId();
		final Item item = this.getGermplasmListSource().getItem(currentListId);
		if (item != null) {
			this.getGermplasmListSource().removeItem(currentListId);
		}
		final GermplasmList parent = germplasmList.getParent();
		if (parent == null) {
			this.getGermplasmListSource().select(ListSelectorComponent.LISTS);
			this.setSelectedListId(ListSelectorComponent.LISTS);
		} else {
			this.getGermplasmListSource().select(parent.getId());
			this.getGermplasmListSource().expandItem(parent.getId());
			this.setSelectedListId(parent.getId());
		}
		this.updateButtons(this.selectedListId);
	}

	public void treeItemClickAction(final int germplasmListId) {

		try {

			this.germplasmList = this.germplasmListManager.getGermplasmListById(germplasmListId);
			this.selectedListId = germplasmListId;

			final boolean isEmptyFolder = this.isEmptyFolder(this.germplasmList);
			if (!isEmptyFolder) {
				final boolean hasChildList = this.hasChildList(germplasmListId);

				if (!hasChildList) {
					this.studyClickedAction(this.germplasmList);
					// toggle folder
				} else if (hasChildList) {
					this.folderClickedAction(this.germplasmList);
					this.expandOrCollapseListTreeNode(Integer.valueOf(germplasmListId));
				}

				this.selectListSourceDetails(germplasmListId, false);
			} else {
				// when an empty folder is clicked
				this.folderClickedAction(this.germplasmList);
			}

		} catch (final NumberFormatException e) {

			ListSelectorComponent.LOG.error("Error clicking of list.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
					this.messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
		} catch (final MiddlewareQueryException e) {
			ListSelectorComponent.LOG.error("Error in displaying germplasm list details.", e);
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
		}
	}

	public void expandOrCollapseListTreeNode(final Object nodeId) {

		if (!this.getGermplasmListSource().isExpanded(nodeId)) {
			this.getGermplasmListSource().expandItem(nodeId);
		} else {
			this.getGermplasmListSource().collapseItem(nodeId);
		}

		this.selectListSourceDetails(nodeId, false);
	}

	public void expandNode(final Object itemId) {
		this.getGermplasmListSource().expandItem(itemId);
	}

	public void addGermplasmListNodeToComponent(final List<GermplasmList> germplasmListChildren, final int parentGermplasmListId) {
		List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();
		final Map<Integer, GermplasmListMetadata> allListMetaData = germplasmListManager.getGermplasmListMetadata(germplasmListChildren);

		for (final GermplasmList listChild : germplasmListChildren) {
			if (this.doAddItem(listChild)) {
				GermplasmListMetadata listMetadata = allListMetaData.get(listChild.getId());
				final String listSize = listMetadata != null ? String.valueOf(listMetadata.getNumberOfEntries()) : "";
				final String listOwner = listMetadata != null ? listMetadata.getOwnerName() : "";

				this.getGermplasmListSource()
						.addItem(this.generateCellInfo(listChild.getName(), listOwner,
								BreedingManagerUtil.getDescriptionForDisplay(listChild),
								BreedingManagerUtil.getTypeString(listChild.getType(), listTypes), listSize),
								listChild.getId());
				this.setNodeItemIcon(listChild.getId(), listChild.isFolder());
				this.getGermplasmListSource().setItemCaption(listChild.getId(), listChild.getName());
				this.getGermplasmListSource().setParent(listChild.getId(), parentGermplasmListId);
				// allow children if list has sub-lists
				this.getGermplasmListSource().setChildrenAllowed(listChild.getId(), listChild.isFolder());
			}
		}
		
		if(this.tooltipGenerator == null) {
			this.tooltipGenerator = new ToolTipGenerator(BreedingManagerUtil.getAllNamesAsMap(userDataManager), germplasmListManager.getGermplasmListTypes());
		}
		
		this.getGermplasmListSource().setItemDescriptionGenerator(tooltipGenerator.getItemDescriptionGenerator(germplasmListChildren));

		this.selectListSourceDetails(parentGermplasmListId, false);
	}

	private void selectListSourceDetails(final Object itemId, final boolean nullSelectAllowed) {
		this.getGermplasmListSource().setNullSelectionAllowed(nullSelectAllowed);
		this.getGermplasmListSource().select(itemId);
		this.getGermplasmListSource().setValue(itemId);
	}

	public String addRenameItemAction() {
		if (this.doSaveNewFolder()) {
			this.germplasmListTreeUtil.addFolder(this.selectedListId, this.folderTextField);
		} else {

			final String oldName = this.getGermplasmListSource().getItemCaption(this.selectedListId);
			this.germplasmListTreeUtil.renameFolderOrList(Integer.valueOf(this.selectedListId.toString()), this.treeActionsListener,
					this.folderTextField, oldName);
		}
		return this.folderTextField.getValue().toString().trim();
	}

	public void refreshComponent() {
		this.listId = null;
		this.createTree();
		this.reinitializeTree(false);
	}

	public void instantiateListComponent() {
		this.setGermplasmListSource(new GermplasmListTree());
		this.createTree();
		this.germplasmListTreeUtil = new GermplasmListTreeUtil(this, this.getGermplasmListSource());
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public void createGermplasmList() {

		this.instantiateGermplasmListSourceComponent();

		if (this.isTreeItemsDraggable()) {
			this.getGermplasmListSource().setDragMode(Tree.TreeDragMode.NODE, Table.TableDragMode.ROW);
		}

		this.addGermplasmListSourceListeners();
		this.addGermplasmsToTheList();

		this.initializeGermplasmList();
	}

	private void initializeGermplasmList() {
		try {
			if (this.listId != null) {
				final GermplasmList list = this.germplasmListManager.getGermplasmListById(this.listId);

				if (list != null) {
					final Deque<GermplasmList> parents = new ArrayDeque<GermplasmList>();
					GermplasmListTreeUtil.traverseParentsOfList(this.germplasmListManager, list, parents);

					this.getGermplasmListSource().expandItem(ListSelectorComponent.LISTS);

					while (!parents.isEmpty()) {
						final GermplasmList parent = parents.pop();
						this.getGermplasmListSource().setChildrenAllowed(parent.getId(), true);
						this.addGermplasmListNode(parent.getId().intValue());
						this.getGermplasmListSource().expandItem(parent.getId());
					}

					this.getGermplasmListSource().setNullSelectionAllowed(false);
					this.getGermplasmListSource().select(this.listId);
					this.getGermplasmListSource().setValue(this.listId);
					this.setSelectedListId(this.listId);
					this.updateButtons(this.listId);
				}

			} else if (this.selectListsFolderByDefault) {
				this.getGermplasmListSource().select(ListSelectorComponent.LISTS);
				this.getGermplasmListSource().setValue(ListSelectorComponent.LISTS);
				this.updateButtons(ListSelectorComponent.LISTS);
			}
		} catch (final MiddlewareQueryException ex) {
			ListSelectorComponent.LOG.error("Error with getting parents for hierarchy of list id: " + this.listId, ex);
		}
	}

	private void addGermplasmsToTheList() {
		List<GermplasmList> germplasmListParent = new ArrayList<GermplasmList>();
		try {
			germplasmListParent =
					this.germplasmListManager.getAllTopLevelListsBatched(this.getCurrentProgramUUID(), ListSelectorComponent.BATCH_SIZE);
		} catch (final MiddlewareQueryException e) {
			ListSelectorComponent.LOG.error("Error in getting top level lists.", e);
			if (this.getWindow() != null) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
			}
			germplasmListParent = new ArrayList<GermplasmList>();
		}

		this.getGermplasmListSource().addItem(this.generateCellInfo(ListSelectorComponent.LISTS, "", "", "", ""),
				ListSelectorComponent.LISTS);
		this.setNodeItemIcon(ListSelectorComponent.LISTS, true);
		this.getGermplasmListSource().setItemCaption(ListSelectorComponent.LISTS, ListSelectorComponent.LISTS);
		List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();
		
		
		final Map<Integer, GermplasmListMetadata> allListMetaData = germplasmListManager.getGermplasmListMetadata(germplasmListParent);

		for (final GermplasmList parentList : germplasmListParent) {
			if (this.doAddItem(parentList)) {
				GermplasmListMetadata listMetadata = allListMetaData.get(parentList.getId());
				final String listSize = listMetadata != null ? String.valueOf(listMetadata.getNumberOfEntries()) : "";
				final String listOwner = listMetadata != null ? listMetadata.getOwnerName() : "";
				this.getGermplasmListSource().addItem(
						this.generateCellInfo(parentList.getName(), listOwner, BreedingManagerUtil.getDescriptionForDisplay(parentList),
								BreedingManagerUtil.getTypeString(parentList.getType(), listTypes), listSize),
						parentList.getId());
				this.setNodeItemIcon(parentList.getId(), parentList.isFolder());
				this.getGermplasmListSource().setItemCaption(parentList.getId(), parentList.getName());
				this.getGermplasmListSource().setChildrenAllowed(parentList.getId(), parentList.isFolder());
				this.getGermplasmListSource().setParent(parentList.getId(), ListSelectorComponent.LISTS);
			}
		}

	}

	// logic used when expanding nodes
	@Override
	public void nodeExpand(final Tree.ExpandEvent event) {
		if (!event.getItemId().toString().equals(ListSelectorComponent.LISTS)) {
			try {
				this.addGermplasmListNode(Integer.valueOf(event.getItemId().toString()));
			} catch (final InternationalizableException e) {
				ListSelectorComponent.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(event.getComponent().getWindow(), e.getCaption(), e.getDescription());
			}
		}

		this.setSelectedListId(event.getItemId());
		this.updateButtons(event.getItemId());
		this.toggleFolderSectionForItemSelected();
	}

	private void addGermplasmListSourceListeners() {
		this.getGermplasmListSource().addListener(this);
		this.getGermplasmListSource().addListener(new GermplasmListItemClickListener(this));
		this.getGermplasmListSource().addListener(new GermplasmListTreeCollapseListener(this));
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setFolderTextField(final TextField folderTextField) {
		this.folderTextField = folderTextField;
	}

	public void setFolderSaveMode(final FolderSaveMode folderSaveMode) {
		this.folderSaveMode = folderSaveMode;
	}

	public GermplasmListTreeUtil getGermplasmListTreeUtil() {
		return this.germplasmListTreeUtil;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public Button getAddFolderBtn() {
		return this.addFolderBtn;
	}

	public Button getDeleteFolderBtn() {
		return this.deleteFolderBtn;
	}

	public Button getRenameFolderBtn() {
		return this.renameFolderBtn;
	}

	public void setUtil(final ContextUtil util) {
		this.util = util;
	}
}
