# JDBC Project

A Java-based backend setup for managing MySQL databases using JDBC and environment variables.

## 📂 Project Structure

```text
JDBC/
└── backend/
    ├── lib/                # External libraries (.jar)
    ├── src/                # Source code
    │   ├── sql/            # SQL scripts
    │   └── CreateDB.java   # Main initialization logic
    ├── bin/                # Compiled .class files (auto-generated)
    └── .env                # Environment configuration
```

## 🛠️ Prerequisites & Dependencies

To run this project manually without Maven/Gradle, you must download the following JAR files and place them in the `backend/lib/` folder:

- **MySQL Connector/J**: [mysql-connector-j-9.1.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar)
- **Dotenv Java**: [dotenv-java-3.0.0.jar](https://repo1.maven.org/maven2/io/github/cdimascio/dotenv-java/3.0.0/dotenv-java-3.0.0.jar)
  
## ⚙️ Setup Instructions

### Environment Configuration
Create a `.env` file in the `backend/src/` directory with the following variables:

```env
DB_URL=jdbc:mysql://localhost:3306/
DB_USER=your_username
DB_PASS=your_password
```

## 🚀 Compilation and Execution
Run these commands from the `backend/` directory in your terminal:

**Compile:**
```powershell
javac -d bin -cp "lib/*" src/CreateDB.java
```
 **Command Breakdown**
* **`-d bin`**: Directs the compiled `.class` files into the `bin` folder.
* **`-cp "lib/*"`**: Includes all JAR files in the library folder in the compilation classpath.

**Run:**
```powershell
java -cp "bin;lib/*" CreateDB
```
* **`"bin;lib/*"`**: Tells the Java Virtual Machine to look for your code in `bin` and dependencies in `lib`.



---

## 📝 Features
* [x] **Automated Database Creation**: Checks for database existence before creation.
* [x] **Environment Management**: Securely loads credentials via `.env`.
* [x] **SQL Script Execution**: Reads and executes complex SQL from external files.

---
