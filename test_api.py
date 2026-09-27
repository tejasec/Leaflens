import urllib.request
import json
import os

def get_api_key():
    if os.path.exists("local.properties"):
        with open("local.properties", "r") as f:
            for line in f:
                if line.strip().startswith("gemini.api.key="):
                    return line.strip().split("=", 1)[1]
    return os.environ.get("GEMINI_API_KEY", "YOUR_API_KEY_HERE")

key = get_api_key()
url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key={key}"
headers = {
    "Content-Type": "application/json"
}
data = {
    "contents": [{"parts": [{"text": "Explain how AI works in a few words"}]}]
}

req = urllib.request.Request(url, json.dumps(data).encode('utf-8'), headers)
try:
    with urllib.request.urlopen(req) as response:
        print(response.read().decode())
except urllib.error.HTTPError as e:
    print(e.read().decode())
