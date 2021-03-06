package com.humanharvest.organz.actions.client.procedure;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.humanharvest.organz.ProcedureRecord;
import com.humanharvest.organz.actions.client.ClientAction;
import com.humanharvest.organz.state.ClientManager;
import com.humanharvest.organz.utilities.enums.Organ;

/**
 * A reversible action to modify a given procedure record.
 */
public class ModifyProcedureRecordAction extends ClientAction {

    private ProcedureRecord record;
    private String oldSummary, newSummary;
    private String oldDescription, newDescription;
    private LocalDate oldDate, newDate;
    private Set<Organ> oldAffectedOrgans, newAffectedOrgans;

    /**
     * Creates a new action to modify a procedure record. Will initialise all attributes to be the same as the
     * current ones.
     *
     * @param record The procedure record to modify.
     */
    public ModifyProcedureRecordAction(ProcedureRecord record, ClientManager manager) {
        super(record.getClient(), manager);
        this.record = record;

        oldSummary = record.getSummary();
        oldDescription = record.getDescription();
        oldDate = record.getDate();
        oldAffectedOrgans = record.getAffectedOrgans();

        newSummary = oldSummary;
        newDescription = oldDescription;
        newDate = oldDate;
        newAffectedOrgans = oldAffectedOrgans;
    }

    public void changeSummary(String newSummary) {
        this.newSummary = newSummary;
    }

    public void changeDescription(String newDescription) {
        this.newDescription = newDescription;
    }

    /**
     * Make the action change the procedure's date to the one given.
     *
     * @param newDate The new procedure date.
     */
    public void changeDate(LocalDate newDate) {
        this.newDate = newDate;
    }

    public void changeAffectedOrgans(Set<Organ> newAffectedOrgans) {
        this.newAffectedOrgans = newAffectedOrgans;
    }

    /**
     * Apply all changes to the procedure record.
     *
     * @throws IllegalStateException If no changes were made.
     */
    @Override
    protected void execute() {
        if (Objects.equals(newSummary, oldSummary) &&
                Objects.equals(newDescription, oldDescription) &&
                Objects.equals(newDate, oldDate) &&
                Objects.equals(newAffectedOrgans, oldAffectedOrgans)) {
            throw new IllegalStateException("No changes were made to the ProcedureRecord.");
        }
        super.execute();
        if (!Objects.equals(newSummary, oldSummary)) {
            record.setSummary(newSummary);
        }
        if (!Objects.equals(newDescription, oldDescription)) {
            record.setDescription(newDescription);
        }
        if (!Objects.equals(newDate, oldDate)) {
            record.setDate(newDate);
        }
        if (!Objects.equals(newAffectedOrgans, oldAffectedOrgans)) {
            record.setAffectedOrgans(newAffectedOrgans);
        }
        manager.applyChangesTo(record.getClient());
    }

    @Override
    protected void unExecute() {
        super.unExecute();
        if (!Objects.equals(newSummary, oldSummary)) {
            record.setSummary(oldSummary);
        }
        if (!Objects.equals(newDescription, oldDescription)) {
            record.setDescription(oldDescription);
        }
        if (!Objects.equals(newDate, oldDate)) {
            record.setDate(oldDate);
        }
        if (!Objects.equals(newAffectedOrgans, oldAffectedOrgans)) {
            record.setAffectedOrgans(oldAffectedOrgans);
        }
        manager.applyChangesTo(record.getClient());
    }

    /**
     * Returns a string describing the changes.
     *
     * @return a string describing the changes.
     */
    private String getChangesText() {
        StringBuilder builder = new StringBuilder();

        if (!Objects.equals(newSummary, oldSummary)) {
            builder.append(String.format("%nProcedure summary changed from '%s' to '%s'", oldSummary, newSummary));
        }
        if (!Objects.equals(newDescription, oldDescription)) {
            builder.append(String.format("%nProcedure description changed from '%s' to '%s'",
                    oldDescription, newDescription));
        }
        if (!Objects.equals(newDate, oldDate)) {
            builder.append(String.format("%nProcedure date changed from %s to %s", oldDate, newDate));
        }
        if (!Objects.equals(newAffectedOrgans, oldAffectedOrgans)) {
            builder.append(String.format("%nAffected organs changed from '%s' to '%s'",
                    oldAffectedOrgans.stream().map(Organ::toString).collect(Collectors.joining(", ")),
                    newAffectedOrgans.stream().map(Organ::toString).collect(Collectors.joining(", "))));
        }

        return builder.toString();
    }

    @Override
    public String getExecuteText() {
        return String.format("Changed procedure record for '%s':%s", record.getSummary(), getChangesText());
    }

    @Override
    public String getUnexecuteText() {
        return String.format("Reversed these changes to procedure record for '%s':%s", record.getSummary(),
                getChangesText());
    }

}
