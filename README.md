Commandes liées à Mosquitto :


Démarrer Mosquitto (serveur MQTT) :

sudo systemctl start mosquitto
Vérifier l'état de Mosquitto :
sudo systemctl status mosquitto
Vérifier les logs de Mosquitto :
sudo journalctl -u mosquitto
Publier un message avec le client Mosquitto :
mosquitto_pub -h localhost -t "topic/maison" -m "Bonjour MQTT"
commandes liée a la camera de raspberry :
1. Mettre à jour le système:
   
 sudo apt update && sudo apt upgrade -y
3. Installer les bibliothèques de la caméra:
sudo apt install -y libcamera-apps python3-picamera2

4. Vérifier et activer la caméra:

sudo nano /boot/config.txt
Vérifie que ces lignes existent (sinon, ajoute-les à la fin) :

camera_auto_detect=1
dtoverlay=vc4-kms-v3d
Sauvegarde et quitte (CTRL + X, puis Y, puis Entrée).::

Puis redémarre le Raspberry Pi :

sudo reboot
5. Tester la caméra: 

libcamera-hello


