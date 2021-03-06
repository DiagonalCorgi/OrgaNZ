package com.humanharvest.organz.actions.client;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import com.humanharvest.organz.BaseTest;
import com.humanharvest.organz.Client;
import com.humanharvest.organz.MedicationRecord;
import com.humanharvest.organz.actions.ActionInvoker;
import com.humanharvest.organz.actions.client.medication.ModifyMedicationRecordAction;
import com.humanharvest.organz.state.ClientManager;
import com.humanharvest.organz.state.ClientManagerMemory;

import org.junit.Before;
import org.junit.Test;

public class ModifyMedicationRecordActionTest extends BaseTest {

    private ActionInvoker invoker;
    private ClientManager manager;
    private Client baseClient;
    private MedicationRecord record;

    @Before
    public void init() {
        invoker = new ActionInvoker();
        manager = new ClientManagerMemory();
        baseClient = new Client("First", null, "Last", LocalDate.of(1970, 1, 1), 1);
        record = new MedicationRecord("Generic Name", LocalDate.of(2018, 4, 9), null);
        baseClient.addMedicationRecord(record);
    }

    @Test
    public void ModifySingleMedicationCurrentTest() {
        ModifyMedicationRecordAction action = new ModifyMedicationRecordAction(record, manager);

        LocalDate newDate = LocalDate.of(2018, 4, 11);

        action.changeStarted(newDate);

        invoker.execute(action);

        assertEquals(newDate, baseClient.getCurrentMedications().get(0).getStarted());
    }

    @Test
    public void ModifySingleMedicationCurrentToPastTest() {
        ModifyMedicationRecordAction action = new ModifyMedicationRecordAction(record, manager);

        LocalDate newDate = LocalDate.of(2018, 4, 11);

        action.changeStopped(newDate);

        invoker.execute(action);

        assertEquals(1, baseClient.getPastMedications().size());
        assertEquals(newDate, baseClient.getPastMedications().get(0).getStopped());
    }

    @Test
    public void ModifySingleMedicationCurrentUndoTest() {
        ModifyMedicationRecordAction action = new ModifyMedicationRecordAction(record, manager);

        LocalDate newDate = LocalDate.of(2018, 4, 11);

        action.changeStarted(newDate);

        invoker.execute(action);
        invoker.undo();

        assertEquals(LocalDate.of(2018, 4, 9), baseClient.getCurrentMedications().get(0).getStarted());
    }

    @Test
    public void ModifySingleMedicationCurrentUndoRedoTest() {
        ModifyMedicationRecordAction action = new ModifyMedicationRecordAction(record, manager);

        LocalDate newDate = LocalDate.of(2018, 4, 11);

        action.changeStarted(newDate);

        invoker.execute(action);
        invoker.undo();
        invoker.redo();

        assertEquals(newDate, baseClient.getCurrentMedications().get(0).getStarted());
    }

}
