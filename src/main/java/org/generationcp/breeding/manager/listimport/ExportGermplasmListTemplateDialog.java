
package org.generationcp.breeding.manager.listimport;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.CropType.CropEnum;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ExportGermplasmListTemplateDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	private static final String EXPANDED = "Expanded";
	private static final String ADVANCED = "Advanced";
	private static final String BASIC = "Basic";
	private static final long serialVersionUID = -9047374755825933209L;
	private static final Logger LOG = LoggerFactory.getLogger(ExportGermplasmListTemplateDialog.class);

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private VerticalLayout mainLayout;
	private Label templateFormalLbl;
	private Label chooseTemplateFormatLbl;
	private ComboBox formatOptionsCbx;
	private Button exportButton;
	private Button cancelButton;

	private final Component source;

	public ExportGermplasmListTemplateDialog(Component source) {
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
		this.templateFormalLbl = new Label(this.messageSource.getMessage(Message.TEMPLATE_FORMAT).toUpperCase());
		this.templateFormalLbl.setStyleName(Bootstrap.Typography.H2.styleName());

		this.chooseTemplateFormatLbl = new Label(this.messageSource.getMessage(Message.CHOOSE_A_TEMPLATE_FORMAT) + ":");

		this.formatOptionsCbx = new ComboBox();
		this.formatOptionsCbx.setImmediate(true);
		this.formatOptionsCbx.setNullSelectionAllowed(false);
		this.formatOptionsCbx.setTextInputAllowed(false);
		this.formatOptionsCbx.setWidth("100px");

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setWidth("80px");

		this.exportButton = new Button(this.messageSource.getMessage(Message.EXPORT));
		this.exportButton.setWidth("80px");
		this.exportButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		this.formatOptionsCbx.addItem(ExportGermplasmListTemplateDialog.BASIC);
		this.formatOptionsCbx.addItem(ExportGermplasmListTemplateDialog.ADVANCED);
		this.formatOptionsCbx.addItem(ExportGermplasmListTemplateDialog.EXPANDED);

		// default value
		this.formatOptionsCbx.setValue(ExportGermplasmListTemplateDialog.BASIC);
	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());

		this.exportButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 3036613348361168491L;

			@Override
			public void buttonClick(ClickEvent event) {
				ExportGermplasmListTemplateDialog.this.exportGermplasmTemplate();
			}
		});
	}

	protected void exportGermplasmTemplate() {
		try {

			String fileName = this.getGermplasmTemplateFileName();
			String fileToDownloadPath = this.getFileToDownloadPath(fileName);

			FileDownloadResource fileDownloadResource = this.createFileDownloadResource(fileToDownloadPath);
			fileDownloadResource
					.setFilename(FileDownloadResource.getDownloadFileName(fileName, BreedingManagerUtil.getApplicationRequest()));
			this.source.getWindow().open(fileDownloadResource);
		} catch (IOException e) {
			ExportGermplasmListTemplateDialog.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR), e.getMessage());
		} catch (MiddlewareQueryException e) {
			ExportGermplasmListTemplateDialog.LOG.error(e.getMessage(), e);
		}
	}

	protected String getGermplasmTemplateFileName() {
		return "GermplasmImportTemplate-" + this.getSelectedTemplateType() + "-rev4.xls";
	}

	protected String getFileToDownloadPath(String fileName) throws MiddlewareQueryException {
		String cropType = this.getCurrentProjectCropType();
		String installationDirectory = this.getInstallationDirectory();

		String fileToDownloadPath;
		if (!"".equals(installationDirectory)) {
			fileToDownloadPath =
					installationDirectory + File.separator + "Examples" + File.separator + cropType + File.separator + "templates"
							+ File.separator + fileName;
		} else {
			fileToDownloadPath =
					"C:" + File.separator + "Breeding Management System" + File.separator + "Examples" + File.separator + cropType
							+ File.separator + "templates" + File.separator + fileName;
		}
		return fileToDownloadPath;
	}

	private String getCurrentProjectCropType() throws MiddlewareQueryException {
		Project currentProject = this.contextUtil.getProjectInContext();
		String cropType = currentProject.getCropType().getCropName();
		// if it is a custom crop
		if (!this.isADefaultCrop(cropType)) {
			cropType = "generic";
		}

		return cropType;
	}

	protected boolean isADefaultCrop(String cropType) {
		for (CropEnum type : CropType.CropEnum.values()) {
			if (cropType.equalsIgnoreCase(type.toString())) {
				return true;
			}
		}

		return false;
	}

	protected String getInstallationDirectory() throws MiddlewareQueryException {
		String installationDirectory = "";
		WorkbenchSetting workbenchSetting = this.workbenchDataManager.getWorkbenchSetting();
		if (workbenchSetting != null && !StringUtil.isEmpty(workbenchSetting.getInstallationDirectory())) {
			installationDirectory = workbenchSetting.getInstallationDirectory();
		}

		return installationDirectory;
	}

	protected FileDownloadResource createFileDownloadResource(String fileToDownloadPath) throws IOException {
		File fileToDownload = new File(fileToDownloadPath);

		FileDownloadResource fileDownloadResource = null;
		if (!fileToDownload.exists()) {
			throw new IOException("Germplasm Template File does not exist.");
		} else {
			fileDownloadResource = new FileDownloadResource(fileToDownload, this.source.getApplication());
		}
		return fileDownloadResource;
	}

	protected String getSelectedTemplateType() {
		return this.formatOptionsCbx.getValue().toString();
	}

	@Override
	public void layoutComponents() {
		// window formatting
		this.setCaption(this.messageSource.getMessage(Message.GERMPLASM_LIST_EXPORT_TEMPLATE));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("225px");
		this.setWidth("380px");

		HorizontalLayout fieldLayout = new HorizontalLayout();
		fieldLayout.setSpacing(true);
		fieldLayout.addComponent(this.chooseTemplateFormatLbl);
		fieldLayout.addComponent(this.formatOptionsCbx);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setHeight("50px");
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.exportButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.exportButton, Alignment.BOTTOM_LEFT);

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setSpacing(true);
		this.mainLayout.addComponent(this.templateFormalLbl);
		this.mainLayout.addComponent(fieldLayout);
		this.mainLayout.addComponent(buttonLayout);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
