/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.germplasm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.FindGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Bibref;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GermplasmQueries implements Serializable, InitializingBean {

    private GermplasmSearchResultModel germplasmResultByGID;
    private GermplasmDetailModel germplasmDetail;
    private ArrayList<Integer> gids;

    private static final long serialVersionUID = 1L;
    // private HibernateUtil hibernateUtil;

    @Autowired
    private ManagerFactory managerFactory;
    
    private GermplasmDataManager germplasmDataManager;
    private GermplasmListManager germplasmListManager;

    public GermplasmQueries() {
        
    }

    public ArrayList<GermplasmSearchResultModel> getGermplasmListResultByPrefName(String searchBy, String searchString,
            Database databaseInstance) throws QueryException {
        List<Germplasm> germplasmList;
        int count;

        if (searchString.contains("%")) {
            count = 500;
            germplasmList = germplasmDataManager.findGermplasmByName(searchString, 0, count, FindGermplasmByNameModes.NORMAL, Operation.LIKE,
                    null, null, databaseInstance);
        } else {
            count = germplasmDataManager.countGermplasmByName(searchString, FindGermplasmByNameModes.NORMAL, Operation.EQUAL, null, null,
                    databaseInstance);
            germplasmList = germplasmDataManager.findGermplasmByName(searchString, 0, count, FindGermplasmByNameModes.NORMAL, Operation.EQUAL,
                    null, null, databaseInstance);
        }
        ArrayList<GermplasmSearchResultModel> toReturn = new ArrayList<GermplasmSearchResultModel>();
        for (Germplasm g : germplasmList) {
            Germplasm gData = g;
            GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();
            toReturn.add(setGermplasmSearchResult(gResult, gData));

        }
        return toReturn;
    }

    public GermplasmSearchResultModel getGermplasmResultByGID(String gid) throws QueryException {

        Germplasm gData = germplasmDataManager.getGermplasmByGID(new Integer(Integer.valueOf(gid)));
        GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();
        return this.germplasmResultByGID = setGermplasmSearchResult(gResult, gData);
    }

    private GermplasmSearchResultModel setGermplasmSearchResult(GermplasmSearchResultModel gResult, Germplasm gData) throws QueryException {
        gResult.setGid(gData.getGid());
        gResult.setNames(getGermplasmNames(gData.getGid()));

        Method method = germplasmDataManager.getMethodByID(gData.getMethodId());
        if (method != null) {
            gResult.setMethod(method.getMname());
        } else {
            gResult.setMethod("");
        }

        Location loc = germplasmDataManager.getLocationByID(gData.getLocationId());
        if (loc != null) {
            gResult.setLocation(loc.getLname());
        } else {
            gResult.setLocation("");
        }

        return gResult;
    }

    public GermplasmDetailModel getGermplasmDetails(int gid) throws QueryException {

        germplasmDetail = new GermplasmDetailModel();
        Germplasm g = germplasmDataManager.getGermplasmByGID(new Integer(gid));
        Name name = germplasmDataManager.getPreferredNameByGID(gid);

        germplasmDetail.setGid(g.getGid());
        germplasmDetail.setGermplasmMethod(germplasmDataManager.getMethodByID(g.getMethodId()).getMname());
        germplasmDetail.setGermplasmPreferredName(name.getNval());
        germplasmDetail.setGermplasmCreationDate(String.valueOf(name.getNdate()));
        germplasmDetail.setPrefID(getGermplasmPrefID(g.getGid()));
        germplasmDetail.setGermplasmLocation(getLocation(g.getLocationId()));
        germplasmDetail.setReference(getReference(g.getReferenceId()));
        germplasmDetail.setAttributes(getAttributes(g.getGid()));
        germplasmDetail.setNames(getNames(g.getGid()));
        germplasmDetail.setGenerationhistory(getGenerationHistory(g.getGid()));

        return germplasmDetail;
    }

    private ArrayList<GermplasmDetailModel> getGenerationHistory(Integer gid) throws QueryException {
        ArrayList<GermplasmDetailModel> toreturn = new ArrayList<GermplasmDetailModel>();
        List<Germplasm> generationHistoryList = new ArrayList<Germplasm>();
        generationHistoryList = germplasmDataManager.getGenerationHistory(new Integer(gid));
        for (Germplasm g : generationHistoryList) {
            GermplasmDetailModel genHistory = new GermplasmDetailModel();
            genHistory.setGid(g.getGid());
            genHistory.setGermplasmPreferredName(g.getPreferredName().getNval());
            toreturn.add(genHistory);
        }
        return toreturn;
    }

    private ArrayList<GermplasmNamesAttributesModel> getNames(int gid) throws QueryException {
        ArrayList<Name> names = (ArrayList<Name>) germplasmDataManager.getNamesByGID(gid, null, null);
        ArrayList<GermplasmNamesAttributesModel> germplasmNames = new ArrayList<GermplasmNamesAttributesModel>();

        for (Name n : names) {
            GermplasmNamesAttributesModel gNamesRow = new GermplasmNamesAttributesModel();
            gNamesRow.setName(n.getNval());
            gNamesRow.setLocation(getLocation(n.getLocationId()));

            UserDefinedField type = germplasmDataManager.getUserDefinedFieldByID(n.getTypeId());
            if (type != null) {
                gNamesRow.setType(type.getFcode());
                gNamesRow.setTypeDesc(type.getFname());
            }

            gNamesRow.setDate(n.getNdate().toString());
            germplasmNames.add(gNamesRow);
        }
        return germplasmNames;
    }

    private ArrayList<GermplasmNamesAttributesModel> getAttributes(int gid) throws QueryException {
        ArrayList<Attribute> attr = (ArrayList<Attribute>) germplasmDataManager.getAttributesByGID(gid);
        ArrayList<GermplasmNamesAttributesModel> germplasmAttributes = new ArrayList<GermplasmNamesAttributesModel>();

        for (Attribute a : attr) {
            GermplasmNamesAttributesModel gAttributeRow = new GermplasmNamesAttributesModel();
            gAttributeRow.setName(a.getAval());

            Location location = germplasmDataManager.getLocationByID(a.getLocationId());
            if (location != null) {
                gAttributeRow.setLocation(location.getLname());
            }

            UserDefinedField type = germplasmDataManager.getUserDefinedFieldByID(a.getTypeId());
            if (type != null) {
                gAttributeRow.setType(type.getFcode());
                gAttributeRow.setTypeDesc(type.getFname());
            }

            gAttributeRow.setDate(a.getAdate().toString());
            germplasmAttributes.add(gAttributeRow);
        }
        return germplasmAttributes;
    }

    private String getGermplasmPrefID(int gid) throws QueryException {
        ArrayList<Name> names = (ArrayList<Name>) germplasmDataManager.getNamesByGID(gid, 8, null);
        String prefId = "";
        for (Name n : names) {
            if (n.getNstat() == 8) {
                prefId = n.getNval();
                break;
            }

        }
        return prefId;
    }

    private String getGermplasmNames(int gid) throws QueryException {

        List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
        StringBuffer germplasmNames = new StringBuffer("");
        int i = 0;
        for (Name n : names) {
            if (i < names.size() - 1) {
                germplasmNames.append(n.getNval() + ",");
            } else {
                germplasmNames.append(n.getNval());
            }
            i++;
        }

        return germplasmNames.toString();
    }

    private String getReference(int refId) {
        Bibref bibRef = germplasmDataManager.getBibliographicReferenceByID(refId);
        if (bibRef != null) {
            return bibRef.getAnalyt();
        } else {
            return "";
        }

    }

    private String getLocation(int locId) {
        try {
            Location x = germplasmDataManager.getLocationByID(locId);
            return x.getLname();
        } catch (Exception e) {
            return "";
        }
    }

    public GermplasmPedigreeTree generatePedigreeTree(Integer gid, int i) throws QueryException {
        return germplasmDataManager.generatePedigreeTree(gid, i);
    }

	@Override
	public void afterPropertiesSet() throws Exception {

		this.germplasmDataManager = managerFactory.getGermplasmDataManager();
		this.germplasmListManager=managerFactory.getGermplasmListManager();
		
	}

	public List<GermplasmListData> getGermplasmListByGID(int gid) throws QueryException {

		return this.germplasmListManager.getGermplasmListDataByGID(gid, 0, 1000);
	}

}
