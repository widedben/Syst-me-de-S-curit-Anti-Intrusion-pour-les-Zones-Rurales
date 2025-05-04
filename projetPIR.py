import RPi.GPIO as GPIO
import time
from picamera2 import Picamera2
from datetime import datetime
import io
import base64
import paho.mqtt.client as mqtt
from PIL import Image  # Assurez-vous que cette bibliothèque est installée

# Initialisation GPIO
PIR_PIN = 15
GPIO.setmode(GPIO.BCM)
GPIO.setup(PIR_PIN, GPIO.IN)
GPIO.setwarnings(False)  # Désactiver les avertissements GPIO

# Initialisation de la caméra
picam2 = Picamera2()
picam2.configure(picam2.create_still_configuration())

# Configuration MQTT
MQTT_BROKER = "192.168.79.84"  # Remplace par l'IP de ton Raspberry Pi
MQTT_PORT = 1883
MQTT_TOPIC = "camera/topic"
MQTT_CLIENT_ID = "raspberry-client"

# Verrouillage pour éviter la double détection rapide
lock = False

# Fonction pour capturer l'image et la convertir en base64
def capture_image(image_path):
    try:
        picam2.start()
        time.sleep(2)  # Laisser le temps à la caméra de se stabiliser
        picam2.capture_file(image_path)
        picam2.stop()
        
        # Convertir l'image en base64 pour l'envoi via MQTT
        image = Image.open(image_path)
        buffered = io.BytesIO()
        image.save(buffered, format="JPEG")
        img_str = base64.b64encode(buffered.getvalue()).decode('utf-8')
        return img_str
    except Exception as e:
        print(f"Erreur lors de la capture de l'image: {e}")
        return None

# Fonction de connexion MQTT
def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")

# Initialisation du client MQTT
client = mqtt.Client(MQTT_CLIENT_ID)
client.on_connect = on_connect

# Connexion au broker
client.connect(MQTT_BROKER, MQTT_PORT, 60)

# Démarrer la boucle MQTT en arrière-plan
client.loop_start()

print("🕵️ Attente de détection par le capteur PIR...")

try:
    while True:
        if GPIO.input(PIR_PIN) and not lock:  # Vérifie si le verrou n'est pas activé
            print("📢 Mouvement détecté ! Démarrage de la caméra...")
            
            # Activer le verrou pour éviter une double détection rapide
            lock = True

            # Création du nom de fichier avec horodatage
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            image_path = f"/home/rasbypi/image_{timestamp}.jpg"
            
            # Capture de l'image
            image_data = capture_image(image_path)
            if image_data:
                # Publier l'image capturée sur le topic MQTT
                client.publish(MQTT_TOPIC, image_data)
                print(f"📸 Image capturée et envoyée au topic MQTT : {image_path}")
            else:
                print("❌ Impossible de capturer l'image.")
            
            # Pause pour éviter plusieurs déclenchements instantanés
            time.sleep(5)  # La caméra ne sera pas déclenchée trop souvent

            # Réinitialiser la variable de verrouillage après le traitement de l'image
            lock = False

        time.sleep(0.1)

except KeyboardInterrupt:
    print("🛑 Arrêt du script.")
    GPIO.cleanup()
    client.loop_stop()  # Arrêter la boucle MQTT proprement
