
package org.generationcp.breeding.manager.customcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class RemoveSelectedGermplasmAsDialog extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private VerticalLayout mainRemoveGermplasmLayout;
	private Label titleRemoveGermplasmLabel;
	private Label warningTextRemoveGermplasmLabel;
	private Button acceptButton;
	private Button cancelButton;

	private final ListManagerMain source;

	private final GermplasmList germplasmList;

	private Table listDataTable;

	private final Label totalListEntriesLabel;

	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private PlatformTransactionManager transactionManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public RemoveSelectedGermplasmAsDialog(final ListManagerMain source, final GermplasmList germplasmList, final Table listDataTable,
			final Label totalListEntriesLabel) {
		this.source = source;
		this.germplasmList = germplasmList;
		this.listDataTable = listDataTable;
		this.totalListEntriesLabel = totalListEntriesLabel;
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

		this.titleRemoveGermplasmLabel = new Label(this.getMessageSource().getMessage(Message.REMOVE_SELECTED_GERMPLASM).toUpperCase());
		this.titleRemoveGermplasmLabel.setDebugId("titleRemoveGermplasmLabel");
		this.titleRemoveGermplasmLabel.setStyleName(Bootstrap.Typography.H2.styleName());

		this.warningTextRemoveGermplasmLabel = new Label(this.getMessageSource().getMessage(Message.REMOVE_SELECTED_GERMPLASM_CONFIRM));
		this.warningTextRemoveGermplasmLabel.setDebugId("warningTextRemoveGermplasmLabel");

		this.setCancelButton(new Button(this.getMessageSource().getMessage(Message.NO)));
		this.getCancelButton().setDebugId("cancelButton");
		this.getCancelButton().setWidth("80px");

		this.setAcceptButton(new Button(this.getMessageSource().getMessage(Message.YES)));
		this.getAcceptButton().setDebugId("acceptButton");
		this.getAcceptButton().setWidth("80px");
		this.getAcceptButton().addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		this.getCancelButton().addListener(new CloseWindowAction());

		this.getAcceptButton().addListener(new AcceptButtonListener(this));
	}

	@Override
	public void layoutComponents() {
		// window formatting
		this.setCaption(this.getMessageSource().getMessage(Message.REMOVE_SELECTED_GERMPLASM));
		this.center();
		this.addStyleName(Bootstrap.WINDOW.CONFIRM.styleName());
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("175px");
		this.setWidth("400px");

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setHeight("50px");
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.getCancelButton());
		buttonLayout.addComponent(this.getAcceptButton());
		buttonLayout.setComponentAlignment(this.getCancelButton(), Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.getAcceptButton(), Alignment.BOTTOM_LEFT);

		this.mainRemoveGermplasmLayout = new VerticalLayout();
		this.mainRemoveGermplasmLayout.setDebugId("mainRemoveGermplasmLayout");
		this.mainRemoveGermplasmLayout.setSpacing(true);
		this.mainRemoveGermplasmLayout.addComponent(this.warningTextRemoveGermplasmLabel);
		this.mainRemoveGermplasmLayout.addComponent(buttonLayout);

		this.addComponent(this.mainRemoveGermplasmLayout);
	}

	@Override
	public void updateLabels() {

	}

	public Component getSource() {
		return this.source;
	}

	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public Table getListDataTable() {
		return this.listDataTable;
	}

	public void setListDataTable(final Table listDataTable) {
		this.listDataTable = listDataTable;
	}

	public GermplasmListManager getGermplasmListManager() {
		return this.germplasmListManager;
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public Button getAcceptButton() {
		return this.acceptButton;
	}

	public void setAcceptButton(final Button acceptButton) {
		this.acceptButton = acceptButton;
	}

	public Button getCancelButton() {
		return this.cancelButton;
	}

	public void setCancelButton(final Button cancelButton) {
		this.cancelButton = cancelButton;
	}

	static class AcceptButtonListener implements Button.ClickListener {

		RemoveSelectedGermplasmAsDialog removeSelectedGermplasmAsDialog;

		AcceptButtonListener(final RemoveSelectedGermplasmAsDialog removeSelectedGermplasmAsDialog) {
			this.removeSelectedGermplasmAsDialog = removeSelectedGermplasmAsDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			final Collection<? extends Integer> selectedIdsToDelete = (Collection<? extends Integer>) this.removeSelectedGermplasmAsDialog.listDataTable.getValue();
			this.removeSelectedGermplasmAsDialog.setDebugId("removeSelectedGermplasmAsDialog");
			this.removeSelectedGermplasmAsDialog.deleteGermplasmsAction(selectedIdsToDelete);
			this.removeSelectedGermplasmAsDialog.getParent().removeWindow(this.removeSelectedGermplasmAsDialog);
		}
	}

	protected void deleteGermplasmsAction(final  Collection<? extends Integer> selectedIdsToDelete) {
		final TransactionTemplate transactionTemplate = new TransactionTemplate(RemoveSelectedGermplasmAsDialog.this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {

				final List<Integer> selectedDeleteGids = new ArrayList<>();
				for (final Integer itemId : selectedIdsToDelete) {
					final Integer gid = getGidByDataTable(itemId);
					selectedDeleteGids.add(gid);

				}

				List<Integer> deletedGids = RemoveSelectedGermplasmAsDialog.this.deleteGermplasmsByGids(selectedDeleteGids);

				final List<Integer> selectedIdsNotDeleted = new ArrayList<>();
				for (final Integer itemId : selectedIdsToDelete) {
					final Integer gid = getGidByDataTable(itemId);
					if(deletedGids.isEmpty() || !deletedGids.contains(Integer.valueOf(gid))){
						selectedIdsNotDeleted.add(itemId);
					}
				}

				RemoveSelectedGermplasmAsDialog.this.refreshTable(selectedIdsNotDeleted);
			}

			Integer getGidByDataTable(Integer itemId) {
				final Button desigButton = (Button) RemoveSelectedGermplasmAsDialog.this.getListDataTable().getItem(itemId)
						.getItemProperty(ColumnLabels.GID.getName()).getValue();
				final String gid = desigButton.getCaption();
				return Integer.valueOf(gid);
			}
		});
	}

	protected List<Integer> deleteGermplasmsByGids(final List<Integer> selectedDeleteGids) {
		final List<Integer> deletedGids = this.getGermplasmListManager().deleteGermplasms(selectedDeleteGids, this.getGermplasmList().getId());
		final String totalDeletedGids = String.valueOf(deletedGids.size());
		final String totalSelectedDeleteGids = String.valueOf(selectedDeleteGids.size());

		if (selectedDeleteGids.size() == deletedGids.size()) {
			MessageNotifier.showMessage(this.source.getWindow(),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.SUCCESS),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.SUCCESS_DELETED_GERMPLASM,
							totalDeletedGids));

		} else if (deletedGids.size() != 0) {
			final Integer gselectedIdsNotDeleted = selectedDeleteGids.size() - deletedGids.size();
			MessageNotifier.showWarning(this.source.getWindow(),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.WARNING),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.WARNING_DELETED_GERMPLASM, totalDeletedGids,
							gselectedIdsNotDeleted.toString()));

		} else {
			MessageNotifier.showError(this.source.getWindow(),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.ERROR), RemoveSelectedGermplasmAsDialog.this
							.getMessageSource().getMessage(Message.GERMPLASM_COULD_NOT_BE_DELETED, totalSelectedDeleteGids));
		}

		return deletedGids;
	}

	@SuppressWarnings("unchecked")
	private void refreshTable(List<Integer> selectedIdsNotDeleted) {
		final Collection<? extends Integer> selectedIdsToDelete = (Collection<? extends Integer>) this.getListDataTable().getValue();

		if (this.listDataTable.getItemIds().size() == selectedIdsNotDeleted.size()) {
			this.getListDataTable().getContainerDataSource().removeAllItems();
		} else {
			for (final Integer selectedItemId : selectedIdsToDelete) {
				if (!selectedIdsNotDeleted.contains(selectedItemId)) {
					this.getListDataTable().getContainerDataSource().removeItem(selectedItemId);
				}
			}
		}

		this.assignSerializedEntryNumber();
		this.listDataTable.focus();
		this.getListDataTable().setValue(null);
		this.updateNoOfEntries();
		this.source.getListSelectionComponent().getListTreeComponent().refreshComponent();
	}

	/**
	 * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
	 */
	private void assignSerializedEntryNumber() {
		final List<Integer> itemIds = this.getItemIds(this.listDataTable);

		int id = 1;
		for (final Integer itemId : itemIds) {
			this.listDataTable.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(id);
			id++;
		}
	}

	/**
	 * Get item id's of a table, and return it as a list
	 *
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(final Table table) {
		final List<Integer> itemIds = new ArrayList<>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());

		return itemIds;
	}

	public void updateNoOfEntries() {
		int count = 0;
		count = this.listDataTable.getItemIds().size();
		this.updateNoOfEntries(count);
	}

	private void updateNoOfEntries(final long count) {
		final String countLabel = "  <b>" + count + "</b>";
		this.totalListEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + countLabel);
	}
}
