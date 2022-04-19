// Ron Levy - 208677203

import java.io.File;

public class DiskSearcher {

    public static final int MAXIMUM_FOLDERS = 10;
    public static final int RESULTS_QUEUE_CAPACITY = 50;
    public static final int DIRECTORY_QUEUE_CAPACITY = 50;

    public static void main(String[] args) {

        int id = 0;
        String fileExtension = args[1];
        File rootDirectory = new File(args[2]);
        File destinationDirectory = new File(args[3]);
        boolean milestoneQueueFlag = Boolean.parseBoolean(args[0]);
        long startTime = System.currentTimeMillis();
        int searchers = Integer.parseInt(args[4]);
        int copyNumber = Integer.parseInt(args[5]);

        // input val
        if ( copyNumber < 0 || searchers < 0 ) {
            throw new NumberFormatException();
        }
        if (args.length != 6) {
            System.out.println("illegal number of arguments");
        }

        SynchronizedQueue<File> resultQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);
        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<String> milestonesQueue = null;

        // milestone
        if (milestoneQueueFlag) {
            milestonesQueue = new SynchronizedQueue<String>(1000);
            milestonesQueue.registerProducer();
            milestonesQueue.enqueue("General, program has started the search");
            milestonesQueue.unregisterProducer();
        }

        Scouter scouter = new Scouter(id + 1,directoryQueue, rootDirectory,milestonesQueue,milestoneQueueFlag);
        Thread scouterThread = new Thread(scouter);
        scouterThread.start();

        Thread[] searcheThreads = new Thread[searchers];
        for (int i = 0; i < searchers; i++) {
            Searcher searcher = new Searcher(id + 1,fileExtension,directoryQueue,resultQueue,milestonesQueue,milestoneQueueFlag);
            Thread searcherThread = new Thread(searcher);
            searcheThreads[i] = searcherThread;
            searcheThreads[i].start();
        }

        Thread[] copierThreads = new Thread[copyNumber];
        for (int i = 0; i < copyNumber; i++) {
            Copier copier = new Copier(id + 1,destinationDirectory,resultQueue,milestonesQueue,milestoneQueueFlag);
            Thread copierThread = new Thread(copier);
            copierThreads[i] = copierThread;
            copierThreads[i].start();
        }

        try {
            scouterThread.join();
            for (Thread currThread : searcheThreads) currThread.join();
            for (Thread currThread : copierThreads) currThread.join();

        } catch (InterruptedException exep) {
            exep.printStackTrace();
        }

        long endTime = System.currentTimeMillis() - startTime;
        System.out.printf("Running time is " + endTime + " milliseconds");
    }
}
