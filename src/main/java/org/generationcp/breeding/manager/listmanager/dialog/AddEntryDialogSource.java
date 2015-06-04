
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.List;

import org.generationcp.breeding.manager.listmanager.ListManagerMain;

public interface AddEntryDialogSource {

	public void finishAddingEntry(Integer gid);

	public void finishAddingEntry(List<Integer> gids);

	public ListManagerMain getListManagerMain();

}
