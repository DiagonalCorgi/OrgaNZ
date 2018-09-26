package com.humanharvest.organz.actions.client.illness;

import com.humanharvest.organz.IllnessRecord;
import com.humanharvest.organz.actions.client.ClientAction;
import com.humanharvest.organz.state.ClientManager;
import com.humanharvest.organz.views.client.ModifyIllnessObject;

import org.springframework.beans.BeanUtils;

public class ModifyIllnessRecordByObjectAction extends ClientAction {

    private IllnessRecord oldRecord;
    private IllnessRecord record;
    private ModifyIllnessObject oldIllnessDetails;

    public ModifyIllnessRecordByObjectAction(IllnessRecord oldRecord, ClientManager manager,
            ModifyIllnessObject oldIllnessDetails,
            ModifyIllnessObject newIllnessDetails) {
        super(oldRecord.getClient(), manager);
        this.oldIllnessDetails = oldIllnessDetails;
        this.oldRecord = oldRecord;

        record = new IllnessRecord(newIllnessDetails.getIllnessName(),
                newIllnessDetails.getDiagnosisDate(), newIllnessDetails.getCuredDate(),
                newIllnessDetails.getIsChronic());
    }

    @Override
    protected void execute() {
        super.execute();
        BeanUtils.copyProperties(oldIllnessDetails.getUnmodifiedFields(), record);
        client.addIllnessRecord(record);
        client.deleteIllnessRecord(oldRecord);
        manager.applyChangesTo(client);
    }

    @Override
    protected void unExecute() {
        super.unExecute();
        BeanUtils.copyProperties(oldIllnessDetails.getUnmodifiedFields(), record);
        client.addIllnessRecord(oldRecord);
        client.deleteIllnessRecord(record);
        manager.applyChangesTo(client);
    }

    @Override
    public String getExecuteText() {
        return String.format("Modified record for illness '%s' for client %d: %s.",
                record.getIllnessName(), client.getUid(), client.getFullName());
    }

    @Override
    public String getUnexecuteText() {
        return String.format("Reversed the modifications of the record for illness '%s' for client %d: %s.",
                record.getIllnessName(), client.getUid(), client.getFullName());
    }
}
