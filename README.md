# 💻 Java-Based Command-Line Shell

A lightweight, extensible, and feature-rich command-line interface (CLI) built entirely in **Java**. This shell mimics core terminal functionality, supporting pipes, redirection, command history, and a colorful UI, making it a powerful tool for interacting with the underlying operating system.

![Java](https://img.shields.io/badge/Language-Java-orange) ![License](https://img.shields.io/badge/License-MIT-blue) ![Platform](https://img.shields.io/badge/Platform-Cross--Platform-lightgrey)

## ✨ Features

### 🚀 Core Functionality
* **Command Execution:** Runs both built-in shell commands and external system programs (e.g., `git`, `python`, `notepad`).
* **Pipeline Support (`|`):** Chain commands together! The output of one command becomes the input of the next.
    * *Example:* `history | find "git"`
* **I/O Redirection:**
    * `>` : Overwrite output to a file.
    * `>>` : Append output to a file.
* **Quoted Arguments:** specific handling for single (`'`) and double (`"`) quotes to preserve spaces in arguments.

### 🛠 Built-in Commands
| Command | Description |
| :--- | :--- |
| **`cd <dir>`** | Change the current directory (supports `~`, absolute, and relative paths). |
| **`pwd`** | Print the current working directory. |
| **`ls`** | List files and directories in the current folder (Directories shown in **Blue**). |
| **`cat <file>`** | Read and display the contents of a file. |
| **`echo <text>`** | Print text to the console. |
| **`history`** | View the list of commands executed in the current session. |
| **`clear`** | Clear the terminal screen and scrollback history. |
| **`type <cmd>`** | Identify if a command is a shell builtin or an external executable. |
| **`exit`** | Close the shell session. |

### 🎨 UI & UX
* **Colorful Prompt:** Displays the current folder name in **Blue** and the prompt symbol (`$`) in **Green**.
* **Clean Output:** Optimized for readability with ANSI color codes.
* **Smart Path Resolution:** Automatically detects executable extensions on Windows (`.exe`, `.bat`, `.cmd`).

---

## 📂 Project Structure

The project follows a modular architecture for maintainability and scalability:

* **`Main.java`**: The core controller containing the REPL (Read-Eval-Print Loop) and built-in command logic.
* **`CommandParser.java`**: Handles tokenizing user input, respecting quotes and escape characters.
* **`ProcessRunner.java`**: Manages the execution of external processes and handles piping/redirection logic.
* **`Builtin.java`**: Enum defining the registry of supported internal commands.

---

## ⚙️ Installation & Usage

### Prerequisites
* **Java Development Kit (JDK) 22** or higher (Compatible with recent Java versions).

### How to Run
1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/vishalchoudhary07/java-based-shell-project.git](https://github.com/vishalchoudhary07/java-based-shell-project.git)
    ```

2.  **Navigate to the source directory:**
    ```bash
    cd java-based-shell-project/src/main/java
    ```

3.  **Compile the Java files:**
    ```bash
    javac *.java
    ```

4.  **Start the Shell:**
    ```bash
    java Main
    ```

---

## 💡 Usage Examples

**1. File Navigation & Listing**
```bash
java $ ls
src/
README.md
java $ cd src
src $ pwd
C:\Projects\java-shell\src