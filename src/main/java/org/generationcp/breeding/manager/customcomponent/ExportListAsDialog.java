
package org.generationcp.breeding.manager.customcomponent;

import java.io.File;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.util.FileDownloadResource;
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

	private static final String XLS_EXT = ".xls";

	private static final String CSV_EXT = ".csv";

	private static final int NO_OF_REQUIRED_COLUMNS = 3;

	private static final String CSV_FORMAT = "CSV";

	private static final String XLS_FORMAT = "Excel";

	private static final long serialVersionUID = -4214986909789479904L;

	private static final Logger LOG = LoggerFactory.getLogger(ExportListAsDialog.class);

	private VerticalLayout mainLayout;
	private Label exportFormalLbl;
	private Label chooseAnExportLbl;
	private ComboBox formatOptionsCbx;
	private Button finishButton;
	private Button cancelButton;

	private final Component source;
	private final GermplasmList germplasmList;
	private GermplasmListExporter listExporter;

	private final Table listDataTable;

	public String exportWarningMessage = "";
	private static final String USER_HOME = "user.home";
	public static final String TEMP_FILENAME = System.getProperty(ExportListAsDialog.USER_HOME) + "/temp.csv";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private PlatformTransactionManager transactionManager;

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
		this.exportFormalLbl.setStyleName(Bootstrap.Typography.H2.styleName());

		this.chooseAnExportLbl = new Label(this.messageSource.getMessage(Message.CHOOSE_AN_EXPORT_FORMAT) + ":");

		this.formatOptionsCbx = new ComboBox();
		this.formatOptionsCbx.setImmediate(true);
		this.formatOptionsCbx.setNullSelectionAllowed(false);
		this.formatOptionsCbx.setTextInputAllowed(false);
		this.formatOptionsCbx.setWidth("100px");

		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setWidth("80px");

		this.finishButton = new Button(this.messageSource.getMessage(Message.FINISH));
		this.finishButton.setWidth("80px");
		this.finishButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.listExporter = new GermplasmListExporter(this.germplasmList.getId());
	}

	@Override
	public void initializeValues() {
		this.formatOptionsCbx.addItem(ExportListAsDialog.XLS_FORMAT);
		this.formatOptionsCbx.addItem(ExportListAsDialog.CSV_FORMAT);

		// default value
		this.formatOptionsCbx.setValue(ExportListAsDialog.XLS_FORMAT);
	}

	@Override
	public void addListeners() {
		this.cancelButton.addListener(new CloseWindowAction());

		this.finishButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ExportListAsDialog.this.exportListAction(ExportListAsDialog.this.listDataTable);
			}
		});
	}

	protected void exportListAction(final Table table) {

		final TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(final TransactionStatus status) {
				if (ExportListAsDialog.this.germplasmList.isLockedList()) {
					ExportListAsDialog.this.showWarningMessage(table);
					// do the export
					if (ExportListAsDialog.XLS_FORMAT.equalsIgnoreCase(ExportListAsDialog.this.formatOptionsCbx.getValue().toString())) {
						ExportListAsDialog.this.exportListAsXLS(table);
					} else if (ExportListAsDialog.CSV_FORMAT.equalsIgnoreCase(ExportListAsDialog.this.formatOptionsCbx.getValue()
							.toString())) {
						ExportListAsDialog.this.exportListAsCSV(table);
					}
				} else {
					MessageNotifier.showError(ExportListAsDialog.this.getWindow(),
							ExportListAsDialog.this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST),
							ExportListAsDialog.this.messageSource.getMessage(Message.ERROR_EXPORT_LIST_MUST_BE_LOCKED));
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
		this.setWidth("380px");

		final HorizontalLayout fieldLayout = new HorizontalLayout();
		fieldLayout.setSpacing(true);
		fieldLayout.addComponent(this.chooseAnExportLbl);
		fieldLayout.addComponent(this.formatOptionsCbx);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setHeight("50px");
		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.finishButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.finishButton, Alignment.BOTTOM_LEFT);

		this.mainLayout = new VerticalLayout();
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

			this.listExporter.exportGermplasmListCSV(ExportListAsDialog.TEMP_FILENAME, table);
			final FileDownloadResource fileDownloadResource = this.createFileDownloadResource();
			final String listName = this.germplasmList.getName();
			fileDownloadResource.setFilename(FileDownloadResource
					.getDownloadFileName(listName, BreedingManagerUtil.getApplicationRequest()).replace(" ", "_")
					+ ExportListAsDialog.CSV_EXT);
			this.source.getWindow().open(fileDownloadResource);
			// must figure out other way to clean-up file because deleting it here makes it unavailable for download

		} catch (final GermplasmListExporterException e) {
			ExportListAsDialog.LOG.error(this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e.getMessage() + ". "
					+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}

	}

	protected void exportListAsXLS(final Table table) {
		try {
			this.listExporter.exportGermplasmListXLS(ExportListAsDialog.TEMP_FILENAME, table);
			final FileDownloadResource fileDownloadResource = this.createFileDownloadResource();
			final String listName = this.germplasmList.getName();
			fileDownloadResource.setFilename(FileDownloadResource
					.getDownloadFileName(listName, BreedingManagerUtil.getApplicationRequest()).replace(" ", "_")
					+ ExportListAsDialog.XLS_EXT);
			this.source.getWindow().open(fileDownloadResource);
			// must figure out other way to clean-up file because deleting it here makes it unavailable for download
		} catch (final GermplasmListExporterException e) {
			ExportListAsDialog.LOG.error(this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_EXPORTING_LIST), e.getMessage() + ". "
					+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	protected FileDownloadResource createFileDownloadResource() {
		final FileDownloadResource fileDownloadResource =
				new FileDownloadResource(new File(ExportListAsDialog.TEMP_FILENAME), this.source.getApplication());
		return fileDownloadResource;
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
		this.listExporter = listExporter;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
