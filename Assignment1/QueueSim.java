import java.io.Console;/*
import java.util.Arrays;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.time.Instant;*/
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.security.SecureRandom;

public class QueueSim {
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
  static ArrayList<Server> allServers;

  static LinkedList<Integer> avgWait;
  static LinkedList<Integer> avgTimeInSystem;

  static LinkedList<Integer> maxWait;
  static LinkedList<Integer> maxTimeInSystem;

  static LinkedList<Integer> serverTimeBusy;
  static LinkedList<Integer> serverTimeIdle;

  static LinkedList<Double> avgSystemSize;
  static LinkedList<Double> avgQueueSize;

  public static void main(String[] args) {

    double arrivalRate = 0;
    double serviceRate = 0;
    double avgTimeBetweenArrivals = 60;

    int serviceLength = -1;
    int specialServiceLength = -2;
    int freqOfSpecialClients = 999;
    int numServers = 0;
    int lengthOfSimulation = 0;
    int numSimulations = 1;

    int numBreaks = 0;
    boolean isThereABreak = false;

    HashMap<Integer, ArrayList<Integer>> breakTimes = new HashMap<>();
    int breakLength = 0;

    Console c = System.console();
    if (c == null) {
      System.err.println("No console, exiting.");
      System.exit(1);
    }

    int[] arrivalTimes;

    try {
      lengthOfSimulation = Integer.parseInt(c.readLine("Length of Simulation (in minutes): "));
      System.out.println("This simulation will run for " + lengthOfSimulation + " minutes.");
      System.out.println("All times will be relative to the simulation beginning at time 0.");

      System.out.println("Please enter the following paramaters.");

      arrivalRate = Double.parseDouble(c.readLine("Arrival Rate (x per hour): ")); //lambda
      avgTimeBetweenArrivals = 60/arrivalRate;
      serviceRate = Double.parseDouble(c.readLine("Service Rate (x per hour): ")); //mu
      serviceLength = (int) Math.round(60/serviceRate);
      specialServiceLength = 2 * serviceLength;

      freqOfSpecialClients = Integer.parseInt(c.readLine("Enter n, where every nth client is a Special Client (-1 for no special clients): "));
      isThereABreak = Boolean.parseBoolean(c.readLine("Will the servers take a break? (\"true\" or \"false\"): "));
      if(isThereABreak) {
        numBreaks = Integer.parseInt(c.readLine("Breaks taken per server: "));
        breakLength = Integer.parseInt(c.readLine("Break length (in minutes): "));
      }
      numServers = Integer.parseInt(c.readLine("Number of servers: "));
      //numSimulations = Integer.parseInt(c.readLine("Number of Simulation: "));
      //System.out.println("Traffic Intensity = " + (arrivalRate/(numServers * serviceRate))); //rho

    } catch(Exception e) {
      ;
    }

    int numClients = (int) Math.round((lengthOfSimulation/60.0) * arrivalRate);//round double to long, cast to int


    if(isThereABreak) {
      ArrayList<Integer> tmp;
      System.out.println("Eg: If your sim starts at 0900 and the server takes a break at 1030, enter \"90\"");
      for(int i = 0; i < numServers; i++) {
        tmp = new ArrayList<>();
        for(int j = 0; j < numBreaks; j++) {
          tmp.add(Integer.parseInt(c.readLine("Breaktime " + (j+1) + " for Server " + (i+1) + ": ")));
        }
        breakTimes.put(i, tmp);
      }

    }

    int i = 0;
    while(i < numSimulations) {
      populateClients(avgTimeBetweenArrivals, arrivalRate, lengthOfSimulation,
          serviceLength, specialServiceLength, freqOfSpecialClients);

      if(!isThereABreak) {
        populateServers(numServers);
      } else {
        populateServers(numServers, breakLength, breakTimes);
      }

      runSimulation(lengthOfSimulation);
      i++;
    }
  }

  static void runSimulation(int lengthOfSimulation) {

    String nameOfFile = String.parseString(System.currentTimeMillis());
    PrintWriter pw = new PrintWriter(new File(nameOfFile + ".csv"));

    LinkedList<Client> q = new LinkedList<>();
    int time = 0;
    Client tmpCli;
    Server tmpSer;
    Server[] busySer = new Server[0];

    while(time < lengthOfSimulation || busySer.length > 0) {

//- Check if there's a new arrival
//  -> if so, add to queue
      if(isNewArrival(time)) {
        tmpCli = getNewArrival(time);
        q.addLast(tmpCli);
      }

//- Check if a server is busy
//  -> if so, check if time == client's departureTime
//    -> if so, free up the server, set client to served
      busySer = getBusyServers();
      for(Server s : busySer) {
        tmpCli = s.getCurrentClient();
        if(tmpCli.getDepartureTime() == time) {
          s.releaseClient();
          //busySer = getBusyServers();
        }
      }

//- Check if the queue is populated
//  -> if so, check if a server is free
//    -> if so, make server busy, remove "oldest" client from queue, set client departureTime

      if(q.size() > 0) {
        busySer = getBusyServers();
        if(busySer.length > 0) {
          int i = 0;
          while(i < busySer.length && q.size() > 0) {
            tmpCli = q.remove(0);//remove "head" of queue
            tmpSer = busySer[i];
            tmpSer.giveClient(tmpCli, time);
          }
        }
      }

//->Increment counters
      for(Client c : q) {
        c.incCounter();
      }

      for(Server s : allServers) {
        s.incCounter();
      }


      time++;
    }
    System.out.println("Simulation ended " + (time-lengthOfSimulation) + " minutes over due at " + time);

    //Calculate averages and add to the Lists
  }

  static boolean isNewArrival(int time) {
    for(Client c : allClients) {
      if(c.getArrivalTime() == time) {
        return true;
      }
    }
    return false;
  }

  static Client getNewArrival(int time) {
    //Guaranteed to return a Client,
    //Only called when there is a Client with arrivalTime == time
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
    return Math.log(1-rand.nextDouble())/(-arrivalRate);
  }

  static void populateClients(double avgTimeBetweenArrivals, double arrivalRate,
      int lengthOfSimulation, int serviceLength, int specialServiceLength,
      int freqOfSpecialClients) {
    allClients = new ArrayList<>();
    int i = 0;
    int t = Math.round((float) (30.0/arrivalRate));
    int arrivalTime = t;
    SecureRandom sr = new SecureRandom();
    while(i < lengthOfSimulation) {
      t += avgTimeBetweenArrivals;
      arrivalTime = (int) (0.5 + t + (avgTimeBetweenArrivals/2
          * getArrivalTimeExp(arrivalRate, sr)
          * ( sr.nextBoolean() ? 1 : -1 )));

      if(isSpecialClient(freqOfSpecialClients)) {
        allClients.add(new Client(arrivalTime, specialServiceLength));
      } else {
        allClients.add(new Client(arrivalTime, serviceLength));
      }
      i++;
    }
  }

  static boolean isSpecialClient(int x) {
    if(x < 0)
      return false;
    //Roll a die of x sides and return true if result = x
    return ((int) ((Math.random() * x) + 1)) == x;
  }

  static void populateServers(int numServers, int breakLength, HashMap<Integer, ArrayList<Integer>> breakTimes) {
    allServers = new ArrayList<>();
    int i = 0;
    while(i < numServers) {
      allServers.add(new Server(breakLength, breakTimes.get(i)));
      i++;
    }
  }

  static void populateServers(int numServers) {
    allServers = new ArrayList<>();
    int i = 0;

    while(i < numServers) {
      allServers.add(new Server());
    }
  }

  static Server[] getBusyServers() {
    ArrayList<Server> list = new ArrayList<>();
    for(Server s : allServers) {
      if(s.isBusy()) {
        list.add(s);
      }
    }
    Server[] arr = list.stream().toArray(Server[]::new); //Java 8 streams
    return arr;
  }
}

class Client {
  private int arrivalTime;
  private int startOfServiceTime;
  private int departureTime;
  private int timeInQueue;

  private int serviceLength;

  private boolean isBusy;

  public Client(int a0, int s0) {
    arrivalTime = a0;
    serviceLength = s0;
    isBusy = false;
    departureTime = -1;
    timeInQueue = 0;
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

	public int getServiceLength() {
		return serviceLength;
	}

	public void setServiceLength(int serviceLength) {
		this.serviceLength = serviceLength;
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

  public void setBusy(boolean b) {
    isBusy = b;
  }

  public void incCounter() {
    timeInQueue++;
  }
}

class Server {
  private int breakLength;
  private ArrayList<Integer> breakTimes;// = new ArrayList<>();
  private boolean isBusy;
  private int serviceTime;//how long it takes this server to serve one client
  private Client currentClient;

  private int timeWasted; //Time spent waiting on a client
  private int timeBusy; //Time spent serving a client

  public Server(int breakLength0, ArrayList<Integer> breakTimes0) {
    breakLength = breakLength0;
    breakTimes   = new ArrayList<>();
    for(int bt : breakTimes0) {
      breakTimes.add(bt);
    }
    isBusy = false;
    timeBusy = 0;
    timeWasted = 0;
  }

  public Server() {
    ;
  }

  public void giveClient(Client c, int time) {
    currentClient = c;
    currentClient.setDepartureTime(time + currentClient.getServiceLength());
    currentClient.setStartOfServiceTime(time);
    currentClient.setBusy(true);
  }

  public void releaseClient() {
    currentClient.setBusy(false);
    this.setBusy(false);
    //if(breakTimes.contains(currenttime))
  }

  public Client getCurrentClient() {
    return currentClient;
  }

  public boolean isBusy() {
    return isBusy;
  }

  public void setBusy(boolean b) {
    isBusy = b;
  }


  public void incCounter() {
    if(this.isBusy()) {
      timeBusy++;
    } else {
      timeWasted++;
    }
  }
}
