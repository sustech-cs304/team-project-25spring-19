from flask import Flask, request, jsonify
import subprocess
from flask_cors import CORS

app = Flask(__name__)
CORS(app) 

# Mock from your ConferenceClient usage
from conf_client import ConferenceClient
client = ConferenceClient()

@app.route("/conferences", methods=["GET"])
def list_conferences():
    confs = client.ask_for_conference_list()
    return jsonify(confs or [])

@app.route("/create", methods=["POST"])
def create_conference():
    data = request.get_json()
    username = data.get("username")
    conf_name = data.get("conference_name")
    cmd = ["python", "ui.py", "-username", username, "-create_conference", conf_name]
    subprocess.Popen(cmd)
    return jsonify({"status": "creating", "command": cmd})

@app.route("/join", methods=["POST"])
def join_conference():
    data = request.get_json()
    username = data.get("username")
    conf_name = data.get("conference_name")
    cmd = ["python", "ui.py", "-username", username, "-conference_name", conf_name]
    subprocess.Popen(cmd)
    return jsonify({"status": "joining", "command": cmd})

if __name__ == "__main__":
    app.run(port=5001)
