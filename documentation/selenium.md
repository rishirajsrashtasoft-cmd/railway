# Selenium Check

The application performs a best-effort headless Selenium probe per domain to enrich results.

## What it collects
- Navigation success (reachable)
- Page title
- Page source length
- Load time (ms)
- Error message if navigation fails

## Requirements
- Chrome/Chromium installed
- Compatible ChromeDriver (Selenium Manager can auto-provision)
- Outbound internet allowed

## Configuration & tips
- Runs headless with `--headless=new`
- Errors are non-fatal and stored in `seleniumError`
- To debug, run non-headless locally by removing the headless argument in `DomainCheckerService.runSeleniumCheck`
- Keep drivers and Chrome updated to minimize CDP warnings


