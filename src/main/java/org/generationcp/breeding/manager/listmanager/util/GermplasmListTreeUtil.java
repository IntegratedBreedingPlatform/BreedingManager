
package org.generationcp.breeding.manager.listmanager.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customcomponent.handler.GermplasmListSourceDropHandler;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmListTreeUtil implements Serializable {

	private static final String FOLDER_TYPE = "FOLDER";

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtil.class);

	private ListSelectorComponent source;
	private GermplasmListSource targetListSource;

	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private UserDataManager userDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	public GermplasmListTreeUtil() {

	}

	public GermplasmListTreeUtil(ListSelectorComponent source, GermplasmListSource targetListSource) {
		this.source = source;
		this.targetListSource = targetListSource;
		this.setupTreeDragAndDropHandler();
	}

	public boolean setParent(Object sourceItemId, Object targetItemId) {

		if (sourceItemId.equals(ListSelectorComponent.LISTS)) {
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE),
					this.messageSource.getMessage(Message.UNABLE_TO_MOVE_ROOT_FOLDERS));
			return false;
		}

		if (this.isSourceItemHasChildren(sourceItemId)) {
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE),
					this.messageSource.getMessage(Message.ITEM_HAS_A_CHILD));
			return false;
		}

		Integer sourceId = null;
		Integer targetId = null;

		if (sourceItemId != null && !sourceItemId.equals(ListSelectorComponent.LISTS)) {
			sourceId = Integer.valueOf(sourceItemId.toString());
		}
		if (targetItemId != null && !targetItemId.equals(ListSelectorComponent.LISTS)) {
			targetId = Integer.valueOf(targetItemId.toString());
		}

		// Apply to back-end data
		try {
			GermplasmList sourceGermplasmList = this.germplasmListManager.getGermplasmListById(sourceId);
			if (targetId != null) {
				GermplasmList targetGermplasmList = this.germplasmListManager.getGermplasmListById(targetId);
				sourceGermplasmList.setParent(targetGermplasmList);
			} else {
				sourceGermplasmList.setParent(null);
			}
			this.germplasmListManager.updateGermplasmList(sourceGermplasmList);

		} catch (MiddlewareQueryException e) {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_INTERNAL),
					this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			GermplasmListTreeUtil.LOG.error("Error with update the germplasm list.", e);
		}

		// apply to UI
		if (targetItemId == null || this.targetListSource.getItem(targetItemId) == null) {
			this.targetListSource.setChildrenAllowed(sourceItemId, true);
			this.targetListSource.setParent(sourceItemId, ListSelectorComponent.LISTS);
			this.targetListSource.expandItem(ListSelectorComponent.LISTS);
		} else {
			this.targetListSource.setChildrenAllowed(targetItemId, true);
			this.targetListSource.setParent(sourceItemId, targetItemId);
			this.targetListSource.expandItem(targetItemId);
		}

		this.source.setSelectedListId(sourceItemId);
		this.targetListSource.select(sourceItemId);
		this.targetListSource.setValue(sourceItemId);
		return true;
	}

	protected boolean isSourceItemHasChildren(Object sourceItemId) {
		List<GermplasmList> listChildren = new ArrayList<GermplasmList>();

		try {
			listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(Integer.valueOf(sourceItemId.toString()),
					this.getCurrentProgramUUID());
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtil.LOG.error("Error in getting germplasm lists by parent id.", e);
			listChildren = new ArrayList<GermplasmList>();
		}

		return !listChildren.isEmpty();
	}

	public void setupTreeDragAndDropHandler() {
		this.targetListSource.setDropHandler(new GermplasmListSourceDropHandler(this.targetListSource, this.source, this));
	}

	public void addFolder(final Object parentItemId, final TextField folderTextField) {

		Integer newFolderId = null;
		GermplasmList newFolder = new GermplasmList();
		GermplasmList parentList = null;

		try {
			folderTextField.validate();
			String folderName = folderTextField.getValue().toString().trim();

			Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();

			newFolder.setName(folderName);
			newFolder.setDescription(folderName);
			newFolder.setType(GermplasmListTreeUtil.FOLDER_TYPE);
			newFolder.setStatus(0);
			newFolder.setUserId(ibdbUserId);
			newFolder.setDate(DateUtil.getCurrentDateAsLongValue());
			newFolder.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

			if (parentItemId == null || parentItemId instanceof String || this.targetListSource.getItem(parentItemId) == null) {
				newFolder.setParent(null);
			} else if (!this.source.isFolder(parentItemId)) {
				parentList = this.germplasmListManager.getGermplasmListById((Integer) parentItemId);
				newFolder.setParent(this.germplasmListManager.getGermplasmListById(parentList.getParentId()));
			} else {
				newFolder.setParent(this.germplasmListManager.getGermplasmListById((Integer) parentItemId));
			}

			newFolderId = this.germplasmListManager.addGermplasmList(newFolder);

			// update UI
			this.addFolderToTree(parentItemId, folderName, newFolderId, newFolder, parentList);

		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), e.getMessage());
			GermplasmListTreeUtil.LOG.error("Error adding new folder.", e);
		} catch (MiddlewareQueryException e) {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_INTERNAL),
					this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			GermplasmListTreeUtil.LOG.error("Error with adding the new germplasm list.", e);
		}
	}

	public String renameFolderOrList(final Integer listId, final ListTreeActionsListener listener, TextField folderTextField, String oldName) {

		String newName = folderTextField.getValue().toString().trim();
		if (newName.equals(oldName)) {
			this.source.showAddRenameFolderSection(false);
			return "";
		}

		try {
			folderTextField.validate();

			GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);

			germplasmList.setName(newName);
			this.germplasmListManager.updateGermplasmList(germplasmList);

			this.targetListSource.setItemCaption(listId, newName);
			this.targetListSource.select(listId);

			// rename tabs
			if (listener != null) {
				listener.updateUIForRenamedList(germplasmList, newName);
			}

			this.source.showAddRenameFolderSection(false);
			this.source.refreshRemoteTree();
			if (this.source.getWindow() != null) {
				MessageNotifier.showMessage(this.source.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
						"Item renamed successfully.");
			}
			return this.targetListSource.getItemCaption(listId);

		} catch (MiddlewareQueryException e) {
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), e.getMessage());
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
		}
		return "";
	}

	public void deleteFolderOrList(final ListSelectorComponent listSelectorComponent, final Integer lastItemId,
			final ListTreeActionsListener listener) {

		final Window mainWindow;
		if (this.source.usedInSubWindow()) {
			mainWindow = this.source.getWindow().getParent();
		} else {
			mainWindow = this.source.getWindow();
		}

		try {
			this.validateItemToDelete(lastItemId);
		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(mainWindow, e.getMessage());
			GermplasmListTreeUtil.LOG.error("Error validation for deleting a list.", e);
			return;
		}

		final GermplasmList finalGpList = this.getGermplasmList(lastItemId);
		ConfirmDialog.show(mainWindow, this.messageSource.getMessage(Message.DELETE_ITEM),
				this.messageSource.getMessage(Message.DELETE_ITEM_CONFIRM), this.messageSource.getMessage(Message.YES),
				this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

			private static final long serialVersionUID = -6164460688355101277L;

			@Override
			public void onClose(ConfirmDialog dialog) {
				if (dialog.isConfirmed()) {
					try {
						ListCommonActionsUtil.deleteGermplasmList(GermplasmListTreeUtil.this.germplasmListManager, finalGpList,
								GermplasmListTreeUtil.this.contextUtil, GermplasmListTreeUtil.this.source.getWindow(),
								GermplasmListTreeUtil.this.messageSource, "item");
						listSelectorComponent.removeListFromTree(finalGpList);
						GermplasmListTreeUtil.this.source.refreshRemoteTree();
						((BreedingManagerApplication) mainWindow.getApplication()).updateUIForDeletedList(finalGpList);
					} catch (MiddlewareQueryException e) {
						MessageNotifier.showError(mainWindow,
								GermplasmListTreeUtil.this.messageSource.getMessage(Message.INVALID_OPERATION), e.getMessage());
						GermplasmListTreeUtil.LOG.error("Error with deleting a germplasm list.", e);
					}
				}
			}
		});

	}

	protected void validateItemToDelete(Integer itemId) {
		GermplasmList gpList = this.getGermplasmList(itemId);

		this.validateIfItemExist(itemId, gpList);
		this.validateItemByStatusAndUser(gpList);
		this.validateItemIfItIsAFolderWithContent(gpList);
	}

	private void validateIfItemExist(Integer itemId, GermplasmList gpList) {
		if (itemId == null) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.ERROR_NO_SELECTION));
		}

		if (!this.doesGermplasmListExist(gpList)) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.ERROR_ITEM_DOES_NOT_EXISTS));
		}
	}

	private boolean doesGermplasmListExist(GermplasmList germplasmList) {
		if (germplasmList == null) {
			return false;
		}
		return true;
	}

	private void validateItemByStatusAndUser(GermplasmList gpList) {
		if (this.isListLocked(gpList)) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LOCKED_LIST));
		}

		if (!this.isListOwnedByTheUser(gpList)) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.ERROR_UNABLE_TO_DELETE_LIST_NON_OWNER));
		}
	}

	private boolean isListOwnedByTheUser(GermplasmList gpList) {
		try {
			Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();
			if (!gpList.getUserId().equals(ibdbUserId)) {
				return false;
			}
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtil.LOG.error("Error retrieving workbench user id.", e);
		}
		return true;
	}

	private boolean isListLocked(GermplasmList gpList) {
		if (gpList != null && gpList.getStatus() > 100) {
			return true;
		}
		return false;
	}

	private void validateItemIfItIsAFolderWithContent(GermplasmList gpList) {
		try {
			if (this.hasChildren(gpList.getId())) {
				throw new InvalidValueException(this.messageSource.getMessage(Message.ERROR_HAS_CHILDREN));
			}
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtil.LOG.error("Error retrieving children items of a parent item.", e);
		}
	}

	public boolean hasChildren(Integer id) {
		return !this.germplasmListManager.getGermplasmListByParentFolderId(id, this.getCurrentProgramUUID())
				.isEmpty();
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	private GermplasmList getGermplasmList(Integer itemId) {
		GermplasmList gpList = null;

		try {
			gpList = this.germplasmListManager.getGermplasmListById(itemId);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtil.LOG.error("Error retrieving germplasm list by Id.", e);
		}

		return gpList;
	}

	public static void traverseParentsOfList(GermplasmListManager germplasmListManager, GermplasmList list, Deque<GermplasmList> parents) {
		if (list == null) {
			return;
		} else {
			Integer parentId = list.getParentId();

			if (parentId != null && parentId != 0) {
				GermplasmList parent = germplasmListManager.getGermplasmListById(list.getParentId());

				if (parent != null) {
					parents.push(parent);
					GermplasmListTreeUtil.traverseParentsOfList(germplasmListManager, parent, parents);
				}
			}

			return;
		}
	}

	public void addFolderToTree(final Object parentItemId, String folderName, Integer newFolderId, GermplasmList newFolder,
			GermplasmList parentList) {
		if (newFolderId != null) {
			final List<UserDefinedField> listTypes =
					this.germplasmDataManager.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.LIST_TYPE.getFtable(),
							RowColumnType.LIST_TYPE.getFtype());
			this.targetListSource.addItem(
					this.source.generateCellInfo(folderName,
							BreedingManagerUtil.getOwnerListName(newFolder.getUserId(), this.userDataManager),
							BreedingManagerUtil.getDescriptionForDisplay(newFolder),
							BreedingManagerUtil.getTypeString(newFolder.getType(), listTypes), ""), newFolderId);
			this.source.setNodeItemIcon(newFolderId, true);
			this.targetListSource.setItemCaption(newFolderId, folderName);
			this.targetListSource.setChildrenAllowed(newFolderId, true);

			this.source.setSelectedListId(newFolderId);

			// If parent of list does not exist
			if (parentList == null && !this.source.isFolder(parentItemId)) {
				this.targetListSource.setChildrenAllowed(ListSelectorComponent.LISTS, true);
				this.targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
				// If parent of list is root node
			} else if (parentList != null && !this.source.isFolder(parentItemId)
					&& (parentList.getParentId() == null || parentList.getParentId() == 0)) {
				this.targetListSource.setChildrenAllowed(ListSelectorComponent.LISTS, true);
				this.targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
				// If folder
			} else if (newFolder.getParent() != null && this.targetListSource.getItem(parentItemId) != null
					&& this.source.isFolder(parentItemId)) {
				this.targetListSource.setChildrenAllowed(parentItemId, true);
				Boolean parentSet = this.targetListSource.setParent(newFolderId, parentItemId);
				if (!parentSet) {
					parentSet = this.targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
				}
				// If list, add to parent
			} else if (newFolder.getParent() != null && this.targetListSource.getItem(parentItemId) != null) {
				this.targetListSource.setChildrenAllowed(parentList.getParentId(), true);
				this.targetListSource.setParent(newFolderId, parentList.getParentId());
				// All else, add to LOCAL list
			} else {
				this.targetListSource.setChildrenAllowed(ListSelectorComponent.LISTS, true);
				this.targetListSource.setParent(newFolderId, ListSelectorComponent.LISTS);
			}

			if (this.targetListSource.getValue() != null) {
				if (!this.targetListSource.isExpanded(this.targetListSource.getValue())) {
					this.targetListSource.expandItem(parentItemId);
				}
			} else {
				this.targetListSource.expandItem(ListSelectorComponent.LISTS);
			}

			this.targetListSource.select(newFolderId);
			this.source.updateButtons(newFolderId);
			this.source.showAddRenameFolderSection(false);
			this.source.refreshRemoteTree();
			MessageNotifier.showMessage(this.source.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					"Folder saved successfully.");
		}
	}

	public void setSource(ListSelectorComponent source) {
		this.source = source;
	}

	public void setTargetListSource(GermplasmListSource targetListSource) {
		this.targetListSource = targetListSource;
	}

	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
