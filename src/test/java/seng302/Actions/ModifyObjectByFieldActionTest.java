package seng302.Actions;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ModifyObjectByFieldActionTest {

    private ActionInvoker invoker;

    @Before
    public void init() {
        invoker = new ActionInvoker();
    }

    @Test
    public void ModifyStringValidTest() throws Exception {
        SettableItem testItem = new SettableItem();
        testItem.setString("initVal");
        ModifyObjectByFieldAction action = new ModifyObjectByFieldAction(testItem, "setString", testItem.getString(), "newVal");
        invoker.execute(action);
        assertEquals("newVal", testItem.getString());
    }

    @Test
    public void ModifyStringValidUndoTest() throws Exception {
        SettableItem testItem = new SettableItem();
        testItem.setString("initVal");
        ModifyObjectByFieldAction action = new ModifyObjectByFieldAction(testItem, "setString", testItem.getString(), "newVal");
        invoker.execute(action);
        invoker.undo();
        assertEquals("initVal", testItem.getString());
    }

    @Test(expected = NoSuchMethodException.class)
    public void ModifyStringInvalidFieldTest() throws Exception {
        SettableItem testItem = new SettableItem();
        testItem.setString("initVal");
        ModifyObjectByFieldAction action = new ModifyObjectByFieldAction(testItem, "notafield", testItem.getString(), "newVal");
    }

    @Test(expected = NoSuchFieldException.class)
    public void ModifyStringInvalidParamTest() throws Exception {
        SettableItem testItem = new SettableItem();
        testItem.setString("initVal");
        ModifyObjectByFieldAction action = new ModifyObjectByFieldAction(testItem, "setString", testItem.getString(), 123);
    }

    @Test
    public void ModifyPrimitiveIntValidTest() throws Exception {
        SettableItem testItem = new SettableItem();
        testItem.setAnInt(1);
        ModifyObjectByFieldAction action = new ModifyObjectByFieldAction(testItem, "setAnInt", testItem.getAnInt(), 2);
        invoker.execute(action);
        assertEquals(2, testItem.getAnInt());
    }

    @Test
    public void ModifyPrimitiveIntSetIntegerValidTest() throws Exception {
        SettableItem testItem = new SettableItem();
        testItem.setAnInt(1);
        ModifyObjectByFieldAction action = new ModifyObjectByFieldAction(testItem, "setAnInt", testItem.getAnInt(), new Integer(2));
        invoker.execute(action);
        assertEquals(2, testItem.getAnInt());
    }

    @Test
    public void ModifyObjectFromPrimitiveValidTest() throws Exception {
        SettableItem testItem = new SettableItem();
        testItem.setInteger(1);
        ModifyObjectByFieldAction action = new ModifyObjectByFieldAction(testItem, "setAnInt", testItem.getInteger(), 2);
        invoker.execute(action);
        assertEquals(2, testItem.getAnInt());
    }
}