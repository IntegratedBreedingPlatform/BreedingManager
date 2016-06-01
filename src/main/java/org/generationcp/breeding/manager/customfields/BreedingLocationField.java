
package org.generationcp.breeding.manager.customfields;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
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
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BreedingLocationField extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 4506866031376540836L;
	private static final Logger LOG = LoggerFactory.getLogger(BreedingLocationField.class);

	private static final String DEFAULT_LOCATION = "Unknown";

	/**
	 * CONSTRUCTOR VARIABLES
	 */
	private BreedingLocationFieldSource source;
	private String caption;
	private boolean changed;
	// The parent window where the germplasm import dialog is attached to
	private Window attachToWindow;
	// The no of pixel indentation for breeding location combobox
	private int leftIndentPixels = 130;
	private Integer locationType = 0;
	// flags
	private boolean displayFavoriteLocationsFilter = true;
	private boolean displayManageLocationLink = true;

	private Label captionLabel;
	private ComboBox breedingLocationComboBox;
	private List<Location> locations;
	private CheckBox showFavoritesCheckBox;
	private Button manageFavoritesLink;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private BreedingManagerService breedingManagerService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private ContextUtil contextUtil;

	private String programUniqueId;

	public BreedingLocationField() {
		// do nothing
	}

	public BreedingLocationField(final BreedingLocationFieldSource source) {
		this.source = source;
		this.caption = "Location: ";
		this.changed = false;
	}

	public BreedingLocationField(final BreedingLocationFieldSource source, final Window attachToWindow) {
		this(source);
		this.attachToWindow = attachToWindow;
	}

	public BreedingLocationField(final BreedingLocationFieldSource source, final Window attachToWindow, final int pixels) {
		this(source, attachToWindow);
		this.leftIndentPixels = pixels;
	}

	public BreedingLocationField(final BreedingLocationFieldSource source, final int pixels) {
		this(source, null, pixels);
	}

	public BreedingLocationField(final BreedingLocationFieldSource source, final Window attachToWindow, final int pixels,
			final Integer locationType) {
		this(source, attachToWindow, pixels);
		this.locationType = locationType;
	}

	public BreedingLocationField(final BreedingLocationFieldSource source, final int pixels, final Integer locationType) {
		this(source, null, pixels, locationType);
	}

	@Override
	public void instantiateComponents() {
		this.captionLabel = new Label(this.caption);
		this.captionLabel.addStyleName("bold");

		this.breedingLocationComboBox = new ComboBox();
		this.breedingLocationComboBox.setWidth("320px");
		this.breedingLocationComboBox.setImmediate(true);
		this.breedingLocationComboBox.setNullSelectionAllowed(true);

		this.showFavoritesCheckBox = new CheckBox();
		this.showFavoritesCheckBox.setCaption(this.messageSource.getMessage(Message.SHOW_ONLY_FAVORITE_LOCATIONS));
		this.showFavoritesCheckBox.setImmediate(true);

		this.manageFavoritesLink = new Button();
		this.manageFavoritesLink.setStyleName(BaseTheme.BUTTON_LINK);
		this.manageFavoritesLink.setCaption(this.messageSource.getMessage(Message.MANAGE_LOCATIONS));

		try {
			this.programUniqueId = this.breedingManagerService.getCurrentProject().getUniqueID();
		} catch (final MiddlewareQueryException e) {
			BreedingLocationField.LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void initializeValues() {
		this.populateLocations(this.programUniqueId);
		this.initPopulateFavLocations(this.programUniqueId);
	}

	public boolean initPopulateFavLocations(final String programUUID) {
		boolean hasFavorite = false;
		if (BreedingManagerUtil.hasFavoriteLocation(this.germplasmDataManager, 0, programUUID)) {
			this.showFavoritesCheckBox.setValue(true);
			this.populateHarvestLocation(true, this.programUniqueId);
			hasFavorite = true;
		}
		return hasFavorite;
	}

	@Override
	public void addListeners() {

		this.breedingLocationComboBox.addListener(new ComboBox.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				BreedingLocationField.this.changed = true;
			}
		});

		this.breedingLocationComboBox.addListener(new ComboBox.ItemSetChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void containerItemSetChange(final ItemSetChangeEvent event) {
				BreedingLocationField.this.changed = true;
			}
		});

		this.showFavoritesCheckBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				BreedingLocationField.this.populateHarvestLocation(((Boolean) event.getProperty().getValue()).equals(true),
						BreedingLocationField.this.programUniqueId);
			}
		});

		this.manageFavoritesLink.addListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				BreedingLocationField.this.launchManageWindow();
			}
		});

	}

	@Override
	public void layoutComponents() {
		if (this.displayManageLocationLink || this.displayFavoriteLocationsFilter) {
			this.setHeight("250px");
		} else {
			this.setHeight("190px");
		}

		this.addComponent(this.captionLabel, "top:3px; left:0;");
		this.addComponent(this.breedingLocationComboBox, "top:0; left:" + this.leftIndentPixels + "px");

		if (this.displayFavoriteLocationsFilter) {
			this.addComponent(this.showFavoritesCheckBox, "top:30px; left:" + this.leftIndentPixels + "px");
		}

		if (this.displayManageLocationLink) {
			final int pixels = this.leftIndentPixels + 220;
			this.addComponent(this.manageFavoritesLink, "top:33px; left:" + pixels + "px");
		}
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public ComboBox getBreedingLocationComboBox() {
		return this.breedingLocationComboBox;
	}

	public void setBreedingLocationComboBox(final ComboBox breedingLocationComboBox) {
		this.breedingLocationComboBox = breedingLocationComboBox;
	}

	public void setValue(final String value) {
		this.breedingLocationComboBox.select(value);
	}

	public String getValue() {
		return (String) this.breedingLocationComboBox.getValue();
	}

	public void validate() {
		this.breedingLocationComboBox.validate();
	}

	public boolean isChanged() {
		return this.changed;
	}

	public void setChanged(final boolean changed) {
		this.changed = changed;
	}

	public void populateHarvestLocation(final Integer selectedLocation, final String programUUID) {
		this.populateHarvestLocation(this.showFavoritesCheckBox.getValue().equals(true), programUUID);
		if (selectedLocation != null) {
			this.breedingLocationComboBox.setValue(selectedLocation);
		}
	}

	private void populateHarvestLocation(final boolean showOnlyFavorites, final String programUUID) {
		this.breedingLocationComboBox.removeAllItems();

		if (showOnlyFavorites) {
			try {
				BreedingManagerUtil.populateWithFavoriteLocations(this.workbenchDataManager, this.germplasmDataManager,
						this.breedingLocationComboBox, null, programUUID);

			} catch (final MiddlewareQueryException e) {
				BreedingLocationField.LOG.error(e.getMessage(), e);
				MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR),
						"Error getting favorite locations!");
			}

		} else {
			this.populateLocations(programUUID);
		}

	}

	/*
	 * Fill with all locations
	 */
	private void populateLocations(final String programUUID) {

		try {
			if (this.locationType > 0) {
				this.locations = this.locationDataManager.getLocationsByType(this.locationType, programUUID);
			} else {
				this.locations = this.locationDataManager.getLocationsByUniqueID(programUUID);
			}
		} catch (final MiddlewareQueryException e) {
			BreedingLocationField.LOG.error(e.getMessage(), e);
		}

		Integer firstId = null;
		boolean hasDefault = false;
		for (final Location location : this.locations) {
			if (firstId == null) {
				firstId = location.getLocid();
			}
			this.breedingLocationComboBox.addItem(location.getLocid());
			this.breedingLocationComboBox.setItemCaption(location.getLocid(), BreedingManagerUtil.getLocationNameDisplay(location));
			if (BreedingLocationField.DEFAULT_LOCATION.equalsIgnoreCase(location.getLname())) {
				this.breedingLocationComboBox.setValue(location.getLocid());
				hasDefault = true;
			}
		}
		if (!hasDefault && firstId != null) {
			this.breedingLocationComboBox.setValue(firstId);
		}
	}

	private void launchManageWindow() {
		try {
			final Project project = this.contextUtil.getProjectInContext();

			final Window window = this.attachToWindow != null ? this.attachToWindow : this.getWindow();
			final Window manageFavoriteLocationsWindow =
					Util.launchLocationManager(this.workbenchDataManager, project.getProjectId(), window,
							this.messageSource.getMessage(Message.MANAGE_LOCATIONS));
			manageFavoriteLocationsWindow.addListener(new CloseListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void windowClose(final CloseEvent e) {
					BreedingLocationField.this.source.updateAllLocationFields();
				}
			});
		} catch (final MiddlewareQueryException e) {
			BreedingLocationField.LOG.error("Error on manageFavoriteLocations click", e);
		}
	}

	@Override
	public void setCaption(final String caption) {
		this.caption = caption;
		if (this.captionLabel != null) {
			this.captionLabel.setValue(this.caption);
		}
	}

	public boolean isDisplayFavoriteLocationsFilter() {
		return this.displayFavoriteLocationsFilter;
	}

	public void setDisplayFavoriteLocationsFilter(final boolean displayFavoriteLocationsFilter) {
		this.displayFavoriteLocationsFilter = displayFavoriteLocationsFilter;
	}

	public boolean isDisplayManageLocationLink() {
		return this.displayManageLocationLink;
	}

	public void setDisplayManageLocationLink(final boolean displayManageLocationLink) {
		this.displayManageLocationLink = displayManageLocationLink;
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

	public LocationDataManager getLocationDataManager() {
		return this.locationDataManager;
	}

	public void setLocationDataManager(final LocationDataManager locationDataManager) {
		this.locationDataManager = locationDataManager;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}
}
