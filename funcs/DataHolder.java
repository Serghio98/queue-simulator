package funcs;
import java.util.*;
import randomNGenerator.Rngs;
import randomNGenerator.Rvgs;

//this class's only purpose is to hold the common variables between DropSim and BlockSim and also some on Sim
public abstract class DataHolder {
    // Class DropSim variables
    public int lostClients;
    public double[] arrayDropRate;
    public double dropRate;

    // Class BlockSim variables
    public Event blockedClient;
    public double timeBlocked;
    public double percBlockedTime;
    public double[] arrayBlockRate;


    public double clock;
    public double lastEventTime;
    public long totalDepartures;//on the whole system, also, same as queue 2 as it is counted on that
    public long nDeparturesQ1;//on the queue 1

    //INIT VALUE = 0
    public double totalBusy1;
    public double totalBusy2;
    public double maxQueueLength1;
    public double maxQueueLength2;
    public double sumRespTimeQ1;
    public double sumRespTimeQ2;
    public long queueLength1;
    public long queueLength2;
    public long numberInService1;
    public long numberInService2;

    public final int ARRIVAL_Q1 = 1;
    public final int DEPARTURE_Q1 = 2;
    public final int DEPARTURE_Q2 = 3;

    public EventList futureEventList;
    public Queue customersQ1;
    public Queue customersQ2;
    public Random stream;

    //input values
    public int nRun;
    public float meanServiceTime1;
    public float meanServiceTime2;
    public float meanInterArrivalTime;
    public float sigma1;
    public float sigma2;
    public int maxQueue2Dim;
    public int totalCustomers;

    //Global index point estimates
    //INIT VALUE = 0
    public double throughput;
    public double utilQ1;
    public double utilQ2;
    public double nCustomersQ1;
    public double nCustomersQ2;
    public double respTimeQ1;
    public double respTimeQ2;
    
    //Arrays interval estimates
    public double[] arrayThroughput;
    public double[] arrayUtil1;
    public double[] arrayUtil2;
    public double[] arrayNCustomers1;
    public double[] arrayNCustomers2;
    public double[] arrayRespTimeQ1;
    public double[] arrayRespTimeQ2;

    public Rngs rngs = new Rngs();
    public Rvgs rvgs = new Rvgs(rngs);
    public int tmp = 0;
}
