package org.generationcp.breeding.manager.inventory;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.io.FilenameUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.UploadField;
import org.generationcp.breeding.manager.inventory.exception.SeedInventoryImportException;
import org.generationcp.breeding.manager.pojos.ImportedSeedInventoryList;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.InvalidFileDataException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Configurable
public class SeedInventoryImportFileComponent extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout  {

	private static final Logger LOG = LoggerFactory.getLogger(SeedInventoryImportFileComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private VerticalLayout mainLayout;

	private HorizontalLayout importSeedTitleLayout;
	private ComponentContainer parent;
	private Button cancelButton;
	private Button finishButton;

	private Label selectFileLabel;

	private UploadField uploadSeedPreparationComponent;
	private SeedInventoryListUploader seedInventoryListUploader;

	final GermplasmList selectedGermplsmList;
	final Component source;

	private final Set<String> extensionSet = new HashSet<>();

	private static final String ERROR_IMPORTING = "Error importing ";
	private static final String ERROR = "Error";

	public SeedInventoryImportFileComponent(final Component source,GermplasmList selectedGermplsmList){
		this.source = source;
		this.selectedGermplsmList = selectedGermplsmList;
	}


	@Override
	public void instantiateComponents() {

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("importListMainLayout");
		this.mainLayout.setSpacing(true);

		this.importSeedTitleLayout = new HorizontalLayout();
		this.importSeedTitleLayout.setDebugId("importSeedTitleLayout");
		this.importSeedTitleLayout.setSpacing(true);


		this.selectFileLabel = new Label(this.messageSource.getMessage(Message.SELECT_SEED_INVENTORY_FILE) + "&nbsp");
		this.selectFileLabel.setDebugId("selectFileLabel");
		this.selectFileLabel.setContentMode(Label.CONTENT_XHTML);

		this.uploadSeedPreparationComponent = new UploadField() {

			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFinished(final Upload.FinishedEvent event) {
				super.uploadFinished(event);
				SeedInventoryImportFileComponent.this.finishButton.setEnabled(true);
			}
		};
		this.uploadSeedPreparationComponent.discard();

		this.uploadSeedPreparationComponent.setButtonCaption(this.messageSource.getMessage(Message.UPLOAD));
		this.uploadSeedPreparationComponent.setNoFileSelectedText(this.messageSource.getMessage("NO_FILE_SELECTED"));
		this.uploadSeedPreparationComponent.setSelectedFileText(this.messageSource.getMessage("SELECTED_IMPORT_FILE"));
		this.uploadSeedPreparationComponent.setDeleteCaption(this.messageSource.getMessage("CLEAR"));
		this.uploadSeedPreparationComponent.setFieldType(UploadField.FieldType.FILE);
		this.uploadSeedPreparationComponent.setButtonCaption("Browse");

		this.uploadSeedPreparationComponent.getRootLayout().setWidth("100%");
		this.uploadSeedPreparationComponent.getRootLayout().setStyleName("bms-upload-container");

		this.seedInventoryListUploader = new SeedInventoryListUploader();

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");

		this.finishButton = new Button(this.messageSource.getMessage(Message.FINISH));
		this.finishButton.setDebugId("finishButton");
		this.finishButton.setEnabled(false);
		this.finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		this.extensionSet.add("xls");
		this.extensionSet.add("xlsx");
	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8787686200326172252L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				SeedInventoryImportFileComponent.this.cancelButtonAction();
			}

		});

		this.finishButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8787686200326172252L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				SeedInventoryImportFileComponent.this.finishButtonClickListener();
			}
		});


		this.uploadSeedPreparationComponent.setDeleteButtonListener(new Button.ClickListener() {

			private static final long serialVersionUID = -1357425494204377238L;

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				SeedInventoryImportFileComponent.this.finishButton.setEnabled(false);
			}
		});
		this.uploadSeedPreparationComponent.setFileFactory(this.seedInventoryListUploader);
	}

	@Override
	public void layoutComponents() {

		this.setCaption(this.messageSource.getMessage(Message.IMPORT_SEED_LIST));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("225px");
		this.setWidth("450px");


		final HorizontalLayout downloadMessageLayout = new HorizontalLayout();
		downloadMessageLayout.setDebugId("downloadMessageLayout");
		downloadMessageLayout.addComponent(this.selectFileLabel);
		downloadMessageLayout.addComponent(new Label(this.messageSource.getMessage(Message.PERIOD)));
		this.mainLayout.addComponent(downloadMessageLayout);

		this.mainLayout.addComponent(this.uploadSeedPreparationComponent);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("40px");
		buttonLayout.setSpacing(true);

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.finishButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.finishButton, Alignment.BOTTOM_LEFT);

		this.mainLayout.addComponent(buttonLayout);

		this.addComponent(mainLayout);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void updateLabels() {

	}

	protected void cancelButtonAction() {
		this.getParent().removeWindow(this);
	}

	public void finishButtonClickListener() {

		final String extension = FilenameUtils.getExtension(this.seedInventoryListUploader.getOriginalFilename()).toLowerCase();
		if (!this.extensionSet.contains(extension)) {
			MessageNotifier.showError(this.getWindow(), ERROR, this.messageSource.getMessage(Message.SEED_INVALID_FILE_EXTENSION_ERROR));
			return;
		}

		try {
			this.seedInventoryListUploader.doParseWorkbook();
			ImportedSeedInventoryList importedSeedInventoryList = this.seedInventoryListUploader.getImportedSeedInventoryList();
			//TODO BMS-3347 start validation of input file and importing inventory

		} catch (final SeedInventoryImportException e) {
			SeedInventoryImportFileComponent.LOG.debug(ERROR_IMPORTING + e.getMessage(), e);
			MessageNotifier.showError(this.getWindow(), e.getCaption(), e.getMessage());
		} catch (final FileParsingException e) {
			SeedInventoryImportFileComponent.LOG.debug(ERROR_IMPORTING + e.getMessage(), e);
			final String message = this.messageSource.getMessage(e.getMessage(), e.getMessageParameters(), Locale.getDefault());
			MessageNotifier.showError(this.getWindow(), ERROR, message);
		} catch (final InvalidFileDataException e) {
			SeedInventoryImportFileComponent.LOG.debug(ERROR_IMPORTING + e.getMessage(), e);
			final String message = this.messageSource.getMessage(e.getMessage(), e.getMessageParameters(), Locale.getDefault());
			MessageNotifier.showError(this.getWindow(), ERROR, message);
			this.finishButton.setEnabled(false);
		}
	}



}
