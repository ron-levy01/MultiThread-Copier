// Ron Levy - 208677203

import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;

public class Copier implements Runnable {

    public static final int COPY_BUFFER_SIZE = 1;
    int Id;
    SynchronizedQueue<String> milestoneQueue;
    SynchronizedQueue<File> resultQueue;
    boolean isMilestones;
    File destination;

    // ctor
    public Copier(int id, File destination, SynchronizedQueue<File> resultsQueue,
                  SynchronizedQueue<String> milestonesQueue, boolean isMilestones) {
        this.Id = id;
        this.resultQueue = resultsQueue;
        this.destination = destination;
        this.milestoneQueue = milestonesQueue;
        this.isMilestones = isMilestones;
    }

    public void run() {

        if (!destination.exists()) {
            if (!destination.mkdir()) {
                return;
            }
        }

        File fileToMove = resultQueue.dequeue();
        try {
            while (fileToMove != null) {
                File dest = new File(destination, fileToMove.getName());
                Files.copy(fileToMove.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (isMilestones) mileStone(Id, fileToMove.toString());
                fileToMove = resultQueue.dequeue();
            }
        } catch (IOException exep) {
            exep.printStackTrace();
        }
    }

    private void mileStone(int id, String fileToMove) {
        milestoneQueue.registerProducer();
        milestoneQueue.enqueue("Copier from thread id " + Id + ": file named" + fileToMove +" was copied");
        milestoneQueue.unregisterProducer();
    }
}