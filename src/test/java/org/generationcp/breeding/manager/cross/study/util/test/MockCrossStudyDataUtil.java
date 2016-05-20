
package org.generationcp.breeding.manager.cross.study.util.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.cross.study.constants.EnvironmentWeight;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.ObservationKey;

import com.vaadin.ui.ComboBox;

public class MockCrossStudyDataUtil {

	private static final int ENVT_COUNT = 47;

	// Trait 1 Observations
	private static final Double[] G1_T1_OBS = {2250.0, 3000.0, 1185.0, 3300.0, 2231.0, 3967.0, 6535.0, 1400.0, 4.75, 3853.0, 1200.0,
			2500.0, 1366.0, 1234.0, 3063.0, 2893.0, 5460.0, 3.241, 367.0, 807.0, 1790.0
		// ff values should not be included in computation
		, 67.0, 123.4, 89.7, 99.0, 1236.0};

	private static final Double[] G2_T1_OBS = {1950.0, 2850.0, 745.0, 3500.0, 3022.0, 5651.0, 3216.0, 1100.0, 3.63, 3900.0, 1300.0, 2714.0,
			0.0, 1588.0, 1879.0, 2761.0, 930.0, 0.5, 0.0, 1006.0, 591.0};

	// Trait 2 Observations
	private static final Double[] G1_T2_OBS = {119.4, 75.3, 92.0, 122.2, 100.0, 85.0, 115.0, 115.0, 95.0, 95.0, 53.0, 120.67, 94.0, 88.0,
			100.0, 98.2, 100.2, 85.0, 111.0, 116.0, 99.5, 91.4, 88.6, 86.0, 91.0, 15.4};

	private static final Double[] G2_T2_OBS = {110.3, 82.6, 76.0, 111.0, 0.0, 85.0, 122.0, 126.0, 90.0, 61.0, 75.0, 109.67, 91.0, 81.0,
			114.0, 0.0, 121.8, 98.0, 111.0, 112.0, 74.0, 77.0, 91.4, 70.0, 81.0, 82.8
			// ff values should not be included in computation
			, 107.0, 98.6, 65.9, 77.0, 999.0, 95.6, 88.8, 89.92, 100.5, 74.0};

	/**
	 * Create mock observation map for given trait and germplasms. Includes a few observations on GID1 on environments that don't have GID2
	 *
	 * @param traitId
	 * @param gid1
	 * @param gid2
	 * @return
	 */
	public static Map<String, ObservationList> getHeadToHeadData(int tid1, int tid2, int gid1, int gid2) {
		// key format: <traitId>:<envtId>:<gid>
		final Map<String, ObservationList> headToHeadMap = new HashMap<String, ObservationList>();

		MockCrossStudyDataUtil.createTraitOneData(tid1, gid1, gid2, headToHeadMap);
		MockCrossStudyDataUtil.createTraitTwoData(tid2, gid1, gid2, headToHeadMap);
		return headToHeadMap;
	}

	private static void createTraitOneData(int traitId, int gid1, int gid2, final Map<String, ObservationList> headToHeadMap) {
		for (int i = 0; i < MockCrossStudyDataUtil.G2_T1_OBS.length; i++) {
			int envtId = i + 1;

			String key = MockCrossStudyDataUtil.buildObsKey(traitId, envtId, gid1);
			ObservationList obsList = new ObservationList(key);
			obsList.addObservation(new Observation(new ObservationKey(traitId, gid1, envtId), MockCrossStudyDataUtil.G1_T1_OBS[i]
					.toString()));
			headToHeadMap.put(key, obsList);

			key = MockCrossStudyDataUtil.buildObsKey(traitId, envtId, gid2);
			obsList = new ObservationList(key);
			obsList.addObservation(new Observation(new ObservationKey(traitId, gid2, envtId), MockCrossStudyDataUtil.G2_T1_OBS[i]
					.toString()));
			headToHeadMap.put(key, obsList);
		}

		// add in observations for envts for GID 1 only
		int extraCount = MockCrossStudyDataUtil.G1_T1_OBS.length - MockCrossStudyDataUtil.G2_T1_OBS.length;
		for (int i = 0; i < extraCount; i++) {
			int index = MockCrossStudyDataUtil.G1_T1_OBS.length - i - 1;
			int envtId = index + 1;
			String key = MockCrossStudyDataUtil.buildObsKey(traitId, envtId, gid1);
			ObservationList obsList = new ObservationList(key);
			obsList.addObservation(new Observation(new ObservationKey(traitId, gid1, envtId), MockCrossStudyDataUtil.G1_T1_OBS[index]
					.toString()));
			headToHeadMap.put(key, obsList);
		}
	}

	private static void createTraitTwoData(int traitId, int gid1, int gid2, final Map<String, ObservationList> headToHeadMap) {
		for (int i = 0; i < MockCrossStudyDataUtil.G1_T2_OBS.length; i++) {
			int envtId = i + 1;

			String key = MockCrossStudyDataUtil.buildObsKey(traitId, envtId, gid1);
			ObservationList obsList = new ObservationList(key);
			obsList.addObservation(new Observation(new ObservationKey(traitId, gid1, envtId), MockCrossStudyDataUtil.G1_T2_OBS[i]
					.toString()));
			headToHeadMap.put(key, obsList);

			key = MockCrossStudyDataUtil.buildObsKey(traitId, envtId, gid2);
			obsList = new ObservationList(key);
			obsList.addObservation(new Observation(new ObservationKey(traitId, gid2, envtId), MockCrossStudyDataUtil.G2_T2_OBS[i]
					.toString()));
			headToHeadMap.put(key, obsList);
		}

		// add in observations for envts for GID 2 only
		int extraCount = MockCrossStudyDataUtil.G2_T2_OBS.length - MockCrossStudyDataUtil.G1_T2_OBS.length;
		for (int i = 0; i < extraCount; i++) {
			int index = MockCrossStudyDataUtil.G2_T2_OBS.length - i - 1;
			int envtId = index + 1;
			String key = MockCrossStudyDataUtil.buildObsKey(traitId, envtId, gid1);
			ObservationList obsList = new ObservationList(key);
			obsList.addObservation(new Observation(new ObservationKey(traitId, gid1, envtId), MockCrossStudyDataUtil.G2_T2_OBS[index]
					.toString()));
			headToHeadMap.put(key, obsList);
		}
	}

	public static List<EnvironmentForComparison> getEqualEnvironmentForComparisons() {
		List<EnvironmentForComparison> envtList = new ArrayList<EnvironmentForComparison>();

		for (int i = 0; i < MockCrossStudyDataUtil.ENVT_COUNT; i++) {
			int envtId = i + 1;

			ComboBox weightCombo = new ComboBox();
			weightCombo.addItem(EnvironmentWeight.IMPORTANT);
			weightCombo.setValue(EnvironmentWeight.IMPORTANT);
			EnvironmentForComparison environment = new EnvironmentForComparison(envtId, "ENVT " + envtId, "", "", weightCombo);
			envtList.add(environment);
		}

		return envtList;
	}

	public static List<EnvironmentForComparison> getVariedEnvironmentForComparisons() {
		List<EnvironmentForComparison> envtList = new ArrayList<EnvironmentForComparison>();

		for (int i = 0; i < MockCrossStudyDataUtil.ENVT_COUNT; i++) {
			int envtId = i + 1;
			int mod = i % 4;

			EnvironmentWeight weight = EnvironmentWeight.IGNORED;
			switch (mod) {
				case 0:
					weight = EnvironmentWeight.IGNORED;
					break;

				case 1:
					weight = EnvironmentWeight.DESIRABLE;
					break;

				case 2:
					weight = EnvironmentWeight.IMPORTANT;
					break;

				case 3:
					weight = EnvironmentWeight.CRITICAL;
					break;
			}

			ComboBox weightCombo = new ComboBox();
			weightCombo.addItem(weight);
			weightCombo.setValue(weight);
			EnvironmentForComparison environment = new EnvironmentForComparison(envtId, "ENVT " + envtId, "", "", weightCombo);
			envtList.add(environment);
		}

		return envtList;
	}

	private static String buildObsKey(int traitId, int envtId, int gid) {
		return traitId + ":" + envtId + ":" + gid;
	}

}
