
package org.generationcp.breeding.manager.listimport;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ConfirmOption;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class NameHandlingDialog extends BaseSubWindow implements BreedingManagerLayout, InitializingBean, Window.CloseListener {

	private static final long serialVersionUID = -8163123932480001987L;

	private VerticalLayout mainLayout;

	private Label setAsPreferredNameLbl;
	private OptionGroup setAsPreferredNameOption;

	private Label useAsPreferredLbl;
	private ComboBox nameTypesComboBox;
	private Button cancelBtn;
	private Button continueBtn;

	private final NameHandlingDialogSource source;
	private List<String> importedNameFactors;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public NameHandlingDialog(final NameHandlingDialogSource source, final List<String> importedNameFactors) {
		this.source = source;
		this.importedNameFactors = importedNameFactors;
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
		this.setModal(true);
		this.setCaption(this.messageSource.getMessage(Message.NAME_HANDLING_FOR_IMPORTED_GERMPLASM));
		this.setStyleName(Reindeer.WINDOW_LIGHT);

		// define window size, set as not resizable
		this.setWidth("520px");
		this.setHeight("180px");
		this.setResizable(false);

		// center window within the browser
		this.center();

		this.setAsPreferredNameLbl = new Label(this.messageSource.getMessage(Message.SET_IMPORTED_NAME_AS_PREFERRED_NAME));
		this.setAsPreferredNameLbl.setDebugId("setAsPreferredNameLbl");

		this.setAsPreferredNameOption = new OptionGroup();
		this.setAsPreferredNameOption.setDebugId("setAsPreferredNameOption");
		this.setAsPreferredNameOption.setImmediate(true);
		this.setAsPreferredNameOption.setStyleName("v-select-optiongroup-horizontal");

		this.useAsPreferredLbl = new Label(this.messageSource.getMessage(Message.USE_AS_PREFERRED));
		this.useAsPreferredLbl.setDebugId("useAsPreferredLbl");

		this.nameTypesComboBox = new ComboBox();
		this.nameTypesComboBox.setDebugId("nameTypesComboBox");
		this.nameTypesComboBox.setStyleName("v-select-optiongroup-horizontal");

		this.cancelBtn = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelBtn.setDebugId("cancelBtn");

		this.continueBtn = new Button(this.messageSource.getMessage(Message.CONTINUE));
		this.continueBtn.setDebugId("continueBtn");
		this.continueBtn.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());

	}

	@Override
	public void initializeValues() {
		this.setAsPreferredNameOption.addItem(ConfirmOption.YES);
		this.setAsPreferredNameOption.setItemCaption(ConfirmOption.YES, this.messageSource.getMessage(Message.YES));
		this.setAsPreferredNameOption.addItem(ConfirmOption.NO);
		this.setAsPreferredNameOption.setItemCaption(ConfirmOption.NO, this.messageSource.getMessage(Message.NO));
		this.setAsPreferredNameOption.select(ConfirmOption.NO);

		this.populateNameTypesComboBox();

	}

	void populateNameTypesComboBox() {
		for (final String nameFactor : this.importedNameFactors) {
			this.nameTypesComboBox.addItem(nameFactor);
		}

		// Select the first value in dropdown
		if (!this.importedNameFactors.isEmpty()){
			this.nameTypesComboBox.setValue(this.importedNameFactors.get(0));
		}
	}

	@Override
	public void addListeners() {
		this.cancelBtn.addListener(new CloseWindowAction());

		this.continueBtn.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				final boolean setImportedNameAsPreferredName =
						NameHandlingDialog.this.setAsPreferredNameOption.getValue().equals(ConfirmOption.YES) ? true : false;
				final String preferredNameType = NameHandlingDialog.this.nameTypesComboBox.getValue().toString();
				NameHandlingDialog.this.source.setImportedNameAsPreferredName(setImportedNameAsPreferredName, preferredNameType);
			}
		});

		this.continueBtn.addListener(new CloseWindowAction());
	}

	@Override
	public void layoutComponents() {
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("nameHandlingMainLayout");
		this.mainLayout.setSpacing(true);

		final HorizontalLayout setAsPreferredNameLayout = new HorizontalLayout();
		setAsPreferredNameLayout.setDebugId("setAsPreferredNameLayout");
		setAsPreferredNameLayout.setSpacing(true);
		setAsPreferredNameLayout.addComponent(this.setAsPreferredNameLbl);
		setAsPreferredNameLayout.addComponent(this.setAsPreferredNameOption);

		this.mainLayout.addComponent(setAsPreferredNameLayout);

		final HorizontalLayout useAsPreferredLayout = new HorizontalLayout();
		useAsPreferredLayout.setDebugId("useAsPreferredLayout");
		useAsPreferredLayout.setSpacing(true);
		useAsPreferredLayout.addComponent(this.useAsPreferredLbl);
		useAsPreferredLayout.addComponent(this.nameTypesComboBox);

		this.mainLayout.addComponent(useAsPreferredLayout);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setSpacing(true);
		buttonLayout.setWidth("100%");
		buttonLayout.addComponent(this.cancelBtn);
		buttonLayout.addComponent(this.continueBtn);
		buttonLayout.setComponentAlignment(this.cancelBtn, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.continueBtn, Alignment.BOTTOM_LEFT);

		this.mainLayout.addComponent(buttonLayout);

		this.addComponent(this.mainLayout);
	}

	@Override
	public void windowClose(final CloseEvent e) {
		// do nothing
	}
	
	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	
	public ComboBox getNameTypesComboBox() {
		return nameTypesComboBox;
	}
	
	
	public List<String> getImportedNameFactors() {
		return importedNameFactors;
	}

	public void setImportedNameFactors(final List<String> importedNameFactors) {
		this.importedNameFactors = importedNameFactors;
	}

	
	public NameHandlingDialogSource getSource() {
		return source;
	}

}
