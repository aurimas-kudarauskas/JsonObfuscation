
/**
 * @author Aurimas Kudarauskas
 */
import java.io.File;                         // Import the File class
import java.io.FileNotFoundException;        // Import this class to handle errors
import java.util.Scanner;                    // Import the Scanner class to read text files
import java.io.FileWriter;                   // Import the Writter class to perfrom write on text files
import java.io.IOException;                  // Import the IOException class to handle errors
import java.util.ArrayList;                  // Import ArrayList to store all strings inside line
import java.util.List;                       // Import List to store all strings inside line
import org.apache.commons.text.StringEscapeUtils; //Import the StringEscapeUtils to convert string into unicode escape sequence"
import org.apache.commons.lang3.StringUtils; //Import the StringUtils to extract strings inside json text separated with ""
import java.util.HashMap;                    // import the HashMap class to store obfuscator map

public class JsonObfuscator_Main {

    /**
     * Store input file that will be obfuscated
     */
    private static String InputFileName;
    /**
     * File name where map of strings will be dumped
     */
    private static String MapFileName;
    /**
     * File name where obfuscated text will be saved
     */
    private static String OutputFileName;

    /**
     * File object to store handle of current input file
     */
    private static File InputFileObject;
    /**
     * File object to store handle of current output file
     */
    private static File OutputFileObject;
    /**
     * File reader handle to read from current input file
     */
    private static Scanner InputFileReader;
    /**
     * File writer handle to write into current output file
     */
    private static FileWriter OutputFileWriter;

    /**
     * Variable to store obfusctation map values
     */
    private static HashMap<String, String> ObfuscationMap;

    /**
     * Method for setting default values of properties
     */
    private static void InitVariables() {
        InputFileName = "";
        OutputFileName = "";
        MapFileName = "mapping.txt";
        ObfuscationMap = new HashMap<>();
    }

    /**
     * Write line to the output file that is associated with OutputFileWriter
     *
     * @param line the data that will be written into the file
     */
    private static void WriteLineToOutputFile(String line) {
        try {
            OutputFileWriter.write(line + "\n");
            OutputFileWriter.flush();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to output file " + OutputFileObject.getName());
            System.exit(1);
        }
    }

    /**
     * Opens OutputFileObject handle with provided file name and initializes
     * OutputFileWriter;
     *
     * @param outPutFileName File name of the file that will be associated with
     * OutputFileObject and OutputFileWriter
     */
    private static void InitOutputFile(String outPutFileName) {
        try {
            OutputFileObject = new File(outPutFileName);
            OutputFileWriter = new FileWriter(OutputFileObject);
        } catch (IOException e) {
            System.out.println("An error occurred while opening  output file " + outPutFileName);
            System.exit(1);
        }
    }

    /**
     * Initializes file handles for Json input file and obfuscated Json output
     * file
     */
    private static void InitFiles() {
        try {
            InputFileObject = new File(InputFileName);
            InputFileReader = new Scanner(InputFileObject);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while opening input file. File " + InputFileName + " could not be faund.");
            System.exit(1);
        }
        if (!OutputFileName.isBlank()) {
            InitOutputFile(OutputFileName);
        }
    }

    /**
     * Outputs obfuscated line to output file. If output file not selected
     * prints to std
     *
     * @param line - data that will be printed to std or written to file
     */
    private static void OutputLine(String line) {
        if (OutputFileName.isBlank()) {
            System.out.println(line);
        } else {
            WriteLineToOutputFile(line);
        }
    }

    /**
     * Prints values map to file
     */
    private static void PrintObfuscationMap() {
        InitOutputFile(MapFileName);
        ObfuscationMap.keySet().forEach(key -> {
            WriteLineToOutputFile(String.format("%-30s -> %s", key, ObfuscationMap.get(key)));
        });

    }

    /**
     * Search for string in json file line. If string has been found returns the
     * string
     *
     * @param line - raw data from json input that will be seached for ""
     * terminated string
     */
    private static String ExtractString(String line) {
        String subString = StringUtils.substringBetween(line, "\"");

        if ((subString != null) && (!subString.isBlank())) {
            while (subString.endsWith("\\")) // Handel usage of " inside strings of json itself
            {
                subString += "\"" + StringUtils.substringBetween(line.substring(line.indexOf(subString) + subString.length()), "\"");
            }
        }
        return subString;
    }

    /**
     * Search for all strings in provided line. If no strings are found empty
     * list will be returned.
     *
     * @param line - raw data from json input that will be obfuscated
     */
    private static List<String> ExtractAllStrings(String line) {
        List<String> strList = new ArrayList<>();
        String str = ExtractString(line);
        while (str != null) {
            strList.add(str);
            str = ExtractString(line.substring(line.indexOf(str) + str.length() + "\"".length()));
        }

        return strList;
    }

    /**
     * Replace chars of the string with unicode escape sequences
     *
     * @param str - string to be obfuscated into unicode escape sequences
     */
    private static String ObfuscateString(String str) {
        String ObfuscatedString = "";
        for (char c : str.toCharArray()) {
            ObfuscatedString += "\\u" + String.format("%04x", (int) c);
        }
        return ObfuscatedString;
        //return StringEscapeUtils.escapeJava(str);
    }

    /**
     * Search for string in json file line. If string has been found obfuscation
     * is performed
     *
     * @param line - raw data from json input that will be obfuscated
     */
    private static void ObfuscateLine(String line) {
        String obfuscated;
        List<String> strList = ExtractAllStrings(line);
        for (String str : strList) {
            if (ObfuscationMap.containsKey(str)) {
                obfuscated = ObfuscationMap.get(str);
            } else {
                obfuscated = ObfuscateString(str);
                ObfuscationMap.put(str, obfuscated);
            }
            line = line.replace(str, obfuscated);
        }

        OutputLine(line);
    }

    /**
     * Scans input file line by line and performs obfuscation on single line
     *
     * @param inputFileReader - handle for reading input file
     */
    private static void ObfuscateFile(Scanner inputFileReader) {

        while (inputFileReader.hasNextLine()) {
            ObfuscateLine(inputFileReader.nextLine());
        }

        inputFileReader.close();

    }

    /**
     * Process user inputs
     *
     * @param args the command line arguments
     */
    private static void ParseArguments(String[] args) {

         if (args.length == 0) {
            System.out.println("User must provide input for application. Please see help for instuctions by running application with -h flag");
            System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                case "-O":
                    i++; // Flag should be followed by value
                    if (args[i].startsWith("-")) {
                        System.out.println(" Option flag -o is used in correctly. It should be followed by filename. Eg:-o outpuFile.txt ");
                        System.exit(0);
                    } else {
                        OutputFileName = args[i];
                    }
                    break;

                case "-m":
                case "-M":
                    i++; // Flag should be followed by value
                    if (args[i].startsWith("-")) {
                        System.out.println(" Option flag -m is used in correctly. It should be followed by filename. Eg:-m mapFile.txt ");
                        System.exit(1);
                    } else {
                        MapFileName = args[i];
                    }

                    break;

                case "-h":
                case "-H":
                    System.out.print("This application meant to obfuscate any string in provided json file.\n\r"
                            + "Example of application usage: JsonObfuscator InputJsonFile \n\n\r "
                            + "Additional options for application:\n\r"
                            + "JsonObfuscator InputJsonFile -o outputFileName - specifie filename where obfuscated json should be saved.\n\r"
                            + "JsonObfuscator InputJsonFile -m mapFileName - specifie filename where map of obfuscated strings should be saved \n\r"
                            + "JsonObfuscator -h - print this help text \n\r");
                    System.exit(0);
                    break;

                default:
                    if (InputFileName.isBlank())// First string withough flag is input file name
                    {
                        InputFileName = args[i];
                    } else {
                        System.out.println("Argument \"" + args[i] + "\" not supported. Please see help for instuctions by running application with -h flag");
                        System.exit(0);
                    }
            }
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        InitVariables();
        ParseArguments(args);
        InitFiles();
        ObfuscateFile(InputFileReader);
        PrintObfuscationMap();

        System.exit(0);
    }
}
