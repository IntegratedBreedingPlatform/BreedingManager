
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GenerateStockIDsDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final String DEFAULT_STOCKID_PREFIX = "SID";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(GenerateStockIDsDialog.class);

	@Resource
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private StockService stockService;

	private Button btnContinue;
	private Button btnCancel;

	private Label lblSpecifyPrefix;
	private Label lblDefaultPrefixDescription;
	private Label lblNextPrefixInSequence;
	private Label lblStockIdForThisList;

	private TextField txtSpecifyPrefix;
	private Label lblExampleNextPrefixInSequence;
	private Label lblExampleStockIdForThisList;

	private final VerticalLayout source;

	public GenerateStockIDsDialog(VerticalLayout source, GermplasmList germplasmList) {
		this.source = source;
	}

	private void initializeSubWindow() {

		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setModal(true);
		this.setWidth("600px");
		this.setResizable(false);
		this.center();
		this.setCaption(this.messageSource.getMessage(Message.GENERATE_STOCKID_HEADER));

	}

	@Override
	public void afterPropertiesSet() throws Exception {

		this.initializeSubWindow();
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
		this.updateLabels();

	}

	@Override
	public void instantiateComponents() {

		this.lblSpecifyPrefix = new Label(this.messageSource.getMessage(Message.SPECIFY_STOCKID_PREFIX_LABEL));
		this.lblSpecifyPrefix.setDebugId("lblSpecifyPrefix");
		this.lblSpecifyPrefix.addStyleName("bold");
		this.lblSpecifyPrefix.setImmediate(true);

		this.txtSpecifyPrefix = new TextField();
		this.txtSpecifyPrefix.setDebugId("txtSpecifyPrefix");
		this.txtSpecifyPrefix.setImmediate(true);
		this.txtSpecifyPrefix.setMaxLength(15);
		this.txtSpecifyPrefix.focus();

		this.lblDefaultPrefixDescription = new Label(this.messageSource.getMessage(Message.DEFAULT_PREFIX_DESCRIPTION_LABEL));
		this.lblDefaultPrefixDescription.setDebugId("lblDefaultPrefixDescription");
		this.lblDefaultPrefixDescription.addStyleName("italic");

		this.lblNextPrefixInSequence = new Label(this.messageSource.getMessage(Message.NEXT_PREFIX_IN_SEQUENCE_LABEL));
		this.lblNextPrefixInSequence.setDebugId("lblNextPrefixInSequence");
		this.lblNextPrefixInSequence.addStyleName("bold");
		this.lblNextPrefixInSequence.setImmediate(true);

		this.lblExampleNextPrefixInSequence = new Label();
		this.lblExampleNextPrefixInSequence.setDebugId("lblExampleNextPrefixInSequence");

		this.lblStockIdForThisList = new Label(this.messageSource.getMessage(Message.EXAMPLE_STOCKID_LABEL));
		this.lblStockIdForThisList.setDebugId("lblStockIdForThisList");
		this.lblStockIdForThisList.addStyleName("bold");
		this.lblStockIdForThisList.setImmediate(true);

		this.lblExampleStockIdForThisList = new Label();
		this.lblExampleStockIdForThisList.setDebugId("lblExampleStockIdForThisList");

		this.btnContinue = new Button(this.messageSource.getMessage(Message.CONTINUE));
		this.btnContinue.setDebugId("btnContinue");
		this.btnContinue.setWidth("80px");
		this.btnContinue.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.btnCancel = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.btnCancel.setDebugId("btnCancel");
		this.btnCancel.setWidth("80px");
		this.btnCancel.setDescription("Cancel");
		this.btnCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

	}

	@Override
	public void initializeValues() {

		this.updateSampleStockId("");

	}

	@Override
	public void addListeners() {

		this.btnCancel.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1271362384141739702L;

			@Override
			public void buttonClick(ClickEvent event) {
				Window win = event.getButton().getWindow();
				win.getParent().removeWindow(win);
			}

		});

		this.btnContinue.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 2853818327406493402L;

			@Override
			public void buttonClick(ClickEvent event) {
				GenerateStockIDsDialog.this.continueAction(event);
			}
		});

		this.txtSpecifyPrefix.addListener(new TextField.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				GenerateStockIDsDialog.this.updateSampleStockId(GenerateStockIDsDialog.this.txtSpecifyPrefix.getValue().toString());
			}
		});

	}

	@Override
	public void layoutComponents() {

		VerticalLayout rootLayout = new VerticalLayout();
		rootLayout.setDebugId("rootLayout");
		rootLayout.setWidth("100%");
		rootLayout.setSpacing(true);
		rootLayout.setMargin(true);
		this.setContent(rootLayout);

		rootLayout.addComponent(new OneLineLayout(this.lblSpecifyPrefix, this.txtSpecifyPrefix));
		rootLayout.addComponent(this.lblDefaultPrefixDescription);
		rootLayout.addComponent(new OneLineLayout(this.lblNextPrefixInSequence, this.lblExampleNextPrefixInSequence));
		rootLayout.addComponent(new OneLineLayout(this.lblStockIdForThisList, this.lblExampleStockIdForThisList));

		HorizontalLayout hButton = new HorizontalLayout();
		hButton.setDebugId("hButton");
		hButton.setSpacing(true);
		hButton.setMargin(true);
		hButton.addComponent(this.btnCancel);
		hButton.addComponent(this.btnContinue);

		rootLayout.addComponent(hButton);
		rootLayout.setComponentAlignment(hButton, Alignment.MIDDLE_CENTER);

	}

	@Override
	public void updateLabels() {
		// do nothing

	}

	protected Label getLblExampleNextPrefixInSequence() {
		return this.lblExampleNextPrefixInSequence;
	}

	protected Label getLblExampleStockIdForThisList() {
		return this.lblExampleStockIdForThisList;
	}

	private class OneLineLayout extends HorizontalLayout {

		private static final long serialVersionUID = 1L;

		OneLineLayout(AbstractComponent... components) {
			this.setSpacing(true);
			for (AbstractComponent component : components) {
				this.addComponent(component);
			}
		}

	}

	protected void updateSampleStockId(String prefix) {
		try {

			if (!this.isValidPrefix(prefix)) {
				this.showMessageValidationForPrefix();
			} else {
				String nextStockIDPrefix = "";

				if (!StringUtils.isEmpty(prefix.trim())) {
					nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(prefix, "-");
				} else {
					nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(GenerateStockIDsDialog.DEFAULT_STOCKID_PREFIX, "-");
				}

				this.lblExampleNextPrefixInSequence.setValue(nextStockIDPrefix.substring(0, nextStockIDPrefix.length() - 1));
				this.lblExampleStockIdForThisList.setValue(nextStockIDPrefix + "1");
			}

		} catch (MiddlewareException e) {
			GenerateStockIDsDialog.LOG.error(e.getMessage(), e);
		}
	}

	private void showMessageValidationForPrefix() {
		MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
				this.messageSource.getMessage(Message.INVALID_PREFIX));
		this.txtSpecifyPrefix.focus();
	}

	boolean isValidPrefix(String prefix) {
		String pattern = "^[a-zA-Z]*$";
		return Pattern.matches(pattern, prefix);
	}

	protected void applyStockIdToImportedGermplasm(String prefix, List<ImportedGermplasm> importedGermplasmList) {

		String nextStockIDPrefix;
		try {

			if (StringUtils.isEmpty(prefix)) {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(GenerateStockIDsDialog.DEFAULT_STOCKID_PREFIX, "-");
			} else {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(prefix, "-");
			}

			int stockIdSequence = 1;
			for (ImportedGermplasm importedGermplasm : importedGermplasmList) {
				if (importedGermplasm.getSeedAmount() != null && importedGermplasm.getSeedAmount() > 0) {
					if (StringUtils.isEmpty(importedGermplasm.getInventoryId())) {
						importedGermplasm.setInventoryId(nextStockIDPrefix + stockIdSequence);
						stockIdSequence++;
					}
				}

			}
		} catch (MiddlewareException e) {
			GenerateStockIDsDialog.LOG.error(e.getMessage(), e);
		}

	}

	private void continueAction(ClickEvent event) {
		if (GenerateStockIDsDialog.this.source instanceof SpecifyGermplasmDetailsComponent) {

			String prefix = GenerateStockIDsDialog.this.txtSpecifyPrefix.getValue().toString();

			if (!GenerateStockIDsDialog.this.isValidPrefix(prefix)) {
				GenerateStockIDsDialog.this.showMessageValidationForPrefix();
			} else {
				GenerateStockIDsDialog.this.applyStockIdToImportedGermplasm(prefix,
						((SpecifyGermplasmDetailsComponent) GenerateStockIDsDialog.this.source).getImportedGermplasm());

				((SpecifyGermplasmDetailsComponent) GenerateStockIDsDialog.this.source).popupSaveAsDialog();
				Window win = event.getButton().getWindow();
				win.getParent().removeWindow(win);
			}

		}
	}

}
