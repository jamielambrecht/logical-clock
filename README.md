# 474-Project-1
Project 1: Servers

Group members:

Jamie Lambrecht mjlambrecht@csu.fullerton.edu

## USAGE

This program contains a file called Main.java, with the source code for the program, and a file called Main.class, which is obtianed by compiling the code using the javac command. It was compiled and tested using OpenJDK 11 on WSL Debian Linux, although it should be compatible with most if not all Java versions because it only uses very basic and fundamental Java libraries. If the Java SDK is installed on the computer, the program can be run using the following generalized syntax: java Main.java <mode option> <input filename>. The <mode option> must be either -c for "calculate" or -v for "verify". The program itself will only function with exactly 2 arguments, one for the option, and another for the input filename. 

The "-c" option will run the "calculate" algorithm on the specified input file, while the "-v" option will run the "verify" algorithm on the specified input file. 1 test case entitled "input_calculate.txt" is included for use with "calculate" algorithm and 3 test cases entitled "input_verify*.txt" (where '*' is substituted with the regular expression [1-3]). Any other input files can be specified in the command line arguments, but must be properly formatted. 

The format for the "calculate" algorithm is a series of rows in a text file consisting of event specifiers where [a-q|t-z|A-Q|T-Z] denote internal events, s[1-9] and r[1-9] send and represent receive events, respectively, as defined by the Lamport Logical Clock algorithm, seperated by ASCII whitespace (preferably tab characters). The format for the "verify" algorithm is a series of rows in a text file consisting of strictly increasing integers which can be translated to the aforementioned event specifiers. The verify algorithm will determine incorrect input based on Logical Clock sequencing of events, but it is not guaranteed that all invalid input will be determined by all algorithms. The calculate has handling for several obvious syntax errors, but it is generally expected that even logically incorrect input is at least syntactically correct.