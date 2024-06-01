import java.util.Arrays;
import funcs.*;
import randomNGenerator.*;

class Sim extends Funcs {
	// Class Sim variables are in DataHolder abstract class.
	// The name of the variables might contain a 1 in the name as they have the same name
	// for this single queue simulation as for the double queue simulations
	long longService;
	public void main() {
		//INIT rngs
		rngs.selectStream(0); //seleziona una stream
        rngs.plantSeeds(0);   //imposta il seme iniziale delle stream
		input(false); //false as second parameter as we use 1 queue only

        //Initialization arrays interval estimates
		initArrays();

		//Create all the simulation runs
		for (int i = 0; i < nRun; i++) {
			tmp = i;
			futureEventList = new EventList();
			customersQ1 = new Queue();
			initialization();

			// Loop until first "TotalCustomers" have departed
			while (totalDepartures < totalCustomers) {
				Event evt = (Event) futureEventList.getMin();  // get imminent event
				futureEventList.dequeue();                    // be rid of it
				clock = evt.get_time();                       // advance simulation time
				if (evt.get_type() == ARRIVAL_Q1) processArrival(evt);
				else processDeparture(evt);
			}
			printSimResult();
		}
		printStimePuntuali();
        intervalEstimates();
	}

	void initArrays() {
		throughput =0;
		utilQ1 =0;
		nCustomersQ1 =0;
		respTimeQ1 =0;
        //Initialization arrays interval estimates
        arrayThroughput = new double[nRun];
        arrayUtil1 = new double[nRun];
        arrayNCustomers1 = new double[nRun];
        arrayRespTimeQ1 = new double[nRun];
        Arrays.fill(arrayThroughput, 0);
        Arrays.fill(arrayUtil1, 0);
        Arrays.fill(arrayNCustomers1, 0);
        Arrays.fill(arrayRespTimeQ1, 0);
    }

	void initialization()   {
		clock = 0.0;
		queueLength1 = 0;
		numberInService1 = 0;
		lastEventTime = 0.0;
		totalBusy1 = 0 ;
		maxQueueLength1 = 0;
		sumRespTimeQ1 = 0;
		totalDepartures = 0;
		longService = 0;

		// create first arrival event
		rngs.selectStream(tmp + 5);
		Event evt = new Event(ARRIVAL_Q1, rvgs.exponential(meanInterArrivalTime));
		futureEventList.enqueue(evt);
	}

	// Queue events functions

	void processArrival(Event evt) {
		// if the server is idle, fetch the event, do statistics and put into service
		if( numberInService1 == 0) scheduleDeparture();
		else totalBusy1 += numberInService1*(clock - lastEventTime);  // server is busy

		customersQ1.enqueue(evt); 
		queueLength1++;
		// adjust max queue length registered yet
		if (maxQueueLength1 < queueLength1) maxQueueLength1 = queueLength1;

		// schedule the next arrival
        rngs.selectStream(tmp + 6);
        Event next_arrival = new Event(ARRIVAL_Q1, clock + rvgs.exponential(meanInterArrivalTime));
        futureEventList.enqueue(next_arrival);
        lastEventTime = clock;
	}

	void scheduleDeparture() {
		double serviceTime;
		// get the job at the head of the queue
		rngs.selectStream(tmp + 7);
		while ((serviceTime = rvgs.normal(meanServiceTime1, sigma1)) < 0);
		Event depart = new Event(DEPARTURE_Q1, clock + serviceTime);
		futureEventList.enqueue(depart);
		numberInService1 = 1;
		queueLength1--;
	}

	void processDeparture(Event e) {
		totalBusy1 += numberInService1*(clock - lastEventTime);
		lastEventTime = clock;
		// get the customer
		Event finished = (Event) customersQ1.dequeue();
		// if there are customers in the queue then schedule the departure of the next one
		if( queueLength1 > 0 ) scheduleDeparture();
		else numberInService1 = 0;
		// measure the response time and add to the sum
		double response = (clock - finished.get_time());
		sumRespTimeQ1 += response;
		if( response > 4.0 ) longService++; // record long service
		totalDepartures++;
	}

	// Print results functions

	void printStimePuntuali(){
        System.out.println("------------------------------ Point Estimates ------------------------------");
        System.out.println("\tThroughput       " + (throughput/nRun));
        System.out.println("\tUtilization      " + (utilQ1/nRun));
        System.out.println("\tCustomers        " + (nCustomersQ1/nRun));
        System.out.println("\tResponse time    " + (respTimeQ1/nRun));
        System.out.println();
    }

	void printSimResult() {
		System.out.println( "------------------------------ RUN " + (tmp+1) + " ------------------------------");
		System.out.println( "\tThroughput                  " + totalDepartures / clock);
		System.out.println( "\tAvg customers               " + sumRespTimeQ1/clock);
		System.out.println( "\tServer utilization          " + totalBusy1/clock);
		System.out.println( "\tMax clients in queue        " + maxQueueLength1);
		System.out.println( "\tAvg response time           " + sumRespTimeQ1/totalCustomers + " min");
		System.out.println( "\tNum clients who spent more\n\tthan 4 minutes in the system: " + ((double)longService)/totalCustomers);
		System.out.println( "\tSimulation runlength        " + clock + " min");
		System.out.println( "\tDepartures from system      " + totalDepartures);
	
        //Update indexes point estimates
        throughput += totalDepartures / clock;
        utilQ1 += totalBusy1/clock;
        nCustomersQ1 += sumRespTimeQ1/clock;
        respTimeQ1 += sumRespTimeQ1/totalCustomers;

        //Update arrays interval estimates
        arrayThroughput[tmp] = totalDepartures / clock;
        arrayUtil1[tmp] = totalBusy1/clock;
        arrayNCustomers1[tmp] = sumRespTimeQ1/clock;
        arrayRespTimeQ1[tmp] = sumRespTimeQ1/totalCustomers;
	}
	
	void intervalEstimates(){
        double sQuadroT = calculateS(arrayThroughput, (throughput/nRun))/(nRun-1);
        double sQuadroU = calculateS(arrayUtil1, (utilQ1/nRun))/(nRun-1);
        double sQuadroNOC = calculateS(arrayNCustomers1, (nCustomersQ1/nRun))/(nRun-1);
        double sQuadroR = calculateS(arrayRespTimeQ1, (respTimeQ1/nRun))/(nRun-1);

        double sigmaT = sQuadroT/nRun;
        double sigmaU = sQuadroU/nRun;
        double sigmaNOC = sQuadroNOC/nRun;
        double sigmaR = sQuadroR/nRun;

        Rvms rvms = new Rvms();
        double tStudent = rvms.idfStudent(nRun-1,0.975);     //alpha=0.05, conf level=95%, t(0.025,9)

        System.out.println("------------------------------ Interval Estimates ------------------------------");
        double[] array;
        array = calculateInterval((throughput/nRun), tStudent, sigmaT);
        System.out.println("Conf. I. Throughput              " + "MAX: "+array[0]+ "\tMIN:  "+ array[1]);
        System.out.println("Relative err Throughput:         " + calculateRelativeError(array,(throughput/nRun))+" %\n"); 
        array = calculateInterval((utilQ1/nRun), tStudent, sigmaU);
        System.out.println("Conf. I. Utilization             " + "MAX: "+array[0]+ "\tMIN:  "+ array[1]);
        System.out.println("Relative err Utilization:        " + calculateRelativeError(array,(utilQ1/nRun))+" %\n");
        array = calculateInterval( (nCustomersQ1/nRun), tStudent, sigmaNOC);
        System.out.println("Conf. I. Num of customers        " + "MAX: "+array[0]+ "\tMIN:  "+ array[1]);
        System.out.println("Relative err Num of customers:   " + calculateRelativeError(array,(nCustomersQ1/nRun))+" %\n");
        array = calculateInterval((respTimeQ1/nRun), tStudent, sigmaR);
        System.out.println("Conf. I. Response time           " + "MAX: "+array[0]+ "\tMIN:  "+ array[1]);
        System.out.println("Relative err Response time:      " + calculateRelativeError(array,(respTimeQ1/nRun))+" %\n");
    }
}