package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.EnvironmentsAvailableComponent;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.browser.study.StudyAccordionMenu;
import org.generationcp.browser.study.StudyDetailComponent;
import org.generationcp.browser.util.SelectedTabCloseHandler;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.TabSheet.Tab;

@Configurable
public class StudyInfoDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7651767452229107837L;
    
    private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);
    
    public static final String CLOSE_SCREEN_BUTTON_ID = "StudyInfoDialog Close Button ID";

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Component source;
    private Label germplasmComponent;
    private Window parentWindow;
    
    private VerticalLayout mainLayout;
    
    private Button cancelButton;
    private Integer studyId;
    @Autowired
    private StudyDataManagerImpl studyDataManager;

	private boolean h2hCall;
    
    public StudyInfoDialog(Component source, Window parentWindow, Integer studyId,boolean h2hCall){
        this.source = source;
        this.parentWindow = parentWindow;
        this.studyId = studyId;
        this.h2hCall= h2hCall;
        
        	
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("1100px");
        setHeight("650px");
        setResizable(false);
        //setCaption("Study Information");
        // center window within the browser
        center();

         
        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setMargin(true);
        mainLayout.setWidth("1100px");
        mainLayout.setHeight("550px");
        
        
        try {
            Study study = this.studyDataManager.getStudy(studyId);
            setCaption("Study Information: "+study.getName());
            //don't show study details if study record is a Folder ("F")
            String studyType = study.getType();
            //if (!hasChildStudy(studyId) && !isFolderType(studyType)){
               // createStudyInfoTab(studyId);
            //}
            Accordion accordion = new StudyAccordionMenu(studyId, new StudyDetailComponent(this.studyDataManager, studyId),
                    studyDataManager, false,h2hCall);
            accordion.setWidth("93%");
            accordion.setHeight("490px");
            mainLayout.addComponent(accordion, "top:10px;left:5px");
        } catch (NumberFormatException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                    messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
        }
        
        
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        
        cancelButton = new Button("Close");
        cancelButton.setData(CLOSE_SCREEN_BUTTON_ID);
        cancelButton.addListener(new CloseWindowAction());
        
        
        
        
        buttonLayout.addComponent(cancelButton);
        //buttonLayout.setComponentAlignment(doneButton, Alignment.MIDDLE_RIGHT);
        //buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(buttonLayout, "top:520px;left:950px");
        
        
        addComponent(mainLayout);
    }

    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
}
