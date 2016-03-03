package org.generationcp.breeding.manager.listimport.actions;

import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.middleware.pojos.Germplasm;

public interface NoPedigreeConncetionProcess {

	GermplasmName generateGermplasmNameProcess(Integer ibdbUserId, Integer dateIntValue, Map<String, Germplasm> createdGermplasms,
			int gid, String desig);
}
