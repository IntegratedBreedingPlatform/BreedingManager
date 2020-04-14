package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.data.Item;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.KeySequenceRegister;
import org.generationcp.middleware.util.StringUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configurable
public class DeletePrefixCacheDialog extends BaseSubWindow
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final String PREFIXES_TABLE_DATA = "Prefixes Table Data";
	private static final String PREFIXES_PROPERTY_ID = "PREFIX";
	private static final String REMOVE_PROPERTY_ID = "REMOVE";
	private static final String SEQUENCE_NUMBER_REGEX = ")\\s?(\\d+).*";

	private Label specifyPrefixLabel;
	private Label deletePrefixLabel;
	private TextField prefixTextField;
	private Button addPrefixButton;
	private Button deletePrefixesButton;
	private Button cancelButton;

	private List<Integer> deletedGIDs;
	private Table prefixesTable;
	private ListManagerMain source;


	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public DeletePrefixCacheDialog(final List<Integer> deletedGIDs, final  ListManagerMain source) {
		super();
		this.deletedGIDs = deletedGIDs;
		this.source = source;
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
		this.specifyPrefixLabel = new Label(this.messageSource.getMessage(Message.SPECIFY_PREFIX));
		this.specifyPrefixLabel.setDebugId("specifyPrefixLabel");
		this.specifyPrefixLabel.setWidth("180px");

		this.deletePrefixLabel = new Label(this.messageSource.getMessage(Message.DELETE_PREFIX_LABEL));
		this.deletePrefixLabel.setDebugId("deletePrefixLabel");
		this.deletePrefixLabel.setWidth("100%");
		this.deletePrefixLabel.setStyleName("italic");

		this.prefixTextField = new TextField();
		this.prefixTextField.setDebugId("prefixTextField");
		this.prefixTextField.setWidth("170px");
		this.prefixTextField.setRequired(false);

		this.addPrefixButton = new Button(this.messageSource.getMessage(Message.ADD));
		this.addPrefixButton.setDebugId("addPrefixButton");
		this.addPrefixButton.setWidth("60px");
		this.addPrefixButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setWidth("80px");

		this.deletePrefixesButton = new Button(this.messageSource.getMessage(Message.DELETE));
		this.deletePrefixesButton.setDebugId("deletePrefixesButton");
		this.deletePrefixesButton.setWidth("80px");
		this.deletePrefixesButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.deletePrefixesButton.setEnabled(false);

		this.instantiateTable();
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());
		this.addPrefixButton.addListener(new AddPrefixButtonListener(this));
		this.deletePrefixesButton.addListener(new DeletePrefixButtonListener(this));
	}

	@Override
	public void layoutComponents() {
		// window formatting
		this.setCaption(this.messageSource.getMessage(Message.DELETE_PREFIX).toUpperCase());
		this.center();
		this.addStyleName(Bootstrap.WINDOW.CONFIRM.styleName());
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("500px");
		this.setWidth("500px");

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setHeight("50px");
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.deletePrefixesButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.deletePrefixesButton, Alignment.BOTTOM_LEFT);

		final VerticalLayout deletePrefixLayout = new VerticalLayout();
		deletePrefixLayout.setDebugId("deletePrefixLayout");
		deletePrefixLayout.setSpacing(true);
		deletePrefixLayout.addComponent(this.deletePrefixLabel);
		deletePrefixLayout.addComponent(new OneLineLayout(this.specifyPrefixLabel, this.prefixTextField, this.addPrefixButton));
		deletePrefixLayout.addComponent(this.prefixesTable);
		deletePrefixLayout.addComponent(buttonLayout);

		this.addComponent(deletePrefixLayout);

	}

	@Override
	public void updateLabels() {

	}

	private class OneLineLayout extends HorizontalLayout {

		private static final long serialVersionUID = 1L;

		OneLineLayout(final AbstractComponent... components) {
			this.setSpacing(true);
			for (final AbstractComponent component : components) {
				this.addComponent(component);
			}
		}
	}

	private void instantiateTable() {
		this.prefixesTable = new ControllableRefreshTable();
		this.prefixesTable.setWidth("100%");
		this.prefixesTable.setHeight("300px");

		this.prefixesTable.setData(DeletePrefixCacheDialog.PREFIXES_TABLE_DATA);
		this.prefixesTable.addContainerProperty(DeletePrefixCacheDialog.PREFIXES_PROPERTY_ID, String.class, null);
		this.prefixesTable.addContainerProperty(DeletePrefixCacheDialog.REMOVE_PROPERTY_ID, Button.class, null);

		this.prefixesTable.setColumnHeader(DeletePrefixCacheDialog.PREFIXES_PROPERTY_ID, DeletePrefixCacheDialog.PREFIXES_PROPERTY_ID);
		this.prefixesTable.setColumnHeader(DeletePrefixCacheDialog.REMOVE_PROPERTY_ID, DeletePrefixCacheDialog.REMOVE_PROPERTY_ID);
		this.prefixesTable.setColumnWidth(DeletePrefixCacheDialog.PREFIXES_PROPERTY_ID, 350);
		this.prefixesTable.setColumnAlignment(DeletePrefixCacheDialog.PREFIXES_PROPERTY_ID, Table.ALIGN_CENTER);
		this.prefixesTable.setColumnAlignment(DeletePrefixCacheDialog.REMOVE_PROPERTY_ID, Table.ALIGN_CENTER);
	}

	private void addPrefix() {
		final String prefix = this.prefixTextField.getValue().toString().trim();
		if(!StringUtil.isEmpty(prefix) && !this.prefixesTable.getVisibleItemIds().contains(prefix.toUpperCase())) {
			final Item newItem = this.prefixesTable.getContainerDataSource().addItem(prefix.toUpperCase());
			newItem.getItemProperty(DeletePrefixCacheDialog.PREFIXES_PROPERTY_ID).setValue(prefix);

			final Button removeButton = new Button("REMOVE");
			removeButton.addListener(new RemovePrefixButtonListener(this));
			removeButton.setData(prefix.toUpperCase() );
			newItem.getItemProperty(DeletePrefixCacheDialog.REMOVE_PROPERTY_ID).setValue(removeButton);
			this.prefixesTable.requestRepaint();
			this.deletePrefixesButton.setEnabled(true);
		}
		this.prefixTextField.setValue("");
	}

	private void deleteFromTable(final String itemId) {
		this.prefixesTable.getContainerDataSource().removeItem(itemId);
		this.prefixesTable.requestRepaint();
		if(CollectionUtils.isEmpty(this.prefixesTable.getVisibleItemIds())) {
			this.deletePrefixesButton.setEnabled(false);
		}
	}

	void deletePrefixes() {
		final List<String> prefixes = new ArrayList<>((Collection<? extends String>) this.prefixesTable.getVisibleItemIds());
		final List<String> names = this.germplasmDataManager.getNamesByGidsAndPrefixes(this.deletedGIDs, prefixes);
		if(!CollectionUtils.isEmpty(names)) {
			final List<KeySequenceRegister> keySequenceRegistersOfDeletedGermplasm = this.germplasmDataManager.getKeySequenceRegistersByPrefixes(prefixes);
			if(CollectionUtils.isEmpty(keySequenceRegistersOfDeletedGermplasm)) {
				MessageNotifier
					.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
						this.messageSource.getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX));
			} else {
				this.deleteKeyRegisters(names, keySequenceRegistersOfDeletedGermplasm);
			}
		} else {
			MessageNotifier
				.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX));
		}
		this.cancelButton.click();
	}

	void deleteKeyRegisters(final List<String> names, final List<KeySequenceRegister> keySequenceRegistersOfDeletedGermplasm) {
		final Set<String> prefixesToBeDeleted = new HashSet<>();

		for(final KeySequenceRegister keySequenceRegister: keySequenceRegistersOfDeletedGermplasm) {
			final String prefix = keySequenceRegister.getKeyPrefix().trim().toUpperCase();
			for (String name : names) {
				name = name.trim().toUpperCase();
				final Pattern namePattern = Pattern.compile("^(" + prefix + DeletePrefixCacheDialog.SEQUENCE_NUMBER_REGEX);
				final Matcher nameMatcher  = namePattern.matcher(name);
				if(nameMatcher.find()) {
					prefixesToBeDeleted.add(keySequenceRegister.getKeyPrefix());
					break;
				}
			}
		}

		if (this.prefixesTable.getVisibleItemIds().size() == prefixesToBeDeleted.size()) {
			MessageNotifier
				.showMessage(this.source.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(Message.SUCCESS_PREFIX_DELETE));

		} else if(prefixesToBeDeleted.isEmpty()) {
			MessageNotifier
				.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX));
		} else {
			final int noOfNonExistingPrefixes = this.prefixesTable.getVisibleItemIds().size() - prefixesToBeDeleted
				.size();
			MessageNotifier
				.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.WARNING_PREFIX_DELETE,
						String.valueOf(prefixesToBeDeleted.size()), Integer.toString(noOfNonExistingPrefixes)));

		}

		if(!prefixesToBeDeleted.isEmpty()) {
			this.germplasmDataManager.deleteKeySequenceRegistersByKeyPrefixes(new ArrayList<>(prefixesToBeDeleted));
		}
	}

	static class AddPrefixButtonListener implements Button.ClickListener {

		DeletePrefixCacheDialog deletePrefixCacheDialog;

		AddPrefixButtonListener(final DeletePrefixCacheDialog deletePrefixCacheDialog) {
			this.deletePrefixCacheDialog = deletePrefixCacheDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			this.deletePrefixCacheDialog.addPrefix();
		}
	}

	static class RemovePrefixButtonListener implements Button.ClickListener {

		DeletePrefixCacheDialog deletePrefixCacheDialog;

		RemovePrefixButtonListener(final DeletePrefixCacheDialog deletePrefixCacheDialog) {
			this.deletePrefixCacheDialog = deletePrefixCacheDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			this.deletePrefixCacheDialog.deleteFromTable(event.getButton().getData().toString());
		}
	}

	static class DeletePrefixButtonListener implements Button.ClickListener {

		DeletePrefixCacheDialog deletePrefixCacheDialog;

		DeletePrefixButtonListener(final DeletePrefixCacheDialog deletePrefixCacheDialog) {
			this.deletePrefixCacheDialog = deletePrefixCacheDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			this.deletePrefixCacheDialog.deletePrefixes();
		}
	}

	void setPrefixesTable(final Table prefixesTable) {
		this.prefixesTable = prefixesTable;
	}

	void setDeletedGIDs(final List<Integer> deletedGIDs) {
		this.deletedGIDs = deletedGIDs;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setSource(final ListManagerMain source) {
		this.source = source;
	}

	public void setCancelButton(final Button cancelButton) {
		this.cancelButton = cancelButton;
	}
}
