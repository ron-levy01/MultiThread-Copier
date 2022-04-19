// Ron Levy - 208677203

import java.io.File;

public class Scouter implements Runnable {
    int id;
    SynchronizedQueue<File> directoryQueue;
    File root;
    SynchronizedQueue<String> milestonesQueue;
    boolean isMilestones;
    public static int folders_scouted_counter = 0;

    // ctor
    public Scouter(int id, SynchronizedQueue<File> directoryQueue, File root
            , SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    // helper recursive func
    public void enqueueFile (File currentDirectory) throws NullPointerException {
        // stop rec
        if (currentDirectory.listFiles().length == 0) {
            return;
        }

        for (File file : currentDirectory.listFiles(File::isDirectory)) {
            if (folders_scouted_counter++ <  DiskSearcher.MAXIMUM_FOLDERS){
                directoryQueue.enqueue(file);
                enqueueFile(file);
                if (isMilestones) milestone(id, file.getName());

            }
        }
    }

    private void milestone(int id, String fileName){
        milestonesQueue.registerProducer();
        milestonesQueue.enqueue("Scouter on thread id " + id + ": directory named " + fileName +
                " was scouted");
        milestonesQueue.unregisterProducer();
    }

    public void run() {
        directoryQueue.registerProducer();
        try {
            if (root.isDirectory() && folders_scouted_counter + 1 <  DiskSearcher.MAXIMUM_FOLDERS) {
                directoryQueue.enqueue(root);
                if (isMilestones) milestone(id, root.getName());
                enqueueFile(root);
            }
        } catch (NullPointerException exep) {
            exep.printStackTrace();
        }

        this.directoryQueue.unregisterProducer();
    }
}