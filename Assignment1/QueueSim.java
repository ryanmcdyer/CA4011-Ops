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
    boolean shouldUsePoisson = false;

    int serviceLength = -1;
    int specialServiceLength = -2;

    int freqOfSpecialClients = 999;
    boolean keepSpecialUntilEnd = false;
    int numServers = 0;
    int lengthOfSimulation = 0;
    int numSimulations = 1;

    int numBreaks = 0;
    boolean isThereABreak = false;

    int numClients = 0;

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

      System.out.println("Please enter the following parameters.");

      arrivalRate = Double.parseDouble(c.readLine("Arrival Rate (x per hour): ")); //lambda
      avgTimeBetweenArrivals = 60/arrivalRate;

      shouldUsePoisson = Boolean.parseBoolean(c.readLine("Should arrivals be Poisson or Exponentially distributed? (\"true\" for Poisson dist, or \"false\" for Exponential dist): "));


      serviceRate = Double.parseDouble(c.readLine("Service Rate (x per hour): ")); //mu
      serviceLength = (int) Math.round(60/serviceRate);
      System.out.println("Therefore average service takes " + serviceLength + " mins");
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
    numClients = (int) Math.round((lengthOfSimulation/60.0) * arrivalRate)-1;//round double to long, cast to int
    //System.out.println("There will be " + numClients + " clients");

    int i = 0;

    PrintWriter pw;
    String nameOfFile = "" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    try {
      pw = new PrintWriter(new File(nameOfFile + ".txt"));
      pw.write("PARAMS: ");
      pw.write("\nLength of simulation (mins): " + lengthOfSimulation);
      pw.write("\nArrival rate (lambda): " + arrivalRate);
      pw.write("\nService rate (mu): " + serviceRate);
      pw.write("\nNumber of servers: " + numServers);
      pw.write("\nTraffic Intensity (assuming no special clients): " + (arrivalRate/((double) numServers * serviceRate))); //rho
      if(shouldUsePoisson) {
        pw.write("\nVars are Poisson Distributed.");
      } else {
        pw.write("\nVars are Exponentially Distributed.");
      }

      if(freqOfSpecialClients > 0) {
        pw.write("\nFrequency of special clients " + freqOfSpecialClients);
        if(keepSpecialUntilEnd) {
          pw.write("\nSpecial Clients will arrive last.");
        } else {
          pw.write("\nSpecial Clients will arrive regularly.");
        }
      } else {
        pw.write("\nThere are no special clients.");
      }

      if(isThereABreak) {
        pw.write("\nBreaks per server " + numBreaks);
        pw.write("\nLength of breaks " + breakLength);
      }
      pw.write("\nNumber of Simulations " + numSimulations);

      pw.flush();

      while(i < numSimulations) {
        populateClients(avgTimeBetweenArrivals, arrivalRate, numClients,
            serviceLength, specialServiceLength, freqOfSpecialClients,
            shouldUsePoisson, lengthOfSimulation);

//        System.out.println("Clients populated");

        if(!isThereABreak) {
          populateServers(numServers);
        } else {
          populateServers(numServers, breakLength, breakTimes);
        }
//        System.out.println("Servers populated");


        runSimulation(lengthOfSimulation, pw);
        i++;
      }

      if(isThereABreak) {
        i = 0;
        ArrayList<Integer> scheduledBreakTimes;
        ArrayList<Integer> actualBreakTimes;
        for(i = 0; i < allServers.size(); i++) {
          scheduledBreakTimes = breakTimes.get(i);
          actualBreakTimes = allServers.get(i).getActualBreakTimes();
          for(int j = 0; j < numBreaks; j++) {
            pw.write("\nServer " + i + " was scheduled to break at " + scheduledBreakTimes.get(j) + " but broke at " + actualBreakTimes.get(j));

          }
        }
      }

      pw.write("\n========================================\n");

      writeOutput(lengthOfSimulation, pw);


      pw.flush();
      pw.close();
    } catch(Exception e) {
      ;
    }
  }

  static void writeOutput(int lengthOfSimulation, PrintWriter pw) {
/*static LinkedList<Double> avgWaits;
static LinkedList<Double> avgTimesInSystem;*/

    double maxD = -1.0;
    int maxI = -1;

    for(Client c : allClients) {
      System.out.println(c.getArrivalTime());
      System.out.println(c.getDepartureTime());
    }

    pw.write("\nAverage waiting times (Client): \n");
    for(Double d : avgWaits) {
      pw.write(d + " ");
      if(d > maxD)
        maxD = d;
    }
    pw.write("\nLongest average waiting time client: " + maxD + "\n");
    maxD = -1;
    pw.write("\nAverage time spent in system: \n");
    for(Double d : avgTimesInSystem) {
      pw.write(d + " ");
      if(d > maxD)
        maxD = d;
    }
    pw.write("\nLongest average time spent in system: " + maxD + "\n");
    maxD = -1;

/*static LinkedList<Integer> maxWaits;
static LinkedList<Integer> maxTimesInSystem;*/
    pw.write("\nMaximum waiting times (Client): \n");
    for(Integer i : maxWaits) {
      pw.write(i + " ");
      if(i > maxI)
        maxI = i;
    }
    pw.write("\nOverall largest waiting time (Client): " + maxI + "\n");
    maxI = -1;
    pw.write("\nMaximum time spent in system: \n");
    for(Integer i : maxTimesInSystem) {
      pw.write(i + " ");
      if(i > maxI)
        maxI = i;
    }
    pw.write("\nOverall largest time spent in system (Client): " + maxI + "\n");
    maxI = -1;

/*static LinkedList<Integer> serverTimeBusy;
static LinkedList<Integer> serverTimeIdle;*/
    pw.write("\nTime spent serving Clients (mins): \n");
    for(Integer i : serverTimeBusy) {
      pw.write(i + " ");
      if(i > maxI)
        maxI = i;
    }
    pw.write("\nMost time spent serving Clients: " + maxI + "\n");
    maxI = -1;
    pw.write("\nTime spent idle (Server, mins): \n");
    for(Integer i : serverTimeIdle) {
      pw.write(i + " ");
      if(i > maxI)
        maxI = i;
    }
    pw.write("\nLongest time spent idle (Server): " + maxI + "\n");
    maxI = -1;

/*static LinkedList<Double> avgSystemSize;
static LinkedList<Double> avgQueueSize;*/
    pw.write("\nAverage system size (num active customers): \n");
    for(Double d : avgSystemSize) {
      pw.write(d + " ");
      if(d > maxD)
        maxD = d;
    }
    pw.write("\nLargest average system size: " + maxD + "\n");
    maxD = -1;
    pw.write("\nAverage queue size: \n");
    for(Double d : avgQueueSize) {
      pw.write(d + " ");
      if(d > maxD)
        maxD = d;
    }
    pw.write("\nLargest average queue size: " + maxD + "\n");
    maxD = -1;


    pw.write("\nFinishing times (Last Client arrival is at or before " + lengthOfSimulation + " mins): \n");
    for(Integer i : finishingTimes) {
      pw.write(i + " ");
      if(i > maxI)
        maxI = i;
    }
    pw.write("\nLatest finishing time: " + maxI + "\n");
    maxI = -1;

    pw.flush();

  }

  static void runSimulation(int lengthOfSimulation, PrintWriter pw) {

      //doublecheck : //TODO: Ensure Server is removed from available servers when it finishes with a client if it's due a break

    LinkedList<Client> q = new LinkedList<>();
    int totalQueueSize = 0;
    int maxQueueSize = -1;
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

      totalQueueSize += q.size();

      if(q.size() > maxQueueSize)
        maxQueueSize = q.size();

      busySer = getBusyServers();
      totalSystemSize += q.size();
      totalSystemSize += busySer.length;

//- Check if a server is busy
//  -> if so, check if time == client's departureTime
//    -> if so, free up the server, set client to served
      for(Server s : busySer) {
        tmpCli = s.getCurrentClient();
        if(tmpCli.getDepartureTime() == time) {
          s.releaseClient(time);
          //TODO re-pull the busySer ? dont think so
        }
      }

//- Check if any servers are on break
//  -> if so, check if they are due to finish
//    -> if so, set them to not on break
    for(Server s : allServers) {
      s.checkBreaktimeStatus(time);
    }

//- Check if the queue is populated
//  -> if so, check if a server is free
//    -> if so, make server busy, remove "oldest" client from queue, set client departureTime
    for(Server s : allServers) {
      if(!s.isBusy() && q.size() > 0 && !s.isOnBreak()) { //&&
        tmpCli = q.remove(0);//remove "head" of queue
        s.giveClient(tmpCli, time);
      }
      if(q.size() == 0) {
        break;
      }
    }

//  Maybe, not sure about below comment
//- The queue is empty. Check if any servers are due their break
//  -> if so, send them on break


//->Increment counters
      for(Client c : q) {
        c.incCounter();
      }

      for(Server s : allServers) {
        s.incCounter();
      }

//TODO: change this so it gets cli.queuetime and cli.servicetime at end
//Or not
      totalSystemSize += getBusyServers().length;

      time++;
      busySer = getBusyServers();
    }
//    System.out.println("maxQueueSize" + maxQueueSize);

    //Calculate averages and add to the Lists

    int tmpInt = 0;
    double tmpDouble = 0.0;
    finishingTimes.add(time);
//    System.out.println("finishingTime" + time);


    tmpDouble = ((double) totalQueueSize/time);
//    System.out.println("avgQueueSize" + tmpDouble);
    avgQueueSize.add(tmpDouble);
    tmpDouble = ((double) totalSystemSize/time);
    avgSystemSize.add(tmpDouble);
//    System.out.println("avgSystemSize" + tmpDouble);


    double tmpBusy = 0.0;
    double tmpWaiting = 0.0;
    double count = 0.0;
    for(Server s : allServers) {
      tmpBusy += s.getTimeBusy();
      serverTimeBusy.add(s.getTimeBusy());
//      System.out.println("server busy" + tmpBusy);
      tmpWaiting += s.getTimeWaiting();
      serverTimeIdle.add(s.getTimeWaiting());
//      System.out.println("server idle" + tmpWaiting);
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
      int numClients, int serviceLength, int specialServiceLength,
      int freqOfSpecialClients, boolean shouldUsePoisson, int lengthOfSimulation) {

    allClients = new ArrayList<>();
    int i = 0;
    int t = Math.round((float) (30.0/arrivalRate));
    int tmpServiceLength = 0;
    int arrivalTime = t;
    SecureRandom sr = new SecureRandom();

    if(!shouldUsePoisson) {
      while(i < numClients) {
        t += avgTimeBetweenArrivals;
          arrivalTime = (int) (0.5 + t + (avgTimeBetweenArrivals/2
          * getExpDist(arrivalRate, sr)
          * ( sr.nextBoolean() ? 1 : -1 )));

        if(isSpecialClient(freqOfSpecialClients)) {
          tmpServiceLength = (int) (0.5 + specialServiceLength + (specialServiceLength/2
              * getExpDist(specialServiceLength, sr)
              * ( sr.nextBoolean() ? 1 : -1 )));
          allClients.add(new Client(arrivalTime, specialServiceLength));
        } else {
          tmpServiceLength = (int) (0.5 + serviceLength + (serviceLength/2
              * getExpDist(serviceLength, sr)
              * ( sr.nextBoolean() ? 1 : -1 )));
          allClients.add(new Client(arrivalTime, tmpServiceLength));
        }
        i++;
      }
    } else {//Poisson distributed

      i += avgTimeBetweenArrivals/2 + (int) (0.5 + (avgTimeBetweenArrivals
          * getExpDist(arrivalRate, sr)
          * ( sr.nextBoolean() ? 1 : -1 )));

      while(i <= lengthOfSimulation) {

        tmpServiceLength = (int) (0.5 + serviceLength + (serviceLength/2
            * getExpDist(serviceLength, sr)
            * ( sr.nextBoolean() ? 1 : -1 )));

        allClients.add(new Client(i, tmpServiceLength));

        i += avgTimeBetweenArrivals + (int) (0.5 + (avgTimeBetweenArrivals
            * getExpDist(arrivalRate, sr)
            * ( sr.nextBoolean() ? 1 : -1 )));
      }
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

  static Server[] getServersOnBreak() {
    ArrayList<Server> list = new ArrayList<>();
    for(Server s : allServers) {
      if(s.isOnBreak()) {
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
  private boolean isOnBreak;
  private int breaksTaken;
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
    breaksTaken = 0;
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
    this.setBusy(true);
  }

  public Client releaseClient(int time) {
    currentClient.setBusy(false);
    this.setBusy(false);
    checkBreaktimeStatus(time);
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

  public void setIsOnBreak(boolean b) {
    isOnBreak = b;
  }

  public boolean isOnBreak() {
    return isOnBreak;
  }

  public ArrayList<Integer> getActualBreakTimes() {
    return actualBreakTimes;
  }

  public void checkBreaktimeStatus(int time) {

    if(breaksTaken >= breakTimes.size())//If no more breaks left
      return;


    if(isOnBreak) {//Check if break is over
      if(time > (actualBreakTimes.get(breaksTaken) + breakLength)) {
        breaksTaken++;
        isOnBreak = false;
        isBusy = false;
      }
    } else {
      if(time > breakTimes.get(breaksTaken)) {
        actualBreakTimes.add(time);
        isOnBreak = true;
      }
    }

  }

  public void incCounter() {
    if(!this.isOnBreak()) {
      if(this.isBusy()) {
        timeBusy++;
      } else {
        timeWaiting++;
      }
    }
  }

  public int getTimeWaiting() {
    return timeWaiting;
  }

  public int getTimeBusy() {
    return timeBusy;
  }
}
