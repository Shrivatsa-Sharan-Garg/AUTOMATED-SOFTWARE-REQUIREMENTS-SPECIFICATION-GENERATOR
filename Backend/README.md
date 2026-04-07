# SRS Generator Backend

The core Java backend for the Automated SRS Generator. This engine handles HTTP requests, manages the SQLite database lifecycle, and serves SRS templates.

## 📂 Project Structure

```text
Backend/
├── bin/                 # Compiled .class files
├── lib/                 # External Dependencies (.jar)
├── src/                 # Source Code
│   ├── com/srs/         # Main Java Packages
│   ├── db/              # Auto-generated SQLite Database (srs.db)
│   ├── resources/       # JSON Templates
│   ├── sql/             # Initialization & Query Scripts
│   └── .env             # Environment Configuration
└── README.md
```

## 🛠️ Prerequisites & Dependencies

To run this project manually without Maven/Gradle, you must download the following JAR files and place them in the `backend/lib/` folder:

- **sqlite-jdbc-3.45.2.0.jar**: [sqlite-jdbc-3.45.2.0.jar](https://www.google.com/search?q=https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar)
- **Dotenv Java**: [dotenv-java-3.0.0.jar](https://repo1.maven.org/maven2/io/github/cdimascio/dotenv-java/3.0.0/dotenv-java-3.0.0.jar)
- **Tomcat Servlet API**: [tomcat-servlet-api-9.0.55.jar](https://repo1.maven.org/maven2/org/apache/tomcat/tomcat-servlet-api/9.0.55/tomcat-servlet-api-9.0.55.jar)
- Logging (Required for SQLite):
  - [slf4j-api-1.7.36.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar)
  - [slf4j-simple-1.7.36.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar)

## ⚙️ Setup Instructions

1. Environment Configuration
   Create a `.env` file in the `backend/src/` directory with the following variables:

```env
DB_URL=jdbc:sqlite:src/db/srs.db
```

2. 🚀 Compilation and Execution
   Run these commands from the `backend/` directory in your terminal:

```cmd
javac -cp "lib/*;bin" -d bin src/com/srs/Main.java src/com/srs/db/DBconnections.java src/com/srs/api/handler.java
```

3. Running the Server

```cmd
java -cp "bin;lib/*" com.srs.Main
```

The engine will automatically create the `/db` folder and initialize the schema if it doesn't exist.

---

## 📊 Database Management

For visualizing data within VS Code, it is highly recommended to use the MySQL/SQLite Client extension:
🔗 [Download: MySQL/SQLite Client for VS Code](https://marketplace.visualstudio.com/items?itemName=cweijan.vscode-mysql-client2)
How to connect:

- Open the extension sidebar.
- Click **Add Connection** -> Select **SQLite**.
- Point the Database Path to `Backend/src/db/srs.db`.

## 📝 Backend Features

- [x] **Self-Healing Infrastructure**: Auto-creates database directories and missing `.db` files.
- [x] **Safe Stream Handling**: Implements `Try-With-Resources` to prevent memory leaks.
- [x] **CORS Enabled**: Pre-configured to allow requests from the Frontend dashboard.
- [x] **Dynamic SQL Loading**: Reads schema and insert queries from external `.sql` files for modularity.

---
