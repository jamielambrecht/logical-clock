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
import java.util.Collections;

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

        int mode = 0;
        
        if (args.length == 2) {
            filename = args[1];
            switch (args[0]) {
            case "-c":
                mode = 1;
                break;
            case "-v":
                mode = 2;
                break;    
            default:
                System.out.println("Unrecognized option: " + args[0] + ".");
                System.exit(1);                
            }
            
        } else {
            System.out.println("Expected 2 arguments but received " + args.length + ".");
            System.exit(1);
        }

        String [][] data = parseData(filename);

        int N = data.length;
        int M = data[0].length;

        if (mode == 1) {
            int [][] result = calculate(data);

            // Print output of calculate
            for (int i = 0; i < result.length; i++) {
                for (int j = 0; j < result[0].length; j++) {  
                    System.out.print(result[i][j] + "\t");
                }
                System.out.print("\n");
            }
        } else if (mode == 2) {

            // Convert data from String to int
            int [][] verifyData = new int[N][M];

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) { 
                    try {
                        verifyData[i][j] = Integer.parseInt(data[i][j]);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing integers from input.");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }

            String [][] result = verify(verifyData);

            // Print output of verify
            for (int i = 0; i < result.length; i++) {
                for (int j = 0; j < result[0].length; j++) {  
                    System.out.print(result[i][j] + "\t");
                }
                System.out.print("\n");
            }

        }

        // verify(data);
    }

    private static String [][] verify(int [][] data) {

        // We can eliminate some cases immediately
        // If all elements in a row are not in ascending order, the input is incorrect
        // If at any time, there are more receive events than potential send events, the input is incorrect

        // Determine size of data and create matrix for output/result          
        int N = data.length;
        int M = data[0].length;
        String [][] output = new String [N][M];
        for (int i = 0; i < N; i++) {
            Arrays.fill(output[i], " ");
        }


        // Represent the current expected logical clock value for each row
        int [] expectedLC = new int [N];
        Arrays.fill(expectedLC, 1);

        // Keep a set of non-sequential LC values encountered
        Map<Integer, Integer> receive_LCs = new HashMap<Integer, Integer>();

        // Create and initialize row index variable
        int row = 0;

        // Store the indexes of completed rows in a set
        Set<Integer> completed_rows = new HashSet<Integer>();

        // Store current iterator for given row
        int [] current_index = new int [N];

        // Store index for first undetermined element in given row
        int [] first_undetermined = new int [N];
        Arrays.fill(first_undetermined, -1);

        // Store index for first undetermined element in given row
        int [] last_determined_element = new int [N];
        Arrays.fill(first_undetermined, 0);

        // Keep track of the maximum of sequential LC values (exclude receives)
        int [] max_LC = new int [N];
        int max_LC_ever = 0;

        // The current sequential letter used to label internal events
        char letter = 'a';

        // Sequential enumeration for labelling send/receive events
        int current_send = 0;

        // Create and initialize the error indicator
        Error error = Error.NONE;

        int pending_receives = 0;

        boolean have_seen_all_rows = false;

        // Continue until all process have been completed
        while (completed_rows.size() < N) {
            
            if (!have_seen_all_rows) {
                if (row == N - 1) {
                    have_seen_all_rows = true;
                }
            }

            if (!completed_rows.contains(row)) {
                // determine our current position for a particular row
                int column = current_index[row];

                // get the current element for a particular row
                int element;

                if (column < M) {
                    element = data[row][column];
                } else {
                    // If we have advanced beyond the last index, then we have finished calculating that row
                    completed_rows.add(row);
                    // We are finished with this element and row, recheck whether all rows are finished and start over if not
                    continue;
                }

                if (output[row][column] == null) {
                    while (current_index[row] < M) {
                        output[row][column] = null;
                        current_index[row] += 1;
                    }
                    completed_rows.add(row);
                    continue;
                }

                // If we have a suspected internal or send event
                if (output[row][column].equals("s")) {

                    //  We cannot say whether an event is internal or send until we have determined that it cannot be matched with a receive
                    if (receive_LCs.containsKey(element + 1) && receive_LCs.get(element + 1) == 0) {

                        // We have found a corresponding receive
                        output[row][column] += Integer.toString(++current_send);

                        receive_LCs.put(element + 1, current_send);

                        current_index[row] += 1;

                        first_undetermined[row] = current_index[row];
                        last_determined_element[row] = element;
                    
                    } else {
                        // If it is not greater than the smallest receive LC or there are no unresolved LCs
                        
                        // if (receive_LCs.isEmpty() || element >= Collections.min(receive_LCs.keySet()) - 1) {
                            //
                        if (receive_LCs.isEmpty() || (element >= Collections.max(receive_LCs.values()) - 1 && Collections.max(receive_LCs.values()) - 1 > 0) ) {

                            if (element != last_determined_element[row] + 1) {
                                System.out.println("INCORRECT");
                                System.exit(1);
                            }

                            // We can label it as an internal event
                            output[row][column] = Character.toString(letter++);
    
                            current_index[row] += 1;

                            first_undetermined[row] = current_index[row];
                            last_determined_element[row] = element;
//&& element == ((int)letter) - 96 &&
                        } else if (pending_receives >= N - completed_rows.size() - 1 && have_seen_all_rows) {

                            if (element != last_determined_element[row] + 1) {
                                    System.out.println("INCORRECT");
                                    System.exit(1);
                            }

                            // We can label it as an internal event
                            output[row][column] = Character.toString(letter++);
    
                            current_index[row] += 1;

                            first_undetermined[row] = current_index[row];
                            last_determined_element[row] = element;
                        } else {

                            row = (row + 1) % N;
                        }
                        // if we cannnot determine a send or internal event, try the next row

                    }

                } else if (output[row][column].equals("r")) {

                    if (element != last_determined_element[row] + 1) {
                        if (pending_receives >= N - completed_rows.size() && have_seen_all_rows && element > max_LC_ever + 1 && element == Collections.min(receive_LCs.keySet())) {
                            System.out.println("INCORRECT");
                            System.exit(1);
                        }
                    }

                    if (receive_LCs.get(element) != 0) {   

                        output[row][column] += receive_LCs.get(element);

                        expectedLC[row] = element + 1;

                        current_index[row]++;
                        first_undetermined[row] = current_index[row];
                        last_determined_element[row] = element;

                        receive_LCs.remove(element);

                        pending_receives -= 1;

                    } else {
                        row = (row + 1) % N;
                    }
                    
                } else {

                    // If the current element in a row matches a sequential LC value
                    if (expectedLC[row] == element) {

                        // it is either an internal or send event
                        output[row][column] = Character.toString('s');

                        // Keep track of the largest sequential event number seen for matching send events to recieves
                        max_LC[row] = Math.max(max_LC[row], element);
                        max_LC_ever = Math.max(max_LC_ever, element);

                        if (first_undetermined[row] < 0) {
                            first_undetermined[row] = current_index[row];
                        }

                        if (current_index[row] < M - 1) {
                            current_index[row] += 1;

                            expectedLC[row] += 1;
                        } else {
                            current_index[row] = first_undetermined[row];
                        }

                    } else if (element != 0) {
                        // it is a receive event
                        output[row][column] = Character.toString('r');

                        // Keep track of the LC values of receive events
                        receive_LCs.put(element, 0);

                        // Send index pointer back to the first undetermined array position
                        if (first_undetermined[row] == -1) {
                            current_index[row] = 0;
                        } else {
                            current_index[row] = first_undetermined[row];
                        }
                        pending_receives += 1;

                        // Reset the first undetermined index until the next string of s is begun
                        first_undetermined[row] = -1;

                    } else {
                        output[row][column] = null;
                        if (current_index[row] != first_undetermined[row]) {
                            current_index[row] = first_undetermined[row];
                        } else {
                            current_index[row] += 1;
                        }
                        
                    }
                }
            } else {
                row = (row + 1) % N;
            }
        }

        // Error handling by enumerated states
        switch (error) {
            case NONE:
                break;
            case BAD_GENERAL_INPUT:
                System.out.println("ERROR: Invalid input.");
                System.exit(1);
                break;
            default: 

            }

        return output;
    }

    private static int [][] calculate(String [][] data) {

        // Determine size of data and create matrix for output/result  
        int N = data.length;
        int M = data[0].length;
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
        Map<Integer, Integer> previous_sends = new HashMap<Integer, Integer>();

        // Store send number expected by receive event by row 
        int [] waiting_for = new int [N];

        // Store current iterator for given row
        int [] current_index = new int [N];

        // Create and initialize row index variable
        int row = 0;

        // Create and initialize the error indicator
        Error error = Error.NONE;

        // Continue until all process have been completed
        while (completed_rows.size() < N) {

            // Continue from saved index for each row/process
            int column = current_index[row];

            // Skip completed rows/processes
            if (!completed_rows.contains(row)) {

                // If this process is not already determined to be a receive waiting for a send event
                if (waiting_for[row] == 0) {

                    // Read the element from the input data matrix
                    String element = new String();

                    if (column < M) {
                        element = data[row][column];
                    } else {
                        // If we have advanced beyond the last index, then we have finished calculating that row
                        completed_rows.add(row);
                        // We are finished with this element and row, recheck whether all rows are finished and start over if not
                        continue;
                    }

                    if (element == null) {

                        // Write "0" to any null spaces
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
                                    previous_sends.put(digit, LC[row]);

                                    // Write the current LC to output and increment LC
                                    output[row][column] = LC[row];

                                    LC[row] += 1;

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
                        
                        // if (LC[row] > sends.get(receive_number)) {
                        //     output[row][column] = LC[row] + 1;
                        //     LC[row] = output[row][column] + 1;
                        // } else if (LC[row] == sends.get(receive_number)) {
                        //     output[row][column] = sends.get(receive_number) + 1;
                        //     LC[row] = output[row][column] + 1;
                        // } else {
                        //     output[row][column] = sends.get(receive_number) + 1;
                        //     LC[row] = output[row][column];
                        // }
                        output[row][column] = Math.max(LC[row], sends.get(receive_number) + 1);
                        
                        LC[row] = output[row][column] + 1;

                        // Increment the index to next element in row.
                        current_index[row] += 1;

                        // We are not longer waiting for the send
                        waiting_for[row] = 0;

                        // TODO this will have to be fixed for broadcasts
                        
                        sends.remove(receive_number);

                    } else if (previous_sends.containsKey(receive_number)) {

                        // if (LC[row] > previous_sends.get(receive_number)) {
                        //     output[row][column] = LC[row] + 1;
                        //     LC[row] = output[row][column] + 2;
                        // } else if (LC[row] == previous_sends.get(receive_number)) {
                        //     output[row][column] = previous_sends.get(receive_number) + 1;
                        //     LC[row] = output[row][column] + 2;
                        // } else {
                        //     output[row][column] = previous_sends.get(receive_number) + 1;
                        //     LC[row] = output[row][column] + 2;
                        // }
                        output[row][column] = Math.max(LC[row], previous_sends.get(receive_number) + 1);
                        
                        LC[row] = output[row][column] + 1;

                        // Increment the index to next element in row.
                        current_index[row] += 1;

                        // We are not longer waiting for the send
                        waiting_for[row] = 0;


                    } else {
                        // Go to next row
                        row = (row + 1) % N;
                    }
                }
            } else {
                // Go to next row
                row = (row + 1) % N;
            }

            // Error handling by enumerated states
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
            System.exit(1);
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