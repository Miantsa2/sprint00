package com.framework.object;

import java.io.*;
import java.util.*;

public class CSVreader {

    public static List<String[]> readCsv(InputStream inputStream) throws IOException {
        List<String[]> records = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        
        boolean isFirstLine = true; 

        while ((line = reader.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; 
            }
            String[] values = line.split(";");
            records.add(values);
        }

        reader.close();
        return records;
    }
}

