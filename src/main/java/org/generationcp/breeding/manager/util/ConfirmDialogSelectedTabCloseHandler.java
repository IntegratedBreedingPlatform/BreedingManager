package org.generationcp.breeding.manager.util;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListDataComponent;
import org.generationcp.breeding.manager.listmanager.ListManagerTreeMenu;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@Deprecated
@Configurable
public class ConfirmDialogSelectedTabCloseHandler implements TabSheet.CloseHandler{

    private static final long serialVersionUID = 1L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Override
    public void onTabClose(final TabSheet tabsheet, final Component tabContent) {
    	
        // if tab to be closed is a Germplasm List
        VerticalLayout content = (VerticalLayout) tabContent;
        if (content.getComponent(0) instanceof ListManagerTreeMenu) {
            final ListManagerTreeMenu treeMenu = (ListManagerTreeMenu) content.getComponent(0);
            boolean valuesModified = treeMenu.hasChanged();
            
            // check if the list in the tab to be closed has values that were modified
            if (valuesModified) {
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
                            ListDataComponent listDataComponent = treeMenu.getListManagerListDataComponent();
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
        } else {
            // directly proceed to close tab if contents is not a germplasm list 
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
        }
        tabsheet.requestRepaintAll();
    }
}

