package com.humanharvest.organz.controller.administrator;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.ListViewMatchers.hasItems;
import static org.testfx.matcher.control.ListViewMatchers.hasListCell;

import com.humanharvest.organz.Administrator;
import com.humanharvest.organz.Clinician;
import com.humanharvest.organz.controller.ControllerTest;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.enums.Region;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.WindowContext;
import org.junit.Test;

public class StaffListControllerTest extends ControllerTest {

    private Administrator admin1 = new Administrator("admin1", "password");
    private Administrator admin2 = new Administrator("admin2", "password");
    private Clinician clinician1 = new Clinician("First", "Middle", "Last", "UC", Region.CANTERBURY, 50,
            "password");

    @Override
    protected Page getPage() {
        return Page.STAFF_LIST;
    }

    @Override
    protected void initState() {
        State.reset();

        // Add clinician and admins
        State.getAdministratorManager().addAdministrator(admin1);
        State.getAdministratorManager().addAdministrator(admin2);
        State.getClinicianManager().addClinician(clinician1);

        // Login as an admin and open staff list page
        State.login(admin1);
        mainController.setWindowContext(WindowContext.defaultContext());
    }

    /**
     * Check that all 5 staff members are in the list, and nothing else
     */
    @Test
    public void testAllStaffVisible() {
        String defaultClinicianId = Integer.toString(State.getClinicianManager().getDefaultClinician().getStaffId());
        String defaultAdministratorUsername = State.getAdministratorManager().getDefaultAdministrator().getUsername();

        verifyThat("#staffList", hasListCell(defaultClinicianId));
        verifyThat("#staffList", hasListCell(defaultAdministratorUsername));
        verifyThat("#staffList", hasListCell(admin1.getUsername()));
        verifyThat("#staffList", hasListCell(admin2.getUsername()));
        verifyThat("#staffList", hasListCell(Integer.toString(clinician1.getStaffId())));

        verifyThat("#staffList", hasItems(5));
    }
}