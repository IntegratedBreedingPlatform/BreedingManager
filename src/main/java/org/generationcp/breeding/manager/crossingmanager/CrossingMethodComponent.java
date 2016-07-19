
package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.crossingmanager.constants.CrossType;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class CrossingMethodComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean,
		InternationalizableComponent {

	private static final long serialVersionUID = -8847158352169444182L;

	public static final String MAKE_CROSS_BUTTON_ID = "Make Cross Button";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private Panel crossingMethodPanel;
	private Label crossingMethodLabel;

	private ComboBox crossingMethodComboBox;
	private CheckBox chkBoxMakeReciprocalCrosses;
	private CheckBox chkBoxExcludeSelf;

	private Button btnMakeCross;

	private final CrossingManagerMakeCrossesComponent makeCrossesMain;
	private MakeCrossesParentsComponent parentsComponent;

	public CrossingMethodComponent(CrossingManagerMakeCrossesComponent makeCrossesMain) {
		super();
		this.makeCrossesMain = makeCrossesMain;
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

	@Override
	public void instantiateComponents() {
		this.crossingMethodLabel = new Label(this.messageSource.getMessage(Message.CROSSING_METHOD));
		this.crossingMethodLabel.setWidth("200px");
		this.crossingMethodLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.crossingMethodLabel.addStyleName(AppConstants.CssStyles.BOLD);

		this.crossingMethodComboBox = new ComboBox();
		this.crossingMethodComboBox.setNewItemsAllowed(false);
		this.crossingMethodComboBox.setNullSelectionAllowed(false);
		this.crossingMethodComboBox.setWidth("400px");

		this.chkBoxMakeReciprocalCrosses = new CheckBox(this.messageSource.getMessage(Message.MAKE_CROSSES_CHECKBOX_LABEL));
		this.chkBoxExcludeSelf = new CheckBox(this.messageSource.getMessage(Message.EXCLUDE_SELF_LABEL));

		this.btnMakeCross = new Button(this.messageSource.getMessage(Message.MAKE_CROSSES_BUTTON_LABEL));
		this.btnMakeCross.setData(CrossingMethodComponent.MAKE_CROSS_BUTTON_ID);
		this.btnMakeCross.addStyleName(Bootstrap.Buttons.INFO.styleName());
	}

	@Override
	public void initializeValues() {
		this.crossingMethodComboBox.addItem(CrossType.PLEASE_CHOOSE);
		this.crossingMethodComboBox.setItemCaption(CrossType.PLEASE_CHOOSE,
				this.messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_PLEASE_CHOOSE));
		this.crossingMethodComboBox.addItem(CrossType.MULTIPLY);
		this.crossingMethodComboBox.setItemCaption(CrossType.MULTIPLY,
				this.messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_ONE_LABEL));
		this.crossingMethodComboBox.addItem(CrossType.TOP_TO_BOTTOM);
		this.crossingMethodComboBox.setItemCaption(CrossType.TOP_TO_BOTTOM,
				this.messageSource.getMessage(Message.MAKE_CROSSES_OPTION_GROUP_ITEM_TWO_LABEL));
		this.crossingMethodComboBox.select(CrossType.PLEASE_CHOOSE);
	}

	@Override
	public void addListeners() {
		this.btnMakeCross.addListener(new CrossingManagerImportButtonClickListener(this));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		VerticalLayout layoutCrossOption = new VerticalLayout();
		layoutCrossOption.setWidth("460px");
		layoutCrossOption.setSpacing(true);
		layoutCrossOption.setMargin(true);
		layoutCrossOption.addComponent(this.crossingMethodComboBox);
		layoutCrossOption.addComponent(this.chkBoxMakeReciprocalCrosses);
		layoutCrossOption.addComponent(this.chkBoxExcludeSelf);
		layoutCrossOption.addComponent(this.btnMakeCross);

		this.crossingMethodPanel = new Panel();
		this.crossingMethodPanel.setWidth("460px");
		this.crossingMethodPanel.setLayout(layoutCrossOption);
		this.crossingMethodPanel.addStyleName("section_panel_layout");

		// provides this slot for an icon
		HeaderLabelLayout crossingMethodLayout = new HeaderLabelLayout(null, this.crossingMethodLabel);
		this.addComponent(crossingMethodLayout);
		this.addComponent(this.crossingMethodPanel);
	}

	public void makeCrossButtonAction() {
		// TODO temporary fix, will have a new fix approach soon
		if (this.makeCrossesMain.getModeView().equals(ModeView.INVENTORY_VIEW)) {
			String message = "Please switch to list view first before making crosses.";
			MessageNotifier.showError(this.getWindow(), "Warning!", message);
		} else {
			CrossType type = (CrossType) this.crossingMethodComboBox.getValue();
			if (CrossType.PLEASE_CHOOSE.equals(type)) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
						this.messageSource.getMessage(Message.PLEASE_CHOOSE_CROSSING_METHOD));
			} else {
				this.parentsComponent = this.makeCrossesMain.getParentsComponent();

				Table femaleParents = this.parentsComponent.getFemaleTable();
				Table maleParents = this.parentsComponent.getMaleTable();

				List<GermplasmListEntry> femaleList = this.parentsComponent.getCorrectSortedValue(femaleParents);
				List<GermplasmListEntry> maleList = this.parentsComponent.getCorrectSortedValue(maleParents);
				this.updateParentsDesignationToPreferredName(femaleList, maleList);
				this.parentsComponent.updateFemaleListNameForCrosses();
				this.parentsComponent.updateMaleListNameForCrosses();

				this.makeCrossesMain.makeCrossButtonAction(femaleList, maleList, this.parentsComponent.getFemaleListNameForCrosses(),
						this.parentsComponent.getMaleListNameForCrosses(), type, this.chkBoxMakeReciprocalCrosses.booleanValue(),
						this.chkBoxExcludeSelf.booleanValue());
			}
		}
	}

	void updateParentsDesignationToPreferredName(final List<GermplasmListEntry> femaleList, final List<GermplasmListEntry> maleList) {
		final List<Integer> gids = this.getGidsOfParents(femaleList, maleList);
		final Map<Integer, String> gidToPreferredNameMap = this.germplasmDataManager.getPreferredNamesByGids(gids);
		this.updateDesignationToPreferredName(femaleList, gidToPreferredNameMap);
		this.updateDesignationToPreferredName(maleList, gidToPreferredNameMap);
	}
	
	private void updateDesignationToPreferredName(final List<GermplasmListEntry> listEntries, final Map<Integer, String> gidToPreferredNameMap) {
		for (final GermplasmListEntry germplasmListEntry : listEntries) {
			Integer gid = germplasmListEntry.getGid();
			String preferredName = gidToPreferredNameMap.get(gid);
			germplasmListEntry.setDesignation(preferredName);
		}
	}

	private List<Integer> getGidsOfParents(
			final List<GermplasmListEntry> femaleList, final List<GermplasmListEntry> maleList) {
		final Set<Integer> uniqueGids = new HashSet<>();
		final Set<Integer> femaleGids = this.getGids(femaleList);
		final Set<Integer> maleGids = this.getGids(maleList);
		uniqueGids.addAll(femaleGids);
		uniqueGids.addAll(maleGids);
		
		final List<Integer> gidList = new ArrayList<>();
		gidList.addAll(uniqueGids);
		return gidList;
	}

	private Set<Integer> getGids(final List<GermplasmListEntry> listEntries) {
		final Set<Integer> gids = new HashSet<>();
		for (final GermplasmListEntry germplasmListEntry : listEntries) {
			final Integer gid = germplasmListEntry.getGid();
			gids.add(gid);
		}
		return gids;
	}

	
	public void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
	
	
}
