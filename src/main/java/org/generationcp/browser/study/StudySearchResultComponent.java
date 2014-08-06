package org.generationcp.browser.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.GermplasmStudyBrowserLayout;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.containers.StudyDataIndexContainer;
import org.generationcp.browser.study.listeners.StudyItemClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class StudySearchResultComponent extends VerticalLayout implements InitializingBean, 
					InternationalizableComponent, GermplasmStudyBrowserLayout{
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(StudySearchResultComponent.class);
    
	private static final String STUDY_ID = "ID";
    private static final String STUDY_NAME = "NAME";
    
	private Label totalEntriesLabel;
    private Table searchResultTable;
    private StudyDataIndexContainer studyDataIndexContainer;
    
    private StudySearchMainComponent parentComponent;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private StudyDataManager studyDataManager;
    
    public StudySearchResultComponent(StudySearchMainComponent parentComponent) {
        this.parentComponent = parentComponent;
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
        instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		studyDataIndexContainer = new StudyDataIndexContainer(studyDataManager, 0);
		
        //search Results
        totalEntriesLabel = new Label("",Label.CONTENT_XHTML);
        totalEntriesLabel.setWidth("120px");
        updateNoOfEntries(0);
        
        initializeSearchResultTable();
	}

	private void initializeSearchResultTable() {
		searchResultTable = new Table();
		searchResultTable.setWidth("98%");
		searchResultTable.setHeight("250px");
		searchResultTable.setSelectable(true);
		searchResultTable.setMultiSelect(false);
		searchResultTable.setImmediate(true); 
		searchResultTable.setColumnReorderingAllowed(true);
		searchResultTable.setColumnCollapsingAllowed(true);
		searchResultTable.setCaption(null);

		searchResultTable.addContainerProperty(STUDY_ID, String.class, null);
		searchResultTable.addContainerProperty(STUDY_NAME, String.class, null);		
		messageSource.setColumnHeader(searchResultTable, STUDY_ID, Message.STUDY_ID_LABEL);
		messageSource.setColumnHeader(searchResultTable, STUDY_NAME, Message.STUDY_NAME_LABEL);

		searchResultTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.CLICK_TO_VIEW_STUDY_DETAILS);
            }
        });
	}
	
	public void setSearchResultDataSource(IndexedContainer dataSource){
		searchResultTable.setContainerDataSource(dataSource);
	}

	@Override
	public void initializeValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListeners() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutComponents() {
		setSpacing(true);
		setWidth("425px");
		addComponent(totalEntriesLabel);
        addComponent(searchResultTable);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
	
	public void updateNoOfEntries(int count){
		totalEntriesLabel.setValue(messageSource.getMessage(Message.SEARCH_RESULT_LABEL) + ": " 
	       		 + "  <b>" + count + "</b>");
	}
	
    public void resetSearchResultLayout() {
		removeAllComponents();
		addComponent(totalEntriesLabel);
        addComponent(searchResultTable);
	}
    
    public void searchStudy(String name, String country, Season season, Integer date){
    	if (searchResultTable != null){
    		searchResultTable.removeAllItems();
    	}
    	
        IndexedContainer dataSourceResult = studyDataIndexContainer.getStudies(name, country, season, date);
        
        if (dataSourceResult.size() == 0){
        	updateNoOfEntries(0);	
            MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.NO_STUDIES_FOUND), ""); 
        } else {
        	setSearchResultDataSource(dataSourceResult);
            updateNoOfEntries(dataSourceResult.size());
            searchResultTable.addListener(new StudyItemClickListener(this));
            
            resetSearchResultLayout();
            requestRepaint();
        }
    }
    
    public void studyItemClickAction(Integer studyId) {
        studyDataIndexContainer = new StudyDataIndexContainer(studyDataManager, studyId);

        try {
            Study study = this.studyDataManager.getStudy(Integer.valueOf(studyId));
            //don't show study details if study record is a Folder ("F")
            String studyType = study.getType();
            if (!hasChildStudy(studyId) && !isFolderType(studyType)){
            	parentComponent.createStudyInfoTab(studyId);
            }
        } catch (NumberFormatException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                    messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
        }
    }
    
    private boolean hasChildStudy(int studyId) {

        List<Reference> studyChildren = new ArrayList<Reference>();

        try {
            studyChildren.addAll(this.studyDataManager.getChildrenOfFolder(Integer.valueOf(studyId)));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
            studyChildren = new ArrayList<Reference>();
        }
        if (!studyChildren.isEmpty()) {
            return true;
        }
        return false;
    }
    
    private boolean isFolderType(String type){
        if(type != null){
            type = type.toLowerCase();
            if(type.equals("f") || type.equals("folder")){
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }
    
    public void resetSearchResultTable(){
    	searchResultTable.removeAllItems();
    }
    
    public Table getSearchResultTable(){
    	return searchResultTable;
    }
}
