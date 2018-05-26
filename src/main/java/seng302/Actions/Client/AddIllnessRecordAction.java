package seng302.Actions.Client;

import seng302.Client;
import seng302.IllnessRecord;
import seng302.State.ClientManager;

/**
 * A reversible action that will add the given illness record to the given client's medical history.
 */
public class AddIllnessRecordAction extends ClientAction {

    private Client client;
    private IllnessRecord record;
    private ClientManager manager;

    /**
     * Creates a new action to add an illness record.
     * @param client The client whose medical history to add to.
     * @param record The illness record to add.
     */
    public AddIllnessRecordAction(Client client, IllnessRecord record, ClientManager manager) {
        this.client = client;
        this.record = record;
        this.manager = manager;
    }

    @Override
    public void execute() {
        super.execute();
        client.addIllnessRecord(record);
        manager.applyChangesTo(client);
    }

    @Override
    public void unExecute() {
        super.unExecute();
        client.deleteIllnessRecord(record);
        manager.applyChangesTo(client);
    }

    @Override
    public String getExecuteText() {
        return String.format("Added record for illness '%s' to the history of client %d: %s.",
                record.getIllnessName(), client.getUid(), client.getFullName());
    }

    @Override
    public String getUnexecuteText() {
        return String.format("Reversed the addition of record for illness '%s' from the history of client %d: %s.",
                record.getIllnessName(), client.getUid(), client.getFullName());
    }

    @Override
    protected Client getAffectedClient() {
        return client;
    }
}
