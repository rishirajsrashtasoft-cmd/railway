# Usage

## Web Interface
1. Home (`/`): Submit a single domain
2. Bulk (`/bulk`): Paste multiple domains (one per line)
3. Results (`/domains/list`): Filter, sort, paginate, and export

## REST API
- `POST /api/check?domain=example.com` → JSON result
- `GET /domains/{id}` → JSON for a stored result
- `POST /domains` (form) → performs a check and redirects
- `POST /domains/{id}/delete` → remove a result

### Example
```bash
curl -X POST "http://localhost:8080/api/check?domain=google.com"
```

## Filters, sorting, pagination
- Filter by domain substring, reachability (yes/no), safety (safe/unsafe), date range
- Sort by time asc/desc (default: desc)
- 50 results per page; shows total counters

