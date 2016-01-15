/**
* TriangleCounter example
*
* @author Eleftherios Anagnostopoulos
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class TriangleCounter extends MapReduceFramework {

    public void map(final File fileEntry, Map<String, ArrayList<String>> map) {
        Scanner sc = null;
        String word;
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

    public ArrayList<String> intersection(ArrayList<String> list1, ArrayList<String> list2) {
        ArrayList<String> inter = new ArrayList<String>();

        for (String s : list1) {

            if (list2.contains(s)) {
                inter.add(s);
            }
        }

        return inter;
    }

    public void reduce(String key, ArrayList<String>value) {
        String value1, value2;
        Integer counter = 0;

        for (int i = 0; i < value.size(); i++) {
            value1 = value.get(i);

            if (value1.equals(key)) {
                continue;
            }

            for (int j = i + 1; j < value.size(); j++) {
                value2 = value.get(j);

                if (value1.equals(value2)) {
                    continue;
                }

                synchronized (mapping_output) {

                    if (mapping_output.get(value1).contains(value2)) {
                        counter++;
                    }
                }
            }
        }
        synchronized (reducing_output) {
            reducing_output.put(key, counter.toString());
        }
    }

    public static void main(String[] args) {

        if (args.length != 4) {
            System.err.println("Usage: java TriangleCounter input_directory output_directory "
                             + "number_of_mapper_threads number_of_reducer_threads");
            System.exit(1);
        }

        TriangleCounter tc = new TriangleCounter();
        tc.driver(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }
}
