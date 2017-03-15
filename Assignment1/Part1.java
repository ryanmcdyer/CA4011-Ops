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
import java.util.LinkedList;
import java.util.Arrays;
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

  static ArrayList<Client> allClients;

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


    populateClients(avgTimeBetweenArrivals, arrivalRate, lengthOfSimulation);

    /*for(Client tmp : allClients) {
      System.out.println(tmp.getArrivalTime());
    }*/


    int i = 0;
    while(i < numSimulations) {
      runSimulation(lengthOfSimulation);
      i++;
    }
  }

  static void runSimulation(int lengthOfSimulation) {

/*
- Check if there's a new arrival
  -> if so, add to queue

- Check if the queue is !empty
  -> if so, check if a server is free
    -> if so, make server busy, remove client from queue, set client departureTime

- Check if

*/


    LinkedList<Client> q = new LinkedList<>();
    int time = 0;
    Client tmp;
    while(time < lengthOfSimulation) {
      tmp = newArrival(time);
      if(tmp != null) {
        //tmp.setBusy();
      }
      time++;
    }
  }

  static Client newArrival(int time) {
    for(Client c : allClients) {
      if(c.getArrivalTime() == time) {
        return c;
      }
    }
    return null;
  }

  static int getArrivalTimePoisson(double lambda) {
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

  static double getArrivalTimeExp(double arrivalRate, Random rand) {
    //http://stackoverflow.com/questions/2106503/pseudorandom-number-generator-exponential-distribution
    /*double d = Math.log(1-rand.nextDouble())/(-arrivalRate);
    System.out.println("d" + d);
    return d;*/
    return Math.log(1-rand.nextDouble())/(-arrivalRate);
  }

  static void populateClients(double avgTimeBetweenArrivals, double arrivalRate, int lengthOfSimulation) {
    allClients = new ArrayList<>();
    int i = 0;
    int t = Math.round((float) (30.0/arrivalRate)); //set it to 5 mins
    int arrivalTime = t;
    SecureRandom sr = new SecureRandom();
    while(i < lengthOfSimulation) {
      t += avgTimeBetweenArrivals;
      arrivalTime = (int) (0.5 + t + (avgTimeBetweenArrivals/2
          * getArrivalTimeExp(arrivalRate, sr)
          * ( sr.nextBoolean() ? 1 : -1 )));

      //System.out.println(t + " " + arrivalTime);
      //Time

      q.add(new Client(arrivalTime));
      i++;
    }
  }
}

class Client {
  private int arrivalTime;
  private int startOfServiceTime;
  private int departureTime;

  private boolean isBusy;

  public Client(int a0) {
    arrivalTime = a0;
    isBusy = false;
    departureTime = -1;
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

  public boolean hasBeenServed() {
    if(departureTime != -1)
      return true;
    return false;
  }

  public boolean isBusy() {
    return isBusy;
  }
}

class Server {
  private int breakLength;
  private ArrayList<Integer> breakTimes;// = new ArrayList<>();
  private boolean isBusy;
  private int serviceTime;//how long it takes this server to serve one client

  public Server(int breakLength0, int[] breakTimes0, int serviceTime0) {
    breakLength = breakLength0;
    breakTimes = new ArrayList<>();
    for(int bt : breakTimes0) {
      breakTimes.add(bt);
    }
    serviceTime = serviceTime0;
    isBusy = false;
  }

  public Server() {
    ;
  }

  public boolean isBusy() {
    return isBusy;
  }

}
