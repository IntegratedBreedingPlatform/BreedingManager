
package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

@Configurable
public class AddEnvironmentalConditionsDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(AddEnvironmentalConditionsDialog.class);

	public static final String CLOSE_SCREEN_BUTTON_ID = "AddEnvironmentalConditionsDialog Close Button ID";
	public static final String APPLY_BUTTON_ID = "AddEnvironmentalConditionsDialog Apply Button ID";
	public static final String STUDY_BUTTON_ID = "AddEnvironmentalConditionsDialog Study Button ID";

	private static final String CONDITION_COLUMN_ID = "AddEnvironmentalConditionsDialog Condition Column Id";
	private static final String DESCRIPTION_COLUMN_ID = "AddEnvironmentalConditionsDialog Description Column Id";
	private static final String NUMBER_OF_ENV_COLUMN_ID = "AddEnvironmentalConditionsDialog Number of Environments Column Id";
	private static final String TAG_COLUMN_ID = "AddEnvironmentalConditionsDialog Tag Column Id";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	private final Component source;
	private final List<Integer> environmentIds;

	private Button applyButton;
	private Button cancelButton;

	private Table conditionsTable;

	public static String DELIMITER = "^^^^^^";
	private Label popupLabel;
	private final Map<String, CheckBox> checkBoxMap = new HashMap<String, CheckBox>();
	private List<String> conditionNames = new ArrayList<String>();
	private Set<TrialEnvironmentProperty> selectedProperties = new LinkedHashSet<TrialEnvironmentProperty>();
	private CheckBox tagUnTagAll;

	public AddEnvironmentalConditionsDialog(Component source, Window parentWindow, List<Integer> environmentIds) {
		this.source = source;
		this.environmentIds = environmentIds;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("780px");
		this.setHeight("520px");
		this.setResizable(false);
		this.setCaption(this.messageSource.getMessage(Message.ADD_ENVT_CONDITION_COLUMNS_LABEL));
		// center window within the browser
		this.center();

		this.popupLabel = new Label(this.messageSource.getMessage(Message.SELECTED_ENVT_CONDITIONS_WILL_BE_ADDED));

		AbsoluteLayout mainLayout = new AbsoluteLayout();
		mainLayout.setWidth("780px");
		mainLayout.setHeight("440px");

		this.initializeConditionsTable();
		this.populateConditionsTable();

		this.tagUnTagAll = new CheckBox();
		this.tagUnTagAll.setValue(true);
		this.tagUnTagAll.setImmediate(true);
		this.tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));

		mainLayout.addComponent(this.popupLabel, "top:10px;left:20px");
		mainLayout.addComponent(this.conditionsTable, "top:30px;left:20px");

		mainLayout.addComponent(this.tagUnTagAll, "top:33px;left:630px");

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		this.cancelButton = new Button();
		this.cancelButton.setData(AddEnvironmentalConditionsDialog.CLOSE_SCREEN_BUTTON_ID);
		this.cancelButton.addListener(new CloseWindowAction());

		this.applyButton = new Button();
		this.applyButton.setData(AddEnvironmentalConditionsDialog.APPLY_BUTTON_ID);
		this.applyButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this, this.source));
		this.applyButton.addListener(new CloseWindowAction());
		this.applyButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		// TODO disable
		this.applyButton.setEnabled(true);

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.applyButton);
		mainLayout.addComponent(buttonLayout, "top:410px;left:310px");

		this.addComponent(mainLayout);
	}

	public void clickCheckBoxTag(boolean val, String classname) {
		java.util.Iterator<CheckBox> checkboxes = this.checkBoxMap.values().iterator();
		while (checkboxes.hasNext()) {
			CheckBox box = checkboxes.next();
			box.setValue(val);
		}

		if (classname.equals("EnvironmentFilter")) {
			((EnvironmentFilter) this.source).reopenAddEnvironmentConditionsWindow();
		}
	}

	private void populateConditionsTable() throws MiddlewareQueryException {
		List<TrialEnvironmentProperty> properties = new ArrayList<TrialEnvironmentProperty>();

		if (this.environmentIds != null && this.environmentIds.size() > 0) {
			properties = this.crossStudyDataManager.getPropertiesForTrialEnvironments(this.environmentIds);
		}

		this.selectedProperties = new HashSet<TrialEnvironmentProperty>();
		this.conditionNames = new ArrayList<String>();

		for (TrialEnvironmentProperty environmentCondition : properties) {
			String condition = environmentCondition.getName();
			if (condition != null && !condition.isEmpty()) {
				CheckBox box = new CheckBox();
				box.setImmediate(true);
				box.setValue(true);
				box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, environmentCondition));

				Object[] itemObj =
						new Object[] {environmentCondition.getName(), environmentCondition.getDescription(),
								environmentCondition.getNumberOfEnvironments(), box};
				this.conditionsTable.addItem(itemObj, environmentCondition);

				this.selectedProperties.add(environmentCondition);
				this.conditionNames.add(condition);

				this.checkBoxMap.put(condition, box);
			}
		}
	}

	private void initializeConditionsTable() {
		this.conditionsTable = new Table();
		this.conditionsTable.setWidth("700px");
		this.conditionsTable.setHeight("350px");
		this.conditionsTable.setImmediate(true);
		this.conditionsTable.setPageLength(-1);

		this.conditionsTable.setSelectable(true);
		this.conditionsTable.setMultiSelect(true);
		this.conditionsTable.setNullSelectionAllowed(false);

		this.conditionsTable.addContainerProperty(AddEnvironmentalConditionsDialog.CONDITION_COLUMN_ID, String.class, null);
		this.conditionsTable.addContainerProperty(AddEnvironmentalConditionsDialog.DESCRIPTION_COLUMN_ID, String.class, null);
		this.conditionsTable.addContainerProperty(AddEnvironmentalConditionsDialog.NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
		this.conditionsTable.addContainerProperty(AddEnvironmentalConditionsDialog.TAG_COLUMN_ID, CheckBox.class, null);

		this.conditionsTable.setColumnHeader(AddEnvironmentalConditionsDialog.CONDITION_COLUMN_ID,
				this.messageSource.getMessage(Message.CONDITION_HEADER));
		this.conditionsTable.setColumnHeader(AddEnvironmentalConditionsDialog.DESCRIPTION_COLUMN_ID,
				this.messageSource.getMessage(Message.DESCRIPTION_HEADER));
		this.conditionsTable.setColumnHeader(AddEnvironmentalConditionsDialog.NUMBER_OF_ENV_COLUMN_ID,
				this.messageSource.getMessage(Message.NUMBER_OF_ENVIRONMENTS_HEADER));
		this.conditionsTable.setColumnHeader(AddEnvironmentalConditionsDialog.TAG_COLUMN_ID,
				this.messageSource.getMessage(Message.HEAD_TO_HEAD_TAG));

		this.conditionsTable.setColumnWidth(AddEnvironmentalConditionsDialog.CONDITION_COLUMN_ID, 116);
		this.conditionsTable.setColumnWidth(AddEnvironmentalConditionsDialog.DESCRIPTION_COLUMN_ID, 290);
		this.conditionsTable.setColumnWidth(AddEnvironmentalConditionsDialog.NUMBER_OF_ENV_COLUMN_ID, 130);
		this.conditionsTable.setColumnWidth(AddEnvironmentalConditionsDialog.TAG_COLUMN_ID, 110);

	}

	public void applyButtonClickAction() {
		// apply to previous screen the filter
	}

	public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
		sourceTable.select(itemId);
	}

	public void clickCheckBox(boolean val, TrialEnvironmentProperty environmentCondition) {
		if (val) {
			this.selectedProperties.add(environmentCondition);
		} else {
			this.selectedProperties.remove(environmentCondition);
		}
	}

	public void clickApplyButton(String classname) {
		if (classname.equals("EnvironmentFilter")) {
			((EnvironmentFilter) this.source).addEnviromentalConditionColumns(this.selectedProperties);
		}

	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.applyButton, Message.DONE);
		this.messageSource.setCaption(this.cancelButton, Message.CANCEL_LABEL);
	}
}
