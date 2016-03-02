package org.generationcp.breeding.manager.listimport.actions;

import org.generationcp.middleware.pojos.Germplasm;


public interface GermplasmBuilder {

	public Germplasm createGermplasmObject(final Integer gid, final Integer gnpgs, final Integer gpid1, final Integer gpid2,
			final Integer ibdbUserId, final Integer dateIntValue);

	public Germplasm build(GermplasmDataProvider provider);

}
