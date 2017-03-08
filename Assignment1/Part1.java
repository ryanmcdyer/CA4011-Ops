import java.io.Console;/*
import java.util.Arrays;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;*/

public class Part1 {
  /*
In queueing systems, there are various measures of performance and these are essential
results from your software model. Key measures of performance include
  Average time a customer spends in the system (queueing & being served)
  Average time a customer spends waiting for service (that is, in the queue)
  Maximum time a customer spends in the system (queueing & being served)
  Maximum time a customer spends waiting for service (that is, in the queue)
  Proportion of time each server is idle
  Average number of customers in the system (queueing & being served)
  Average number of customers in the queue

In stochastic simulations (i.e. those involving some random elements) it is essential to
provide for replications of experiments. This is necessary in order to provide a measure of the
confidence that may be placed on the results obtained. Typically, there might be 100 or more
replications and from these averages and standard deviations can be calculated as explained
in lecture notes (see section 1.2 of the notes on simulation, for example). The number of
replications to be applied should be an input parameter for the simulation system.
*/

  public static void main(String[] args) {

    double arrivalRate, serviceRate;
    int numServers;

    Console c = System.console();
    if (c == null) {
      System.err.println("No console, exiting.");
      System.exit(1);
    }

    try {
      arrivalRate = Double.parseDouble(c.readLine("Arrival Rate (x per hour): ")); //lambda
      serviceRate = Double.parseDouble(c.readLine("Service Rate (x per hour): ")); //mu
      numServers = Integer.parseInt(c.readLine("Number of servers: "));
      System.out.println("Traffic Instensity = " + (arrivalRate/(numServers * serviceRate))); //rho

    } catch(Exception e) {
      ;
    }


  }
}
