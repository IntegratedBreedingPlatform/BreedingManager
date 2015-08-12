
package org.generationcp.breeding.manager.crossingmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

public class CrossingManagerUtil {

	/**
	 * Determines the proper crossing method for a germplasm based on how its parental lines have been created.
	 * 
	 * @param child - the germplasm whose breeding method will be set
	 * @param female - female parent
	 * @param male - male parent
	 * @param motherOfFemale - maternal female grand parent (mommy of female parent)
	 * @param fatherOfFemale - maternal male grand parent (daddy of female parent)
	 * @param motherOfMale - paternal female grand parent (mommy of male parent)
	 * @param fatherOfMale - paternal male grand parent (daddy of male parent)
	 * @return Germplasm - the parameter gc will be returned and its method id should have been set correctly
	 * @throws MiddlewareQueryException
	 */
	public static Germplasm setCrossingBreedingMethod(Germplasm child, Germplasm female, Germplasm male, Germplasm motherOfFemale,
			Germplasm fatherOfFemale, Germplasm motherOfMale, Germplasm fatherOfMale) {

		if (female != null && female.getGnpgs() < 0) {
			if (male != null && male.getGnpgs() < 0) {
				child.setMethodId(101);
			} else {
				if (male != null && male.getGnpgs() == 1) {
					child.setMethodId(101);
				} else if (male != null && male.getGnpgs() == 2) {
					if (motherOfMale != null && motherOfMale.getGid() == female.getGid() || fatherOfMale != null
							&& fatherOfMale.getGid() == female.getGid()) {
						child.setMethodId(107);
					} else {
						child.setMethodId(102);
					}
				} else {
					child.setMethodId(106);
				}
			}
		} else {
			if (male != null && male.getGnpgs() < 0) {
				if (female != null && female.getGnpgs() == 1) {
					child.setMethodId(101);
				} else if (female != null && female.getGnpgs() == 2) {
					if (motherOfFemale != null && motherOfFemale.getGid() == male.getGid() || fatherOfFemale != null
							&& fatherOfFemale.getGid() == male.getGid()) {
						child.setMethodId(107);
					} else {
						child.setMethodId(102);
					}
				} else {
					child.setMethodId(106);
				}
			} else {
				if (female != null && female.getMethodId() == 101 && male != null && male.getMethodId() == 101) {
					child.setMethodId(103);
				} else {
					child.setMethodId(106);
				}
			}
		}

		if (child.getMethodId() == null) {
			child.setMethodId(101);
		}
		return child;
	}

	public static Collection<?> getOddEntries(Table listDataTable) {
		List<Object> oddIds = new ArrayList<Object>();

		Collection<?> itemIds = listDataTable.getItemIds();
		for (Object itemId : itemIds) {
			Item item = listDataTable.getItem(itemId);
			Integer entryNo = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			if (entryNo.intValue() % 2 != 0) {
				oddIds.add(itemId);
			}
		}

		return oddIds;
	}

	public static Collection<?> getEvenEntries(Table listDataTable) {
		List<Object> evenIds = new ArrayList<Object>();

		Collection<?> itemIds = listDataTable.getItemIds();
		for (Object itemId : itemIds) {
			Item item = listDataTable.getItem(itemId);
			Integer entryNo = (Integer) item.getItemProperty(ColumnLabels.ENTRY_ID.getName()).getValue();
			if (entryNo.intValue() % 2 == 0) {
				evenIds.add(itemId);
			}
		}

		return evenIds;
	}
}
