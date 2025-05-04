package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class Test extends AppCompatActivity {
    private static final String TAG = "MQTT";
    private static final String MQTT_BROKER = "tcp://192.168.79.84:1883"; // ← IP du Raspberry Pi
    private static final String MQTT_TOPIC = "camera/topic";
    private ImageView imageView;
    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);
        imageView = findViewById(R.id.imageView);  // Assure-toi d'utiliser le bon ID pour l'ImageView
        connectToMqttBroker();
    }

    private void connectToMqttBroker() {
        Toast.makeText(this, "Connexion MQTT en cours...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                mqttClient = new MqttClient(MQTT_BROKER, MqttClient.generateClientId(), null);

                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e(TAG, "Connexion perdue : " + cause.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(Test.this, "Connexion perdue", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        Log.d(TAG, "📨 Message reçu sur le topic: " + topic);

                        try {
                            String imageData = new String(message.getPayload(), StandardCharsets.UTF_8);
                            Log.d(TAG, "✅ Données Base64 reçues.");
                            Log.d(TAG, "📦 Taille du payload: " + message.getPayload().length);
                            Log.d(TAG, "🔍 Début de l'image reçue : " + imageData.substring(0, Math.min(100, imageData.length())));

                            // Décodage Base64
                            byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);

                            // Conversion en Bitmap
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            if (bitmap != null) {
                                Log.d(TAG, "✅ Bitmap décodé avec succès : " + bitmap.getWidth() + "x" + bitmap.getHeight());
                                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                            } else {
                                Log.e(TAG, "❌ Échec du décodage en bitmap.");
                                runOnUiThread(() ->
                                        Toast.makeText(Test.this, "Échec du décodage de l'image", Toast.LENGTH_SHORT).show()
                                );
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Erreur lors du traitement du message MQTT", e);
                            runOnUiThread(() ->
                                    Toast.makeText(Test.this, "Erreur image MQTT : " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                        }
                    }


                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.d(TAG, "Livraison complète.");
                    }
                });

                MqttConnectOptions options = new MqttConnectOptions();
                mqttClient.connect(options);
                Log.d(TAG, "Connexion MQTT réussie.");
                mqttClient.subscribe(MQTT_TOPIC);
                Log.d(TAG, "Abonné au topic : " + MQTT_TOPIC);

                runOnUiThread(() ->
                        Toast.makeText(Test.this, "Connecté au broker MQTT", Toast.LENGTH_SHORT).show()
                );

            } catch (MqttException e) {
                Log.e(TAG, "Erreur MQTT : " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(Test.this, "Erreur MQTT : " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}