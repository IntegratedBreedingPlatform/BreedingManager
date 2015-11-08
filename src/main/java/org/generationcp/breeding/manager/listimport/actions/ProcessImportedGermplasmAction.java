
package org.generationcp.breeding.manager.listimport.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.listimport.GermplasmFieldsComponent;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listimport.NewDesignationForGermplasmConfirmDialog;
import org.generationcp.breeding.manager.listimport.SelectGermplasmWindow;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Window;

@Configurable
public class ProcessImportedGermplasmAction implements Serializable {

	private static final long serialVersionUID = -9047259985457065559L;
	private static final int PREFERRED_NAME_STATUS = 1;

	private final SpecifyGermplasmDetailsComponent germplasmDetailsComponent;

	private List<Integer> doNotCreateGermplasmsWithId = new ArrayList<>();
	private final List<ImportGermplasmEntryActionListener> importEntryListeners = new ArrayList<>();
	private List<GermplasmName> germplasmNameObjects = new ArrayList<>();
	private List<Name> newDesignationsForExistingGermplasm = new ArrayList<>();

	private Map<String, Germplasm> nameGermplasmMap = new HashMap<>();
	private static final Integer UNKNOWN_DERIVATIVE_METHOD = 31;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private ContextUtil contextUtil;

	public ProcessImportedGermplasmAction(final SpecifyGermplasmDetailsComponent germplasmDetailsComponent) {
		super();
		this.germplasmDetailsComponent = germplasmDetailsComponent;
		if (this.importEntryListeners != null) {
			this.importEntryListeners.clear();
		}
	}

	public void processGermplasm() {
		this.germplasmNameObjects = new ArrayList<>();
		this.doNotCreateGermplasmsWithId = new ArrayList<>();
		this.nameGermplasmMap = new HashMap<>();
		this.importEntryListeners.clear();
		this.newDesignationsForExistingGermplasm = new ArrayList<>();

		final String pedigreeOptionChosen = this.germplasmDetailsComponent.getPedigreeOption();
		if ("1".equalsIgnoreCase(pedigreeOptionChosen) && this.getImportedGermplasms() != null) {
			this.performFirstPedigreeAction();
		} else if ("2".equalsIgnoreCase(pedigreeOptionChosen) && this.getImportedGermplasms() != null) {
			this.performSecondPedigreeAction();
		} else if ("3".equalsIgnoreCase(pedigreeOptionChosen) && this.getImportedGermplasms() != null) {
			this.performThirdPedigreeAction();
		}
		if (this.importEntryListeners.isEmpty()) {
			this.saveImport();
		}
	}

	protected void performFirstPedigreeAction() {

		final Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();
		final Integer dateIntValue = this.getGermplasmDateValue();

		final Map<String, Germplasm> createdGermplasms = new HashMap<>();

		for (int i = 0; i < this.getImportedGermplasms().size(); i++) {
			final ImportedGermplasm importedGermplasm = this.getImportedGermplasms().get(i);

			final Germplasm germplasm = this.createGermplasmObject(i, -1, 0, 0, ibdbUserId, dateIntValue);

			final Name name = this.createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());

			if (!createdGermplasms.containsKey(name.getNval())) {
				createdGermplasms.put(name.getNval(), germplasm);

				this.germplasmNameObjects.add(new GermplasmName(germplasm, name));
			} else {
				this.germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()), name));
			}
		}
	}

	protected void performSecondPedigreeAction() {
		final Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();
		final Integer dateIntValue = this.getGermplasmDateValue();

		final Map<String, Germplasm> createdGermplasms = new HashMap<>();
		final Map<String, Integer> germplasmMatchesMap = this.mapImportedGermplasmsForDuplication(this.getImportedGermplasms());
		final Integer totalMatches = this.getTotalFromGermplasmMatches(germplasmMatchesMap);

		Integer currentMatch = 1;

		for (int i = 0; i < this.getImportedGermplasms().size(); i++) {

			final ImportedGermplasm importedGermplasm = this.getImportedGermplasms().get(i);
			final String designationName = importedGermplasm.getDesig();
			// gpid1 and gpid 2 values are default here, actual values will be set below based on matched germplasm
			final Germplasm germplasm = this.createGermplasmObject(i, -1, 0, 0, ibdbUserId, dateIntValue);
			List<Germplasm> foundGermplasm = new ArrayList<>();
			final Integer germplasmMatchesCount = germplasmMatchesMap.get(designationName);

			if (this.isGidSpecified(importedGermplasm)) {
				foundGermplasm.add(this.germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid()));
			} else if (germplasmMatchesCount == 1) {
				// If a single match is found, multiple matches will be
				// handled by SelectGemrplasmWindow and
				// then receiveGermplasmFromWindowAndUpdateGermplasmData()
				foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
			}

			if (foundGermplasm != null && !foundGermplasm.isEmpty() && foundGermplasm.get(0) != null) {
				this.updatePedigreeConnections(germplasm, foundGermplasm.get(0));
			}

			final Name name = this.createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());
			name.setNstat(ProcessImportedGermplasmAction.PREFERRED_NAME_STATUS);

			if (!createdGermplasms.containsKey(name.getNval())) {
				createdGermplasms.put(name.getNval(), germplasm);

				this.germplasmNameObjects.add(new GermplasmName(germplasm, name));
			} else {

				this.germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()), name));
			}

			if (germplasmMatchesCount > 1) {
				this.displaySelectGermplasmWindowIfNecessary(importedGermplasm.getDesig(), i, germplasm, currentMatch, totalMatches);
				//increment current match for referring to current iteration
				currentMatch++;
			}
		}
	}

	// Set imported germplasm's gpid1 and gpid2 based on source/connecting germplasm
	protected void updatePedigreeConnections(final Germplasm germplasm, final Germplasm sourceGermplasm) {
		if (germplasm.getGnpgs() == -1) {
			if (sourceGermplasm.getGpid1() == 0) {
				germplasm.setGpid1(sourceGermplasm.getGid());
			} else {
				germplasm.setGpid1(sourceGermplasm.getGpid1());
			}
		} else {
			germplasm.setGpid1(sourceGermplasm.getGid());
		}

		germplasm.setGpid2(sourceGermplasm.getGid());
	}

	protected void performThirdPedigreeAction() {
		final Integer ibdbUserId = this.contextUtil.getCurrentUserLocalId();
		final Integer dateIntValue = this.getGermplasmDateValue();

		Map<String, Germplasm> createdGermplasms = new HashMap<>();
		final Map<String, Integer> germplasmMatchesMap = this.mapImportedGermplasmsForDuplication(this.getImportedGermplasms());
		final Integer totalMatches = this.getTotalFromGermplasmMatches(germplasmMatchesMap);

		Integer currentMatch = 1;

		for (int i = 0; i < this.getImportedGermplasms().size(); i++) {

			ImportedGermplasm importedGermplasm = this.getImportedGermplasms().get(i);
			final String designationName = importedGermplasm.getDesig();
			Germplasm germplasm = new Germplasm();
			final Integer germplasmMatchesCount = germplasmMatchesMap.get(designationName);

			boolean searchByNameOrNewGermplasmIsNeeded = true;
			if (this.isGidSpecified(importedGermplasm)) {
				germplasm = this.germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());

				if (germplasm != null) {
					List<Name> names = this.germplasmDataManager.getNamesByGID(importedGermplasm.getGid(), 0, null);
					boolean thereIsMatchingName = false;
					for (Name name : names) {
						String nameInDb = name.getNval().toLowerCase();
						String nameInImportFile = importedGermplasm.getDesig().toLowerCase();
						String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile).toLowerCase();
						String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile).toLowerCase();

						if (nameInDb.equals(nameInImportFile) || nameInDb.equals(standardizedNameInImportFile) || nameInDb
								.equals(nameInImportFileWithSpacesRemoved)) {
							thereIsMatchingName = true;
						}
					}

					if (thereIsMatchingName) {
						this.doNotCreateGermplasmsWithId.add(importedGermplasm.getGid());
					} else {
						NewDesignationForGermplasmConfirmDialog dialog =
								new NewDesignationForGermplasmConfirmDialog(this, importedGermplasm.getDesig(), i,
										importedGermplasm.getGid(), ibdbUserId, dateIntValue, germplasmMatchesCount);

						this.addImportEntryListener(dialog);
					}

					searchByNameOrNewGermplasmIsNeeded = false;
				} else {
					MessageNotifier.showWarning(this.germplasmDetailsComponent.getWindow(), "Warning!",
							"GID: " + importedGermplasm.getGid() + " written on file does not exist in database.");
				}
			}

			if (germplasm == null) {
				germplasm = new Germplasm();
			}

			if (searchByNameOrNewGermplasmIsNeeded) {
				// gid at creation is temporary, will be set properly below
				germplasm = this.createGermplasmObject(i, 0, 0, 0, ibdbUserId, dateIntValue);

				if (germplasmMatchesCount == 1 && this.germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox()) {
					// If a single match is found, multiple matches will be
					// handled by SelectGemrplasmWindow and
					// then receiveGermplasmFromWindowAndUpdateGermplasmData()
					List<Germplasm> foundGermplasm =
							this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
					germplasm.setGid(foundGermplasm.get(0).getGid());
					this.doNotCreateGermplasmsWithId.add(foundGermplasm.get(0).getGid());
				}
			}

			Name name = this.createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());

			if (!createdGermplasms.containsKey(name.getNval())) {
				createdGermplasms.put(name.getNval(), germplasm);

				this.germplasmNameObjects.add(new GermplasmName(germplasm, name));
			} else {
				this.germplasmNameObjects.add(new GermplasmName(createdGermplasms.get(name.getNval()), name));
			}

			if (this.isNeedToDisplayGermplasmSelectionWindow(germplasmMatchesCount) && searchByNameOrNewGermplasmIsNeeded) {
				this.displaySelectGermplasmWindowIfNecessary(importedGermplasm.getDesig(), i, germplasm, currentMatch, totalMatches);
				currentMatch++;
			}
		}
	}

	/**
	 * Update GID to the existing germplasm's id. Otherwise, gid is set to 0
	 *
	 * @param ibdbUserId
	 * @param dateIntValue
	 * @param importedGermplasm
	 * @param germplasmMatchesCount
	 * @param germplasm
	 * @param searchByNameOrNewGermplasmIsNeeded
	 * @return
	 */
	Germplasm updateGidForSingleMatch(final Integer ibdbUserId, final Integer dateIntValue, final ImportedGermplasm importedGermplasm,
			final int germplasmMatchesCount, Germplasm germplasm, final boolean searchByNameOrNewGermplasmIsNeeded) {
		if (searchByNameOrNewGermplasmIsNeeded) {
			// gid at creation is temporary, will be set properly below
			germplasm = this.createGermplasmObject(0, 0, 0, 0, ibdbUserId, dateIntValue);

			if (germplasmMatchesCount == 1 && this.germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox()) {
				// If a single match is found, multiple matches will be
				// handled by SelectGemrplasmWindow and
				// then receiveGermplasmFromWindowAndUpdateGermplasmData()
				final List<Germplasm> foundGermplasm =
						this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1, Operation.EQUAL);
				germplasm.setGid(foundGermplasm.get(0).getGid());
				this.doNotCreateGermplasmsWithId.add(foundGermplasm.get(0).getGid());
			}
		}
		return germplasm;
	}

	//TODO : Method getCountByNamePermutations of GermplasmDataManager need to be used to improve performance and efficient DB call.
	private Map<String, Integer> mapImportedGermplasmsForDuplication(List<ImportedGermplasm> importedGermplasms) {

		Map<String, Integer> germplasmMatchesMap = new HashMap<>();

		for (final ImportedGermplasm germplasm : importedGermplasms) {
			String designationName = germplasm.getDesig();
			Integer count = (int) this.germplasmDataManager.countGermplasmByName(designationName, Operation.EQUAL);
			germplasmMatchesMap.put(designationName, count);
		}

		return germplasmMatchesMap;
	}

	private Integer getTotalFromGermplasmMatches(Map<String, Integer> germplasmMatchesMap){
		Integer totalMatches = 0;
		for (Integer count : germplasmMatchesMap.values()) {
			if(count > 0) {
				totalMatches++;
			}

		}
		return totalMatches;
	}

	protected boolean isNeedToDisplayGermplasmSelectionWindow(int germplasmMatchesCount) {
		return germplasmMatchesCount > 0 && !this.germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox();
	}

	protected boolean isGidSpecified(final ImportedGermplasm importedGermplasm) {
		return importedGermplasm.getGid() != null && !importedGermplasm.getGid().equals(Integer.valueOf(0));
	}

	public Name createNameObject(final Integer ibdbUserId, final Integer dateIntValue, final String desig) {
		final Name name = new Name();

		name.setTypeId((Integer) this.getGermplasmFieldsComponent().getNameTypeComboBox().getValue());
		name.setUserId(ibdbUserId);
		name.setNval(desig);
		name.setLocationId((Integer) this.getGermplasmFieldsComponent().getLocationComboBox().getValue());
		name.setNdate(dateIntValue);
		name.setReferenceId(0);

		return name;
	}

	protected Germplasm createGermplasmObject(final Integer gid, final Integer gnpgs, final Integer gpid1, final Integer gpid2,
			final Integer ibdbUserId, final Integer dateIntValue) {
		final Germplasm germplasm = new Germplasm();

		germplasm.setGid(gid);
		germplasm.setUserId(ibdbUserId);
		germplasm.setLocationId((Integer) this.getGermplasmFieldsComponent().getLocationComboBox().getValue());
		germplasm.setGdate(dateIntValue);

		final int methodId = this.getGermplasmMethodId(this.getGermplasmFieldsComponent().getBreedingMethodComboBox().getValue());
		germplasm.setMethodId(methodId);
		germplasm.setGnpgs(this.getGermplasmGnpgs(methodId, gnpgs));
		germplasm.setGpid1(gpid1);
		germplasm.setGpid2(gpid2);

		germplasm.setLgid(0);
		germplasm.setGrplce(0);
		germplasm.setReferenceId(0);
		germplasm.setMgid(0);

		return germplasm;
	}

	private int getGermplasmMethodId(final Object methodValue) {
		Integer methodId = 0;
		if (methodValue == null) {
			methodId = ProcessImportedGermplasmAction.UNKNOWN_DERIVATIVE_METHOD;
		} else {
			methodId = (Integer) methodValue;
		}
		return methodId;
	}

	private int getGermplasmGnpgs(final Integer methodId, final Integer prevGnpgs) {
		int gnpgs = 0;
		if (Objects.equals(methodId, ProcessImportedGermplasmAction.UNKNOWN_DERIVATIVE_METHOD)) {
			gnpgs = -1;
		} else {
			final Method selectedMethod = this.germplasmDataManager.getMethodByID(methodId);
			if ("GEN".equals(selectedMethod.getMtype())) {
				gnpgs = 2;
			} else {
				gnpgs = prevGnpgs;
			}
		}
		return gnpgs;
	}

	protected Integer getGermplasmDateValue() {
		String sDate = "";
		Integer dateIntValue = 0;
		final Date dateFieldValue = (Date) this.getGermplasmFieldsComponent().getGermplasmDateField().getValue();
		if (dateFieldValue != null && !"".equals(dateFieldValue.toString())) {
			sDate = DateUtil.formatDateAsStringValue(dateFieldValue, GermplasmImportMain.DATE_FORMAT);
			dateIntValue = Integer.parseInt(sDate.replace("-", ""));
		}
		return dateIntValue;
	}

	private SelectGermplasmWindow createSelectGermplasmWindow(final String germplasmName, final int i, final Germplasm germplasm) {
		return new SelectGermplasmWindow(this, germplasmName, i, germplasm, this.getWindow());
	}

	private void displaySelectGermplasmWindowIfNecessary(final String germplasmName,final int rowNumber,final Germplasm germplasm,final Integer currentMatch,final Integer totalMatches) {
		final SelectGermplasmWindow selectGermplasmWindow = this.createSelectGermplasmWindow(germplasmName, rowNumber, germplasm, currentMatch, totalMatches);
		this.addImportEntryListener(selectGermplasmWindow);
	}

	private SelectGermplasmWindow createSelectGermplasmWindow(String germplasmName, int rowNumber, Germplasm germplasm, Integer currentMatch, Integer totalMatches) {
		return new SelectGermplasmWindow(this, germplasmName, rowNumber, germplasm, this.getWindow(), currentMatch, totalMatches);
	}

	protected void addImportEntryListener(ImportGermplasmEntryActionListener listener) {
		if (this.importEntryListeners.isEmpty()) {
			this.showImportEntryListener(listener);
		}
		this.importEntryListeners.add(listener);
	}

	public void searchOrAddANewGermplasm(final NewDesignationForGermplasmConfirmDialog listener) {

		final int index = listener.getGermplasmIndex();
		final String desig = listener.getGermplasmName();
		final Germplasm germplasm = this.createGermplasmObject(index, 0, 0, 0, listener.getIbdbUserId(), listener.getDateIntValue());

		if (listener.getNameMatchesCount() == 1 && this.germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox()) {
			// If a single match is found, multiple matches will be
			// handled by SelectGemrplasmWindow and
			// then receiveGermplasmFromWindowAndUpdateGermplasmData()
			final List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(desig, 0, 1, Operation.EQUAL);

			final Integer gid = foundGermplasm.get(0).getGid();
			germplasm.setGid(gid);
			this.doNotCreateGermplasmsWithId.add(gid);
		}

		if (this.isNeedToDisplayGermplasmSelectionWindow(listener.getNameMatchesCount())) {
			// force process the select germplasm window first for this entry before other entries
			final SelectGermplasmWindow window = this.createSelectGermplasmWindow(desig, index, germplasm);
			this.importEntryListeners.add(0, window);

		} else {
			this.germplasmNameObjects.get(index).setGermplasm(germplasm);
		}

		this.removeCurrentListenerAndProcessNextItem(listener);

	}

	public void removeCurrentListenerAndProcessNextItem(final ImportGermplasmEntryActionListener listener) {
		this.removeListener(listener);
		this.processNextItems();
	}

	public void closeAllImportEntryListeners() {
		for (int i = 0; i < this.importEntryListeners.size(); i++) {
			final ImportGermplasmEntryActionListener listener = this.importEntryListeners.get(i);
			if (listener instanceof Window) {
				final Window window = (Window) listener;
				this.getWindow().removeWindow(window);
			}
		}
		this.importEntryListeners.clear();
		this.germplasmDetailsComponent.closeSaveListAsDialog();
		this.nameGermplasmMap.clear();
	}

	public void receiveGermplasmFromWindowAndUpdateGermplasmData(final int index, final Germplasm importedGermplasm,
			final Germplasm selectedGermplasm) {
		final String pedigreeOption = this.germplasmDetailsComponent.getPedigreeOption();
		if ("2".equalsIgnoreCase(pedigreeOption)) {
			// Update GPID 1 & 2 to values of selected germplasm, and update germplasmList using the updated germplasm
			this.updatePedigreeConnections(importedGermplasm, selectedGermplasm);

			this.germplasmNameObjects.get(index).setGermplasm(importedGermplasm);

		} else if ("3".equalsIgnoreCase(pedigreeOption)) {
			// Add logic here to not insert new record on DB when saved, maybe use existing GID?
			importedGermplasm.setGid(selectedGermplasm.getGid());
			this.doNotCreateGermplasmsWithId.add(selectedGermplasm.getGid());
			this.germplasmNameObjects.get(index).setGermplasm(importedGermplasm);
		}
	}

	public GermplasmFieldsComponent getGermplasmFieldsComponent() {
		return this.germplasmDetailsComponent.getGermplasmFieldsComponent();
	}

	public List<ImportedGermplasm> getImportedGermplasms() {
		return this.germplasmDetailsComponent.getImportedGermplasms();
	}

	public List<Integer> getMatchedGermplasmIds() {
		return this.doNotCreateGermplasmsWithId;
	}

	public List<GermplasmName> getGermplasmNameObjects() {
		return this.germplasmNameObjects;
	}

	public List<Name> getNewNames() {
		return this.newDesignationsForExistingGermplasm;
	}

	public Map<String, Germplasm> getNameGermplasmMap() {
		return this.nameGermplasmMap;
	}

	public void setNameGermplasmMap(final Map<String, Germplasm> nameGermplasmMap) {
		this.nameGermplasmMap = nameGermplasmMap;
	}

	public void removeListener(final ImportGermplasmEntryActionListener importEntryListener) {
		this.importEntryListeners.remove(importEntryListener);
	}

	public void processNextItems() {
		final Iterator<ImportGermplasmEntryActionListener> listenersIterator = this.importEntryListeners.iterator();
		if (!listenersIterator.hasNext()) {
			this.saveImport();
			return;
		}
		final ImportGermplasmEntryActionListener listener = listenersIterator.next();
		if (listener instanceof SelectGermplasmWindow) {
			final String germplasmName = listener.getGermplasmName();
			final int germplasmIndex = listener.getGermplasmIndex();
			final Germplasm germplasm = this.checkIfThereIsAMatch(germplasmName);
			if (germplasm != null) {
				this.nameGermplasmMap.get(germplasmName);
				this.germplasmNameObjects.get(germplasmIndex).setGermplasm(germplasm);
				this.removeListener(listener);
				this.processNextItems();
			} else {
				// If not from popup
				this.showImportEntryListener(listener);
			}

			// New Name confirm dialog
		} else {
			this.showImportEntryListener(listener);
		}
	}

	protected void showImportEntryListener(final ImportGermplasmEntryActionListener listener) {
		if (listener instanceof Window) {
			this.getWindow().addWindow((Window) listener);
		}

	}

	public Window getWindow() {
		if (this.germplasmDetailsComponent.getSource().getGermplasmImportPopupSource() == null) {
			return this.germplasmDetailsComponent.getWindow();
		} else {
			return this.germplasmDetailsComponent.getSource().getGermplasmImportPopupSource().getParentWindow();
		}
	}

	public void ignoreRemainingMatches() {
		final Iterator<ImportGermplasmEntryActionListener> listenersIterator = this.importEntryListeners.iterator();
		final List<ImportGermplasmEntryActionListener> selectWindows = new ArrayList<>();
		while (listenersIterator.hasNext()) {
			final ImportGermplasmEntryActionListener listener = listenersIterator.next();
			if (listener instanceof SelectGermplasmWindow) {
				final String germplasmName = listener.getGermplasmName();
				final int germplasmIndex = listener.getGermplasmIndex();
				final Germplasm germplasm = this.checkIfThereIsAMatch(germplasmName);
				if (germplasm != null) {
					this.germplasmNameObjects.get(germplasmIndex).setGermplasm(germplasm);
				}
				selectWindows.add(listener);
			}
		}
		if (!selectWindows.isEmpty()) {
			this.importEntryListeners.removeAll(selectWindows);
		}

		if (this.importEntryListeners.isEmpty()) {
			this.saveImport();
		} else {
			this.processNextItems();
		}
	}

	public void saveImport() {
		this.germplasmDetailsComponent.saveTheList();
	}

	public void mapGermplasmNamesToGermplasm(final String germplasmName, final Germplasm germplasm) {
		final String nameInImportFile = germplasmName.toLowerCase();
		final String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile).toLowerCase();
		final String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile).toLowerCase();

		this.nameGermplasmMap.put(nameInImportFile, germplasm);
		this.nameGermplasmMap.put(standardizedNameInImportFile, germplasm);
		this.nameGermplasmMap.put(nameInImportFileWithSpacesRemoved, germplasm);
	}

	public Germplasm checkIfThereIsAMatch(final String germplasmName) {
		if (this.nameGermplasmMap == null || this.nameGermplasmMap.isEmpty()) {
			return null;
		}
		final String nameInImportFile = germplasmName.toLowerCase();
		if (this.nameGermplasmMap.containsKey(nameInImportFile)) {
			return this.nameGermplasmMap.get(nameInImportFile);
		}
		final String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile).toLowerCase();
		if (this.nameGermplasmMap.containsKey(standardizedNameInImportFile)) {
			return this.nameGermplasmMap.get(standardizedNameInImportFile);
		}
		final String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile).toLowerCase();
		if (this.nameGermplasmMap.containsKey(nameInImportFileWithSpacesRemoved)) {
			return this.nameGermplasmMap.get(nameInImportFileWithSpacesRemoved);
		}
		return null;
	}

	public void addNameToGermplasm(final Name name, final Integer gid) {
		this.doNotCreateGermplasmsWithId.add(gid);
		this.newDesignationsForExistingGermplasm.add(name);
	}

	void setGermplasmDataManager(GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}
}
