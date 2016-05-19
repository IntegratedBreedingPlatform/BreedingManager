
package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.Window;

@Configurable
public class FilterLocationDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -7651767452229107837L;

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);

	public static final String CLOSE_SCREEN_BUTTON_ID = "FilterLocationDialog Close Button ID";
	public static final String APPLY_BUTTON_ID = "FilterLocationDialog Apply Button ID";

	private static final String COUNTRY_LOCATION_COLUMN_ID = "FilterLocationDialog Country/Location Column Id";
	private static final String NUMBER_OF_ENV_COLUMN_ID = "FilterLocationDialog Number of Environments Column Id";
	private static final String TAG_COLUMN_ID = "FilterLocationDialog Tag Column Id";
	private static final String TAG_ALL = "FilterLocationDialog TAG_ALL Column Id";

	private final Component source;

	private Button applyButton;
	private Button cancelButton;

	private TreeTable locationTreeTable;

	private final Map<String, FilterByLocation> filterLocationCountryMap;
	public static String DELIMITER = "^^^^^^";
	private Label popupLabel;
	private List<FilterLocationDto> checkFilterLocationLevel1DtoList = new ArrayList<FilterLocationDto>();
	private List<FilterLocationDto> checkFilterLocationLevel3DtoList = new ArrayList<FilterLocationDto>();
	private final Map<String, CheckBox> locationCountryCheckBoxMap = new HashMap<String, CheckBox>();
	private final Map<String, FilterLocationDto> locationCountryFilterDtoMap = new HashMap<String, FilterLocationDto>();
	private final Map<String, List<String>> countryLocationMapping = new HashMap<String, List<String>>();
	private CheckBox tagUnTagAll;

	public FilterLocationDialog(Component source, Window parentWindow, Map<String, FilterByLocation> filterLocationCountryMap) {
		this.source = source;
		this.filterLocationCountryMap = filterLocationCountryMap;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("980px");
		this.setHeight("530px");
		this.setResizable(false);
		this.setCaption("Filter by Location");
		// center window within the browser
		this.center();

		this.popupLabel = new Label("Specify filter by checking or unchecking countries/locations.");

		AbsoluteLayout mainLayout = new AbsoluteLayout();
		mainLayout.setWidth("1000px");
		mainLayout.setHeight("450px");

		this.initializeCountryLocationTable();

		this.showCountryLocationRows();

		this.tagUnTagAll = new CheckBox();
		this.tagUnTagAll.setValue(true);
		this.tagUnTagAll.setImmediate(true);
		this.tagUnTagAll.setData(FilterLocationDialog.TAG_ALL);
		this.tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));

		mainLayout.addComponent(this.popupLabel, "top:10px;left:20px");
		mainLayout.addComponent(this.locationTreeTable, "top:30px;left:20px");

		mainLayout.addComponent(this.tagUnTagAll, "top:33px;left:810px");

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		this.cancelButton = new Button("Cancel");
		this.cancelButton.setData(FilterLocationDialog.CLOSE_SCREEN_BUTTON_ID);
		this.cancelButton.addListener(new CloseWindowAction());

		String buttonlabel = "Apply";

		this.applyButton = new Button(buttonlabel);
		this.applyButton.setData(FilterLocationDialog.APPLY_BUTTON_ID);
		this.applyButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this, this.source));
		this.applyButton.addListener(new CloseWindowAction());
		this.applyButton.setEnabled(false);
		this.applyButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.applyButton);
		mainLayout.addComponent(buttonLayout, "top:420px;left:415px");

		this.addComponent(mainLayout);
	}

	private void showCountryLocationRows() {
		for (String countryName : this.filterLocationCountryMap.keySet()) {
			FilterByLocation filterByLocation = this.filterLocationCountryMap.get(countryName);
			FilterLocationDto filterLocationDto = new FilterLocationDto(countryName, null, null, null, 1);
			CheckBox box = new CheckBox();
			box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));
			box.setValue(true);
			box.setImmediate(true);

			Object countryObj =
					this.locationTreeTable.addItem(new Object[] {countryName, filterByLocation.getNumberOfEnvironmentForCountry(), box},
							countryName);
			this.locationCountryCheckBoxMap.put(countryName, box);
			this.locationCountryFilterDtoMap.put(countryName, filterLocationDto);
			List<String> keyList = new ArrayList<String>();
			for (String locationNames : filterByLocation.getListOfLocationNames()) {
				CheckBox boxLocation = new CheckBox();
				FilterLocationDto filterLocationDto1 = new FilterLocationDto(countryName, null, locationNames, null, 3);
				boxLocation.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto1));
				boxLocation.setImmediate(true);
				String key = countryName + FilterLocationDialog.DELIMITER + locationNames;
				boxLocation.setValue(true);
				Object locationObj =
						this.locationTreeTable
								.addItem(new Object[] {locationNames, filterByLocation.getNumberOfEnvironmentForLocation(locationNames),
										boxLocation}, key);

				this.locationTreeTable.setParent(locationObj, countryObj);
				this.locationCountryCheckBoxMap.put(key, boxLocation);
				keyList.add(key);
				this.locationCountryFilterDtoMap.put(key, filterLocationDto1);
				this.locationTreeTable.setChildrenAllowed(locationObj, false);

			}
			this.locationTreeTable.setCollapsed(countryObj, true);
			this.countryLocationMapping.put(countryName, keyList);
		}
	}

	private void initializeCountryLocationTable() {
		this.locationTreeTable = new TreeTable();
		this.locationTreeTable.setWidth("900px");
		this.locationTreeTable.setHeight("350px");
		this.locationTreeTable.setImmediate(true);
		this.locationTreeTable.setPageLength(-1);

		this.locationTreeTable.setSelectable(true);
		this.locationTreeTable.setMultiSelect(true);
		this.locationTreeTable.setNullSelectionAllowed(false);

		this.locationTreeTable.addContainerProperty(FilterLocationDialog.COUNTRY_LOCATION_COLUMN_ID, String.class, null);
		this.locationTreeTable.addContainerProperty(FilterLocationDialog.NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
		this.locationTreeTable.addContainerProperty(FilterLocationDialog.TAG_COLUMN_ID, CheckBox.class, null);

		this.locationTreeTable.setColumnHeader(FilterLocationDialog.COUNTRY_LOCATION_COLUMN_ID, "Country/Location");
		this.locationTreeTable.setColumnHeader(FilterLocationDialog.NUMBER_OF_ENV_COLUMN_ID, "# of Environments");
		this.locationTreeTable.setColumnHeader(FilterLocationDialog.TAG_COLUMN_ID, "Tag");

		this.locationTreeTable.setColumnWidth(FilterLocationDialog.COUNTRY_LOCATION_COLUMN_ID, 597);
		this.locationTreeTable.setColumnWidth(FilterLocationDialog.NUMBER_OF_ENV_COLUMN_ID, 130);
		this.locationTreeTable.setColumnWidth(FilterLocationDialog.TAG_COLUMN_ID, 115);

	}

	public void applyButtonClickAction() {
		// apply to previous screen the filter
	}

	public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
		sourceTable.select(itemId);
	}

	public void clickCheckBox(boolean val, FilterLocationDto filterLocationDto) {

		if (filterLocationDto.getLevel() == 1) {
			if (val) {
				// we check all the location
				List<String> locationList = this.countryLocationMapping.get(filterLocationDto.getCountryName());
				if (locationList != null) {
					for (String locKey : locationList) {
						CheckBox checkBox = this.locationCountryCheckBoxMap.get(locKey);
						checkBox.setValue(true);
					}
				}
			} else {
				List<String> locationList = this.countryLocationMapping.get(filterLocationDto.getCountryName());
				if (locationList != null) {
					for (String locKey : locationList) {
						CheckBox checkBox = this.locationCountryCheckBoxMap.get(locKey);
						checkBox.setValue(false);
					}
				}
			}
		} else if (filterLocationDto.getLevel() == 3) {
			if (val) {

				Map<CheckBox, Boolean> prevStateMap = new HashMap<CheckBox, Boolean>();
				List<String> locationList = this.countryLocationMapping.get(filterLocationDto.getCountryName());
				if (locationList != null) {
					for (String locKey : locationList) {
						CheckBox checkBox = this.locationCountryCheckBoxMap.get(locKey);
						if ((Boolean) checkBox.getValue() == false) {
							prevStateMap.put(checkBox, (Boolean) checkBox.getValue());
						}
					}
				}

				CheckBox checkBoxCountry = this.locationCountryCheckBoxMap.get(filterLocationDto.getCountryName());
				checkBoxCountry.setValue(true);
				Iterator<CheckBox> iter = prevStateMap.keySet().iterator();
				while (iter.hasNext()) {
					CheckBox temp = iter.next();
					temp.setValue(prevStateMap.get(temp));
				}

			} else {
				// we check all the location
				List<String> locationList = this.countryLocationMapping.get(filterLocationDto.getCountryName());
				boolean isAtLeast1Check = false;
				if (locationList != null) {
					for (String locKey : locationList) {
						CheckBox checkBox = this.locationCountryCheckBoxMap.get(locKey);
						if ((Boolean) checkBox.getValue()) {
							isAtLeast1Check = true;
						}
					}
				}
				if (isAtLeast1Check == false) {
					CheckBox checkBox = this.locationCountryCheckBoxMap.get(filterLocationDto.getCountryName());
					checkBox.setValue(false);
				}
			}
		}

		this.setupApplyButton();
	}

	public void clickCheckBoxTag(boolean val, String className) {
		for (String sKey : this.countryLocationMapping.keySet()) {
			CheckBox temp = this.locationCountryCheckBoxMap.get(sKey);
			temp.setValue(val);
		}
		this.setupApplyButton();

		if (className.equals("EnvironmentFilter")) {
			((EnvironmentFilter) this.source).reopenFilterWindow();
		}

	}

	public void clickApplyButton(String classname) {

		this.checkFilterLocationLevel1DtoList = new ArrayList<FilterLocationDto>();
		this.checkFilterLocationLevel3DtoList = new ArrayList<FilterLocationDto>();
		for (String sKey : this.locationCountryCheckBoxMap.keySet()) {
			CheckBox temp = this.locationCountryCheckBoxMap.get(sKey);
			if ((Boolean) temp.getValue()) {
				FilterLocationDto dto = this.locationCountryFilterDtoMap.get(sKey);
				if (dto.getLevel() == 1) {
					this.checkFilterLocationLevel1DtoList.add(dto);
				} else if (dto.getLevel() == 3) {
					this.checkFilterLocationLevel3DtoList.add(dto);
				}
			}
		}

		if (classname.equals("EnvironmentFilter")) {
			((EnvironmentFilter) this.source).clickFilterByLocationApply(this.checkFilterLocationLevel1DtoList,
					this.checkFilterLocationLevel3DtoList);
		}
	}

	public void initializeButtons() {
		this.setupApplyButton();
	}

	private void setupApplyButton() {
		if (this.applyButton != null) {
			for (CheckBox checkBox : this.locationCountryCheckBoxMap.values()) {
				if ((Boolean) checkBox.getValue()) {
					this.applyButton.setEnabled(true);
					break;
				} else {
					this.applyButton.setEnabled(false);
				}
			}
		}
	}

	@Override
	public void updateLabels() {

	}
}
