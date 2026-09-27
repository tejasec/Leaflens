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
url = f"https://generativelanguage.googleapis.com/v1beta/models?key={key}"

req = urllib.request.Request(url)
try:
    with urllib.request.urlopen(req) as response:
        print(response.read().decode())
except urllib.error.HTTPError as e:
    print(f"HTTPError: {e.code}")
    print(e.read().decode())
