#!/bin/bash

# ╔══════════════════════════════════════════════════════════════╗
# ║              ZUPLY - CODESPACE SETUP SCRIPT                  ║
# ║         Run this once after opening a new Codespace          ║
# ╚══════════════════════════════════════════════════════════════╝

echo ""
echo "======================================================="
echo "         ZUPLY CODESPACE SETUP STARTING..."
echo "======================================================="
echo ""

# ── Step 1: Install MySQL ──────────────────────────────────────
echo "[ 1/9 ] Installing MySQL..."
sudo apt-get update -qq && sudo apt-get install -y -qq mysql-server
echo "✅ MySQL installed"

# ── Step 2: Start MySQL ────────────────────────────────────────
echo ""
echo "[ 2/9 ] Starting MySQL..."
sudo service mysql start
echo "✅ MySQL started"

# ── Step 3: Create Database ────────────────────────────────────
echo ""
echo "[ 3/9 ] Creating database and user..."
sudo mysql -u root -e "
  CREATE DATABASE IF NOT EXISTS zuplydb;
  CREATE USER IF NOT EXISTS 'zuply'@'localhost' IDENTIFIED BY 'zuply123';
  GRANT ALL PRIVILEGES ON zuplydb.* TO 'zuply'@'localhost';
  FLUSH PRIVILEGES;
"
echo "✅ Database 'zuplydb' created"

# ── Step 4: Install Java 21 ────────────────────────────────────
echo ""
echo "[ 4/9 ] Installing Java 21..."
sudo apt-get install -y -qq openjdk-21-jdk
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
echo "export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
echo "✅ Java 21 set as default"

# ── Step 5: Create application.properties ─────────────────────
echo ""
echo "[ 5/9 ] Creating application.properties..."
mkdir -p /workspaces/Zuply/Backend/src/main/resources
mkdir -p /workspaces/Zuply/Backend/uploads

cat > /workspaces/Zuply/Backend/src/main/resources/application.properties << 'EOF'
spring.datasource.url=jdbc:mysql://localhost:3306/zuplydb
spring.datasource.username=zuply
spring.datasource.password=zuply123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
server.port=9090
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.jwt.secret=${JWT_SECRET}
gemini.api.key=${GEMINI_API_KEY}
razorpay.key.id=${RAZORPAY_KEY_ID}
razorpay.key.secret=${RAZORPAY_KEY_SECRET}
upload.path=/workspaces/Zuply/Backend/uploads
EOF
echo "✅ application.properties created"

# ── Step 6: Create .env file ───────────────────────────────────
echo ""
echo "[ 6/9 ] Creating .env file..."
echo ""
echo "⚠️  You need to provide your own API keys for the .env file."
echo ""

read -p "Enter your GEMINI_API_KEY: " GEMINI_API_KEY_INPUT
read -p "Enter your RAZORPAY_KEY_ID: " RAZORPAY_KEY_ID_INPUT
read -p "Enter your RAZORPAY_KEY_SECRET: " RAZORPAY_KEY_SECRET_INPUT

cat > /workspaces/Zuply/Backend/.env << EOF
JWT_SECRET=zuply-secret-key-that-is-at-least-256-bits-long-for-hs256
GEMINI_API_KEY=${GEMINI_API_KEY_INPUT}
RAZORPAY_KEY_ID=${RAZORPAY_KEY_ID_INPUT}
RAZORPAY_KEY_SECRET=${RAZORPAY_KEY_SECRET_INPUT}
EOF
echo "✅ .env file created"

# ── Step 7: Fix Backend permissions ───────────────────────────
echo ""
echo "[ 7/9 ] Setting backend permissions..."
chmod +x /workspaces/Zuply/Backend/mvnw
echo "✅ Backend permissions set"

# ── Step 8: Install Frontend dependencies ─────────────────────
echo ""
echo "[ 8/9 ] Installing Frontend dependencies (this takes 2-3 mins)..."
cd /workspaces/Zuply/Frontend/Zuply-FrontEnd
rm -rf node_modules package-lock.json
npm install --silent
chmod +x node_modules/.bin/ng
echo "✅ Frontend dependencies installed"

# ── Step 9: Done ───────────────────────────────────────────────
echo ""
echo "======================================================="
echo "         ✅ SETUP COMPLETE!"
echo "======================================================="
echo ""
echo "Now run these two commands in SEPARATE terminals:"
echo ""
echo "  Terminal 1 (Backend):"
echo "  cd /workspaces/Zuply/Backend && ./mvnw spring-boot:run"
echo ""
echo "  Terminal 2 (Frontend):"
echo "  cd /workspaces/Zuply/Frontend/Zuply-FrontEnd && node_modules/.bin/ng serve --proxy-config proxy.conf.json --host 0.0.0.0 --port 4200"
echo ""
echo "Then go to PORTS tab → set port 9090 and 4200 to PUBLIC"
echo ""
echo "Admin Login → admin@zuply.in / Admin@123"
echo "======================================================="
