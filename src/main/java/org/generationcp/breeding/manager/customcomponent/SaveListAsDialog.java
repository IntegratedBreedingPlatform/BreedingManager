
package org.generationcp.breeding.manager.customcomponent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.ParentTabComponent;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectTreeItemOnSaveListener;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.ListDateField;
import org.generationcp.breeding.manager.customfields.LocalListFoldersTreeComponent;
import org.generationcp.breeding.manager.inventory.ReserveInventoryAction;
import org.generationcp.breeding.manager.inventory.ReserveInventorySource;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class SaveListAsDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final String FOLDER_TYPE = "FOLDER";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SaveListAsDialog.class);

	private CssLayout mainLayout;
	private HorizontalLayout contentLayout;
	private HorizontalLayout buttonLayout;

	private final SaveListAsDialogSource source;

	private Label guideMessage;
	private LocalListFoldersTreeComponent germplasmListTree;
	private BreedingManagerListDetailsComponent listDetailsComponent;

	private Button cancelButton;
	private Button saveButton;

	private final String windowCaption;
	private boolean showFoldersOnlyInListTree = false;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	private GermplasmList originalGermplasmList;
	private GermplasmList germplasmList;

	public static final Integer LIST_NAMES_STATUS = 1;

	public SaveListAsDialog(final SaveListAsDialogSource source, final GermplasmList germplasmList) {
		this(source, germplasmList, null);
	}

	public SaveListAsDialog(final SaveListAsDialogSource source, final GermplasmList germplasmList, final String windowCaption) {
		this.source = source;
		this.originalGermplasmList = germplasmList;
		this.germplasmList = germplasmList;
		this.windowCaption = windowCaption;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		if (this.windowCaption == null) {
			this.setCaption(this.messageSource.getMessage(Message.SAVE_LIST_AS));
		} else {
			this.setCaption(this.windowCaption);
		}

		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setResizable(false);
		this.setModal(true);

		if (this.germplasmList != null) {
			this.germplasmListTree =
					new LocalListFoldersTreeComponent(new SelectTreeItemOnSaveListener(this, this.source.getParentComponent()),
							this.germplasmList.getId(), this.isShowFoldersOnlyInListTree(), true);
		} else {
			this.germplasmListTree =
					new LocalListFoldersTreeComponent(new SelectTreeItemOnSaveListener(this, this.source.getParentComponent()), null,
							this.isShowFoldersOnlyInListTree(), true);
		}

		this.guideMessage = new Label(this.messageSource
				.getMessage(Message.SELECT_A_FOLDER_TO_CREATE_A_LIST_OR_SELECT_AN_EXISTING_LIST_TO_EDIT_AND_OVERWRITE_ITS_ENTRIES) + ".");

		this.listDetailsComponent = new BreedingManagerListDetailsComponent(this.defaultListType(), this.germplasmList);
		this.listDetailsComponent.setDebugId("listDetailsComponent");

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setWidth("80px");

		this.saveButton = new Button(this.messageSource.getMessage(Message.SAVE_LABEL));
		this.saveButton.setDebugId("saveButton");
		this.saveButton.setWidth("80px");
		this.saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	public String defaultListType() {
		return "LST";
	}

	@Override
	public void initializeValues() {
		if (this.germplasmList != null) {
			this.listDetailsComponent.setGermplasmListDetails(this.germplasmList);
		} else {
			this.listDetailsComponent.setGermplasmListDetails(null);
		}

		this.germplasmListTree.reinitializeTree(true);
	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());
		this.saveButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 993268331611479850L;

			@Override
			public void buttonClick(final ClickEvent event) {
				SaveListAsDialog.this.doSaveAction(event);
			}
		});
	}

	@Override
	public void layoutComponents() {
		this.setWidth("740px");
		this.setHeight("510px");

		this.contentLayout = new HorizontalLayout();
		this.contentLayout.setDebugId("contentLayout");
		this.contentLayout.setSpacing(true);
		this.contentLayout.addComponent(this.germplasmListTree);
		this.contentLayout.addComponent(this.listDetailsComponent);
		this.contentLayout.addStyleName("contentLayout");

		this.contentLayout.setWidth("714px");
		this.contentLayout.setHeight("356px");

		this.listDetailsComponent.addStyleName("listDetailsComponent");

		this.buttonLayout = new HorizontalLayout();
		this.buttonLayout.setDebugId("buttonLayout");
		this.buttonLayout.setSpacing(true);
		this.buttonLayout.setMargin(true);
		this.buttonLayout.addComponent(this.cancelButton);
		this.buttonLayout.addComponent(this.saveButton);
		this.buttonLayout.addStyleName("buttonLayout");

		final HorizontalLayout buttonLayoutMain = new HorizontalLayout();
		buttonLayoutMain.setDebugId("buttonLayoutMain");
		buttonLayoutMain.addComponent(this.buttonLayout);
		buttonLayoutMain.setComponentAlignment(this.buttonLayout, Alignment.MIDDLE_CENTER);
		buttonLayoutMain.setWidth("100%");
		buttonLayoutMain.setHeight("50px");
		buttonLayoutMain.addStyleName("buttonLayoutMain");

		this.mainLayout = new CssLayout();
		this.mainLayout.setDebugId("saveListDialogMainLayout");
		this.mainLayout.setWidth("741px");
		this.mainLayout.setHeight("420px");
		this.mainLayout.addComponent(this.guideMessage);
		this.mainLayout.addComponent(this.contentLayout);
		this.mainLayout.addComponent(buttonLayoutMain);
		this.mainLayout.addStyleName("mainlayout");

		this.addComponent(this.mainLayout);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public GermplasmList getSelectedListOnTree() {
		Integer folderId = null;
		if (this.germplasmListTree.getSelectedListId() instanceof Integer) {
			folderId = (Integer) this.germplasmListTree.getSelectedListId();
		}

		GermplasmList folder = null;
		if (folderId != null) {
			try {
				folder = this.germplasmListManager.getGermplasmListById(folderId);
			} catch (final MiddlewareQueryException e) {
				SaveListAsDialog.LOG.error("Error with retrieving list with id: " + folderId, e);
			}
		}

		return folder;
	}

	public GermplasmList getGermplasmListToSave() {
		Integer currentId = null;
		if (this.germplasmList != null) {
			currentId = this.germplasmList.getId();
		}

		GermplasmList selectedList = this.getSelectedListOnTree();

		// If selected item on list/folder tree is a list, use that as target germplasm list
		if (selectedList != null && !SaveListAsDialog.FOLDER_TYPE.equalsIgnoreCase(selectedList.getType())) {
			this.germplasmList = this.getSelectedListOnTree();

			// Needed for overwriting
			this.source.setCurrentlySavedGermplasmList(this.germplasmList);

			// If selected item is a folder, get parent of that folder
			try {
				selectedList = this.germplasmListManager.getGermplasmListById(selectedList.getParentId());
			} catch (final MiddlewareQueryException e) {
				SaveListAsDialog.LOG.error("Error with getting parent list: " + selectedList.getParentId(), e);
			}

			// If not, use old method, get germplasm list the old way
		} else {
			this.germplasmList = this.listDetailsComponent.getGermplasmList();
			this.germplasmList.setId(currentId);
			this.germplasmList.setStatus(SaveListAsDialog.LIST_NAMES_STATUS);
		}

		this.germplasmList.setParent(selectedList);
		return this.germplasmList;
	}

	protected boolean validateAllFields() {
		return this.listDetailsComponent.validate();
	}

	public BreedingManagerListDetailsComponent getDetailsComponent() {
		return this.listDetailsComponent;
	}

	public SaveListAsDialogSource getSource() {
		return this.source;
	}

	public void setGermplasmList(final GermplasmList germplasmList) {
		this.germplasmList = germplasmList;
	}

	public BreedingManagerListDetailsComponent getListDetailsComponent() {
		return this.listDetailsComponent;
	}

	public LocalListFoldersTreeComponent getGermplasmListTree() {
		return this.germplasmListTree;
	}

	public void saveListChangesAction() {
		if (this.source instanceof ListBuilderComponent) {
			((ListBuilderComponent) this.source).saveListAction();
		}
	}

	protected boolean isShowFoldersOnlyInListTree() {
		return this.showFoldersOnlyInListTree;
	}

	protected void setShowFoldersOnlyInListTree(final boolean showFoldersOnlyInListTree) {
		this.showFoldersOnlyInListTree = showFoldersOnlyInListTree;
	}

	public GermplasmList getOriginalGermplasmList() {
		return this.originalGermplasmList;
	}

	public void setOriginalGermplasmList(final GermplasmList originalGermplasmList) {
		this.originalGermplasmList = originalGermplasmList;
	}

	private void doSaveAction(final ClickEvent event) {
		// Call method so that the variables will be updated, values will be used for the logic below
		this.germplasmList = this.getGermplasmListToSave();

		if (this.isListDateValid(this.listDetailsComponent.getListDateField())) {
			// If target list is locked
			if (this.isSelectedListLocked()) {
				MessageNotifier.showError(this.getWindow().getParent().getWindow(), this.messageSource.getMessage(Message.ERROR),
						this.messageSource.getMessage(Message.UNABLE_TO_EDIT_LOCKED_LIST));

				// If target list to be overwritten is not itself and is an existing list
			} else if (this.isSelectedListAnExistingListButNotItself()) {

				final GermplasmList gl = this.getGermplasmListToSave();
				this.setGermplasmListDetails(gl);

				ConfirmDialog.show(this.getWindow().getParent().getWindow(),
						this.messageSource.getMessage(Message.DO_YOU_WANT_TO_OVERWRITE_THIS_LIST) + "?",
						this.messageSource.getMessage(
								Message.LIST_DATA_WILL_BE_DELETED_AND_WILL_BE_REPLACED_WITH_THE_DATA_FROM_THE_LIST_THAT_YOU_JUST_CREATED),
						this.messageSource.getMessage(Message.OK), this.messageSource.getMessage(Message.CANCEL),
						new ConfirmDialog.Listener() {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClose(final ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									SaveListAsDialog.this.source.saveList(gl);
									SaveListAsDialog.this.saveListChangesAction();
									final Window window = event.getButton().getWindow();
									window.getParent().removeWindow(window);
								}
							}
						});

				// If target list to be overwritten is itself
			} else {
				if (this.validateAllFields()) {

					final GermplasmList gl = this.getGermplasmListToSave();
					this.setGermplasmListDetails(gl);

					this.source.saveList(gl);
					this.saveListChangesAction();

					final Window window = event.getButton().getWindow();
					window.getParent().removeWindow(window);
				}
			}

		}
	}

	private void setGermplasmListDetails(final GermplasmList gl) {
		gl.setName(this.listDetailsComponent.getListNameField().getValue().toString());
		gl.setDescription(this.listDetailsComponent.getListDescriptionField().getValue().toString());
		gl.setType(this.listDetailsComponent.getListTypeField().getValue().toString());
		gl.setDate(this.getCurrentParsedListDate(this.listDetailsComponent.getListDateField().getValue().toString()));
		gl.setNotes(this.listDetailsComponent.getListNotesField().getValue().toString());
	}

	protected boolean isSelectedListAnExistingListButNotItself() {
		return this.isSelectedListAnExistingList() || this.isSelectedListNotSameWithTheOriginalList();
	}

	protected boolean isSelectedListNotSameWithTheOriginalList() {
		return this.germplasmList.getId() != null && this.originalGermplasmList != null
				&& this.germplasmList.getId() != this.originalGermplasmList.getId();
	}

	protected boolean isSelectedListAnExistingList() {
		return this.germplasmList.getType() != null && !SaveListAsDialog.FOLDER_TYPE.equalsIgnoreCase(this.germplasmList.getType())
				&& this.germplasmList.getId() != null && this.originalGermplasmList == null;
	}

	protected boolean isSelectedListLocked() {
		return this.germplasmList != null && this.germplasmList.getStatus() >= 100;
	}

	private boolean isListDateValid(final ListDateField listDateField) {

		try {
			listDateField.validate();
		} catch (final InvalidValueException e) {
			SaveListAsDialog.LOG.error(e.getMessage(), e);
			MessageNotifier.showRequiredFieldError(this.getWindow().getParent().getWindow(), e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Parse the date value return from a DateField object to this format yyyymmdd
	 *
	 * @param listDate string with format: E MMM dd HH:mm:ss Z yyyy If doesn't follow the format, will return the current date
	 * @return
	 */
	protected Long getCurrentParsedListDate(final String listDate) {
		Date date;
		try {
			final SimpleDateFormat sdf = (SimpleDateFormat) DateUtil.getSimpleDateFormat("E MMM dd HH:mm:ss Z yyyy").clone();
			sdf.setLenient(true);
			date = sdf.parse(listDate);
		} catch (final ParseException e) {
			date = new Date();
			SaveListAsDialog.LOG.error(e.getMessage(), e);
		}
		final String dateAsString = DateUtil.formatDateAsStringValue(date, DateUtil.DATE_AS_NUMBER_FORMAT);
		return Long.parseLong(dateAsString);
	}

	public void setListDetailsComponent(final BreedingManagerListDetailsComponent listDetailsComponent) {
		this.listDetailsComponent = listDetailsComponent;
	}

	public void setGermplasmListTree(final LocalListFoldersTreeComponent germplasmListTree) {
		this.germplasmListTree = germplasmListTree;
	}
}
