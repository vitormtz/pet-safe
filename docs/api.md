# API
Prefixo **/api/v1**

## 📍 Localizações
POST /locations → dispositivo envia sua localização
``` json
	"device_id": "" required
	"latitude": "" required
	"longitude": "" required
	"accuracy": "" 
	"speed": "" 
	"heading": "" 
	"updated_at": "" required
```

## 🔑 Autenticação e Sessão

**POST /auth/register** → criar usuário
``` json
{
	"email":"" required,email
	"password":"" required,min=8
	"full_name":""
	"phone":""
}
```

**POST /auth/login** → autenticar (gera access token + refresh token)
``` json
{
    "email": "", required,email
    "password": "" required
}
```

**POST /auth/refresh** → renovar access token com refresh token
``` json
{
    "refresh_token": "" required
}
```

<!-- **POST /auth/logout** → revogar refresh token -->

## 👤 Usuários
GET /me → obter dados do usuário logado

PATCH /me → atualizar nome, telefone etc do usuário logado.
``` json
{
    "full_name": ""
	"phone": ""
}
```

<!-- Para admins
GET /users/{id} → obter dados do usuário (exceto hash da senha)

PATCH /users/{id} → atualizar nome, telefone etc.

DELETE /users/{id} → excluir usuário -->

## 🐾 Pets

POST /pets → criar pet (vinculado ao usuário autenticado)

``` json
	"name":"" required
	"species":""
	"breed":""
	"microchip_id":""
	"dob":""
```
GET /pets → listar pets do usuário

<!-- GET /pets/{id} → detalhes de um pet

PATCH /pets/{id} → atualizar informações do pet

DELETE /pets/{id} → excluir -->

## 📱 Dispositivos

POST /devices → registrar dispositivo (serial, imei, etc.)
``` json
	"serial_number": "" required
	"imei": ""
	"model": ""
	"firmware": ""
```
<!-- GET /devices → listar dispositivos do usuário -->

<!-- GET /devices/{id} → detalhes -->

<!-- PATCH /devices/{id} → atualizar dados (pet vinculado, firmware, ativo/inativo) -->
<!-- DELETE /devices/{id} → remover dispositivo -->

### Dados dinâmicos de dispositivos:

GET /devices/{id}/status → último status (bateria, última localização, conectividade)

<!-- GET /devices/{id}/battery-history → histórico de bateria

GET /devices/{id}/locations → rota do dispositivo (últimos N pontos) -->


<!-- 
## 🛰️ Geofences

POST /geofences → criar cerca eletrônica (circle: centro + raio)

GET /geofences → listar geofences do usuário

GET /geofences/{id} → detalhes

PATCH /geofences/{id} → atualizar

DELETE /geofences/{id} → excluir

POST /geofences/{id}/devices → vincular dispositivos a geofence

DELETE /geofences/{id}/devices/{device_id} → remover vínculo

## ⚠️ Alertas & Notificações

GET /alerts → listar alertas do usuário

PATCH /alerts/{id}/ack → marcar alerta como reconhecido

GET /notifications → listar notificações enviadas

POST /notifications/test → enviar notificação de teste

## 📊 Eventos

POST /devices/{id}/events → registrar evento (ex: botão de emergência, desligamento)

GET /devices/{id}/events → listar eventos de um dispositivo -->