package com.humanharvest.organz.controller.client;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import javafx.scene.input.KeyCode;

import com.humanharvest.organz.utilities.enums.Country;
import com.humanharvest.organz.utilities.enums.Organ;
import com.humanharvest.organz.utilities.enums.Region;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing that date and time of death are editable when client has cancelled the overrides on all overridden organs
 */
public class ViewClientControllerClinician3Test extends ViewClientControllerClinicianBaseTest {

    @Override
    @Before
    public void setClientDetails() {
        testClient.setFirstName("a");
        testClient.setLastName("b");
        testClient.setDateOfBirth(dateOfBirth);
        testClient.donateOrgan(Organ.LIVER);
        testClient.markDead(dateOfDeath, timeOfDeath, Country.NZ, Region.CANTERBURY.toString(), "ChCh");
        testClient.getDonatedOrgans().get(0).manuallyOverride("dropped it");
        testClient.getDonatedOrgans().get(0).cancelManualOverride();
    }

    @Test
    @Ignore("Fails in headless mode - test passes normally")
    public void dateOfDeathIsEditableAgainTest() {
        clickOn("#deathDatePicker");
        doubleClickOn("#deathDatePicker").type(KeyCode.BACK_SPACE).write("10/10/" + recentYear);
        clickOn("#applyButton");
        assertEquals(LocalDate.of(recentYear, 10, 10), testClient.getDateOfDeath());
    }

    @Test
    @Ignore("Fails in headless mode - test passes normally")
    public void timeOfDeathIsEditableAgainTest() {
        clickOn("#deathTimeField");
        doubleClickOn("#deathTimeField").type(KeyCode.BACK_SPACE).write(adjustedTimeOfDeathString);
        clickOn("#applyButton");
        assertEquals(adjustedTimeOfDeath, testClient.getTimeOfDeath());
    }

}
