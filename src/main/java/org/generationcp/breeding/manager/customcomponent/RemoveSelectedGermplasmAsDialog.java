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
public class RemoveSelectedGermplasmAsDialog extends BaseSubWindow
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(RemoveSelectedGermplasmAsDialog.class);

	private VerticalLayout mainRemoveGermplasmLayout;
	private Label titleRemoveGermplasmLabel;
	private Label warningTextRemoveGermplasmLabel;
	private Button acceptButton;
	private Button cancelButton;

	private final Component source;

	private final GermplasmList germplasmList;

	private final Table listDataTable;

	@Resource private SimpleResourceBundleMessageSource messageSource;

	@Resource private PlatformTransactionManager transactionManager;

	@Autowired private GermplasmDataManager germplasmDataManager;

	@Autowired private GermplasmListManager germplasmListManager;

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

		this.cancelButton = new Button(this.getMessageSource().getMessage(Message.NO));
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setWidth("80px");

		this.acceptButton = new Button(this.getMessageSource().getMessage(Message.YES));
		this.acceptButton.setDebugId("acceptButton");
		this.acceptButton.setWidth("80px");
		this.acceptButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());

		this.acceptButton.addListener(new AcceptButtonListener(this));
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
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.acceptButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.acceptButton, Alignment.BOTTOM_LEFT);

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

	public Table getListDataTable() {
		return listDataTable;
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

	static class AcceptButtonListener implements Button.ClickListener {

		RemoveSelectedGermplasmAsDialog removeSelectedGermplasmAsDialog;

		AcceptButtonListener(final RemoveSelectedGermplasmAsDialog removeSelectedGermplasmAsDialog) {
			this.removeSelectedGermplasmAsDialog = removeSelectedGermplasmAsDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			this.removeSelectedGermplasmAsDialog.deleteGermplasmsAction(this.removeSelectedGermplasmAsDialog.getListDataTable());
			this.removeSelectedGermplasmAsDialog.getParent().removeWindow(this.removeSelectedGermplasmAsDialog);
		}
	}

	protected void deleteGermplasmsAction(final Table table) {

		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {

				final List<Integer> selectedDeleteGids = new ArrayList<>();
				final Collection<?> selectedIdsToDelete =
					(Collection<?>) RemoveSelectedGermplasmAsDialog.this.getListDataTable().getValue();

				for (final Object itemId : selectedIdsToDelete) {
					final Button desigButton = (Button) RemoveSelectedGermplasmAsDialog.this.getListDataTable().getItem(itemId)
						.getItemProperty(ColumnLabels.GID.getName()).getValue();
					final String gid = desigButton.getCaption();
					selectedDeleteGids.add(Integer.valueOf(gid));

				}

				final String message;
				List<Integer> deletedGids =
					RemoveSelectedGermplasmAsDialog.this.getGermplasmDataManager().deleteGermplasms(selectedDeleteGids);
				RemoveSelectedGermplasmAsDialog.this.getGermplasmListManager()
					.performListEntriesDeletion(deletedGids, RemoveSelectedGermplasmAsDialog.this.getGermplasmList().getId());

				if (selectedDeleteGids.size() == deletedGids.size()) {
					message = deletedGids.size() + " germplasm were deteled successfully!";
					MessageNotifier.showMessage(RemoveSelectedGermplasmAsDialog.this.getParent().getWindow(), null, message);
				} else if (deletedGids.size() != 0) {
					final Integer gidsNotDeleted = selectedDeleteGids.size() - deletedGids.size();
					message = deletedGids.size() + " germplasm were deteled successfully and " + gidsNotDeleted
						+ " can not be deleted due to internal validations (has inventory, it is used in other lists or it is fixed). For further information please contact support.";
					MessageNotifier.showWarning(RemoveSelectedGermplasmAsDialog.this.getParent().getWindow(), "Warning!", message);
				} else {
					message = selectedDeleteGids.size()
						+ " germplasm can not be deleted due to internal validations (has inventory, it is used in other lists or it is fixed). For further information please contact support.";
					MessageNotifier.showError(RemoveSelectedGermplasmAsDialog.this.getParent().getWindow(),
						RemoveSelectedGermplasmAsDialog.this.getMessageSource().getMessage(Message.REMOVE_SELECTED_GERMPLASM), message);
				}
			}
		});
	}
}

