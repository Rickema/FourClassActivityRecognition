package com.example.emanuele.fourclassactivityrecognition;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class Training_Activity extends ActionBarActivity implements  SensorEventListener{
    private SensorManager sensorManager;
    private Sensor accelerometer;
    ImageButton idleButton = null;
    ImageButton stillButton = null;
    ImageButton walkButton = null;
    ImageButton runButton = null;
    Button stopButton = null;
    OnClickListener stopListener = null;
    OnClickListener idleListener = null;
    OnClickListener stillListener = null;
    OnClickListener walkListener = null;
    OnClickListener runListener = null;

    int start = 0; //questa variabile rimane a zero finchè non è stato cliccato nessun bottone, e assume i valori 1,2,3,4 in base all'attività selezionata, rispettivamente idle, still, alk e run
    /////////////////////////////Campioni e Frame///////////////////////////////////
    float [] singleSample = {0,0,0};
    int numberOfSample = 25;                                     //Definisce la dimensione dei singoli Frame, deve risultare poco più di 5 sec.
    float [][] singleFrame = new float[numberOfSample][3];
    int counterOfSample = 0;

    /////////////////////////////Campioni e Frame///////////////////////////////////

    /////////////////////////////////Features///////////////////////////////////////
    float [] meanOfPeriodCoord = new float[3];
    float [] standardDeviationFrame = new float[3];
    int [] numberOfPeaks = new int[3];
    double Km = 0;
    /////////////////////////////////Features///////////////////////////////////////

    //////////////////////////////DICHIARAZIONE FILE////////////////////////////////
    private final String TAG = "TrainingActivity";
    public final String PATH = Environment.getExternalStorageDirectory() + "/EsameAppMultimediali/";
    public final String FILE_IDLE = "Idle.txt";
    public final String FILE_STILL = "Still.txt";
    public final String FILE_WALK = "Walk.txt";
    public final String FILE_RUN = "Run.txt";
    public final String FILE_FRAME = "Frame.txt";
    File frameFile = null;
    File idleFile = null;
    File stillFile = null;
    File walkFile = null;
    File runFile = null;
    FileOutputStream writerF = null;
    FileOutputStream writerI = null;
    FileOutputStream writerS = null;
    FileOutputStream writerW = null;
    FileOutputStream writerR = null;
    BufferedWriter bfdWriterF = null;
    BufferedWriter bfdWriterI = null;
    BufferedWriter bfdWriterS = null;
    BufferedWriter bfdWriterW = null;
    BufferedWriter bfdWriterR = null;
    //////////////////////////////DICHIARAZIONE FILE////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_);

        ////////////////////////////ACQUISIZIONE INTENT/////////////////////////////////
        Intent intent = getIntent();
        ////////////////////////////ACQUISIZIONE INTENT/////////////////////////////////

        ////////////////////////////COLLEGAMENTO IDLE BUTTON////////////////////////////
        idleButton = (ImageButton) findViewById(R.id.idleButton);
        idleListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                start = 1;
            }
        };
        idleButton.setOnClickListener(idleListener);
        ////////////////////////////COLLEGAMENTO IDLE BUTTON////////////////////////////

        ////////////////////////////COLLEGAMENTO STILL BUTTON///////////////////////////
        stillButton = (ImageButton) findViewById(R.id.stillButton);
        stillListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                start = 2;
            }
        };
        stillButton.setOnClickListener(stillListener);
        ////////////////////////////COLLEGAMENTO STILL BUTTON///////////////////////////

        ////////////////////////////COLLEGAMENTO WALK BUTTON////////////////////////////
        walkButton = (ImageButton) findViewById(R.id.walkButton);
        walkListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                start = 3;
            }
        };
        walkButton.setOnClickListener(walkListener);
        ////////////////////////////COLLEGAMENTO RUN BUTTON/////////////////////////////
        runButton = (ImageButton) findViewById(R.id.runButton);
        runListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                start = 4;
            }
        };
        runButton.setOnClickListener(runListener);
        ////////////////////////////COLLEGAMENTO RUN BUTTON/////////////////////////////

        ////////////////////////////COLLEGAMENTO STOP BUTTON////////////////////////////
        stopButton = (Button) findViewById(R.id.stopButton);
        stopListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                start = 0;
                counterOfSample = 0;
            }
        };
        stopButton.setOnClickListener(stopListener);
        ////////////////////////////COLLEGAMENTO STOP BUTTON////////////////////////////

        //////////////////////////INIZIALIZZAZIONE FILE IDLE/////////////////////////////
        idleFile = new File(PATH + FILE_IDLE);

        if(idleFile.exists())
            Log.i(TAG, "Il file Idle.txt" + PATH + " esiste");
        else
        {
            try {
                idleFile.createNewFile();
                Log.i(TAG, "Il file Idle.txt" + PATH +  " non esiste");
                Log.i(TAG, "Il file Idle.txt" + PATH +  " è stato creato");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writerI = new FileOutputStream(idleFile);
            bfdWriterI = new BufferedWriter(new OutputStreamWriter(writerI));

        } catch (IOException e) {
            e.printStackTrace();
        }
        //////////////////////////INIZIALIZZAZIONE FILE IDLE/////////////////////////////

        //////////////////////////INIZIALIZZAZIONE FILE STILL/////////////////////////////
        stillFile = new File(PATH + FILE_STILL);

        if(stillFile.exists())
            Log.i(TAG, "Il file Still.txt" + PATH +  " esiste");
        else
        {
            try {
                stillFile.createNewFile();
                Log.i(TAG, "Il file Still.txt" + PATH +  " non esiste");
                Log.i(TAG, "Il file Still.txt" + PATH +  " è stato creato");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writerS = new FileOutputStream(stillFile);
            bfdWriterS = new BufferedWriter(new OutputStreamWriter(writerS));

        } catch (IOException e) {
            e.printStackTrace();
        }
        ///////////////////////////INIZIALIZZAZIONE FILE STILL/////////////////////////////
        //////////////////////////////INIZIALIZZAZIONE FILE WALK/////////////////////////////
        walkFile = new File(PATH + FILE_WALK);

        if(walkFile.exists())
            Log.i(TAG, "Il file Walk.txt" + PATH +  " esiste");
        else
        {
            try {
                walkFile.createNewFile();
                Log.i(TAG, "Il file Walk.txt" + PATH +  " non esiste");
                Log.i(TAG, "Il file Walk.txt" + PATH +  " è stato creato");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writerW = new FileOutputStream(walkFile);
            bfdWriterW = new BufferedWriter(new OutputStreamWriter(writerW));

        } catch (IOException e) {
            e.printStackTrace();
        }
        //////////////////////////////INIZIALIZZAZIONE FILE WALK/////////////////////////////

        /////////////////////////////INIZIALIZZAZIONE FILE RUN//////////////////////////
        runFile = new File(PATH + FILE_RUN);

        if(runFile.exists())
            Log.i(TAG, "Il file Run.txt" + PATH +  " esiste");
        else
        {
            try {
                runFile.createNewFile();
                Log.i(TAG, "Il file Run.txt" + PATH +  " non esiste");
                Log.i(TAG, "Il file Run.txt" + PATH +  " è stato creato");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writerR = new FileOutputStream(runFile);
            bfdWriterR = new BufferedWriter(new OutputStreamWriter(writerR));

        } catch (IOException e) {
            e.printStackTrace();
        }
        ////////////////////////////INIZIALIZZAZIONE FILE RUN///////////////////////////

        ///////////////////////////INIZIALIZZAZIONE FILE FRAME//////////////////////////
        frameFile = new File(PATH + FILE_FRAME);

        if(frameFile.exists())
            Log.i(TAG, "Il file Frame.txt" + PATH +  " esiste");
        else
        {
            try {
                frameFile.createNewFile();
                Log.i(TAG, "Il file Frame.txt" + PATH +  " non esiste");
                Log.i(TAG, "Il file Frame.txt" + PATH +  " è stato creato");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writerF = new FileOutputStream(frameFile);
            bfdWriterF = new BufferedWriter(new OutputStreamWriter(writerF));

        } catch (IOException e) {
            e.printStackTrace();
        }
        //////////////////////////INIZIALIZZAZIONE FILE FRAME///////////////////////////

        ///////////////////////INIZIALIZZAZIONE ACCELEROMETRO///////////////////////////
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);           //inizializzazione oggetto sensor manager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.i(TAG, "Accelerometer not found");// fail! we dont have an accelerometer!
        }
        ///////////////////////INIZIALIZZAZIONE ACCELEROMETRO///////////////////////////
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(start > 0)
        {
            ///////////////////////CAMPIONAMENTO E MEMORIZZAZIONE///////////////////////////
            //long start = System.nanoTime();
            singleSample[0] = event.values[0];
            singleSample[1] = event.values[1];
            singleSample[2] = event.values[2];

            ///////////////////SCRIVO SUI LOG I CAMPIONI PER VERIFICA///////////////////////
            //if(counterOfSample == 0 ) Log.i(TAG, "-- CAMPIONI -- ");
            //Log.i(TAG, "X: " + singleSample[0] + " Y: " + singleSample[1] + " Z: " + singleSample[2] + " campione n. " + counterOfSample);
            ///////////////////SCRIVO SUI LOG I CAMPIONI PER VERIFICA///////////////////////

            for(int i = 0; i < 3; i++) {                                //Ogni volta che campioniamo su i tre assi,
                singleFrame[counterOfSample][i] = singleSample[i];      //inseriamo il singolo campione all'interno del frame.
            }

            counterOfSample++;
            ///////////////////////CAMPIONAMENTO E MEMORIZZAZIONE///////////////////////////

            ///////////////////////////SCRITTURA DELLE FEATURE//////////////////////////////
            if (counterOfSample == numberOfSample) {

                /////////////////////SCRIVO SUI LOG IL FRAME PER VERIFICA///////////////////////
                Log.i(TAG, "-- FRAME -- ");
                for(int j = 0; j < numberOfSample; j++) {

                    Log.i(TAG,(j+1) + "- X: " + singleFrame[j][0] + " Y: " + singleFrame[j][1] + " Z: " + singleFrame[j][2]);
                }
                /////////////////////SCRIVO SUI LOG IL FRAME PER VERIFICA///////////////////////

                ///////////////////////////////Calcolo feature//////////////////////////////////
                /////////////////////SCRIVO SUI LOG LE FEATURE PER VERIFICA///////////////////////
                Log.i(TAG, "-- FEATURE -- ");

                PeriodMeanEvaluation();
                Log.i(TAG, "Mean: X: " + meanOfPeriodCoord[0] + " Y: " + meanOfPeriodCoord[1] + " Z: " + meanOfPeriodCoord[2]);

                PeriodStandardDeviation();
                Log.i(TAG, "Standard Deviation: X: " + standardDeviationFrame[0] + " Y: " + standardDeviationFrame[1] + " Z: " + standardDeviationFrame[2]);

                NumberOfPeak();
                Log.i(TAG, "Peaks: X: " + numberOfPeaks[0] + " Y: " + numberOfPeaks[1] + " Z: " + numberOfPeaks[2]);

                KmEvalueation();
                Log.i(TAG, "Km: " + Km);
                /////////////////////SCRIVO SUI LOG LE FEATURE PER VERIFICA///////////////////////
                ///////////////////////////////Calcolo feature//////////////////////////////////

                //////////////////////////////Scrittura su file/////////////////////////////////
                WritingFeatureToFile();
                //////////////////////////////Scrittura su file/////////////////////////////////
                Km = 0;
                counterOfSample = 0; // azzero il contatore affinche, dopo i prossimi n campioni, vengono rivalutate le feature e riscritte sul file.
            }

        }
    }

    /////////////////////////F    E    A    T    U    R    E////////////////////////
    ////////////////////////////////CALCOLO MEDIA///////////////////////////////////
    private void PeriodMeanEvaluation () {

        float x = 0;
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < numberOfSample; j++)
            {
                x = x + singleFrame[j][i];
            }
            meanOfPeriodCoord[i] = (x/numberOfSample);
            x = 0;
        }
    }

    ///////////////////////CALCOLO DEVIAZIONE STANDARD//////////////////////////////
    private void PeriodStandardDeviation (){

        float x = 0;
        double sd = 0;
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < numberOfSample; j++)
            {
                x = x + singleFrame[j][i];
            }
            x = 0;
        }

        /////////////////////////////////Evaluation on X////////////////////////////////
        for (int i=0; i < numberOfSample; i++)
        {
            sd = sd + Math.pow(singleFrame[i][0] - meanOfPeriodCoord[0], 2);
        }
        double standardDeviation = Math.sqrt(sd/(numberOfSample-1));            //DANGER!!! Secondo me non c'è il -1
        standardDeviationFrame[0] = (float)standardDeviation;
        sd = 0;
        /////////////////////////////////Evaluation on X////////////////////////////////

        /////////////////////////////////Evaluation on Y////////////////////////////////
        for (int i=0; i < numberOfSample; i++)
        {
            sd = sd + Math.pow(singleFrame[i][1] - meanOfPeriodCoord[1], 2);
        }
        standardDeviation = Math.sqrt(sd/(numberOfSample-1));
        standardDeviationFrame[1] = (float)standardDeviation;
        sd = 0;
        /////////////////////////////////Evaluation on Y////////////////////////////////

        /////////////////////////////////Evaluation on Z////////////////////////////////
        for (int i=0; i < numberOfSample; i++)
        {
            sd = sd + Math.pow(singleFrame[i][2] - meanOfPeriodCoord[2], 2);
        }
        standardDeviation = Math.sqrt(sd/(numberOfSample-1));
        standardDeviationFrame[2] = (float)standardDeviation;
        /////////////////////////////////Evaluation on Z////////////////////////////////
    }
    //////////////////////////CALCOLO NUMERO PICCHI/////////////////////////////////
    private void NumberOfPeak() {
        /////////////////////////Azzeramento conteggio picchi///////////////////////////
        numberOfPeaks[0] = 0;
        numberOfPeaks[1] = 0;
        numberOfPeaks[2] = 0;
        /////////////////////////Azzeramento conteggio picchi///////////////////////////

        float epsilon = 0.2f;//!DANGER!ho messo un valore a caso per ora, dobbiamo capire quanto vale.
        Log.i(TAG,"epsilon = " + (meanOfPeriodCoord[0]+ 0.6 * meanOfPeriodCoord[0]));

        for (int j = 0; j < 3; j++) {
            for (int m = 0; m < numberOfSample; m++) {
                if (m > 0 && m < (numberOfSample-1)) {
                    if ((((singleFrame[m + 1][j] - singleFrame[m][j]) * (singleFrame[m][j] - singleFrame[m - 1][j])) < 0)) {

                        // Verifico che il modulo del campione si > di epsilon
                        if ((singleFrame[m][j] < 0) && (singleFrame[m][j] < (meanOfPeriodCoord[0]+ 0.9 * meanOfPeriodCoord[0]))) {
                            numberOfPeaks[j]++;
                        } else if ((singleFrame[m][j] > 0) && (singleFrame[m][j] > (meanOfPeriodCoord[0]+ 0.9 * meanOfPeriodCoord[0]))) {
                            numberOfPeaks[j]++;
                        }
                    }
                } else if (m == 0) {
                    if (((singleFrame[m + 1][j] - singleFrame[m][j]) < 0) && (singleFrame[m][j] > epsilon)) {

                        // Verifico che il modulo del campione sia > di epsilon
                        if ((singleFrame[m][j] < 0) && (singleFrame[m][j] < (meanOfPeriodCoord[1]+ 5 * meanOfPeriodCoord[1]))) {
                            numberOfPeaks[j]++;
                        } else if ((singleFrame[m][j] > 0) && (singleFrame[m][j] > (meanOfPeriodCoord[1]+ 5 * meanOfPeriodCoord[1]))) {
                            numberOfPeaks[j]++;
                        }
                    } else if (m == (numberOfSample-1)) {
                        if (((singleFrame[m][j] - singleFrame[m - 1][j]) < 0) && (singleFrame[m][j] > epsilon)) {

                            // Verifico che il modulo del campione sia > di epsilon
                            if ((singleFrame[m][j] < 0) && (singleFrame[m][j] < (meanOfPeriodCoord[2]+ 5 * meanOfPeriodCoord[2]))) {
                                numberOfPeaks[j]++;
                            } else if ((singleFrame[m][j] > 0) && (singleFrame[m][j] > (meanOfPeriodCoord[2]+ 5 * meanOfPeriodCoord[2]))) {
                                numberOfPeaks[j]++;
                            }
                        }
                    }
                }
            }
        }
    }
    /////////////////////////////////CALCOLO Km/////////////////////////////////////
    private void KmEvalueation(){

        float firstSum = 0;
        float secondSum = 0;
        float total_X = 0;
        float total_Y = 0;
        double totalFirstSum = 0;
        double totalSecondSum = 0;
        float delta;
        double coeff;

        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < numberOfSample; j++)
            {
                firstSum = (float)Math.pow(singleFrame[j][i],2);
                total_X += firstSum;
            }

            totalFirstSum += total_X;
        }

        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < numberOfSample; j++)
            {
                secondSum += (singleFrame[j][i]);
            }

            total_Y += Math.pow(secondSum,2);
            secondSum = 0;
        }

        totalSecondSum = total_Y/numberOfSample;

        Log.i(TAG,"totalY " + total_Y);
        Log.i(TAG,"totalsecondSum " + totalSecondSum);

        delta = (float)(totalFirstSum - totalSecondSum);
        Log.i(TAG,"delta " + delta);
        coeff = (1/((float)numberOfSample - 1)) * delta;
        Log.i(TAG,"coeff " + coeff);

        Km = Math.sqrt((coeff));
        //Km = (float)Math.sqrt((1/(numberOfSample - 1))*(totalFirstSum - totalSecondSum));
    }
    /////////////////////////F    E    A    T    U    R    E////////////////////////

    ///////////////////////////////SCRITTURA SU FILE////////////////////////////////
    private void WritingFeatureToFile() {

        try {

            String numberOfPeaksX = Float.toString(numberOfPeaks[0]);
            String numberOfPeaksY = Float.toString(numberOfPeaks[1]);
            String numberOfPeaksZ = Float.toString(numberOfPeaks[2]);
            String meanPeriodX = Float.toString(meanOfPeriodCoord[0]);
            String meanPeriodY = Float.toString(meanOfPeriodCoord[1]);
            String meanPeriodZ = Float.toString(meanOfPeriodCoord[2]);
            String standardDeviationX = Float.toString(standardDeviationFrame[0]);
            String standardDeviationY = Float.toString(standardDeviationFrame[1]);
            String standardDeviationZ = Float.toString(standardDeviationFrame[2]);
            String KmEvaluation = Double.toString(Km);

            ///////////////////////VARIABILI PER STAMPA FRAME SU FILE///////////////////////
            FileWriter writerF = new FileWriter(frameFile, true);
            FileOutputStream fileoutF = new FileOutputStream(frameFile, true);
            PrintWriter scrittoreFrame = new PrintWriter(fileoutF);
            ///////////////////////VARIABILI PER STAMPA FRAME SU FILE///////////////////////

            FileWriter writer = null;
            FileOutputStream fileout = null;
            PrintWriter scrittore = null;
            boolean fileExist = false;
            switch (start)
            {
                case 1:
                    writer = new FileWriter(idleFile, true);
                    fileout = new FileOutputStream(idleFile, true);
                    scrittore = new PrintWriter(fileout);
                    fileExist = idleFile.exists();
                    break;
                case 2:
                    writer = new FileWriter(stillFile, true);
                    fileout = new FileOutputStream(stillFile, true);
                    scrittore = new PrintWriter(fileout);
                    fileExist = stillFile.exists();
                    break;
                case 3:
                    writer = new FileWriter(walkFile, true);
                    fileout = new FileOutputStream(walkFile, true);
                    scrittore = new PrintWriter(fileout);
                    fileExist = walkFile.exists();
                    break;
                case 4:
                    writer = new FileWriter(runFile, true);
                    fileout = new FileOutputStream(runFile, true);
                    scrittore = new PrintWriter(fileout);
                    fileExist = runFile.exists();
                    break;
                default:
                    break;
            }

            if (!fileExist) {
                writer.write( meanPeriodX + "\t" + meanPeriodY + "\t" + meanPeriodZ + "\t");
                writer.write( standardDeviationX + "\t" + standardDeviationY + "\t" + standardDeviationZ + "\t");
                writer.write( numberOfPeaksX + "\t" + numberOfPeaksY + "\t" + numberOfPeaksZ + "\t");
                writer.write( KmEvaluation + "\n");
                writer.flush();
                writer.close();
            }

            else {

                scrittore.append( meanPeriodX + "\t" + meanPeriodY + "\t" + meanPeriodZ + "\t");
                scrittore.append( standardDeviationX + "\t" + standardDeviationY + "\t" + standardDeviationZ + "\t");
                scrittore.append( numberOfPeaksX + "\t" + numberOfPeaksY + "\t" + numberOfPeaksZ + "\t");
                scrittore.append( KmEvaluation + "\n");
                scrittore.flush();
                scrittore.close();
            }

            //////////////////////////////STAMPO FRAME SU FILE//////////////////////////////
            if(!frameFile.exists()) {
                for(int j = 0; j < numberOfSample; j++) {               //Sul file verrà stampato il frame con i campioni sulle righe e gli assi sulle colonne
                    for(int i = 0; i < 3; i++) {
                        writerF.write(singleFrame[j][i] + "\t");
                    }
                    writerF.write("\n");
                }
                writerF.flush();
                writerF.close();
            }else {
                for(int j = 0; j < numberOfSample; j++) {               //Sul file verrà stampato il frame con i campioni sulle righe e gli assi sulle colonne
                    for(int i = 0; i < 3; i++) {
                        scrittoreFrame.append(singleFrame[j][i] + "\t");
                    }
                    scrittoreFrame.append("\n");
                }
                scrittoreFrame.flush();
                scrittoreFrame.close();
            }
            //////////////////////////////STAMPO FRAME SU FILE//////////////////////////////

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_training_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
