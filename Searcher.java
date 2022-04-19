// Ron Levy - 208677203

import java.io.File;

public class Searcher implements Runnable {

    private int id;
    private String extension;
    private SynchronizedQueue<File> directoryQueue;
    private SynchronizedQueue<String> milestoneQueue;
    private SynchronizedQueue<File> resultQueue;
    boolean isMilestone;

    // ctor
    public Searcher(int id, String extension, SynchronizedQueue<File> directoryQueue,
                    SynchronizedQueue<File> resultsQueue,
                    SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {

        this.id = id;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.milestoneQueue = milestonesQueue;
        this.resultQueue = resultsQueue;
        this.isMilestone = isMilestones;
    }

    public void run() {
        File directory = directoryQueue.dequeue();
        resultQueue.registerProducer();

        try {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(extension)) {
                    resultQueue.enqueue(file);
                    if (isMilestone) milestone(file.getName());
                }
            }
        }
        catch (NullPointerException exep){
            exep.getStackTrace();
        }

        resultQueue.unregisterProducer();
    }

    private void milestone(String fileName){
        milestoneQueue.registerProducer();
        milestoneQueue.enqueue("Searcher on thread id " + id + ": file named " + fileName + " was found");
        milestoneQueue.unregisterProducer();
    }
}
