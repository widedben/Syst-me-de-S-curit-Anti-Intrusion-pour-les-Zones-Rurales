package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class hall_dentree extends AppCompatActivity {

    private static final String TAG = "MQTT";
    private static final String MQTT_BROKER = "tcp://192.168.79.84:1883"; // Remplace par l'adresse de ton Raspberry Pi
    private static final String MQTT_TOPIC = "camera/topic";
    private MqttClient mqttClient;
    private static final String CHANNEL_ID = "mqtt_notifications"; // ID du canal de notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_dentree);
        findViewById(R.id.imageView5).setOnClickListener(v -> {
            Log.d(TAG, "🖱️ ImageView cliqué. Début du test de notification...");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Test")
                    .setContentText("Simple notification")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Log.d(TAG, "✅ NotificationBuilder créé avec succès.");

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(999, builder.build());
                Log.d(TAG, "📬 Notification test envoyée avec succès (ID = 999).");
            } else {
                Log.e(TAG, "❌ NotificationManager est null !");
            }
        });




        createNotificationChannel();

        // Démarre la connexion MQTT sur un thread en arrière-plan
        new ConnectMqttTask().execute();
    }

    // Classe AsyncTask pour la connexion MQTT en arrière-plan
    private class ConnectMqttTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mqttClient = new MqttClient(MQTT_BROKER, MqttClient.generateClientId(), null);

                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e(TAG, "Connexion perdue : " + cause.getMessage());
                        runOnUiThread(() -> Toast.makeText(hall_dentree.this, "Connexion perdue", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        Log.d(TAG, "📨 Message reçu sur le topic: " + topic);
                        try {
                            // Décoder l'image Base64
                            byte[] decodedBytes = Base64.decode(message.getPayload(), Base64.DEFAULT);
                            Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            Log.d(TAG, "✅ Image reçue et décodée.");

                            // Afficher la notification avec l'image
                            runOnUiThread(() -> showNotification(decodedImage));

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Erreur lors du traitement du message MQTT", e);
                            runOnUiThread(() -> Toast.makeText(hall_dentree.this, "Erreur MQTT : " + e.getMessage(), Toast.LENGTH_LONG).show());
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

            } catch (MqttException e) {
                Log.e(TAG, "Erreur MQTT : " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(hall_dentree.this, "Erreur MQTT : " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Actions après la connexion MQTT
            Log.d(TAG, "Connexion MQTT terminée.");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Camera Notifications";
            String description = "Notifications pour les images MQTT";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Log.d(TAG, "NotificationChannel créé !");
        }
    }

    private void showNotification(Bitmap imageBitmap) {
        Log.d(TAG, "Test de notification démarré...");

        try {
            // Crée une notification simple pour tester
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)  // Icône par défaut Android
                    .setContentTitle("Message MQTT Reçu")
                    .setContentText("Une nouvelle image a été reçue.")  // Message textuel
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true); // La notification se ferme quand on la clique

            // Si l'image n'est pas nulle, ajoute-la
            if (imageBitmap != null) {
                Log.d(TAG, "Une image valide a été reçue.");
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(imageBitmap)); // Affichage de l'image
            } else {
                Log.d(TAG, "Aucune image reçue, notification sans image.");
            }

            // Log de l'ID de la notification pour vérification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());

            Log.d(TAG, "Notification envoyée avec succès.");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'envoi de la notification", e);
        }
    }



}
