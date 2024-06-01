package funcs;
import java.util.Arrays;
import java.util.Scanner;
import randomNGenerator.Rvms;

public class Funcs extends DataHolder {

    // Class constructor
    public void main() {
    }
    
    // User input function
    public void input(boolean doubleQueue){
        Scanner sc = new Scanner(System.in);
        System.out.println("Number of runs:");
        nRun = sc.nextInt();
        System.out.println("Interarrival time:");
        meanInterArrivalTime = sc.nextFloat();
        System.out.println("Average service time queue 1:");
        meanServiceTime1 = sc.nextFloat();
        System.out.println("Variance service time queue 1:");
        sigma1 = sc.nextFloat();
        if (doubleQueue) {
            System.out.println("Average service time queue 2:");
            meanServiceTime2 = sc.nextFloat();
            System.out.println("Variance service time queue 2:");
            sigma2 = sc.nextFloat();
            System.out.println("Max dimension queue 2:");
            maxQueue2Dim = sc.nextInt();
        }
        System.out.println("Total clients in the system:");
        totalCustomers = sc.nextInt();
        sc.close();
    }

    // Initialization functions

    public void initCommonArrays() {
        //Initialization arrays interval estimates
        arrayThroughput = new double[nRun];
        arrayUtil1 = new double[nRun];
        arrayUtil2 = new double[nRun];
        arrayNCustomers1 = new double[nRun];
        arrayNCustomers2 = new double[nRun];
        arrayRespTimeQ1 = new double[nRun];
        arrayRespTimeQ2 = new double[nRun];
        Arrays.fill(arrayThroughput, 0);
        Arrays.fill(arrayUtil1, 0);
        Arrays.fill(arrayUtil2, 0);
        Arrays.fill(arrayNCustomers1, 0);
        Arrays.fill(arrayNCustomers2, 0);
        Arrays.fill(arrayRespTimeQ1, 0);
        Arrays.fill(arrayRespTimeQ2, 0);
    }

    public void initCommon() {
        clock = 0.0;
        nDeparturesQ1 = 0;
        lastEventTime = 0.0;
        totalDepartures = 0;

        //reset to 0 at every run
        queueLength1 = 0;
        queueLength2 = 0;
        numberInService1 = 0;
        numberInService2 = 0;
        totalBusy1= 0;
        totalBusy2= 0;
        maxQueueLength1= 0;
        maxQueueLength2= 0;
        sumRespTimeQ1= 0;
        sumRespTimeQ2= 0;

        // create first arrival event
        rngs.selectStream(tmp + 3);
        Event evt = new Event(ARRIVAL_Q1, rvgs.exponential(meanInterArrivalTime));
        futureEventList.enqueue(evt);
    }

    // Support functions

    public double calculateS(double[] array, double media){
        double sum = 0;
        for (int i=0; i<nRun; i++) {
            sum += Math.pow((array[i]-media),2);
        }
        return sum;
    }

    public double calculateRelativeError(double[] array, double media) {
        return Math.round(((((array[0]-array[1])/2)/media) * 100) * 100.0) / 100.0;
    }

    public double[] calculateInterval(double mean, double tStudent, double sigma){
        double[] array = new double[2];
        array[0] = mean + (tStudent * Math.sqrt(sigma));
        array[1] = mean - (tStudent * Math.sqrt(sigma));
        return array;
    }

    // Queue events functions

    public void processArrivalQ1(Event evt) {
        // if the server is idle, fetch the event, do statistics and put into service
        if (numberInService1 == 0) scheduleDepartureQ1();
        else
            totalBusy1 += numberInService1*(clock - lastEventTime);
            totalBusy2 += numberInService2*(clock - lastEventTime);

        customersQ1.enqueue(evt);
        queueLength1++;
        // adjust max queue length registered yet
        if (maxQueueLength1 < queueLength1) maxQueueLength1 = queueLength1;

        // schedule the next arrival on the fist queue
        rngs.selectStream(tmp + 3);
        Event next_arrival = new Event(ARRIVAL_Q1, clock + rvgs.exponential(meanInterArrivalTime));
        futureEventList.enqueue(next_arrival);
        lastEventTime = clock;
    }

    public void scheduleDepartureQ1() {
        double ServiceTime;
        // get the job at the head of the queue
        rngs.selectStream(tmp + 5);
        while ((ServiceTime = rvgs.normal(meanServiceTime1, sigma1)) < 0);
        Event depart = new Event(DEPARTURE_Q1, clock + ServiceTime);
        futureEventList.enqueue(depart);
        numberInService1 = 1;
        queueLength1--;
    }

    public void scheduleDepartureQ2() {
        double ServiceTime;
        // get the job at the head of the queue
        rngs.selectStream(tmp + 7);
        while ((ServiceTime = rvgs.normal(meanServiceTime2, sigma2)) < 0);
        Event depart = new Event(DEPARTURE_Q2, clock + ServiceTime);
        futureEventList.enqueue(depart);
        numberInService2 = 1;
        queueLength2--;
    }
    
    public void processDepartureQ1(Event e, String caller) {
        totalBusy1 += numberInService1*(clock - lastEventTime);
        totalBusy2 += numberInService2*(clock - lastEventTime);
        lastEventTime = clock;
        // get the customer
        Event finished = (Event) customersQ1.dequeue();

        if (caller == "dropSim") {
            // if there are customers in the queue then schedule the departure of the next one
            if (queueLength1 > 0) scheduleDepartureQ1();
            else numberInService1 = 0; //server idle
            // measure the response time and add to the sum
            double response = (clock - finished.get_time());
            sumRespTimeQ1 += response;
            nDeparturesQ1++;
        }
        //Queue2 might be not full (if) or full (else)
        if (queueLength2 < maxQueue2Dim) {
            if (caller =="blockSim") {
                // if there are customers in the queue then schedule the departure of the next one
                if (queueLength1 > 0) scheduleDepartureQ1();
                else numberInService1 = 0; //server idle
                // measure the response time and add to the sum
                double response = (clock - finished.get_time());
                sumRespTimeQ1 += response;
                nDeparturesQ1++;
            }

            // if the server is idle, fetch the event, do statistics and put into service
            if (numberInService2 == 0) scheduleDepartureQ2();  //create event for customer leaving queue2
            else
                totalBusy1 += numberInService1*(clock - lastEventTime);
                totalBusy2 += numberInService2*(clock - lastEventTime);

            Event arrive_queue2 = new Event(finished.get_type(),clock);  //create event for customer getting in queue2
            customersQ2.enqueue(arrive_queue2);
            queueLength2++;
            // adjust max queue length registered yet
            if (maxQueueLength2 < queueLength2) maxQueueLength2 = queueLength2;

        } else { //queue2 is full
            if (caller == "blockSim") { //called from blockSim
                numberInService1 = 1;//block server1 until q2 frees a place
                blockedClient = new Event(finished.get_type(), clock); //create a substitute event for arrival in q2

            } else { //else means called from dropSim
                //Drop the client if queue 2 is full
                lostClients++;
            }
        }
    }

    public void processDepartureQ2(Event e) {
        totalBusy1 += numberInService1*(clock - lastEventTime);
        totalBusy2 += numberInService2*(clock - lastEventTime);
        // get the customer
        Event finished = (Event) customersQ2.dequeue();
        // if there are customers in the queue then schedule the departure of the next one
        if (queueLength2 > 0) scheduleDepartureQ2();
        else numberInService2 = 0; //server idle
        // measure the response time and add to the sum
        double response = (clock - finished.get_time());
        sumRespTimeQ2 += response;

        totalDepartures++;
        // if the func is called by dropSim then blockeUser will be null
        // if it's called by blockSim can be null or an Event object, in case there is a blockedClient on the serverQ1
        if (blockedClient != null) {
            queueLength2++; //add the now unlocked client into q2
            customersQ2.enqueue(blockedClient); //create departure from q2 for previously blocked client
            timeBlocked += (clock - blockedClient.get_time());          //t.blocco += t.attuale - t.fine servizio
            blockedClient = null;
            numberInService1=0; //unlock server 1
        }
        lastEventTime = clock;
    }

    // Print results functions

    public void printPointEstimates(String caller){
        //caller can be dropSim or blockSim, used to choose which line to print/not print
        System.out.println("------------------------------ Point Estimates ------------------------------");
        System.out.println("\tThroughput               " + (throughput/nRun));
        System.out.println("\tUtil queue 1             " + (utilQ1/nRun));
        System.out.println("\tUtil queue 2             " + (utilQ2/nRun));
        System.out.println("\tCustomers queue 1        " + (nCustomersQ1/nRun));
        System.out.println("\tCustomers queue 2        " + (nCustomersQ2/nRun));
        System.out.println("\tResponse time queue 1    " + (respTimeQ1/nRun));
        System.out.println("\tResponse time queue 2    " + (respTimeQ2/nRun));
        if (caller == "blockSim") System.out.println("\tBlocked time             " + (percBlockedTime/nRun)+" %");
        if (caller == "dropSim") System.out.println("\tDrop rate                " + (dropRate/nRun));
        System.out.println();
    }

    public void printIntervalEstimates(String caller){
        double sQuadroT = calculateS(arrayThroughput, (throughput/nRun))/(nRun-1);

        double[] sQuadroU = new double[2];
        sQuadroU[0] = calculateS(arrayUtil1, (utilQ1/nRun))/(nRun-1);
        sQuadroU[1] = calculateS(arrayUtil2, (utilQ2/nRun))/(nRun-1);

        double[] sQuadroNOC = new double[2];
        sQuadroNOC[0] = calculateS(arrayNCustomers1, (nCustomersQ1/nRun))/(nRun-1);
        sQuadroNOC[1] = calculateS(arrayNCustomers2, (nCustomersQ2/nRun))/(nRun-1);

        double[] sQuadroR = new double[2];
        sQuadroR[0] = calculateS(arrayRespTimeQ1, (respTimeQ1/nRun))/(nRun-1);
        sQuadroR[1] = calculateS(arrayRespTimeQ2, (respTimeQ2/nRun))/(nRun-1);

        // if dropSim else blockSim
        double sQuadroSpec = caller == "dropSim" ? calculateS(arrayDropRate, dropRate/nRun)/(nRun-1) : calculateS(arrayBlockRate, percBlockedTime/nRun)/(nRun-1);

        double sigmaT = sQuadroT/nRun;
        double sigmaU_0 = sQuadroU[0]/nRun;
        double sigmaU_1 = sQuadroU[1]/nRun;
        double sigmaNOC_0 = sQuadroNOC[0]/nRun;
        double sigmaNOC_1 = sQuadroNOC[1]/nRun;
        double sigmaR_0 = sQuadroR[0]/nRun;
        double sigmaR_1 = sQuadroR[1]/nRun;
        double sigmaSpec = sQuadroSpec/nRun;

        Rvms rvms = new Rvms();
        double tStudent = rvms.idfStudent(nRun-1,0.975);     //alpha=0.05, conf level=95%, t(0.025,3)

        System.out.println("------------------------------ Interval Estimates ------------------------------");
        double[] array;
        array = calculateInterval((throughput/nRun), tStudent, sigmaT);

        System.out.println("\tConf. I. Throughput           " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);

        array = calculateInterval((utilQ1/nRun), tStudent, sigmaU_0);
        System.out.println("\tConf. I. Util queue 1         " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);
        array = calculateInterval((utilQ2/nRun), tStudent, sigmaU_1);
        System.out.println("\tConf. I. Util queue 2         " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);

        array = calculateInterval( (nCustomersQ1/nRun), tStudent, sigmaNOC_0);
        System.out.println("\tConf. I. Customers queue 1    " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);
        array = calculateInterval( (nCustomersQ2/nRun), tStudent, sigmaNOC_1);
        System.out.println("\tConf. I. Customers queue 2    " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);

        array = calculateInterval((respTimeQ1/nRun), tStudent, sigmaR_0);
        System.out.println("\tConf. I. resp time queue 1    " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);
        array = calculateInterval((respTimeQ2/nRun), tStudent, sigmaR_1);
        System.out.println("\tConf. I. resp time queue 2    " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);

        array = caller == "dropSim" ? calculateInterval((dropRate/nRun), tStudent, sigmaSpec) : calculateInterval((percBlockedTime/nRun), tStudent, sigmaSpec);
        if (caller == "dropSim") System.out.println("\tConf. I. drop rate            " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);
        if (caller == "blockSim") System.out.println("\tConf. I. %  blocked time      " + "MAX: "+array[0]+ "\t\tMIN:  "+ array[1]);
    }
}