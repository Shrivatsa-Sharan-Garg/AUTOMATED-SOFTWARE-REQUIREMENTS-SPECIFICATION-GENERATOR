# SRS Generator Backend

The core Java backend for the Automated SRS Generator. This engine handles high-fidelity HTTP requests, manages the SQLite database lifecycle, and generates IEEE-standard documentation.

## 📂 Project Structure

```text
Backend/
├── bin/                 # Compiled .class files
├── lib/                 # Dependencies managed by jar-cart
├── src/                 # Source Code
│   ├── com/srs/         # Main Java Packages
│   │   ├── api/         # DocumentGenerator, handler, Router
│   │   ├── db/          # DBconnections
│   │   ├── ui/          # TUI
│   │   └── App.java     # Entry Point
│   ├── db/              # Auto-generated SQLite Database (srs.db)
│   ├── resources/       # JSON Templates
│   ├── sql/             # Initialization & Query Scripts
│   └── .env             # Environment Configuration (place in root)
├── jar-cart.json        # Dependency Manifest
├── jar-cart.lock        # Dependency Lockfile
├── .env
└── README.md
```

## 🛠️ Getting Started

This project uses **[jar-cart](https://github.com/Sudhanshu-Ambastha/jar-cart)** for zero-config dependency management, removing the need for Maven or Gradle.

### 1. Install jar-cart

Follow the installation instructions in the official repository:

https://github.com/Sudhanshu-Ambastha/jar-cart

### 2. Provision Dependencies

From the `Backend/` directory, synchronize all project dependencies:

```bash
jar-cart sync
```

`jar-cart` automatically:

- Downloads all required libraries from Maven repositories.
- Resolves and installs transitive dependencies.
- Caches artifacts globally for reuse across projects.
- Links dependencies into the project's `lib/` directory.

## 3. Run the Project

Compile and launch the interactive CLI:

```bash
jar-cart run src
```

Launch the HTTP server:

```bash
jar-cart run src -- --server
```

> `jar-cart` uses the `--` separator to forward application arguments to your Java program (similar to npm).

`jar-cart` automatically:

- Uses the project's configured Java runtime.
- Compiles every source file under `src/`.
- Resolves the complete classpath.
- Launches the application.

### Available Modes

| Command                        | Description                                            |
| ------------------------------ | ------------------------------------------------------ |
| `jar-cart run src`             | Starts the interactive terminal (TUI).                 |
| `jar-cart run src -- --server` | Starts the HTTP API server on `http://localhost:8080`. |

## ⚙️ Environment Configuration

Create a `.env` file inside the `Backend/` directory:

```env
DB_URL=jdbc:sqlite:src/db/srs.db
```

---

## ⚡ Recommended Setup: VS Code

For the best development experience, install the [**No-Build Java Power Pack 🛒☕**](https://marketplace.visualstudio.com/items?itemName=SudhanshuAmbastha.vscode-java-pack-no-build)

The extension integrates seamlessly with `jar-cart` and provides:

- One-click dependency synchronization.
- Integrated Java debugging.
- Test execution.
- IntelliSense and language support.
- A streamlined no-build Java workflow.

### Recommended Extensions

#### 📊 Database Management

[MySQL / SQLite Client](https://marketplace.visualstudio.com/items?itemName=cweijan.vscode-mysql-client2)

Use it to inspect:

```text
src/db/srs.db
```

#### 📄 PDF Preview

[vscode-pdf](https://marketplace.visualstudio.com/items?itemName=tomoki1207.pdf): Preview generated IEEE SRS documents directly inside VS Code.

---

## 📝 Backend Features

- ✅ Dual Execution Modes — Run as an interactive TUI or an HTTP API server.
- ✅ Automated PDF Engine — Generates IEEE-standard PDF reports through the `/api/download-pdf` endpoint.
- ✅ Zero-Config Dependency Management — Powered entirely by `jar-cart`.
- ✅ Automatic Dependency Resolution — Downloads and manages all required libraries automatically.
- ✅ Global Dependency Cache — Reuses previously downloaded artifacts across projects.
- ✅ Self-Healing Database — Automatically creates directories and initializes `srs.db` on first launch.
- ✅ CORS Support — Ready for seamless frontend dashboard integration.
- ✅ Lightweight Architecture — No Maven or Gradle required.
- ✅ Fast Prototyping Workflow — Focus on writing Java instead of managing build configurations.

---

## ⚖️ License

This project is licensed under the [**GNU General Public License v3.0**](../LICENSE).

See the `LICENSE` file for the complete license text.
