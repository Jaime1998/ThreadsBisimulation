package main;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ProcessTask implements Runnable {

    private final ArrayList<String> bisimilarList;
    private final String pathFolder;
    private final String automatonA;
    private final String automatonB;
    private final FileWriter fw;
    private final FileWriter fwLog;
    CountDownLatch latch;

    ProcessTask( ArrayList<String> bisimilarList,
                 String pathFolder,
                 String automatonA,
                 String automatonB,
                 String saveFile,
                 String saveFileLog,
                 CountDownLatch latch) throws IOException {
        this.bisimilarList = bisimilarList;
        this.pathFolder = pathFolder;
        this.automatonA = automatonA;
        this.automatonB = automatonB;
        this.fw = new FileWriter(saveFile, true);
        this.fwLog = new FileWriter(saveFile, true);
        this.latch = latch;
    }


    public synchronized void writeFile(String[] write)  {
        try{
            for(String w: write){
                this.fw.write(w);
                this.fw.write(",");
            }
            this.fw.write("\n");
            this.fw.flush();
            this.fw.close();
        } catch (IOException e) {
            writeFileLog(write);
        }

    }

    public synchronized void writeFileLog(String[] write)  {
        try{
            for(String w: write){
                this.fwLog.write(w);
                this.fwLog.write(",");
            }
            this.fwLog.write("\n");
            this.fwLog.flush();
            this.fwLog.close();
        }catch (IOException e){
            System.out.println("No se pudo escribir en log: ");
            for(String w: write){
                System.out.print(w);
            }
            System.out.println();
        }

    }

    public void run () {

        try {
            //String cmd = "\"C:\\Program Files (x86)\\Common Files\\Oracle\\Java\\javapath\\java.exe\" -Djava.library.path=PATH\\x64_win64 -cp \"tool.jar;*;PATH\\cplex.jar\" main.Main -b 30 \"".concat(pathFolder).concat("\\".concat(pathnames[finalI])).concat("\" \"").concat(pathFolder).concat("\\".concat(pathnames[finalJ])).concat("\"");

            //Process p = Runtime.getRuntime().exec(cmd);

            String[] logWriter = new String[1];
            logWriter[0] = this.automatonA.concat(" ").concat(automatonB);

            writeFile(logWriter);

            String[] cmd1 = {"java",
                            "-Djava.library.path=PATH\\x64_win64",
                            "-cp",
                            "\"tool.jar;*;PATH\\cplex.jar\"",
                            "main.Main" ,
                            "-b",
                            "30",
                            "\"".concat(this.pathFolder).concat(File.separator).concat(this.automatonA).concat("\""),
                            "\"".concat(this.pathFolder).concat(File.separator).concat(this.automatonB).concat("\""),};

            String[] cmd = {"java",
                    "-jar",
                    "ImplThesis.jar",
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

            String[] error= new String[1];
            error[0] = "Error: ".concat(this.automatonA).concat(" ").concat(this.automatonB);
            System.out.println(error[0]);
            this.writeFileLog(error);
            this.latch.countDown();
            e.printStackTrace();
        }

    }



}
