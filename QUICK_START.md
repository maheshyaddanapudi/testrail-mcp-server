
# TestRail MCP Server - Quick Start Guide

This guide provides step-by-step instructions to get the TestRail MCP Server up and running in your local environment and integrated with Cursor.

---

## 1. Prerequisites

Ensure you have the following software installed on your system:

- **Java Development Kit (JDK)**: Version 17 or higher
- **Git**: For cloning the repository

---

## 2. Clone the Repository

Open your terminal and clone the repository to your local machine:

```bash
git clone https://github.com/maheshyaddanapudi/testrail-mcp-server.git
cd testrail-mcp-server
```

---

## 3. Configure Environment Variables

This server requires three environment variables to connect to your TestRail instance. You can set them in your system's environment or create a `.env` file in the project root.

**Create a `.env` file:**

```bash
# In the testrail-mcp-server directory

TESTRAIL_URL=https://yourcompany.testrail.io
TESTRAIL_USERNAME=your.email@company.com
TESTRAIL_API_KEY=your-api-key
```

- **`TESTRAIL_URL`**: The base URL of your TestRail instance.
- **`TESTRAIL_USERNAME`**: The email address you use to log in to TestRail.
- **`TESTRAIL_API_KEY`**: Your personal API key generated from your TestRail user settings.

---

## 4. Build the Application

The project includes a Gradle wrapper, so you don't need to install Gradle manually. Run the following command to build the executable JAR file:

```bash
# For Linux/macOS
./gradlew clean build

# For Windows
.\gradlew.bat clean build
```

This will compile the code, run all tests, and create the JAR file in the `build/libs/` directory. The file will be named something like `testrail-mcp-server-1.0.0.jar`.

---

## 5. Configure Cursor MCP Server

Now, you need to tell Cursor how to run the TestRail MCP server. 

1. In Cursor, press `Cmd/Ctrl + Shift + P` to open the command palette.
2. Type `mcp` and select **"Configure MCP Servers"**.
3. This will open the `mcp.json` file. Add the following configuration:

```json
{
  "mcpServers": {
    "testrail": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/your/testrail-mcp-server/build/libs/testrail-mcp-server-1.0.0.jar"
      ],
      "env": {
        "MCP_MODE": "true",
        "TESTRAIL_URL": "https://yourcompany.testrail.io",
        "TESTRAIL_USERNAME": "your.email@company.com",
        "TESTRAIL_API_KEY": "your-api-key"
      }
    }
  }
}
```

**IMPORTANT:**
- Replace `/absolute/path/to/your/` with the **full, absolute path** to the project directory on your machine.
- Ensure the JAR file version in the path matches the one you built.
- The `env` section in `mcp.json` is an alternative to the `.env` file. You can use either, but the `mcp.json` configuration will override system environment variables.
- **`MCP_MODE: "true"`** is crucial. It ensures the server process is properly managed by Cursor and prevents orphaned processes when Cursor restarts.

---

## 6. Restart Cursor

For the MCP server configuration to take effect, you must **fully quit and restart Cursor**.

---

## 7. Verify the Integration

Once Cursor has restarted, you can verify that the TestRail MCP server is running and integrated correctly.

1. Open a new chat in Cursor.
2. Type `@testrail` to ensure the tool is recognized.
3. Ask a question to invoke one of the tools:

> **"Hey @testrail, what tools do you have?"**

Cursor should first respond with the four main gateway tools:

- `search_tools`
- `get_categories`
- `get_tools_by_category`
- `execute_tool`

This confirms the gateway is working. Now, you can test the discovery features. For example:

> **"OK, @testrail, what tool categories are there?"**

Cursor should now use the `get_categories` tool and respond with a list of available categories, such as:
- `attachments`
- `test-cases`
- `projects`
- `test-runs`
- ...and 15 more.

Alternatively, you can use search:

> **"@testrail, search for tools to 'add a new test case'"**

Cursor will use the `search_tools` function and likely return the `add_case` tool as the top result.

If you see this list, your TestRail MCP server is successfully integrated! 🎉

---

## Troubleshooting

- **"Command not found"**: Double-check the absolute path to the JAR file in `mcp.json`.
- **Authentication errors**: Verify that your `TESTRAIL_URL`, `TESTRAIL_USERNAME`, and `TESTRAIL_API_KEY` are correct.
- **No response from `@testrail`**: Ensure you have restarted Cursor after configuring `mcp.json`.
- **Build failures**: Make sure you have JDK 17+ installed and that you are in the project's root directory when running the build command.

