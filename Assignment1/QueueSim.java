import java.io.Console;
import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.time.Instant;

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


  static LinkedList<Double> avgWaits;
  static LinkedList<Double> avgTimesInSystem;

  static LinkedList<Integer> maxWaits;
  static LinkedList<Integer> maxTimesInSystem;

  static LinkedList<Integer> serverTimeBusy;
  static LinkedList<Integer> serverTimeIdle;

  static LinkedList<Double> avgSystemSize;
  static LinkedList<Double> avgQueueSize;

  static LinkedList<Integer> finishingTimes;

  public static void main(String[] args) {

    avgWaits = new LinkedList<>();
    avgTimesInSystem = new LinkedList<>();
    maxWaits = new LinkedList<>();
    maxTimesInSystem = new LinkedList<>();
    serverTimeBusy = new LinkedList<>();
    serverTimeIdle = new LinkedList<>();
    avgSystemSize = new LinkedList<>();
    avgQueueSize = new LinkedList<>();
    finishingTimes = new LinkedList<>();

    double arrivalRate = 0;
    double serviceRate = 0;
    double avgTimeBetweenArrivals = 60;
    boolean areArrivalsExpDisted = true;

    int serviceLength = -1;
    int specialServiceLength = -2;

    int freqOfSpecialClients = 999;
    boolean keepSpecialUntilEnd = false;
    int numServers = 0;
    int lengthOfSimulation = 0;
    int numSimulations = 1;

    int numBreaks = 0;
    boolean isThereABreak = false;

    int numClients = 1;

    HashMap<Integer, ArrayList<Integer>> breakTimes = new HashMap<>();
    int breakLength = 0;

    Console c = System.console();
    if (c == null) {
      System.err.println("No console, exiting.");
      System.exit(1);
    }


    //Read in the parameters
    try {
      lengthOfSimulation = Integer.parseInt(c.readLine("Length of Simulation (in minutes): "));
      System.out.println("This simulation will run for " + lengthOfSimulation + " minutes.");
      System.out.println("All times will be relative to the simulation beginning at time 0.");

      System.out.println("Please enter the following paramaters.");

      arrivalRate = Double.parseDouble(c.readLine("Arrival Rate (x per hour): ")); //lambda
      avgTimeBetweenArrivals = 60/arrivalRate;

      areArrivalsExpDisted = Boolean.parseBoolean(c.readLine("Should all special customers arrive last? (\"true\" or \"false\"): "));


      serviceRate = Double.parseDouble(c.readLine("Service Rate (x per hour): ")); //mu
      serviceLength = (int) Math.round(60/serviceRate);
      specialServiceLength = 2 * serviceLength;

      freqOfSpecialClients = Integer.parseInt(c.readLine("Enter n, where every nth client is a Special Client (-1 for no special clients): "));
      if(freqOfSpecialClients >= 1) {
          keepSpecialUntilEnd = Boolean.parseBoolean(c.readLine("Should all special customers arrive last? (\"true\" or \"false\"): "));
      }
      isThereABreak = Boolean.parseBoolean(c.readLine("Will the servers take a break? (\"true\" or \"false\"): "));
      if(isThereABreak) {
        numBreaks = Integer.parseInt(c.readLine("Breaks taken per server: "));
        breakLength = Integer.parseInt(c.readLine("Break length (in minutes): "));
      }
      numServers = Integer.parseInt(c.readLine("Number of servers: "));
      numSimulations = Integer.parseInt(c.readLine("Number of simulations: "));

    } catch(Exception e) {
      ;
    }

    System.out.println("Traffic Intensity: " + (arrivalRate/(numServers * serviceRate))); //rho


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
    numClients = (int) Math.round((lengthOfSimulation/60.0) * arrivalRate);//round double to long, cast to int

    int i = 0;

    PrintWriter pw;
    String nameOfFile = "" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    try {
      pw = new PrintWriter(new File(nameOfFile + ".txt"));
      pw.write("PARAMS: ");
      pw.write("\nlength of sim in minutes " + lengthOfSimulation);
      pw.write("\narrivalRate " + arrivalRate);
      pw.write("\nareArrivalsExpDisted " + areArrivalsExpDisted);
      pw.write("\nserviceRate " + serviceRate);
      pw.write("\nnumServers " + numServers);
      pw.write("\nfreqOfSpecialClients " + freqOfSpecialClients);
      if(freqOfSpecialClients > 0) {
        pw.write("\nkeepSpecialUntilEnd " + keepSpecialUntilEnd);
      }
      if(isThereABreak) {
        pw.write("\nnumBreaks per server " + numBreaks);
        pw.write("\nbreakLength " + breakLength);
      }
      pw.write("\nnumSimulations " + numSimulations);
      pw.flush();

      while(i < numSimulations) {
        populateClients(avgTimeBetweenArrivals, arrivalRate, lengthOfSimulation,
            serviceLength, specialServiceLength, freqOfSpecialClients, areArrivalsExpDisted);

        System.out.println("Clients populated");

        if(!isThereABreak) {
          populateServers(numServers);
        } else {
          populateServers(numServers, breakLength, breakTimes);
        }
        System.out.println("Servers populated");


        runSimulation(lengthOfSimulation, pw);
        i++;
      }

      writeOutput(pw);

      pw.flush();
      pw.close();
    } catch(Exception e) {
      ;
    }
  }

  static void writeOutput(PrintWriter pw) {
/*static LinkedList<Double> avgWaits;
static LinkedList<Double> avgTimesInSystem;*/
    pw.write("\nAverage waiting times (Client): \n");
    for(Double d : avgWaits) {
      pw.write(d + " ");
    }
    pw.write("\nAverage time spent in system: \n");
    for(Double d : avgTimesInSystem) {
      pw.write(d + " ");
    }

/*static LinkedList<Integer> maxWaits;
static LinkedList<Integer> maxTimesInSystem;*/
    pw.write("\nMaximum waiting times (Client): \n");
    for(Integer i : maxWaits) {
      pw.write(i + " ");
    }
    pw.write("\nMaximum time spent in system: \n");
    for(Integer i : maxTimesInSystem) {
      pw.write(i + " ");
    }

/*static LinkedList<Integer> serverTimeBusy;
static LinkedList<Integer> serverTimeIdle;*/
    pw.write("\nTime spent serving Clients (mins): \n");
    for(Integer i : serverTimeBusy) {
      pw.write(i + " ");
    }
    pw.write("\nTime spent idle (Server, mins): \n");
    for(Integer i : serverTimeIdle) {
      pw.write(i + " ");
    }

/*static LinkedList<Double> avgSystemSize;
static LinkedList<Double> avgQueueSize;*/
    pw.write("\nAverage system size (num active customers): \n");
    for(Double d : avgSystemSize) {
      pw.write(d + " ");
    }
    pw.write("\nAverage queue size: \n");
    for(Double d : avgQueueSize) {
      pw.write(d + " ");
    }

    pw.flush();

  }

  static void runSimulation(int lengthOfSimulation, PrintWriter pw) {

    LinkedList<Client> q = new LinkedList<>();
    int totalQueueSize = 0;
    int totalSystemSize = 0;
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
          s.releaseClient(time);
          //busySer = getBusyServers();
          //TODO re-pull the busySer ?
        }
      }

//- Check if any servers are on lunch
//  -> if so, check if they are due to finish
//    -> if so, set them to not on lunch
    for(Server s : allServers) {
      //TODO
      s.checkLunchtimeStatus(time);
    }

//- Check if the queue is populated
//  -> if so, check if a server is free
//    -> if so, make server busy, remove "oldest" client from queue, set client departureTime

      if(q.size() > 0) {
        busySer = getBusyServers();
        //TODO: fix this shite
        if(busySer.length > 0) {
          int i = 0;
          while(i < busySer.length && q.size() > 0) {
            tmpCli = q.remove(0);//remove "head" of queue
            tmpSer = busySer[i];
            tmpSer.giveClient(tmpCli, time);
          }
        }
      }
//- The queue is empty. Check if any servers are due their lunch
//  -> if so, send them on lunch


//->Increment counters
      for(Client c : q) {
        System.out.println(q.size());
        c.incCounter();
      }

      for(Server s : allServers) {
        s.incCounter();
      }

      totalQueueSize += q.size();
      totalSystemSize += q.size();
      totalSystemSize += getBusyServers().length;

      time++;
    }

    //Calculate averages and add to the Lists

    int tmpInt = 0;
    double tmpDouble = 0.0;
    finishingTimes.add(time);

    tmpDouble = ((double) totalQueueSize/time);
    avgQueueSize.add(tmpDouble);
    tmpDouble = ((double) totalSystemSize/time);
    avgSystemSize.add(tmpDouble);


    double tmpBusy = 0;
    double tmpWaiting = 0;
    double count = 0.0;
    for(Server s : allServers) {
      tmpBusy += s.getTimeBusy();
      serverTimeBusy.add(s.getTimeBusy());
      tmpWaiting += s.getTimeWaiting();
      serverTimeIdle.add(s.getTimeWaiting());
      count++;
    }

    count = 0.0;
    int maxWait = -1;
    int maxTimeInSystem = -1;
    int totalWait = 0;
    int totalTimeInSystem = 0;

    for(Client c : allClients) {
      tmpInt = c.getTimeInQueue();
      if(tmpInt > maxWait) {
        maxWait = tmpInt;
      }
      totalWait += tmpInt;

      tmpInt += c.getServiceLength();
      if(tmpInt > maxTimeInSystem) {
        maxTimeInSystem = tmpInt;
      }
      totalTimeInSystem += tmpInt;

      count++;
    }
    avgWaits.add(totalWait/count);
    avgTimesInSystem.add(totalTimeInSystem/count);
    maxWaits.add(maxWait);
    maxTimesInSystem.add(maxTimeInSystem);
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

  static int getPoissonDist(double lambda) {
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

  static double getExpDist(double arrivalRate, Random rand) {
    //http://stackoverflow.com/questions/2106503/pseudorandom-number-generator-exponential-distribution
    return Math.log(1-rand.nextDouble())/(-arrivalRate);
  }

  static void populateClients(double avgTimeBetweenArrivals, double arrivalRate,
      int lengthOfSimulation, int serviceLength, int specialServiceLength,
      int freqOfSpecialClients, boolean areArrivalsExpDisted) {
    allClients = new ArrayList<>();
    int i = 0;
    int t = Math.round((float) (30.0/arrivalRate));
    int arrivalTime = t;
    SecureRandom sr = new SecureRandom();
    while(i < lengthOfSimulation) {
      t += avgTimeBetweenArrivals;
      if(areArrivalsExpDisted) {
        arrivalTime = (int) (0.5 + t + (avgTimeBetweenArrivals/2
        * getExpDist(arrivalRate, sr)
        * ( sr.nextBoolean() ? 1 : -1 )));
      } else {

        //TODO: Poisson

        arrivalTime = (int) (0.5 + t + getPoissonDist(t));
        /*arrivalTime = (int) (0.5 + t + (avgTimeBetweenArrivals/2
        * getExpDist(arrivalRate, sr)
        * ( sr.nextBoolean() ? 1 : -1 )));*/

      }

      //TODO: Change serviceLength to exponential dist
      if(isSpecialClient(freqOfSpecialClients)) {
        allClients.add(new Client(arrivalTime, specialServiceLength));
      } else {
        allClients.add(new Client(arrivalTime, serviceLength));
      }
      i++;
    }
  }

  static boolean isSpecialClient(int x) {
    if(x <= 0)
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
      i++;
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

  static Server[] getServersOnLunch() {
    ArrayList<Server> list = new ArrayList<>();
    for(Server s : allServers) {
      if(s.isOnLunch()) {
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
    System.out.println("Incing count for client");
    timeInQueue++;
  }

  public int getTimeInQueue() {
    return timeInQueue;
  }

  public int getTimeInSystem() {
    return timeInQueue + serviceLength;
  }
}

class Server {
  private int breakLength;
  private ArrayList<Integer> breakTimes;// = new ArrayList<>();
  private ArrayList<Integer> actualBreakTimes;// = new ArrayList<>();
  private boolean isBusy;
  private boolean isOnLunch;
  private int serviceTime;//how long it takes this server to serve one client
  private Client currentClient;

  private int timeWaiting; //Time spent waiting on a client
  private int timeBusy; //Time spent serving a client

  public Server(int breakLength0, ArrayList<Integer> breakTimes0) {
    breakLength = breakLength0;
    breakTimes = new ArrayList<>();
    actualBreakTimes = new ArrayList<>();
    for(int bt : breakTimes0) {
      breakTimes.add(bt);
    }
    isBusy = false;
    timeBusy = 0;
    timeWaiting = 0;
  }

  public Server() {
    ;
  }

  public void giveClient(Client c, int time) {
    currentClient = c;
    currentClient.setStartOfServiceTime(time);
    currentClient.setDepartureTime(time + currentClient.getServiceLength());
    currentClient.setBusy(true);
  }

  public Client releaseClient(int time) {
    currentClient.setBusy(false);
    this.setBusy(false);
    currentClient = null;
    if(breakTimes.contains(time)){
      isOnLunch = true;
    }
    return currentClient;
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

  public boolean isOnLunch() {
    return isOnLunch;
  }

  public void checkLunchtimeStatus(int time) {
    //Check if server's lunchtime is over
    if(time > 0) {
      isBusy = false;
      isOnLunch = false;
    }
  }

  public void incCounter() {
    if(this.isBusy()) {
      timeBusy++;
    } else if(!this.isOnLunch()){
      timeWaiting++;
    }
  }

  public int getTimeWaiting() {
    return timeWaiting;
  }

  public int getTimeBusy() {
    return timeBusy;
  }
}
