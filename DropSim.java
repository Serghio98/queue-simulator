import java.util.*;
import funcs.*;
import funcs.Queue;

class DropSim extends Funcs {
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
            System.err.println("test1");
            initialization();

            // Loop until initial customers have departed
            System.err.println("before while print");
            while ((totalDepartures+lostClients) < totalCustomers) {
                // if ((totalDepartures+lostClients)%1000 == 0) 
                System.err.println(totalDepartures+lostClients);
                Event evt = (Event) futureEventList.getMin(); // get next event
                futureEventList.dequeue();                    // dequeue event
                clock = evt.get_time();                       // advance simulation time
                if (evt.get_type() == ARRIVAL_Q1) processArrivalQ1(evt);
                if (evt.get_type() == DEPARTURE_Q1) processDepartureQ1(evt, "dropSim");
                if (evt.get_type() == DEPARTURE_Q2) processDepartureQ2(evt);
            }
            System.err.println("after while print");
            printSimResult();
        }
        printPointEstimates("dropSim");
        printIntervalEstimates("dropSim");
    }

    void initialization() {
        initCommon();
        lostClients = 0;
    }

    void initArrays() {
        //Initialization arrays interval estimates
        initCommonArrays();
        arrayDropRate = new double[nRun];
        Arrays.fill(arrayDropRate, 0);
    }

    // molto simile al corrispettivo in blockSim ma cercare di fare un unico metodo lo avrebbe
    // reso illegibile per colpa dei troppi if statements
    void printSimResult() {
        System.out.println("------------------------------ RUN " + (tmp+1) + " ------------------------------");
        System.out.println("\tThroughput                " + totalDepartures / clock);
        System.out.println("\tCustomers                 " + totalCustomers); // = to lostClients + totalDepartures
        System.out.println("\tUtil queue 1              " + totalBusy1 / clock);
        System.out.println("\tUtil queue 2              " + totalBusy2 / clock);
        System.out.println("\tCustomers queue 1         " + sumRespTimeQ1 / clock);
        System.out.println("\tCustomers queue 2         " + sumRespTimeQ2 / clock);
        System.out.println("\tMax clients in queue 1    " + maxQueueLength1);
        System.out.println("\tMax clients in queue 2    " + maxQueueLength2);
        System.out.println("\tAvg resp time queue 1     " + sumRespTimeQ1 / (nDeparturesQ1+lostClients) + " min");
        System.out.println("\tAvg resp time queue 2     " + sumRespTimeQ2 / totalCustomers + " min");
        System.out.println("\tClock                     " + clock + " min");
        System.out.println("\tDropped clients           " + lostClients);
        System.out.println("\tDepartures from system    " + totalDepartures); //does not count the dropped clients
        // System.out.println("\tDrop rate %               " + ((float) lostClients / totalCustomers)*100); //percentage of clients lost
        System.out.println("\tDrop rate %               " + (float) lostClients / clock); //drop rate based on time
        System.out.println("-----------------------------------------------------------------------------------\n");

        //Update indexes point estimates
        throughput += totalDepartures / clock;
        utilQ1 += totalBusy1 / clock;
        utilQ2 += totalBusy2 / clock;
        nCustomersQ1 += sumRespTimeQ1 / clock;
        nCustomersQ2 += sumRespTimeQ2 / clock;
        respTimeQ1 += sumRespTimeQ1 / (nDeparturesQ1+lostClients);
        respTimeQ2 += sumRespTimeQ2 / totalCustomers;
        // dropRate += ((float) lostClients / totalCustomers)*100;
        dropRate += (float) lostClients / clock;

        //Update arrays interval estimates
        arrayThroughput[tmp] = totalDepartures / clock;

        arrayUtil1[tmp] = totalBusy1 / clock;
        arrayUtil2[tmp] = totalBusy2 / clock;

        arrayNCustomers1[tmp] = sumRespTimeQ1/clock;
        arrayNCustomers2[tmp] = sumRespTimeQ2/clock;

        arrayRespTimeQ1[tmp] = sumRespTimeQ1 / (nDeparturesQ1+lostClients);
        arrayRespTimeQ2[tmp] = sumRespTimeQ2 / (totalCustomers);

        // arrayDropRate[tmp] = ((float) lostClients / totalCustomers)*100;
        arrayDropRate[tmp] = (float) lostClients / clock;

        //NOTE JMT calcola il drop rate come clienti persi in rapporto del tempo totale della simulazione
        //piuttosto che la percentuale di clienti persi. Io avrei calcolato la % di clienti, ma ho modificato per 
        //far corrispondere i risultati
    }
}