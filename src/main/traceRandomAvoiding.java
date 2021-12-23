package main;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class traceRandomAvoiding {

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    public static int editDistance(String s1, String s2) {


        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static synchronized void writeFile(PrintWriter out, String[] write){

        for(String w: write){
            out.print(w);
            out.print(",");
        }
        out.print("\n");
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        String saveFile = "mut46Time100Sim1ClockT.csv";


        FileWriter fw = new FileWriter(saveFile);
        PrintWriter out = new PrintWriter(fw, true);

        File file = new File("C:\\Users\\Jaime\\Desktop\\Train-Gate-Controller\\mut46");
        final String[] pathnames = file.list();

        String pathFolder = file.getAbsolutePath();

        assert pathnames != null;
        int n = pathnames.length;
        Thread[] threadsTraces = new Thread[n];
        //HashMap<String, Thread> threadTraces = new HashMap<>();
        //Thread[] threadsComparison = new Thread[((n*(n-1)))/2];

        HashMap<String, Thread> threadsComparison = new HashMap<>();

        ArrayList<String> bisimilarList = new ArrayList<>();

        HashMap<String, String> traces = new HashMap<>();


        long globalStart = System.currentTimeMillis();

        for(int i=0; i<n; i++){
            int finalI = i;
            threadsTraces[i] = new Thread(()->{
                try{
                    String cmd = "\"C:\\Program Files\\uppaal64-4.1.25-5\\bin-Windows\\verifyta.exe\" -q -t 0 -r 0 ".concat(pathFolder).concat("\\".concat(pathnames[finalI]).concat("\""));
                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.redirectErrorStream(true);
                    Process p = null;
                    p = pb.start();

                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

                    String line = null;
                    String traceString = "";
                    long start = System.currentTimeMillis();

                    while ((line = stdInput.readLine()) != null) {
                        traceString = traceString.concat(line);
                    }
                    long end = System.currentTimeMillis();

                    String[] outWrite = new String[2];

                    outWrite[0] = pathnames[finalI];
                    outWrite[1] = String.valueOf(end-start);
                    writeFile(out, outWrite);

                    traces.put(pathnames[finalI], traceString);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }



        for (Thread thread : threadsTraces) {
            thread.start();
        }

        for (Thread thread : threadsTraces) {
            thread.join();
        }

        for(int i=0; i<n; i++){
            for(int j=i+1; j<n; j++){
                int finalI = i;
                int finalJ = j;
                Thread comparisonThread = new Thread(()->{
                    long start = System.currentTimeMillis();
                    String traceI = traces.get(pathnames[finalI]);
                    String traceJ = traces.get(pathnames[finalJ]);

                    double vSimilar = new JaroWinklerSimilarity().apply(traceI, traceJ);


                    if(vSimilar==1){

                        String[] outWrite = new String[4];

                        outWrite[0] = pathnames[finalI];
                        outWrite[1] = pathnames[finalJ];

                        long end = System.currentTimeMillis();
                        outWrite[2] = String.valueOf(end-start);


                        bisimilarList.add(pathnames[finalI].concat("<->").concat(pathnames[finalJ]));

                        outWrite[3] = Integer.toString(bisimilarList.size());

                        //outWrite[4] = String.join("  " , bisimilarList);

                        writeFile(out, outWrite);
                    }
                    else{

                        String[] outWrite = new String[3];

                        outWrite[0] = pathnames[finalI];
                        outWrite[1] = pathnames[finalJ];

                        long end = System.currentTimeMillis();
                        outWrite[2] = String.valueOf(end-start);

                        writeFile(out, outWrite);

                    }
                });

            }
        }


        for (Thread thread : threadsComparison) {
            thread.start();
        }

        for (Thread thread : threadsComparison) {
            thread.join();
        }


        long globalEnd = System.currentTimeMillis();


        System.out.println(bisimilarList);
        float sec = (globalEnd - globalStart) / 1000F;

        out.print(bisimilarList.size());
        out.print(", ");
        for(String bisimilar: bisimilarList){
            out.print(bisimilar);
            out.print(", ");
        }
        out.print("\n");
        out.print(", ");
        out.print(sec);
        out.print(", ");

        //Flush the output to the file
        out.flush();

        //Close the Print Writer
        out.close();

        //Close the File Writer
        fw.close();

        System.out.println(new JaroWinklerSimilarity().apply(traces.get("tmi5.xml"), traces.get("tmi10.xml")));

/**
        System.out.println(traces.get("cxl1.xml").equals(traces.get("cxl2.xml")));
        System.out.println(traces.get("cxl1.xml"));
        System.out.println(traces.get("cxl2.xml"));

 */


    }
}
