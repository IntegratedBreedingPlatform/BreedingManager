package org.generationcp.breeding.manager.customcomponent;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.util.FileDownloaderUtility;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.CustomReportType;
import org.generationcp.commons.reports.service.JasperReportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.reports.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ExportListAsDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	protected static final String XLS_EXT = ".xls";

	protected static final String CSV_EXT = ".csv";

	private static final int NO_OF_REQUIRED_COLUMNS = 3;

	private static final String CSV_FORMAT = "CSV";

	private static final String XLS_FORMAT = "Excel";

	private static final long serialVersionUID = -4214986909789479904L;

	private static final Logger LOG = LoggerFactory.getLogger(ExportListAsDialog.class);
	protected static final String TEMP_FILENAME = "temp";
	protected static final String TEMP_FILENAME_FOR_GENOTYPING = "tempListForGenotyping";
	public static final int DEFAULT_PLATE_SIZE = 96;

	private VerticalLayout mainLayout;
	private Label exportFormalLbl;
	private Label chooseAnExportLbl;
	private ComboBox formatOptionsCbx;
	private Button finishButton;
	private Button cancelButton;

	private final Component source;

	private final GermplasmList germplasmList;

	private final Table listDataTable;

	private String exportWarningMessage = "";

	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private PlatformTransactionManager transactionManager;

	@Resource
	private JasperReportService jasperReportService;

	@Resource
	private GermplasmListExporter germplasmListExporter;

	@Resource
	private FileDownloaderUtility fileDownloaderUtility;

	@Resource
	private ContextUtil contextUtil;

	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	public ExportListAsDialog(final Component source, final GermplasmList germplasmList, final Table listDataTable) {
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
		this.exportWarningMessage = this.messageSource.getMessage(Message.EXPORT_WARNING_MESSAGE);

		this.exportFormalLbl = new Label(this.messageSource.getMessage(Message.EXPORT_FORMAT).toUpperCase());
		this.exportFormalLbl.setDebugId("exportFormalLbl");
		this.exportFormalLbl.setStyleName(Bootstrap.Typography.H2.styleName());

		this.chooseAnExportLbl = new Label(this.messageSource.getMessage(Message.CHOOSE_AN_EXPORT_FORMAT) + ":");
		this.chooseAnExportLbl.setDebugId("chooseAnExportLbl");

		this.formatOptionsCbx = new ComboBox();
		this.formatOptionsCbx.setDebugId("formatOptionsCbx");
		this.formatOptionsCbx.setImmediate(true);
		this.formatOptionsCbx.setNullSelectionAllowed(false);
		this.formatOptionsCbx.setTextInputAllowed(false);
		this.formatOptionsCbx.setWidth("250px");

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setWidth("80px");

		this.finishButton = new Button(this.messageSource.getMessage(Message.FINISH));
		this.finishButton.setDebugId("finishButton");
		this.finishButton.setWidth("80px");
		this.finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	@Override
	public void initializeValues() {
		this.formatOptionsCbx.addItem(ExportListAsDialog.XLS_FORMAT);
		this.formatOptionsCbx.addItem(ExportListAsDialog.CSV_FORMAT);
		this.formatOptionsCbx.addItem(this.messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER));
		this.addCustomReports(this.formatOptionsCbx);

		// default value
		this.formatOptionsCbx.setValue(ExportListAsDialog.XLS_FORMAT);
	}

	private void addCustomReports(final ComboBox formatOptions) {

		final List<CustomReportType> customReports = this.jasperReportService
				.getCustomReportTypes(ToolSection.BM_LIST_MGR_CUSTOM_REPORT.name(), ToolName.LIST_MANAGER.getName());
		for (final CustomReportType customReport : customReports) {
			formatOptions.addItem(customReport.getCode().concat(" - ").concat(customReport.getName()));
		}

	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());

		this.finishButton.addListener(new FinishButtonListener(this));
	}

	protected void exportListAction(final Table table) {

		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {

				ExportListAsDialog.this.showWarningMessage(table);
				// do the export
				final String exportType = ExportListAsDialog.this.formatOptionsCbx.getValue().toString();
				if (ExportListAsDialog.XLS_FORMAT.equalsIgnoreCase(ExportListAsDialog.this.formatOptionsCbx.getValue().toString())) {
					ExportListAsDialog.this.exportListAsXLS(table);
				} else if (ExportListAsDialog.CSV_FORMAT.equalsIgnoreCase(ExportListAsDialog.this.formatOptionsCbx.getValue().toString())) {
					ExportListAsDialog.this.exportListAsCSV(table);
				} else if (exportType
						.equalsIgnoreCase(ExportListAsDialog.this.messageSource.getMessage(Message.EXPORT_LIST_FOR_GENOTYPING_ORDER))) {
					ExportListAsDialog.this.exportListForGenotypingOrderAction();
				} else {
					final String userSelection = ExportListAsDialog.this.formatOptionsCbx.getValue().toString();
					final String reportCode = userSelection.substring(0, userSelection.indexOf("-")).trim();
					ExportListAsDialog.this.exportCustomReport(reportCode);
				}

			}
		});

	}

	@Override
	public void layoutComponents() {
		// window formatting
		this.setCaption(this.messageSource.getMessage(Message.EXPORT_GERMPLASM_LIST));
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("225px");
		this.setWidth("450px");

		final HorizontalLayout fieldLayout = new HorizontalLayout();
		fieldLayout.setDebugId("fieldLayout");
		fieldLayout.setSpacing(true);
		fieldLayout.addComponent(this.chooseAnExportLbl);
		fieldLayout.addComponent(this.formatOptionsCbx);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setHeight("50px");
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.finishButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.finishButton, Alignment.BOTTOM_LEFT);

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("exportListMainLayout");
		this.mainLayout.setSpacing(true);
		this.mainLayout.addComponent(this.exportFormalLbl);
		this.mainLayout.addComponent(fieldLayout);
		this.mainLayout.addComponent(buttonLayout);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	protected void exportListAsCSV(final Table table) {
		try {

			final String visibleFileName = this.germplasmList.getName() + ExportListAsDialog.CSV_EXT;
			final String temporaryFilePath = this.installationDirectoryUtil
					.getTempFileInOutputDirectoryForProjectAndTool(this.germplasmList.getName(),  ExportListAsDialog.CSV_EXT, contextUtil.getProjectInContext(),
							ToolName.BM_LIST_MANAGER_MAIN);
			this.germplasmListExporter.exportGermplasmListCSV(temporaryFilePath, table, this.germplasmList.getId());

			this.fileDownloaderUtility.initiateFileDownload(temporaryFilePath, visibleFileName, this.source);

		} catch (final GermplasmListExporterException | IOException e) {
			ExportListAsDialog.LOG.error(this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
					e.getMessage() + ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

	}

	protected void exportCustomReport(final String reportCode) {
		try {
			final String temporaryFilePath = installationDirectoryUtil
					.getTempFileInOutputDirectoryForProjectAndTool(ExportListAsDialog.TEMP_FILENAME, CSV_EXT, contextUtil.getProjectInContext(),
							ToolName.BM_LIST_MANAGER_MAIN);
			final Reporter customReport =
					this.germplasmListExporter.exportGermplasmListCustomReport(this.germplasmList.getId(), temporaryFilePath, reportCode);

			this.fileDownloaderUtility.initiateFileDownload(temporaryFilePath, customReport.getFileName(), this.source);

		} catch (final GermplasmListExporterException | IOException e) {
			ExportListAsDialog.LOG.error(this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
					e.getMessage() + ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	protected void exportListAsXLS(final Table table) {
		try {
			final String temporaryFilePath = installationDirectoryUtil
					.getTempFileInOutputDirectoryForProjectAndTool(this.germplasmList.getName(), ExportListAsDialog.XLS_EXT, this.contextUtil.getProjectInContext(),
							ToolName.BM_LIST_MANAGER_MAIN);
			this.germplasmListExporter.exportGermplasmListXLS(this.germplasmList.getId(), temporaryFilePath, table);
			final String visibleFileName = this.germplasmList.getName() + ExportListAsDialog.XLS_EXT;

			this.fileDownloaderUtility.initiateFileDownload(temporaryFilePath, visibleFileName, this.source);
			// must figure out other way to clean-up file because deleting it here makes it unavailable for download
		} catch (final GermplasmListExporterException | IOException e) {
			ExportListAsDialog.LOG.error(this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
					e.getMessage() + ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	protected void exportListForGenotypingOrderAction() {
		if (this.germplasmList.isLockedList()) {

			try {

				final String temporaryFilePath = installationDirectoryUtil.getTempFileInOutputDirectoryForProjectAndTool(
						TEMP_FILENAME_FOR_GENOTYPING, XLS_EXT, this.contextUtil.getProjectInContext(), ToolName.BM_LIST_MANAGER_MAIN);

				this.germplasmListExporter
						.exportKBioScienceGenotypingOrderXLS(this.germplasmList.getId(), temporaryFilePath, DEFAULT_PLATE_SIZE);

				final String visibleFileName = this.germplasmList.getName().replace(" ", "_") + "ForGenotyping.xls";
				this.fileDownloaderUtility.initiateFileDownload(temporaryFilePath, visibleFileName, this.source);

			} catch (final GermplasmListExporterException | IOException e) {
				ExportListAsDialog.LOG.error(e.getMessage(), e);
				MessageNotifier
						.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e.getMessage());
			}
		} else {
			MessageNotifier.showError(this.source.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
					this.messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED));
		}
	}

	protected void showWarningMessage(final Table table) {
		if (this.isARequiredColumnHidden(table)) {
			this.showMessage(this.exportWarningMessage);
		}
	}

	protected void showMessage(final String message) {
		MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING), message);
	}

	protected boolean isARequiredColumnHidden(final Table listDataTable) {
		int visibleRequiredColumns = 0;
		final Object[] visibleColumns = listDataTable.getVisibleColumns();

		for (final Object column : visibleColumns) {
			if (this.isARequiredColumn(column.toString()) && !listDataTable.isColumnCollapsed(column)) {
				visibleRequiredColumns++;
			}
		}

		return visibleRequiredColumns < ExportListAsDialog.NO_OF_REQUIRED_COLUMNS;
	}

	protected boolean isARequiredColumn(final String column) {
		return ColumnLabels.ENTRY_ID.getName().equalsIgnoreCase(column) || ColumnLabels.GID.getName().equalsIgnoreCase(column)
				|| ColumnLabels.DESIGNATION.getName().equalsIgnoreCase(column);
	}

	public void setListExporter(final GermplasmListExporter listExporter) {
		this.germplasmListExporter = listExporter;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setTransactionManager(final PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setJasperReportService(final JasperReportService jasperReportService) {
		this.jasperReportService = jasperReportService;
	}

	public void setFileDownloaderUtility(final FileDownloaderUtility fileDownloaderUtility) {
		this.fileDownloaderUtility = fileDownloaderUtility;
	}

	public void setExportOptionValue(final String comboValue) {
		this.formatOptionsCbx.setValue(comboValue);
	}

	protected String formatCustomReportString(final CustomReportType type) {
		return type.getCode() + " - " + type.getName();
	}

	protected GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	protected Table getListDataTable() {
		return this.listDataTable;
	}

	protected SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setInstallationDirectoryUtil(final InstallationDirectoryUtil installationDirectoryUtil) {
		this.installationDirectoryUtil = installationDirectoryUtil;
	}

	static class FinishButtonListener implements Button.ClickListener {

		private static final long serialVersionUID = 1L;

		ExportListAsDialog exportListAsDialog;

		FinishButtonListener(final ExportListAsDialog exportListAsDialog) {
			this.exportListAsDialog = exportListAsDialog;
		}


		@Override
		public void buttonClick(final ClickEvent event) {

			if (this.exportListAsDialog.getGermplasmList().isLockedList()) {
				this.exportListAsDialog.exportListAction(this.exportListAsDialog.getListDataTable());
				this.exportListAsDialog.getParent().removeWindow(this.exportListAsDialog);
			} else {
				MessageNotifier.showError(this.exportListAsDialog.getWindow(),
						this.exportListAsDialog.getMessageSource().getMessage(Message.ERROR_EXPORTING_LIST),
						this.exportListAsDialog.getMessageSource().getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED));
			}

		}

	}
}
