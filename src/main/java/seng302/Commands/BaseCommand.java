package seng302.Commands;

import seng302.Commands.Modify.CreateDonor;
import seng302.Commands.Modify.DeleteDonor;
import seng302.Commands.Modify.Load;
import seng302.Commands.Modify.Redo;
import seng302.Commands.Modify.Save;
import seng302.Commands.Modify.SetAttribute;
import seng302.Commands.Modify.SetOrganStatus;
import seng302.Commands.Modify.Undo;
import seng302.Commands.View.GetChanges;
import seng302.Commands.View.PrintAllInfo;
import seng302.Commands.View.PrintAllOrgan;
import seng302.Commands.View.PrintDonorInfo;
import seng302.Commands.View.PrintDonorOrgan;

import picocli.CommandLine.Command;

/**
 * The main command hub used to access the other commands within the program such as save, help, createuser etc.
 * @author Dylan Carlyle, Jack Steel
 * @version sprint 1.
 * date 05/03/2018
 */

@Command(name = "DonorCLI", description = "DonorCLI is a command based management tool for the team-21 donor registration system.",
        subcommands = {
                CreateDonor.class,
                SetAttribute.class,
                SetOrganStatus.class,
                DeleteDonor.class,
                PrintAllInfo.class,
                PrintAllOrgan.class,
                PrintDonorInfo.class,
                PrintDonorOrgan.class,
                GetChanges.class,
                Save.class,
                Load.class,
                Help.class,
                Undo.class,
                Redo.class
        })

public class BaseCommand implements Runnable {


    public void run() {
        System.out.println("Invalid command");
    }
}
