package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configurable
public class RemoveSelectedGermplasmAsDialog extends BaseSubWindow 	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(RemoveSelectedGermplasmAsDialog.class);

	private VerticalLayout mainRemoveGermplasmLayout;
	private Label titleRemoveGermplasmLabel;
	private Label warningTextRemoveGermplasmLabel;
	private Button acceptButton;
	private Button cancelButton;

	private final Component source;

	private final GermplasmList germplasmList;

	private Table listDataTable;

	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private PlatformTransactionManager transactionManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;


	public RemoveSelectedGermplasmAsDialog(final Component source, final GermplasmList germplasmList, final Table listDataTable) {
		this.source = source;
		this.germplasmList = germplasmList;
		this.listDataTable = listDataTable;
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
		this.mainRemoveGermplasmLayout.addComponent(warningTextRemoveGermplasmLabel);
		this.mainRemoveGermplasmLayout.addComponent(buttonLayout);

		this.addComponent(this.mainRemoveGermplasmLayout);
	}

	@Override
	public void updateLabels() {

	}

	public Component getSource() {
		return source;
	}

	public GermplasmList getGermplasmList() {
		return germplasmList;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
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

	public GermplasmDataManager getGermplasmDataManager() {
		return germplasmDataManager;
	}

	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public GermplasmListManager getGermplasmListManager() {
		return germplasmListManager;
	}

	public void setGermplasmListManager(GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

	public Button getAcceptButton() {
		return acceptButton;
	}

	public void setAcceptButton(Button acceptButton) {
		this.acceptButton = acceptButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(Button cancelButton) {
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
			final Collection<?> selectedIdsToDelete = (Collection<?>) this.removeSelectedGermplasmAsDialog.listDataTable.getValue();
			this.removeSelectedGermplasmAsDialog.setDebugId("removeSelectedGermplasmAsDialog");
			this.removeSelectedGermplasmAsDialog.deleteGermplasmsAction(selectedIdsToDelete);
			this.removeSelectedGermplasmAsDialog.getParent().removeWindow(this.removeSelectedGermplasmAsDialog);

		}
	}

	protected void deleteGermplasmsAction(final Collection<?> selectedIdsToDelete) {
		final TransactionTemplate transactionTemplate = new TransactionTemplate(RemoveSelectedGermplasmAsDialog.this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {

				final List<Integer> selectedDeleteGids = new ArrayList<>();

				for (final Object itemId : selectedIdsToDelete) {
					final Button desigButton = (Button) RemoveSelectedGermplasmAsDialog.this.getListDataTable().getItem(itemId).getItemProperty(ColumnLabels.GID.getName()).getValue();
					final String gid = desigButton.getCaption();
					selectedDeleteGids.add(Integer.valueOf(gid));

				}

				RemoveSelectedGermplasmAsDialog.this.deleteGermplasmsByGids(selectedDeleteGids);
			}
		});
	}

	protected void deleteGermplasmsByGids(final List<Integer> selectedDeleteGids) {
		final List<Integer> deletedGids = getGermplasmDataManager().deleteGermplasms(selectedDeleteGids);
		this.getGermplasmListManager().performGermplasmListEntriesDeletion(deletedGids, this.getGermplasmList().getId());
		this.getGermplasmListManager().performListDataProjectEntriesDeletion(deletedGids, this.getGermplasmList().getId());
		final String totalDeletedGids = String.valueOf(deletedGids.size());
		final String totalSelectedDeleteGids = String.valueOf(selectedDeleteGids.size());

		if (selectedDeleteGids.size() == deletedGids.size()) {
			MessageNotifier.showMessage(this.source.getWindow(),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.SUCCESS),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.SUCCESS_DELETED_GERMPLASM, totalDeletedGids));
		} else if (deletedGids.size() != 0) {
			final Integer gidsNotDeleted = selectedDeleteGids.size() - deletedGids.size();
			MessageNotifier.showWarning(this.source.getWindow(),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.WARNING),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.WARNING_DELETED_GERMPLASM, totalDeletedGids, gidsNotDeleted.toString()));
		} else {
			MessageNotifier.showError(this.source.getWindow(),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.ERROR),
					RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.GERMPLASM_COULD_NOT_BE_DELETED, totalSelectedDeleteGids));
		}
	}
}

