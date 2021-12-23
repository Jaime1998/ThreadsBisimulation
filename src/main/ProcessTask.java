package main;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ProcessTask implements Runnable {

    private final ArrayList<String> bisimilarList;
    private final String pathFolder;
    private final String automatonA;
    private final String automatonB;
    private final FileWriter fw;
    CountDownLatch latch;

    ProcessTask( ArrayList<String> bisimilarList,
                 String pathFolder,
                 String automatonA,
                 String automatonB,
                 String saveFile,
                 CountDownLatch latch) throws IOException {
        this.bisimilarList = bisimilarList;
        this.pathFolder = pathFolder;
        this.automatonA = automatonA;
        this.automatonB = automatonB;
        this.fw = new FileWriter(saveFile, true);
        this.latch = latch;
    }


    public synchronized void writeFile(String[] write) throws IOException {

        for(String w: write){
            this.fw.write(w);
            this.fw.write(",");
        }
        this.fw.write("\n");
        this.fw.flush();
        this.fw.close();
    }

    public void run () {

        try {
            //String cmd = "\"C:\\Program Files (x86)\\Common Files\\Oracle\\Java\\javapath\\java.exe\" -Djava.library.path=PATH\\x64_win64 -cp \"tool.jar;*;PATH\\cplex.jar\" main.Main -b 30 \"".concat(pathFolder).concat("\\".concat(pathnames[finalI])).concat("\" \"").concat(pathFolder).concat("\\".concat(pathnames[finalJ])).concat("\"");

            //Process p = Runtime.getRuntime().exec(cmd);
            String[] cmd = {"java",
                            "-Djava.library.path=PATH\\x64_win64",
                            "-cp",
                            "\"tool.jar;*;PATH\\cplex.jar\"",
                            "main.Main" ,
                            "-b",
                            "30",
                            "\"".concat(this.pathFolder).concat(File.separator).concat(this.automatonA).concat("\""),
                            "\"".concat(this.pathFolder).concat(File.separator).concat(this.automatonB).concat("\""),};


            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = null;
            p = pb.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = null;
            long start = System.currentTimeMillis();

            while ((line = stdInput.readLine()) != null) {


                if(line.contains("Result of bisimulation check: true")){

                    String[] outWrite = new String[5];

                    outWrite[0] = automatonA;
                    outWrite[1] = automatonB;

                    long end = System.currentTimeMillis();
                    outWrite[2] = String.valueOf(end-start);


                    this.bisimilarList.add(automatonA.concat("<->").concat(automatonB));

                    outWrite[3] = Integer.toString(this.bisimilarList.size());
                    outWrite[4] = String.join("  " , this.bisimilarList);

                    writeFile(outWrite);

                    start = end;

                }if(line.contains("Result of bisimulation check: false")){

                    String[] outWrite = new String[3];

                    outWrite[0] = automatonA;
                    outWrite[1] = automatonB;

                    long end = System.currentTimeMillis();
                    outWrite[2] = String.valueOf(end-start);

                    writeFile(outWrite);

                    start = end;
                }
            }
            p.waitFor();
            stdInput.close();
            this.latch.countDown();
        }catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }



}
