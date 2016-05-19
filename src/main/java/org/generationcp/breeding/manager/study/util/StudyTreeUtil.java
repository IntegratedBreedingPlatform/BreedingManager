
package org.generationcp.breeding.manager.study.util;

import java.io.Serializable;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.exception.GermplasmStudyBrowserException;
import org.generationcp.breeding.manager.study.StudyTreeComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeTargetDetails;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class StudyTreeUtil implements Serializable {

	private static final long serialVersionUID = -4427723835290060592L;
	private static final int STUDY_NAME_LIMITS = 255;
	private static final Logger LOG = LoggerFactory.getLogger(StudyTreeUtil.class);

	private static final String NO_SELECTION = "Please select a folder item";
	public static final String NOT_FOLDER = "Selected item is not a folder.";
	public static final String NO_PARENT = "Selected item is a root item, please choose another item on the tree";
	public static final String HAS_CHILDREN = "Folder has child items.";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private StudyDataManager studyDataManager;

	private final Tree targetTree;
	private final StudyTreeComponent source;

	public StudyTreeUtil(Tree targetTree, StudyTreeComponent source) {
		this.targetTree = targetTree;
		this.source = source;
		this.setupTreeDragAndDropHandler();
	}

	public void addFolder(final Object parentItemId, final String programUUID) {

		final BaseSubWindow w = new BaseSubWindow("Add new folder");
		w.setOverrideFocus(true);
		w.setWidth("320px");
		w.setHeight("160px");
		w.setModal(true);
		w.setResizable(false);
		w.setStyleName(Reindeer.WINDOW_LIGHT);

		VerticalLayout container = new VerticalLayout();
		container.setSpacing(true);
		container.setMargin(true);

		HorizontalLayout formContainer = new HorizontalLayout();
		formContainer.setSpacing(true);

		Label l = new Label("Folder Name");
		l.addStyleName("gcp-form-title");
		final TextField name = new TextField();
		name.setMaxLength(50);
		name.setWidth("190px");
		name.focus();

		formContainer.addComponent(l);
		formContainer.addComponent(name);

		HorizontalLayout btnContainer = new HorizontalLayout();
		btnContainer.setSpacing(true);
		btnContainer.setWidth("100%");

		Label spacer = new Label("");
		btnContainer.addComponent(spacer);
		btnContainer.setExpandRatio(spacer, 1.0F);

		Button ok = new Button("Ok");
		ok.setClickShortcut(KeyCode.ENTER);
		ok.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		ok.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -6313787074401316900L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				Integer newFolderId = null;
				String newFolderName = name.getValue().toString();
				// 1 by default because root study folder has id = 1
				int parentFolderId = 1;

				try {
					if (!StudyTreeUtil.this.isValidNameInput(name.getValue().toString(), programUUID)) {
						return;
					}

					if (parentItemId != null && parentItemId instanceof Integer) {
						if (StudyTreeUtil.this.source.isFolder((Integer) parentItemId)) {
							parentFolderId = ((Integer) parentItemId).intValue();
						} else {
							int selectItemId = ((Integer) parentItemId).intValue();
							DmsProject parentFolder = StudyTreeUtil.this.studyDataManager.getParentFolder(selectItemId);
							parentFolderId = parentFolder.getProjectId().intValue();
						}
					}

					newFolderId =
							Integer.valueOf(StudyTreeUtil.this.studyDataManager.addSubFolder(parentFolderId, newFolderName, newFolderName,
									programUUID));
				} catch (MiddlewareQueryException ex) {
					StudyTreeUtil.LOG.error("Error with adding a study folder.", ex);
					MessageNotifier.showError(StudyTreeUtil.this.source.getWindow(),
							StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_DATABASE),
							StudyTreeUtil.this.messageSource.getMessage(Message.PLEASE_SEE_ERROR_LOG));
					return;
				}

				// update UI
				if (newFolderId != null) {
					StudyTreeUtil.this.targetTree.addItem(newFolderId);
					StudyTreeUtil.this.targetTree.setItemCaption(newFolderId, newFolderName);
					StudyTreeUtil.this.targetTree.setItemIcon(newFolderId, new ThemeResource("../vaadin-retro/svg/folder-icon.svg"));
					StudyTreeUtil.this.targetTree.setChildrenAllowed(newFolderId, true);

					StudyTreeUtil.this.source.setSelectedStudyTreeNodeId(newFolderId);

					if (parentFolderId == 1) {
						StudyTreeUtil.this.targetTree.setChildrenAllowed(StudyTreeComponent.STUDY_ROOT_NODE, true);
						StudyTreeUtil.this.targetTree.setParent(newFolderId, StudyTreeComponent.STUDY_ROOT_NODE);
					} else {
						StudyTreeUtil.this.targetTree.setChildrenAllowed(Integer.valueOf(parentFolderId), true);
						StudyTreeUtil.this.targetTree.setParent(newFolderId, Integer.valueOf(parentFolderId));
					}

					if (StudyTreeUtil.this.targetTree.getValue() != null) {
						if (!StudyTreeUtil.this.targetTree.isExpanded(StudyTreeUtil.this.targetTree.getValue())) {
							StudyTreeUtil.this.targetTree.expandItem(parentItemId);
						}
					} else {
						StudyTreeUtil.this.targetTree.expandItem(StudyTreeComponent.STUDY_ROOT_NODE);
					}

					StudyTreeUtil.this.targetTree.select(newFolderId);
					StudyTreeUtil.this.source.updateButtons(newFolderId);
				}

				// close popup
				StudyTreeUtil.this.source.getParentComponent().getWindow().removeWindow(event.getComponent().getWindow());
			}

		});

		Button cancel = new Button("Cancel");
		cancel.setClickShortcut(KeyCode.ESCAPE);
		cancel.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -6542741100092010158L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				StudyTreeUtil.this.source.getWindow().focus();
				StudyTreeUtil.this.source.getParentComponent().getWindow().removeWindow(w);
			}
		});

		btnContainer.addComponent(ok);
		btnContainer.addComponent(cancel);

		container.addComponent(formContainer);
		container.addComponent(btnContainer);

		w.setContent(container);

		// show window
		this.source.getParentComponent().getWindow().addWindow(w);
	}

	protected boolean isValidNameInput(String newFolderName, String programUUID) throws MiddlewareQueryException {
		if ("".equals(newFolderName.replace(" ", ""))) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(), this.messageSource.getMessage(Message.INVALID_ITEM_NAME));
			return false;

		} else if (newFolderName.length() > StudyTreeUtil.STUDY_NAME_LIMITS) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					this.messageSource.getMessage(Message.INVALID_LONG_STUDY_FOLDER_NAME));
			return false;

		} else if (this.studyDataManager.checkIfProjectNameIsExistingInProgram(newFolderName, programUUID)) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					this.messageSource.getMessage(Message.EXISTING_STUDY_ERROR_MESSAGE));
			return false;

		} else if (newFolderName.equalsIgnoreCase(this.messageSource.getMessage(Message.NURSERIES_AND_TRIALS))) {
			MessageNotifier.showRequiredFieldError(this.source.getWindow(),
					this.messageSource.getMessage(Message.EXISTING_STUDY_ERROR_MESSAGE));
			return false;
		}

		return true;
	}

	protected boolean setParent(Object sourceItemId, Object targetItemId, boolean isStudy) {

		if (sourceItemId.equals(StudyTreeComponent.STUDY_ROOT_NODE)) {
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_WITH_MODIFYING_STUDY_TREE),
					this.messageSource.getMessage(Message.MOVE_ROOT_FOLDERS_NOT_ALLOWED));
			return false;
		}

		Integer sourceId = null;
		Integer targetId = null;

		if (sourceItemId != null && !sourceItemId.equals(StudyTreeComponent.STUDY_ROOT_NODE)) {
			sourceId = Integer.valueOf(sourceItemId.toString());
		}

		if (this.source.hasChildStudy(sourceId)) {
			MessageNotifier.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_WITH_MODIFYING_STUDY_TREE),
					StudyTreeUtil.HAS_CHILDREN);
			return false;
		}

		if (targetItemId != null && !StudyTreeComponent.STUDY_ROOT_NODE.equals(targetItemId)) {
			targetId = Integer.valueOf(targetItemId.toString());
		}

		try {
			if (targetId != null && sourceId != null) {
				this.studyDataManager.moveDmsProject(sourceId.intValue(), targetId.intValue(), isStudy);
			}
		} catch (MiddlewareQueryException e) {
			StudyTreeUtil.LOG.error("Error with moving node to target folder.", e);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_INTERNAL),
					this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

		// apply to UI
		if (targetItemId == null || this.targetTree.getItem(targetItemId) == null) {
			this.targetTree.setChildrenAllowed(sourceItemId, true);
			this.targetTree.setParent(sourceItemId, StudyTreeComponent.STUDY_ROOT_NODE);
			this.targetTree.expandItem(StudyTreeComponent.STUDY_ROOT_NODE);
		} else {
			this.targetTree.setChildrenAllowed(targetItemId, true);
			this.targetTree.setParent(sourceItemId, targetItemId);
			this.targetTree.expandItem(targetItemId);
		}
		this.targetTree.select(sourceItemId);
		return true;
	}

	private void setupTreeDragAndDropHandler() {
		this.targetTree.setDropHandler(new DropHandler() {

			private static final long serialVersionUID = -6676297159926786216L;

			@Override
			public void drop(DragAndDropEvent dropEvent) {
				Transferable t = dropEvent.getTransferable();
				if (t.getSourceComponent() != StudyTreeUtil.this.targetTree) {
					return;
				}

				TreeTargetDetails target = (TreeTargetDetails) dropEvent.getTargetDetails();

				Object sourceItemId = t.getData("itemId");
				Object targetItemId = target.getItemIdOver();

				VerticalDropLocation location = target.getDropLocation();

				if (location != VerticalDropLocation.MIDDLE || sourceItemId.equals(targetItemId)) {
					return;
				}

				boolean sourceIsStudy = !StudyTreeUtil.this.source.isFolder((Integer) sourceItemId);
				if (targetItemId instanceof Integer) {
					Boolean targetIsFolder = StudyTreeUtil.this.source.isFolder((Integer) targetItemId);
					if (targetIsFolder) {
						StudyTreeUtil.this.setParent(sourceItemId, targetItemId, sourceIsStudy);
					} else {
						try {
							DmsProject parentFolder =
									StudyTreeUtil.this.studyDataManager.getParentFolder(((Integer) targetItemId).intValue());
							if (parentFolder != null) {
								if (parentFolder.getProjectId().equals(Integer.valueOf(1))) {
									StudyTreeUtil.this.setParent(sourceItemId, StudyTreeComponent.STUDY_ROOT_NODE, sourceIsStudy);
								} else {
									StudyTreeUtil.this.setParent(sourceItemId, parentFolder.getProjectId(), sourceIsStudy);
								}
							} else {
								StudyTreeUtil.this.setParent(sourceItemId, StudyTreeComponent.STUDY_ROOT_NODE, sourceIsStudy);
							}
						} catch (MiddlewareQueryException e) {
							StudyTreeUtil.LOG.error("Error with getting parent folder of a project record.", e);
							MessageNotifier.showError(StudyTreeUtil.this.source.getWindow(),
									StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_INTERNAL),
									StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
						}
					}
				} else {
					StudyTreeUtil.this.setParent(sourceItemId, targetItemId, sourceIsStudy);
				}
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
		});
	}

	public void renameFolder(final Integer studyId, final String name, final String programUUID) {

		final BaseSubWindow w = new BaseSubWindow();
		w.setOverrideFocus(true);
		w.setCaption(this.messageSource.getMessage(Message.RENAME_ITEM));
		w.setWidth("320px");
		w.setHeight("160px");
		w.setModal(true);
		w.setResizable(false);
		w.setStyleName(Reindeer.WINDOW_LIGHT);

		VerticalLayout container = new VerticalLayout();
		container.setSpacing(true);
		container.setMargin(true);

		HorizontalLayout formContainer = new HorizontalLayout();
		formContainer.setSpacing(true);

		Label l = new Label(this.messageSource.getMessage(Message.ITEM_NAME));
		l.addStyleName("gcp-form-title");

		final TextField nameField = new TextField();
		nameField.setMaxLength(50);
		nameField.setValue(name);
		nameField.setCursorPosition(nameField.getValue() == null ? 0 : nameField.getValue().toString().length());
		nameField.setWidth("200px");
		nameField.focus();

		formContainer.addComponent(l);
		formContainer.addComponent(nameField);

		HorizontalLayout btnContainer = new HorizontalLayout();
		btnContainer.setSpacing(true);
		btnContainer.setWidth("100%");

		Label spacer = new Label("");
		btnContainer.addComponent(spacer);
		btnContainer.setExpandRatio(spacer, 1.0F);

		Button ok = new Button("Ok");
		ok.setClickShortcut(KeyCode.ENTER);
		ok.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		ok.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {

				try {
					String newName = nameField.getValue().toString().trim();
					if (!name.equals(newName)) {
						if (!StudyTreeUtil.this.isValidNameInput(newName, programUUID)) {
							return;
						}

						StudyTreeUtil.this.studyDataManager.renameSubFolder(newName, studyId, programUUID);

						StudyTreeUtil.this.targetTree.setItemCaption(studyId, newName);
						StudyTreeUtil.this.targetTree.select(studyId);

						// if node is study - rename tab name to new name
						if (!StudyTreeUtil.this.source.isFolder(studyId)) {
							StudyTreeUtil.this.source.renameStudyTab(name, newName);
						}
					}

				} catch (MiddlewareQueryException e) {
					MessageNotifier.showWarning(StudyTreeUtil.this.source.getWindow(),
							StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_DATABASE),
							StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
				} catch (Exception e) {
					MessageNotifier.showError(StudyTreeUtil.this.source.getWindow(),
							StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_INTERNAL),
							StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_REPORT_TO));
					StudyTreeUtil.LOG.error(e.getMessage(), e);
					return;
				}

				StudyTreeUtil.this.source.getParentComponent().getWindow().removeWindow(event.getComponent().getWindow());
			}
		});

		Button cancel = new Button("Cancel");
		cancel.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				StudyTreeUtil.this.source.getWindow().focus();
				StudyTreeUtil.this.source.getParentComponent().getWindow().removeWindow(w);
			}
		});
		cancel.setClickShortcut(KeyCode.ESCAPE);

		btnContainer.addComponent(ok);
		btnContainer.addComponent(cancel);

		container.addComponent(formContainer);
		container.addComponent(btnContainer);

		w.setContent(container);

		// show window
		this.source.getParentComponent().getWindow().addWindow(w);
	}

	/**
	 * Checks if given id is: 1. existing in the database 2. is a folder 3. does not has have children items
	 *
	 * If any of the checking failed, throws exception
	 *
	 * @param id
	 * @throws GermplasmStudyBrowserException
	 */
	public void validateForDeleteNurseryList(Integer id) throws GermplasmStudyBrowserException {
		StudyTreeUtil.LOG.info("id = " + id);
		if (id == null) {
			throw new GermplasmStudyBrowserException(StudyTreeUtil.NO_SELECTION);
		}
		DmsProject project = null;

		try {
			project = this.studyDataManager.getProject(id);

		} catch (MiddlewareQueryException e) {
			throw new GermplasmStudyBrowserException(this.messageSource.getMessage(Message.ERROR_DATABASE));
		}

		if (project == null) {
			throw new GermplasmStudyBrowserException(this.messageSource.getMessage(Message.ERROR_DATABASE));
		}

		if (!this.source.isFolder(id)) {
			throw new GermplasmStudyBrowserException(StudyTreeUtil.NOT_FOLDER);
		}

		if (this.source.hasChildStudy(id)) {
			throw new GermplasmStudyBrowserException(StudyTreeUtil.HAS_CHILDREN);
		}

	}

	/**
	 * Performs validations on folder to be deleted. If folder can be deleted, deletes it from database and adjusts tree view
	 * 
	 * @param studyId
	 */
	public void deleteFolder(final Integer studyId, final String programUUID) {
		try {
			this.validateForDeleteNurseryList(studyId);
		} catch (GermplasmStudyBrowserException e) {
			StudyTreeUtil.LOG.error(e.getMessage());
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_TEXT), e.getMessage());
			return;
		}

		ConfirmDialog.show(this.source.getParentComponent().getWindow(), this.messageSource.getMessage(Message.DELETE_ITEM),
				this.messageSource.getMessage(Message.DELETE_ITEM_CONFIRM), this.messageSource.getMessage(Message.YES),
				this.messageSource.getMessage(Message.NO), new ConfirmDialog.Listener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							try {

								DmsProject parent = StudyTreeUtil.this.studyDataManager.getParentFolder(studyId);
								StudyTreeUtil.this.studyDataManager.deleteEmptyFolder(studyId, programUUID);

								StudyTreeUtil.this.targetTree.removeItem(StudyTreeUtil.this.targetTree.getValue());
								if (parent != null) {
									Integer parentId = parent.getProjectId();
									if (parentId == 1) {
										StudyTreeUtil.this.targetTree.select(StudyTreeComponent.STUDY_ROOT_NODE);
									} else {
										StudyTreeUtil.this.targetTree.select(parentId);
										StudyTreeUtil.this.targetTree.expandItem(parentId);
									}
								}
								StudyTreeUtil.this.targetTree.setImmediate(true);
								StudyTreeUtil.this.source.updateButtons(StudyTreeUtil.this.targetTree.getValue());

							} catch (MiddlewareQueryException e) {
								MessageNotifier.showError(StudyTreeUtil.this.source.getWindow(),
										StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_DATABASE),
										StudyTreeUtil.this.messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
							}
						}
					}
				});
	}

	protected void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	protected void setStudyDataManager(StudyDataManager studyDataManager) {
		this.studyDataManager = studyDataManager;
	}

}
