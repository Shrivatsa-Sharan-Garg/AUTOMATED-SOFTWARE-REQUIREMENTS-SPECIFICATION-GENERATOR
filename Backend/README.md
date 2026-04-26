# SRS Generator Backend

The core Java backend for the Automated SRS Generator. This engine handles high-fidelity HTTP requests, manages the SQLite database lifecycle, and generates IEEE-standard documentation.

## 📂 Project Structure

```text
Backend/
├── bin/                 # Compiled .class files
├── lib/                 # External Dependencies (.jar)
├── src/                 # Source Code
│   ├── com/srs/         # Main Java Packages
│   │   ├── api/         # handler.java & DocumentGenerator.java
│   │   ├── db/          # DBconnections.java
│   │   └── Main.java    # Entry Point
│   ├── db/              # Auto-generated SQLite Database (srs.db)
│   ├── resources/       # JSON Templates
│   ├── sql/             # Initialization & Query Scripts
│   └── .env             # Environment Configuration
└── README.md
```

## 🛠️ Prerequisites & Dependencies

To run this project manually without Maven/Gradle, you must download the following JAR files and place them in the `backend/lib/` folder:

- [sqlite-jdbc-3.45.2.0.jar](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar)
- [dotenv-java-3.0.0.jar](https://repo1.maven.org/maven2/io/github/cdimascio/dotenv-java/3.0.0/dotenv-java-3.0.0.jar)
- [gson-2.10.1.jar](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)
- [tomcat-servlet-api-9.0.55.jar](https://repo1.maven.org/maven2/org/apache/tomcat/tomcat-servlet-api/9.0.55/tomcat-servlet-api-9.0.55.jar)
- [slf4j-api-1.7.36.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar)
- [slf4j-simple-1.7.36.jar](https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar)
- [openpdf-1.3.30.jar](https://repo1.maven.org/maven2/com/github/librepdf/openpdf/1.3.30/openpdf-1.3.30.jar)
- [poi-5.2.5.jar](https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.5/poi-5.2.5.jar)
- [poi-ooxml-5.2.5.jar](https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.5/poi-ooxml-5.2.5.jar)
- [xmlbeans-5.2.0.jar](https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/5.2.0/xmlbeans-5.2.0.jar)
- [commons-collections4-4.4.jar](https://repo1.maven.org/maven2/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar)

## ⚡ Recommended Setup in VS Code

Instead of manually downloading JARs, you can install the  
[No-Build Java Power Pack 🛒☕](https://marketplace.visualstudio.com/items?itemName=SudhanshuAmbastha.vscode-java-pack-no-build).

Lightweight Java extension pack for students and prototypers — run Java instantly without Maven or Gradle.  
Includes JAR Cart for zero‑config dependency management plus essential tools (Red Hat Java, Debugger, Test Runner, Code Runner, Oracle Java).  
Perfect for quick setups, assignments, and high‑speed prototyping.

## ⚙️ Setup Instructions

1. Environment Configuration
   Create a `.env` file in the `backend/src/` directory with the following variables:

```env
DB_URL=jdbc:sqlite:src/db/srs.db
```

2. 🚀 Compilation and Execution
   Run these commands from the `backend/` directory in your terminal:

```cmd
javac -cp "lib/*;bin" -d bin src/com/srs/Main.java src/com/srs/db/*.java src/com/srs/api/*.java
```

3. Running the Server

```cmd
java -cp "bin;lib/*" com.srs.Main
```

The engine will automatically create the `/db` folder and initialize the schema if it doesn't exist.

---

## 📊 Database Management

For visualizing data within VS Code, it is highly recommended to use the MySQL/SQLite Client extension:

- 🔗 [Download: MySQL/SQLite Client for VS Code](https://marketplace.visualstudio.com/items?itemName=cweijan.vscode-mysql-client2)

How to connect:

- Open the extension sidebar.
- Click **Add Connection** -> Select **SQLite**.
- Point the Database Path to `Backend/src/db/srs.db`.

## 📝 Backend Features

- [x] **Automated PDF Engine**: Generates IEEE-standard PDF reports via the /api/download-pdf endpoint.
- [x] **Separation of Concerns**: Logic is split between handler (routing) and DocumentGenerator (rendering).
- [x] **Self-Healing DB**: Auto-creates directories and initializes srs.db using schema.sql.
- [x] **CORS Support**: Pre-configured for seamless communication with the Frontend dashboard.
- [x] **Pragmatic JSON Parsing**: Lightweight, vanilla Java implementation for extracting data without heavy libraries.

---

## ⚖️ License

This project is licensed under the **GNU General Public License v3.0**.
See the [LICENSE](LICENSE) file for the full text.
