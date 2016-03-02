package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class GNPGSCalculator_UT {
    public static final int GENERATIVE_METHOD_ID = 5;
    public static final int EXPECTED_GNPGS_VALUE_FOR_GENERATIVES = 2;
    public static final String METHOD_GEN_TYPE = "GEN";

    public static final int UNKNOWN_DERIVATIVE_METHOD_ID = 31;
    private static final Integer KNOWN_DERIVATIVE_METHOD_ID = 10;
    public static final int EXPECTED_GNPGS_VALUE_FOR_DERIVATIVES = -1;
    public static final String METHOD_DER_TYPE = "DER";


    GNPGSCalculator action;

    @Mock
    SpecifyGermplasmDetailsComponent germplasmDetailsComponentMock;

    @Mock
    private ContextUtil contextUtilMock;

    @Mock
    GermplasmDataManager germplasmDataManagerMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        action = new ProcessImportedGermplasmAction(germplasmDetailsComponentMock);
        ((ProcessImportedGermplasmAction) action).setContextUtil(contextUtilMock);
        ((ProcessImportedGermplasmAction) action).setGermplasmDataManager(germplasmDataManagerMock);
    }

    @Test
    public void givenADerivativeMethodThenGpngsValueIsMinus1() throws Exception {

        int gnpgs = action.calculateGNPGS(UNKNOWN_DERIVATIVE_METHOD_ID, null);
        assertEquals(EXPECTED_GNPGS_VALUE_FOR_DERIVATIVES, gnpgs);
    }

    @Test
    public void givenAKnownDerivativeMethodThenGpngsValueIsMinus1() throws Exception {

        int gnpgs = action.calculateGNPGS(UNKNOWN_DERIVATIVE_METHOD_ID, null);
        assertEquals(EXPECTED_GNPGS_VALUE_FOR_DERIVATIVES, gnpgs);
    }

    @Test
    public void givenAGenerativeMethodThenGpngsValueIs2() throws Exception {
        Method method = new Method(GENERATIVE_METHOD_ID);
        method.setMtype(METHOD_GEN_TYPE);
        when(germplasmDataManagerMock.getMethodByID(GENERATIVE_METHOD_ID)).thenReturn(method);

        int gnpgs = action.calculateGNPGS(GENERATIVE_METHOD_ID, null);

        assertEquals(EXPECTED_GNPGS_VALUE_FOR_GENERATIVES,gnpgs);
        verify(germplasmDataManagerMock).getMethodByID(GENERATIVE_METHOD_ID);
    }
}