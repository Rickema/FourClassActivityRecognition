///////////////////////////////////////////////////////////////////////////////
package com.example.emanuele.fourclassactivityrecognition;

        import android.os.Environment;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View.OnClickListener;
        import android.view.Menu;
        import android.widget.Button;
        import android.content.Intent;
        import android.view.MenuItem;
        import android.view.View;


        import android.content.Context;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.widget.TextView;

        import java.io.*;
        import java.io.File;
        import java.io.FileReader;
        import java.io.BufferedReader;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.StringReader;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStreamWriter;

        import java.lang.Math;

//Import messi da EMA, TEST
        import java.io.BufferedWriter;
        import java.io.FileReader;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.regex.Matcher;
        import java.util.regex.Pattern;

        import libsvm.svm;
        import libsvm.svm_model;
        import libsvm.svm_node;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    /////////////////////////////////VARIABILI//////////////////////////////////////
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView currentX, currentY, currentZ;      //maxX, maxY, maxZ;
    Button trainingButton = null;
    Button startButton = null;
    OnClickListener startListener = null;
    OnClickListener trainingListener = null;
    boolean start = false;

    /////////////////////////////Campioni e Frame///////////////////////////////////
    float[] singleSample = {0, 0, 0};
    int numberOfSample = 25;                              //Definisce la dimensione dei singoli Frame, deve risultare poco più cdi 5 sec.
    float[][] singleFrame = new float[numberOfSample][3];
    int counterOfSample = 0;
    int multipleOfFrame = 1;

    /////////////////////////////Campioni e Frame///////////////////////////////////

    ////////////////////////////DICHIARAZIONE MATRICE RANGE/////////////////////////

    float[][] range = new float[10][2];
    float range_lower = -1;
    float range_upper = 1;
    float range_min = 0;
    float range_max = 0;

    ////////////////////////////DICHIARAZIONE MATRICE RANGE/////////////////////////

    /////////////////////////////////Features///////////////////////////////////////
    float [] meanOfPeriodCoord = new float[3];
    float [] standardDeviationFrame = new float[3];
    int [] numberOfPeaks = new int[3];
    double Km = 0;
    float[] feature = new float[10];
    /////////////////////////////////Features///////////////////////////////////////

    /////////////////////////////////Scale Features///////////////////////////////////////
    float [] meanOfPeriodCoordScaled = new float[3];
    float [] standardDeviationFrameScaled = new float[3];
    int [] numberOfPeaksScaled = new int[3];
    double KmScaled = 0;
    /////////////////////////////////Scale Features///////////////////////////////////////

    //////////////////////////////////VARIABILI/////////////////////////////////////

    //////////////////////////////DICHIARAZIONE FILE////////////////////////////////
    private final String TAG = "MainActivity";
    public final String PATH = Environment.getExternalStorageDirectory() + "/EsameAppMultimediali/";
    public final String FILE_NAME = "Features.txt";
    File file = null;
    FileOutputStream writer = null;
    BufferedWriter bfdWriter = null;
    //////////////////////////////DICHIARAZIONE FILE////////////////////////////////

    ///////////////////////////////////FUNZIONI/////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //////////////////////////////INIZIALIZZAZIONE FILE/////////////////////////////
        file = new File(PATH + FILE_NAME);

        if(file.exists())
            Log.i(TAG, "Il file " + PATH +  " esiste");
        else
        {
            try {
                file.createNewFile();
                Log.i(TAG, "Il file " + PATH +  " non esiste");
                Log.i(TAG, "Il file " + PATH +  " è stato creato");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writer = new FileOutputStream(file);
            bfdWriter = new BufferedWriter(new OutputStreamWriter(writer));

        } catch (IOException e) {
            e.printStackTrace();
        }
        //////////////////////////////INIZIALIZZAZIONE FILE/////////////////////////////

        initializeViews();

        /////////////////////////LINK TRA BOTTONE START E LISTENER//////////////////////
        startListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick - Start Button");
                start = true;
            }
        };
        startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener(startListener);
        /////////////////////////LINK TRA BOTTONE START E LISTENER//////////////////////

        ///////////////LINK TRA BOTTONE E LISTENER PER ACTIVITY DI TRAINING/////////////
        trainingListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick - trainingButton");
                // 1. create an intent pass class name or intent action name
                Intent intent = new Intent("com.example.fourclassactivityrecognition.TrainingActivity");

                //3. start the activity
                startActivity(intent);
            }
        };
        trainingButton = (Button) findViewById(R.id.buttonTraining);
        trainingButton.setOnClickListener(trainingListener);
        ///////////////LINK TRA BOTTONE E LISTENER PER ACTIVITY DI TRAINING/////////////

        ///////////////////////INIZIALIZZAZIONE ACCELEROMETRO//////////////////////////
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);           //inizializzazione oggetto sensor manager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);    //associa L'accelerometro al sensor manager
        } else {
            // fail! we dont have an accelerometer!
        }
        ///////////////////////INIZIALIZZAZIONE ACCELEROMETRO//////////////////////////

        //////////////////////INIZIALIZZAZIONE MATRICE RANGE///////////////////////////////

        // The name of the file to open.
        //String fileName = "temp.txt";
        String fileName = Environment.getExternalStorageDirectory() + "/training.txt.range";

        // This will reference one line at a time
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);
                //    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            //range [j][1] = Float.parseFloat(line);
            for(int i=0; i < 12; i++)
            {
                String line = bufferedReader.readLine();
                if(i>1)
                {
                    String[] splat = line.split(" ");
                    Log.i(TAG, "Line: " + splat[0] + " split 2: " + splat[1] + " split 3: " + splat[2]);
                    range[i-2][0] = Float.parseFloat(splat[1]);
                    range[i-2][1] = Float.parseFloat(splat[2]);
                }

            }
            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        //////////////////////INIZIALIZZAZIONE MATRICE RANGE///////////////////////////////
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        start = false;
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        start = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override //consiglio del prof: tenere leggera questa funzione
    public void onSensorChanged(SensorEvent event) {
        if(start)
        {
            // clean current values
            displayCleanValues();
            // display the current x,y,z accelerometer values
            displayCurrentValues();

            ///////////////////////CAMPIONAMENTO E MEMORIZZAZIONE///////////////////////////
            singleSample[0] = event.values[0];
            singleSample[1] = event.values[1];
            singleSample[2] = event.values[2];

            ///////////////////SCRIVO SUI LOG I CAMPIONI PER VERIFICA///////////////////////
            if(counterOfSample == 0 ) Log.i(TAG, "-- CAMPIONI -- ");
            Log.i(TAG, "X: " + singleSample[0] + " Y: " + singleSample[1] + " Z: " + singleSample[2] + " campione n. " + counterOfSample);
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

                feature[0] = meanOfPeriodCoordScaled[0];
                feature[1] = meanOfPeriodCoordScaled[1];
                feature[2] = meanOfPeriodCoordScaled[2];
                feature[3] = standardDeviationFrameScaled[0];
                feature[4] = standardDeviationFrameScaled[1];
                feature[5] = standardDeviationFrameScaled[2];
                feature[6] = numberOfPeaksScaled[0];
                feature[7] = numberOfPeaksScaled[1];
                feature[8] = numberOfPeaksScaled[2];
                feature[9] = (float)KmScaled;

                ///////////////////////////////Normalizzazione//////////////////////////////////
                ScaleFeatures();
                ///////////////////////////////Normalizzazione//////////////////////////////////

                for(int i = 0; i < 10; i++)
                {
                    Log.i(TAG, "Scaled Feature: " + feature[i]);
                }

                /////////////////////SCRIVO SUI LOG LE FEATURE PER VERIFICA///////////////////////
                ///////////////////////////////Calcolo feature//////////////////////////////////



                //////////////////////////////Classificazione Frame///////////////////

                svm_model model = null;             //non mi ha fatto mettere private, mettere poi tutto sopra
                svm_node[] feat = null;


                try {
                    model = svm.svm_load_model(Environment.getExternalStorageDirectory() + "/training.txt.model");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                feat = new svm_node[11];
                for (int i = 0; i < 10; i++) {
                    feat[i] = new svm_node();
                    feat[i].index = i;
                    feat[i].value = feature[i];
                }
                feat[10] = new svm_node();
                feat[10].index = -1;

                double res = svm.svm_predict(model, feat);
                Log.i(TAG, "State: " + res);
                //////////////////////////////Classificazione Frame///////////////////////////*/



                //////////////////////////////Scrittura su file/////////////////////////////////
                //WritingFeatureToFile();
                //////////////////////////////Scrittura su file/////////////////////////////////
                Km = 0;
                counterOfSample = 0; // azzero il contatore affinche, dopo i prossimi n campioni, vengono rivalutate le feature e riscritte sul file.
            }

        }

    }

    public void ScaleFeatures()
    {
        for(int i = 0; i < 10 ; i++) {
            feature[i] = range_lower + (range_upper - (range_lower)) * (feature[i] - range[i][0])/(range[i][1]-range[i][0]);
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
                        if ((singleFrame[m][j] < 0) && (singleFrame[m][j] < (meanOfPeriodCoord[0]+ 0.6 * meanOfPeriodCoord[0]))) {
                            numberOfPeaks[j]++;
                        } else if ((singleFrame[m][j] > 0) && (singleFrame[m][j] > (meanOfPeriodCoord[0]+ 0.6 * meanOfPeriodCoord[0]))) {
                            numberOfPeaks[j]++;
                        }
                    }
                } else if (m == 0) {
                    if (((singleFrame[m + 1][j] - singleFrame[m][j]) < 0) && (singleFrame[m][j] > epsilon)) {

                        // Verifico che il modulo del campione sia > di epsilon
                        if ((singleFrame[m][j] < 0) && (singleFrame[m][j] < (meanOfPeriodCoord[1]+ 0.6 * meanOfPeriodCoord[1]))) {
                            numberOfPeaks[j]++;
                        } else if ((singleFrame[m][j] > 0) && (singleFrame[m][j] > (meanOfPeriodCoord[1]+ 0.6 * meanOfPeriodCoord[1]))) {
                            numberOfPeaks[j]++;
                        }
                    } else if (m == (numberOfSample-1)) {
                        if (((singleFrame[m][j] - singleFrame[m - 1][j]) < 0) && (singleFrame[m][j] > epsilon)) {

                            // Verifico che il modulo del campione sia > di epsilon
                            if ((singleFrame[m][j] < 0) && (singleFrame[m][j] < (meanOfPeriodCoord[2]+ 0.6 * meanOfPeriodCoord[2]))) {
                                numberOfPeaks[j]++;
                            } else if ((singleFrame[m][j] > 0) && (singleFrame[m][j] > (meanOfPeriodCoord[2]+ 0.6 * meanOfPeriodCoord[2]))) {
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

        float x = 0;
        float y = 0;
        float totalx = 0;
        float totaly = 0;
        double totalFirstSum = 0;
        double totalSecondSum = 0;

        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < numberOfSample; j++)
            {
                x = (float)Math.pow(singleFrame[j][i],2);
                totalx += x;
            }

            totalFirstSum += totalx;
        }

        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < numberOfSample; j++)
            {
                y += (singleFrame[j][i]);
            }

            totaly += y;
        }

        totalSecondSum = Math.pow(totaly,2);
        totalSecondSum = totalSecondSum/numberOfSample;

        Km = Math.sqrt(((float)1/(float)(numberOfSample - 1))*(totalFirstSum - totalSecondSum));
    }
    /////////////////////////F    E    A    T    U    R    E////////////////////////

   /* ///////////////////////////////SCRITTURA SU FILE////////////////////////////////
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

            if (!file.exists()) {

                FileWriter fw = new FileWriter(file, true);
                fw.write("- Mean of period: ( " + meanPeriodX + "; " + meanPeriodY + "; " + meanPeriodZ + " )\n");
                fw.write("- Standard Deviation: ( " + standardDeviationX + "; " + standardDeviationY + "; " + standardDeviationZ + " )\n");
                fw.write("- Number of Peaks: ( " + numberOfPeaksX + "; " + numberOfPeaksY + ";" + numberOfPeaksZ + " )\n");
                fw.write("- Km : " + KmEvaluation + "\n\n");
                fw.flush();
                fw.close();
            } else {
                FileOutputStream fileout = new FileOutputStream(file, true);
                PrintWriter scrittore = new PrintWriter(fileout);
                scrittore.append("- Mean of period: ( " + meanPeriodX + "; " + meanPeriodY + "; " + meanPeriodZ + " )\n");
                scrittore.append("- Standard Deviation: ( " + standardDeviationX + "; " + standardDeviationY + "; " + standardDeviationZ + " )\n\n");
                scrittore.append("- Number of Peaks: ( " + numberOfPeaksX + "; " + numberOfPeaksY + ";" + numberOfPeaksZ + " )\n");
                scrittore.append("- Km: " + KmEvaluation + "\n\n");
                scrittore.flush();
                scrittore.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }*/

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    //display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(singleSample[0]));
        currentY.setText(Float.toString(singleSample[1]));
        currentZ.setText(Float.toString(singleSample[2]));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        start = false;
        try {
            bfdWriter.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> parseIntsAndFloats(String raw) {

        ArrayList<String> listBuffer = new ArrayList<String>();

        Pattern p = Pattern.compile("[0-9]*\\.?[0-9]+");

        Matcher m = p.matcher(raw);

        while (m.find()) {
            listBuffer.add(m.group());
        }

        return listBuffer;
    }

}

    ///////////////////**************CORREZZIONE****************////////////////////
    /////////////////////////Credo si possa cancellare//////////////////////////////
    /*private float[] MeanEvaluation(float [] array) {

        float x;
        for(int i = 0; i < 3; i++) {

            x = totalSum[i];
            x += array[i];
            totalSum[i] = x;
            float mean = x / counterOfSample;
            meanHistory[i] = mean;
        }
        return meanHistory;
    }

    private float MeanPeriodEvaluation(long a, long b) {

        long timeFrame = (b - a) * (10 ^ 9);                        //Tempo impiegato per recuperare un frame di valori X,Y e Z
        count++;

        sum += timeFrame;
        samplePeriod = sum / count;                                 //Il pdf parla di un periodo di acquisizione dati e uno di pausa, che abbiamo tralasciato. Insieme formano un periodo

        return samplePeriod;
    }*/
    /////////////////////////Credo si possa cancellare//////////////////////////////
    ///////////////////**************CORREZZIONE****************////////////////////
    ///////////////////////////////FINE FUNZIONI////////////////////////////////////


////////////////////////////// BOZZA /////////////////////////////////////

 /*
    private double TimeSinceGo() {

        float estimatedTime = (System.nanoTime() - startTestTime);
        double seconds = (double)estimatedTime / 1000000000.0;

        return seconds;
    }

    */

//FUNZIONE TENTATIVO 3
//public String[] OpenFile() throws IOException {
//
//    FileReader reader = new FileReader(Environment.getExternalStorageDirectory() +
//            "/" + "training.txt.range");
//    BufferedReader textReader = new BufferedReader(reader);
//
//    int numberOfLines = readLines();
//    String[] textData = new String[numberOfLines];
//    int i;
//
//    for (i=0; i<numberOfLines; i++) {
//        textData[i] = textReader.readLine();
//    }
//
//    // close the line-by-line reader and return the data
//    textReader.close();
//    return textData;
//}
//    int readLines() throws IOException {
//        FileReader reader = new FileReader(Environment.getExternalStorageDirectory() +
//                "/" + "training.txt.range");
//        BufferedReader textReader = new BufferedReader(reader);
//        String line;
//        int numberOfLines = 0;
//
//        while ((line = textReader.readLine()) != null) {
//            // I tried this:
//            if (line.contains("//")) {
//                line.skip();
//            }
//            numberOfLines++;
//        }
//        reader.close();
//        return numberOfLines;
//    }


