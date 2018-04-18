package seng302.Commands.View;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;

import seng302.Client;
import seng302.State.ClientManager;

import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

public class PrintAllInfoTest {

    private ClientManager spyClientManager;
    private PrintAllInfo spyPrintAllInfo;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void init() {
        spyClientManager = spy(new ClientManager());

        spyPrintAllInfo = spy(new PrintAllInfo(spyClientManager));
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    public void printallinfo_no_clients() {
        ArrayList<Client> people = new ArrayList<>();

        when(spyClientManager.getPeople()).thenReturn(people);
        String[] inputs = {};

        CommandLine.run(spyPrintAllInfo, System.out, inputs);

        assertThat(outContent.toString(), containsString("No people exist"));
    }

    @Test
    public void printallinfo_single_client() {
        Client client = new Client("First", "mid", "Last", LocalDate.of(1970, 1, 1), 1);

        ArrayList<Client> people = new ArrayList<>();
        people.add(client);

        when(spyClientManager.getPeople()).thenReturn(people);
        String[] inputs = {};

        CommandLine.run(spyPrintAllInfo, System.out, inputs);

        assertThat(outContent.toString(),
                containsString("User: 1. Name: First mid Last, date of birth: 1970-01-01, date of death: null"));
    }

    @Test
    public void printallinfo_multiple_clients() {
        Client client = new Client("First", "mid", "Last", LocalDate.of(1970, 1, 1), 1);
        Client client2 = new Client("FirstTwo", null, "LastTwo", LocalDate.of(1971, 2, 2), 2);

        ArrayList<Client> people = new ArrayList<>();
        people.add(client);
        people.add(client2);

        when(spyClientManager.getPeople()).thenReturn(people);
        String[] inputs = {};

        CommandLine.run(spyPrintAllInfo, System.out, inputs);

        assertThat(outContent.toString(),
                containsString("User: 1. Name: First mid Last, date of birth: 1970-01-01, date of death: null"));
        assertThat(outContent.toString(),
                containsString("User: 2. Name: FirstTwo LastTwo, date of birth: 1971-02-02, date of death: null"));
    }
}
