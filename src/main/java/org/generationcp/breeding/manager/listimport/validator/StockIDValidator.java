
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
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class StockIDValidator {

	private static final Logger LOG = LoggerFactory.getLogger(StockIDValidator.class);

	@Resource
	private InventoryDataManager inventoryDataManager;

	public void validate(String header,ImportedGermplasmList importedGermplasmList) throws FileParsingException {
		this.validateForDuplicateStockIds(header,importedGermplasmList);
		this.validateForMissingStockIDValues(header,importedGermplasmList);
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

	private void validateForMissingStockIDValues(String header,ImportedGermplasmList importedGermplasmList) throws FileParsingException {
		if (importedGermplasmList.hasMissingStockIDValues()) {
			throw new FileParsingException("GERMPLSM_PARSE_GID_MISSING_STOCK_ID_VALUE", 0, "", header);
		}
	}

}
