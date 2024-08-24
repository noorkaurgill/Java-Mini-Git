package gitlet;
import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

/** A snapshot of the staging area with the latest commit,
 * staged files to be removed,
 * files to remove when committing, and all of the staged files.
 * @author Noor Gill */
class Stage implements Serializable {

    /** Pointer to the latest commit.*/
    private Commit currentCommit;
    /** Files that are staged for commit, in an ArrayList. */
    private ArrayList<String> staged;
    /** Files that staged and clear after each commit, in an ArrayList. */
    private ArrayList<String> stagedToClear;
    /** Files that should not be tracked in the next commit, in an ArrayList. */
    private ArrayList<String> forRemoval;

    /** Retrieves the latest commit.
     * @return the current commit. */
    Commit getCurrentCommit() {
        return currentCommit;
    }
    /** Retrieves the staged files.
     * @return the files that have been staged, in an arraylist. */
    ArrayList<String> getStaged() {
        return staged;
    }
    /** Retrieves the files that are staged and cleared after each commit.
     * @return the files that are staged and
     * cleared after committing, in an arraylist. */
    ArrayList<String> getStagedToClear() {
        return stagedToClear;
    }
    /** Retrieves the files that should not be tracked in the next commit.
     * @return the files that should be removed for the next commit. */
    ArrayList<String> getForRemoval() {
        return forRemoval;
    }

    /** Constructs a stage associated with the most recent commit.
     * @param latestCommit the most recent commit. */
    Stage(Commit latestCommit) {
        currentCommit = latestCommit;
        stagedToClear = new ArrayList<>(); staged = new ArrayList<>();
        forRemoval = new ArrayList<>();
        Set<String> addedFiles = latestCommit.getFileUpdatedToStage();
        if (addedFiles != null) {
            staged.addAll(addedFiles);
        }
    }

    /** If a file exists and it has changed,
     * then it is added to the staging area.
     * If the file was marked for removal, that marker is deleted.
     * @param file the file. */
    void add(String file) {
        File toAdd = new File(file);
        if (!toAdd.exists()) {
            System.out.println("File does not exist.");
        }
        String saved = null;
        String currentSHA = Utils.sha1(Utils.readContentsAsString(toAdd));
        if (currentCommit.contains(file)) {
            saved = currentCommit.getFileMapped().get(file);
        }
        if (!currentSHA.equals(saved)) {
            staged.add(file);
            stagedToClear.add(file);
        } else {
            if (staged.contains(file)) {
                staged.remove(file);
            }
        }
        if (forRemoval.contains(file)) {
            forRemoval.remove(file);
        }
    }

    /** Removes a file from the staging area.
     * @param file the file. */
    public void remove(String file) {
        if (staged.contains(file)) {
            if (currentCommit.contains(file)) {
                forRemoval.add(file);
                Utils.restrictedDelete(file);
            }
            stagedToClear.remove(file);
            staged.remove(file);
        } else if (stagedToClear.contains(file)) {
            if (currentCommit.contains(file)) {
                forRemoval.add(file);
                Utils.restrictedDelete(file);
            }
            stagedToClear.remove(file);
            staged.remove(file);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    /** For syntactical purposes. */
    @Override
    public String toString() {
        StringBuilder myString = new StringBuilder();
        myString.append("=== Staged Files === \n");
        for (String file : stagedToClear) {
            myString.append(file + "\n");
        }
        myString.append("\n" + "=== Removed Files === \n");
        for (String fileName : forRemoval) {
            myString.append(fileName + "\n");
        }
        return myString.toString();
    }
}

