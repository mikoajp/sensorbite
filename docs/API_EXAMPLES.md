# API Examples

## Building Management

### Create Building
```bash
curl -X POST http://localhost:8080/api/buildings \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Hub",
    "address": "789 Innovation Drive"
  }'
```

### Get All Buildings
```bash
curl http://localhost:8080/api/buildings
```

### Get Building by ID
```bash
curl http://localhost:8080/api/buildings/1
```

### Update Building
```bash
curl -X PUT http://localhost:8080/api/buildings/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Hub - Updated",
    "address": "789 Innovation Drive, Suite 100"
  }'
```

### Delete Building
```bash
curl -X DELETE http://localhost:8080/api/buildings/1
```

## Floor Management

### Create Floor
```bash
curl -X POST http://localhost:8080/api/floors \
  -H "Content-Type: application/json" \
  -d '{
    "level": 3,
    "buildingId": 1
  }'
```

### Get Floors by Building
```bash
curl http://localhost:8080/api/floors/building/1
```

## Room Management

### Create Room
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "roomNumber": "305",
    "description": "Meeting Room",
    "floorId": 1
  }'
```

### Get Rooms by Floor
```bash
curl http://localhost:8080/api/rooms/floor/1
```

## Sensor Management

### Create Sensor
```bash
curl -X POST http://localhost:8080/api/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TEMPERATURE",
    "description": "Climate control sensor",
    "roomId": 1
  }'
```

### Get Sensors by Room
```bash
curl http://localhost:8080/api/sensors/room/1
```

## Sensor Reading Management

### Create Reading
```bash
curl -X POST http://localhost:8080/api/sensor-readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 23.5,
    "sensorId": 1
  }'
```

### Get Readings by Sensor
```bash
curl http://localhost:8080/api/sensor-readings/sensor/1
```

### Get Readings by Time Range
```bash
curl "http://localhost:8080/api/sensor-readings/sensor/1/range?startTime=2024-01-01T00:00:00&endTime=2024-12-31T23:59:59"
```

## PowerShell Examples

### Create Building (PowerShell)
```powershell
$body = @{
    name = "Tech Hub"
    address = "789 Innovation Drive"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/buildings" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

### Get All Buildings (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/buildings" -Method Get
```

### Create Sensor Reading (PowerShell)
```powershell
$body = @{
    value = 23.5
    sensorId = 1
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/sensor-readings" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```
