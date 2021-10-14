import java.io.*;
import java.lang.Math;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Main {

    public enum Error {
        NONE,
        BAD_RECEIVE_INPUT,
        BAD_SEND_INPUT,
        DUPLICATE_SEND_INPUT,
        BAD_GENERAL_INPUT   
    }

    public static void main(String [] args) {
        String filename = new String();
        
        if (args.length == 0) {
            filename = "./input.txt";
        } else if (args.length == 1) {
            filename = args[0];
        } else {
            System.out.println("Expected 1 argument but received " + args.length);
            System.exit(1);
        }

        String [][] data = parseData(filename);

        int [][] result = calculate(data);

        // Print output of verify
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {  
                System.out.print(result[i][j] + "\t");
            }
            System.out.print("\n");
        }

        // verify(data);
    }

    private static int [][] calculate(String [][] data) {

        int N = data.length;
        int M = data[0].length;

        // Create matrix for output/result
        int [][] output = new int [N][M];

        // // Print input for debugging/testing purposes
        // for (int i = 0; i < N; i++) {
        //     for (int j = 0; j < M; j++) {  
        //         System.out.print(data[i][j] + "\t");
        //     }
        //     System.out.print("\n");
        // }

        // Begin the logical clock for each process
        int [] LC = new int [N];
        Arrays.fill(LC, 1);

        // Store the indexes of completed rows in a set
        Set<Integer> completed_rows = new HashSet<Integer>();

        // Store the sequence numbers for send events
        Map<Integer, Integer> sends = new HashMap<Integer, Integer>();

        // Store send number expected by receive event by row 
        int [] waiting_for = new int [N];

        // Store current iterator for given row
        int [] current_index = new int [N];

        int row = 0;

        Error error = Error.NONE;

        // Continue until all process have been completed
        while (completed_rows.size() < N) {

            // Continue from saved index for each row/process
            int column = current_index[row];

            // Skip completed rows/processes
            if (!completed_rows.contains(row)) {

                // If this process is not already determined to be a receive waiting for a send event
                if (waiting_for[row] == 0) {

                    String element = new String();

                    // Read the element from the input data matrix
                    if (column < M) {
                        element = data[row][column];
                    } else {
                        completed_rows.add(row);
                        continue;
                    }

                    if (element == null) {

                        output[row][column] = 0;
                        current_index[row] += 1;

                    // If the element denotes a receive event
                    } else if (element.charAt(0) == 'r') {

                        // Ensure there is a second field in the receive event identifier
                        if (element.length() == 2) {

                            // Ensure the second field in the receive event identifier is a digit
                            if (Character.isDigit(element.charAt(1))) {
                                Character digit_field = element.charAt(1);
                                waiting_for[row] = Character.getNumericValue(digit_field);

                            } else {
                                // If r is not followed by a digit...
                                error = Error.BAD_RECEIVE_INPUT;
                            }

                        } else {
                            // If r is not followed by anything...
                            error = Error.BAD_RECEIVE_INPUT;
                        }

                    } else if (element.charAt(0) == 's') {

                        // Ensure there is a second field in the send event identifier
                        if (element.length() == 2) {

                            // Ensure the second field in the receive send identifier is a digit
                            if (Character.isDigit(element.charAt(1))) {

                                // convert digit character to int
                                Character digit_field = element.charAt(1);
                                int digit = Character.getNumericValue(digit_field);

                                // Prevent duplicate send events
                                if (!sends.containsKey(digit)) {

                                    // Add digit to outgoing sends
                                    sends.put(digit, LC[row]);

                                    // Write the current LC to output and increment LC
                                    output[row][column] = LC[row]++;

                                    // Increment the index to next element in row.
                                    current_index[row] += 1;

                                } else {
                                    error = Error.DUPLICATE_SEND_INPUT;
                                }

                            } else {
                                // If r is not followed by a digit...
                                error = Error.BAD_SEND_INPUT;
                            }

                        } else {
                            // If r is not followed by anything...
                            error = Error.BAD_SEND_INPUT;
                        }

                    } else if (element.length() == 1) {

                        // Ensure that character is a letter
                        if (Character.isLetter(element.charAt(0))) {

                            // Write the current LC to output and increment LC
                            output[row][column] = LC[row]++;

                            // Increment the index to next element in row.
                            current_index[row] += 1;

                        } else {
                            error = Error.BAD_GENERAL_INPUT;
                        }

                    } else {
                        error = Error.BAD_GENERAL_INPUT;
                    }


                } else {
                    int receive_number = waiting_for[row];

                    // If a send for the receive has happened
                    if (sends.containsKey(receive_number)) {
                        
                        // Write the current LC to output and increment LC
                        
                        output[row][column] = Math.max(LC[row], sends.get(receive_number)) + 1;
                        
                        LC[row] = output[row][column] + 1;

                        // Increment the index to next element in row.
                        current_index[row] += 1;

                        // We are not longer waiting for the send
                        waiting_for[row] = 0;

                        // TODO this will have to be fixed for broadcasts
                        sends.remove(receive_number);

                    } else {
                        // Go to next row
                        row = (row + 1) % N;
                    }
                }
            } else {
                // Go to next row
                row = (row + 1) % N;
            }

            switch (error) {
            case NONE:
                break;
            case BAD_RECEIVE_INPUT:
                System.out.println("ERROR: Bad input for receive event.");
                System.exit(1);
                break;
            case BAD_SEND_INPUT:
                System.out.println("ERROR: Bad input for send event.");
                System.exit(1);
                break;
            case DUPLICATE_SEND_INPUT:
                System.out.println("ERROR: Duplicate send event.");
                System.exit(1);
                break;
            case BAD_GENERAL_INPUT:
                System.out.println("ERROR: Invalid input.");
                System.exit(1);
                break;
            default: 

            }
        }
        
        return output;

    }

    // private static void verify(String [][] data) {
    // }

    private static String [][] parseData(String filename) {
        // First, we will parse out every word by lines from the input file
        ArrayList <String []> lines = new ArrayList<String []>();

        // Used to store the greatest number of events for a process
        int M = 0;

        // Begin reading from the file
        File file = new File(filename);

        try {
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()) {
                String [] line = reader.nextLine().split("\\s+");

                if (line.length != 0) {
                    lines.add(line);
                    // Each word in a line of the input file contains data for an event
                    M = Math.max(line.length, M);
                }
            }

            reader.close();

        } catch (FileNotFoundException e) {
            System.out.println("Error reading from the input file.");
            e.printStackTrace();
        }

        // The number of lines in the input file is the number of processes
        int N = lines.size();

        // We will use a primitive 2-d array to pass the data to the primary procedure 
        String [][] data = new String [N][M];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                String [] row = lines.get(i);
                if (j < row.length) {
                    data[i][j] = row[j];
                } else {
                    data[i][j] = null;
                }
            }
        }
        
        return data;
    }

}