
package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.domain.dms.StudyReference;
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
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class FilterStudyDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -7651767452229107837L;

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);

	public static final String CLOSE_SCREEN_BUTTON_ID = "FilterStudyDialog Close Button ID";
	public static final String APPLY_BUTTON_ID = "FilterStudyDialog Apply Button ID";
	public static final String STUDY_BUTTON_ID = "FilterStudyDialog Study Button ID";

	private static final String STUDY_NAME_COLUMN_ID = "FilterStudyDialog Study Name Column Id";
	private static final String STUDY_DESCRIPTION_COLUMN_ID = "FilterStudyDialog Study Description Column Id";
	private static final String NUMBER_OF_ENV_COLUMN_ID = "FilterStudyDialog Number of Environments Column Id";
	private static final String TAG_COLUMN_ID = "FilterStudyDialog Tag Column Id";

	private final Component source;
	private Window parentWindow;

	private Button applyButton;
	private Button cancelButton;

	private Table studyTable;

	private final Map<String, List<StudyReference>> filterStudyMap;
	public static String DELIMITER = "^^^^^^";
	private Label popupLabel;
	private final Map<String, CheckBox> checkBoxMap = new HashMap<String, CheckBox>();
	private final List<FilterLocationDto> checkFilterLocationLevel4DtoList = new ArrayList<FilterLocationDto>();
	private CheckBox tagUnTagAll;
	boolean h2hCall = true;

	private String windowName;

	public FilterStudyDialog(Component source, Window parentWindow, Map<String, List<StudyReference>> filterStudyMap) {
		this.source = source;
		this.parentWindow = parentWindow;
		this.filterStudyMap = filterStudyMap;
	}

	public FilterStudyDialog(Component source, Window parentWindow, Map<String, List<StudyReference>> filterStudyMap, String windowName) {
		this.source = source;
		this.parentWindow = parentWindow;
		this.filterStudyMap = filterStudyMap;
		this.windowName = windowName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("785px");
		this.setHeight("530px");
		this.setResizable(false);
		this.setCaption("Filter by Study");
		// center window within the browser
		this.center();

		this.popupLabel = new Label("Specify filter by checking or unchecking studies.");

		AbsoluteLayout mainLayout = new AbsoluteLayout();
		mainLayout.setWidth("780px");
		mainLayout.setHeight("450px");

		this.initializeStudyTable();

		this.showStudyRows();

		this.tagUnTagAll = new CheckBox();
		this.tagUnTagAll.setValue(true);
		this.tagUnTagAll.setImmediate(true);
		this.tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));

		mainLayout.addComponent(this.popupLabel, "top:10px;left:20px");
		mainLayout.addComponent(this.studyTable, "top:30px;left:20px");

		mainLayout.addComponent(this.tagUnTagAll, "top:33px;left:630px");

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		this.cancelButton = new Button("Cancel");
		this.cancelButton.setData(FilterStudyDialog.CLOSE_SCREEN_BUTTON_ID);
		this.cancelButton.addListener(new CloseWindowAction());

		String buttonlabel = "Apply";

		this.applyButton = new Button(buttonlabel);
		this.applyButton.setData(FilterStudyDialog.APPLY_BUTTON_ID);
		this.applyButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this, this.source));
		this.applyButton.addListener(new CloseWindowAction());
		this.applyButton.setEnabled(false);
		this.applyButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.applyButton);
		mainLayout.addComponent(buttonLayout, "top:420px;left:415px");

		this.addComponent(mainLayout);
	}

	public void clickCheckBoxTag(boolean val, String className) {

		java.util.Iterator<CheckBox> checkboxes = this.checkBoxMap.values().iterator();
		while (checkboxes.hasNext()) {
			CheckBox box = checkboxes.next();
			box.setValue(val);
		}
		this.setupApplyButton();

		if (className.equals("EnvironmentFilter")) {
			((EnvironmentFilter) this.source).reopenFilterStudyWindow();
		}
	}

	private void showStudyRows() {
		for (String studyKey : this.filterStudyMap.keySet()) {
			List<StudyReference> studyReferenceList = this.filterStudyMap.get(studyKey);
			StudyReference studyRef = studyReferenceList.get(0);

			CheckBox box = new CheckBox();
			box.setImmediate(true);
			FilterLocationDto filterLocationDto = new FilterLocationDto(null, null, null, studyRef.getName(), 4);

			box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));

			Button studyNameLink = new Button(studyRef.getName());
			studyNameLink.setImmediate(true);
			studyNameLink.setStyleName(BaseTheme.BUTTON_LINK);
			studyNameLink.setData(FilterStudyDialog.STUDY_BUTTON_ID);
			studyNameLink.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this, null, null, studyRef.getId()));

			Object[] itemObj = new Object[] {studyNameLink, studyRef.getDescription(), studyReferenceList.size(), box};
			this.studyTable.addItem(itemObj, studyKey);
			this.checkBoxMap.put(studyKey, box);
			box.setValue(true);
		}
	}

	public void showStudyInfo(Integer studyId) {
		if (this.parentWindow == null && this.windowName != null) {
			this.parentWindow = this.getApplication().getWindow(this.windowName);
		}

		StudyInfoDialog studyInfoDialog = new StudyInfoDialog(this, this.parentWindow, studyId, this.h2hCall);
		studyInfoDialog.addStyleName(Reindeer.WINDOW_LIGHT);
		this.parentWindow.addWindow(studyInfoDialog);
	}

	private void initializeStudyTable() {
		this.studyTable = new Table();
		this.studyTable.setWidth("700px");
		this.studyTable.setHeight("350px");
		this.studyTable.setImmediate(true);
		this.studyTable.setPageLength(-1);

		this.studyTable.setSelectable(true);
		this.studyTable.setMultiSelect(true);
		this.studyTable.setNullSelectionAllowed(false);

		this.studyTable.addContainerProperty(FilterStudyDialog.STUDY_NAME_COLUMN_ID, Button.class, null);
		this.studyTable.addContainerProperty(FilterStudyDialog.STUDY_DESCRIPTION_COLUMN_ID, String.class, null);
		this.studyTable.addContainerProperty(FilterStudyDialog.NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
		this.studyTable.addContainerProperty(FilterStudyDialog.TAG_COLUMN_ID, CheckBox.class, null);

		this.studyTable.setColumnHeader(FilterStudyDialog.STUDY_NAME_COLUMN_ID, "Study Name");
		this.studyTable.setColumnHeader(FilterStudyDialog.STUDY_DESCRIPTION_COLUMN_ID, "Study Title");
		this.studyTable.setColumnHeader(FilterStudyDialog.NUMBER_OF_ENV_COLUMN_ID, "# of Environments");
		this.studyTable.setColumnHeader(FilterStudyDialog.TAG_COLUMN_ID, "Tag");

		this.studyTable.setColumnWidth(FilterStudyDialog.STUDY_NAME_COLUMN_ID, 111);
		this.studyTable.setColumnWidth(FilterStudyDialog.STUDY_DESCRIPTION_COLUMN_ID, 295);
		this.studyTable.setColumnWidth(FilterStudyDialog.NUMBER_OF_ENV_COLUMN_ID, 130);
		this.studyTable.setColumnWidth(FilterStudyDialog.TAG_COLUMN_ID, 110);

	}

	public void applyButtonClickAction() {
		// apply to previous screen the filter
	}

	public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
		sourceTable.select(itemId);
	}

	public void clickCheckBox(boolean val, FilterLocationDto filterLocationDto) {
		List<FilterLocationDto> tempList = new ArrayList<FilterLocationDto>();
		if (filterLocationDto.getLevel() == 4) {
			tempList = this.checkFilterLocationLevel4DtoList;
		}

		if (val) {
			tempList.add(filterLocationDto);
		} else {
			tempList.remove(filterLocationDto);
		}
		this.setupApplyButton();
	}

	public void clickApplyButton(String classname) {
		if (classname.equals("EnvironmentFilter")) {
			((EnvironmentFilter) this.source).clickFilterByStudyApply(this.checkFilterLocationLevel4DtoList);
		}
	}

	public void initializeButtons() {
		this.setupApplyButton();
	}

	private void setupApplyButton() {
		if (this.applyButton != null) {
			if (!this.checkFilterLocationLevel4DtoList.isEmpty()) {
				this.applyButton.setEnabled(true);
			} else {
				this.applyButton.setEnabled(false);
			}
		}
	}

	@Override
	public void updateLabels() {

	}
}
