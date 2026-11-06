import argparse
import datetime
import json
import math
import os
import random
import sys
import time
import urllib.error
import urllib.parse
import urllib.request


def http_post(url, data=None, headers=None):
    req_headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if headers:
        req_headers.update(headers)
    body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=body, headers=req_headers, method="POST")
    try:
        with urllib.request.urlopen(req) as resp:
            content = resp.read().decode("utf-8")
            return resp.status, json.loads(content) if content else {}
    except urllib.error.HTTPError as e:
        err_content = e.read().decode("utf-8")
        try:
            err_json = json.loads(err_content)
        except Exception:
            err_json = {"detail": err_content}
        return e.code, err_json


def send_measurement(api_url, api_key, value, timestamp=None):
    endpoint = f"{api_url.rstrip('/')}/api/measurements"
    payload = {"value": round(float(value), 2)}
    if timestamp:
        payload["timestamp"] = timestamp

    status, resp = http_post(endpoint, payload, headers={"X-API-Key": api_key})
    if status == 201:
        print(f"[OK] Measurement recorded: id={resp.get('id')}, value={resp.get('value')}, timestamp={resp.get('timestamp')}")
        return True, resp
    else:
        title = resp.get("title", "Error")
        detail = resp.get("detail", str(resp))
        print(f"[REJECTED] HTTP {status} {title}: {detail}")
        return False, resp


def run_sine_generator(api_url, api_key, min_val, max_val, count, interval, historical):
    mid = (min_val + max_val) / 2.0
    amp = (max_val - min_val) / 2.0
    now = datetime.datetime.now(datetime.timezone.utc)

    for i in range(count):
        val = mid + amp * math.sin(2.0 * math.pi * i / max(1, count))
        ts = None
        if historical:
            dt = now - datetime.timedelta(seconds=(count - i) * interval)
            ts = dt.strftime("%Y-%m-%dT%H:%M:%SZ")
        send_measurement(api_url, api_key, val, ts)
        if not historical and interval > 0 and i < count - 1:
            time.sleep(interval)


def run_random_generator(api_url, api_key, min_val, max_val, count, interval, historical):
    now = datetime.datetime.now(datetime.timezone.utc)
    for i in range(count):
        val = random.uniform(min_val, max_val)
        ts = None
        if historical:
            dt = now - datetime.timedelta(seconds=(count - i) * interval)
            ts = dt.strftime("%Y-%m-%dT%H:%M:%SZ")
        send_measurement(api_url, api_key, val, ts)
        if not historical and interval > 0 and i < count - 1:
            time.sleep(interval)


def run_openmeteo(api_url, api_key, metric, count, interval):
    geo_url = "https://api.open-meteo.com/v1/forecast?latitude=52.23&longitude=21.01&current=temperature_2m,relative_humidity_2m,surface_pressure"
    for i in range(count):
        try:
            req = urllib.request.Request(geo_url, headers={"User-Agent": "ZaiSensorEmulator/1.0"})
            with urllib.request.urlopen(req) as r:
                data = json.loads(r.read().decode("utf-8"))
            current = data.get("current", {})
            if metric == "temperature":
                val = current.get("temperature_2m")
            elif metric == "humidity":
                val = current.get("relative_humidity_2m")
            elif metric == "pressure":
                val = current.get("surface_pressure")
            else:
                val = current.get("temperature_2m")

            if val is not None:
                print(f"[Open-Meteo] Live {metric} in Warsaw: {val}")
                send_measurement(api_url, api_key, val)
        except Exception as e:
            print(f"[Open-Meteo] Error fetching weather data: {e}")

        if interval > 0 and i < count - 1:
            time.sleep(interval)


def run_seed_mode(api_url, username, password):
    print(f"Logging in to {api_url} as {username}...")
    status, login_resp = http_post(f"{api_url.rstrip('/')}/api/auth/login", {"username": username, "password": password})
    if status != 200 or "accessToken" not in login_resp:
        print(f"Failed to login: HTTP {status} {login_resp}")
        sys.exit(1)

    token = login_resp["accessToken"]
    auth_header = {"Authorization": f"Bearer {token}"}

    sample_series = [
        {"name": "Temperatura zewnętrzna", "minValue": -30.0, "maxValue": 50.0, "color": "#e74c3c", "unit": "°C", "icon": "thermometer"},
        {"Wilgotność powietrza": "Wilgotność powietrza", "name": "Wilgotność powietrza", "minValue": 0.0, "maxValue": 100.0, "color": "#3498db", "unit": "%", "icon": "droplet"},
        {"name": "Stężenie pyłu PM2.5", "minValue": 0.0, "maxValue": 500.0, "color": "#2ecc71", "unit": "µg/m³", "icon": "wind"}
    ]

    now = datetime.datetime.now(datetime.timezone.utc)

    for s_info in sample_series:
        payload = {
            "name": s_info["name"],
            "minValue": s_info["minValue"],
            "maxValue": s_info["maxValue"],
            "color": s_info["color"],
            "unit": s_info.get("unit"),
            "icon": s_info.get("icon")
        }
        st, created_series = http_post(f"{api_url.rstrip('/')}/api/series", payload, auth_header)
        if st != 201:
            print(f"Failed to create series {s_info['name']}: {created_series}")
            continue

        series_id = created_series["id"]
        print(f"Created series: {created_series['name']} (id={series_id})")

        sensor_name = f"Czujnik-{s_info['name']}"
        st_sen, created_sensor = http_post(f"{api_url.rstrip('/')}/api/sensors", {"name": sensor_name, "seriesId": series_id}, auth_header)
        if st_sen != 201:
            print(f"Failed to create sensor: {created_sensor}")
            continue

        api_key = created_sensor["apiKey"]
        print(f"Registered sensor: {sensor_name} (id={created_sensor['id']})")

        min_v = s_info["minValue"]
        max_v = s_info["maxValue"]
        mid = (min_v + max_v) / 2.0
        amp = (max_v - min_v) * 0.35

        print(f"Sending 20 historical points for {s_info['name']}...")
        for i in range(20):
            val = mid + amp * math.sin(2.0 * math.pi * i / 20.0)
            val += random.uniform(-amp * 0.1, amp * 0.1)
            val = max(min_v, min(max_v, val))
            dt = now - datetime.timedelta(hours=(20 - i))
            ts = dt.strftime("%Y-%m-%dT%H:%M:%SZ")
            send_measurement(api_url, api_key, val, ts)

    print("Sample data seed completed successfully (F11 fulfilled).")


def main():
    parser = argparse.ArgumentParser(description="IoT Sensor Emulator and Data Generator (ZAI 26Z)")
    parser.add_argument("--api", default=os.getenv("API_URL", "http://localhost:8080"), help="Base API URL")
    parser.add_argument("--key", default=os.getenv("SENSOR_API_KEY"), help="Sensor API key")
    parser.add_argument("--mode", choices=["sine", "random", "openmeteo"], default="sine", help="Generation pattern")
    parser.add_argument("--min", type=float, default=15.0, help="Minimum value")
    parser.add_argument("--max", type=float, default=25.0, help="Maximum value")
    parser.add_argument("--count", type=int, default=20, help="Number of measurements to send")
    parser.add_argument("--interval", type=float, default=1.0, help="Interval in seconds between measurements")
    parser.add_argument("--historical", action="store_true", help="Send timestamps from past")
    parser.add_argument("--metric", choices=["temperature", "humidity", "pressure"], default="temperature", help="Metric for openmeteo mode")
    parser.add_argument("--seed", action="store_true", help="Seed 3 sample series with 20 measurements each (F11)")
    parser.add_argument("--user", default=os.getenv("ADMIN_USERNAME", "admin"), help="Admin username for seed mode")
    parser.add_argument("--pass", dest="password", default=os.getenv("ADMIN_PASSWORD", "Admin123!"), help="Admin password for seed mode")

    args = parser.parse_args()

    if args.seed:
        run_seed_mode(args.api, args.user, args.password)
        return

    if not args.key:
        print("Error: --key or SENSOR_API_KEY environment variable is required (unless using --seed).")
        sys.exit(1)

    if args.mode == "openmeteo":
        run_openmeteo(args.api, args.key, args.metric, args.count, args.interval)
    elif args.mode == "sine":
        run_sine_generator(args.api, args.key, args.min, args.max, args.count, args.interval, args.historical)
    elif args.mode == "random":
        run_random_generator(args.api, args.key, args.min, args.max, args.count, args.interval, args.historical)


if __name__ == "__main__":
    main()
