package com.example.conversormonedaxmldianayrafa;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Moneda> monedas;
    Moneda monedaOrigen, monedaDestino;
    Spinner spinorigen,spindestino;
    EditText ct_origen;
    EditText ct_destino;
    boolean escribirOrigen;
    Double textoOrigen;
    Double textoDestino;
    int divisaOrigen = 0;
    int divisaDestino = 1;
    float cambioOrigen, cambioDestino;
    DecimalFormat df = new DecimalFormat("#.##");

    Switch opcionXML;
    TextView txtLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Para que no aparezca el ActionBar
         */
        getSupportActionBar().hide();

        ct_origen =(EditText) findViewById(R.id.origen_edit);
        ct_destino = (EditText) findViewById(R.id.destino_edit);
        escribirOrigen = true;
        spinorigen = (Spinner) findViewById(R.id.origen_spinner);
        spindestino = (Spinner) findViewById(R.id.destino_spinner);
        opcionXML= (Switch) findViewById(R.id.sXML);
        txtLog= (TextView) findViewById(R.id.txtLog);

        df.setRoundingMode(RoundingMode.CEILING);


        dom = false;

        CargarXmlTask tarea = new CargarXmlTask();
        tarea.execute("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");

        //spinorigen.setSelection(0);
        //spindestino.setSelection(1);


        ct_origen.setOnFocusChangeListener (new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                escribirOrigen = true;
            }
        });

        ct_destino.setOnFocusChangeListener (new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                escribirOrigen = false;
            }
        });

        ct_origen.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                //Sobreescrito
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Sobreescrito
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (escribirOrigen){
                    if(ct_origen.getText().toString().equalsIgnoreCase("") || ct_origen.getText().toString() == null){
                        ct_destino.setText("");
                    }else{
                        textoOrigen = Double.parseDouble(ct_origen.getText().toString());
                        textoDestino = ((textoOrigen * cambioDestino)/cambioOrigen);
                         ct_destino.setText(df.format(textoDestino));
                    }
                }
                //escribirOrigen = false;
            }
        });


        ct_destino.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                //Sobreescrito
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Sobreescrito
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!escribirOrigen){//Significa que no se ha escrito en el origen
                    if(ct_destino.getText().toString().equalsIgnoreCase("") || ct_destino.getText().toString() == null){
                        ct_origen.setText("");
                    }else{
                        textoDestino = Double.parseDouble(ct_destino.getText().toString());
                        textoOrigen = ((textoDestino * cambioOrigen)/cambioDestino);
                        ct_origen.setText(df.format(textoOrigen));
                    }
                }

            }
        });


        opcionXML.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if(isChecked){
                    txtLog.setText("Checked");
                    dom = true;
                    CargarXmlTask tarea = new CargarXmlTask();
                    tarea.execute("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");

                }else{
                    txtLog.setText("NOOO");
                    dom = false;
                    CargarXmlTask tarea = new CargarXmlTask();
                    tarea.execute("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
                }



            }
        });

    }


    boolean dom;


    private class CargarXmlTask extends AsyncTask<String,Void,Boolean> {
        @Override
        protected Boolean doInBackground(String... p) {
            //Se ejecuta este hilo a parte para no tener que esperar al fichero XML


                if(dom){

                    MonedaParserDOM parserDOM= new MonedaParserDOM(p[0]);
                    monedas= (ArrayList<Moneda>)parserDOM.parse();
                    monedas.add(0, new Moneda("EUR", 1f));

                }else{
                    MonedaParserSAX parserSAX=new MonedaParserSAX(p[0]);
                    monedas=(ArrayList<Moneda>)parserSAX.parse();
                    monedas.add(0,new Moneda("EUR",1f));
                }


            return true;

        }

        @Override
        protected void onPostExecute(Boolean o) {
           //Cuando se carga el doc XML se ejecuta este metodo
            spinorigen=findViewById(R.id.origen_spinner);
            spindestino=findViewById(R.id.destino_spinner);
            final AdaptadorSpinner a1=new AdaptadorSpinner(MainActivity.this,R.layout.layout_elemento_spinner,monedas);

            spinorigen.setAdapter(a1);
            spindestino.setAdapter(a1);
            spindestino.setSelection(1);


            spinorigen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    divisaOrigen=position;

                    if(divisaOrigen== divisaDestino){
                        if(divisaOrigen== 32){
                            divisaOrigen= 0;
                        } else{
                            divisaOrigen++;
                        }
                        spinorigen.setSelection(divisaOrigen);
                    }
                    monedaOrigen = monedas.get(divisaOrigen);
                    cambioOrigen = monedaOrigen.getCambio();
                    ct_origen.setText("");
                    //Toast.makeText(MainActivity.this, divisaOrigen, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this, "Moneda: "+monedaOrigen.getNombre()+"\nValor: "+monedaOrigen.getCambio(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.i("Infarm","No se selecciona nada");
                }
            });

            spindestino.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    divisaDestino=position;

                    if(divisaDestino== divisaOrigen){
                        if(divisaDestino== divisaOrigen){
                            if(divisaDestino== 32){
                                divisaDestino= 0;
                            } else{
                                divisaDestino++;
                            }
                            spindestino.setSelection(divisaDestino);
                        }
                    }
                    monedaDestino = monedas.get(divisaDestino);
                    cambioDestino = monedaDestino.getCambio();
                    ct_destino.setText("");
                    //Toast.makeText(MainActivity.this, "Moneda: "+monedaDestino.getNombre()+"\nValor: "+monedaDestino.getCambio(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.i("Infarm","No se selecciona nada");
                }
            });

        }
    }
}
