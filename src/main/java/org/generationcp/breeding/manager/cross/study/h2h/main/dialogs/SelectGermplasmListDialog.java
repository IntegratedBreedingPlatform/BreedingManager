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

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Mark Agarrado
 *
 */
@Configurable
public class SelectGermplasmListDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -8113004135173349534L;

	public final static String CLOSE_BUTTON_ID = "SelectGermplasmListDialog Close Button";
	public final static String ADD_BUTTON_ID = "SelectGermplasmListDialog Add Button";

	private VerticalLayout mainLayout;
	private SelectGermplasmListComponent selectGermplasmList;
	private Button cancelButton;
	private Button doneButton;
	private HorizontalLayout buttonArea;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private boolean isTestEntry;
	private boolean doCloseDialog = true;

	private Component source;

	public SelectGermplasmListDialog() {
		super();
	}

	public SelectGermplasmListDialog(Component source, Window parentWindow, boolean isTestEntry) {
		this.source = source;
		this.isTestEntry = isTestEntry;
	}

	protected void assemble() {
		this.initializeComponents();
		this.initializeValues();
		this.initializeLayout();
		this.initializeActions();
	}

	protected void initializeComponents() {
		this.mainLayout = new VerticalLayout();
		this.selectGermplasmList = new SelectGermplasmListComponent(null, this);

		this.buttonArea = new HorizontalLayout();
		this.cancelButton = new Button(); // "Cancel"
		this.cancelButton.setData(SelectGermplasmListDialog.CLOSE_BUTTON_ID);
		this.doneButton = new Button(); // "Done"
		this.doneButton.setData(SelectGermplasmListDialog.ADD_BUTTON_ID);
		this.doneButton.setEnabled(false);
		this.doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	public void setDoneButton(boolean bool) {
		this.doneButton.setEnabled(bool);
	}

	protected void initializeValues() {

	}

	protected void initializeLayout() {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("800px");
		this.setHeight("540px");
		this.setResizable(false);
		this.setCaption("Select Germplasm List");
		// center window within the browser
		this.center();

		this.buttonArea.setMargin(false, true, false, true);
		this.buttonArea.setSpacing(true);

		this.buttonArea.addComponent(this.doneButton);
		this.buttonArea.addComponent(this.cancelButton);

		this.mainLayout.addComponent(this.selectGermplasmList);
		this.mainLayout.addComponent(this.buttonArea);
		this.mainLayout.setComponentAlignment(this.buttonArea, Alignment.MIDDLE_RIGHT);

		this.setContent(this.mainLayout);
	}

	protected void initializeActions() {
		this.doneButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
		// only close window if calling screen used the list successfully.
		// eg. table permutations will not cause heap space error
		this.doneButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (SelectGermplasmListDialog.this.doCloseDialog) {
					Window window = event.getButton().getWindow();
					window.getParent().removeWindow(window);
				}
			}
		});

		this.cancelButton.addListener(new CloseWindowAction());
	}

	// called by SelectListButtonClickListener for the "Done" button
	public void populateParentList() {
		// retrieve list entries and add them to the parent ListSelect component
		SelectGermplasmListInfoComponent listInfoComponent = this.selectGermplasmList.getListInfoComponent();
		this.doCloseDialog =
				((SpecifyGermplasmsComponent) this.source).addGermplasmList(listInfoComponent.getGermplasmListId(), listInfoComponent
						.getEntriesTable().size(), this.isTestEntry);

		Table listEntryValues = listInfoComponent.getEntriesTable();
		// remove existing list entries if selected list has entries
		if (listEntryValues.size() == 0) {
			this.doneButton.setEnabled(false);
		} else {
			this.doneButton.setEnabled(true);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.assemble();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.cancelButton, Message.CLOSE_SCREEN_LABEL);
		this.messageSource.setCaption(this.doneButton, Message.ADD_LIST_ENTRY);
	}
}
