# AUTOMATED-SOFTWARE-REQUIREMENTS-SPECIFICATION-GENERATOR
An automated web-based tool designed to help developers and students create IEEE 830 Standard Software Requirements Specification (SRS) documents through a structured, multi-stage workflow.

## 📌 Project Overview
Writing an SRS in a standard word processor often leads to formatting errors, inconsistent styling, and a fragmented workflow. The **Automated SRS Generator** solves this by providing a "Guided Staging" process.

Users input project details stage-by-stage, and the system ensures data persistence using a database backend, eventually generating a formatted, professional document.

## ✨ Key Features
- **Staged Workflow:** Breaks the 30+ page SRS process into manageable sections (Introduction, Overall Description, Specific Requirements).

- **Progress Tracking:** Visual indicators show the current "Stage" of the project.

- **Data Persistence:** Saves progress in real-time so users can exit and resume later.

- **Structured Output:** Generates a consistent, IEEE-compliant document layout.

- **Conflict Prevention:** Unlike shared docs, the structured input prevents layout breaks caused by multiple contributors.

## 🔄 How it Works
1. **Initiation:** The user starts a new project and enters basic metadata.

2. **Guided Input:** The system prompts the user for specific IEEE sections (Scope, Constraints, Functional Reqs).

3. **Database Storage:** As each stage is completed, JDBC sends the data to MySQL.

4. **Final Generation:** The system pulls all saved sections and renders them into a single, printable SRS view.

## 🚀 Future Enhancements
- Export to PDF/Docx directly from the browser.
- Integrated UML Diagram builder using Mermaid.js.
- Collaborative "Team Mode" for simultaneous editing.