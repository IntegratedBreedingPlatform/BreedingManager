package org.generationcp.breeding.manager.customcomponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DeletePrefixCacheDialogTest {

	private List<Integer> deletedGids;
	private Collection prefixes;

	@Mock
	private ControllableRefreshTable prefixesTable;

	@Mock
	private ListManagerMain source;

	@Mock
	private Window window;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private PlatformTransactionManager transactionManager;

	@InjectMocks
	private DeletePrefixCacheDialog dialog;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.dialog.setPrefixesTable(this.prefixesTable);
		this.deletedGids = Arrays.asList(1, 2);
		this.dialog.setDeletedGIDs(this.deletedGids);
		this.dialog.setGermplasmDataManager(this.germplasmDataManager);
		this.dialog.setMessageSource(this.messageSource);
		this.dialog.setCancelButton(new Button());
		this.dialog.setTransactionManager(this.transactionManager);


		Mockito.when(this.germplasmDataManager.getNamesByGidsAndPrefixes(ArgumentMatchers.eq(this.deletedGids), ArgumentMatchers.anyList()))
			.thenReturn(Collections.singletonList("PREF001"));
		this.prefixes = Collections.singletonList("PREF");
		Mockito.when(this.prefixesTable.getVisibleItemIds()).thenReturn(this.prefixes);

		Mockito.when(this.source.getWindow()).thenReturn(this.window);
	}

	@Test
	public void testDeletePrefixesSuccessDelete() {
		this.dialog.deletePrefixes();
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS);
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS_PREFIX_DELETE);
	}

	@Test
	public void testDeletePrefixesWithNoNamesWithSpecifiedPrefix() {
		Mockito.when(this.germplasmDataManager.getNamesByGidsAndPrefixes(ArgumentMatchers.eq(this.deletedGids), ArgumentMatchers.anyList()))
			.thenReturn(new ArrayList<>());
		this.dialog.deletePrefixes();
		Mockito.verify(this.messageSource).getMessage(Message.WARNING);
		Mockito.verify(this.messageSource).getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX);
	}

	@Test
	public void testDeleteKeyRegistersSuccess() {
		final List<String> names = Collections.singletonList("PREF*-(12) 001");
		this.dialog.deleteKeyRegisters(names, Collections.singletonList("PREF*-(12)"));
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS);
		Mockito.verify(this.messageSource).getMessage(Message.SUCCESS_PREFIX_DELETE);
	}

	@Test
	public void testDeleteKeyRegistersWithError() {
		final List<String> names = Collections.singletonList("PREFS 001");
		this.dialog.deleteKeyRegisters(names, Collections.singletonList("PREF"));
		Mockito.verify(this.messageSource).getMessage(Message.WARNING);
		Mockito.verify(this.messageSource).getMessage(Message.NO_EXISTING_NAME_WITH_PREFIX);
	}

	@Test
	public void testDeleteKeyRegistersWithWarning() {
		this.prefixes = Arrays.asList("PREF", "PREFS");
		Mockito.when(this.prefixesTable.getVisibleItemIds()).thenReturn(this.prefixes);
		final List<String> names = Collections.singletonList("PREF001");
		this.dialog.deleteKeyRegisters(names, Collections.singletonList("PREF"));
		Mockito.verify(this.messageSource).getMessage(Message.WARNING);
		Mockito.verify(this.messageSource).getMessage(Message.WARNING_PREFIX_DELETE,
			"1", "PREFS");
	}
}
