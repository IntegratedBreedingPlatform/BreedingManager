
package org.generationcp.breeding.manager.crossingmanager.listeners;

import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;

public interface CrossingManagerTreeActionsListener extends ListTreeActionsListener {

	public void addListToFemaleList(Integer germplasmListId);

	public void addListToMaleList(Integer germplasmListId);

}
