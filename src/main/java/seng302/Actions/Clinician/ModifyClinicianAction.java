package seng302.Actions.Clinician;

import java.util.ArrayList;
import java.util.stream.Collectors;

import seng302.Actions.Action;
import seng302.Actions.ModifyObjectByFieldAction;
import seng302.Clinician;

/**
 * A reversible clinician modification Action
 */
public class ModifyClinicianAction extends Action {

    private ArrayList<ModifyObjectByFieldAction> actions = new ArrayList<>();
    private Clinician clinician;

    /**
     * Create a new Action
     * @param clinician The clinician to be modified
     */
    public ModifyClinicianAction(Clinician clinician) {
        this.clinician = clinician;
    }

    /**
     * Add a modification to the clinician
     * @param field The setter field of the clinician. Must match a valid setter in the Clinician object
     * @param oldValue The object the field initially had. Should be taken from the Clinicians equivalent getter
     * @param newValue The object the field should be update to. Must match the setters Object type
     * @throws NoSuchMethodException Thrown if the Clinician does not have the specified setter
     * @throws NoSuchFieldException Thrown if the Clinicians specified setter does not take the same type as given in
     * one of the values
     */
    public void addChange(String field, Object oldValue, Object newValue)
            throws NoSuchMethodException, NoSuchFieldException {
        actions.add(new ModifyObjectByFieldAction(clinician, field, oldValue, newValue));
    }

    @Override
    protected void execute() {
        for (ModifyObjectByFieldAction action : actions) {
            action.execute();
        }
    }

    @Override
    protected void unExecute() {
        for (ModifyObjectByFieldAction action : actions) {
            action.unExecute();
        }
    }

    @Override
    public String getExecuteText() {
        String changesText = actions.stream()
                .map(ModifyObjectByFieldAction::getExecuteText)
                .collect(Collectors.joining("\n"));

        return String.format("Updated details for clinician %d: %s %s. \n"
                        + "These changes were made: \n\n%s",
                clinician.getStaffId(), clinician.getFirstName(), clinician.getLastName(), changesText);
    }

    @Override
    public String getUnexecuteText() {
        String changesText = actions.stream()
                .map(ModifyObjectByFieldAction::getExecuteText)
                .collect(Collectors.joining("\n"));

        return String.format("Reversed update for clinician %d: %s %s. \n"
                        + "These changes were reversed: \n\n%s",
                clinician.getStaffId(), clinician.getFirstName(), clinician.getLastName(), changesText);
    }
}
