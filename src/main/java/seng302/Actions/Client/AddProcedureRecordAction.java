package seng302.Actions.Client;

import seng302.Client;
import seng302.ProcedureRecord;
import seng302.State.ClientManager;

/**
 * A reversible action that will add the given procedure record to the given client's medical history.
 */
public class AddProcedureRecordAction extends ClientAction {

    private Client client;
    private ProcedureRecord record;
    private ClientManager manager;

    /**
     * Creates a new action to add an procedure record.
     * @param client The client whose medical history to add to.
     * @param record The procedure record to add.
     */
    public AddProcedureRecordAction(Client client, ProcedureRecord record, ClientManager manager) {
        this.client = client;
        this.record = record;
        this.manager = manager;
    }

    @Override
    public void execute() {
        super.execute();
        client.addProcedureRecord(record);
        manager.applyChangesTo(client);
    }

    @Override
    public void unExecute() {
        super.unExecute();
        client.deleteProcedureRecord(record);
        manager.applyChangesTo(client);
    }

    @Override
    public String getExecuteText() {
        return String.format("Added record for procedure '%s' to client %d: %s.",
                record.getSummary(), client.getUid(), client.getFullName());
    }

    @Override
    public String getUnexecuteText() {
        return String.format("Reversed the addition of record for procedure '%s' from client %d: %s.",
                record.getSummary(), client.getUid(), client.getFullName());
    }

    @Override
    protected Client getAffectedClient() {
        return client;
    }
}
