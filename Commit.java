package gitlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Map;
import java.util.Date;
import java.util.Set;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.nio.file.Paths;
import java.nio.file.Path;

/** Defines features of a commit.
 * @author Noor Gill */
public class Commit implements Serializable {

    /** File name is mapped to its unique SHA1 identification.
     * This id is directly from parent's but is different if
     * the file is updated from previous version.*/
    private Map<String, String> fileMapped;
    /** File added name mapped to the SHA1 identification
     * of its commit.*/
    private Map<String, String> filesUpdatedToStage;
    /** The commit's home directory. */
    private String commitDir;
    /** The parent commit of the current, "child" commit. */
    private Commit parentCommit;
    /** The message for the commit. */
    private String message;
    /** The timestamp for the commit. */
    private Date time;
    /** The unique SHA1 identification for the commit. */
    private String sHA;
    /** Checker for whether or not the current commit is a merge. */
    private boolean hasMerged = false;
    /** The SHA1 identifications of the parents involved in
     * the merge. */
    private String mergeParents;
    /** Retrieve mapped files.
     * @return the files names mapped to their SHA ids. */
    Map<String, String> getFileMapped() {
        return fileMapped;
    }
    /** Retrieve map of file name to directory.
     * @return the file names mapper to the SHA ids of respective commits. */
    Set<String> getFileUpdatedToStage() {
        return filesUpdatedToStage.keySet();
    }
    /** Retrieve the commit directory.
     * @return the commit directory. */
    String getCommitDir() {
        return commitDir;
    }
    /** Retrieve the parent of the current commit.
     * @return the parent of the commit currently being considered. */
    Commit getParentCommit() {
        return parentCommit;
    }
    /** Retrieve the message of the current commit.
     * @return the message associated with the commit. */
    private String getMessage() {
        return message;
    }
    /** Retrieve the timestamp of the current commit.
     * @return the timestamp associated with the commit. */
    private Date getTime() {
        return time;
    }
    /** Retrieve the unique SHA1 identification of the current commit.
     * @return the SHA id associated with the commit. */
    String getSHA() {
        return sHA;
    }

    /** Retrieve the name of the file.
     * @param file the file.
     * @return the file name. */
    File getFile(String file) {
        File saver = new File(commitDir + fileMapped.get(file)
                + File.separator + file);
        return saver;
    }
    /** Retrieve the / symbol used to separate objects in paths.
     * @return the "/" symbol. */
    String getDash() {
        return File.separator;
    }

    /** Obtains information for each commit from the stage.
     * @param snap the stage picture. */
    public Commit(Stage snap) {
        fileMapped = new HashMap<>();
        filesUpdatedToStage = new HashMap<>();
        if (snap == null) {
            getTimeStamp();
        } else {
            parentCommit = snap.getCurrentCommit();
            time = new Date();
        }
        if (parentCommit != null) {
            filesUpdatedToStage.putAll(parentCommit.filesUpdatedToStage);
            if (snap.getForRemoval() != null) {
                for (String file : snap.getForRemoval()) {
                    if (filesUpdatedToStage.containsKey(file)) {
                        filesUpdatedToStage.remove(file);
                    }
                }
            } else {
                fileMapped.putAll(parentCommit.fileMapped);
            }
        }
    }
    /** Provides the timestamp information for the commit. */
    private void getTimeStamp() {
        Date toReturn = null;
        String date = "Wed Dec 01 13:00:00 1970 -0800";
        SimpleDateFormat formatter = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        try {
            toReturn = formatter.parse(date);
        } catch (ParseException p) {
            System.out.println(p.getMessage());
        }
        time = toReturn;
    }

    /** Checks if the commit contains the file.
     * @param file the file in question.
     * @return boolean that is true if yes and false if no. */
    boolean contains(String file) {
        return ((filesUpdatedToStage != null)
                && (filesUpdatedToStage.containsKey(file)));
    }

    /** Creates a commit object of the stage with the message provided.
     * @param snap the stage picture.
     * @param msg the commit message. */
    public Commit(Stage snap, String msg) {
        this(snap);

        message = msg;
        if (snap != null) {
            String filesClearing = snap.getStagedToClear().toString();
            String parentId = getParentCommit().getSHA();
            sHA = Utils.sha1(filesClearing, parentId,
                    getMessage(), getTime().toString());
        } else {
            sHA = Utils.sha1(getTime().toString(), getMessage());
        }
        if (snap != null) {
            for (String fileName : snap.getStaged()) {
                File f = new File(fileName);
                String id2 = Utils.sha1(Utils.readContentsAsString(f));
                if (fileMapped.containsKey(fileName)) {
                    String id1 = fileMapped.get(fileName);
                    if (!id1.equals(id2)) {
                        filesUpdatedToStage.put(fileName, sHA);
                        fileMapped.replace(fileName, id2);
                    }
                } else {
                    filesUpdatedToStage.put(fileName, sHA);
                    fileMapped.put(fileName, id2);
                }
            }
        }
        boolean unchanged = true;
        if (!unchangedCommit()) {
            unchanged = false;
        }
        if (unchanged) {
            System.out.println("No changes added to the commit.");
        } else {
            commitDir = ".gitlet/objects/" + getSHA() + File.separator;
            File dir = new File(commitDir);
            dir.mkdirs();
            if (filesUpdatedToStage.isEmpty()) {
                return;
            }
            for (String name : filesUpdatedToStage.keySet()) {
                Path p = Paths.get(commitDir
                        + fileMapped.get(name) + File.separator + name);
                try {
                    Files.createDirectories(p.getParent());
                    Files.createFile(p);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                Utils.writeContents(p.toFile(),
                        Utils.readContentsAsString(new File(name)));
                filesUpdatedToStage.put(name, commitDir);
            }
        }
    }
    /** Checks whether or not the current commit is the.
     * same as the parent commit.
     * @return a boolean indicating whether or not there has been a
     * change between commits. */
    private boolean unchangedCommit() {
        if (parentCommit == null) {
            return false;
        }
        Set prevFiles = parentCommit.getFileUpdatedToStage();
        Set curFiles = getFileUpdatedToStage();
        if (curFiles.size() != prevFiles.size()) {
            return false;
        }
        for (String name : filesUpdatedToStage.keySet()) {
            if (prevFiles.contains(name)) {
                String curSHA1 = fileMapped.get(name);
                String prevSHA1 = parentCommit.getFileMapped().get(name);
                if (!curSHA1.equals(prevSHA1)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /** For merges, commits the stage with the given message and parents.
     * @param snap the stage picture.
     * @param messages the message.
     * @param parent1 the first parent.
     * @param parent2 the second parent. */
    public Commit(Stage snap, String messages, String parent1, String parent2) {
        this(snap, messages);
        hasMerged = true;
        mergeParents = "Merge: " + parent1 + " " + parent2;
    }
    /** File is checked out.
     * @param file the file. */
    void checkout(String file) {
        File filer = new File(file);
        try {
            Utils.writeContents(filer, Utils.readContentsAsString(
                    new File(commitDir + fileMapped.get(file)
                            + File.separator + file)));
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
    }
    /** The current commit is checked out. */
    void checkout() {
        Set<String> helper = filesUpdatedToStage.keySet();
        for (String item : helper) {
            checkout(item);
        }
        File gitlet = new File(commitDir);
        String full = gitlet.getAbsolutePath();
        String path = full.substring(0, full.length() - commitDir.length() + 1);
        File newPath = new File(path);
        for (File file: newPath.listFiles()) {
            if (!helper.contains(file.getName())) {
                Utils.restrictedDelete(file);
            }
        }
    }

    /** Checks whether or not the file has been changed between
     * the current commit and the head commit.
     * @param head the head commit.
     * @param fileName the file in question.
     * @return a boolean that is true if no change and false if changed. */
    boolean altered(Commit head, String fileName) {
        if (fileMapped.containsKey(fileName)) {
            boolean check1 = head.getFileMapped().containsKey(fileName);
            boolean check2 = head.getFileMapped().get(fileName).equals(
                    this.fileMapped.get(fileName));
            return check1 && check2;
        }
        if (head.getFileMapped().containsKey(fileName)) {
            boolean check1 = getFileMapped().containsKey(fileName);
            boolean check2 = head.getFileMapped().get(fileName).equals(
                    this.fileMapped.get(fileName));
            return check1 && check2;
        }
        return Utils.readContentsAsString(this.getFile(fileName)).equals(
                Utils.readContentsAsString(head.getFile(fileName)));
    }
    /** For syntactical purposes. */
    @Override
    public String toString() {
        StringBuilder myString = new StringBuilder();
        myString.append("=== \n" + "commit " + sHA + " \n");
        if (hasMerged) {
            myString.append(mergeParents + "\n");
        }
        String timeStamp = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z").format(time);
        myString.append("Date: " + timeStamp + " \n" + message);
        return myString.toString();
    }

}

