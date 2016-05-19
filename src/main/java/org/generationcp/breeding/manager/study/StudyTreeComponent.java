/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.GermplasmStudyBrowserLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.util.StudyBrowserTabCloseHandler;
import org.generationcp.breeding.manager.study.listeners.StudyItemClickListener;
import org.generationcp.breeding.manager.study.listeners.StudyTreeCollapseListener;
import org.generationcp.breeding.manager.study.listeners.StudyTreeExpandListener;
import org.generationcp.breeding.manager.study.util.StudyTreeUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserProgramStateDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class StudyTreeComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		GermplasmStudyBrowserLayout {

	private static final long serialVersionUID = -3481988646509402160L;

	private static final Logger LOG = LoggerFactory.getLogger(StudyTreeComponent.class);

	public static final String REFRESH_BUTTON_ID = "StudyTreeComponent Refresh Button";
	public static final String STUDY_ROOT_NODE = "STUDY_ROOT_NODE";

	@Autowired
	private StudyDataManager studyDataManager;

	private VerticalLayout treeContainer;
	private Tree studyTree;
	private TabSheet tabSheetStudy;

	private final StudyBrowserMain studyBrowserMain;
	private StudyBrowserMainLayout studyBrowserMainLayout;

	private Label controlButtonsHeading;
	private HorizontalLayout controlButtonsLayout;
	private HorizontalLayout controlButtonsSubLayout;
	private Button addFolderBtn;
	private Button deleteFolderBtn;
	private Button renameFolderBtn;

	private final ThemeResource folderResource = new ThemeResource("../vaadin-retro/svg/folder-icon.svg");
	private final ThemeResource studyResource = new ThemeResource("../vaadin-retro/svg/study-icon.svg");
	private final ThemeResource dataSetResource = new ThemeResource("../vaadin-retro/svg/dataset-icon.svg");

	private Button refreshButton;

	private Integer rootNodeProjectId;
	private Map<Integer, Integer> parentChildItemIdMap;
	private Object selectedStudyTreeNodeId;
	private StudyTreeUtil studyTreeUtil;

	@Autowired
	private ContextUtil contextUtil;

	@Autowired
	private UserProgramStateDataManager programStateManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public StudyTreeComponent(StudyBrowserMain studyBrowserMain) {
		this.studyBrowserMain = studyBrowserMain;
	}

	@Override
	public void afterPropertiesSet() {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {

		this.studyBrowserMainLayout = this.studyBrowserMain.getMainLayout();

		this.tabSheetStudy = new TabSheet();
		this.tabSheetStudy.setHeight("615px");

		this.initializeButtonPanel();

		this.studyTree = this.createCombinedStudyTree();

		// add tooltip
		this.studyTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = -2669417630841097077L;

			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				return StudyTreeComponent.this.messageSource.getMessage(Message.STUDY_DETAILS_LABEL);
			}
		});

		this.refreshButton = new Button(); // "Refresh"
		this.refreshButton.setData(StudyTreeComponent.REFRESH_BUTTON_ID);
		this.refreshButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		this.studyTreeUtil = new StudyTreeUtil(this.studyTree, this);

		this.refreshButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				createTree();
				reinitializeTree();
			}
		});

		this.renameFolderBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				int studyId = Integer.valueOf(StudyTreeComponent.this.selectedStudyTreeNodeId.toString());
				String name = StudyTreeComponent.this.studyTree.getItemCaption(StudyTreeComponent.this.selectedStudyTreeNodeId);
				StudyTreeComponent.this.studyTreeUtil
						.renameFolder(studyId, name, StudyTreeComponent.this.getCurrentProject().getUniqueID());
			}
		});

		this.addFolderBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				StudyTreeComponent.this.studyTreeUtil.addFolder(StudyTreeComponent.this.selectedStudyTreeNodeId, StudyTreeComponent.this
						.getCurrentProject().getUniqueID());
			}
		});

		this.deleteFolderBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				int studyId = Integer.valueOf(StudyTreeComponent.this.selectedStudyTreeNodeId.toString());
				StudyTreeComponent.this.studyTreeUtil.deleteFolder(studyId, StudyTreeComponent.this.getCurrentProject().getUniqueID());
			}
		});
	}

	public void reinitializeTree() {
		try {
			List<String> parsedState =
					programStateManager.getUserProgramTreeStateByUserIdProgramUuidAndType(contextUtil.getCurrentWorkbenchUserId(),
							contextUtil.getCurrentProgramUUID(), ListTreeState.STUDY_LIST.name());

			if (parsedState.isEmpty() || (parsedState.size() == 1 && StringUtils.isEmpty(parsedState.get(0)))) {
				studyTree.collapseItem(STUDY_ROOT_NODE);
				return;
			}

			studyTree.expandItem(STUDY_ROOT_NODE);
			for (String s : parsedState) {
				String trimmed = s.trim();
				if (!StringUtils.isNumeric(trimmed)) {
					continue;
				}

				int itemId = Integer.parseInt(trimmed);
				studyTree.expandItem(itemId);
			}

			studyTree.select(null);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		this.controlButtonsSubLayout = new HorizontalLayout();
		this.controlButtonsSubLayout.addComponent(this.addFolderBtn);
		this.controlButtonsSubLayout.addComponent(this.renameFolderBtn);
		this.controlButtonsSubLayout.addComponent(this.deleteFolderBtn);
		this.controlButtonsSubLayout.setComponentAlignment(this.addFolderBtn, Alignment.BOTTOM_RIGHT);
		this.controlButtonsSubLayout.setComponentAlignment(this.renameFolderBtn, Alignment.BOTTOM_RIGHT);
		this.controlButtonsSubLayout.setComponentAlignment(this.deleteFolderBtn, Alignment.BOTTOM_RIGHT);

		this.controlButtonsLayout = new HorizontalLayout();
		this.controlButtonsLayout.setWidth("100%");
		this.controlButtonsLayout.setHeight("30px");
		this.controlButtonsLayout.setSpacing(true);

		this.controlButtonsLayout.addComponent(this.controlButtonsHeading);
		this.controlButtonsLayout.addComponent(this.controlButtonsSubLayout);
		this.controlButtonsLayout.setComponentAlignment(this.controlButtonsHeading, Alignment.BOTTOM_LEFT);
		this.controlButtonsLayout.setComponentAlignment(this.controlButtonsSubLayout, Alignment.BOTTOM_RIGHT);

		this.treeContainer = new VerticalLayout();
		this.treeContainer.addComponent(this.studyTree);

		this.addComponent(this.controlButtonsLayout);
		this.addComponent(this.treeContainer);
		this.addComponent(this.refreshButton);
	}

	// Called by StudyButtonClickListener
	public void createTree() {
		this.treeContainer.removeComponent(this.studyTree);
		this.studyTree.removeAllItems();

		this.studyTree = this.createCombinedStudyTree();
		this.studyTree.setNullSelectionAllowed(false);
		this.studyTreeUtil = new StudyTreeUtil(this.studyTree, this);

		this.treeContainer.addComponent(this.studyTree);
	}

	private Tree createCombinedStudyTree() {

		this.studyTree = new Tree();
		this.studyTree.setDragMode(TreeDragMode.NODE);

		this.studyTree.addItem(StudyTreeComponent.STUDY_ROOT_NODE);
		this.studyTree.setItemCaption(StudyTreeComponent.STUDY_ROOT_NODE, this.messageSource.getMessage(Message.NURSERIES_AND_TRIALS));
		this.studyTree.setItemIcon(StudyTreeComponent.STUDY_ROOT_NODE, this.getThemeResourceByReference(new FolderReference(null, null)));

		this.populateRootNode(this.studyTree, StudyTreeComponent.STUDY_ROOT_NODE);

		this.studyTree.addListener(new StudyTreeExpandListener(this));
		this.studyTree.addListener(new StudyItemClickListener(this));
		this.studyTree.addListener(new StudyTreeCollapseListener(this));

		this.studyTree.addStyleName("studyBrowserTree");
		this.studyTree.setImmediate(true);
		this.studyTree.setWidth("98%");

		return this.studyTree;
	}

	private Project getCurrentProject() {
		try {
			return contextUtil.getProjectInContext();
		} catch (MiddlewareQueryException e) {
			StudyTreeComponent.LOG.error(e.getMessage(), e);
			if (this.getWindow() != null) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						"Could not determine project in context.");
			}
			return null;
		}
	}

	public void populateRootNode(Tree studyTree, String rootNodeId) {
		List<Reference> rootFolders = new ArrayList<Reference>();
		try {
			rootFolders = this.studyDataManager.getRootFolders(this.getCurrentProject().getUniqueID(), StudyType.nurseriesAndTrials());
		} catch (MiddlewareQueryException e) {
			StudyTreeComponent.LOG.error(e.getMessage(), e);
			if (this.getWindow() != null) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_STUDIES));
			}
		}

		for (Reference ps : rootFolders) {
			studyTree.addItem(ps.getId());
			studyTree.setItemCaption(ps.getId(), ps.getName());

			if (!ps.isFolder()) {
				studyTree.setItemIcon(ps.getId(), this.studyResource);
			} else {
				studyTree.setItemIcon(ps.getId(), this.getThemeResourceByReference(ps));
			}

			studyTree.setParent(ps.getId(), rootNodeId);
			if (!this.hasChildStudy(ps.getId())) {
				studyTree.setChildrenAllowed(ps.getId(), false);
			}
		}
	}

	// FIXME - Performance problem if such checking is done per tree node. The query that retrieves tree metadata should have all the information already.
	// Can not get rid of it until Vaadin tree object is constructed with appropriate information already available from Middleware service.
	public Boolean isFolder(Integer studyId) {
		try {
			boolean isStudy = this.studyDataManager.isStudy(studyId);
			return !isStudy;
		} catch (MiddlewareQueryException e) {
			return false;
		}
	}

	// Called by StudyItemClickListener
	public void studyTreeItemClickAction(Object itemId) {

		try {
			this.expandOrCollapseStudyTreeNode(itemId);
			int studyId = Integer.valueOf(itemId.toString());

			if (!this.hasChildStudy(studyId) && !this.isFolder(studyId)) {
				this.createStudyInfoTab(studyId);
			}

		} catch (NumberFormatException e) {
			StudyTreeComponent.LOG.error(e.getMessage(), e);
		} finally {
			this.updateButtons(itemId);
			this.selectedStudyTreeNodeId = itemId;
		}
	}

	public Boolean studyExists(int studyId) throws InternationalizableException {
		try {
			DmsProject study = this.studyDataManager.getProject(studyId);
			if (study == null) {
				return false;
			} else {
				if (!this.hasChildStudy(studyId) && !this.isFolder(studyId)) {
					return true;
				}
				return false;
			}
		} catch (MiddlewareQueryException e) {
			StudyTreeComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
			return false;
		}
	}

	public void addStudyNode(int parentStudyId) {

		List<Reference> studyChildren = new ArrayList<Reference>();
		try {
			studyChildren =
					this.studyDataManager.getChildrenOfFolder(Integer.valueOf(parentStudyId), this.getCurrentProject().getUniqueID(), StudyType.nurseriesAndTrials());
		} catch (MiddlewareQueryException e) {
			StudyTreeComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
			studyChildren = new ArrayList<Reference>();
		}

		for (Reference sc : studyChildren) {
			this.studyTree.addItem(sc.getId());
			this.studyTree.setItemCaption(sc.getId(), sc.getName());
			this.studyTree.setParent(sc.getId(), parentStudyId);

			// check if the study has sub study
			if (this.hasChildStudy(sc.getId())) {
				this.studyTree.setChildrenAllowed(sc.getId(), true);
				this.studyTree.setItemIcon(sc.getId(), this.getThemeResourceByReference(sc));
			} else {
				this.studyTree.setChildrenAllowed(sc.getId(), false);
				this.studyTree.setItemIcon(sc.getId(), this.getThemeResourceByReference(sc));
			}

		}
	}

	private void createStudyInfoTab(int studyId) {
		VerticalLayout layout = new VerticalLayout();

		if (!Util.isTabExist(this.tabSheetStudy, this.getStudyName(studyId))) {
			layout.addComponent(new StudyAccordionMenu(studyId, new StudyDetailComponent(this.studyDataManager, studyId),
					this.studyDataManager, false, false));
			Tab tab = this.tabSheetStudy.addTab(layout, this.getStudyName(studyId), null);
			tab.setClosable(true);

			this.studyBrowserMainLayout.addStudyInfoTabSheet(this.tabSheetStudy);
			this.studyBrowserMainLayout.showDetailsLayout();
			this.tabSheetStudy.setSelectedTab(layout);
			this.tabSheetStudy.setCloseHandler(new StudyBrowserTabCloseHandler(this.studyBrowserMainLayout));
		} else {
			Tab tab = Util.getTabAlreadyExist(this.tabSheetStudy, this.getStudyName(studyId));
			this.tabSheetStudy.setSelectedTab(tab.getComponent());
		}
	}

	private String getStudyName(int studyId) {
		try {
			DmsProject studyDetails = this.studyDataManager.getProject(studyId);
			if (studyDetails != null) {
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
			studyChildren = this.studyDataManager.getChildrenOfFolder(new Integer(studyId), this.getCurrentProject().getUniqueID(), StudyType.nurseriesAndTrials());
		} catch (MiddlewareQueryException e) {
			StudyTreeComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
			studyChildren = new ArrayList<Reference>();
		}
		if (!studyChildren.isEmpty()) {
			return true;
		}
		return false;
	}

	private void initializeButtonPanel() {
		this.controlButtonsHeading = new Label();
		this.controlButtonsHeading.setValue(this.messageSource.getMessage(Message.ALL_STUDIES));
		this.controlButtonsHeading.setStyleName(Bootstrap.Typography.H4.styleName());
		this.controlButtonsHeading.setWidth("177px");

		this.renameFolderBtn =
				new Button("<span class='bms-edit' style='left: 2px; color: #0083c0;font-size: 18px; font-weight: bold;'></span>");
		this.renameFolderBtn.setHtmlContentAllowed(true);
		this.renameFolderBtn.setDescription(this.messageSource.getMessage(Message.RENAME_ITEM));
		this.renameFolderBtn.setStyleName(BaseTheme.BUTTON_LINK);
		this.renameFolderBtn.setWidth("25px");
		this.renameFolderBtn.setHeight("30px");
		this.renameFolderBtn.setEnabled(false);

		this.addFolderBtn =
				new Button("<span class='bms-add' style='left: 2px; color: #00a950;font-size: 18px; font-weight: bold;'></span>");
		this.addFolderBtn.setHtmlContentAllowed(true);
		this.addFolderBtn.setDescription(this.messageSource.getMessage(Message.ADD_NEW_FOLDER));
		this.addFolderBtn.setStyleName(BaseTheme.BUTTON_LINK);
		this.addFolderBtn.setWidth("25px");
		this.addFolderBtn.setHeight("30px");
		this.addFolderBtn.setEnabled(false);

		this.deleteFolderBtn =
				new Button("<span class='bms-delete' style='left: 2px; color: #f4a41c;font-size: 18px; font-weight: bold;'></span>");
		this.deleteFolderBtn.setHtmlContentAllowed(true);
		this.deleteFolderBtn.setDescription(this.messageSource.getMessage(Message.DELETE_ITEM));
		this.deleteFolderBtn.setStyleName(BaseTheme.BUTTON_LINK);
		this.deleteFolderBtn.setWidth("25px");
		this.deleteFolderBtn.setHeight("30px");
		this.deleteFolderBtn.setEnabled(false);

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.refreshButton, Message.REFRESH_LABEL);
	}

	public TabSheet getTabSheetStudy() {
		return this.tabSheetStudy;
	}

	public void showChild(Integer childItemId) {
		this.buildChildMap(childItemId, true);
		Integer rootItemId = this.rootNodeProjectId;

		this.studyTree.expandItem(StudyTreeComponent.STUDY_ROOT_NODE);

		if (rootItemId != null) {
			this.addStudyNode(rootItemId);
			this.studyTree.expandItem(rootItemId);
		}

		Integer currentItemId = this.parentChildItemIdMap.get(rootItemId);
		if (currentItemId != null) {
			this.addStudyNode(currentItemId);
			this.studyTree.expandItem(currentItemId);
		}

		while (this.parentChildItemIdMap.get(currentItemId) != childItemId && currentItemId != null) {
			currentItemId = this.parentChildItemIdMap.get(currentItemId);
			if (currentItemId != null) {
				this.addStudyNode(currentItemId);
				this.studyTree.expandItem(currentItemId);
			}
		}
		this.studyTree.select(childItemId);
		this.studyTree.setNullSelectionAllowed(false);
		this.studyTree.select(childItemId);

	}

	private void buildChildMap(Integer studyId, Boolean endNode) {
		if (endNode) {
			this.parentChildItemIdMap = new HashMap<Integer, Integer>();
		}
		try {
			DmsProject studyParent = this.studyDataManager.getParentFolder(studyId);
			if (studyParent != null && (studyId < 0 && studyParent.getProjectId() != 1 || studyId > 0)) {
				int parentProjectId = studyParent.getProjectId();
				this.parentChildItemIdMap.put(parentProjectId, studyId);
				this.buildChildMap(studyParent.getProjectId(), false);
			} else {
				this.rootNodeProjectId = studyId;
			}
		} catch (MiddlewareQueryException e) {
			StudyTreeComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
		}
	}

	public void expandOrCollapseStudyTreeNode(Object itemId) {
		if (!this.studyTree.isExpanded(itemId)) {
			this.studyTree.expandItem(itemId);
		} else {
			this.studyTree.collapseItem(itemId);
		}
	}

	public void setSelectedStudyTreeNodeId(Object id) {
		this.selectedStudyTreeNodeId = id;
	}

	public void updateButtons(Object itemId) {
		this.setSelectedStudyTreeNodeId(itemId);
		if (itemId instanceof String) {
			// this means its the ROOT Folder
			this.addFolderBtn.setEnabled(true);
			this.renameFolderBtn.setEnabled(false);
			this.deleteFolderBtn.setEnabled(false);
		} else if (this.isFolder((Integer) itemId)) {
			this.addFolderBtn.setEnabled(true);
			this.renameFolderBtn.setEnabled(true);
			this.deleteFolderBtn.setEnabled(true);
			// The rest of the local lists
		} else {
			this.addFolderBtn.setEnabled(true);
			this.renameFolderBtn.setEnabled(true);
			this.deleteFolderBtn.setEnabled(false);
		}

	}

	/*
	 * Update the tab header and displayed study name with new name. This is called by rename function in study tree
	 */
	public void renameStudyTab(String oldName, String newName) {
		Tab studyTab = Util.getTabAlreadyExist(this.tabSheetStudy, oldName);
		if (studyTab != null) {
			studyTab.setCaption(newName);
		}
		Component component = studyTab.getComponent();

		if (component instanceof VerticalLayout) {
			VerticalLayout layout = (VerticalLayout) component;
			Iterator<Component> componentIterator = layout.getComponentIterator();
			while (componentIterator.hasNext()) {
				Component child = componentIterator.next();
				if (child instanceof StudyAccordionMenu) {
					StudyAccordionMenu accordion = (StudyAccordionMenu) child;
					accordion.updateStudyName(newName);
				}
			}
		}
	}

	public Tree getStudyTree() {
		return this.studyTree;
	}

	private ThemeResource getThemeResourceByReference(Reference r) {

		if (r instanceof FolderReference) {
			StudyTreeComponent.LOG.debug("r is FolderReference");
			return this.folderResource;
		} else if (r instanceof StudyReference) {
			StudyTreeComponent.LOG.debug("r is StudyReference");
			return this.studyResource;
		} else if (r instanceof DatasetReference) {
			StudyTreeComponent.LOG.debug("r is DatasetReference");
			return this.dataSetResource;
		} else {
			return this.folderResource;
		}

	}

	public StudyBrowserMain getParentComponent() {
		return this.studyBrowserMain;
	}
}
