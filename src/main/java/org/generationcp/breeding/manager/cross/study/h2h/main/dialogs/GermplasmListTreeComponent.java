
package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.SelectListButtonClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.SelectListItemClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.SelectListTreeExpandListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.spring.util.ContextUtil;
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

import com.vaadin.ui.Button;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmListTreeComponent extends VerticalLayout implements InternationalizableComponent, InitializingBean, Serializable {

	private static final String ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_ID = "Error in getting germplasm lists by parent id.";

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListTreeComponent.class);

	private static final int BATCH_SIZE = 50;
	public static final String REFRESH_BUTTON_ID = "NewListTreeUI Refresh Button";
	public static final String ROOT_FOLDER_NAME = "Lists";

	private Tree germplasmListTree;
	private Button refreshButton;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private ContextUtil contextUtil;

	private VerticalLayout treeContainerLayout;

	private Integer listId;
	private final SelectGermplasmListComponent selectListComponent;

	public GermplasmListTreeComponent(SelectGermplasmListComponent selectListComponent) {
		this.selectListComponent = selectListComponent;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setSpacing(true);

		this.germplasmListTree = new Tree();
		this.germplasmListTree.setImmediate(true);

		this.refreshButton = new Button();
		this.refreshButton.setData(GermplasmListTreeComponent.REFRESH_BUTTON_ID);
		this.refreshButton.addListener(new SelectListButtonClickListener(this));

		this.refreshButton.setCaption(this.messageSource.getMessage(Message.REFRESH_LABEL));
		this.refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.treeContainerLayout = new VerticalLayout();
		this.treeContainerLayout.addComponent(this.germplasmListTree);

		this.addComponent(this.treeContainerLayout);
		this.addComponent(this.refreshButton);

		this.createTree();

	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void createTree() {
		this.treeContainerLayout.removeComponent(this.germplasmListTree);
		this.germplasmListTree.removeAllItems();
		this.germplasmListTree = this.createGermplasmListTree();
		this.germplasmListTree.addStyleName("listManagerTreeGPSB");

		this.germplasmListTree.setItemStyleGenerator(new ItemStyleGenerator() {

			private static final long serialVersionUID = -5690995097357568121L;

			@Override
			public String getStyle(Object itemId) {

				GermplasmList currentList = null;

				try {
					currentList =
							GermplasmListTreeComponent.this.germplasmListManager.getGermplasmListById(Integer.valueOf(itemId.toString()));
				} catch (NumberFormatException e) {
					currentList = null;
				} catch (MiddlewareQueryException e) {
					GermplasmListTreeComponent.LOG.error("Erro with getting list by id: " + itemId, e);
					currentList = null;
				}

				if (itemId.equals(GermplasmListTreeComponent.ROOT_FOLDER_NAME)) {
					return "listManagerTreeRootNode";
				} else if (currentList != null && "FOLDER".equalsIgnoreCase(currentList.getType())) {
					return "listManagerTreeRegularParentNode";
				} else {
					return "listManagerTreeRegularChildNode";
				}

			}
		});

		this.treeContainerLayout.addComponent(this.germplasmListTree);
		this.germplasmListTree.requestRepaint();

	}

	protected Tree createGermplasmListTree() {
		List<GermplasmList> germplasmListParent = new ArrayList<GermplasmList>();

		try {
			germplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(this.getCurrentProgramUUID(),
					GermplasmListTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeComponent.LOG.error("Error in getting top level lists.", e);
			if (this.getWindow() != null) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
			}
			germplasmListParent = new ArrayList<GermplasmList>();
		}

		Tree currentGermplasmListTree = new Tree();
		currentGermplasmListTree.setDragMode(TreeDragMode.NODE);

		currentGermplasmListTree.addItem(GermplasmListTreeComponent.ROOT_FOLDER_NAME);
		currentGermplasmListTree.setItemCaption(GermplasmListTreeComponent.ROOT_FOLDER_NAME, GermplasmListTreeComponent.ROOT_FOLDER_NAME);

		for (GermplasmList parentList : germplasmListParent) {
			currentGermplasmListTree.addItem(parentList.getId());
			currentGermplasmListTree.setItemCaption(parentList.getId(), parentList.getName());
			currentGermplasmListTree.setChildrenAllowed(parentList.getId(), this.hasChildList(parentList.getId()));
			currentGermplasmListTree.setParent(parentList.getId(), GermplasmListTreeComponent.ROOT_FOLDER_NAME);
		}

		currentGermplasmListTree.addListener(new SelectListTreeExpandListener(this));
		currentGermplasmListTree.addListener(new SelectListItemClickListener(this));

		try {
			if (this.listId != null) {
				GermplasmList list = this.germplasmListManager.getGermplasmListById(this.listId);

				if (list != null) {
					Deque<GermplasmList> parents = new ArrayDeque<GermplasmList>();
					this.traverseParentsOfList(list, parents);

					currentGermplasmListTree.expandItem(GermplasmListTreeComponent.ROOT_FOLDER_NAME);

					while (!parents.isEmpty()) {
						GermplasmList parent = parents.pop();
						currentGermplasmListTree.setChildrenAllowed(parent.getId(), true);
						this.addGermplasmListNode(parent.getId().intValue(), currentGermplasmListTree);
						currentGermplasmListTree.expandItem(parent.getId());
					}

					currentGermplasmListTree.select(this.listId);
				}
			}
		} catch (MiddlewareQueryException ex) {
			GermplasmListTreeComponent.LOG.error("Error with getting parents for hierarchy of list id: " + this.listId, ex);
		}

		return currentGermplasmListTree;
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	private void traverseParentsOfList(GermplasmList list, Deque<GermplasmList> parents) throws MiddlewareQueryException {
		if (list == null) {
			return;
		} else {
			Integer parentId = list.getParentId();

			if (parentId != null && parentId != 0) {
				GermplasmList parent = this.germplasmListManager.getGermplasmListById(list.getParentId());

				if (parent != null) {
					parents.push(parent);
					this.traverseParentsOfList(parent, parents);
				}
			}

			return;
		}
	}

	public void listManagerTreeItemClickAction(int germplasmListId) {

		try {
			GermplasmList list = this.getGermplasmList(germplasmListId);
			boolean isEmptyFolder = this.isEmptyFolder(list);
			boolean hasChildList = this.hasChildList(germplasmListId);

			if (!hasChildList && !isEmptyFolder && this.selectListComponent != null) {
				this.selectListComponent.getListInfoComponent().displayListInfo(list);

				// toggle folder
			} else if (hasChildList && !isEmptyFolder) {
				this.expandOrCollapseListTreeNode(Integer.valueOf(germplasmListId));
			}
		} catch (NumberFormatException e) {
			GermplasmListTreeComponent.LOG.error("Error clicking of list.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
					this.messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeComponent.LOG.error("Error in displaying germplasm list details.", e);
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
		}

	}

	private boolean hasChildList(int listId) {

		List<GermplasmList> listChildren = new ArrayList<GermplasmList>();

		try {
			listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, this.getCurrentProgramUUID(), 0, 1);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeComponent.LOG.error(GermplasmListTreeComponent.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_ID, e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			listChildren = new ArrayList<GermplasmList>();
		}

		return !listChildren.isEmpty();
	}

	private boolean isEmptyFolder(GermplasmList list) throws MiddlewareQueryException {
		boolean isFolder = "FOLDER".equalsIgnoreCase(list.getType());
		return isFolder && !this.hasChildList(list.getId());
	}

	private GermplasmList getGermplasmList(int listId) throws MiddlewareQueryException {
		return this.germplasmListManager.getGermplasmListById(listId);
	}

	public void addGermplasmListNode(int parentGermplasmListId) {
		this.germplasmListTree.select(null);
		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

		try {
			germplasmListChildren =
					this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
							this.getCurrentProgramUUID(), GermplasmListTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeComponent.LOG.error(GermplasmListTreeComponent.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_ID, e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			germplasmListChildren = new ArrayList<GermplasmList>();
		}

		for (GermplasmList listChild : germplasmListChildren) {
			this.germplasmListTree.addItem(listChild.getId());
			this.germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
			this.germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
			// allow children if list has sub-lists
			this.germplasmListTree.setChildrenAllowed(listChild.getId(), this.hasChildList(listChild.getId()));
		}
	}

	public void addGermplasmListNode(int parentGermplasmListId, Tree germplasmListTree) {
		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

		try {
			germplasmListChildren =
					this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
							this.getCurrentProgramUUID(), GermplasmListTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			GermplasmListTreeComponent.LOG.error(GermplasmListTreeComponent.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_ID, e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			germplasmListChildren = new ArrayList<GermplasmList>();
		}

		for (GermplasmList listChild : germplasmListChildren) {
			germplasmListTree.addItem(listChild.getId());
			germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
			germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
			// allow children if list has sub-lists
			germplasmListTree.setChildrenAllowed(listChild.getId(), this.hasChildList(listChild.getId()));
		}
	}

	public void expandOrCollapseListTreeNode(Object nodeId) {
		if (!this.germplasmListTree.isExpanded(nodeId)) {
			this.germplasmListTree.expandItem(nodeId);
		} else {
			this.germplasmListTree.collapseItem(nodeId);
		}
	}

	public void setTreeContainerLayout(VerticalLayout treeContainerLayout) {
		this.treeContainerLayout = treeContainerLayout;
	}

	public void setGermplasmListTree(Tree germplasmListTree) {
		this.germplasmListTree = germplasmListTree;
	}

	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setListId(Integer listId) {
		this.listId = listId;
	}

	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
