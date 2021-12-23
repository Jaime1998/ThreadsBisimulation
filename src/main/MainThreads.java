package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainThreads {
    public static void main(String[] args) throws InterruptedException {
        int nThreads = Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(nThreads);

        String saveFile = "TramBisimthreadstesting.csv";

        //File file = new File("C:\\Users\\Jaime\\OneDrive - correounivalle.edu.co\\Documentos\\Github\\TranslationTraceUppaal\\Train-Gate-Controller\\mutantsUniform");
        File file = new File("C:\\Users\\Jaime\\Desktop\\tramBisim3");
        final String[] pathnames = file.list();

        String pathFolder = file.getAbsolutePath();

        assert pathnames != null;
        int n = pathnames.length;

        ArrayList<String> bisimilarList = new ArrayList<>();

        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(((n*(n-1))/2));


        for(int i=0; i<n; i++){
            for(int j=i+1; j<n; j++){
                ProcessTask processTask = null;
                try {
                    processTask = new ProcessTask(bisimilarList, pathFolder, pathnames[i], pathnames[j], saveFile, latch);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert processTask != null;
                executor.execute(processTask);

            }
        }

        executor.shutdown();

        latch.await();

        long end = System.currentTimeMillis();



        try {
            FileWriter fw = new FileWriter(saveFile, true);

            fw.write("Final Time: ".concat(Integer.toString((int) (end-start))));
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
