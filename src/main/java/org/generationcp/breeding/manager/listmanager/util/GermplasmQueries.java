package org.generationcp.breeding.manager.listmanager.util;

import java.io.Serializable;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.util.GermplasmDetailModel;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Bibref;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GermplasmQueries implements Serializable, InitializingBean{

//    private GermplasmSearchResultModel germplasmResultByGID;
    private GermplasmDetailModel germplasmDetail;
//    private ArrayList<Integer> gids;

    private static final long serialVersionUID = 1L;

    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
//    @Autowired
//    private StudyDataManager studyDataManager;
//
//    @Autowired
//    private InventoryDataManager inventoryDataManager;
//    
//    @Autowired
//    private PedigreeDataManager pedigreeDataManager;

    public GermplasmQueries() {

    }

//    public ArrayList<GermplasmSearchResultModel> getGermplasmListResultByPrefName(String searchBy, String searchString,
//            Database databaseInstance) throws InternationalizableException {
//        try {
//            List<Germplasm> germplasmList;
//            long count;
//
//            if (searchString.contains("%")) {
//                count = germplasmDataManager.countGermplasmByName(searchString, GetGermplasmByNameModes.NORMAL, Operation.LIKE, null, null,
//                        databaseInstance);
//                germplasmList = germplasmDataManager.getGermplasmByName(searchString, 0, (int) count, GetGermplasmByNameModes.NORMAL,
//                        Operation.LIKE, null, null, databaseInstance);
//            } else {
//                count = germplasmDataManager.countGermplasmByName(searchString, GetGermplasmByNameModes.NORMAL, Operation.EQUAL, null,
//                        null, databaseInstance);
//                germplasmList = germplasmDataManager.getGermplasmByName(searchString, 0, (int) count, GetGermplasmByNameModes.NORMAL,
//                        Operation.EQUAL, null, null, databaseInstance);
//            }
//            ArrayList<GermplasmSearchResultModel> toReturn = new ArrayList<GermplasmSearchResultModel>();
//            for (Germplasm g : germplasmList) {
//                Germplasm gData = g;
//                GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();
//                toReturn.add(setGermplasmSearchResult(gResult, gData));
//
//            }
//            return toReturn;
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE,
//                    Message.ERROR_IN_GETTING_GERMPLASM_LIST_RESULT_BY_PREFERRED_NAME);
//        }
//    }
//
//    public ArrayList<GermplasmSearchResultModel> getGermplasmListResultByPrefStandardizedName(String searchString)
//            throws InternationalizableException {
//        try {
//            List<Germplasm> germplasmList;
//            long count;
//
//            if (searchString.contains("%")) {
//                count = germplasmDataManager.countGermplasmByName(searchString, Operation.LIKE);
//                germplasmList = germplasmDataManager.getGermplasmByName(searchString, 0, (int) count, Operation.LIKE);
//            } else {
//                count = germplasmDataManager.countGermplasmByName(searchString, Operation.EQUAL);
//                germplasmList = germplasmDataManager.getGermplasmByName(searchString, 0, (int) count, Operation.EQUAL);
//            }
//            ArrayList<GermplasmSearchResultModel> toReturn = new ArrayList<GermplasmSearchResultModel>();
//            for (Germplasm g : germplasmList) {
//                Germplasm gData = g;
//                GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();
//                toReturn.add(setGermplasmSearchResult(gResult, gData));
//
//            }
//            return toReturn;
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE,
//                    Message.ERROR_IN_GETTING_GERMPLASM_LIST_RESULT_BY_PREFERRED_NAME);
//        }
//    }
//
//    public GermplasmSearchResultModel getGermplasmResultByGID(String gid) throws InternationalizableException {
//        try {
//            Germplasm gData = germplasmDataManager.getGermplasmByGID(new Integer(Integer.valueOf(gid)));
//            GermplasmSearchResultModel gResult = new GermplasmSearchResultModel();
//
//            if (gData != null) {
//                return this.germplasmResultByGID = setGermplasmSearchResult(gResult, gData);
//            } else {
//                return null; // not found
//            }
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SEARCH);
//        }
//    }
//
//    private GermplasmSearchResultModel setGermplasmSearchResult(GermplasmSearchResultModel gResult, Germplasm gData)
//            throws InternationalizableException {
//        try {
//            gResult.setGid(gData.getGid());
//            gResult.setNames(getGermplasmNames(gData.getGid()));
//
//            Method method = germplasmDataManager.getMethodByID(gData.getMethodId());
//            if (method != null) {
//                gResult.setMethod(method.getMname());
//            } else {
//                gResult.setMethod("");
//            }
//
//            Location loc = germplasmDataManager.getLocationByID(gData.getLocationId());
//            if (loc != null) {
//                gResult.setLocation(loc.getLname());
//            } else {
//                gResult.setLocation("");
//            }
//
//            return gResult;
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_SEARCH);
//        }
//    }

    public GermplasmDetailModel getGermplasmDetails(int gid) throws InternationalizableException {
        try {
            germplasmDetail = new GermplasmDetailModel();
            Germplasm g = germplasmDataManager.getGermplasmByGID(new Integer(gid));
            Name name = germplasmDataManager.getPreferredNameByGID(gid);

            if (g != null) {
                germplasmDetail.setGid(g.getGid());
                germplasmDetail.setGermplasmMethod(germplasmDataManager.getMethodByID(g.getMethodId()).getMname());
                germplasmDetail.setGermplasmPreferredName(name == null ? "" : name.getNval());
                germplasmDetail.setGermplasmCreationDate(name == null ? "" : String.valueOf(name.getNdate()));
//                germplasmDetail.setPrefID(getGermplasmPrefID(g.getGid()));
                germplasmDetail.setGermplasmLocation(getLocation(g.getLocationId()));
                germplasmDetail.setReference(getReference(g.getReferenceId()));
            }
            return germplasmDetail;
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_GERMPLASM_DETAILS);
        }
    }

//    public ArrayList<GermplasmDetailModel> getGenerationHistory(Integer gid) throws InternationalizableException {
//        try {
//            ArrayList<GermplasmDetailModel> toreturn = new ArrayList<GermplasmDetailModel>();
//            List<Germplasm> generationHistoryList = new ArrayList<Germplasm>();
//           
//            generationHistoryList = pedigreeDataManager.getGenerationHistory(new Integer(gid));
//            for (Germplasm g : generationHistoryList) {
//                GermplasmDetailModel genHistory = new GermplasmDetailModel();
//                String name = g.getPreferredName() != null ? g.getPreferredName().getNval() : "";
//                genHistory.setGid(g.getGid());
//                genHistory.setGermplasmPreferredName(name);
//                toreturn.add(genHistory);
//            }
//            return toreturn;
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_GENERATION_HISTORY);
//        }
//    }

//    private String getPreferredName(Germplasm g) {
//
//        Name names = g.getPreferredName();
//        if (names != null) {
//            return g.getPreferredName().getNval();
//        }
//        return "";
//    }

//    public ArrayList<GermplasmNamesAttributesModel> getNames(int gid) throws InternationalizableException {
//        try {
//            ArrayList<Name> names = (ArrayList<Name>) germplasmDataManager.getNamesByGID(gid, null, null);
//            ArrayList<GermplasmNamesAttributesModel> germplasmNames = new ArrayList<GermplasmNamesAttributesModel>();
//
//            for (Name n : names) {
//                GermplasmNamesAttributesModel gNamesRow = new GermplasmNamesAttributesModel();
//                gNamesRow.setName(n.getNval());
//                gNamesRow.setLocation(getLocation(n.getLocationId()));
//
//                UserDefinedField type = germplasmDataManager.getUserDefinedFieldByID(n.getTypeId());
//                if (type != null) {
//                    gNamesRow.setType(type.getFcode());
//                    gNamesRow.setTypeDesc(type.getFname());
//                }
//
//                gNamesRow.setDate(n.getNdate().toString());
//                germplasmNames.add(gNamesRow);
//            }
//            return germplasmNames;
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_NAMES_BY_GERMPLASM_ID);
//        }
//    }
//
//    public ArrayList<GermplasmNamesAttributesModel> getAttributes(int gid) throws InternationalizableException {
//        try {
//            ArrayList<Attribute> attr = (ArrayList<Attribute>) germplasmDataManager.getAttributesByGID(gid);
//            ArrayList<GermplasmNamesAttributesModel> germplasmAttributes = new ArrayList<GermplasmNamesAttributesModel>();
//
//            for (Attribute a : attr) {
//                GermplasmNamesAttributesModel gAttributeRow = new GermplasmNamesAttributesModel();
//                gAttributeRow.setName(a.getAval());
//
//                Location location = germplasmDataManager.getLocationByID(a.getLocationId());
//                if (location != null) {
//                    gAttributeRow.setLocation(location.getLname());
//                }
//
//                UserDefinedField type = germplasmDataManager.getUserDefinedFieldByID(a.getTypeId());
//                if (type != null) {
//                    gAttributeRow.setType(type.getFcode());
//                    gAttributeRow.setTypeDesc(type.getFname());
//                }
//
//                gAttributeRow.setDate(a.getAdate().toString());
//                germplasmAttributes.add(gAttributeRow);
//            }
//            return germplasmAttributes;
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_ATTRIBUTES_BY_GERMPLASM_ID);
//        }
//    }

//    private String getGermplasmPrefID(int gid) throws InternationalizableException {
//        try {
//            ArrayList<Name> names = (ArrayList<Name>) germplasmDataManager.getNamesByGID(gid, 8, null);
//            String prefId = "";
//            for (Name n : names) {
//                if (n.getNstat() == 8) {
//                    prefId = n.getNval();
//                    break;
//                }
//
//            }
//            return prefId;
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_NAMES_BY_GERMPLASM_ID);
//        }
//    }

//    private String getGermplasmNames(int gid) throws InternationalizableException {
//
//        try {
//            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
//            StringBuffer germplasmNames = new StringBuffer("");
//            int i = 0;
//            for (Name n : names) {
//                if (i < names.size() - 1) {
//                    germplasmNames.append(n.getNval() + ",");
//                } else {
//                    germplasmNames.append(n.getNval());
//                }
//                i++;
//            }
//
//            return germplasmNames.toString();
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_NAMES_BY_GERMPLASM_ID);
//        }
//    }

    private String getReference(int refId) throws MiddlewareQueryException{
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
            return ""; // TODO: Verify that this doesn't need ui error notification and really just returns ""
        }
    }

//    public GermplasmPedigreeTree generatePedigreeTree(Integer gid, int i) throws InternationalizableException {
//        try {
//            return pedigreeDataManager.generatePedigreeTree(gid, i);
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GENERATING_PEDIGREE_TREE);
//        }
//    }
//    
//    public GermplasmPedigreeTree generatePedigreeTree(Integer gid, int i, Boolean includeDerivativeLines) throws InternationalizableException {
//        try {
//            return pedigreeDataManager.generatePedigreeTree(gid, i, includeDerivativeLines);
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GENERATING_PEDIGREE_TREE);
//        }
//    }    

//    public GermplasmPedigreeTree getDerivativeNeighborhood(Integer gid, int numberOfStepsBackward, int numberOfStepsForward)
//            throws InternationalizableException {
//        try {
//            return pedigreeDataManager.getDerivativeNeighborhood(gid, numberOfStepsBackward, numberOfStepsForward);
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_DERIVATIVE_NEIGHBORHOOD);
//        }
//    }
//
//    
//    public GermplasmPedigreeTree getMaintenanceNeighborhood(Integer gid, int numberOfStepsBackward, int numberOfStepsForward)
//            throws InternationalizableException {
//        try {
//            return pedigreeDataManager.getMaintenanceNeighborhood(gid, numberOfStepsBackward, numberOfStepsForward);
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_DERIVATIVE_NEIGHBORHOOD);
//        }
//    }
    /**
        public List<GermplasmListData> getGermplasmListByGID(int gid) throws InternationalizableException {
                int count = this.germplasmListManager.countGermplasmListDataByGID(gid);

                try {
                        return this.germplasmListManager.getGermplasmListDataByGID(gid, 0, count);
                } catch (QueryException e) {
                        throw new InternationalizableException(e, Message.error_database, Message.error_in_getting_germplasm_list_by_id);
                }
        }
        **/

//    public List<LotReportRow> getReportOnLotsByEntityTypeAndEntityId(String type, Integer gid) throws InternationalizableException {
//        List<LotReportRow> result = new ArrayList<LotReportRow>();
//        try {
//            long count = this.inventoryDataManager.countLotsByEntityTypeAndEntityId(type, gid);
//            result = this.inventoryDataManager.generateReportOnLotsByEntityTypeAndEntityId(type, gid, 0, (int) count);
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE,
//                    Message.ERROR_IN_GETTING_REPORT_ON_LOTS_BY_ENTITY_TYPE_AND_ENTITY_ID);
//        }
//        return result;
//    }

//    public List<StudyReference> getGermplasmStudyInfo(int gid) throws InternationalizableException {
//        List<StudyReference> results = new ArrayList<StudyReference>();
//        try {
//            GidStudyQueryFilter gidFilter = new GidStudyQueryFilter(gid);
//            StudyResultSet resultSet = studyDataManager.searchStudies(gidFilter, 50);
//            while(resultSet.hasMore()){
//                StudyReference reference = resultSet.next();
//                results.add(reference);
//            }
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GERMPLASM_STUDY_INFORMATION_BY_GERMPLASM_ID);
//        }
//        return results;
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}