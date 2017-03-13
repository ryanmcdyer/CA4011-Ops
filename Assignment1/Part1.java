import java.io.Console;/*
import java.util.Arrays;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.time.Instant;*/
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.security.SecureRandom;

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

* The number of replications to be applied should be an input parameter for the simulation system.

*/

  public static void main(String[] args) {

    double arrivalRate = 0;
    double serviceRate = 0;
    double avgTimeBetweenArrivals = 60;
    int numServers = 0;
    int lengthOfSimulation = 0;
    int numSimulations = 1;

    Console c = System.console();
    if (c == null) {
      System.err.println("No console, exiting.");
      System.exit(1);
    }

    int[] arrivalTimes;

    try {
      arrivalRate = Double.parseDouble(c.readLine("Arrival Rate (x per hour): ")); //lambda
      avgTimeBetweenArrivals = 60/arrivalRate;
      serviceRate = Double.parseDouble(c.readLine("Service Rate (x per hour): ")); //mu
      numServers = Integer.parseInt(c.readLine("Number of servers: "));
      lengthOfSimulation = Integer.parseInt(c.readLine("Length of Simulation (in minutes): "));
      //numSimulations = Integer.parseInt(c.readLine("Number of Simulation: "));
      System.out.println("Traffic Intensity = " + (arrivalRate/(numServers * serviceRate))); //rho

    } catch(Exception e) {
      ;
    }

    int numClients = (int) Math.round((lengthOfSimulation/60.0) * arrivalRate);
    System.out.println(numClients);

    ArrayList<Client> q = new ArrayList<>();

    int i = 0;
    double t = 30/arrivalRate;
    double arrivalTime = t;
    SecureRandom sr = new SecureRandom();
    while(i < 100) {
      t += avgTimeBetweenArrivals;
      arrivalTime = t + (avgTimeBetweenArrivals/2 * getArrivalTimeExp(arrivalRate, sr) * ( sr.nextBoolean() ? 1 : -1 ));
      //Time

      q.add(new Client((int) arrivalTime));
      i++;
    }

    for(Client tmp : q) {
      System.out.println(tmp.getArrivalTime());
    }

  }

  static double getArrivalTimeExp(double arrivalRate, Random rand) {
    //http://stackoverflow.com/questions/2106503/pseudorandom-number-generator-exponential-distribution
    /*double d = Math.log(1-rand.nextDouble())/(-arrivalRate);
    System.out.println("d" + d);
    return d;*/
    return Math.log(1-rand.nextDouble())/(-arrivalRate);
  }

  static int getPoisson(double lambda) {
    //http://stackoverflow.com/questions/1241555/algorithm-to-generate-poisson-and-binomial-random-numbers
    double L = Math.exp(-lambda);
    double p = 1.0;
    int k = 0;

    do {
      k++;
      p *= Math.random();
    } while (p > L);

    return k - 1;
  }
}


class Client {
  private int arrivalTime;
  private int startOfServiceTime;
  private int departureTime;

  public Client(int a0) {
    arrivalTime = a0;
  }

  public Client() {
    ;
  }

	public int getArrivalTime() {
		return arrivalTime;
	}

	/*public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}*/

	public int getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(int departureTime) {
		this.departureTime = departureTime;
	}

	public int getStartOfServiceTime() {
		return startOfServiceTime;
	}

	public void setStartOfServiceTime(int startOfServiceTime) {
		this.startOfServiceTime = startOfServiceTime;
	}
}

class Server {

  private int breakLength;
  private ArrayList<Integer> breakTimes;

  public Server() {
    ;
  }
}
