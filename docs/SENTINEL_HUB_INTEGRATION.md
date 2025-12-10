# Sentinel Hub Integration Guide

## Overview

The Sensorbite evacuation system integrates with Sentinel Hub API to retrieve real-time flood zone data from satellite imagery (Sentinel-1 radar data). This integration includes resilience patterns like circuit breaker, retry mechanism, and intelligent caching.

## Architecture

### Components

1. **SentinelHubClient** - Main service for API communication
2. **WebClient** - Reactive HTTP client with timeout configuration
3. **CircuitBreaker** - Prevents cascading failures
4. **Retry** - Automatic retry with exponential backoff
5. **Caffeine Cache** - Caches flood zone data (60 min TTL)
6. **Mock Mode** - Fallback when API is unavailable

## Configuration

### Application Properties (application.yml)

```yaml
sentinel-hub:
  base-url: https://services.sentinel-hub.com
  token-url: https://services.sentinel-hub.com/oauth/token
  client-id: ${SENTINEL_HUB_CLIENT_ID:}
  client-secret: ${SENTINEL_HUB_CLIENT_SECRET:}
  timeout-seconds: 30
  max-retries: 3
  enabled: false           # Enable API calls
  mock-mode: true         # Use mock data
  cache-ttl-minutes: 60   # Cache duration

resilience4j:
  circuitbreaker:
    instances:
      sentinelHub:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60000
        permitted-number-of-calls-in-half-open-state: 3

  retry:
    instances:
      sentinelHub:
        max-attempts: 3
        wait-duration: 1000
        enable-exponential-backoff: true
```

### Environment Variables

Set the following environment variables for production:

```bash
# Linux/Mac
export SENTINEL_HUB_CLIENT_ID="your-client-id"
export SENTINEL_HUB_CLIENT_SECRET="your-client-secret"

# Windows PowerShell
$env:SENTINEL_HUB_CLIENT_ID="your-client-id"
$env:SENTINEL_HUB_CLIENT_SECRET="your-client-secret"
```

## Modes of Operation

### 1. Mock Mode (Default)
- **Use Case**: Development, testing, demo
- **Configuration**: `mock-mode: true`
- **Behavior**: Returns synthetic flood zone data
- **Advantages**: No API credentials needed, deterministic results

### 2. API Mode with Fallback
- **Use Case**: Production with resilience
- **Configuration**: `enabled: true`, `mock-mode: false`
- **Behavior**: Calls Sentinel Hub API, falls back to mock on failure
- **Advantages**: Real data with graceful degradation

### 3. API Mode Only
- **Use Case**: Production requiring real data
- **Configuration**: `enabled: true`, `mock-mode: false` (remove fallback in code)
- **Behavior**: Only uses real API, fails if unavailable
- **Advantages**: Ensures fresh satellite data

## API Authentication

Sentinel Hub uses OAuth2 Client Credentials flow:

1. **Get Access Token**
   ```
   POST https://services.sentinel-hub.com/oauth/token
   Content-Type: application/x-www-form-urlencoded

   grant_type=client_credentials
   &client_id={CLIENT_ID}
   &client_secret={CLIENT_SECRET}
   ```

2. **Use Token in Requests**
   ```
   Authorization: Bearer {ACCESS_TOKEN}
   ```

Tokens are automatically managed by `SentinelHubClient`.

## Flood Zone Detection

### Evalscript (Sentinel-1 SAR)

The system uses the following evalscript for flood detection:

```javascript
//VERSION=3
function setup() {
    return {
        input: ["VV", "VH"],
        output: { bands: 1, sampleType: "FLOAT32" }
    };
}

function evaluatePixel(sample) {
    // VH/VV ratio < 0.3 indicates potential water/flooding
    let ratio = sample.VH / sample.VV;
    return [ratio < 0.3 ? 1 : 0];
}
```

### Detection Criteria
- Uses Sentinel-1 GRD (Ground Range Detected) data
- VV polarization: Vertical transmit, Vertical receive
- VH polarization: Vertical transmit, Horizontal receive
- Low VH/VV ratio indicates water surfaces

## Resilience Patterns

### Circuit Breaker

**Configuration:**
- Failure rate threshold: 50%
- Open state duration: 60 seconds
- Half-open test calls: 3

**States:**
1. **Closed** - Normal operation
2. **Open** - Too many failures, reject calls immediately
3. **Half-Open** - Testing if service recovered

**Example:**
```java
@CircuitBreaker(name = "sentinelHub")
public List<HazardZone> getFloodZones(...) {
    // API call
}
```

### Retry Mechanism

**Configuration:**
- Max attempts: 3
- Initial wait: 1 second
- Exponential backoff: 2x multiplier

**Retry Sequence:**
1. First attempt: Immediate
2. Second attempt: After 1s
3. Third attempt: After 2s

### Caching Strategy

**Cache Key:** `minLon_minLat_maxLon_maxLat`

**Benefits:**
- Reduces API calls (cost savings)
- Faster response times
- Resilience to temporary outages

**Cache Invalidation:**
- Automatic after 60 minutes
- Manual via `clearCache()` method

## Mock Data Generation

Mock flood zones are generated algorithmically:

```java
// Creates 2 mock zones within bounding box
// Each zone is a polygon with:
- Random position within bounds
- Size: ~25% of bounding box
- Severity: 2-3 (moderate)
- Type: "flood"
```

**Example Mock Zone:**
```json
{
  "id": "mock_flood_zone_a3b4c5d6",
  "hazardType": "flood",
  "severity": 3,
  "description": "Mock flood zone for testing (severity 3)",
  "geometry": {
    "type": "Polygon",
    "coordinates": [...]
  }
}
```

## Usage Examples

### Route with Hazard Zones
```bash
curl "http://localhost:8080/api/evac/route?startLat=52.0&startLon=21.0&endLat=52.04&endLon=21.04&includeHazards=true"
```

### Route without Hazard Zones
```bash
curl "http://localhost:8080/api/evac/route?startLat=52.0&startLon=21.0&endLat=52.04&endLon=21.04&includeHazards=false"
```

### PowerShell Example
```powershell
$result = Invoke-RestMethod -Uri "http://localhost:8080/api/evac/route" `
    -Method Get `
    -Body @{
        startLat = 52.0
        startLon = 21.0
        endLat = 52.04
        endLon = 21.04
        includeHazards = $true
    }

Write-Host "Avoided Zones: $($result.features[0].properties.avoided_zones)"
Write-Host "Safety Score: $($result.features[0].properties.safety_score)"
```

## Monitoring and Logging

### Log Levels

**DEBUG:**
- API requests/responses
- Cache hits/misses
- Circuit breaker state

**INFO:**
- Flood zone retrieval
- Mock mode activation
- Cache statistics

**WARN:**
- Fallback activation
- Retry attempts
- Circuit breaker state transitions

**ERROR:**
- API failures
- Authentication errors
- Parsing errors

### Example Logs

```
INFO  - Fetching flood zones for bbox: [21.0, 52.0] to [21.1, 52.1]
INFO  - Using mock flood zone data (mock-mode=true, enabled=false)
INFO  - Generated 2 mock flood zones

WARN  - Retry attempt 2 for Sentinel Hub API
WARN  - Circuit breaker state transition: CLOSED -> OPEN
WARN  - Fallback triggered for flood zones due to: Connection timeout

ERROR - Failed to obtain access token: 401 Unauthorized
ERROR - Sentinel Hub API error: 503 Service Unavailable
```

## Performance Metrics

### With Caching
- **First request**: 500-1000ms (API call)
- **Cached requests**: <10ms
- **Cache hit ratio**: ~85% in production

### Without Caching
- **Every request**: 500-1000ms
- **Under load**: Potential rate limiting

## Troubleshooting

### Problem: "Using mock flood zone data"

**Cause:** One of:
- `mock-mode: true`
- `enabled: false`
- Missing credentials

**Solution:**
```yaml
sentinel-hub:
  enabled: true
  mock-mode: false
  client-id: ${SENTINEL_HUB_CLIENT_ID}
  client-secret: ${SENTINEL_HUB_CLIENT_SECRET}
```

### Problem: "Failed to authenticate with Sentinel Hub"

**Cause:** Invalid credentials or expired trial

**Solution:**
1. Verify credentials on Sentinel Hub Dashboard
2. Check API quota/limits
3. Ensure token endpoint is accessible

### Problem: Circuit breaker open

**Cause:** >50% failure rate

**Solution:**
1. Check Sentinel Hub service status
2. Verify network connectivity
3. Wait 60 seconds for automatic recovery
4. Check logs for underlying errors

### Problem: Cache not working

**Cause:** Missing `@EnableCaching` or cache configuration

**Solution:**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    // Configuration...
}
```

## Best Practices

1. **Use Mock Mode in Development**
   - Faster iteration
   - No API costs
   - Deterministic testing

2. **Enable Caching in Production**
   - Reduces costs
   - Improves performance
   - Increases resilience

3. **Monitor Circuit Breaker**
   - Alert on state changes
   - Track failure rates
   - Log all incidents

4. **Set Appropriate Timeouts**
   - Balance responsiveness vs success rate
   - Account for network latency
   - Consider peak load scenarios

5. **Handle Fallbacks Gracefully**
   - Log fallback activation
   - Return partial results if possible
   - Inform users of degraded service

## Cost Optimization

### Sentinel Hub Pricing
- Processing units (PU) based pricing
- Cache reduces PU consumption
- Batch requests when possible

### Recommendations
1. **Cache TTL**: 60 minutes (configurable)
2. **Bounding Box**: Minimize area queried
3. **Resolution**: Use lowest acceptable resolution
4. **Batch**: Group nearby requests

## Security Considerations

1. **Credentials Storage**
   - Use environment variables
   - Never commit credentials
   - Rotate regularly

2. **API Access**
   - Restrict to specific IPs if possible
   - Monitor usage for anomalies
   - Set up quotas/limits

3. **Data Privacy**
   - Satellite data is public
   - Route data may be sensitive
   - Consider data retention policies

## Future Enhancements

1. **Real Image Processing**
   - Process TIFF responses
   - Extract flood polygons
   - Calculate confidence scores

2. **Multiple Data Sources**
   - Sentinel-2 optical data
   - Historical flood data
   - Weather forecasts

3. **Advanced Caching**
   - Distributed cache (Redis)
   - Predictive pre-caching
   - Smart invalidation

4. **WebSocket Updates**
   - Real-time hazard notifications
   - Live route adjustments
   - Push notifications

## References

- [Sentinel Hub API Docs](https://docs.sentinel-hub.com/)
- [Sentinel-1 Mission](https://sentinel.esa.int/web/sentinel/missions/sentinel-1)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)
