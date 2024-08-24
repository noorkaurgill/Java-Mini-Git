package gitlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;


/** Driver class for Gitlet, the tiny [amazing] version-control system.
 * @author Noor Gill */
public class Main {

    /** First portion of the command. */
    private static String argument2;
    /** Second portion of the command. */
    private static String argument3;

    /** Retrieves the initial system if there isn't one in
     * the current directory.
     * @return the initialized system. */
    private static Tree init() {
        File directory = new File(".gitlet" + File.separator);
        if (!directory.exists()) {
            directory.mkdirs();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return null;
        }
        return Tree.init();
    }

    /** Initializes the repository with a tree data structure.
     * @return the repository. */
    private static Tree developRepo() {
        Tree repo = null;
        File file1 = new File(".gitlet" + File.separator + "path");
        if (file1.exists()) {
            try {
                FileInputStream inner = new FileInputStream(file1);
                ObjectInputStream outer = new ObjectInputStream(inner);
                repo = (Tree) outer.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return repo;
    }
    /** Sets up each of the arguments as Git commands.
     * @param command the length of the program input.
     * @param args the arguments for method call. */
    private static void argSetUp(int command, String...args) {
        if (command >= 3) {
            argument3 = args[2];
        }
        if (command >= 2) {
            argument2 = args[1];
        }
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ...
     *  Performs the corresponding and adequate method with the argument. */
    public static void main(String... args) {
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
        }
        String argument1 = args[0];
        int command = args.length;
        argSetUp(command, args);
        Tree repo = developRepo();
        try {
            switch (argument1) {
            case "init":
                repo = init();
                break;
            case "add":
                repo.add(argument2);
                break;
            case "commit":
                repo.commit(argument2);
                break;
            case "log":
                repo.log();
                break;
            case "checkout":
                checkout(repo, args);
                break;
            case "global-log":
                repo.globalLog();
                break;
            case "rm":
                repo.remove(argument2);
                break;
            case "find":
                repo.find(argument2);
                break;
            case "status":
                repo.status();
                break;
            case "branch":
                repo.addBranch(argument2);
                break;
            case "rm-branch":
                repo.removeBranch(argument2);
                break;
            case "merge":
                repo.merge(argument2);
                break;
            case "reset":
                repo.reset(argument2);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Incorrect operands.");
        } catch (NullPointerException f) {
            System.out.println("Not in an initialized Gitlet directory.");
        }
        serialization(repo);
    }

    /** Performs serialization in saving the files as needed.
     * @param repo the repository. */
    private static void serialization(Tree repo) {
        File file2 = new File(".gitlet" + File.separator + "path");
        if (repo == null) {
            return;
        } else {
            try {
                FileOutputStream outer = new FileOutputStream(file2);
                ObjectOutputStream inner = new ObjectOutputStream(outer);
                inner.writeObject(repo);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /** Checks out the arguments in the repository.
     * @param repo repository.
     * @param args arguments. */
    private static void checkout(Tree repo, String...args) {
        int command = args.length;
        try {
            if (command == 4) {
                case1(repo, args);
            } else if (command == 3) {
                case2(repo, args);
            } else if (command == 2) {
                case3(repo, args);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Incorrect Operands.");
        }
    }

    /** The first case of checkout in which 4 arguments are used.
     * @param repo the repository.
     * @param args the commanding arguments.
     */
    private static void case1(Tree repo, String...args) {
        if (args[2].equals("--")) {
            argument2 = args[1];
            argument3 = args[3];
            repo.checkout3(argument2, argument3);
        }
    }

    /** The second case of checkout in which 3 arguments are used.
     * @param repo the repository.
     * @param args the commanding arguments.
     */
    private static void case2(Tree repo, String...args) {
        if (args[1].equals("--")) {
            argument2 = args[2];
            repo.checkout2(argument2);
        }
    }

    /** The first case of checkout in which 2 arguments are used.
     * @param repo the repository.
     * @param args the commanding arguments.
     */
    private static void case3(Tree repo, String...args) {
        argument2 = args[1];
        repo.checkout1(argument2);
    }
}
