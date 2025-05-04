package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTAndroid {
    private static final String MQTT_BROKER = "tcp://192.168.79.84:1883";  // Remplace par l'IP du Raspberry Pi
    private static final String MQTT_TOPIC = "camera/topic";  // Le topic où le Raspberry Pi publie les images
    private MqttClient mqttClient;

    // Méthode pour initialiser la connexion MQTT
    public void connectToBroker(final ImageView imageView) { // Passe l'ImageView en paramètre
        try {
            mqttClient = new MqttClient(MQTT_BROKER, MqttClient.generateClientId(), null);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // Gérer la perte de connexion
                    Log.e("MQTT", "Connexion perdue : " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Recevoir l'image en base64 et logguer l'image reçue
                    // Log pour vérifier que l'image est bien reçue
                    Log.d("MQTT", "Message reçu sur le topic: " + topic);
                    String imageData = new String(message.getPayload());
                    Log.d("MQTT", "Image reçue : " + imageData);
                    // Décoder l'image en base64
                    byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                    // Appeler la méthode pour afficher l'image
                    showImage(decodedBytes, imageView);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Gérer la livraison complète du message
                }
            });

            // Connexion au broker MQTT avec options
            MqttConnectOptions options = new MqttConnectOptions();
            mqttClient.connect(options);

            // S'abonner au topic où le Raspberry Pi envoie les images
            mqttClient.subscribe(MQTT_TOPIC);

        } catch (MqttException e) {
            Log.e("MQTT", "Erreur lors de la connexion ou de l'abonnement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour afficher l'image sur l'UI (le décodage en base64 et l'affichage dans une ImageView)
    private void showImage(final byte[] imageBytes, final ImageView imageView) {
        try {
            // Convertir les bytes en Bitmap
            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            // Assurer que l'UI est mise à jour sur le thread principal
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    // Afficher l'image dans l'ImageView
                    imageView.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
            Log.e("MQTT", "Erreur lors du décodage de l'image : " + e.getMessage());
        }
    }
}
