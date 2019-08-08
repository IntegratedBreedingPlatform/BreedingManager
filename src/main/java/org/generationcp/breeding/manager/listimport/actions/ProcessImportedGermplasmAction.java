
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

	protected static final String CREATE_NEW_RECORD = "1";
	protected static final String CREATE_NEW_RECORD_WITH_PEDIGREE_CONN = "2";
	protected static final String SELECT_MATCHING_GERMPLASM = "3";
	private static final long serialVersionUID = -9047259985457065559L;
	private static final int PREFERRED_NAME_STATUS = 1;
	static final Integer DEFAULT_LOCATION_ID = 0;
	private final SpecifyGermplasmDetailsComponent germplasmDetailsComponent;

	private List<Integer> matchedGermplasmIds = new ArrayList<>();
	private List<ImportGermplasmEntryActionListener> importEntryListeners = new ArrayList<>();
	private List<GermplasmName> germplasmNameObjects = new ArrayList<>();
	private List<Name> newDesignationsForExistingGermplasm = new ArrayList<>();

	private Map<String, Germplasm> designationToGermplasmForReuseMap = new HashMap<>();
	static final Integer UNKNOWN_DERIVATIVE_METHOD = 31;

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
		this.matchedGermplasmIds = new ArrayList<>();
		this.designationToGermplasmForReuseMap = new HashMap<>();
		this.importEntryListeners.clear();
		this.newDesignationsForExistingGermplasm = new ArrayList<>();

		if (this.getImportedGermplasm() != null) {
			if (this.doCreateNewRecordsWithNoPedigreeConnection()) {
				this.performFirstPedigreeAction();

			} else if (this.doCreateNewRecordsWithPedigreeConnections()) {
				this.performSecondPedigreeAction();

			} else if (this.doSelectMatchingGermplasmWheneverFound()) {
				this.performThirdPedigreeAction();
			}
		}

		if (this.importEntryListeners.isEmpty()) {
			this.saveImport();
		}
	}

	private boolean doCreateNewRecordsWithNoPedigreeConnection() {
		return ProcessImportedGermplasmAction.CREATE_NEW_RECORD
				.equalsIgnoreCase(this.germplasmDetailsComponent.getPedigreeOption());
	}

	private boolean doCreateNewRecordsWithPedigreeConnections() {
		return ProcessImportedGermplasmAction.CREATE_NEW_RECORD_WITH_PEDIGREE_CONN
				.equalsIgnoreCase(this.germplasmDetailsComponent.getPedigreeOption());
	}

	private boolean doSelectMatchingGermplasmWheneverFound() {
		return ProcessImportedGermplasmAction.SELECT_MATCHING_GERMPLASM
				.equalsIgnoreCase(this.germplasmDetailsComponent.getPedigreeOption());
	}

	/**
	 * Creates new germplasm records without linking pedigree to any germplasm
	 * matches by designation
	 */
	protected void performFirstPedigreeAction() {

		final Integer ibdbUserId = this.contextUtil.getCurrentWorkbenchUserId();
		final Integer dateIntValue = this.getGermplasmDateValue();

		for (int i = 0; i < this.getImportedGermplasm().size(); i++) {
			final ImportedGermplasm importedGermplasm = this.getImportedGermplasm().get(i);

			final Germplasm germplasm = this.createGermplasmObject(i, -1, 0, 0, ibdbUserId, dateIntValue);
			final Name name = this.createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());

			this.germplasmNameObjects.add(new GermplasmName(germplasm, name));
		}
	}

	/**
	 * Creates new germplasm records and links pedigree to germplasm matched by
	 * designation. If there are multiple matches, user will be asked to select
	 * which one to link to.
	 */
	protected void performSecondPedigreeAction() {
		final Integer ibdbUserId = this.contextUtil.getCurrentWorkbenchUserId();
		final Integer dateIntValue = this.getGermplasmDateValue();

		final Map<String, Integer> germplasmMatchesMap = this.mapGermplasmNameCount(this.getImportedGermplasm());
		final Integer noOfImportedGermplasm = this.getImportedGermplasm().size();

		for (int i = 0; i < noOfImportedGermplasm; i++) {

			final ImportedGermplasm importedGermplasm = this.getImportedGermplasm().get(i);
			final String designationName = importedGermplasm.getDesig();
			// gpid1 and gpid 2 values are default here, actual values will be
			// set below based on matched germplasm
			final Germplasm germplasm = this.createGermplasmObject(i, -1, 0, 0, ibdbUserId, dateIntValue);
			List<Germplasm> foundGermplasm = new ArrayList<>();
			final Integer germplasmMatchesCount = germplasmMatchesMap.get(designationName);

			if (this.isGidSpecified(importedGermplasm)) {
				foundGermplasm.add(this.germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid()));
			} else if (germplasmMatchesCount == 1) {
				// If a single match is found, multiple matches will be
				// handled by SelectGermplasmWindow
				foundGermplasm = this.germplasmDataManager.getGermplasmByName(importedGermplasm.getDesig(), 0, 1,
						Operation.EQUAL);
			}

			if (foundGermplasm != null && !foundGermplasm.isEmpty() && foundGermplasm.get(0) != null) {
				this.updatePedigreeConnections(germplasm, foundGermplasm.get(0));
			}

			final Name name = this.createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());
			name.setNstat(ProcessImportedGermplasmAction.PREFERRED_NAME_STATUS);

			this.germplasmNameObjects.add(new GermplasmName(germplasm, name));

			if (germplasmMatchesCount > 1) {
				this.addSelectGermplasmWindowImportListener(importedGermplasm.getDesig(), i, noOfImportedGermplasm);
			}
		}
	}

	// Set imported germplasm's gpid1 and gpid2 based on source/connecting
	// germplasm
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

	/**
	 * Search matching germplasm 1) by GID, if specified and 2) by designation.
	 * If GID was specified and it was found, link to that one otherwise search
	 * for existing germplasm by given designation. If multiple matches are
	 * found, user will be asked to select which one to use.
	 */
	protected void performThirdPedigreeAction() {
		final Integer ibdbUserId = this.contextUtil.getCurrentWorkbenchUserId();
		final Integer dateIntValue = this.getGermplasmDateValue();

		final Map<String, Integer> germplasmMatchesMap = this.mapGermplasmNameCount(this.getImportedGermplasm());
		final Integer noOfImportedGermplasm = this.getImportedGermplasm().size();

		for (int i = 0; i < noOfImportedGermplasm; i++) {
			final ImportedGermplasm importedGermplasm = this.getImportedGermplasm().get(i);
			final String designationName = importedGermplasm.getDesig();
			final Integer germplasmMatchesCount = germplasmMatchesMap.get(designationName);
			boolean searchByNameOrNewGermplasmIsNeeded = true;

			// Create a germplasm with temporary GID, which will be replaced
			// with GID in file (if applicable) or when
			// once germplasm is matched by designation (either automatic for
			// single match) or via Select Germplasm pop-up
			final Germplasm germplasm = this.createGermplasmObject(i, 0, 0, 0, ibdbUserId, dateIntValue);
			final Name name = this.createNameObject(ibdbUserId, dateIntValue, importedGermplasm.getDesig());
			final GermplasmName germplasmToName = new GermplasmName(germplasm, name);
			this.germplasmNameObjects.add(germplasmToName);

			if (this.isGidSpecified(importedGermplasm)) {
				final Integer gidInFile = importedGermplasm.getGid();
				final Germplasm germplasmFound = this.germplasmDataManager.getGermplasmByGID(gidInFile);

				// Check if designation in file is one of the names of the GID
				// specified
				if (germplasmFound != null) {
					final List<Name> names = this.germplasmDataManager.getNamesByGID(gidInFile, 0, null);
					boolean thereIsMatchingName = false;
					for (final Name matchedName : names) {
						final String nameInDb = matchedName.getNval().toLowerCase();
						final String nameInImportFile = importedGermplasm.getDesig().toLowerCase();
						final String standardizedNameInImportFile = GermplasmDataManagerUtil
								.standardizeName(nameInImportFile).toLowerCase();
						final String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil
								.removeSpaces(nameInImportFile).toLowerCase();

						if (nameInDb.equals(nameInImportFile) || nameInDb.equals(standardizedNameInImportFile)
								|| nameInDb.equals(nameInImportFileWithSpacesRemoved)) {
							thereIsMatchingName = true;
						}
					}

					if (thereIsMatchingName) {
						this.addToMatchedGermplasmIds(gidInFile, i);
					} else {
						final NewDesignationForGermplasmConfirmDialog dialog = new NewDesignationForGermplasmConfirmDialog(
								this, importedGermplasm.getDesig(), i, gidInFile, ibdbUserId, dateIntValue,
								germplasmMatchesCount);

						this.addImportEntryListener(dialog);
					}
					searchByNameOrNewGermplasmIsNeeded = false;
				} else {
					MessageNotifier.showWarning(this.germplasmDetailsComponent.getWindow(), "Warning!",
							"GID: " + gidInFile + " written on file does not exist in database.");
				}
			}

			if (searchByNameOrNewGermplasmIsNeeded) {
				this.updateGidForSingleMatch(i, importedGermplasm, germplasmMatchesCount);
			}

			if (this.isNeedToDisplayGermplasmSelectionWindow(germplasmMatchesCount)
					&& searchByNameOrNewGermplasmIsNeeded) {
				this.addSelectGermplasmWindowImportListener(importedGermplasm.getDesig(), i, noOfImportedGermplasm);
			}

		}
	}

	/**
	 * If automatically accept checkbox was chosen and there is only one match
	 * for designation of imported germplasm, update to GID of matched
	 * germplasm. If automatically accept checkbox was chosen, try to find a
	 * match from previous entries in the file that had no name match in DB and
	 * reuse that germplasm.
	 *
	 * @param index
	 *            - index of entry in import file
	 * @param importedGermplasm
	 *            - imported germplasm as specified in file
	 * @param germplasmMatchesCount
	 *            - number of germplasm matched by given designation in file
	 * @return
	 */
	void updateGidForSingleMatch(final Integer index, final ImportedGermplasm importedGermplasm,
			final int germplasmMatchesCount) {
		// If a single match is found on designation, add it to matched
		// germplasm IDs so new germplasm won't be created
		// Multiple matches will be handled by SelectGermplasmWindow
		if (this.germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox()) {
			final String designation = importedGermplasm.getDesig();
			if (germplasmMatchesCount == 1) {
				final List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(designation, 0, 1,
						Operation.EQUAL);
				this.addToMatchedGermplasmIds(foundGermplasm.get(0).getGid(), index);

				// If name not yet in DB, try to find a match from previous
				// entries in the file so that same germplasm can be reused
			} else if (germplasmMatchesCount == 0) {
				final Germplasm germplasmToReuse = this.retrieveGermplasmToReuseForDesignation(designation);
				if (germplasmToReuse != null) {
					this.germplasmNameObjects.get(index).getGermplasm().setGid(germplasmToReuse.getGid());
				} else {
					this.mapDesignationToGermplasmForReuse(designation, index);
				}

			}
		}
	}

	/**
	 * Creates map of germplasm name count with the germplasm designation as the
	 * key.
	 *
	 * @param importedGermplasm
	 *
	 * @return germplasmMatchesMap
	 */
	private Map<String, Integer> mapGermplasmNameCount(final List<ImportedGermplasm> importedGermplasm) {

		final Map<String, Integer> germplasmMatchesMap = new HashMap<>();
		for (final ImportedGermplasm germplasm : importedGermplasm) {
			final String designationName = germplasm.getDesig();
			final Integer count = (int) this.germplasmDataManager.countGermplasmByName(designationName,
					Operation.EQUAL);
			germplasmMatchesMap.put(designationName, count);
		}

		return germplasmMatchesMap;
	}

	protected boolean isNeedToDisplayGermplasmSelectionWindow(final int germplasmMatchesCount) {
		return germplasmMatchesCount > 1 || germplasmMatchesCount == 1
				&& !this.germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox();
	}

	protected boolean isGidSpecified(final ImportedGermplasm importedGermplasm) {
		return importedGermplasm.getGid() != null && !importedGermplasm.getGid().equals(Integer.valueOf(0));
	}

	public Name createNameObject(final Integer ibdbUserId, final Integer dateIntValue, final String desig) {
		final Name name = new Name();

		name.setTypeId((Integer) this.getGermplasmFieldsComponent().getNameTypeComboBox().getValue());
		name.setUserId(ibdbUserId);
		name.setNval(desig);

		// Set the location id to the id of Unknown Location (0) if the user did
		// not select any location
		final Object locationIdObject = this.getGermplasmFieldsComponent().getLocationComboBox().getValue();
		final Integer locationID = locationIdObject != null ? Integer.valueOf(locationIdObject.toString())
				: ProcessImportedGermplasmAction.DEFAULT_LOCATION_ID;
		name.setLocationId(locationID);

		name.setNdate(dateIntValue);
		name.setReferenceId(0);

		return name;
	}

	protected Germplasm createGermplasmObject(final Integer gid, final Integer gnpgs, final Integer gpid1,
			final Integer gpid2, final Integer ibdbUserId, final Integer dateIntValue) {
		final Germplasm germplasm = new Germplasm();

		germplasm.setGid(gid);
		germplasm.setUserId(ibdbUserId);

		// Set the location id to the id of Unknown Location (0) if the user did
		// not select any location
		final Object locationIdObject = this.getGermplasmFieldsComponent().getLocationComboBox().getValue();
		final Integer locationID = locationIdObject != null ? Integer.valueOf(locationIdObject.toString())
				: ProcessImportedGermplasmAction.DEFAULT_LOCATION_ID;
		germplasm.setLocationId(locationID);

		germplasm.setGdate(dateIntValue);

		final int methodId = this
				.getGermplasmMethodId(this.getGermplasmFieldsComponent().getBreedingMethodComboBox().getValue());
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

	private SelectGermplasmWindow createSelectGermplasmWindow(final String designation, final int index) {
		return new SelectGermplasmWindow(this, designation, index, this.getWindow());
	}

	void addSelectGermplasmWindowImportListener(final String designation, final int rowNumber,
			final Integer noOfImportedGermplasm) {
		final SelectGermplasmWindow selectGermplasmWindow = this.createSelectGermplasmWindow(designation, rowNumber,
				noOfImportedGermplasm);
		this.addImportEntryListener(selectGermplasmWindow);
	}

	private SelectGermplasmWindow createSelectGermplasmWindow(final String designation, final int rowNumber,
			final Integer noOfImportedGermplasm) {
		return new SelectGermplasmWindow(this, designation, rowNumber, this.getWindow(), noOfImportedGermplasm);
	}

	void addImportEntryListener(final ImportGermplasmEntryActionListener listener) {
		if (this.importEntryListeners.isEmpty()) {
			this.showImportEntryListener(listener);
		}
		this.importEntryListeners.add(listener);
	}

	public void searchOrAddANewGermplasm(final NewDesignationForGermplasmConfirmDialog listener) {

		final int index = listener.getGermplasmIndex();
		final String desig = listener.getDesignation();
		final Germplasm germplasm = this.createGermplasmObject(index, 0, 0, 0, listener.getIbdbUserId(),
				listener.getDateIntValue());

		if (listener.getNameMatchesCount() == 1
				&& this.germplasmDetailsComponent.automaticallyAcceptSingleMatchesCheckbox()) {
			final List<Germplasm> foundGermplasm = this.germplasmDataManager.getGermplasmByName(desig, 0, 1,
					Operation.EQUAL);
			final Integer gid = foundGermplasm.get(0).getGid();
			germplasm.setGid(gid);
			this.addToMatchedGermplasmIds(gid, index);
		}

		if (this.isNeedToDisplayGermplasmSelectionWindow(listener.getNameMatchesCount())) {
			// force process the select germplasm window first for this entry
			// before other entries
			final SelectGermplasmWindow window = this.createSelectGermplasmWindow(desig, index);
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
		this.designationToGermplasmForReuseMap.clear();
	}

	public void receiveGermplasmFromWindowAndUpdateGermplasmData(final int index, final Germplasm selectedGermplasm) {
		if (this.doCreateNewRecordsWithPedigreeConnections()) {
			// Update GPID 1 & 2 to values of selected germplasm, and update
			// germplasmList using the updated germplasm
			this.updatePedigreeConnections(this.germplasmNameObjects.get(index).getGermplasm(), selectedGermplasm);

		} else if (this.doSelectMatchingGermplasmWheneverFound()) {
			this.addToMatchedGermplasmIds(selectedGermplasm.getGid(), index);
			this.germplasmNameObjects.get(index).getGermplasm().setGid(selectedGermplasm.getGid());
		}
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
			final String designation = listener.getDesignation();
			final Germplasm germplasmToReuse = this.retrieveGermplasmToReuseForDesignation(designation);
			final int index = listener.getGermplasmIndex();

			// Check if there is a germplasm previously selected that has been
			// marked for reuse for same designation
			if (germplasmToReuse != null) {
				if (this.doCreateNewRecordsWithPedigreeConnections()) {
					this.updatePedigreeConnections(this.germplasmNameObjects.get(index).getGermplasm(),
							germplasmToReuse);

				} else if (this.doSelectMatchingGermplasmWheneverFound()) {
					this.setMatchedGermplasmGid(germplasmToReuse.getGid(), this.germplasmNameObjects.get(index));
				}
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
			if (listener instanceof SelectGermplasmWindow) {
				((SelectGermplasmWindow) listener).initializeTableValues();
			}
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
				final String germplasmName = listener.getDesignation();
				final int germplasmIndex = listener.getGermplasmIndex();
				final Germplasm germplasm = this.retrieveGermplasmToReuseForDesignation(germplasmName);
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

	public void mapDesignationToGermplasmForReuse(final String designation, final Integer index) {
		if (this.designationToGermplasmForReuseMap == null) {
			this.designationToGermplasmForReuseMap = new HashMap<>();
		}
		final String nameInImportFile = designation.toLowerCase();
		final String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile)
				.toLowerCase();
		final String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile)
				.toLowerCase();
		final Germplasm germplasm = this.germplasmNameObjects.get(index).getGermplasm();

		this.designationToGermplasmForReuseMap.put(nameInImportFile, germplasm);
		this.designationToGermplasmForReuseMap.put(standardizedNameInImportFile, germplasm);
		this.designationToGermplasmForReuseMap.put(nameInImportFileWithSpacesRemoved, germplasm);
	}

	public Germplasm retrieveGermplasmToReuseForDesignation(final String designation) {
		if (this.designationToGermplasmForReuseMap == null || this.designationToGermplasmForReuseMap.isEmpty()) {
			return null;
		}
		final String nameInImportFile = designation.toLowerCase();
		if (this.designationToGermplasmForReuseMap.containsKey(nameInImportFile)) {
			return this.designationToGermplasmForReuseMap.get(nameInImportFile);
		}
		final String standardizedNameInImportFile = GermplasmDataManagerUtil.standardizeName(nameInImportFile)
				.toLowerCase();
		if (this.designationToGermplasmForReuseMap.containsKey(standardizedNameInImportFile)) {
			return this.designationToGermplasmForReuseMap.get(standardizedNameInImportFile);
		}
		final String nameInImportFileWithSpacesRemoved = GermplasmDataManagerUtil.removeSpaces(nameInImportFile)
				.toLowerCase();
		if (this.designationToGermplasmForReuseMap.containsKey(nameInImportFileWithSpacesRemoved)) {
			return this.designationToGermplasmForReuseMap.get(nameInImportFileWithSpacesRemoved);
		}
		return null;
	}

	public void addNameToGermplasm(final Name name, final Integer gid, final Integer index) {
		this.addToMatchedGermplasmIds(gid, index);
		this.newDesignationsForExistingGermplasm.add(name);
	}

	void addToMatchedGermplasmIds(final Integer gid, final Integer index) {
		this.matchedGermplasmIds.add(gid);
		final GermplasmName germplasmToName = this.germplasmNameObjects.get(index);
		if (germplasmToName != null) {
			this.setMatchedGermplasmGid(gid, germplasmToName);
		}
	}

	void setMatchedGermplasmGid(final Integer gid, final GermplasmName germplasmToName) {
		germplasmToName.setIsGidMatched(true);
		germplasmToName.getGermplasm().setGid(gid);
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	List<ImportGermplasmEntryActionListener> getImportEntryListeners() {
		return this.importEntryListeners;
	}

	public void setGermplasmNameObjects(final List<GermplasmName> germplasmNameObjects) {
		this.germplasmNameObjects = germplasmNameObjects;
	}

	public void setImportEntryListener(final List<ImportGermplasmEntryActionListener> importEntryListeners) {
		this.importEntryListeners = importEntryListeners;
	}

	public GermplasmFieldsComponent getGermplasmFieldsComponent() {
		return this.germplasmDetailsComponent.getGermplasmFieldsComponent();
	}

	public List<ImportedGermplasm> getImportedGermplasm() {
		return this.germplasmDetailsComponent.getImportedGermplasm();
	}

	public List<Integer> getMatchedGermplasmIds() {
		return this.matchedGermplasmIds;
	}

	public List<GermplasmName> getGermplasmNameObjects() {
		return this.germplasmNameObjects;
	}

	public List<Name> getNewNames() {
		return this.newDesignationsForExistingGermplasm;
	}

	public Map<String, Germplasm> getDesignationToGermplasmForReuseMap() {
		return this.designationToGermplasmForReuseMap;
	}

	public void setDesignationToGermplasmForReuseMap(final Map<String, Germplasm> designationToGermplasmForReuseMap) {
		this.designationToGermplasmForReuseMap = designationToGermplasmForReuseMap;
	}

}
