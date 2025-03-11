package os.portfolio3.brainstorm;

import os.portfolio3.zfstransactions.ZFSTransaction;
import os.portfolio3.zfstransactions.ZFSUtil;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author Fabian Sponholz (1561546)
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            repl();
        }
        else {
            StringBuilder command = new StringBuilder();
            for (String arg : args) {
                command.append(arg).append(" ");
            }
            evaluate(command.toString(), new Scanner(System.in));
        }
        System.out.println("Goodbye!");
    }

    public static void repl() {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("Enter commands (type 'exit' to quit):");
        while (true) {
            System.out.print("> ");
            try {
                input = scanner.nextLine();
            } catch (NoSuchElementException e) {
                break;
            }


            if ("exit".equalsIgnoreCase(input)) {
                break;
            }

            evaluate(input, scanner);

        }
        scanner.close();
    }

    public static void evaluate(String command, Scanner scanner) {
        String[] commandParts = command.split(" ");
        String name;
        switch (commandParts[0].toLowerCase()) {
            case "hello":
                System.out.println("Hello! How can I help you?");
                break;
            case "list":
                listIdeas();
                break;
            case "add":
                if (commandParts.length > 1) {
                    name = commandParts[1];
                }
                else {
                    System.out.println("Enter idea name: ");
                    name = scanner.nextLine();
                }

                System.out.println("Enter idea: ");
                String idea = scanner.nextLine();
                addIdea(name, idea);
                break;
            case "remove":
                System.out.println("Enter idea name: ");
                if (commandParts.length > 1) {
                    name = commandParts[1];
                }
                else {
                    System.out.println("Enter idea name: ");
                    name = scanner.nextLine();
                }
                removeIdea(name);
                break;
            case "show":
                System.out.println("Enter idea name: ");
                if (commandParts.length > 1) {
                    name = commandParts[1];
                }
                else {
                    System.out.println("Enter idea name: ");
                    name = scanner.nextLine();
                }
                showIdea(name);
                break;
            case "comment":
                System.out.println("Enter idea name: ");
                if (commandParts.length > 1) {
                    name = commandParts[1];
                }
                else {
                    System.out.println("Enter idea name: ");
                    name = scanner.nextLine();
                }
                addComment(name, scanner);
                break;
            case "help":
                System.out.println("Available commands: hello, list, add, remove, show, comment, help, exit");
                break;
            default:
                System.out.println("Unknown command: " + command);
                break;
        }
    }

    public static void listIdeas() {
        System.out.println("Listing ideas...");
        List<String> ideas = ZFSUtil.getFileList();
        for (String idea : ideas) {
            System.out.println(idea);
        }
    }

    public static void addIdea(String name, String idea) {
        System.out.println("Adding idea: " + name);
        Idea ideaObject = new Idea(idea);
        boolean success;
        try {
            success = ZFSTransaction.open().writeFile(name, ideaObject.toJson()).commit();
        }
        catch (IOException | InterruptedException e) {
            success = false;
        }

        if (success) {
            System.out.println("Idea added successfully!");
        } else {
            System.out.println("Failed to add idea!");
        }
    }

    public static void removeIdea(String name) {
        System.out.println("Removing idea: " + name);
        try {
            ZFSTransaction.open()
                .deleteFile(name)
                .commit();
        } catch (IOException e) {
            System.out.println("Failed to remove idea!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showIdea(String name) {
        System.out.println("Showing idea: " + name);
        try {
            ZFSTransaction transaction = ZFSTransaction.open();
            Idea ideaObject = Idea.fromJson(transaction.readFile(name));
            printIdea(ideaObject);
            transaction.close();
        } catch (IOException e) {
            System.out.println("Failed to show idea!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addComment(String name, Scanner scanner) {
        try{
            ZFSTransaction transaction = ZFSTransaction.open();
            Idea ideaObject = Idea.fromJson(transaction.readFile(name));
            printIdea(ideaObject);
            System.out.print("Enter comment: ");
            ideaObject.getComments().add(scanner.nextLine());

            if (!transaction.writeFile(name, ideaObject.toJson()).commit())
                System.out.println("Transaction failed!");

            else
                System.out.println("Comment added successfully!");
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printIdea(Idea ideaObject) {
        System.out.println("IDEA:\n=====\n" + ideaObject.getIdea());
        System.out.println("\nCOMMENTS:\n========");
        for (String comment : ideaObject.getComments()) {
            System.out.println(comment);
        }

    }
}
