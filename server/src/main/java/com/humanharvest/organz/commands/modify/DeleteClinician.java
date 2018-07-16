package com.humanharvest.organz.commands.modify;

import java.util.Optional;

import com.humanharvest.organz.actions.Action;
import com.humanharvest.organz.actions.ActionInvoker;
import com.humanharvest.organz.actions.clinician.DeleteClinicianAction;
import com.humanharvest.organz.Clinician;
import com.humanharvest.organz.HistoryItem;
import com.humanharvest.organz.state.ClinicianManager;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.JSONConverter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command line to modify attributes of clinicians, using their staff id as a reference key
 */
@Command(name = "deleteclinician", description = "Deletes a clinician.")
public class DeleteClinician implements Runnable {

    private ClinicianManager manager;
    private ActionInvoker invoker;

    @Option(names = {"-s", "--staffId"}, description = "Staff id", required = true)
    private int id; // staffId of the clinician

    @Option(names = "-y", description = "Confirms you would like to execute the removal")
    private boolean yes;

    public DeleteClinician() {
        manager = State.getClinicianManager();
        invoker = State.getInvoker();
    }

    public DeleteClinician(ClinicianManager manager, ActionInvoker invoker) {
        this.manager = manager;
        this.invoker = invoker;
    }

    @Override
    public void run() {
        Optional<Clinician> clinician = manager.getClinicianByStaffId(id);

        if (!clinician.isPresent()) {
            System.out.println("No clinician exists with that user ID");
        } else if (clinician.get().getStaffId() == manager.getDefaultClinician().getStaffId()) {
            System.out.println("Default clinician cannot be deleted");
        } else if (!yes) {
            System.out.println(
                    String.format("Removing clinician: %s, with staff id: %s,\nto proceed please rerun the command "
                                    + "with the -y flag",
                            clinician.get().getFullName(),
                            clinician.get().getStaffId()));
        } else {
            Action action = new DeleteClinicianAction(clinician.get(), manager);

            System.out.println(invoker.execute(action));
            System.out.println("This removal will only be permanent once the 'save' command is used");

            HistoryItem deleteClinician = new HistoryItem("DELETE", "Clinician " + id + " deleted");
            JSONConverter.updateHistory(deleteClinician, "action_history.json");
        }
    }
}
