package seng302.Actions.Administrator;


import static org.junit.Assert.assertEquals;

import seng302.Actions.ActionInvoker;
import seng302.Administrator;
import seng302.State.AdministratorManager;

import org.junit.Before;
import org.junit.Test;

public class CreateAdministratorActionTest {

    private AdministratorManager manager;
    private ActionInvoker invoker;
    private Administrator baseAdministrator;

    @Before
    public void init() {
        invoker = new ActionInvoker();
        manager = new AdministratorManager();
        baseAdministrator = new Administrator("First", "pass");
    }

    @Test
    public void checkAdministratorAddedTest() {
        CreateAdministratorAction action = new CreateAdministratorAction(baseAdministrator, manager);
        invoker.execute(action);
        assertEquals(2, manager.getAdministrators().size());
    }

    @Test
    public void checkAdministratorAddedUndoTest() {
        CreateAdministratorAction action = new CreateAdministratorAction(baseAdministrator, manager);
        invoker.execute(action);
        invoker.undo();
        assertEquals(1, manager.getAdministrators().size());
    }

    @Test
    public void checkAdministratorMultipleAddsOneUndoTest() {
        CreateAdministratorAction action = new CreateAdministratorAction(baseAdministrator, manager);
        invoker.execute(action);
        Administrator second = new Administrator("First2", "pass");
        CreateAdministratorAction secondAction = new CreateAdministratorAction(second, manager);
        invoker.execute(secondAction);
        invoker.undo();
        assertEquals(baseAdministrator, manager.getAdministrators().get(1));
        assertEquals(2, manager.getAdministrators().size());
    }

    @Test
    public void checkAdministratorMultipleAddsOneUndoRedoTest() {
        CreateAdministratorAction action = new CreateAdministratorAction(baseAdministrator, manager);
        invoker.execute(action);
        Administrator second = new Administrator("First2", "pass");
        CreateAdministratorAction secondAction = new CreateAdministratorAction(second, manager);
        invoker.execute(secondAction);
        invoker.undo();

        assertEquals(baseAdministrator, manager.getAdministrators().get(1));
        assertEquals(2, manager.getAdministrators().size());

        invoker.redo();

        assertEquals(baseAdministrator, manager.getAdministrators().get(1));
        assertEquals(second, manager.getAdministrators().get(2));
        assertEquals(3, manager.getAdministrators().size());
    }
}
