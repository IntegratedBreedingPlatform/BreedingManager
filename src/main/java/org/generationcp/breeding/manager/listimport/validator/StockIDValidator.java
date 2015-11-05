
package org.generationcp.breeding.manager.listimport.validator;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockIDValidator {

	private static final Logger LOG = LoggerFactory.getLogger(StockIDValidator.class);

	@Resource
	private InventoryDataManager inventoryDataManager;

	public void validate(String header,ImportedGermplasmList importedGermplasmList) throws FileParsingException {
		this.validateForDuplicateStockIds(header,importedGermplasmList);
	}

	private void validateForDuplicateStockIds(String header,ImportedGermplasmList importedGermplasmList) throws FileParsingException {
		String possibleDuplicateStockId = importedGermplasmList.getDuplicateStockIdIfExists();
		if (!"".equals(possibleDuplicateStockId.trim())) {
			throw new FileParsingException("GERMPLASM_PARSE_DUPLICATE_STOCK_ID", 0, possibleDuplicateStockId, header);
		}

		try {
			List<String> possibleExistingDBStockIds =
					this.inventoryDataManager.getSimilarStockIds(importedGermplasmList.getStockIdsAsList());
			if (!possibleExistingDBStockIds.isEmpty()) {
				throw new FileParsingException("GERMPLASM_PARSE_DUPLICATE_DB_STOCK_ID", 0, StringUtils.abbreviate(
						StringUtils.join(possibleExistingDBStockIds, " "), 20), header);
			}
		} catch (MiddlewareQueryException e) {
			StockIDValidator.LOG.error(e.getMessage(), e);
			throw new FileParsingException(e.getMessage());
		}
	}
}
