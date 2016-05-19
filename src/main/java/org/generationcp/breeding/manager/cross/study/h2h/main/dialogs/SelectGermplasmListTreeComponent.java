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

package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.SelectListButtonClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.SelectListItemClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.SelectListTreeExpandListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class SelectGermplasmListTreeComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -8933173351951948514L;

	private static final Logger LOG = LoggerFactory.getLogger(SelectGermplasmListTreeComponent.class);
	private static final int BATCH_SIZE = 50;

	public static final String REFRESH_BUTTON_ID = "SelectGermplasmListTreeComponent Refresh Button";

	private Tree germplasmListTree;
	private Button refreshButton;

	private final SelectGermplasmListInfoComponent listInfoComponent;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private ContextUtil contextUtil;

	public SelectGermplasmListTreeComponent(SelectGermplasmListInfoComponent listInfoComponent) {
		this.listInfoComponent = listInfoComponent;
	}

	@Override
	public void afterPropertiesSet() {
		this.assemble();
	}

	protected void assemble() {
		this.initializeComponents();
		this.initializeValues();
		this.initializeLayout();
		this.initializeActions();
	}

	protected void initializeComponents() {
		this.germplasmListTree = this.createGermplasmListTree();

		this.refreshButton = new Button(); // "Refresh"
		this.refreshButton.setData(SelectGermplasmListTreeComponent.REFRESH_BUTTON_ID);

		// add tooltip
		this.germplasmListTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = -2669417630841097077L;

			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				return SelectGermplasmListTreeComponent.this.messageSource.getMessage(Message.GERMPLASM_LIST_DETAILS_LABEL);
			}
		});
	}

	protected void initializeValues() {
		// do nothing
	}

	protected void initializeLayout() {
		this.setSpacing(true);
		this.setMargin(true);

		this.addComponent(this.refreshButton);
		this.addComponent(this.germplasmListTree);
	}

	protected void initializeActions() {
		this.refreshButton.addListener(new SelectListButtonClickListener(this));
	}

	private Tree createGermplasmListTree() {
		List<GermplasmList> germplasmListParent = new ArrayList<GermplasmList>();

		try {
			germplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(this.getCurrentProgramUUID(),
					SelectGermplasmListTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			SelectGermplasmListTreeComponent.LOG.error(e.getMessage(), e);
			if (this.getWindow() != null) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
			}
			germplasmListParent = new ArrayList<GermplasmList>();
		}

		Tree currentGermplasmListTree = new Tree();

		for (GermplasmList parentList : germplasmListParent) {
			currentGermplasmListTree.addItem(parentList.getId());
			currentGermplasmListTree.setItemCaption(parentList.getId(), parentList.getName());
		}

		currentGermplasmListTree.addListener(new SelectListTreeExpandListener(this));
		currentGermplasmListTree.addListener(new SelectListItemClickListener(this));

		return currentGermplasmListTree;
	}

	protected String getCurrentProgramUUID() {
		return this.contextUtil.getCurrentProgramUUID();
	}

	// Called by SelectListButtonClickListener
	public void createTree() {
		this.removeComponent(this.germplasmListTree);
		this.germplasmListTree.removeAllItems();
		this.germplasmListTree = this.createGermplasmListTree();
		this.addComponent(this.germplasmListTree);
	}

	// called by SelectListItemClickListener
	public void displayGermplasmListDetails(int germplasmListId) {
		try {
			this.displayGermplasmListInfo(germplasmListId);
		} catch (NumberFormatException e) {
			SelectGermplasmListTreeComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
					this.messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
		} catch (MiddlewareQueryException e) {
			SelectGermplasmListTreeComponent.LOG.error(e.toString() + "\n" + e.getStackTrace());
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
		}

	}

	public void displayGermplasmListInfo(int germplasmListId) throws MiddlewareQueryException {
		GermplasmList germplasmList;

		if (!this.hasChildList(germplasmListId) && !this.isEmptyFolder(germplasmListId)) {
			germplasmList = this.germplasmListManager.getGermplasmListById(germplasmListId);
		} else {
			germplasmList = null;
		}

		this.listInfoComponent.displayListInfo(germplasmList);
		this.listInfoComponent.setGermplasmListId(germplasmListId);
	}

	// called by SelectListTreeExpandListener
	public void addGermplasmListNode(int parentGermplasmListId) {
		List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();

		try {
			germplasmListChildren = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId,
					this.getCurrentProgramUUID(),
					SelectGermplasmListTreeComponent.BATCH_SIZE);
		} catch (MiddlewareQueryException e) {
			SelectGermplasmListTreeComponent.LOG.error(e.getMessage(), e);
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

	private boolean hasChildList(int listId) {
		List<GermplasmList> listChildren = new ArrayList<GermplasmList>();
		try {
			listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, this.getCurrentProgramUUID(), 0, 1);
		} catch (MiddlewareQueryException e) {
			SelectGermplasmListTreeComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
			listChildren = new ArrayList<GermplasmList>();
		}
		return !listChildren.isEmpty();
	}

	private boolean isEmptyFolder(int listId) throws MiddlewareQueryException {
		boolean isFolder = "FOLDER".equalsIgnoreCase(this.germplasmListManager.getGermplasmListById(listId).getType());
		return isFolder && !this.hasChildList(listId);
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

}
