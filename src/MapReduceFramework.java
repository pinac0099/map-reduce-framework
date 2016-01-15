/**
* MapReduce Framework in Java
*
* @author Eleftherios Anagnostopoulos
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public abstract class MapReduceFramework {
    /* TreeMap for storing data after map step */
    protected Map<String, ArrayList<String>> mapping_output = new TreeMap<String, ArrayList<String>>();
    /* TreeMap for storing data after reduce step */
    protected Map<String, String> reducing_output = new TreeMap<String, String>();

    public void driver(final String inputDirectoryStr,
                       final String outputDirectoryStr,
                       final int numOfMappers,
                       final int numOfReducers) {

        final File inputDirectory = new File(inputDirectoryStr);
        final File outputDirectory = new File(outputDirectoryStr);

        if (!outputDirectory.exists()) {
            try {
                outputDirectory.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Read files in input directory */
        /* Create partitions for mapper threads */
        File[] listOfFiles = inputDirectory.listFiles();
        int numOfFilesPerMapper = listOfFiles.length / numOfMappers;
        ArrayList<ArrayList<File>> filesPerMapper = new ArrayList<ArrayList<File>>();
        ArrayList<File> tempFiles;
        int counter = 0;
        int index = 0;

        for (int i = 1; i <= numOfMappers; i++) {
            counter = (i == numOfMappers) ? listOfFiles.length
                    - (numOfMappers - 1) * numOfFilesPerMapper
                    : numOfFilesPerMapper;
            tempFiles = new ArrayList<File>();

            for (int j = 0; j < counter; j++) {
                tempFiles.add(listOfFiles[index]);
                index++;
            }
            filesPerMapper.add(tempFiles);
            tempFiles = null;
        }

        /* Initialize and start mapper threads */
        ArrayList<HashMap<String, ArrayList<String>>> mapper_maps =
            new ArrayList<HashMap<String, ArrayList<String>>>();
        ArrayList<Mapper> mappers = new ArrayList<Mapper>();

        for (int i = 0; i < numOfMappers; i++) {
            HashMap<String, ArrayList<String>> mapper_map =
                new HashMap<String, ArrayList<String>>();
            mapper_maps.add(mapper_map);
            Mapper mapper = new Mapper(filesPerMapper.get(i), mapper_map);
            mappers.add(mapper);
            mapper.start();
        }

        /* End of mapping step */
        for (Mapper mapper : mappers) {
            try {
                mapper.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /* Partition results of map function for reducer threads */
        int numOfValuesPerReducer = mapping_output.size() / numOfReducers;

        ArrayList<HashMap<String, ArrayList<String>>> valuesPerReducer =
            new ArrayList<HashMap<String, ArrayList<String>>>();
        HashMap<String, ArrayList<String>> tempMapping;
        index = 0;

        Iterator<Entry<String, ArrayList<String>>> si = mapping_output.entrySet().iterator();

        for (int i = 1; i <= numOfReducers; i++) {
            counter = (i == numOfReducers) ? mapping_output.size()
                    - (numOfReducers - 1) * numOfValuesPerReducer
                    : numOfValuesPerReducer;
            tempMapping = new HashMap<String, ArrayList<String>>();

            for (int j = 0; j < counter; j++) {
                Entry<String, ArrayList<String>> entry = si.next();
                tempMapping.put(entry.getKey(), entry.getValue());
                index++;
            }
                valuesPerReducer.add(tempMapping);
                tempMapping = null;
        }

        /* Initialize and start reducer threads */
        ArrayList<HashMap<String, ArrayList<String>>> reducer_maps =
            new ArrayList<HashMap<String, ArrayList<String>>>();
        ArrayList<Reducer> reducers = new ArrayList<Reducer>();

        for (int i = 0; i < numOfReducers; i++) {
            reducer_maps.add(valuesPerReducer.get(i));
            Reducer reducer = new Reducer(valuesPerReducer.get(i));
            reducers.add(reducer);
            reducer.start();
        }

        /* End of reducing step */
        for (Reducer reducer : reducers) {
            try {
                reducer.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /* Write reduced data to output file */
        Writer fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            fileWriter = new FileWriter(outputDirectory);
            bufferedWriter = new BufferedWriter(fileWriter);

            for (Entry<String, String> entry : reducing_output.entrySet()) {
                String output = entry.getKey() + " " + entry.getValue()
                              + System.getProperty("line.separator");
                bufferedWriter.write(output);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (bufferedWriter != null && fileWriter != null) {
                try {
                    bufferedWriter.close();
                    fileWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Mapper extends Thread {
        final ArrayList<File> files;
        Map<String, ArrayList<String>> map;

        Mapper(final ArrayList<File> files, Map<String, ArrayList<String>> map) {
            this.files = files;
            this.map = map;
        }

        public void run() {
            String key = null;
            ArrayList<String> values = null;

            for (final File fileEntry : files) {
                map(fileEntry, map);
            }

            for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
                key = entry.getKey();
                values = entry.getValue();

                synchronized (mapping_output) {
                    ArrayList<String> old_values = mapping_output.get(key);

                    if (old_values != null) {
                        old_values.addAll(values);
                    }
                    else {
                        mapping_output.put(key, values);
                    }
                }
            }
        }
    }

    private class Reducer extends Thread {
        Map<String, ArrayList<String>> map;

        Reducer(Map<String, ArrayList<String>> map) {
            this.map = map;
        }

        public void run() {

            for (Entry<String, ArrayList<String>> entry : map.entrySet()) {
                reduce(entry.getKey(), entry.getValue());
            }
        }
    }

    public abstract void map(final File fileEntry, Map<String, ArrayList<String>> map);

    public abstract void reduce(String key, ArrayList<String> value);
}
