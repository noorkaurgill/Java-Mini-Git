package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

/** A snapshot of the tree that is used to run
 * all commands for the version-control system.
 * @author Noor Gill */
public class Tree implements Serializable {

    /** Pointer to the current branch. */
    private Branch currentBranch;
    /** Maps each SHA1 identification to its commit, unique. */
    private Map<String, Commit> commits;
    /** Substring of SHA1, shorter and more accessible version. */
    private HashMap<String, String> subSHA;
    /** Maps name of branch to each corresponding branch. */
    private Map<String, Branch> branchMapping;
    /** Maps messages on commits to the SHA1 id of the commits
     * with that particular message.*/
    private Map<String, ArrayList<String>> convert;
    /** Checker for changes in between commits. */
    private boolean changeTrack;
    /** Boolean used for adding and removing branch functionality. */
    private boolean check;
    /** Returns the current branch. */
    private Branch getCurrentBranch() {
        return currentBranch;
    }
    /** Constructs the tree data structure. */
    public Tree() {
        commits = new HashMap<>(); subSHA = new HashMap<>();
        branchMapping = new HashMap<>(); convert = new HashMap<>();
    }
    /** Retrieves a new version-control system within
     * the current directory with a commit, master branch, and timeStamp,
     * which is representative of the Unix Epoch, as described in the spec.
     * @return version-control system for Gitlet. */
    public static Tree init() {
        ArrayList<String> aSHAs = new ArrayList<>();
        Tree test = new Tree();
        String branchName = "master";
        String message = "initial commit";
        Commit initial = new Commit(null, message);
        test.commits.put(initial.getSHA(), initial);
        Branch start = new Branch(branchName, initial);
        test.currentBranch = start;
        test.branchMapping.put(branchName, start);
        aSHAs.add(initial.getSHA());
        test.convert.put(message, aSHAs);
        return test;
    }
    /** Inserts a copy of the file into the staging area.
     * @param file the file to be staged. */
    public void add(String file) {
        assert (file != null);
        currentBranch.stageAFile(file);
    }

    /** Saves a picrure of the files in the current commit and staging area
     * and develops a new commit with a corresponding message.
     * @param message the commit message. */
    public void commit(String message) {
        assert message != null;
        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
        }
        currentBranch.commit(message);
        Commit current = currentBranch.getRecentCommit();
        String curSHA = current.getSHA();
        commits.put(curSHA, current);
        ArrayList<String> curr = convert.get(message);
        if (curr != null) {
            curr.add(current.getSHA());
            convert.put(message, curr);
            String check1SHA = current.getSHA().substring(0, 6);
            subSHA.put(check1SHA, current.getSHA());
        } else {
            curr = new ArrayList<>();
        }
    }
    /** If the branch exists and it is not the current branch,
     * and the files within it have been tracked, the branch is
     * checked out.
     * @param branch the branch. */
    void checkout1(String branch) {
        changeHelper();
        if (changeTrack) {
            return;
        }
        if (!branchMapping.containsKey(branch)) {
            System.out.println("No such branch exists.");
        }
        if (currentBranch.getBranch().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
        }
        Commit lastCommit = currentBranch.getRecentCommit();
        for (String file : lastCommit.getFileUpdatedToStage()) {
            String savedFile = lastCommit.getFileMapped().get(file);
            String currentFile = Utils.sha1(Utils.readContentsAsString
                    (new File(file)));
            if (!savedFile.equals(currentFile)) {
                System.out.println("There is an untracked file "
                        + "in the way delete it or add it first.");
            } else {
                Utils.restrictedDelete(file);
            }
        }
        currentBranch = branchMapping.get(branch);
        lastCommit.checkout();
    }

    /** Checks if there are untracked files in the directory
     * as well as any untracked changes. */
    private void changeHelper() {
        String createGit = currentBranch.getRecentCommit().getCommitDir();
        int a = createGit.length();
        File gitlet = new File(createGit);
        int b = gitlet.getAbsolutePath().length();
        String pathMake = gitlet.getAbsolutePath();
        String path = pathMake.substring(0, b - a + 1);
        File gitPath = new File(path);
        File[] files = gitPath.listFiles();
        if (files != null) {
            System.out.println("There is an untracked file in the way "
                    + "delete it or add it first.");
        }
        for (File file : files) {
            if (file.isFile()) {
                Stage currentStage = currentBranch.getStaging();
                if (currentStage != null) {
                    System.out.println("There is an untracked file in the way "
                            + "delete it or add it first.");
                }
                if (!currentStage.getStagedToClear().contains(file.getName())) {
                    Commit currentCommit = currentBranch.getRecentCommit();
                    if (currentCommit.contains(
                            file.getName())) {
                        String fileSHA = Utils.sha1(
                                Utils.readContentsAsString(file));
                        String commitSHA = currentCommit.getFileMapped()
                                .get(file.getName());
                        if (commitSHA.equals(fileSHA)) {
                            changeTrack = false;
                        }
                    } else {
                        changeTrack = false;
                    }
                }
                if (!currentStage.getStaged().isEmpty()) {
                    changeTrack = false;
                }
            }
        }
        changeTrack = true;
        System.out.println("There is an untracked file in the way "
                + "delete it or add it first.");
    }

    /** If the branch exists and it is not the current branch,
     * and the files within it have been tracked, the branch is
     * checked out.
     * @param branch the branch. */
    void checkout2(String branch) {
        Commit lastCommit = currentBranch.getRecentCommit();
        if (lastCommit.contains(branch)) {
            lastCommit.checkout(branch);
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /** If the file and commit exist, a file from the commit
     * with a particular commit SHA1 id is checked out.
     * @param id the commit id of the file.
     * @param file the name of the file. */
    void checkout3(String id, String file) {
        if (id.length() == 6) {
            id = subSHA.get(id);
        }
        if (id == null || !commits.containsKey(id)) {
            System.out.println("No commit with that id exists.");
        }
        Commit toCheckout = commits.get(id);
        if (toCheckout.contains(file)) {
            toCheckout.checkout(file);
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /** Prints information for each commit starting from current commit
     * up until first commit; the first parent's commits are followed and
     * second parents in merges are not accounted for in display.*/
    void log() {
        Commit now = currentBranch.getRecentCommit();
        while (now != null) {
            System.out.println(now.toString());
            System.out.println();
            now = now.getParentCommit();
        }
    }

    /** Same as log except includes information for
     * all commits. */
    void globalLog() {
        for (String str : commits.keySet()) {
            Commit comm = commits.get(str);
            System.out.println(comm.toString());
            System.out.println();
        }
    }

    /** If a file is staged, it is removed from the staging area.
     * If a file is tracked in the current commit, it is marked so it
     * is not included in the following commit. The file is also removed
     * from the working directory as long as it is tracked within the
     * current commit.
     * @param file the file.
     */
    public void remove(String file) {
        assert (file != null);
        currentBranch.removeAFile(file);
    }

    /** Prints the SHA1 id of every commit with the message
     * provided with line breaks.
     * @param message the message given. */
    void find(String message) {
        if (message == null) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (!convert.containsKey(message)) {
            System.out.println("Found no commit with that message.");
        }
        for (String sHA : convert.get(message)) {
            System.out.println(sHA);
        }
    }

    /** Prints the branches that exist at the moment.
     * The current branch is marked with a * symbol.
     * The files that are marked to track and the files
     * that are staged are also marked as well. */
    void status() {
        System.out.println(this.toString());
    }

    /** Add the pointer to a branch.
     * @param branch the branch.
     */
    void addBranch(String branch) {
        branchHelper(branch);
        if (check) {
            System.out.println("A branch with "
                    + "that name already exists.");
        }
        branchMapping.put(branch, new Branch(branch,
                currentBranch.getRecentCommit()));
    }

    /** Remove the pointer to a branch.
     * @param branch the branch. */
    void removeBranch(String branch) {
        branchHelper(branch);
        if (!check) {
            System.out.println("A branch with that name does not exist.");
        }
        String now = currentBranch.getBranch();
        if (branch.equals(now)) {
            System.out.println("Cannot remove "
                    + "the current branch.");
        }
        branchMapping.remove(branch);
    }

    /** Helper method for adding and removing pointers to branches.
     * @param branch the branch.
     */
    private void branchHelper(String branch) {
        check = false;
        if (branch == null) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (branchMapping.containsKey(branch)) {
            check = true;
        }
    }

    /** All files tracked with a particular commit identification
     * are checked out. Tracked files that are not in the current commit
     * are removed and the current branch head is moved to the current
     * commit node.
     * @param id the commit id. */
    void reset(String id) {
        assert (id != null);
        if (!commits.containsKey(id)) {
            System.out.println("No commit with that id exists.");
        }
        Commit ids = commits.get(id);
        changeHelper();
        if (changeTrack) {
            return;
        }
        ids.checkout();
        currentBranch.setRecentCommit(ids);
        currentBranch.setTheStage(null);
    }

    /** Merge a branch with the current branch.
     * @param branch the branch to be merged with. */
    public void merge(String branch) {
        if (branch == null) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (!branchMapping.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
        }
        if (branch.equals(currentBranch.getBranch())) {
            System.out.println("Cannot merge a branch with itself.");
        }
        changeHelper();
        if (changeTrack) {
            return;
        }
        currentBranch.merge(branchMapping.get(branch));
    }
    /** For syntactical purposes. */
    @Override
    public String toString() {
        StringBuilder myString = new StringBuilder();
        myString.append("=== Branches === \n" + "*"
                + getCurrentBranch().getBranch() + "\n");
        for (String string : branchMapping.keySet()) {
            if (!string.equals(getCurrentBranch().getBranch())) {
                myString.append(string + "\n");
            }
        }
        myString.append("\n");
        if (getCurrentBranch().getStaging() == null) {
            myString.append("=== Staged Files === \n \n");
            myString.append("=== Removed Files === \n \n");
        } else {
            myString.append(getCurrentBranch().getStaging().toString() + "\n");
        }
        myString.append("=== Modifications Not Staged For Commit === \n"
                + "\n" + "=== Untracked Files === \n");
        return myString.toString();
    }
}

