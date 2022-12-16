package com.example.speechtotext;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Map;
import com.google.common.base.CharMatcher;

import info.debatty.java.stringsimilarity.Cosine;


public class MainActivity extends AppCompatActivity {

    // *** Declaramos nuestras variables ***

    protected static final int RESULT_SPEECH = 1;

    //Boton Hablar
    private ImageButton btnSpeak;

    //Módulo de entrada de voz
    private TextView voiceInput;

    //Boton Buscar
    Button search;

    //Declaramos nuestra variable db (Data-Base) ***
    FirebaseFirestore db;

    //Este pequeño modulo fue creado para revisar que los datos obtenidos fueran correctos
    //TextView viewDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializamos nuestro boton hablar
        btnSpeak = findViewById(R.id.btnSpeak);

        //Inicializamos nuestro modulo de entrada de datos
        voiceInput = findViewById(R.id.voiceInput);

        //Modulo de prueba
        //viewDatos = findViewById(R.id.textViewDatos);

        //Creamos la instancia de nuestra Base de Datos
        db = FirebaseFirestore.getInstance();

        //Creamos la instancia de nuestro Boton Buscar
        search = findViewById(R.id.btnSearch);

        //Campo Vacío
        emptyField();

        //Módulo de entrada de Voz
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceInput();
            }
        });

    }

    //Este método nos indica que, en caso de que el reconocimiento de voz sea exitoso, nos guarde el resultado en un ArrayList de Strings
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput.setText(text.get(0));

                    String texto = text.get(0);

                    texto = cleanData(texto);

                    translatorModule(texto);

                }
                break;
        }
    }

    //Esta función se encarga de configurar al Oyente "Microfono"
    private void startVoiceInput(){
        //Utilizamos un Intent (En este caso nuestro Itent, será una tarea que ya fue creada previamente por Google
        //Esta tarea tiene el nombre de "RecognizerIntent.ACTION_RECOGNIZE_SPEECH"
        //Esta tarea lo que hace es iniciar una actividad en la cual se le pide al usuario que hable para que esta sea enviada posteriormente a través
        //de un reconocedor de voz
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // "EXTRA_LANGUAGE_MODEL" Informa a nuestro reconocedor de voz que modelo de voz prefiere al realizar ACTION_RECOGNIZE_SPEECH
        //Por otra parte "LANGUAGE_MODEL_FREE_FORM" utiliza un modelo basado en el reconocimiento de voz de forma libre
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Definimos la etiqueta de idioma IETF, es decir, en que idioma escuchará nuestro microfono de voz
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX");
        //Aquí le estamos asignando el texto de bienvenida que será mostrado al activar el boton hablar
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bienvenido. ¿Qué deseas preguntar?");
        try {
            startActivityForResult(intent, RESULT_SPEECH);
            voiceInput.setText("");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Tu dispositivo no soporta el reconocimiento de voz", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //Módulo de Limpieza
    private String cleanData(String texto) {

        texto = Normalizer.normalize(texto, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        texto = texto.replaceAll("[^A-Za-z ]", "");

        texto = texto.toLowerCase();

        return texto;
    }

    //Módulo Traductor
    private void translatorModule(String texto) {
        db.collection("Preguntas").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                boolean bandera = false;
                String url = "";
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    String clave = document.getString("clave");
                    clave = cleanData(clave);


                    if (texto.contains(clave)) {

                        String s1 = document.getString("pregunta");

                        s1 = cleanData(s1);

                        Cosine cosine = new Cosine(2);

                        Map<String, Integer> profile1 = cosine.getProfile(texto);
                        Map<String, Integer> profile2 = cosine.getProfile(s1);

                        double similitud = (cosine.similarity(profile1,profile2));

                        //viewDatos.setText(String.valueOf(similitud));
                        //viewDatos.setText(s1 + " - " + similitud);

                        if(similitud >= 0.85) {
                            bandera = true;
                            url = document.getString("url");
                            break;
                        }
                    }
                }
                if (bandera) {

                    //Llamamos a nuestro módulo de visualización
                    displayModule(url);

                } else {
                    //En caso de que no se encuentre la pregunta generada mostramos este mensaje de alerta
                    dataNotFound();
                }
            }

        });
    }

    //Módulo de Visualización
    private void displayModule(String url) {
        //VideoView videoView = findViewById(R.id.videoView);

        //String finalUrl = url;
        search.setOnClickListener(view -> {

            //viewDatos.setText(finalUrl);

            VideoView videoView = findViewById(R.id.videoView);

            Uri uri = Uri.parse(url);
            videoView.setVideoURI(uri);
            videoView.start();
            videoView.requestFocus();

            //viewDatos.setText(pregunta);
        });
    }

    //Alerta de Pregunta No Efectuada
    private void emptyField() {
        search.setOnClickListener(view -> {
            if(voiceInput.length() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Por favor genera una pregunta");
                builder.setMessage("Para generar una pregunta selecciona el boton microfono").setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "Seguiremos trabajando para mejorar la App", Toast.LENGTH_SHORT).show();
                    }
                }).setCancelable(false).show();
            }
        });
    }

    //Alerta de Datos no Encontrada
    private void dataNotFound() {
        search.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Aviso");
            builder.setMessage("No encontramos la pregunta que deseas traducir").setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "Seguiremos trabajando para mejorar la App", Toast.LENGTH_SHORT).show();
                }
            }).setCancelable(false).show();
        });
    }

}