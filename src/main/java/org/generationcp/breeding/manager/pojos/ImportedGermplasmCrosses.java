package org.generationcp.breeding.manager.pojos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.pojos.ImportedCondition;
import org.generationcp.breeding.manager.pojos.ImportedConstant;
import org.generationcp.breeding.manager.pojos.ImportedFactor;
import org.generationcp.breeding.manager.pojos.ImportedVariate;

public class ImportedGermplasmCrosses {

    private String filename;
    private String study;
    private String title;
    private String pmKey;
    private String objective;
    private Date startDate;
    private Date endDate;
    private String type;
    
    private List<ImportedCondition> ImportedConditions;
    private List<ImportedFactor> ImportedFactors;
    private List<ImportedConstant> ImportedConstants;
    private List<ImportedVariate> ImportedVariates;
    private List<ImportedGermplasmCross> importedGermplasmCrosses;

    //TODO: consider renaming this class to something else (e.g. NurseryTemplateUploadInformation or something)
    // because ImportedGermplasmCrosses.importedGermplasmCrosses is confusing
    public ImportedGermplasmCrosses (String filename, String study, String title, String pmKey, String objective, Date startDate, Date endDate, String type){
        this.filename = filename;
        this.study = study;
        this.title = title;
        this.pmKey = pmKey;
        this.objective = objective;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.ImportedConditions = new ArrayList<ImportedCondition>();
        this.ImportedFactors = new ArrayList<ImportedFactor>();
        this.ImportedConstants = new ArrayList<ImportedConstant>();
        this.ImportedVariates = new ArrayList<ImportedVariate>();
        this.importedGermplasmCrosses = new ArrayList<ImportedGermplasmCross>();
    }
    
    public ImportedGermplasmCrosses (String filename, String study, String title, String pmKey, String objective, Date startDate, Date endDate, String type
            , List<ImportedCondition> ImportedConditions, List<ImportedFactor> ImportedFactors
            , List<ImportedConstant> ImportedConstants, List<ImportedVariate> ImportedVariates
            , List<ImportedGermplasmCrosses> importedGermplasmCrosses){
        
        this.filename = filename;
        this.study = study;
        this.title = title;
        this.pmKey = pmKey;
        this.objective = objective;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.ImportedConditions = new ArrayList<ImportedCondition>();
        this.ImportedFactors = new ArrayList<ImportedFactor>();
        this.ImportedConstants = new ArrayList<ImportedConstant>();
        this.ImportedVariates = new ArrayList<ImportedVariate>();
        this.importedGermplasmCrosses = new ArrayList<ImportedGermplasmCross>();
    }
    
    public String getFilename(){
        return filename;
    }
    
    public void setFilename(String filename){
        this.filename = filename;
    }
    
    public String getStudy(){
        return study;
    }
    
    public void setStudy(String study){
        this.study = study;
    }
    
    public String getTitle(){
        return title;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public String getPMKey(){
        return pmKey;
    }
    
    public void setPMKey(String pmKey){
        this.pmKey = pmKey;
    }    

    public String getObjective(){
        return objective;
    }
    
    public void setObjective(String objective){
        this.objective = objective;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type){
        this.type = type;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate){
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate){
        this.endDate = endDate;
    }
    
    public String getImportedConditionValue(String conditionName) {
        for (ImportedCondition cond : ImportedConditions) {
            if (cond.getCondition() != null
                    && cond.getCondition().equals(conditionName)) {
                return cond.getValue();
            }
        }
            return "";
    }
    
    public List<ImportedCondition> getImportedConditions(){
        return ImportedConditions;
    }
    
    public void setImportedConditions(List<ImportedCondition> ImportedConditions){
        this.ImportedConditions = ImportedConditions;
    }
    
    public void addImportedCondition(ImportedCondition ImportedCondition){
        this.ImportedConditions.add(ImportedCondition);
    }

    public List<ImportedFactor> getImportedFactors(){
        return ImportedFactors;
    }
    
    public void setImportedFactors(List<ImportedFactor> ImportedFactors){
        this.ImportedFactors = ImportedFactors;
    }

    public void addImportedFactor(ImportedFactor ImportedFactor){
        this.ImportedFactors.add(ImportedFactor);
    }    
    
    public List<ImportedConstant> getImportedConstants(){
        return ImportedConstants;
    }
    
    public void setImportedConstants(List<ImportedConstant> ImportedConstants){
        this.ImportedConstants = ImportedConstants;
    }

    public void addImportedConstant(ImportedConstant ImportedConstant){
        this.ImportedConstants.add(ImportedConstant);
    }
    
    public List<ImportedVariate> getImportedVariates(){
        return ImportedVariates;
    }
    
    public void setImportedVariates(List<ImportedVariate> ImportedVariates){
        this.ImportedVariates = ImportedVariates;
    }

    public void addImportedVariate(ImportedVariate ImportedVariate){
        this.ImportedVariates.add(ImportedVariate);
    }    

    public List<ImportedGermplasmCross> getImportedGermplasmCrosses(){
        return importedGermplasmCrosses;
    }
    
    public void setImportedGermplasmCross(List<ImportedGermplasmCross> importedGermplasmCrosses){
        this.importedGermplasmCrosses = importedGermplasmCrosses;
    }

    public void addImportedGermplasmCross(ImportedGermplasmCross importedGermplasmCross){
        this.importedGermplasmCrosses.add(importedGermplasmCross);
    }
    
    
};