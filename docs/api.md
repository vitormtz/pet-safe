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
	"updated_at": "" required "epoch ms"
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

PATCH /me/password → alterar senha do usuário logado
``` json
{
    "current_password": "" required
    "new_password": "" required,min=8
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


GET /pets/{id} → detalhes de um pet

PATCH /pets/{id} → atualizar informações do pet
``` json
	"name":""
	"species":""
	"breed":""
	"microchip_id":""
	"dob":""
```
DELETE /pets/{id} → excluir pet

## 📱 Dispositivos

POST /devices → registrar dispositivo (serial, imei, etc.)
``` json
	"serial_number": "" required
	"imei": ""
	"model": ""
	"firmware": ""
```
GET /devices → listar dispositivos do usuário

GET /devices/{id} → detalhes

PATCH /devices/{id} → atualizar dados (pet vinculado, firmware, ativo/inativo)

DELETE /devices/{id} → remover dispositivo

### Dados dinâmicos de dispositivos:

GET /devices/{id}/status → último status (bateria, última localização, conectividade)

GET /devices/{id}/locations/{n limit} → rota do dispositivo (últimos N pontos)

<!-- GET /devices/{id}/battery-history → histórico de bateria -->

## 🛰️ Geofences

⚠️ **IMPORTANTE**: Cada usuário pode ter apenas **1 geofence**. O geofence é automaticamente associado a todos os dispositivos do usuário.

**POST /geofence** → criar geofence (área segura circular)
``` json
{
    "name": "" required,
    "latitude": "" required (numeric),
    "longitude": "" required (numeric),
    "radius_m": "" required (integer, raio em metros)
}
```
**Retorno de sucesso:**
``` json
{
    "data": {
        "id": 1,
        "owner_id": 2,
        "name": "Casa",
        "latitude": -23.550520,
        "longitude": -46.633308,
        "radius_m": 100,
        "active": true,
        "created_at": "2025-12-06T14:30:00Z"
    }
}
```
**Retorno de erro (já existe geofence):**
``` json
{
    "error": "Você já possui um geofence cadastrado. Delete o existente para criar um novo."
}
```

**GET /geofence** → obter o geofence do usuário
``` json
{
    "data": {
        "id": 1,
        "owner_id": 2,
        "name": "Casa",
        "latitude": -23.550520,
        "longitude": -46.633308,
        "radius_m": 100,
        "active": true,
        "created_at": "2025-12-06T14:30:00Z"
    }
}
```

**PATCH /geofence** → atualizar geofence do usuário
``` json
{
    "name": "" (opcional),
    "latitude": "" (opcional, numeric),
    "longitude": "" (opcional, numeric),
    "radius_m": "" (opcional, integer),
    "active": "" (opcional, boolean)
}
```

**DELETE /geofence** → excluir o geofence do usuário
``` json
{
    "data": true
}
```

### Comportamento automático:
- ✅ Ao criar um **geofence**: Automaticamente associa a todos os devices do usuário
- ✅ Ao criar um **device**: Automaticamente associa ao geofence do usuário (se existir)
- ✅ A associação é feita na tabela `geofence_device`

## ⚠️ Alertas & Notificações

GET /alerts → listar alertas do usuário

PATCH /alerts/{id}/ack → marcar alerta como reconhecido

GET /notifications → listar notificações enviadas

POST /notifications/test → enviar notificação de teste

## 📊 Eventos

POST /devices/{id}/events → registrar evento (ex: botão de emergência, desligamento)

GET /devices/{id}/events → listar eventos de um dispositivo -->