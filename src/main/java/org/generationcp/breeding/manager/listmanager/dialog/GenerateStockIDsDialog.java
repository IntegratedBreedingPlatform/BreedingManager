package org.generationcp.breeding.manager.listmanager.dialog;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.List;

@Configurable
public class GenerateStockIDsDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final String DEFAULT_STOCKID_PREFIX = "SID";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(GenerateStockIDsDialog.class);

	@Resource
    private SimpleResourceBundleMessageSource messageSource;
	
	@Resource
	private StockService stockService;
	
	private Button btnContinue;
	private Button btnCancel;
	
	private Label lblSpecifyPrefix;
	private Label lblDefaultPrefixDescription;
	private Label lblNextPrefixInSequence;
	private Label lblStockIdForThisList;
	
	private TextField txtSpecifyPrefix;
	private Label lblExampleNextPrefixInSequence;
	private Label lblExampleStockIdForThisList;

	private VerticalLayout source; 
	
	
	public GenerateStockIDsDialog(
			VerticalLayout source,
			GermplasmList germplasmList) {
		this.source = source;
	}


	private void initializeSubWindow() {
       
    	addStyleName(Reindeer.WINDOW_LIGHT);
        setModal(true);
        setWidth("600px");
        setResizable(false);
        center();
        setCaption(messageSource.getMessage(Message.GENERATE_STOCKID_HEADER));

    }
	
	
	@Override
    public void afterPropertiesSet() throws Exception {
		
		initializeSubWindow();
        instantiateComponents();
        initializeValues();
        addListeners();
        layoutComponents();
        updateLabels();
        
    }
	
	
	@Override
	public void instantiateComponents() {
		
		lblSpecifyPrefix = new Label(messageSource.getMessage(Message.SPECIFY_STOCKID_PREFIX_LABEL));
		lblSpecifyPrefix.addStyleName("bold");
		lblSpecifyPrefix.setImmediate(true);
		
		txtSpecifyPrefix = new TextField();
		txtSpecifyPrefix.setImmediate(true);
		txtSpecifyPrefix.setMaxLength(15);
		txtSpecifyPrefix.focus();
		
		lblDefaultPrefixDescription = new Label(messageSource.getMessage(Message.DEFAULT_PREFIX_DESCRIPTION_LABEL));
		lblDefaultPrefixDescription.addStyleName("italic");
		
		lblNextPrefixInSequence = new Label(messageSource.getMessage(Message.NEXT_PREFIX_IN_SEQUENCE_LABEL));
		lblNextPrefixInSequence.addStyleName("bold");
		lblNextPrefixInSequence.setImmediate(true);
		
		lblExampleNextPrefixInSequence = new Label();
		
		lblStockIdForThisList = new Label(messageSource.getMessage(Message.EXAMPLE_STOCKID_LABEL));
		lblStockIdForThisList.addStyleName("bold");
		lblStockIdForThisList.setImmediate(true);
		
		lblExampleStockIdForThisList = new Label();

        btnContinue = new Button(messageSource.getMessage(Message.CONTINUE));
        btnContinue.setWidth("80px");
        btnContinue.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        btnCancel = new Button(messageSource.getMessage(Message.CANCEL));
        btnCancel.setWidth("80px");
        btnCancel.setDescription("Cancel");
	    btnCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
		
	}

	@Override
	public void initializeValues() {
		
		updateSampleStockId("");
		
	}

	@Override
	public void addListeners() {
		
		btnCancel.addListener(new Button.ClickListener(){

			private static final long serialVersionUID = 1271362384141739702L;

			@Override
			public void buttonClick(ClickEvent event) {
				Window win = event.getButton().getWindow() ;
			    win.getParent().removeWindow(win) ;
			}
			
		});
		
		btnContinue.addListener(new Button.ClickListener(){
			
			private static final long serialVersionUID = 2853818327406493402L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				
				if (source instanceof SpecifyGermplasmDetailsComponent){
					
					applyStockIdToImportedGermplasm(txtSpecifyPrefix.getValue().toString(),((SpecifyGermplasmDetailsComponent) source).getImportedGermplasms());
					
					((SpecifyGermplasmDetailsComponent) source).popupSaveAsDialog();
					Window win = event.getButton().getWindow() ;
				    win.getParent().removeWindow(win);
				    
				}
				
			}});
		

		txtSpecifyPrefix.setTextChangeEventMode(TextChangeEventMode.EAGER);
		txtSpecifyPrefix.addListener(new TextChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void textChange(TextChangeEvent event) {
			
				updateSampleStockId(event.getText());
				
			}
		});
		txtSpecifyPrefix.addListener(new TextField.ValueChangeListener(){

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateSampleStockId(txtSpecifyPrefix.getValue().toString());
				
			}});
		
		
           
	}
	
	@Override
	public void layoutComponents() {
		
		VerticalLayout rootLayout = new VerticalLayout();
		rootLayout.setWidth("100%");
        rootLayout.setSpacing(true);
        rootLayout.setMargin(true);
        setContent(rootLayout);
        
        rootLayout.addComponent(new OneLineLayout(lblSpecifyPrefix, txtSpecifyPrefix));
        rootLayout.addComponent(lblDefaultPrefixDescription);
        rootLayout.addComponent(new OneLineLayout(lblNextPrefixInSequence, lblExampleNextPrefixInSequence));
        rootLayout.addComponent(new OneLineLayout(lblStockIdForThisList, lblExampleStockIdForThisList));
	    
		HorizontalLayout hButton = new HorizontalLayout();
        hButton.setSpacing(true);
        hButton.setMargin(true);
        hButton.addComponent(btnCancel);
        hButton.addComponent(btnContinue);
        
        rootLayout.addComponent(hButton);
        rootLayout.setComponentAlignment(hButton, Alignment.MIDDLE_CENTER);
		
	}

	@Override
	public void updateLabels() {
		//do nothing
		
	}
	
	protected Label getLblExampleNextPrefixInSequence() {
		return lblExampleNextPrefixInSequence;
	}
	protected Label getLblExampleStockIdForThisList() {
		return lblExampleStockIdForThisList;
	}
	
	private class OneLineLayout extends HorizontalLayout {
		
		private static final long serialVersionUID = 1L;

		OneLineLayout(AbstractComponent ... components){
			setSpacing(true);
			for (AbstractComponent component : components){
				addComponent(component);
			}
		}
		
	}
	
	protected void updateSampleStockId(String prefix){
		try {
			
			String nextStockIDPrefix = "";
			
			if (!StringUtils.isEmpty(prefix.trim())){
				nextStockIDPrefix = stockService.calculateNextStockIDPrefix(prefix, "-");
			}else{
				nextStockIDPrefix = stockService.calculateNextStockIDPrefix(DEFAULT_STOCKID_PREFIX, "-");
			}
			
			lblExampleNextPrefixInSequence.setValue(nextStockIDPrefix.substring(0, nextStockIDPrefix.length()-1));
			lblExampleStockIdForThisList.setValue(nextStockIDPrefix + "1");
			
		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	protected void applyStockIdToImportedGermplasm(String prefix, List<ImportedGermplasm> importedGermplasmList){
	
		
		String nextStockIDPrefix;
		try {
			
			if (StringUtils.isEmpty(prefix)) {
				nextStockIDPrefix = stockService.calculateNextStockIDPrefix(DEFAULT_STOCKID_PREFIX, "-");
			}else{
				nextStockIDPrefix = stockService.calculateNextStockIDPrefix(prefix, "-");
			}
			
			int stockIdSequence = 1;
			for (ImportedGermplasm importedGermplasm: importedGermplasmList){
				if (importedGermplasm.getSeedAmount()!= null && importedGermplasm.getSeedAmount() > 0){
					importedGermplasm.setInventoryId(nextStockIDPrefix + stockIdSequence);
					stockIdSequence++;
				}
				
			}
		} catch (MiddlewareException e) {
			LOG.error(e.getMessage(), e);
		}
		
		
	}
	
	

}
