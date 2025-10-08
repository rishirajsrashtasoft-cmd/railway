# Domain Checker - Deployment Guide

## Free Hosting Options

### 1. Railway (Recommended)
- **Free tier**: 500 hours/month, $5 credit
- **Database**: Free PostgreSQL included
- **Custom domain**: Free subdomain + custom domain support
- **URL**: https://railway.app

### 2. Render
- **Free tier**: 750 hours/month, sleeps after 15min inactivity
- **Database**: Free PostgreSQL
- **URL**: https://render.com

### 3. Vercel (JAR deployment)
- **Free tier**: Generous limits
- **URL**: https://vercel.com

## Railway Deployment (Step-by-Step)

### 1. Prepare Your Repository
1. Push your code to GitHub/GitLab
2. Ensure your project has:
   - `Procfile` (already exists)
   - `pom.xml` with PostgreSQL dependency (added)
   - Environment variable configuration (updated)

### 2. Deploy on Railway

#### Option A: GitHub Integration
1. Go to [Railway.app](https://railway.app)
2. Sign up with GitHub
3. Click "New Project" → "Deploy from GitHub repo"
4. Select your repository
5. Railway will automatically detect it's a Java project

#### Option B: Manual Upload
1. Go to [Railway.app](https://railway.app)
2. Sign up
3. Click "New Project" → "Empty Project"
4. Connect your GitHub repository

### 3. Configure Environment Variables
In Railway dashboard, go to your project → Variables tab and add:

```
DATABASE_URL=postgresql://username:password@host:port/database
DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
DB_DRIVER=org.postgresql.Driver
APP_SECURITY_USER=your_admin_username
APP_SECURITY_PASS=your_secure_password
VIRUSTOTAL_API_KEY=your_virustotal_api_key
THYMELEAF_CACHE=true
SHOW_SQL=false
FORMAT_SQL=false
```

### 4. Add Database
1. In Railway project, click "New" → "Database" → "PostgreSQL"
2. Railway will automatically set `DATABASE_URL` environment variable
3. Your app will connect automatically

### 5. Deploy
1. Railway will automatically build and deploy
2. Your app will be available at: `https://your-app-name.railway.app`
3. Login with your `APP_SECURITY_USER` and `APP_SECURITY_PASS`

## Render Deployment

### 1. Connect Repository
1. Go to [Render.com](https://render.com)
2. Sign up with GitHub
3. Click "New" → "Web Service"
4. Connect your repository

### 2. Configure Service
- **Build Command**: `mvn clean package -DskipTests`
- **Start Command**: `java -jar target/domain_checker.jar`
- **Environment**: Java

### 3. Add Database
1. Click "New" → "PostgreSQL"
2. Copy the database URL
3. Add environment variables in your web service

### 4. Environment Variables
```
DATABASE_URL=postgresql://username:password@host:port/database
DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
DB_DRIVER=org.postgresql.Driver
APP_SECURITY_USER=your_admin_username
APP_SECURITY_PASS=your_secure_password
VIRUSTOTAL_API_KEY=your_virustotal_api_key
```

## Custom Domain Setup

### Railway
1. Go to your project → Settings → Domains
2. Add your custom domain
3. Update DNS records as instructed
4. SSL certificate is automatically provided

### Render
1. Go to your service → Settings → Custom Domains
2. Add your domain
3. Update DNS records
4. SSL is automatic

## Local Testing Before Deployment

1. **Build the project**:
   ```bash
   mvn clean package -DskipTests
   ```

2. **Test with environment variables**:
   ```bash
   # Windows
   set DATABASE_URL=postgresql://localhost:5432/testdb
   set DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
   set DB_DRIVER=org.postgresql.Driver
   java -jar target/domain_checker.jar
   
   # Linux/Mac
   export DATABASE_URL=postgresql://localhost:5432/testdb
   export DB_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
   export DB_DRIVER=org.postgresql.Driver
   java -jar target/domain_checker.jar
   ```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check `DATABASE_URL` format
   - Ensure database service is running
   - Verify credentials

2. **Build Failed**
   - Check Java version (requires Java 17+)
   - Verify `pom.xml` dependencies
   - Check build logs

3. **App Won't Start**
   - Check `Procfile` syntax
   - Verify environment variables
   - Check application logs

4. **Selenium Issues**
   - Selenium may not work in cloud environments
   - Consider disabling Selenium checks for production

### Logs
- Railway: Project → Deployments → View logs
- Render: Service → Logs tab

## Security Notes

1. **Change default credentials** in production
2. **Use strong passwords** for admin access
3. **Keep VirusTotal API key secure**
4. **Enable HTTPS** (automatic on most platforms)
5. **Consider rate limiting** for public APIs

## Cost Optimization

1. **Railway**: Free tier includes 500 hours/month
2. **Render**: Free tier includes 750 hours/month
3. **Monitor usage** to avoid overages
4. **Use sleep mode** for development/testing

## Next Steps

1. Deploy to your chosen platform
2. Test all functionality
3. Set up monitoring
4. Configure custom domain
5. Set up automated backups (if needed)
