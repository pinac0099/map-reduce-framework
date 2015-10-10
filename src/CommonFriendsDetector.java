/**
* CommonFriendsDetector example
*
* @author Eleftherios Anagnostopoulos
* @since 2014-12-15
*
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class CommonFriendsDetector extends MapReduceFramework {

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
        String value1, value2, old_key, new_key, new_key_transp;

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

                new_key = value1 + " " + value2 + " #";
                new_key_transp = value2 + " " + value1 + " #";

                synchronized (reducing_output) {
                    old_key = reducing_output.get(new_key_transp);

                    if (old_key == null) {
                        old_key = reducing_output.get(new_key);
                        old_key = (old_key != null) ? old_key + " " + key : key;
                        reducing_output.put(new_key, old_key);
                    }
                    else {
                        old_key = old_key + " " + key;
                        reducing_output.put(new_key_transp, old_key);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {

        if (args.length != 4) {
            System.err.println("Usage: java CommonFriendsDetector input_directory "
                             + "output_directory number_of_mapper_threads "
                             + "number_of_reducer_threads");
            System.exit(1);
        }

        CommonFriendsDetector cfd = new CommonFriendsDetector();
        cfd.driver(args[0], args[1], Integer.parseInt(args[2]) , Integer.parseInt(args[3]));
    }
}
