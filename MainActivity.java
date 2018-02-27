package com.example.saurabhverma.peaks;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
public class MainActivity extends AppCompatActivity {
    LineGraphSeries<DataPoint> series;
    PointsGraphSeries<DataPoint> series2;
    TextView tv;
    Button button;
    GraphView graph;
    int time=60;
    int fs=125;
    int len=time*fs;
    int size=0;

    EditText editTextFileName,editTextTime;
    int one=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextFileName = (EditText) findViewById(R.id.editText1);
        editTextTime = (EditText) findViewById(R.id.editText2);
        tv = (TextView) findViewById(R.id.TextView01);
        graph = (GraphView) findViewById(R.id.graph);
        button = (Button) findViewById(R.id.Button1);
    }
    public void buttonclick(View view)
    {

          String value= editTextTime.getText().toString();
            if(value!="")
            {
                time = Integer.parseInt(value);
                len = time * fs;
            }
        process(len);
    }

    public void process(int len)
    {
            double array[] = new double[len];
            double newarray[] = new double[len];
            double energy[] = new double[len];
            int max[] = new int[len];
            double smooth[]=new double[len];

            StringBuffer stringBuffer = new StringBuffer();
            String filename=editTextFileName.getText().toString();
            int n=0;
            try {
                one=1;
                File myFile = new File("/sdcard/"+filename);
                FileInputStream fIn = new FileInputStream(myFile);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(fIn));
                String test;
                while (true) {
                    test = br.readLine();
                    if (test == null) break;
                    if (n==len-1) break;
                    array[n]=Double.valueOf(test);
                    n++;
                }
                fIn.close();
                br.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),filename + " not found",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        int m=fs*3/10;
        double k[]=new double[m];
        double h[]=new double[m];
        double sigma=m/(2*0.03*fs);
        double totalsum=0;
        double maximum=0;
        h[m-1]=0;
        double p=0;
        int i;
        for(i=0;i<m;i++)
        {
            p=-1*(i-m/2)*(i-m/2)/(sigma*sigma*2);
            k[i] = Math.exp(p);
            if(i>=1)
            {
                h[i - 1] = k[i] - k[i - 1];
            }
        }
        double sum=0;
        int start=0;
        for(i=0;i<len;i++)
        {
            for(int j=0;j<m;j++)
            {
                start=i-m/2+j;
                if(start<0)
                {
                    sum=sum+0;
                }
                else if (start>len-1)
                {
                    sum=sum+0;
                }
                else {
                    sum = sum + array[start] * h[j];
                }
            }
            newarray[i]=sum;
            totalsum=totalsum+sum;
            if(maximum<sum)
            {
                maximum=sum;
            }
            sum=0;
        }


        for(i=0;i<len;i++)
        {
            newarray[i]=newarray[i]-totalsum/len;
            newarray[i]=newarray[i]/maximum;
        }

        double var=0;
        for(i=0;i<len; i++)
        {
            energy[i]=newarray[i]*newarray[i];
            var=var+ energy[i]*energy[i];
        }
        double threshold=1* Math.sqrt(var/len);
        for( i=0;i<len; i++)
        {
            if(energy[i]<threshold)
            {
                energy[i]=0;
            }
        }

        int start3=0;
        int end3=0;
        m=20;
        sum=0;
        for(int j=0;j<len;j++)
        {   sum=0;
            for ( i = 0; i < m; i++) {

                start3 = j- m / 2;
                end3 = j + m / 2;
                if (start3 < 0) {
                    sum = sum + 0;
                } else if (end3 > len - 1) {
                    sum = sum + 0;
                } else {
                    sum = sum + energy[start3+i];
                }
            }
            smooth[j]=sum/20;
        }

        int start2=0;
        int end2=0;
        m=20;
        sum=0;
        for(int j=0;j<len;j++)
        {   sum=0;
            for (i = 0; i < m; i++) {

                start2 = j- m / 2;
                end2 = j + m / 2;
                if (start2 < 0) {
                    sum = sum + 0;
                } else if (end2 > len - 1) {
                    sum = sum + 0;
                } else {
                    sum = sum + smooth[start2+i];
                }
            }
            energy[j]=sum/20;
        }
        for( i=1;i<len-4;i++)
        {
            if((energy[i-1]<energy[i])&&(energy[i]>=energy[i+1]))
            {
                max[size]=i;
                size++;
            }
        }
        m=20;
        int start1=0;
        int end1=len-1;
        for( i=0;i<size-1;i++)
        {
            start1 = max[i]- m / 2;
            end1 = max[i] + m / 2;
            if (start1 < 0)
            {
                start1=0;
            }
            if (end1 > len - 1)
            {
                end1=len-1;
            }
            for( int q=start1;q<end1;q++)
            {
                if(array[q]>array[max[i]])
                {
                    max[i]=q;
                }
            }

        }

        tv.append("PEAKS PER MINUTE ="+size*60/time);

        graphplot(array,max,len);
    }

    private void graphplot(double array[],int max[],int len)
    {
        series=new LineGraphSeries<DataPoint>();
        for(int i=0; i<len-1;i++)
        {
            series.setColor(Color.RED);
            series.setBackgroundColor(Color.RED);
            series.appendData(new DataPoint(i,array[i]),true,len-1);
        }
        series2 = new PointsGraphSeries<DataPoint>();
        int t;

        for(int i=0; i<size-1;i++)
        {
            series2.setShape(PointsGraphSeries.Shape.RECTANGLE);
            series2.setSize( 5);
            series2.setColor(Color.BLUE);
            t=max[i];
            series2.appendData(new DataPoint(t,array[t]),true,len-1);
        }


        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-50);
        graph.getViewport().setMaxY(150);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(4);
        graph.getViewport().setMaxX(500);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.addSeries(series);
        graph.addSeries(series2);
    }

}

