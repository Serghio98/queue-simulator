import java.util.*;
import funcs.*;
import funcs.Queue;
 
class BlockSim extends Funcs {
    Funcs funcs = new Funcs();

    public void main() {
        rngs.selectStream(0);
        rngs.plantSeeds(0);
        input(true); //true as second parameter as we use 2 queues
        initArrays();
        for (int i = 0; i < nRun; i++) {
            tmp = i;
            futureEventList = new EventList();
            customersQ1 = new Queue();
            customersQ2 = new Queue();

            initialization();
            // Loop until initial customers have departed
            while (totalDepartures < totalCustomers) {
                Event evt = (Event) futureEventList.getMin(); // get next event
                futureEventList.dequeue();                    // dequeue event
                clock = evt.get_time();                       // advance simulation time
                if (evt.get_type() == ARRIVAL_Q1) processArrivalQ1(evt);
                if (evt.get_type() == DEPARTURE_Q1) processDepartureQ1(evt, "blockSim");
                if (evt.get_type() == DEPARTURE_Q2) processDepartureQ2(evt);
            }
            printSimResult();
        }
        printPointEstimates("blockSim");
        printIntervalEstimates("blockSim");
    }

    void initialization() {
        initCommon();
        blockedClient = null;
        timeBlocked = 0;
    }

    void initArrays() {
        //Initialization arrays interval estimates
        initCommonArrays();
        arrayBlockRate = new double[nRun];
        Arrays.fill(arrayBlockRate, 0);
    }

    // molto simile al corrispettivo in dropSim ma cercare di fare un unico metodo lo avrebbe
    // reso illegibile per colpa dei troppi if else statements
    void printSimResult() {
        System.out.println("------------------------------ RUN " + (tmp+1) + " ------------------------------");
        System.out.println("\tThroughput                " + totalDepartures / clock);
        System.out.println("\tCustomers                 " + totalCustomers);
        System.out.println("\tUtil queue 1              " + (totalBusy1/*+timeBlocked*/) / clock); //JMT lo calcola senza contare il tempo in cui sta bloccato
        System.out.println("\tUtil queue 2              " + totalBusy2 / clock);
        System.out.println("\tMax users in queue 1      " + maxQueueLength1);
        System.out.println("\tMax users in queue 2      " + maxQueueLength2);
        System.out.println("\tAvg resp time queue 1     " + (sumRespTimeQ1 + timeBlocked) / nDeparturesQ1 + " min");
        System.out.println("\tAvg resp time queue 2     " + sumRespTimeQ2 / totalCustomers + " min");
        System.out.println("\tClock                     " + clock + " min");
        System.out.println("\tDepartures from system    " + totalDepartures);//same with totalCustomers
        System.out.println("\t% block time              " + (timeBlocked / clock)*100 + " %");
        System.out.println("-----------------------------------------------------------------------------------\n");

        //Update indexes point estimates
        throughput += totalDepartures / clock;
        utilQ1 += (totalBusy1/*+timeBlocked*/) / clock;
        utilQ2 += totalBusy2 / clock;
        nCustomersQ1 += (sumRespTimeQ1+timeBlocked) / clock;
        nCustomersQ2 += sumRespTimeQ2 / clock;
        respTimeQ1 += (sumRespTimeQ1 + timeBlocked) / nDeparturesQ1;
        respTimeQ2 += sumRespTimeQ2 / totalCustomers;
        percBlockedTime += (timeBlocked / clock)*100;

        //Update arrays interval estimates
        arrayThroughput[tmp] = totalDepartures / clock;

        arrayUtil1[tmp] = (totalBusy1/*+timeBlocked*/) / clock;
        arrayUtil2[tmp] = totalBusy2 / clock;

        arrayNCustomers1[tmp] = (sumRespTimeQ1+timeBlocked)/clock;
        arrayNCustomers2[tmp] = sumRespTimeQ2/clock;

        arrayRespTimeQ1[tmp] = (sumRespTimeQ1 + timeBlocked) / nDeparturesQ1;
        arrayRespTimeQ2[tmp] = sumRespTimeQ2 / totalCustomers;

        arrayBlockRate[tmp] = (timeBlocked / clock)*100;
    }
}