package seng302.Controller;

import static org.junit.Assert.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import seng302.Clinician;
import seng302.State.State;
import seng302.Utilities.Enums.Region;
import seng302.Utilities.View.Page;


import org.junit.Test;

import seng302.Utilities.View.WindowContext;


public class MenuBarControllerClinicianTest extends  ControllerTest {

    private Clinician testClinician = new Clinician("Mr", null, "Tester",
            "9 Fake St", Region.AUCKLAND, 3, "k");

    @Override
    protected Page getPage() { return Page.MENU_BAR;  }

    @Override
    protected void initState() {
        State.reset(false);
        State.getClinicianManager().addClinician(testClinician);
        State.login(testClinician);
        mainController.setWindowContext(WindowContext.defaultContext());
    }

    @Test
    public void testLogOut() {
        clickOn("#filePrimaryItem");
        clickOn("#logOutItem");
    }

    @Test
    public void testClickOnUndo() {
        clickOn("#editItem");
        clickOn("#undoItem");
    }

    @Test
    public void testClickOnRedo() {
        clickOn("#editItem");
        clickOn("#redoItem");
    }

    // Test clicking on buttons to go to another screen

    @Test
    public void testClickOnSearchClients() {
        clickOn("#clientPrimaryItem");
        clickOn("#searchClientItem");
        assertEquals(Page.SEARCH, mainController.getCurrentPage());
    }

    //Test creating a client
    @Test
    public void testCreateClient() {
        clickOn("#clientPrimaryItem");
        clickOn("#createClientItem");
        assertEquals(Page.CREATE_CLIENT, mainController.getCurrentPage());
    }


    @Test
    public void testClickOnHistory() {
        clickOn("#profilePrimaryItem");
        clickOn("#historyItem");
        assertEquals(Page.HISTORY, mainController.getCurrentPage());
    }

    // Test buttons are hidden that should only be visible when LOGGED IN as a client (ie not just viewing a client)

    @Test
    public void testLogout() {
        clickOn("#filePrimaryItem");
        clickOn("#logOutItem");
        assertEquals(Page.LANDING, mainController.getCurrentPage());
}

    @Test
    public void testClinicianDetails() {
        clickOn("#profilePrimaryItem");
        clickOn("#viewClinicianItem");
        clickOn("#profilePrimaryItem");
        verifyThat("#viewClinicianItem", isVisible());
        assertEquals(Page.VIEW_CLINICIAN, mainController.getCurrentPage());
    }


    @Test
    public void testTransplantRequest() {
        clickOn("#transplantsPrimaryItem");
        verifyThat("#searchTransplantsItem", isVisible());
    }
}