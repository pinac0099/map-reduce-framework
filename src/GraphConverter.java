/**
* GraphConverter example
*
* @author Eleftherios Anagnostopoulos
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class GraphConverter extends MapReduceFramework{

    public void map(final File fileEntry, Map<String, ArrayList<String>> map) {
        Scanner sc = null;
        String word = null;
        String key = "";
        String value = "";
        String temp = "";
        Character c;

        try {
            if (fileEntry.isFile()) {
                sc = new Scanner(fileEntry);

                while (sc.hasNext()) {
                    word = sc.next();

                    for (int i = 0; i < word.length(); i++) {
                        c = word.charAt(i);

                        if (c == ',') {
                            key = temp;
                            temp = "";
                        }
                        if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                            temp = temp + c;
                        }
                    }
                    value = temp;
                    temp = "";

                    mapHelp(map, key, value);
                    mapHelp(map, value, key);
                }
                sc.close();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (sc != null)
            sc.close();
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
            if (!values.contains(value)) {
                values.add(value);
            }
        }
    }

    public void reduce(String key, ArrayList<String> value) {
        String new_value = "#";
        ArrayList<Integer> duplicates = new ArrayList<Integer>();

        for (Integer i = 0; i < value.size(); i++) {

            for (Integer j = i + 1; j < value.size(); j++) {

                if (value.get(i).equals(value.get(j))) {
                    duplicates.add(j);
                }
            }
            if (!duplicates.contains(i)) {
                new_value = new_value + " " + value.get(i);
            }
        }
        synchronized (reducing_output) {
            reducing_output.put(key, new_value.toString());
        }
    }

    public static void main(String[] args) {

        if (args.length != 4) {
            System.err.println("Usage: java GraphConverter input_directory output_directory "
                             + "number_of_mapper_threads number_of_reducer_threads");
            System.exit(1);
        }

        GraphConverter gc = new GraphConverter();
        gc.driver(args[0], args[1], Integer.parseInt(args[2]) , Integer.parseInt(args[3]));
    }
}
