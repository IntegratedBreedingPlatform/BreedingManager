
package org.generationcp.breeding.manager.customfields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.application.BreedingManagerWindowGenerator;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BreedingMethodField extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 4506866031376540836L;
	private final static Logger LOG = LoggerFactory.getLogger(BreedingMethodField.class);
	private static String DEFAULT_METHOD = "UDM";

	private Label captionLabel;
	private String caption;
	private ComboBox breedingMethodComboBox;
	private boolean isMandatory = true;
	private boolean hasDefaultValue = true;
	private boolean changed;
	private int leftIndentPixels = 130;

	private Map<String, String> methodMap;
	private List<Method> methods;
	private CheckBox showFavoritesCheckBox;
	private Button manageFavoritesLink;

	private Window attachToWindow;

	private Label methodDescription;
	private PopupView popup;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private BreedingManagerService breedingManagerService;

	@Autowired
	private BreedingManagerWindowGenerator breedingManagerWindowGenerator;

	@Resource
	private ContextUtil contextUtil;

	private String programUniqueId;

	public BreedingMethodField() {
		this.caption = "Breeding Method: ";
		this.changed = false;
	}

	public BreedingMethodField(final Window attachToWindow) {
		this();
		this.attachToWindow = attachToWindow;
		this.isMandatory = true;
		this.hasDefaultValue = true;
	}

	public BreedingMethodField(final Window attachToWindow, final int pixels) {
		this();
		this.attachToWindow = attachToWindow;
		this.leftIndentPixels = pixels;
		this.isMandatory = true;
		this.hasDefaultValue = true;
	}

	public BreedingMethodField(final int pixels) {
		this();
		this.leftIndentPixels = pixels;
		this.isMandatory = true;
		this.hasDefaultValue = true;
	}

	public BreedingMethodField(final Window attachToWindow, final boolean isMandatory, final boolean hasDefaultValue) {
		this();
		this.attachToWindow = attachToWindow;
		this.isMandatory = isMandatory;
		this.hasDefaultValue = hasDefaultValue;
	}

	public BreedingMethodField(final Window attachToWindow, final int pixels, final boolean isMandatory, final boolean hasDefaultValue) {
		this();
		this.attachToWindow = attachToWindow;
		this.leftIndentPixels = pixels;
		this.isMandatory = isMandatory;
		this.hasDefaultValue = hasDefaultValue;
	}

	public BreedingMethodField(final int pixels, final boolean isMandatory, final boolean hasDefaultValue) {
		this();
		this.leftIndentPixels = pixels;
		this.isMandatory = isMandatory;
		this.hasDefaultValue = hasDefaultValue;
	}

	@Override
	public void instantiateComponents() {
		this.setHeight("250px");

		this.captionLabel = new Label(this.caption);
		this.captionLabel.setDebugId("captionLabel");
		this.captionLabel.addStyleName("bold");

		this.breedingMethodComboBox = new ComboBox();
		this.breedingMethodComboBox.setDebugId("breedingMethodComboBox");
		this.breedingMethodComboBox.setWidth("320px");
		this.breedingMethodComboBox.setImmediate(true);

		if (this.isMandatory) {
			this.breedingMethodComboBox.setNullSelectionAllowed(false);
			this.breedingMethodComboBox.setRequired(true);
			this.breedingMethodComboBox.setRequiredError("Please specify the method.");
		} else {
			this.breedingMethodComboBox.setNullSelectionAllowed(true);
			this.breedingMethodComboBox.setInputPrompt("Please Choose");
		}

		this.showFavoritesCheckBox = new CheckBox();
		this.showFavoritesCheckBox.setDebugId("showFavoritesCheckBox");
		this.showFavoritesCheckBox.setCaption(this.messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_METHODS));
		this.showFavoritesCheckBox.setImmediate(true);

		this.manageFavoritesLink = new Button();
		this.manageFavoritesLink.setDebugId("manageFavoritesLink");
		this.manageFavoritesLink.setStyleName(BaseTheme.BUTTON_LINK);
		this.manageFavoritesLink.setCaption(this.messageSource.getMessage(Message.MANAGE_METHODS));

		this.methodDescription = new Label();
		this.methodDescription.setDebugId("methodDescription");
		this.methodDescription.setWidth("300px");
		this.popup = new PopupView(" ? ", this.methodDescription);
		this.popup.setDebugId("popup");
		this.popup.setStyleName("gcp-popup-view");

		try {
			this.programUniqueId = this.breedingManagerService.getCurrentProject().getUniqueID();
		} catch (final MiddlewareQueryException e) {
			BreedingMethodField.LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void initializeValues() {
		this.populateMethods(this.programUniqueId);
		this.enableMethodHelp(this.hasDefaultValue);
		this.initPopulateFavMethod(this.programUniqueId);
	}

	public boolean initPopulateFavMethod(final String programUUID) {
		boolean hasFavorite = false;
		if (!this.hasDefaultValue && BreedingManagerUtil.hasFavoriteMethods(this.germplasmDataManager, programUUID)) {
			this.showFavoritesCheckBox.setValue(true);
			hasFavorite = true;
			this.populateMethods(true, this.programUniqueId);
		}
		return hasFavorite;
	}

	@Override
	public void addListeners() {

		this.breedingMethodComboBox.addListener(new ComboBox.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				BreedingMethodField.this.updateComboBoxDescription();
				BreedingMethodField.this.changed = true;
			}
		});

		this.breedingMethodComboBox.addListener(new ComboBox.ItemSetChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void containerItemSetChange(final ItemSetChangeEvent event) {
				BreedingMethodField.this.updateComboBoxDescription();
				BreedingMethodField.this.changed = true;
			}
		});

		this.showFavoritesCheckBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				BreedingMethodField.this.populateMethods(((Boolean) event.getProperty().getValue()).equals(true),
						BreedingMethodField.this.programUniqueId);
				BreedingMethodField.this.updateComboBoxDescription();
			}
		});

		this.manageFavoritesLink.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				BreedingMethodField.this.launchManageWindow();
			}
		});

	}

	@Override
	public void layoutComponents() {
		this.addComponent(this.captionLabel, "top:3px; left:0;");
		this.addComponent(this.breedingMethodComboBox, "top:0; left:" + this.leftIndentPixels + "px");

		int pixels = this.leftIndentPixels + 325;
		this.addComponent(this.popup, "top:0; left:" + pixels + "px");

		this.addComponent(this.showFavoritesCheckBox, "top:30px; left:" + this.leftIndentPixels + "px");

		pixels = this.leftIndentPixels + 240;
		this.addComponent(this.manageFavoritesLink, "top:33px; left:" + pixels + "px");
	}

	@Override
	public void updateLabels() {
		// not implemented
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public ComboBox getBreedingMethodComboBox() {
		return this.breedingMethodComboBox;
	}

	public void setBreedingMethodComboBox(final ComboBox breedingMethodComboBox) {
		this.breedingMethodComboBox = breedingMethodComboBox;
	}

	public void setValue(final String value) {
		this.breedingMethodComboBox.select(value);
	}

	public String getValue() {
		return (String) this.breedingMethodComboBox.getValue();
	}

	public void validate() {
		this.breedingMethodComboBox.validate();
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(final boolean changed) {
		this.changed = changed;
	}

	private void updateComboBoxDescription() {
		final Object breedingMethodComboBoxValue = this.breedingMethodComboBox.getValue();

		final Boolean methodSelected = breedingMethodComboBoxValue != null;
		this.enableMethodHelp(methodSelected);

		if (methodSelected) {
			this.methodDescription.setValue(this.methodMap.get(breedingMethodComboBoxValue.toString()));
		}
	}

	private void enableMethodHelp(final Boolean enable) {
		this.methodDescription.setEnabled(enable);
		this.popup.setEnabled(enable);
	}

	private Map<String, String> populateMethods(final String programUUID) {
		try {
			this.methods = this.germplasmDataManager.getMethodsByUniqueID(programUUID);
		} catch (final MiddlewareQueryException e) {
			BreedingMethodField.LOG.error(e.getMessage(), e);
		}

		if (this.methods == null) {
			this.methods = new ArrayList<Method>();
		}

		this.methodMap = new HashMap<String, String>();

		Method defaultMethod = null;
		for (final Method method : this.methods) {
			this.breedingMethodComboBox.addItem(method.getMid());
			this.breedingMethodComboBox.setItemCaption(method.getMid(), method.getMname());

			if (BreedingMethodField.DEFAULT_METHOD.equalsIgnoreCase(method.getMcode())) {
				defaultMethod = method;
			}

			this.methodMap.put(method.getMid().toString(), method.getMdesc());
		}

		if (this.hasDefaultValue) {
			if (defaultMethod != null) {
				this.breedingMethodComboBox.setValue(defaultMethod.getMid());
				this.methodDescription.setValue(defaultMethod.getMdesc());
			} else {
				// if the list of methods has no default method, just select the first item from the list
				if (this.breedingMethodComboBox.getValue() == null && !this.methods.isEmpty() && this.methods.get(0) != null) {
					this.breedingMethodComboBox.setValue(this.methods.get(0).getMid());
					this.breedingMethodComboBox.setDescription(this.methods.get(0).getMdesc());
				}
			}
		}

		return this.methodMap;
	}

	private void populateMethods(final boolean showOnlyFavorites, final String programUUID) {
		this.breedingMethodComboBox.removeAllItems();
		if (showOnlyFavorites) {
			try {
				BreedingManagerUtil.populateWithFavoriteMethods(this.workbenchDataManager, this.germplasmDataManager,
						this.breedingMethodComboBox, null, programUUID);
			} catch (final MiddlewareQueryException e) {
				BreedingMethodField.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
						"Error getting favorite methods!");
			}
		} else {
			this.populateMethods(programUUID);
		}

	}

	private void launchManageWindow() {
		try {
			final Project project = this.contextUtil.getProjectInContext();
			final Window window = this.attachToWindow != null ? this.attachToWindow : this.getWindow();
			final Window manageFavoriteMethodsWindow = breedingManagerWindowGenerator.openMethodManagerPopupWindow(project.getProjectId(), window,
					this.messageSource.getMessage(Message.MANAGE_METHODS));
			manageFavoriteMethodsWindow.addListener(new CloseListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void windowClose(final CloseEvent e) {
					final Object lastValue = BreedingMethodField.this.breedingMethodComboBox.getValue();
					BreedingMethodField.this.populateMethods(
							((Boolean) BreedingMethodField.this.showFavoritesCheckBox.getValue()).equals(true),
							BreedingMethodField.this.programUniqueId);
					BreedingMethodField.this.breedingMethodComboBox.setValue(lastValue);
				}
			});
		} catch (final MiddlewareQueryException e) {
			BreedingMethodField.LOG.error("Error on manageFavoriteMethods click", e);
		}
	}

	@Override
	public void setCaption(final String caption) {
		this.caption = caption;
		if (this.captionLabel != null) {
			this.captionLabel.setValue(this.caption);
		}
	}

	protected int getLeftIndentPixels() {
		return this.leftIndentPixels;
	}

	public SimpleResourceBundleMessageSource getMessageSource() {
		return this.messageSource;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return this.germplasmDataManager;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public boolean isHasDefaultValue() {
		return this.hasDefaultValue;
	}

	public void setHasDefaultValue(final boolean hasDefaultValue) {
		this.hasDefaultValue = hasDefaultValue;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}
}
