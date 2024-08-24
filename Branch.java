package gitlet;
import java.io.File;
import java.io.Serializable;

/** Class representing a branch that points to sequences of related commits.
 * @author Noor Gill */
class Branch implements Serializable {

    /** Branch name.*/
    private String branch;
    /** Branch head. */
    private Commit recentCommit;
    /** Snapshot of the staging area. */
    private Stage staging;
    /** Checker for merge conflicts. */
    private boolean conflict;

    /** Retrieves the name of the branch.
     * @return the branch name. */
    String getBranch() {
        return branch;
    }
    /** Retrieves the head of the branch.
     * @return the branch head. */
    Commit getRecentCommit() {
        return recentCommit;
    }
    /** Retrieves the staging.
     * @return the staging area. */
    Stage getStaging() {
        return staging;
    }

    /** Each branch has a...
     * @param name given name.
     * @param data latest commit. */
    Branch(String name, Commit data) {
        branch = name;
        recentCommit = data;
        staging = new Stage(data);
    }

    /** File is added to staging area.
     * @param file the file.  */
    void stageAFile(String file) {
        staging.add(file);
    }
    /** File is removed from staging area.
     * @param file the file. */
    void removeAFile(String file) {
        staging.remove(file);
    }
    /** The latest commit is set.
     * @param commit the commit. */
    void setRecentCommit(Commit commit) {
        this.recentCommit = commit;
    }
    /** The staging is set.
     * @param stage the stage. */
    void setTheStage(Stage stage) {
        this.staging = stage;
    }
    /** Commits a merge with a...
     * @param message message.
     * @param parent1 merge parent.
     * @param parent2 merge parent. */
    public void commit(String message, String parent1, String parent2) {
        recentCommit = new Commit(staging, message, parent1, parent2);
        staging = new Stage(recentCommit);
    }

    /** Returns the split point of 2 branches.
     * @param x first branch.
     * @param y second branch. */
    private Commit splitPoint(Branch x, Branch y) {
        Commit xCommit = x.getRecentCommit();
        Commit yCommit = y.getRecentCommit();
        int diff = y.length() - y.length();
        for (int i = 0; i < diff; i++) {
            xCommit = xCommit.getParentCommit();
        }
        while (!xCommit.equals(yCommit)) {
            xCommit = xCommit.getParentCommit();
            yCommit = yCommit.getParentCommit();
        }
        return xCommit;
    }
    /** Merges the current branch with.
     * @param branchy another branch. */
    void merge(Branch branchy) {
        Commit givenHead = branchy.getRecentCommit();
        Commit splitter = splitPoint(this, branchy);
        conflict = false;
        Commit currHead = recentCommit;
        if (givenHead.equals(splitter)) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
        }
        if (!staging.getStagedToClear().isEmpty()) {
            System.out.println("You have uncommitted changes.");
        }
        if (currHead.equals(splitter)) {
            recentCommit = givenHead;
            System.out.println("Current branch fast-forwarded.");
        }
        for (String fileName : givenHead.getFileUpdatedToStage()) {
            if (!splitter.contains(fileName) && !currHead.contains(fileName)) {
                givenHead.checkout(fileName);
                stageAFile(fileName);
            } else if (!givenHead.altered(currHead, fileName)) {
                checkForConflict(currHead, givenHead, fileName);
            } else if (currHead.contains(fileName)
                    && currHead.altered(splitter, fileName)) {
                File tester = new File(fileName);
                if (tester.exists()) {
                    String check1 = givenHead.getCommitDir();
                    String check2 = givenHead.getFileMapped().get(fileName);
                    String check3 = givenHead.getDash() + fileName;
                    Utils.writeContents(tester,
                            Utils.readContentsAsString(
                                    new File(check1 + check2 + check3)));
                }
            } else if (splitter.contains(fileName)
                && givenHead.altered(splitter, fileName)) {
                return;
            }
        }
        for (String fileName : currHead.getFileUpdatedToStage()) {
            if (currHead.altered(splitter, fileName)
                    && !givenHead.contains(fileName)) {
                removeAFile(fileName);
            } else if (!givenHead.altered(currHead, fileName)) {
                checkForConflict(currHead, givenHead, fileName);
            } else if (!splitter.contains(fileName)
                    && !givenHead.contains(fileName)) {
                return;
            }
        }
        String message = "Merged " + branchy.getBranch()
                + " into " + this.getBranch() + ".";
        commit(message, currHead.getSHA().substring(0, 6),
                givenHead.getSHA().substring(0, 6));
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Based on the conflicts in the.
     * @param current current head
     * @param given given head,
     *                  it changes the files in the
     * @param add working directory. */
    private void checkForConflict(Commit current,
                                  Commit given, String add) {
        String head1 = "";
        String head2 = "";
        if (given.contains(add)) {
            head2 = Utils.readContentsAsString(given.getFile(add));
        }
        if (current.contains(add)) {
            head1 = Utils.readContentsAsString(current.getFile(add));
        }
        Utils.writeContents(new File(add), "<<<<<<< HEAD\n",
                head1, "=======\n", head2, ">>>>>>>\n");
        conflict = true;
    }

    /** Retrieves the length of the current branch.
     * @return an integer representing branch length. */
    private int length() {
        int counter = 0;
        Commit current = recentCommit;
        while (current != null) {
            current = current.getParentCommit();
            counter++;
        }
        return counter;
    }

    /** A commit with message for staging.
     * @param message the message.
     */
    public void commit(String message) {
        recentCommit = new Commit(staging, message);
        staging = new Stage(recentCommit);

    }
}
