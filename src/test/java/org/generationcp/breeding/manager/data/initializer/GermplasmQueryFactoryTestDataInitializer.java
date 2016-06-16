package org.generationcp.breeding.manager.data.initializer;

import org.generationcp.breeding.manager.containers.GermplasmQueryFactory;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;

import com.vaadin.ui.Table;

public class GermplasmQueryFactoryTestDataInitializer {
	
	public GermplasmQueryFactory createGermplasmQueryFactory(ListManagerMain listManagerMain, GermplasmSearchParameter searchParameter, Table matchingGermplasmsTable){
		return new GermplasmQueryFactory(listManagerMain, true, true, searchParameter, matchingGermplasmsTable);
	}
}
