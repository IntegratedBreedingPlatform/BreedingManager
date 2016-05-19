
package org.generationcp.breeding.manager.germplasmlist;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.SelectGermplasmListComponent;
import org.generationcp.breeding.manager.germplasmlist.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.germplasmlist.listeners.GermplasmListTreeExpandListener;
import org.generationcp.breeding.manager.germplasmlist.util.GermplasmListTreeUtil;
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

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class ListManagerTreeComponent extends VerticalLayout implements InternationalizableComponent, InitializingBean, Serializable {

	protected static final Logger LOG = LoggerFactory.getLogger(ListManagerTreeComponent.class);

	protected static final long serialVersionUID = -224052511814636864L;
	protected final static int BATCH_SIZE = 50;
	public final static String REFRESH_BUTTON_ID = "ListManagerTreeComponent Refresh Button";
	public static final String CENTRAL = "CENTRAL";
	public static final String LOCAL = "LOCAL";

	protected Label heading;
	protected Tree germplasmListTree;
	protected AbsoluteLayout germplasmListBrowserMainLayout;
	protected Button refreshButton;

	@Autowired
	protected GermplasmListManager germplasmListManager;

	@Autowired
	protected SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private ContextUtil contextUtil;

	protected boolean forGermplasmListWindow;

	protected HorizontalLayout controlButtonsLayout;
	protected VerticalLayout treeContainerLayout;

	protected Integer listId;
	protected GermplasmListTreeUtil germplasmListTreeUtil;
	protected SelectGermplasmListComponent selectListComponent;

	protected final ThemeResource ICON_REFRESH = new ThemeResource("images/refresh-icon.png");

	protected Button addFolderBtn;
	protected Button deleteFolderBtn;
	protected Button renameFolderBtn;

	protected Object selectedListId;

	private final boolean forSelectingFolderToSaveIn;

	public ListManagerTreeComponent(boolean forSelectingFolderToSaveIn, Integer folderId) {
		super();
		this.forSelectingFolderToSaveIn = forSelectingFolderToSaveIn;
		this.selectListComponent = null;
		this.germplasmListBrowserMainLayout = null;
		this.forGermplasmListWindow = false;
		this.listId = folderId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setSpacing(true);

		this.heading = new Label();
		this.heading.setValue("List Location");
		this.heading.setStyleName(Bootstrap.Typography.H6.styleName());
		this.heading.setWidth("90px");

		if (this.germplasmListBrowserMainLayout != null) {
			this.initializeButtonPanel();
			this.addComponent(this.controlButtonsLayout);
		}

		if (this.forSelectingFolderToSaveIn) {
			this.initializeButtonPanel();
			this.addComponent(this.controlButtonsLayout);
		}

		this.germplasmListTree = new Tree();

		if (!this.forSelectingFolderToSaveIn) {
			this.refreshButton = new Button();
			this.refreshButton.setData(ListManagerTreeComponent.REFRESH_BUTTON_ID);
			this.refreshButton.setCaption(this.messageSource.getMessage(Message.REFRESH_LABEL));
			this.refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
			this.refreshButton.addListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					ListManagerTreeComponent.this.createTree();
				}
			});
		}

		this.treeContainerLayout = new VerticalLayout();
		this.treeContainerLayout.addComponent(this.germplasmListTree);

		this.addComponent(this.treeContainerLayout);
		if (!this.forSelectingFolderToSaveIn) {
			this.addComponent(this.refreshButton);
		}

		this.createTree();

		this.germplasmListTreeUtil = new GermplasmListTreeUtil(this, this.germplasmListTree);
	}

	protected void initializeButtonPanel() {
		this.renameFolderBtn =
				new Button("<span class='bms-edit' style='left: 2px; color: #0083c0;font-size: 18px; font-weight: bold;'></span>");
		this.renameFolderBtn.setHtmlContentAllowed(true);
		this.renameFolderBtn.setDescription("Rename Item");
		this.renameFolderBtn.setStyleName(BaseTheme.BUTTON_LINK);
		this.renameFolderBtn.setWidth("25px");
		this.renameFolderBtn.setHeight("30px");
		this.renameFolderBtn.setEnabled(false);
		this.renameFolderBtn.addListener(new Button.ClickListener() {

			protected static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				ListManagerTreeComponent.this.germplasmListTreeUtil.renameFolderOrList(Integer
						.valueOf(ListManagerTreeComponent.this.selectedListId.toString()));
			}
		});

		this.addFolderBtn =
				new Button("<span class='bms-add' style='left: 2px; color: #00a950;font-size: 18px; font-weight: bold;'></span>");
		this.addFolderBtn.setHtmlContentAllowed(true);
		this.addFolderBtn.setDescription("Add New Folder");
		this.addFolderBtn.setStyleName(BaseTheme.BUTTON_LINK);
		this.addFolderBtn.setWidth("25px");
		this.addFolderBtn.setHeight("30px");
		this.addFolderBtn.setEnabled(false);
		this.addFolderBtn.addListener(new Button.ClickListener() {

			protected static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				ListManagerTreeComponent.this.germplasmListTreeUtil.addFolder(ListManagerTreeComponent.this.selectedListId);
			}
		});

		this.deleteFolderBtn =
				new Button("<span class='bms-delete' style='left: 2px; color: #f4a41c;font-size: 18px; font-weight: bold;'></span>");
		this.deleteFolderBtn.setHtmlContentAllowed(true);
		this.deleteFolderBtn.setDescription("Delete Selected List/Folder");
		this.deleteFolderBtn.setStyleName(BaseTheme.BUTTON_LINK);
		this.deleteFolderBtn.setWidth("25px");
		this.deleteFolderBtn.setHeight("30px");
		this.deleteFolderBtn.setEnabled(false);
		this.deleteFolderBtn.addListener(new Button.ClickListener() {

			protected static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				ListManagerTreeComponent.this.germplasmListTreeUtil.deleteFolderOrList(
						ListManagerTreeComponent.this.getListManagerTreeComponent(),
						Integer.valueOf(ListManagerTreeComponent.this.selectedListId.toString()));
			}
		});

		this.controlButtonsLayout = new HorizontalLayout();

		HorizontalLayout buttonsPanel = new HorizontalLayout();
		buttonsPanel.addComponent(this.addFolderBtn);
		buttonsPanel.addComponent(this.renameFolderBtn);
		buttonsPanel.addComponent(this.deleteFolderBtn);

		this.controlButtonsLayout.setSizeFull();
		this.controlButtonsLayout.setSpacing(true);
		this.controlButtonsLayout.addComponent(this.heading);
		this.controlButtonsLayout.addComponent(buttonsPanel);
		this.controlButtonsLayout.setComponentAlignment(this.heading, Alignment.MIDDLE_LEFT);
		this.controlButtonsLayout.setComponentAlignment(buttonsPanel, Alignment.MIDDLE_RIGHT);
	}

	@Override
	public void updateLabels() {
	}

	public void simulateItemClickForNewlyAdded(Integer listId, boolean openDetails) {
		this.germplasmListTree.expandItem(ListManagerTreeComponent.LOCAL);
		if (openDetails) {
			this.germplasmListTree.setValue(listId);
		}
	}

	/*
	 * Resets listid to null (in case list was launched via Dashboard) so that tree can be refreshed
	 */
	public void refreshTree() {
		this.listId = null;
		this.createTree();
	}

	public void createTree() {
		this.treeContainerLayout.removeComponent(this.germplasmListTree);
		this.germplasmListTree.removeAllItems();
		this.germplasmListTree = this.createGermplasmListTree();
		this.germplasmListTree.addStyleName("listManagerTreeGPSB");
		this.germplasmListTree.setHeight("220px");

		if (this.selectListComponent != null) {
			this.germplasmListTree.addStyleName("listManagerTree-long");
		}

		this.germplasmListTree.setItemStyleGenerator(new ItemStyleGenerator() {

			protected static final long serialVersionUID = -5690995097357568121L;

			@Override
			public String getStyle(Object itemId) {

				GermplasmList currentList = null;

				try {
					currentList =
							ListManagerTreeComponent.this.germplasmListManager.getGermplasmListById(Integer.valueOf(itemId.toString()));
				} catch (NumberFormatException e) {
					currentList = null;
				} catch (MiddlewareQueryException e) {
					ListManagerTreeComponent.LOG.error("Erro with getting list by id: " + itemId, e);
					currentList = null;
				}

				if (itemId.equals(ListManagerTreeComponent.LOCAL) || itemId.equals(ListManagerTreeComponent.CENTRAL)) {
					return "listManagerTreeRootNode";
				} else if (currentList != null && currentList.getType().equals("FOLDER")) {
					return "listManagerTreeRegularParentNode";
				} else {
					return "listManagerTreeRegularChildNode";
				}

			}
		});

		this.germplasmListTree.setImmediate(true);

		if (this.forSelectingFolderToSaveIn) {
			this.germplasmListTreeUtil = new GermplasmListTreeUtil(this, this.germplasmListTree);
		}
		this.treeContainerLayout.addComponent(this.germplasmListTree);
		this.germplasmListTree.requestRepaint();

	}

	protected Tree createGermplasmListTree() {
		List<GermplasmList> centralGermplasmListParent = new ArrayList<GermplasmList>();

		try {
			centralGermplasmListParent =
					this.germplasmListManager.getAllTopLevelListsBatched(this.getCurrentProgramUUID(), ListManagerTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			ListManagerTreeComponent.LOG.error("Error in getting top level lists.", e);
			if (this.getWindow() != null) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
			}
			centralGermplasmListParent = new ArrayList<GermplasmList>();
		}

		Tree germplasmListTree = new Tree();
		if (this.forSelectingFolderToSaveIn) {
			germplasmListTree.setDragMode(TreeDragMode.NODE);
		}

		germplasmListTree.addItem(ListManagerTreeComponent.CENTRAL);
		germplasmListTree.setItemCaption(ListManagerTreeComponent.CENTRAL, "Lists");

		for (GermplasmList centralParentList : centralGermplasmListParent) {
			if (!this.forSelectingFolderToSaveIn || this.isFolder(centralParentList.getId())) {
				germplasmListTree.addItem(centralParentList.getId());
				germplasmListTree.setItemCaption(centralParentList.getId(), centralParentList.getName());
				germplasmListTree.setChildrenAllowed(centralParentList.getId(), this.hasChildList(centralParentList.getId()));
				germplasmListTree.setParent(centralParentList.getId(), ListManagerTreeComponent.CENTRAL);
			}
		}

		germplasmListTree.addListener(new GermplasmListTreeExpandListener(this));
		germplasmListTree.addListener(new GermplasmListItemClickListener(this));

		try {
			if (this.listId != null) {
				GermplasmList list = this.germplasmListManager.getGermplasmListById(this.listId);

				if (list != null) {
					Deque<GermplasmList> parents = new ArrayDeque<GermplasmList>();
					GermplasmListTreeUtil.traverseParentsOfList(this.germplasmListManager, list, parents);

					germplasmListTree.expandItem(ListManagerTreeComponent.CENTRAL);

					while (!parents.isEmpty()) {
						GermplasmList parent = parents.pop();
						germplasmListTree.setChildrenAllowed(parent.getId(), true);
						this.addGermplasmListNode(parent.getId().intValue(), germplasmListTree);
						germplasmListTree.expandItem(parent.getId());
					}

					germplasmListTree.select(this.listId);
					germplasmListTree.setValue(this.listId);
					this.setSelectedListId(this.listId);
				}
			}
		} catch (MiddlewareQueryException ex) {
			ListManagerTreeComponent.LOG.error("Error with getting parents for hierarchy of list id: " + this.listId, ex);
		}

		if (this.forSelectingFolderToSaveIn) {
			germplasmListTree.setNullSelectionAllowed(false);
		}

		return germplasmListTree;
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	public void updateButtons(Object itemId) {
		this.setSelectedListId(itemId);
		if (this.forSelectingFolderToSaveIn) {
			try {
				// If any of the central lists/folders is selected
				if (Integer.valueOf(itemId.toString()) > 0) {
					this.addFolderBtn.setEnabled(true);
					this.renameFolderBtn.setEnabled(true);
					this.deleteFolderBtn.setEnabled(true);
					// If any of the local folders/lists are selected
				} else {
					this.addFolderBtn.setEnabled(true);
					this.renameFolderBtn.setEnabled(true);
					this.deleteFolderBtn.setEnabled(false);
				}
			} catch (NumberFormatException e) {
				// If selected item is "Shared Lists"
				if (itemId.toString().equals(ListManagerTreeComponent.CENTRAL)) {
					this.addFolderBtn.setEnabled(true);
					this.renameFolderBtn.setEnabled(false);
					this.deleteFolderBtn.setEnabled(false);
					// Any non-numeric itemID (nothing goes here as of the moment)
				} else {
					this.addFolderBtn.setEnabled(true);
					this.renameFolderBtn.setEnabled(true);
					this.deleteFolderBtn.setEnabled(true);
				}
			} catch (ClassCastException e) {
				ListManagerTreeComponent.LOG.error(e.getMessage(), e);
				// If selected item is "Shared Lists"
				if (itemId.toString().equals(ListManagerTreeComponent.CENTRAL)) {
					this.addFolderBtn.setEnabled(true);
					this.renameFolderBtn.setEnabled(false);
					this.deleteFolderBtn.setEnabled(false);
					// Any non-numeric itemID (nothing goes here as of the moment)
				} else {
					this.addFolderBtn.setEnabled(true);
					this.renameFolderBtn.setEnabled(true);
					this.deleteFolderBtn.setEnabled(true);
				}
			}
		}
	}

	public void listManagerTreeItemClickAction(int germplasmListId) throws InternationalizableException {

		try {

			GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(germplasmListId);

			this.selectedListId = germplasmListId;

			boolean hasChildList = this.hasChildList(germplasmListId);
			boolean isEmptyFolder = this.isEmptyFolder(germplasmList);
			if (!isEmptyFolder) {

				if (this.selectListComponent != null) {
					this.selectListComponent.getListInfoComponent().displayListInfo(germplasmList);
					// toggle folder
				} else if (hasChildList) {
					this.expandOrCollapseListTreeNode(Integer.valueOf(germplasmListId));
				}

				this.germplasmListTree.select(germplasmListId);
				this.germplasmListTree.setValue(germplasmListId);
			}

		} catch (NumberFormatException e) {
			ListManagerTreeComponent.LOG.error("Error clicking of list.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
					this.messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
		} catch (MiddlewareQueryException e) {
			ListManagerTreeComponent.LOG.error("Error in displaying germplasm list details.", e);
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
		}

	}

	protected boolean hasChildList(int listId) {

		List<GermplasmList> listChildren = new ArrayList<GermplasmList>();

		try {
			listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, this.getCurrentProgramUUID(), 0, 1);
		} catch (MiddlewareQueryException e) {
			ListManagerTreeComponent.LOG.error("Error in getting germplasm lists by parent id.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			listChildren = new ArrayList<GermplasmList>();
		}

		return !listChildren.isEmpty();
	}

	protected boolean isEmptyFolder(GermplasmList list) throws MiddlewareQueryException {
		boolean isFolder = list.getType().equalsIgnoreCase("FOLDER");
		return isFolder && !this.hasChildList(list.getId());
	}

	public boolean isFolder(Object itemId) {
		try {
			int listId = Integer.valueOf(itemId.toString());
			GermplasmList germplasmList = this.germplasmListManager.getGermplasmListById(listId);
			if (germplasmList == null) {
				return false;
			}
			return germplasmList.getType().equalsIgnoreCase("FOLDER");
		} catch (MiddlewareQueryException e) {
			return false;
		} catch (NumberFormatException e) {
			if (this.listId != null
					&& (this.listId.toString().equals(ListManagerTreeComponent.LOCAL) || this.listId.toString().equals(
							ListManagerTreeComponent.CENTRAL))) {
				return true;
			} else {
				return false;
			}
		}
	}

	public void addGermplasmListNode(int parentGermplasmListId) throws InternationalizableException {
		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

		try {
			germplasmListChildren =
					this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
							this.getCurrentProgramUUID(), ListManagerTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			ListManagerTreeComponent.LOG.error("Error in getting germplasm lists by parent id.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			germplasmListChildren = new ArrayList<GermplasmList>();
		}

		for (GermplasmList listChild : germplasmListChildren) {
			if (!this.forSelectingFolderToSaveIn || this.isFolder(listChild.getId())) {
				this.germplasmListTree.addItem(listChild.getId());
				this.germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
				this.germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
				// allow children if list has sub-lists
				this.germplasmListTree.setChildrenAllowed(listChild.getId(), this.hasChildList(listChild.getId()));
			}
		}
		this.germplasmListTree.select(parentGermplasmListId);
		this.germplasmListTree.setValue(parentGermplasmListId);
	}

	public void addGermplasmListNode(int parentGermplasmListId, Tree germplasmListTree) throws InternationalizableException {
		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

		try {
			germplasmListChildren =
					this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
							this.getCurrentProgramUUID(), ListManagerTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			ListManagerTreeComponent.LOG.error("Error in getting germplasm lists by parent id.", e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			germplasmListChildren = new ArrayList<GermplasmList>();
		}

		for (GermplasmList listChild : germplasmListChildren) {
			if (!this.forSelectingFolderToSaveIn || this.isFolder(listChild.getId())) {
				germplasmListTree.addItem(listChild.getId());
				germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
				germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
				// allow children if list has sub-lists
				germplasmListTree.setChildrenAllowed(listChild.getId(), this.hasChildList(listChild.getId()));
			}
		}
		germplasmListTree.select(parentGermplasmListId);
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public void expandOrCollapseListTreeNode(Object nodeId) {
		if (!this.germplasmListTree.isExpanded(nodeId)) {
			this.germplasmListTree.expandItem(nodeId);
		} else {
			this.germplasmListTree.collapseItem(nodeId);
		}
		this.germplasmListTree.setValue(nodeId);
		this.germplasmListTree.select(nodeId);
	}

	public Tree getGermplasmListTree() {
		return this.germplasmListTree;
	}

	public void setSelectedListId(Object listId) {
		this.selectedListId = listId;
		this.germplasmListTree.select(listId);
		this.germplasmListTree.setValue(listId);
	}

	public void setListId(Integer listId) {
		this.listId = listId;
	}

	public Object getSelectedListId() {
		return this.selectedListId;
	}

	private ListManagerTreeComponent getListManagerTreeComponent() {
		return this;
	}

	public Tree getTree() {
		return this.germplasmListTree;
	}

	@Override
	public Window getWindow() {
		if (super.getWindow().getParent() != null) {
			return super.getWindow().getParent();
		} else {
			return super.getWindow();
		}
	}

}
