import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Generates an HTML file which contains a tag cloud generated based on number
 * of words which the user provides, the input file from which to get the words
 * for the tag cloud, and the output file location requested.
 *
 * @author Anthony Fox and Andrew Mack
 *
 */
public final class TagCloudJava {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudJava() {
    }

    /**
     * Sorts Integer values of Map Pairs in decreasing order.
     */
    private static class CountSort implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {

            int val = o2.getValue().compareTo(o1.getValue());

            // ensures equals consistency
            if (val == 0) {
                val = o2.getKey().compareToIgnoreCase(o1.getKey());
            }
            return val;
        }
    }

    /**
     * Sorts String keys of Map Pairs in alphabetical order.
     */
    private static class WordSort implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {

            int val = o1.getKey().compareToIgnoreCase(o2.getKey());

            // ensures equals consistency
            if (val == 0) {
                val = o1.getValue().compareTo(o2.getValue());
            }
            return val;
        }

    }

    /**
     * String which is used to create the separator set and parse the file
     * correctly.
     */
    private static final String SEPARATORS = " `*\t\n\r,-.!?[];:'/()\"";

    /**
     * The minimum font size in the CSS file.
     */
    private static final int MINIMUM_FONT_SIZE = 11;

    /**
     * The maximum font size in the CSS file.
     */
    private static final int MAXIMUM_FONT_SIZE = 48;

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code HashSet} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            HashSet<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        // initializes variables used to exit the loop early and to track the position
        boolean endEarly = false;
        int endIndex = text.length();
        int i = position;

        // if statement to check if the character found at position
        // is in the separator set
        if (separators.contains(text.charAt(position))) {

            // while loop to run until a character not in the set is found
            while (!endEarly && i < text.length()) {
                if (!(separators.contains(text.charAt(i)))) {
                    // once found the position is recorded and the loop is broken
                    endIndex = i;
                    endEarly = true;
                }
                i++;
            }

        } else {

            // while loop to run until a character in the set is found
            while (!endEarly && i < text.length()) {
                if (separators.contains(text.charAt(i))) {
                    // once found the position is recorded and the loop is broken
                    endIndex = i;
                    endEarly = true;
                }
                i++;
            }
        }

        // returns the next string of non separators or separators
        return text.substring(position, endIndex);

    }

    /**
     * Creates a map based on the {@code in} with the key being all the words
     * and the value being the number of occurrences of the word.
     *
     * @param in
     *            input file
     * @param separatorSet
     *            HashSet of all separator characters
     * @return HashMap<String, Integer> filled with the words and number of
     *         occurrences from {@code in}
     */
    private static HashMap<String, Integer> parseFile(BufferedReader in,
            HashSet<Character> separatorSet) {

        // creates a new HashMap to store the words and their counts in
        HashMap<String, Integer> words = new HashMap<>();

        // Reads in every individual line of the input file

        try {

            // reads in each new line
            String line = in.readLine();

            // while the input is not empty
            while (line != null) {

                line = line.toLowerCase();
                int position = 0;
                while (position < line.length()) {
                    // Finds each individual word and adds it to map or increases
                    // the map value if the word is already in map
                    String token = nextWordOrSeparator(line, position, separatorSet);
                    if (!separatorSet.contains(token.charAt(0))) {
                        if (words.containsKey(token)) {
                            int temp = words.get(token);
                            words.remove(token);
                            words.put(token, temp + 1);
                        } else {
                            words.put(token, 1);
                        }
                    }
                    position += token.length();
                }
                line = in.readLine();
            }

        } catch (IOException e) {
            System.err.println("Error Reading Input File: " + e);
        }

        return words;
    }

    /**
     * Returns an array list which holds n pairs, the n pairs are the pairs with
     * the highest values of all pairs from map.
     *
     * @param map
     *            the HashMap which holds the pairs of all words and their
     *            counts, is used to create the countArray array list
     * @param n
     *            the number of pairs to have in word array
     * @param minMax
     *            the array which holds the minCount and maxCount values for the
     *            pairs
     * @return the array list of the first n words found in countArray
     *
     * @clears map
     *
     * @replaces minMax
     *
     * @ensures the array list returned holds the min of {map.size() || n}
     *          amount of pairs, all pairs being the pairs with the highest
     *          values of all pairs in map
     */
    private static ArrayList<Map.Entry<String, Integer>> sortMap(
            HashMap<String, Integer> map, int n, int[] minMax) {

        // initializes comparators for array list
        Comparator<Map.Entry<String, Integer>> countOrder = new CountSort();
        Comparator<Map.Entry<String, Integer>> wordOrder = new WordSort();

        // initializes first array list
        ArrayList<Map.Entry<String, Integer>> countArray = new ArrayList<>();

        // initializes second array list
        ArrayList<Map.Entry<String, Integer>> wordArray = new ArrayList<>();

        // creates a view and iterator to go over all elements
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
        Iterator<Map.Entry<String, Integer>> it = entrySet.iterator();

        // puts all items in the map into the count array list
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            it.remove();
            countArray.add(entry);
        }

        // sorts the array by count
        countArray.sort(countOrder);

        // iterates over n words in the count array, finding the top n most
        // occuring words in the input file and putting those pairs into the
        // word array
        int length = countArray.size();
        for (int i = 0; i < length && i < n; i++) {

            // removes the current largest count pair in the count array
            Map.Entry<String, Integer> p = countArray.remove(0);

            // sets the maxCoun (and minCount incase n is 1) on first iteration
            // minCount on last iteration
            if (i == 0) {
                minMax[1] = p.getValue();
                minMax[0] = p.getValue();

            } else if (i == n - 1) {
                minMax[0] = p.getValue();
            }

            // adds the pair to the word array
            wordArray.add(p);
        }

        wordArray.sort(wordOrder);

        return wordArray;
    }

    /**
     * Calculates the font size for a specific word based on the its number of
     * occurrences {@code count}, the minimum number of occurrences
     * {@code minCount}, the maximum number of occurrences {@code maxCount} and
     * the minimum and maximum font sizes.
     *
     * @param count
     *            The number of occurrences of a word.
     * @param minCount
     *            The minimum number of occurrences of the chosen words.
     * @param maxCount
     *            The maximum number of occurrences of the chosen words.
     * @requires maxCount >= count, count >= minCount, maxCount > 0, and
     *           minCount > 0
     * @ensures fontSize is a font size from the CSS file that is proportional
     *          to its number of occurrences.
     * @return int fontSize, the corresponding font size of the word based on
     *         its number of occurrences {@code count}.
     */
    private static int fontSize(int count, int minCount, int maxCount) {

        int fontSize;
        // If all the words occur the same all the sizes will be the same
        if (minCount == maxCount) {
            fontSize = (MINIMUM_FONT_SIZE + MAXIMUM_FONT_SIZE) / 2;
        } else {
            // Uses linear scaling formula to calculate font size
            fontSize = MINIMUM_FONT_SIZE + (count - minCount)
                    * (MAXIMUM_FONT_SIZE - MINIMUM_FONT_SIZE) / (maxCount - minCount);
        }

        // Returns font size
        return fontSize;
    }

    /**
     * Prints the opening tags for the HTML file to be produced.
     *
     * @param out
     *            output fileWriter
     * @param inputFile
     *            name of the inputFile, used in the heading
     * @param n
     *            user requested number of words to appear in tag cloud
     *
     */
    private static void printHeader(PrintWriter out, String inputFile, int n) {

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Top " + n + " words in " + inputFile + "</title>");
        out.println("<link href=\"https://cse22x1.engineering.osu.edu/2231/web-sw2/"
                + "assignments/projects/tag-cloud-generator/data/tagcloud.css>\""
                + " rel=\"stylesheet\" type=\"text/css\">");
        out.println("<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");

        out.println("<body data-new-gr-c-s-check-"
                + "loaded=\"14.1207.0\" data-gr-ext-installed>");
        out.println("<h2>Top " + n + " words in " + inputFile + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");

    }

    /**
     * Prints all the words in the array list in alphabetical order with the
     * font size, proportional to the number of occurrences the word appears.
     *
     * @param out
     *            The output file.
     * @param sorted
     *            The array list with the n amount of words in alphabetical
     *            order.
     * @param minMax
     *            The array holding the minimum occurrences and the maximum
     *            occurrences.
     */
    private static void printTagCloud(PrintWriter out,
            ArrayList<Map.Entry<String, Integer>> sorted, int[] minMax) {

        // Prints each of the words with the font size based on its number of occurrences
        while (0 < sorted.size()) {
            Map.Entry<String, Integer> pair = sorted.remove(0);
            out.println("<span style=\"cursor:default\" class=\"f"
                    + fontSize(pair.getValue(), minMax[0], minMax[1])
                    + "\" title=\"count: " + pair.getValue() + "\">" + pair.getKey()
                    + "</span>");
        }
    }

    /**
     * Prints the closing tags for the HTML file to be produced.
     *
     * @param out
     *            output file
     *
     */
    private static void printFooter(PrintWriter out) {

        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        // initializes buffered reader and variables
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String inputFile = null;
        String outputFile = null;

        // creates separator set
        HashSet<Character> separators = new HashSet<Character>();
        for (int i = 0; i < SEPARATORS.length(); i++) {
            separators.add(SEPARATORS.charAt(i));
        }

        // creates array for min and max
        int[] minMax = new int[2];

        // prompts user for input and output file
        try {
            System.out.print("Enter name of input file: ");
            inputFile = in.readLine();
            System.out.print("Enter name of output file: ");
            outputFile = in.readLine();
        } catch (IOException e) {
            System.err.println("Error cannot read from console: " + e);
        }

        // prompts user for number of words
        int n = 0;
        try {
            String numWords = "";
            do {
                System.out.print("Enter a Positive Number of Words to Include: ");
                numWords = in.readLine();
                n = Integer.parseInt(numWords);
            } while (n < 0);

        } catch (NumberFormatException e) {
            System.err.println("Error Wrong Number Format: " + e);
        } catch (IOException e) {
            System.err.println("Error Reading Input: " + e);
        }

        // opens the input file reader
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(inputFile));
        } catch (IOException e) {
            System.err.println("Error opening input file: " + e);
            try {
                in.close();
            } catch (IOException err) {
                System.err.println("Error closing input reader: " + err);

            }
            return;
        }

        // opens the output file writer
        PrintWriter fileWriter = null;
        try {
            fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException e) {
            try {
                fileReader.close();
            } catch (IOException err) {
                System.err.println("Error closing input file: " + err);
            }
            try {
                in.close();
            } catch (IOException err) {
                System.err.println("Error closing input reader: " + err);

            }
            System.err.println("Error opening output file: " + e);
            return;
        }

        // reads the input file and adds words and their counts to the map
        HashMap<String, Integer> map = parseFile(fileReader, separators);

        // sorts the map into alphabetical order and in a map only containing the
        // n number of words the user requested
        ArrayList<Map.Entry<String, Integer>> sorted = sortMap(map, n, minMax);

        // prints to the html file
        printHeader(fileWriter, inputFile, n);
        printTagCloud(fileWriter, sorted, minMax);
        printFooter(fileWriter);

        /*
         * Close input and output streams
         */
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream: " + e);
        }

        try {
            fileReader.close();
        } catch (IOException e) {
            System.err.println("Error closing output stream: " + e);
        }

        fileWriter.close();

    }
}
