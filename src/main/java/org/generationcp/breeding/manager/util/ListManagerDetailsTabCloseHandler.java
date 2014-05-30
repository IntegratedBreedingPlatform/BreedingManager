package org.generationcp.breeding.manager.util;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListTabComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListSelectionLayout;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;

@Configurable
public class ListManagerDetailsTabCloseHandler implements TabSheet.CloseHandler, Button.ClickListener{

    private static final long serialVersionUID = 1L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private ListSelectionLayout detailsLayout;
    
    public ListManagerDetailsTabCloseHandler(ListSelectionLayout detailsLayout) {
        this.detailsLayout = detailsLayout;
    }
    
    @Override
    public void onTabClose(final TabSheet tabsheet, final Component tabContent) {
        // if tab to be closed is a Germplasm List
        if (tabContent instanceof ListTabComponent) {
            confirmTabClose(tabsheet, tabContent);
        } else {
            // directly proceed to close tab if contents is not a germplasm list 
            closeTab(tabsheet, tabContent);
        }
    }
    
    // called by Close All Tabs button
    @Override
    public void buttonClick(ClickEvent event) {
        // "Close" All Tabs
        if (event.getButton().getData().equals(ListSelectionLayout.CLOSE_ALL_TABS_ID)) {
            TabSheet detailsTabSheet = detailsLayout.getDetailsTabsheet();
            for (int i=detailsTabSheet.getComponentCount()-1; i>=0; i--) {
                Tab detailsTab = detailsTabSheet.getTab(i);
                Component detailsTabComponent = detailsTab.getComponent();
                
                if (detailsTabComponent instanceof ListTabComponent) {
                    confirmTabClose(detailsTabSheet, detailsTabComponent);
                } else {
                    closeTab(detailsTabSheet, detailsTabComponent);
                }
            }
        }
    }
    
    private void confirmTabClose(final TabSheet tabsheet, final Component tabContent) {
        final ListTabComponent listDetails = (ListTabComponent) tabContent;
        boolean valuesModified = listDetails.hasChanged();
        
        // check if the list in the tab to be closed is from local and has values that were modified
        if (listDetails.getGermplasmList()!=null && listDetails.getGermplasmList().getId()<0 && valuesModified) {
            String confirmDialogCaption = messageSource.getMessage(Message.WARNING);
            String confirmDialogMessage = messageSource.getMessage(Message.UNSAVED_CHANGES_LISTDATA);
            String okCaption = messageSource.getMessage(Message.YES);
            String cancelCaption = messageSource.getMessage(Message.NO);

            ConfirmDialog.show(tabsheet.getWindow()
                    ,confirmDialogCaption
                    ,confirmDialogMessage
                    ,okCaption
                    ,cancelCaption
                    ,new ConfirmDialog.Listener() {
                private static final long serialVersionUID = 1L;

                public void onClose(ConfirmDialog dialog) {
                    
                    if (dialog.isConfirmed()) {
                        //FIXME sidebyside: call saveChanges if dialog is confirmed
                        ListComponent listDataComponent = listDetails.getListDataComponent();
                        listDataComponent.saveChangesAction(tabsheet.getWindow());
                    }
                    
                    //close tab after confirm dialog is closed (confirmed or not)
                    closeTab(tabsheet, tabContent);
                }
            });
        } else {
            // only close tab if confirm dialog wasn't displayed.
            // otherwise, confirm dialog must close tab itself
            closeTab(tabsheet, tabContent);
        }
    }
    
    private void closeTab(TabSheet tabsheet, Component tabContent) {
        if(tabsheet.getComponentCount() > 1){
            String tabCaption=tabsheet.getTab(tabContent).getCaption();
            Tab tab = Util.getTabToFocus(tabsheet, tabCaption);
            tabsheet.removeTab(tabsheet.getTab(tabContent));
            tabsheet.setSelectedTab(tab.getComponent());
        }else{
            tabsheet.removeTab(tabsheet.getTab(tabContent));
            detailsLayout.hideDetailsTabsheet();
        }
        tabsheet.requestRepaintAll();
    }
}

