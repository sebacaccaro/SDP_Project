package analyst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gateway.store.beans.AvgSdStatsBean;
import gateway.store.beans.NumberOfNodesBean;
import gateway.store.beans.StatsUnitListBean;

public class Analyst {
    private static WebTarget webTarget = ClientBuilder.newClient().target("http://localhost:1337/analyst");

    public static void main(String[] args) throws IOException {
        /* TODO: put parameters for port and ip */
        printHeader();
        boolean continuing = true;
        while (continuing) {
            continuing = printMenu();
        }
    }

    public static void printHeader() {
        System.out.println("***********************************" + "\n" + "**       NODE ANALYZER CLIENT    **" + "\n"
                + "**       SDP Project AA 19/20    **" + "\n" + "**        Sebastiano Caccaro     **" + "\n"
                + "***********************************" + "\n");
    }

    public static boolean printMenu() throws IOException {
        System.out.println("What statistics would you like to consult ?");
        System.out.println("\t1. Number of nodes in the token ring net");
        System.out.println("\t2. Last n measurments");
        System.out.println("\t3. Mean and standard deviation of the last n measurments");
        System.out.println("(Input any other value to quit the program)");
        System.out.print(">>>> ");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String choice = br.readLine();

        switch (choice) {
            case "1":
                numberOfNodesCall();
                return true;

            case "2":
                lastMeasurmentsCall();
                return true;

            case "3":
                averageAndDevationCall();
                return true;

            default:
                return false;
        }

    }

    public static void numberOfNodesCall() {
        WebTarget path = webTarget.path("/number_of_nodes");
        Invocation.Builder invocationBuilder = path.request(MediaType.APPLICATION_JSON);
        NumberOfNodesBean response = invocationBuilder.get(NumberOfNodesBean.class);
        System.out.println("The number of nodes in the system is :" + response.getNumberOfNodes());
    }

    public static void lastMeasurmentsCall() throws IOException {
        int n = getN();

        WebTarget path = webTarget.path("/last_stats").queryParam("n", n);
        Invocation.Builder invocationBuilder = path.request(MediaType.APPLICATION_JSON);

        StatsUnitListBean response = invocationBuilder.get(StatsUnitListBean.class);
        System.out.println(response);
    }

    public static void averageAndDevationCall() throws IOException {
        int n = getN();
        WebTarget path = webTarget.path("/average_deviation").queryParam("n", n);
        Invocation.Builder invocationBuilder = path.request(MediaType.APPLICATION_JSON);

        AvgSdStatsBean response = invocationBuilder.get(AvgSdStatsBean.class);
        System.out.println(response);

    }

    public static int getN() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean validNumber = false;
        int n = 0;
        while (!validNumber) {
            try {
                System.out.println("Insert the number of measurmens you want");
                System.out.print(">>> ");
                n = Integer.parseInt(br.readLine());
                if (n < 0)
                    throw new NumberFormatException();
                validNumber = true;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: You need to insert a valid integer positive number");
            }
        }
        return n;
    }
}