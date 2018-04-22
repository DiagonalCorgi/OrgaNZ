package seng302.Actions.Client;


import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import seng302.Actions.ActionInvoker;
import seng302.Client;
import seng302.State.ClientManager;

import org.junit.Before;
import org.junit.Test;

public class CreateClientActionTest {

    private ClientManager manager;
    private ActionInvoker invoker;
    private Client baseClient;

    @Before
    public void init() {
        invoker = new ActionInvoker();
        manager = new ClientManager();
        baseClient = new Client("First", null, "Last", LocalDate.of(1970, 1, 1), 1);
    }

    @Test
    public void CheckClientAddedTest() {
        CreateClientAction action = new CreateClientAction(baseClient, manager);
        invoker.execute(action);
        assertEquals(1, manager.getPeople().size());
    }

    @Test
    public void CheckClientAddedUndoTest() {
        CreateClientAction action = new CreateClientAction(baseClient, manager);
        invoker.execute(action);
        invoker.undo();
        assertEquals(0, manager.getPeople().size());
    }

    @Test
    public void CheckClientMultipleAddsOneUndoTest() {
        CreateClientAction action = new CreateClientAction(baseClient, manager);
        invoker.execute(action);
        Client second = new Client("SecondClient", null, "Last", LocalDate.of(1970, 1, 1), 1);
        CreateClientAction secondAction = new CreateClientAction(second, manager);
        invoker.execute(secondAction);
        invoker.undo();
        assertEquals(baseClient, manager.getPeople().get(0));
        assertEquals(1, manager.getPeople().size());
    }

    @Test
    public void CheckClientMultipleAddsOneUndoRedoTest() {
        CreateClientAction action = new CreateClientAction(baseClient, manager);
        invoker.execute(action);
        Client second = new Client("SecondClient", null, "Last", LocalDate.of(1970, 1, 1), 1);
        CreateClientAction secondAction = new CreateClientAction(second, manager);
        invoker.execute(secondAction);
        invoker.undo();

        assertEquals(baseClient, manager.getPeople().get(0));
        assertEquals(1, manager.getPeople().size());

        invoker.redo();

        assertEquals(baseClient, manager.getPeople().get(1));
        assertEquals(2, manager.getPeople().size());
    }
}
