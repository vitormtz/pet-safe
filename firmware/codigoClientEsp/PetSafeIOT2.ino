#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <HTTPClient.h>
#include <TinyGPSPlus.h>
#include <esp_sleep.h>  // Deep Sleep

// === HARDWARE ===
#define RXD2 16
#define TXD2 17
#define GPS_BAUD 9600
#define LedEnbarcado 2

// === API ===
const char* urlApi = "http://177.44.248.27:8080/api/v1/locations";
const int dispositivoId = 12345;

// === DEEP SLEEP / ENVIO MÚLTIPLO ===
const uint64_t DEEP_SLEEP_SECONDS = 600ULL;  // dorme 1 hora entre ciclos
const int NUM_ENVIO = 3;                     // envia 3 vezes antes de dormir
const int INTERVALO_ENVIO_MS = 1500;         // intervalo entre envios (1,5s)

// === OBJETOS GLOBAIS ===
HardwareSerial gpsSerial(2);
TinyGPSPlus gps;
WebServer server(80);
Preferences prefs;

String savedSsid, savedPass;
unsigned long ultimoEnvio = 0;
const unsigned long intervaloDeEnvio = 3000; // tempo mínimo após boot para começar a enviar
unsigned long apStart = 0;
unsigned long blinkTimer = 0;
bool ledState = false;
int enviosFeitos = 0;  // conta quantos envios já fizemos neste ciclo

const char* AP_PASS = "setup1234";                     
const unsigned long AP_TIMEOUT_MS = 5UL * 60UL * 1000UL; // 5 min

// === HTML ===
const char* htmlForm = R"HTML(
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1"/>
<title>PetSafe — Configurar Wi-Fi</title>
<style>
  :root{
    /* paleta “pet safe”: verdes/água suaves */
    --bg:#f6fbf8; --card:#ffffff; --ink:#0f172a; --muted:#6b7280;
    --pri:#10b981; --pri-h:#0ea371; --pri-xx:#34d399;
    --warn:#f59e0b; --ok:#16a34a; --err:#dc2626; --bd:#e5e7eb;
    --radius:18px;
  }
  @media (prefers-color-scheme:dark){
    :root{ --bg:#0b1110; --card:#0f1715; --ink:#e5f0ed; --muted:#99a3a0; --bd:#1f2a27; --pri:#34d399; --pri-h:#22c08d; }
    body{background: radial-gradient(900px 600px at 50% -10%, #0d1916 0%, var(--bg) 60%)}
  }
  *{box-sizing:border-box}
  body{margin:0;background:var(--bg);color:var(--ink);font:16px/1.45 system-ui, -apple-system, Segoe UI, Roboto, Arial}
  .wrap{max-width:560px;margin:28px auto;padding:0 16px}
  .card{background:var(--card);border:1px solid var(--bd);border-radius:var(--radius);box-shadow:0 10px 28px rgba(0,0,0,.06);overflow:hidden}
  .head{display:flex;gap:12px;align-items:center;padding:18px 20px;border-bottom:1px solid var(--bd)}
  .logo{width:32px;height:32px;display:grid;place-items:center;border-radius:10px;background:linear-gradient(135deg,var(--pri),var(--pri-xx))}
  .logo svg{width:20px;height:20px;fill:#fff}
  h1{font-size:18px;margin:0}
  .body{padding:18px 20px}
  label{display:block;font-weight:600;margin:10px 0 6px}
  input,select,button{width:100%;padding:13px 14px;border:1px solid var(--bd);border-radius:14px;background:transparent;color:var(--ink);outline:none}
  input::placeholder{color:var(--muted)}
  input,select{transition:border .15s, box-shadow .15s}
  input:focus,select:focus{border-color:var(--pri);box-shadow:0 0 0 4px color-mix(in srgb, var(--pri) 22%, transparent)}
  .row{display:grid;grid-template-columns:1fr;gap:12px}
  .actions{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-top:12px}
  .btn{background:var(--pri);color:#fff;border:none;cursor:pointer;font-weight:700;letter-spacing:.2px;transition:transform .06s ease, background .15s}
  .btn:hover{background:var(--pri-h)}
  .btn:active{transform:translateY(1px)}
  .btn-sec{background:transparent;color:var(--ink);border:1px dashed var(--bd)}
  .muted{color:var(--muted);font-size:13px;margin-top:12px}
  .status{margin-top:14px;padding:12px;border:1px solid var(--bd);border-radius:14px;background:color-mix(in srgb, var(--card) 86%, var(--pri) 14%)}
  .badge{display:inline-block;padding:4px 10px;border-radius:999px;font-size:12px;border:1px solid var(--bd);margin-left:8px}
  .badge.ok{background:color-mix(in srgb, var(--ok) 18%, transparent);border-color:color-mix(in srgb, var(--ok) 60%, var(--bd))}
  .badge.err{background:color-mix(in srgb, var(--err) 18%, transparent);border-color:color-mix(in srgb, var(--err) 60%, var(--bd))}
  .help{font-size:12px;color:var(--muted)}
  .footer{padding:14px 20px;border-top:1px solid var(--bd);display:flex;justify-content:space-between;align-items:center}
  .dot{width:10px;height:10px;border-radius:50%;background:var(--warn);box-shadow:0 0 10px var(--warn)}
</style>
</head>
<body>
  <div class="wrap">
    <div class="card" role="main" aria-labelledby="title">
      <div class="head">
        <div class="logo" aria-hidden="true">
          <!-- patinha em SVG -->
          <svg viewBox="0 0 24 24"><path d="M7.5 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5zm9 0a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5zM5 14.5c0-2.2 2.7-3.9 7-3.9s7 1.7 7 3.9c0 2.1-2.7 3.8-7 3.8S5 16.6 5 14.5zm-1-4a2.2 2.2 0 1 1 0-4.4 2.2 2.2 0 0 1 0 4.4zm16 0a2.2 2.2 0 1 1 0-4.4 2.2 2.2 0 0 1 0 4.4z"/></svg>
        </div>
        <h1 id="title">PetSafe Tracker — Configurar Wi-Fi</h1>
      </div>

      <div class="body">
        <form id="f" action="/save" method="POST" class="row" autocomplete="off">
          <div>
            <label for="ssid">SSID</label>
            <input name="ssid" id="ssid" placeholder="Nome da rede" required aria-required="true" />
            <div class="help">Toque em “Procurar redes” para preencher automaticamente.</div>
          </div>
          <div>
            <label for="pass">Senha</label>
            <input name="pass" id="pass" type="password" placeholder="Senha (pode ser vazia)"/>
          </div>
          <button class="btn" type="submit">Salvar e conectar</button>
        </form>

        <div class="actions" aria-label="Ações auxiliares">
          <button class="btn-sec" id="btnScan">Procurar redes</button>
          <button class="btn-sec" id="btnErase">Apagar credenciais</button>
        </div>

        <div id="status" class="status">
          <b>Status:</b> <span id="st">Carregando…</span>
          <span id="pill" class="badge">—</span>
          <div class="help" id="extra"></div>
        </div>

        <p class="muted">Esta rede é temporária. Após salvar, o dispositivo tentará conectar na sua rede Wi-Fi.</p>
      </div>

      <div class="footer">
        <span class="help">• PetSafe</span>
        <span class="dot" id="led" aria-hidden="true"></span>
      </div>
    </div>
  </div>

<script>
const st   = document.getElementById('st');
const pill = document.getElementById('pill');
const led  = document.getElementById('led');
const ssidInput = document.getElementById('ssid');
const extra = document.getElementById('extra');

async function refreshStatus(){
  try{
    const r = await fetch('/status');
    if(!r.ok) throw 0;
    const j = await r.json();
    const ok = !!j.connected;

    st.textContent = ok ? 'Conectado' : 'Desconectado';
    pill.textContent = ok ? 'Online' : 'Offline';
    pill.className = 'badge ' + (ok ? 'ok' : 'err');
    led.style.background = ok ? 'var(--ok)' : 'var(--warn)';
    led.style.boxShadow  = '0 0 10px ' + (ok ? 'var(--ok)' : 'var(--warn)');

    extra.textContent = 'SSID: ' + (j.ssid || '(nenhum)') + (ok ? '  •  IP: '+ j.ip + '  •  RSSI: ' + j.rssi + ' dBm' : '');
  }catch(e){
    st.textContent = 'Sem resposta do dispositivo';
    pill.textContent = 'Indefinido';
    pill.className = 'badge err';
    led.style.background = 'var(--err)';
    led.style.boxShadow  = '0 0 10px var(--err)';
    extra.textContent = '';
  }
}

document.getElementById('btnScan').onclick = async (ev)=>{
  ev.preventDefault();
  try{
    const r = await fetch('/scan'); const arr = await r.json();
    arr.sort((a,b)=>b.rssi-a.rssi);
    const nomes = arr.map(n=>n.ssid).filter(Boolean);
    if(nomes.length){
      const escolhido = prompt('Escolha um SSID:\n' + nomes.join('\n'));
      if(escolhido){ ssidInput.value = escolhido; ssidInput.focus(); }
    } else {
      alert('Nenhuma rede encontrada.');
    }
  }catch(_){ alert('Falha ao escanear redes.'); }
};

document.getElementById('btnErase').onclick = async (ev)=>{
  ev.preventDefault();
  if(!confirm('Apagar credenciais salvas? O dispositivo irá reiniciar.')) return;
  await fetch('/erase',{method:'POST'});
  alert('Credenciais apagadas. Reiniciando...');
};

refreshStatus();
setInterval(refreshStatus, 3000);
</script>
</body>
</html>
)HTML";


// === PÁGINAS ===
void handleRoot() { server.send(200, "text/html", htmlForm); }

void handleSave() {
  if (server.method() != HTTP_POST) { server.send(405, "text/plain", "Method Not Allowed"); return; }
  String ssid = server.arg("ssid");
  String pass = server.arg("pass");

  prefs.begin("wifi", false);
  prefs.putString("ssid", ssid);
  prefs.putString("pass", pass);
  prefs.end();

  server.send(200, "text/html",
              "<html><body><h3>Salvo! Tentando conectar...</h3><p>Você já pode fechar esta página.</p></body></html>");

  delay(300);
  server.stop();
  WiFi.softAPdisconnect(true);
  apStart = 0;

  savedSsid = ssid;
  savedPass = pass;

  WiFi.mode(WIFI_MODE_STA);
  WiFi.begin(savedSsid.c_str(), savedPass.length() ? savedPass.c_str() : nullptr);
}

// === NOVAS PÁGINAS EXTRAS ===

// /status → retorna JSON
void handleStatus() {
  String s = "{";
  s += "\"ssid\":\"" + (savedSsid.length()? savedSsid : String("(nenhum)")) + "\",";
  s += "\"connected\":" + String(WiFi.status() == WL_CONNECTED ? "true" : "false") + ",";
  s += "\"ip\":\"" + WiFi.localIP().toString() + "\",";
  s += "\"rssi\":" + String(WiFi.status()==WL_CONNECTED ? WiFi.RSSI() : 0) + ",";
  s += "\"uptime_ms\":" + String(millis());
  s += "}";
  server.send(200, "application/json", s);
}

// /erase → apaga credenciais
void handleErase() {
  prefs.begin("wifi", false);
  prefs.clear();
  prefs.end();
  server.send(200, "text/plain", "Credenciais apagadas. Reiniciando...");
  delay(500);
  ESP.restart();
}

// /scan → lista redes
void handleScan() {
  int n = WiFi.scanNetworks();
  String out = "[";
  for (int i = 0; i < n; i++) {
    if (i) out += ",";
    out += "{\"ssid\":\"" + WiFi.SSID(i) + "\",\"rssi\":" + String(WiFi.RSSI(i)) + "}";
  }
  out += "]";
  server.send(200, "application/json", out);
  WiFi.scanDelete();
}

// Redireciona tudo pro "/"
void handleNotFound() { server.sendHeader("Location", "/", true); server.send(302, "text/plain", ""); }

// === HELPER DE AP ===
void startConfigAP() {
  uint8_t mac[6]; WiFi.macAddress(mac);
  char apName[32]; sprintf(apName, "ESP-SETUP-%02X%02X", mac[4], mac[5]);

  WiFi.mode(WIFI_MODE_AP);
  if (strlen(AP_PASS)) WiFi.softAP(apName, AP_PASS);
  else WiFi.softAP(apName);

  IPAddress ip = WiFi.softAPIP();
  Serial.printf("[AP] '%s' pronto. Acesse: http://%s/\n", apName, ip.toString().c_str());

  server.on("/", handleRoot);
  server.on("/save", HTTP_POST, handleSave);
  server.on("/status", HTTP_GET, handleStatus);
  server.on("/scan", HTTP_GET, handleScan);
  server.on("/erase", HTTP_POST, handleErase);
  server.onNotFound(handleNotFound);
  server.begin();

  apStart = millis();
  digitalWrite(LedEnbarcado, LOW);
}

// === CREDENCIAIS SALVAS ===
bool loadSavedWifi() {
  prefs.begin("wifi", true);
  savedSsid = prefs.getString("ssid", "");
  savedPass = prefs.getString("pass", "");
  prefs.end();
  return savedSsid.length() > 0;
}

// === CONECTA ===
bool tryConnectSaved(unsigned long timeoutMs = 20000) {
  if (!savedSsid.length()) return false;

  WiFi.mode(WIFI_MODE_STA);
  WiFi.begin(savedSsid.c_str(), savedPass.length() ? savedPass.c_str() : nullptr);

  unsigned long t0 = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - t0 < timeoutMs) {
    digitalWrite(LedEnbarcado, HIGH); delay(120);
    digitalWrite(LedEnbarcado, LOW);  delay(120);
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.printf("[WIFI] OK '%s' | IP: %s | RSSI: %d dBm\n",
                  savedSsid.c_str(), WiFi.localIP().toString().c_str(), WiFi.RSSI());
    digitalWrite(LedEnbarcado, HIGH);
    return true;
  }
  Serial.println("[WIFI] Falhou conectar com credenciais salvas.");
  return false;
}

// === conectaWifi() ===
void conectaWifi() {
  if (WiFi.status() == WL_CONNECTED) {
    digitalWrite(LedEnbarcado, HIGH);
    return;
  }
  if (loadSavedWifi()) {
    if (tryConnectSaved()) return;
  }
  Serial.println("[WIFI] Abrindo AP de configuração...");
  startConfigAP();
}

// === Deep Sleep ===
void entraDeepSleep() {
  Serial.printf(">> Entrando em deep sleep por %llu segundos...\n",
                (unsigned long long)DEEP_SLEEP_SECONDS);
  digitalWrite(LedEnbarcado, LOW);
  esp_sleep_enable_timer_wakeup(DEEP_SLEEP_SECONDS * 1000000ULL);
  Serial.flush();
  esp_deep_sleep_start();
}

// === Envia GPS para API ===
void EnviaParaApi(double lat, double lon, double acc, double spd, double heading) {
  conectaWifi();
  if (WiFi.status() != WL_CONNECTED) { Serial.println("[HTTP] Sem Wi-Fi, abortando."); return; }

  HTTPClient http;
  http.setTimeout(12000);
  http.begin(urlApi);
  http.addHeader("Content-Type", "application/json");

  unsigned long tempoLigado = millis();
  String pacote = "{";
  pacote += "\"device_id\":" + String(dispositivoId) + ",";
  pacote += "\"latitude\":" + String(lat, 6) + ",";
  pacote += "\"longitude\":" + String(lon, 6) + ",";
  pacote += "\"accuracy\":" + String(acc, 2) + ",";
  pacote += "\"speed\":" + String(spd, 2) + ",";
  pacote += "\"heading\":" + String(heading, 2) + ",";
  pacote += "\"updated_at\":" + String(tempoLigado);
  pacote += "}";

  Serial.println("[HTTP] Enviando payload:");
  Serial.println(pacote);

  int code = http.POST(pacote);
  if (code > 0) {
    Serial.printf("[HTTP] Resposta: %d\n", code);
    Serial.println(http.getString());
  } else {
    Serial.printf("[HTTP] Erro POST: %s\n", http.errorToString(code).c_str());
  }
  http.end();
}

// === SETUP ===
void setup() {
  Serial.begin(115200);
  pinMode(LedEnbarcado, OUTPUT);
  digitalWrite(LedEnbarcado, LOW);

  gpsSerial.begin(GPS_BAUD, SERIAL_8N1, RXD2, TXD2);
  Serial.println("Inicializando GPS e Wi-Fi...");
  conectaWifi();

  enviosFeitos = 0; // novo ciclo após boot / wake
}

// === LOOP ===
void loop() {
  // ===== Se estiver em modo AP =====
  if (WiFi.getMode() == WIFI_MODE_AP) {
    server.handleClient();
    // Pisca LED a cada 250ms sem travar
    if (millis() - blinkTimer > 250) {
      blinkTimer = millis();
      ledState = !ledState;
      digitalWrite(LedEnbarcado, ledState);
    }
    // Timeout do AP
    if (apStart && (millis() - apStart > AP_TIMEOUT_MS)) {
      Serial.println("[AP] Timeout de provisioning. Reiniciando...");
      ESP.restart();
    }
    return;
  }

  // ===== GPS + envio =====
  while (gpsSerial.available() > 0) gps.encode(gpsSerial.read());

  // Só começa a pensar em enviar depois de intervaloDeEnvio
  if (millis() - ultimoEnvio >= intervaloDeEnvio && enviosFeitos < NUM_ENVIO) {
    if (gps.location.isValid() && gps.location.age() < 3000) {
      double lat = gps.location.lat();
      double lon = gps.location.lng();
      double acc = gps.hdop.hdop();
      double spd = gps.speed.kmph();
      double heading = gps.course.deg();
      

      Serial.println("🌎 Enviando coordenadas...");
      Serial.printf("Lat: %.6f | Lon: %.6f | Vel: %.2f km/h | HDOP: %.2f | Dir: %.2f°\n",
                    lat, lon, spd, acc, heading);

      enviosFeitos++;
      Serial.printf("📡 Envio %d de %d neste ciclo...\n", enviosFeitos, NUM_ENVIO);
      EnviaParaApi(lat, lon, acc, spd, heading);

      ultimoEnvio = millis(); // para espaçar os envios

      // Se já enviou 3x → dorme
      if (enviosFeitos >= NUM_ENVIO) {
        entraDeepSleep();
      }
    } else {
      Serial.println("[GPS] Sem fix válido ainda...");
    }
  }
}
