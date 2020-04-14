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
public class UpdatePrefixCacheDialog extends BaseSubWindow
	implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final String PREFIXES_TABLE_DATA = "Prefixes Table Data";
	private static final String PREFIXES_PROPERTY_ID = "PREFIX";
	private static final String REMOVE_PROPERTY_ID = "REMOVE";
	private static final String SEQUENCE_NUMBER_REGEX = ")\\s?(\\d+).*";

	private VerticalLayout updatePrefixLayout;
	private Label specifyPrefixLabel;
	private TextField prefixTextField;
	private Button addPrefixButton;
	private Button updatePrefixesButton;
	private Button cancelButton;

	private List<Integer> deletedGIDs;
	private Table prefixesTable;
	private ListManagerMain source;


	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public UpdatePrefixCacheDialog(final List<Integer> deletedGIDs, final  ListManagerMain source) {
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

		this.updatePrefixesButton = new Button(this.messageSource.getMessage(Message.UPDATE));
		this.updatePrefixesButton.setDebugId("updatePrefixesButton");
		this.updatePrefixesButton.setWidth("80px");
		this.updatePrefixesButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.updatePrefixesButton.setEnabled(false);

		this.instantiateTable();
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());
		this.addPrefixButton.addListener(new AddPrefixButtonListener(this));
		this.updatePrefixesButton.addListener(new UpdatePrefixButtonListener(this));
	}

	@Override
	public void layoutComponents() {
		// window formatting
		this.setCaption(this.messageSource.getMessage(Message.UPDATE_PREFIX).toUpperCase());
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
		buttonLayout.addComponent(this.updatePrefixesButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.updatePrefixesButton, Alignment.BOTTOM_LEFT);

		this.updatePrefixLayout = new VerticalLayout();
		this.updatePrefixLayout.setDebugId("updatePrefixLayout");
		this.updatePrefixLayout.setSpacing(true);
		this.updatePrefixLayout.addComponent(new OneLineLayout(this.specifyPrefixLabel, this.prefixTextField, this.addPrefixButton));
		this.updatePrefixLayout.addComponent(this.prefixesTable);
		this.updatePrefixLayout.addComponent(buttonLayout);

		this.addComponent(this.updatePrefixLayout);

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

	void instantiateTable() {
		this.prefixesTable = new ControllableRefreshTable();
		this.prefixesTable.setWidth("100%");
		this.prefixesTable.setHeight("335px");

		this.prefixesTable.setData(UpdatePrefixCacheDialog.PREFIXES_TABLE_DATA);
		this.prefixesTable.addContainerProperty(UpdatePrefixCacheDialog.PREFIXES_PROPERTY_ID, String.class, null);
		this.prefixesTable.addContainerProperty(UpdatePrefixCacheDialog.REMOVE_PROPERTY_ID, Button.class, null);

		this.prefixesTable.setColumnHeader(UpdatePrefixCacheDialog.PREFIXES_PROPERTY_ID, UpdatePrefixCacheDialog.PREFIXES_PROPERTY_ID);
		this.prefixesTable.setColumnHeader(UpdatePrefixCacheDialog.REMOVE_PROPERTY_ID, UpdatePrefixCacheDialog.REMOVE_PROPERTY_ID);
		this.prefixesTable.setColumnWidth(UpdatePrefixCacheDialog.PREFIXES_PROPERTY_ID, 350);
		this.prefixesTable.setColumnAlignment(UpdatePrefixCacheDialog.PREFIXES_PROPERTY_ID, Table.ALIGN_CENTER);
		this.prefixesTable.setColumnAlignment(UpdatePrefixCacheDialog.REMOVE_PROPERTY_ID, Table.ALIGN_CENTER);
	}

	void addPrefix() {
		final String prefix = this.prefixTextField.getValue().toString().trim();
		if(!StringUtil.isEmpty(prefix) && !this.prefixesTable.getVisibleItemIds().contains(prefix.toUpperCase())) {
			final Item newItem = this.prefixesTable.getContainerDataSource().addItem(prefix.toUpperCase());
			newItem.getItemProperty(UpdatePrefixCacheDialog.PREFIXES_PROPERTY_ID).setValue(prefix);

			final Button removeButton = new Button("REMOVE");
			removeButton.addListener(new RemovePrefixButtonListener(this));
			removeButton.setData(prefix.toUpperCase() );
			newItem.getItemProperty(UpdatePrefixCacheDialog.REMOVE_PROPERTY_ID).setValue(removeButton);
			this.prefixesTable.requestRepaint();
			this.updatePrefixesButton.setEnabled(true);
		}
		this.prefixTextField.setValue("");
	}

	void deleteFromTable(final String itemId) {
		this.prefixesTable.getContainerDataSource().removeItem(itemId);
		this.prefixesTable.requestRepaint();
		if(CollectionUtils.isEmpty(this.prefixesTable.getVisibleItemIds())) {
			this.updatePrefixesButton.setEnabled(false);
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
				this.updateSequences(names, keySequenceRegistersOfDeletedGermplasm);
			}
		} else {
			MessageNotifier
				.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX));
		}
		this.cancelButton.click();
	}

	void updateSequences(final List<String> names, final List<KeySequenceRegister> keySequenceRegistersOfDeletedGermplasm) {
		final Set<KeySequenceRegister> prefixesToBeUpdated = new HashSet<>();

		for(final KeySequenceRegister keySequenceRegister: keySequenceRegistersOfDeletedGermplasm) {
			final String prefix = keySequenceRegister.getKeyPrefix().trim().toUpperCase();
			for (String name : names) {
				name = name.trim().toUpperCase();
				final Pattern namePattern = Pattern.compile("^(" + prefix + UpdatePrefixCacheDialog.SEQUENCE_NUMBER_REGEX);
				final Matcher nameMatcher  = namePattern.matcher(name);
				if(nameMatcher.find()) {
					prefixesToBeUpdated.add(keySequenceRegister);
					continue;
				}
			}
		}

		if (this.prefixesTable.getVisibleItemIds().size() == prefixesToBeUpdated.size()) {
			MessageNotifier
				.showMessage(this.source.getWindow(), this.messageSource.getMessage(Message.SUCCESS),
					this.messageSource.getMessage(Message.SUCCESS_PREFIX_UPDATE));

		} else if(prefixesToBeUpdated.isEmpty()) {
			MessageNotifier
				.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR),
					this.messageSource.getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX));
		} else {
			final int noOfNonExistingPrefixes = this.prefixesTable.getVisibleItemIds().size() - prefixesToBeUpdated
				.size();
			MessageNotifier
				.showWarning(this.source.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.WARNING_PREFIX_UPDATE,
						String.valueOf(prefixesToBeUpdated.size()), Integer.toString(noOfNonExistingPrefixes)));

		}

		if(!prefixesToBeUpdated.isEmpty()) {
			this.germplasmDataManager.updateKeySequenceRegister(new ArrayList<>(prefixesToBeUpdated));
		}
	}

	static class AddPrefixButtonListener implements Button.ClickListener {

		UpdatePrefixCacheDialog updatePrefixCacheDialog;

		AddPrefixButtonListener(final UpdatePrefixCacheDialog updatePrefixCacheDialog) {
			this.updatePrefixCacheDialog = updatePrefixCacheDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			this.updatePrefixCacheDialog.addPrefix();
		}
	}

	static class RemovePrefixButtonListener implements Button.ClickListener {

		UpdatePrefixCacheDialog updatePrefixCacheDialog;

		RemovePrefixButtonListener(final UpdatePrefixCacheDialog updatePrefixCacheDialog) {
			this.updatePrefixCacheDialog = updatePrefixCacheDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			this.updatePrefixCacheDialog.deleteFromTable(event.getButton().getData().toString());
		}
	}

	static class UpdatePrefixButtonListener implements Button.ClickListener {

		UpdatePrefixCacheDialog updatePrefixCacheDialog;

		UpdatePrefixButtonListener(final UpdatePrefixCacheDialog updatePrefixCacheDialog) {
			this.updatePrefixCacheDialog = updatePrefixCacheDialog;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void buttonClick(final Button.ClickEvent event) {
			this.updatePrefixCacheDialog.deletePrefixes();
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
