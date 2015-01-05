package org.generationcp.breeding.manager.customcomponent.handler;

import org.generationcp.breeding.manager.customcomponent.GermplasmListSource;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect;

@Configurable
public class GermplasmListSourceDropHandler implements DropHandler {
    private static final long serialVersionUID = -6676297159926786216L;

    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListSourceDropHandler.class);
    @Autowired
    private GermplasmListManager germplasmListManager;
    private GermplasmListSource targetListSource;
    private ListSelectorComponent source;
    private GermplasmListTreeUtil utilSource;

    public GermplasmListSourceDropHandler(GermplasmListSource targetListSource, ListSelectorComponent source, GermplasmListTreeUtil utilSource){
        this.targetListSource = targetListSource;
        this.source = source;
        this.utilSource = utilSource;
    }

    public void drop(DragAndDropEvent dropEvent) {
        Transferable t = dropEvent.getTransferable();
        if (t.getSourceComponent() != targetListSource) {
            return;
        }

        AbstractSelect.AbstractSelectTargetDetails target = (AbstractSelect.AbstractSelectTargetDetails) dropEvent.getTargetDetails();

        Object sourceItemId = t.getData("itemId");
        Object targetItemId = target.getItemIdOver();

        VerticalDropLocation location = target.getDropLocation();

        if(location != VerticalDropLocation.MIDDLE || sourceItemId.equals(targetItemId)) {
            return;
        }

        GermplasmList targetList = null;
        try {
            targetList = germplasmListManager.getGermplasmListById((Integer) targetItemId);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
        } catch (ClassCastException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
        }

        //Dropped on a folder / public or program list folder
        if (targetItemId instanceof String || targetList == null || "FOLDER".equalsIgnoreCase(targetList.getType())){
            utilSource.setParent(sourceItemId, targetItemId);
            //Dropped on a list
        } else if (targetList!=null){
            if(targetList.getParentId()==null && ((Integer)targetItemId)>0) {
                targetItemId = ListSelectorComponent.LISTS;
            } else {
                targetItemId = targetList.getParentId();
            }
            utilSource.setParent(sourceItemId, targetItemId);
        }

        source.refreshRemoteTree();
    }

    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }
}
