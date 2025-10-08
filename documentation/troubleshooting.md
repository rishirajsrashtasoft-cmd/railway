# Troubleshooting

## Common Issues

### Database connection error
- Verify MySQL is running
- Check username/password and DB name
- Ensure the schema exists

### VirusTotal API issues
- Validate API key
- Check VT rate limits/quotas
- Test with curl to inspect status/headers

```bash
curl -s -D - -H "x-apikey: YOUR_KEY" -H "Accept: application/json" \
  "https://www.virustotal.com/api/v3/domains/example.com"
```

### Domain not reachable
- Hosts may block ICMP ping
- Firewall/DNS issues
- Try the curl-style check result for hints

## Logs
Enable debug logs:
```properties
logging.level.com.example.domainchecker=DEBUG
```


