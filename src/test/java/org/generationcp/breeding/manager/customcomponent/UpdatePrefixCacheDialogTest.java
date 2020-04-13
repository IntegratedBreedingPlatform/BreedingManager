package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.KeySequenceRegister;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UpdatePrefixCacheDialogTest {

	private List<Integer> deletedGids;
	private Collection prefixes;

	@Mock
	private ControllableRefreshTable prefixesTable;

	@Mock
	private ListManagerMain source;

	@Mock
	private Label totalListEntriesLabel;

	@Mock
	private Window window;

	@Mock
	private Window parentWindow;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	private UpdatePrefixCacheDialog dialog;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.dialog.setPrefixesTable(this.prefixesTable);
		this.deletedGids = Arrays.asList(1, 2);
		this.dialog.setDeletedGIDs(this.deletedGids);
		this.dialog.setGermplasmDataManager(this.germplasmDataManager);
		this.dialog.setMessageSource(this.messageSource);
		this.dialog.setCancelButton(new Button());


		Mockito.when(this.germplasmDataManager.getNamesByGidsAndPrefixes(ArgumentMatchers.eq(this.deletedGids), ArgumentMatchers.anyList()))
			.thenReturn(Arrays.asList("PREF001"));
		this.prefixes = Collections.singletonList("PREF");
		Mockito.when(this.prefixesTable.getVisibleItemIds()).thenReturn(prefixes);

		Mockito.when(this.source.getWindow()).thenReturn(window);
	}

	@Test
	public void testDeletePrefixesSuccessDelete() {
		final KeySequenceRegister keySequenceRegister = new KeySequenceRegister(1, "PREF", 2, 2);
		Mockito.when(this.germplasmDataManager.getKeySequenceRegistersByPrefixes(ArgumentMatchers.anyList())).thenReturn(Collections.singletonList(keySequenceRegister));
		this.dialog.deletePrefixes();
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS);
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS_PREFIX_UPDATE);
	}

	@Test
	public void testDeletePrefixesWithNoNamesWithSpecifiedPrefix() {
		Mockito.when(this.germplasmDataManager.getNamesByGidsAndPrefixes(ArgumentMatchers.eq(this.deletedGids), ArgumentMatchers.anyList()))
			.thenReturn(new ArrayList<>());
		this.dialog.deletePrefixes();
		Mockito.verify(this.messageSource).getMessage(Message.ERROR);
		Mockito.verify(this.messageSource).getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX);
	}

	@Test
	public void testDeletePrefixesWithNoExistingPrefixes() {
		Mockito.when(this.germplasmDataManager.getKeySequenceRegistersByPrefixes(ArgumentMatchers.anyList())).thenReturn(new ArrayList<>());
		this.dialog.deletePrefixes();
		Mockito.verify(this.messageSource).getMessage(Message.ERROR);
		Mockito.verify(this.messageSource).getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX);
	}

	@Test
	public void testUpdateSequencesSuccess() {
		final KeySequenceRegister keySequenceRegister = new KeySequenceRegister(1, "PREF", 2, 2);
		final List<String> names = Arrays.asList("PREF 001");
		this.dialog.updateSequences(names, Collections.singletonList(keySequenceRegister));
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS);
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS_PREFIX_UPDATE);
	}

	@Test
	public void testUpdateSequencesWithError() {
		final KeySequenceRegister keySequenceRegister = new KeySequenceRegister(1, "PREF", 2, 2);
		final List<String> names = Arrays.asList("PREFS 001");
		this.dialog.updateSequences(names, Collections.singletonList(keySequenceRegister));
		Mockito.verify(this.messageSource).getMessage(Message.ERROR);
		Mockito.verify(this.messageSource).getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX);
	}

	@Test
	public void testUpdateSequencesWithWarning() {
		this.prefixes = Arrays.asList("PREF", "PREFS");
		Mockito.when(this.prefixesTable.getVisibleItemIds()).thenReturn(prefixes);
		final KeySequenceRegister keySequenceRegister = new KeySequenceRegister(1, "PREF", 2, 2);
		final List<String> names = Arrays.asList("PREF001");
		this.dialog.updateSequences(names, Collections.singletonList(keySequenceRegister));
		Mockito.verify(this.messageSource).getMessage(Message.WARNING);
		Mockito.verify(this.messageSource).getMessage(Message.WARNING_PREFIX_UPDATE,
			"1","1");
	}
}
