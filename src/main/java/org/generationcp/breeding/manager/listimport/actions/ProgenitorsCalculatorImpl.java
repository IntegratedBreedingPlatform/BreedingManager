package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This Interface take care to calculate gerplasm progenitors value to insert on database.
 */
@Component
public class ProgenitorsCalculatorImpl implements ProgenitorsCalculator {

    GermplasmDataManager manager;

    @Autowired
    public ProgenitorsCalculatorImpl(GermplasmDataManager manager) {
        this.manager = manager;
    }

    public int calculate(int methodId, int prevGnpgs) {
        int gnpgs = 0;
        if (methodId == ProcessImportedGermplasmAction.UNKNOWN_DERIVATIVE_METHOD) {
            gnpgs =  -1;
        } else {
            final Method selectedMethod = manager.getMethodByID(methodId);
            if ("GEN".equals(selectedMethod.getMtype())) {
                gnpgs = 2;
            } else {
                gnpgs = prevGnpgs;
            }
        }
        return gnpgs;
    }
}



