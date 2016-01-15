/**
* WordCounter example
*
* @author Eleftherios Anagnostopoulos
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class WordCounter extends MapReduceFramework {

    public void map(final File fileEntry, Map<String, ArrayList<String>> map) {
        Scanner sc = null;
        String key = null;

        try {
            if (fileEntry.isFile()) {
                sc = new Scanner(fileEntry);

                while (sc.hasNext()) {
                    key = stemEntry(sc.next().toLowerCase());
                    mapHelp(map, key, "1");
                }
                sc.close();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (sc != null) {
                sc.close();
            }
        }
    }

    public void mapHelp(Map<String, ArrayList<String>> map, String key, String value) {

        ArrayList<String> values = map.get(key);

        if (values == null) {
            values = new ArrayList<String>();
            values.add(value);
            map.put(key, values);
        }
        else {
            values.add(value);
        }
    }

    /* Removes characters that are not included in [a..z] or [0..9] */
    public String stemEntry(String word) {
        String new_word = "";
        Character c;

        for (int i = 0; i < word.length(); i++) {
            c = word.charAt(i);

            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                new_word = new_word + c;
            }
        }
        return new_word;
    }

    public void reduce(String key, ArrayList<String> value) {
        Integer new_value = 0;

        for (int i = 0; i < value.size(); i++) {
            new_value = new_value + Integer.parseInt(value.get(i));
        }
        synchronized (reducing_output) {
            reducing_output.put(key, new_value.toString());
        }
    }

    public static void main(String[] args) {

        if (args.length != 4) {
            System.err.println("Usage: java WordCounter input_directory output_directory "
                             + "number_of_mapper_threads number_of_reducer_threads");
            System.exit(1);
        }
        WordCounter wc = new WordCounter();
        wc.driver(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }
}
