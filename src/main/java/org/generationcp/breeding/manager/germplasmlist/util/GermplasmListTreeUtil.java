
package org.generationcp.breeding.manager.germplasmlist.util;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasmlist.ListManagerTreeComponent;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmListTreeUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeUtil.class);

	private final ListManagerTreeComponent source;
	private final Tree targetTree;

	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
	public static final String MY_LIST = "";

	protected static final String FOLDER = "FOLDER";

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	public GermplasmListTreeUtil(ListManagerTreeComponent source, Tree targetTree) {
		this.source = source;
		this.targetTree = targetTree;
		this.setupTreeDragAndDropHandler();
	}

	public GermplasmListTreeUtil(ListManagerTreeComponent source, Tree targetTree, GermplasmListManager germplasmListManager,
			SimpleResourceBundleMessageSource messageSource, ContextUtil contextUtil) {
		this.source = source;
		this.targetTree = targetTree;
		this.germplasmListManager = germplasmListManager;
		this.messageSource = messageSource;
		this.contextUtil = contextUtil;
	}
	
	public void setParent(Object sourceItemId, Object targetItemId) {

		if (sourceItemId.equals(ListManagerTreeComponent.CENTRAL)) {
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_WITH_MODIFYING_LIST_TREE),
					this.messageSource.getMessage(Message.UNABLE_TO_MOVE_ROOT_FOLDERS));
			return;
		}

		Integer sourceId = null;
		Integer targetId = null;

		if (sourceItemId != null && !sourceItemId.equals(ListManagerTreeComponent.CENTRAL)) {
			sourceId = Integer.valueOf(sourceItemId.toString());
		}
		if (targetItemId != null && !targetItemId.equals(ListManagerTreeComponent.CENTRAL)) {
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
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
		}

		// apply to UI
		if (targetItemId == null || this.targetTree.getItem(targetItemId) == null) {
			this.targetTree.setChildrenAllowed(sourceItemId, true);
			this.targetTree.setParent(sourceItemId, ListManagerTreeComponent.CENTRAL);
			this.targetTree.expandItem(ListManagerTreeComponent.CENTRAL);
		} else {
			this.targetTree.setChildrenAllowed(targetItemId, true);
			this.targetTree.setParent(sourceItemId, targetItemId);
			this.targetTree.expandItem(targetItemId);
		}

		this.source.setSelectedListId(sourceItemId);
		this.targetTree.select(sourceItemId);
		this.targetTree.setValue(sourceItemId);
	}

	public void setupTreeDragAndDropHandler() {
		this.targetTree.setDropHandler(new DropHandler() {

			private static final long serialVersionUID = -6676297159926786216L;

			@Override
			public void drop(DragAndDropEvent dropEvent) {
				Transferable t = dropEvent.getTransferable();
				if (t.getSourceComponent() != GermplasmListTreeUtil.this.targetTree) {
					return;
				}

				TreeTargetDetails target = (TreeTargetDetails) dropEvent.getTargetDetails();

				Object sourceItemId = t.getData("itemId");
				Object targetItemId = target.getItemIdOver();

				VerticalDropLocation location = target.getDropLocation();

				GermplasmList targetList = null;
				try {
					targetList = GermplasmListTreeUtil.this.germplasmListManager.getGermplasmListById((Integer) targetItemId);
				} catch (MiddlewareQueryException e) {
					GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
				} catch (ClassCastException e) {
					GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
				}

				// Dropped straight to LOCAL (so no germplasmList found for LOCAL)
				if (location == VerticalDropLocation.MIDDLE && targetList == null) {
					GermplasmListTreeUtil.this.setParent(sourceItemId, ListManagerTreeComponent.CENTRAL);
					// Dropped on a folder
				} else if (location == VerticalDropLocation.MIDDLE && GermplasmListTreeUtil.FOLDER.equals(targetList.getType())) {
					GermplasmListTreeUtil.this.setParent(sourceItemId, targetItemId);
					// Dropped on a list with parent != LOCAL
				} else if (targetList != null && targetList.getParentId() >= 0) {
					GermplasmListTreeUtil.this.setParent(sourceItemId, targetList.getParentId());
					// Dropped on a list with parent == LOCAL
				} else {
					GermplasmListTreeUtil.this.setParent(sourceItemId, ListManagerTreeComponent.CENTRAL);
				}
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
		});
	}

	public void addFolder(final Object parentFolderId) {

		final Window w = new BaseSubWindow("Add new folder");
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

			private static final long serialVersionUID = -5652937366625733522L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				GermplasmListTreeUtil.this.addItemAction(parentFolderId, name, event);
			}
		});

		Button cancel = new Button("Cancel");
		cancel.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -971341450278698928L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				GermplasmListTreeUtil.this.source.getWindow().removeWindow(w);
			}
		});

		btnContainer.addComponent(ok);
		btnContainer.addComponent(cancel);

		container.addComponent(formContainer);
		container.addComponent(btnContainer);

		w.setContent(container);

		// show window
		this.source.getWindow().addWindow(w);
	}

	public void renameFolderOrList(final Integer listId) {

		GermplasmList germplasmList = null;
		try {
			germplasmList = this.germplasmListManager.getGermplasmListById(listId);
		} catch (MiddlewareQueryException e1) {
			GermplasmListTreeUtil.LOG.error(e1.getMessage(), e1);
		}

		final Window w = new BaseSubWindow();

		if (GermplasmListTreeUtil.FOLDER.equalsIgnoreCase(germplasmList.getType())) {
			w.setCaption("Rename Folder");
		} else {
			w.setCaption("Rename List");
		}

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

		Label l = new Label();

		if (GermplasmListTreeUtil.FOLDER.equalsIgnoreCase(germplasmList.getType())) {
			l.setCaption("Folder Name");
		} else {
			l.setCaption("List Name");
		}

		final TextField name = new TextField();
		name.setMaxLength(50);

		if (germplasmList != null) {
			name.setValue(germplasmList.getName());
		}

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

			private static final long serialVersionUID = 7284368907912687291L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				GermplasmListTreeUtil.this.renameItemAction(listId, name, event);
			}
		});

		Button cancel = new Button("Cancel");
		cancel.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -387205052924783630L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				GermplasmListTreeUtil.this.source.getWindow().removeWindow(w);
			}
		});

		btnContainer.addComponent(ok);
		btnContainer.addComponent(cancel);

		container.addComponent(formContainer);
		container.addComponent(btnContainer);

		w.setContent(container);

		// show window
		this.source.getWindow().addWindow(w);
	}

	public void deleteFolderOrList(final ListManagerTreeComponent listManagerTreeComponent, final Integer lastItemId) {

		final Window mainWindow = this.source.getWindow();

		try {
			this.validateItemToDelete(lastItemId);
		} catch (InvalidValueException e) {
			MessageNotifier.showRequiredFieldError(mainWindow, e.getMessage());
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
			return;
		}

		final GermplasmList finalGpList = this.getGermplasmList(lastItemId);
		ConfirmDialog.show(this.source.getWindow(),
				this.messageSource.getMessage(Message.DELETE_LIST_FOLDER, this.targetTree.getItemCaption(lastItemId)),
				this.messageSource.getMessage(Message.DELETE_LIST_FOLDER_CONFIRM, this.targetTree.getItemCaption(lastItemId)),
				this.messageSource.getMessage(Message.YES), this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

					private static final long serialVersionUID = -7725802403506726746L;

					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							try {
								GermplasmList parent = GermplasmListTreeUtil.this.germplasmListManager
										.getGermplasmListById(finalGpList.getId()).getParent();
								GermplasmListTreeUtil.this.germplasmListManager.deleteGermplasmList(finalGpList);
								GermplasmListTreeUtil.this.targetTree.removeItem(lastItemId);
								GermplasmListTreeUtil.this.targetTree.select(null);
								if (parent == null) {
									GermplasmListTreeUtil.this.targetTree.select(GermplasmListTreeUtil.MY_LIST);
									listManagerTreeComponent.setSelectedListId(GermplasmListTreeUtil.MY_LIST);
								} else {
									GermplasmListTreeUtil.this.targetTree.select(parent.getId());
									GermplasmListTreeUtil.this.targetTree.expandItem(parent.getId());
									listManagerTreeComponent.setSelectedListId(parent.getId());
								}

							} catch (Exception e) {
								MessageNotifier.showError(GermplasmListTreeUtil.this.source.getWindow(), e.getMessage(), "");
								GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
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

	public boolean hasChildren(Integer id) throws MiddlewareQueryException {
		return !this.germplasmListManager.getGermplasmListByParentFolderId(id, this.getCurrentProgramUUID(), 0, Integer.MAX_VALUE)
				.isEmpty();
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

	public static void traverseParentsOfList(GermplasmListManager germplasmListManager, GermplasmList list, Deque<GermplasmList> parents)
			throws MiddlewareQueryException {
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

	protected void addItemAction(final Object parentFolderId, final TextField name, Button.ClickEvent event) {

		// Validate the itemName first
		String itemName = name.getValue().toString();
		try {
			this.validateItemToAddRename(itemName);
		} catch (InvalidValueException | MiddlewareQueryException e) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), e.getMessage());
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
			return;
		}

		// Start of add
		Integer newFolderId = null;
		GermplasmList newFolder = new GermplasmList();
		GermplasmList parentList = null;

		try {
			newFolder.setName(itemName);
			newFolder.setDescription(itemName);
			newFolder.setType(GermplasmListTreeUtil.FOLDER);
			newFolder.setStatus(0);
			newFolder.setUserId(this.contextUtil.getCurrentUserLocalId());
			newFolder.setDate(DateUtil.getCurrentDateAsLongValue());
			newFolder.setProgramUUID(this.contextUtil.getCurrentProgramUUID());

			parentList = this.getParentItem(parentFolderId);
			newFolder.setParent(parentList);

			newFolderId = this.germplasmListManager.addGermplasmList(newFolder);

		} catch (MiddlewareQueryException e) {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_INTERNAL),
					this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
		}

		// update UI
		if (newFolderId != null) {
			this.targetTree.addItem(newFolderId);
			this.targetTree.setItemCaption(newFolderId, name.getValue().toString());
			this.targetTree.setChildrenAllowed(newFolderId, true);

			this.source.setSelectedListId(newFolderId);

			// If parent of list does not exist
			if (parentList == null && !this.source.isFolder(parentFolderId)) {
				this.targetTree.setChildrenAllowed(ListManagerTreeComponent.CENTRAL, true);
				this.targetTree.setParent(newFolderId, ListManagerTreeComponent.CENTRAL);
				// If parent of list is root node
			} else if (parentList != null && !this.source.isFolder(parentFolderId)
					&& (parentList.getParentId() == null || parentList.getParentId() == 0)) {
				this.targetTree.setChildrenAllowed(ListManagerTreeComponent.CENTRAL, true);
				this.targetTree.setParent(newFolderId, ListManagerTreeComponent.CENTRAL);
				// If folder
			} else if (newFolder.getParent() != null && this.targetTree.getItem(parentFolderId) != null
					&& this.source.isFolder(parentFolderId)) {
				this.targetTree.setChildrenAllowed(parentFolderId, true);
				Boolean parentSet = this.targetTree.setParent(newFolderId, parentFolderId);
				if (!parentSet) {
					parentSet = this.targetTree.setParent(newFolderId, ListManagerTreeComponent.CENTRAL);
				}
				// If list, add to parent
			} else if (newFolder.getParent() != null && this.targetTree.getItem(parentFolderId) != null) {
				this.targetTree.setChildrenAllowed(parentList.getParentId(), true);
				this.targetTree.setParent(newFolderId, parentList.getParentId());
				// All else, add to LOCAL list
			} else {
				this.targetTree.setChildrenAllowed(ListManagerTreeComponent.CENTRAL, true);
				this.targetTree.setParent(newFolderId, ListManagerTreeComponent.CENTRAL);
			}

			if (this.targetTree.getValue() != null) {
				if (!this.targetTree.isExpanded(this.targetTree.getValue())) {
					this.targetTree.expandItem(parentFolderId);
				}
			} else {
				this.targetTree.expandItem(ListManagerTreeComponent.CENTRAL);
			}
			this.targetTree.select(newFolderId);
			this.source.updateButtons(newFolderId);
		}

		// close popup
		this.source.getWindow().removeWindow(event.getComponent().getWindow());
	}

	protected GermplasmList getParentItem(final Object parentItemId) throws MiddlewareQueryException {
		GermplasmList parentList = null;
		if (parentItemId == null || parentItemId instanceof String || this.targetTree.getItem(parentItemId) == null) {
			parentList = null;
		} else if (!this.source.isFolder(parentItemId)) {
			GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById((Integer) parentItemId);
			parentList = this.germplasmListManager.getGermplasmListById(germplasmList.getParentId());
		} else {
			parentList = this.germplasmListManager.getGermplasmListById((Integer) parentItemId);
		}
		return parentList;
	}

	protected void validateItemToAddRename(String itemName) throws MiddlewareQueryException {
		if (itemName == null) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.ERROR_NO_SELECTION));
		}

		if ("".equalsIgnoreCase(itemName.replace(" ", ""))
				|| this.messageSource.getMessage(Message.LISTS_LABEL).equalsIgnoreCase(itemName)) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.INVALID_LIST_FOLDER_NAME));
		}

		List<GermplasmList> matchingGermplasmLists =
				this.germplasmListManager.getGermplasmListByName(itemName, this.getCurrentProgramUUID(), 0, 1, Operation.EQUAL);
		if (!matchingGermplasmLists.isEmpty()) {
			throw new InvalidValueException(this.messageSource.getMessage(Message.EXISTING_LIST_ERROR_MESSAGE));
		}
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	private void renameItemAction(final Integer listId, final TextField name, Button.ClickEvent event) {

		// Validate the itemName first
		String itemName = name.getValue().toString();
		try {
			this.validateItemToAddRename(itemName);
		} catch (InvalidValueException | MiddlewareQueryException e) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), e.getMessage());
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
			return;
		}

		try {
			GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);
			germplasmList.setName(name.getValue().toString());
			this.germplasmListManager.updateGermplasmList(germplasmList);
			this.targetTree.setItemCaption(listId, name.getValue().toString());
			this.targetTree.select(listId);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeUtil.LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

		this.source.getWindow().removeWindow(event.getComponent().getWindow());
	}

}
