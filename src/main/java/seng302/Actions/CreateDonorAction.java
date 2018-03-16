package seng302.Actions;

import seng302.Donor;
import seng302.DonorManager;

public class CreateDonorAction implements Action {


    private Donor donor;
    private DonorManager manager;


    public CreateDonorAction(Donor donor, DonorManager manager) {

        this.donor = donor;

        this.manager = manager;
    }


    @Override
    public void execute() {
        manager.addDonor(donor);
    }

    @Override
    public void unExecute() {
        manager.removeDonor(donor);
    }
}
