/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.germplasm;

import java.util.ArrayList;
import java.util.Iterator;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.TraitDataIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.NumericRange;
import org.generationcp.middleware.pojos.TraitCombinationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class SearchGermplasmByPhenotypicTab extends GridLayout implements InitializingBean, InternationalizableComponent{

    private static final Logger LOG = LoggerFactory.getLogger(SearchGermplasmByPhenotypicTab.class);
    private static final long serialVersionUID = 455865362407450432L;

    public static final String ADD_CRITERIA_BUTTON_ID = "SearchGermplasmByPhenotypicTab Add Criteria Button";
    public static final String DELETE_BUTTON_ID = "SearchGermplasmByPhenotypicTab Delete Button";
    public static final String DELETE_ALL_BUTTON_ID = "SearchGermplasmByPhenotypicTab Delete All Button";
    public static final String SEARCH_BUTTON_ID = "SearchGermplasmByPhenotypicTab Search Button";

    private VerticalLayout componentTrait;
    private VerticalLayout componentTtraitValueInput;
    private Table traitTable;
    private Table scaleTable;
    private Table traitMethodTable;
    private Table scaleValueTable;
    private Table criteriaTable;
    private Table searchResultTable;
    private IndexedContainer dataSourceTrait;
    private IndexedContainer dataSourceScale;
    private IndexedContainer dataSourceTraitMethod;
    private IndexedContainer dataSourceScaleValue;
    private IndexedContainer dataSourceSearchCriteria;
    private IndexedContainer dataSourceSearchResult;
    private TextField txtValueInput;
    private Button btnAddCriteria;
    private Button btnSearch;
    private Button btnDelete;
    private Button btnDeleteAll;
    private Label rangeInstructionLabel;
    private Label valueOptionsInstuctionLabel;
    private Label step1Label;
    private Label step2Label;
    private Label step3Label;
    private Label step4Label;
    private Label step5Label;
    private Label finalStepLabel;
    private Label mainLabel;
    private GidByPhenotypicQueries gidsByPhenotypic;
    private TraitDataIndexContainer dataIndexContainer;
    private Window parentWindow;

    private int traitID;
    private int flagScale;
    private int flagResult = 0;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public SearchGermplasmByPhenotypicTab(GidByPhenotypicQueries gidsByPhenotypicParam, TraitDataIndexContainer dataIndexContainerParam,
            Window parentWindow) {
        this.gidsByPhenotypic = gidsByPhenotypicParam;
        this.dataIndexContainer = dataIndexContainerParam;
        this.parentWindow = parentWindow;
    }

    // Trait Table

    private void displayTraitTable() throws InternationalizableException {
        dataSourceTrait = dataIndexContainer.getAllTrait();
        traitTable = new Table("", dataSourceTrait);

        // set a style name, so we can style rows and cells
        traitTable.setStyleName("iso3166");
        // size
        traitTable.setWidth("600px");
        traitTable.setHeight("200px");

        // selectable
        traitTable.setSelectable(true);
        traitTable.setMultiSelect(false);
        traitTable.setImmediate(true); // react at once when something is
        // selected

        // turn on column reordering and collapsing
        traitTable.setColumnReorderingAllowed(true);
        traitTable.setColumnCollapsingAllowed(true);

        // set column headers
        traitTable.setColumnHeaders(new String[] { "TraitID", "Trait Name", "Description" });
        traitTable.setVisibleColumns(new String[] { "traitName", "traitDesc" });

        // Column alignment
        traitTable.setCaption("Traits");

        traitTable.setValue(traitTable.firstItemId());
        Object itemID = traitTable.getValue();
        traitID = Integer.valueOf(traitTable.getItem(itemID).getItemProperty("traitID").toString());

        componentTrait.addComponent(step1Label);
        componentTrait.addComponent(traitTable);

        displayScaleTable(traitID);
        displayMethodTable(traitID);
    }

    // Scale Table
    private void displayScaleTable(int traitID) throws InternationalizableException {
        dataSourceScale = dataIndexContainer.getScaleByTraitID(traitID);
        scaleTable = new Table("", dataSourceScale);

        // set a style name, so we can style rows and cells
        scaleTable.setStyleName("iso3166");
        // size
        scaleTable.setWidth("600px");
        scaleTable.setHeight("200px");

        // selectable
        scaleTable.setSelectable(true);
        scaleTable.setMultiSelect(false);
        scaleTable.setImmediate(true); // react at once when something is
        // selected

        // turn on column reordering and collapsing
        scaleTable.setColumnReorderingAllowed(true);
        scaleTable.setColumnCollapsingAllowed(true);

        // set column headers
        scaleTable.setColumnHeaders(new String[] { "ScaleID", "Name", "Type" });
        scaleTable.setVisibleColumns(new String[] { "scaleName", "scaleType" });

        // Column alignment
        scaleTable.setCaption("Scales");

        scaleSetFirstRowSelected();
        displayInputValueForm();
        displayScaleValueTable();
        updateScaleValueInputDisplay(getScaleType(), getScaleID());

        componentTrait.addComponent(step2Label);
        componentTrait.addComponent(scaleTable);
    }

    // TraitMethod Table
    private void displayMethodTable(int traitID) {
        dataSourceTraitMethod = dataIndexContainer.getMethodTraitID(traitID);
        traitMethodTable = new Table("", dataSourceTraitMethod);

        // set a style name, so we can style rows and cells
        traitMethodTable.setStyleName("iso3166");
        // size
        traitMethodTable.setWidth("600px");
        traitMethodTable.setHeight("200px");

        // selectable
        traitMethodTable.setSelectable(true);
        traitMethodTable.setMultiSelect(false);
        traitMethodTable.setImmediate(true); // react at once when something is
        // selected

        // turn on column reordering and collapsing
        traitMethodTable.setColumnReorderingAllowed(true);
        traitMethodTable.setColumnCollapsingAllowed(true);

        // set column headers
        traitMethodTable.setColumnHeaders(new String[] { "ID", "Name", "Description" });
        traitMethodTable.setVisibleColumns(new String[] { "methodName", "methodDescription" });

        // Column alignment
        traitMethodTable.setCaption("Methods");

        traitMethodTable.setValue(traitMethodTable.firstItemId());

        componentTrait.addComponent(step3Label);
        componentTrait.addComponent(traitMethodTable);
    }

    private void displayScaleValueTable() throws InternationalizableException {
        IndexedContainer dataSourceScaleValue = dataIndexContainer.getValueByScaleID(-1);
        scaleValueTable = new Table("", dataSourceScaleValue);

        // set a style name, so we can style rows and cells
        scaleValueTable.setStyleName("iso3166");
        // size
        scaleValueTable.setWidth("400px");
        scaleValueTable.setHeight("150px");

        // selectable
        scaleValueTable.setSelectable(true);
        scaleValueTable.setMultiSelect(false);
        scaleValueTable.setImmediate(true); // react at once when something is
        // selected

        // set column headers
        scaleValueTable.setColumnHeaders(new String[] { "Select", "Value Description", "Value" });

        // Column alignment
        scaleValueTable.setCaption("Value Options");

        valueOptionsInstuctionLabel.setVisible(false);
        componentTtraitValueInput.addComponent(valueOptionsInstuctionLabel);
        scaleValueTable.setVisible(false);
        componentTtraitValueInput.addComponent(scaleValueTable);
    }

    private void displaySearchCriteria() {
        dataSourceSearchCriteria = dataIndexContainer.addSearchCriteria();
        criteriaTable = new Table("", dataSourceSearchCriteria);

        // set a style name, so we can style rows and cells
        criteriaTable.setStyleName("iso3166");
        // size
        criteriaTable.setWidth("500px");
        criteriaTable.setHeight("200px");

        // selectable
        criteriaTable.setSelectable(true);
        criteriaTable.setMultiSelect(false);
        criteriaTable.setImmediate(true); // react at once when something is
        // selected

        // turn on column reordering and collapsing
        criteriaTable.setColumnReorderingAllowed(true);
        criteriaTable.setColumnCollapsingAllowed(true);

        // set column headers
        criteriaTable.setColumnHeaders(new String[] { "TraitID", "ScaleID", "MethodID", "Trait Name", "Scale Name", "Method Name", "Value",
                "Scale Datatype", "Scale Discrete Value" });
        criteriaTable.setVisibleColumns(new Object[] { "traitName", "scaleName", "methodName", "criteriaValue", "scaleDiscreteValue" });

        // Column alignment
        criteriaTable.setCaption("Search Criteria");
        componentTtraitValueInput.addComponent(criteriaTable);
    }

    private void displayGidsToResultTable(ArrayList<Integer> gids) {

        dataSourceSearchResult = dataIndexContainer.addGidsResult(gids);
        searchResultTable = new Table("", dataSourceSearchResult);

        // set a style name, so we can style rows and cells
        searchResultTable.setStyleName("iso3166");
        // size
        searchResultTable.setWidth("200px");
        searchResultTable.setHeight("400px");

        // selectable
        searchResultTable.setSelectable(true);
        searchResultTable.setMultiSelect(false);
        searchResultTable.setImmediate(true); // react at once when something is
        // selected

        // turn on column reordering and collapsing
        searchResultTable.setColumnReorderingAllowed(true);
        searchResultTable.setColumnCollapsingAllowed(true);

        // set column headers
        searchResultTable.setColumnHeaders(new String[] { "GID" });
    }

    private void displayInputValueForm() {

        componentTtraitValueInput.addComponent(step4Label);
        txtValueInput = new TextField("Value");
        txtValueInput.setEnabled(false);
        txtValueInput.setVisible(false);
        componentTtraitValueInput.addComponent(txtValueInput);
        rangeInstructionLabel.setVisible(false);
        componentTtraitValueInput.addComponent(rangeInstructionLabel);
    }

    private void updateScaleValueInputDisplay(String scaleType, int scaleID) throws InternationalizableException {
        if (scaleType.equals("discrete")) {
            dataSourceScaleValue = dataIndexContainer.getValueByScaleID(scaleID);
            scaleValueTable.setContainerDataSource(dataSourceScaleValue);
            scaleValueTable.setVisibleColumns(new String[] { "select", "Value" });
            valueOptionsInstuctionLabel.setVisible(true);
            scaleValueTable.setEnabled(true);
            scaleValueTable.setVisible(true);
            rangeInstructionLabel.setVisible(false);
            txtValueInput.setVisible(false);
            txtValueInput.setEnabled(false);
            flagScale = 1;
            txtValueInput.setValue("");
        } else {
            valueOptionsInstuctionLabel.setVisible(false);
            scaleValueTable.removeAllItems();
            scaleValueTable.setEnabled(false);
            scaleValueTable.setVisible(false);
            txtValueInput.setVisible(true);
            txtValueInput.setEnabled(true);
            rangeInstructionLabel.setVisible(true);
            flagScale = 2;
        }
    }

    private int getScaleID() {
        Object itemID = scaleTable.getValue();
        int scaleID = Integer.valueOf(scaleTable.getItem(itemID).getItemProperty("scaleID").toString());
        return scaleID;
    }

    private String getScaleType() {
        Object itemID = scaleTable.getValue();
        String scaleType = scaleTable.getItem(itemID).getItemProperty("scaleType").toString();
        return scaleType;
    }

    private void scaleSetFirstRowSelected() {
        scaleTable.setValue(scaleTable.firstItemId());
    }

    private void addToCriteriaTable(String criteriaValue, String scaleDiscreteValue) {

        int traitID = Integer.valueOf(traitTable.getItem(traitTable.getValue()).getItemProperty("traitID").toString());
        String tName = traitTable.getItem(traitTable.getValue()).getItemProperty("traitName").toString();
        int scaleID = Integer.valueOf(scaleTable.getItem(scaleTable.getValue()).getItemProperty("scaleID").toString());
        String sName = scaleTable.getItem(scaleTable.getValue()).getItemProperty("scaleName").toString();
        int methodID = Integer.valueOf(traitMethodTable.getItem(traitMethodTable.getValue()).getItemProperty("methodID").toString());
        String mName = traitMethodTable.getItem(traitMethodTable.getValue()).getItemProperty("methodName").toString();
        String scaleType = scaleTable.getItem(scaleTable.getValue()).getItemProperty("scaleType").toString();

        Object itemId = dataSourceSearchCriteria.addItem();
        Item item = dataSourceSearchCriteria.getItem(itemId);
        item.getItemProperty("traitID").setValue(traitID);
        item.getItemProperty("scaleID").setValue(scaleID);
        item.getItemProperty("methodID").setValue(methodID);
        item.getItemProperty("traitName").setValue(tName);
        item.getItemProperty("scaleName").setValue(sName);
        item.getItemProperty("methodName").setValue(mName);
        item.getItemProperty("criteriaValue").setValue(criteriaValue);
        item.getItemProperty("scaleType").setValue(scaleType);
        item.getItemProperty("scaleDiscreteValue").setValue(scaleDiscreteValue);
        btnSearch.setEnabled(true);
    }

    private ArrayList<TraitCombinationFilter> getSearchFilters() {

        ArrayList<TraitCombinationFilter> tcf = new ArrayList<TraitCombinationFilter>();
        double start = 0;
        double end = 0;

        for (@SuppressWarnings("rawtypes")
        Iterator i = criteriaTable.getItemIds().iterator(); i.hasNext();) {

            int iid = (Integer) i.next();
            Item item = criteriaTable.getItem(iid);
            int traitID = Integer.valueOf(item.getItemProperty("traitID").toString());
            int scaleID = Integer.valueOf(item.getItemProperty("scaleID").toString());
            int methodID = Integer.valueOf(item.getItemProperty("methodID").toString());
            String scaleType = item.getItemProperty("scaleType").toString();
            String criteriaValue = item.getItemProperty("criteriaValue").toString();
            String scaleDiscreteValue = item.getItemProperty("scaleDiscreteValue").toString();

            if (scaleType.equals("discrete")) {
                String valueToUse = scaleDiscreteValue != null && !scaleDiscreteValue.equals("") ? scaleDiscreteValue : criteriaValue;
                valueToUse = valueToUse.trim();

                // if the value can be a number add a filter with value as a
                // double
                try {
                    Double valueToUseInDouble = Double.valueOf(valueToUse);
                    TraitCombinationFilter filter = new TraitCombinationFilter(new Integer(traitID), new Integer(scaleID), new Integer(
                            methodID), valueToUseInDouble);
                    LOG.debug("" + traitID + ":" + scaleID + ":" + methodID + ":" + valueToUseInDouble);
                    System.out.println("" + traitID + ":" + scaleID + ":" + methodID + ":" + valueToUseInDouble);
                    tcf.add(filter);
                } catch (NumberFormatException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    if (getWindow() != null) {
                        MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                                messageSource.getMessage(Message.ERROR_INVALID_NUMBER_FORMAT_MUST_BE_NUMERIC));
                    }
                }

                TraitCombinationFilter filter = new TraitCombinationFilter(new Integer(traitID), new Integer(scaleID),
                        new Integer(methodID), valueToUse);
                LOG.debug("" + traitID + ":" + scaleID + ":" + methodID + ":" + valueToUse);
                System.out.println("" + traitID + ":" + scaleID + ":" + methodID + ":" + valueToUse);
                tcf.add(filter);
            } else {
                // check if the value is a numeric range
                boolean notNumericRange = false;
                if (criteriaValue.contains("-")) {
                    String[] c = criteriaValue.split("-");
                    if (c.length == 2) {
                        try {
                            start = Double.valueOf(c[0].trim());
                            end = Double.valueOf(c[1].trim());
                            NumericRange ranges = new NumericRange(start, end);
                            TraitCombinationFilter tcFilter = new TraitCombinationFilter(new Integer(traitID), new Integer(scaleID),
                                    new Integer(methodID), ranges);
                            tcf.add(tcFilter);
                        } catch (NumberFormatException e) {
                            notNumericRange = true;
                            LOG.error(e.toString() + "\n" + e.getStackTrace());
                            e.printStackTrace();
                            MessageNotifier.showWarning(parentWindow, messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                                    messageSource.getMessage(Message.ERROR_INVALID_NUMBER_FORMAT_MUST_BE_NUMERIC));
                        }
                    } else {
                        notNumericRange = true;
                    }
                } else {
                    notNumericRange = true;
                }

                if (notNumericRange) {
                    TraitCombinationFilter tcFilter = new TraitCombinationFilter(new Integer(traitID), new Integer(scaleID), new Integer(
                            methodID), criteriaValue);
                    tcf.add(tcFilter);
                }
            }

        }

        return tcf;
    }

    boolean withSelectedTraitScaleMethod() throws InternationalizableException {
        try {
            Object itemIDTrait = traitTable.getValue();
            Object itemIDScale = scaleTable.getValue();
            Object itemIDMethod = traitMethodTable.getValue();
            if (traitTable.isSelected(itemIDTrait) && scaleTable.isSelected(itemIDScale) && traitMethodTable.isSelected(itemIDMethod)) {
                return true;
            }
        } catch (NullPointerException e) {
            throw new InternationalizableException(e, Message.ERROR_NULL_TABLE, Message.EMPTY_STRING);
        }
        return false;
    }

    public void addCriteriaButtonClickAction() throws InternationalizableException {
        String valueCriteria = "";
        String scaleDiscreteValue = "";

        if (withSelectedTraitScaleMethod()) {
            if (flagScale == 1) {

                for (@SuppressWarnings("rawtypes")
                Iterator i = scaleValueTable.getItemIds().iterator(); i.hasNext();) {
                    int iid = (Integer) i.next();
                    Item item = scaleValueTable.getItem(iid);
                    Button button = (Button) item.getItemProperty("select").getValue();

                    if ((Boolean) button.getValue() == true) {
                        valueCriteria += item.getItemProperty("Value").getValue() + ",";
                        scaleDiscreteValue = "" + item.getItemProperty("scaleValue").getValue();
                    }
                }

            } else {
                valueCriteria = txtValueInput.getValue().toString();
            }

            addToCriteriaTable(valueCriteria, scaleDiscreteValue);

        } else {
            LOG.error("SearchGermplasmByPhenotypicTab: Error at addCriteriaButtonClickAction()");
            System.out.println("SearchGermplasmByPhenotypicTab: Error at addCriteriaButtonClickAction()");
            throw new InternationalizableException(new Exception("Input error. No selected trait scale method."), 
                    Message.ERROR_INPUT, Message.ERROR_NO_SELECTED_TRAIT_SCALE_METHOD);
        }
    }

    public void deleteButtonClickAction() {
        Object itemID = criteriaTable.getValue();
        if (criteriaTable.isSelected(itemID)) {
            criteriaTable.removeItem(itemID);
        }

        if (criteriaTable.getItemIds().isEmpty()) {
            btnSearch.setEnabled(false);
        }
    }

    public void deleteAllButtonClickAction() {
        criteriaTable.removeAllItems();
        btnSearch.setEnabled(false);
    }

    public void searchButtonClickAction() throws InternationalizableException {

        try {
            ArrayList<Integer> gids = gidsByPhenotypic.getGIDSByPhenotypicData(getSearchFilters());

            if (flagResult == 0) {
                displayGidsToResultTable(gids);
                componentTtraitValueInput.addComponent(searchResultTable);
                flagResult = 1;
            } else {
                searchResultTable.removeAllItems();
                dataSourceSearchResult = dataIndexContainer.addGidsResult(gids);
                searchResultTable.setContainerDataSource(dataSourceSearchResult);
            }
        } catch (Exception e) {
            throw new InternationalizableException(e, Message.ERROR_IN_SEARCH, Message.EMPTY_STRING);
        }
    }

    public void traitTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        try {
            sourceTable.select(itemId);
            int traitID = Integer.valueOf(item.getItemProperty("traitID").toString());
            dataSourceScale = dataIndexContainer.getScaleByTraitID(traitID);
            scaleTable.setContainerDataSource(dataSourceScale);
            scaleTable.setValue(scaleTable.firstItemId());
            scaleTable.setVisibleColumns(new String[] { "scaleName", "scaleType" });
            scaleSetFirstRowSelected();
            updateScaleValueInputDisplay(getScaleType(), getScaleID());
            scaleTable.requestRepaint();

            dataSourceTraitMethod = dataIndexContainer.getMethodTraitID(traitID);
            traitMethodTable.setContainerDataSource(dataSourceTraitMethod);
            traitMethodTable.setVisibleColumns(new String[] { "methodName", "methodDescription" });
            traitMethodTable.setValue(traitMethodTable.firstItemId());
            traitMethodTable.requestRepaint();
        } catch (Exception e) {
            throw new InternationalizableException(e, Message.ERROR_IN_DISPLAYING_DETAILS, Message.EMPTY_STRING);
        }
    }

    public void scaleTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        int scaleID = Integer.valueOf(item.getItemProperty("scaleID").toString());
        String scaleType = item.getItemProperty("scaleType").toString();
        updateScaleValueInputDisplay(scaleType, scaleID);
    }

    @Override
    public void afterPropertiesSet() {

        this.setColumns(4);
        this.setRows(4);
        this.setSpacing(true);

        step1Label = new Label();
        //step1Label.setContentMode(Label.CONTENT_XHTML);
        step1Label.setStyleName("h3");

        step2Label = new Label();
        //step2Label.setContentMode(Label.CONTENT_XHTML);
        step2Label.setStyleName("h3");

        step3Label = new Label();
        //step3Label.setContentMode(Label.CONTENT_XHTML);
        step3Label.setStyleName("h3");

        step4Label = new Label();
        //step4Label.setContentMode(Label.CONTENT_XHTML);
        step4Label.setStyleName("h3");

        step5Label = new Label();
        //step5Label.setContentMode(Label.CONTENT_XHTML);
        step5Label.setStyleName("h3");

        mainLabel = new Label();
        //mainLabel.setContentMode(Label.CONTENT_XHTML);
        mainLabel.setStyleName("h1");

        finalStepLabel = new Label();
        //finalStepLabel.setContentMode(Label.CONTENT_XHTML);
        finalStepLabel.setStyleName("h3");
        finalStepLabel = new Label();

        valueOptionsInstuctionLabel = new Label();

        rangeInstructionLabel = new Label();

        componentTrait = new VerticalLayout();
        componentTrait.setSpacing(true);

        componentTtraitValueInput = new VerticalLayout();
        componentTtraitValueInput.setSpacing(true);

        componentTrait.addComponent(mainLabel);

        try {
            displayTraitTable();
        } catch (InternationalizableException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showError(parentWindow, // TESTED
                    messageSource.getMessage(Message.ERROR_IN_DISPLAYING_TRAIT_TABLE), 
                    "</br>" + messageSource.getMessage(Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR));
        }

        componentTtraitValueInput.addComponent(step5Label);

        btnAddCriteria = new Button();
        btnAddCriteria.setData(ADD_CRITERIA_BUTTON_ID);
        componentTtraitValueInput.addComponent(btnAddCriteria);

        btnAddCriteria.addListener(new GermplasmButtonClickListener(this));

        displaySearchCriteria();

        HorizontalLayout hButton = new HorizontalLayout();
        hButton.setSpacing(true);

        btnDelete = new Button();
        btnDelete.setData(DELETE_BUTTON_ID);
        btnDelete.setDescription("You can delete the currently selected criteria.");
        hButton.addComponent(btnDelete);

        btnDelete.addListener(new GermplasmButtonClickListener(this));

        btnDeleteAll = new Button();
        btnDeleteAll.setData(DELETE_ALL_BUTTON_ID);
        btnDeleteAll.setDescription("You can delete all the criteria.");
        hButton.addComponent(btnDelete);

        btnDeleteAll.addListener(new GermplasmButtonClickListener(this));
        hButton.addComponent(btnDeleteAll);

        componentTtraitValueInput.addComponent(hButton);

        componentTtraitValueInput.addComponent(finalStepLabel);

        btnSearch = new Button();
        btnSearch.setData(SEARCH_BUTTON_ID);
        btnSearch.setEnabled(false);
        componentTtraitValueInput.addComponent(btnSearch);

        btnSearch.addListener(new GermplasmButtonClickListener(this));
        this.addComponent(componentTrait, 1, 1);
        this.addComponent(componentTtraitValueInput, 3, 1);

        traitTable.addListener(new GermplasmItemClickListener(this, traitTable));

        scaleTable.addListener(new GermplasmItemClickListener(this, scaleTable));
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        messageSource.setCaption(btnAddCriteria, Message.ADD_CRITERIA_LABEL);
        messageSource.setCaption(btnDelete, Message.DELETE_LABEL);
        messageSource.setCaption(btnDeleteAll, Message.DELETE_ALL_LABEL);
        messageSource.setCaption(btnSearch, Message.SEARCH_LABEL);

        messageSource.setCaption(step1Label, Message.STEP1_LABEL);
        messageSource.setCaption(step2Label, Message.STEP2_LABEL);
        messageSource.setCaption(step3Label, Message.STEP3_LABEL);
        messageSource.setCaption(step4Label, Message.STEP4_LABEL);
        messageSource.setCaption(step5Label, Message.STEP5_LABEL);
        messageSource.setCaption(mainLabel, Message.GERMPLASM_BY_PHENO_TITLE);
        messageSource.setCaption(finalStepLabel, Message.FINAL_STEP_LABEL);
        messageSource.setCaption(valueOptionsInstuctionLabel, Message.SELECT_A_VALUE_FROM_THE_OPTIONS_BELOW_LABEL);
        messageSource.setCaption(rangeInstructionLabel, Message.TO_ENTER_A_RANGE_OF_VALUES_FOLLOW_THIS_EXAMPLE_LABEL);

        messageSource.setDescription(btnDelete, Message.YOU_CAN_DELETE_THE_CURRENTLY_SELECTED_CRITERIA_DESC);
        messageSource.setDescription(btnDeleteAll, Message.YOU_CAN_DELETE_ALL_THE_CRITERIA_DESC);
    }

}
