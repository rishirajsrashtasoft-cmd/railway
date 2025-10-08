# Domain Checker - Overview

A Spring Boot application to check domains for reachability, HTTP response, and security signals using the VirusTotal API, with a clean web UI, bulk checking, and export options.

## Features
- Ping reachability check (ICMP best-effort)
- HTTP status check (follows redirects; tries HTTP/HTTPS)
- VirusTotal reputation lookup (optional, with API key)
- Curl-style latency/status summary using Java HttpClient
- Headless Selenium page probe (title, source length, load time)
- MySQL persistence of results
- Web UI (Thymeleaf/Bootstrap) with filters, sorting, pagination, quick stats
- Exports to CSV, PDF, Word
- REST API for programmatic checks
- Windows BAT/PowerShell helpers and desktop shortcut launcher

