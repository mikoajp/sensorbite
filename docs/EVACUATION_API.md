# Evacuation Route API Documentation

## Overview

The Evacuation Route API calculates the safest evacuation route between two geographic points, taking into account hazard zones (e.g., flood zones, fires) and using a modified Dijkstra's algorithm.

## Endpoint

### Calculate Evacuation Route

**GET** `/api/evac/route`

Calculates the optimal evacuation route between start and end coordinates.

#### Query Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| startLat | Double | Yes | Starting point latitude (-90 to 90) | 52.2297 |
| startLon | Double | Yes | Starting point longitude (-180 to 180) | 21.0122 |
| endLat | Double | Yes | Ending point latitude (-90 to 90) | 52.2500 |
| endLon | Double | Yes | Ending point longitude (-180 to 180) | 21.0500 |
| networkName | String | No | Road network file name | sample_network.geojson |
| includeHazards | Boolean | No | Include hazard zones (default: true) | true |

#### Example Request

```bash
curl "http://localhost:8080/api/evac/route?startLat=52.0&startLon=21.0&endLat=52.04&endLon=21.04"
```

#### Example Response (GeoJSON)

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [21.0, 52.0],
          [21.01, 52.01],
          [21.02, 52.02],
          [21.03, 52.03],
          [21.04, 52.04]
        ]
      },
      "properties": {
        "distance_km": 6.28,
        "estimated_time_minutes": 75,
        "hazard_level": "low",
        "avoided_zones": 0,
        "safety_score": 95.5,
        "notes": "This is a safe evacuation route."
      }
    }
  ],
  "metadata": {
    "calculation_time_ms": 123,
    "algorithm": "modified_dijkstra",
    "timestamp": "2024-12-06T14:30:00",
    "total_waypoints": 5
  }
}
```

## Response Fields

### Geometry
- **type**: Always "LineString"
- **coordinates**: Array of [longitude, latitude] pairs representing the route

### Properties
- **distance_km**: Total distance of the route in kilometers
- **estimated_time_minutes**: Estimated time to traverse the route (walking speed: 5 km/h)
- **hazard_level**: Safety classification (low/medium/high)
- **avoided_zones**: Number of hazard zones successfully avoided
- **safety_score**: Safety rating from 0-100 (higher is safer)
- **notes**: Human-readable notes and warnings about the route

### Metadata
- **calculation_time_ms**: Time taken to calculate the route in milliseconds
- **algorithm**: Algorithm used (modified_dijkstra)
- **timestamp**: When the route was calculated
- **total_waypoints**: Number of waypoints in the route

## Safety Score Interpretation

| Score Range | Level | Description |
|-------------|-------|-------------|
| 80-100 | Safe | Route avoids all hazards with good clearance |
| 50-79 | Moderate | Route passes near hazards, exercise caution |
| 30-49 | Risky | Route very close to hazards, high risk |
| 0-29 | Dangerous | Route intersects hazard zones, avoid if possible |

## Error Responses

### 400 Bad Request
Invalid or missing parameters

```json
{
  "timestamp": "2024-12-06T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Start latitude must be between -90.0 and 90.0",
  "path": "/api/evac/route"
}
```

### 404 Not Found
No route could be found

```json
{
  "timestamp": "2024-12-06T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "No safe route found between given points. All possible paths may be blocked.",
  "path": "/api/evac/route"
}
```

### 500 Internal Server Error
Server error during calculation

```json
{
  "timestamp": "2024-12-06T14:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to calculate route: ...",
  "path": "/api/evac/route"
}
```

## Health Check

**GET** `/api/evac/health`

Returns the operational status of the evacuation service.

```bash
curl http://localhost:8080/api/evac/health
```

Response:
```
Evacuation service is operational
```

## Algorithm Details

### Modified Dijkstra's Algorithm

The evacuation routing uses a modified Dijkstra's shortest path algorithm with the following enhancements:

1. **Hazard Penalty**: Road segments intersecting hazard zones receive a weight penalty:
   - Regular hazard (severity 1-3): 100x multiplier
   - Severe hazard (severity 4-5): 1000x multiplier

2. **Safety Score Calculation**: Based on:
   - Minimum distance to any hazard zone
   - Number of hazard zones avoided
   - Route length and estimated time

3. **Graph Weighting**:
   - Base weight: segment length in meters
   - Adjusted weight: base weight × penalty multiplier

### Walking Speed Assumptions
- Default walking speed: 5 km/h
- Used for time estimation
- Does not account for terrain difficulty

## PowerShell Examples

### Basic Route Request
```powershell
$params = @{
    startLat = 52.2297
    startLon = 21.0122
    endLat = 52.2500
    endLon = 21.0500
}

$uri = "http://localhost:8080/api/evac/route?" +
       ($params.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" } | Join-String -Separator "&")

Invoke-RestMethod -Uri $uri -Method Get
```

### With Custom Network
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:8080/api/evac/route" `
    -Method Get `
    -Body @{
        startLat = 52.0
        startLon = 21.0
        endLat = 52.04
        endLon = 21.04
        networkName = "warsaw_center.geojson"
        includeHazards = $true
    }

Write-Host "Distance: $($result.features[0].properties.distance_km) km"
Write-Host "Time: $($result.features[0].properties.estimated_time_minutes) minutes"
Write-Host "Safety Score: $($result.features[0].properties.safety_score)"
```

## Testing with Sample Data

The application includes a built-in sample road network (5x5 grid around Warsaw coordinates):
- Center: ~52.02°N, 21.02°E
- Grid spacing: ~0.01° (~1.1 km)
- 25 nodes, 40 edges

You can test with coordinates in this range:
- Latitude: 52.0 to 52.04
- Longitude: 21.0 to 21.04

## Integration Notes

### GeoJSON Compatibility
The response follows the GeoJSON specification (RFC 7946) and can be directly used with:
- Leaflet.js
- OpenLayers
- Mapbox GL JS
- QGIS
- Any GeoJSON-compatible mapping library

### CORS
CORS is configured to allow requests from any origin during development. Configure appropriately for production.

### Rate Limiting
Currently no rate limiting is implemented. Consider adding for production use.

## Future Enhancements

Planned features for next phases:
- Real-time hazard zone data from Sentinel Hub API
- Multiple route options (fastest, safest, shortest)
- Vehicle routing (not just walking)
- Real-time traffic data integration
- WebSocket support for live updates
- Route caching and optimization
