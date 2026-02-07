
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

Once Cursor has restarted, you can verify the integration by having a conversation with `@testrail`. Here are a few example flows.

### Example 1: Browse-Based Discovery

This path is useful when you want to explore the available capabilities.

1.  **Start the conversation and see the gateway tools.**
    > **You:** `@testrail what can you do?`
    > 
    > **Cursor:** _(Responds with the 4 gateway tools: `search_tools`, `get_categories`, `get_tools_by_category`, `execute_tool`)_

2.  **Discover the tool categories.**
    > **You:** `Okay, what are the categories?`
    > 
    > **Cursor:** _(Uses `get_categories` and lists all 19, including `projects`, `test-runs`, `test-cases`, etc.)_

3.  **Explore a specific category.**
    > **You:** `Interesting. What can I do with test runs?`
    > 
    > **Cursor:** _(Understands the intent, uses `get_tools_by_category` with `"test-runs"`, and lists the 6 tools for managing runs: `get_run`, `get_runs`, `add_run`, `update_run`, `close_run`, `delete_run`)_

4.  **Execute a tool using natural language.**
    > **You:** `Get me all the test runs for the "Mobile App Q1" project.`
    > 
    > **Cursor:** _(Now knows the tools exist. It will first call `get_project_by_name` to find the ID for "Mobile App Q1", then call `get_runs` with that project ID, and finally present you with the list of test runs.)_

### Example 2: Search-Based Discovery

This path is faster when you know what you want to do.

1.  **Search for a tool with a natural language query.**
    > **You:** `@testrail I need to add a new test case with a "high" priority.`
    > 
    > **Cursor:** _(Uses `search_tools` with a query like "add new test case with priority". It will find the `add_case` tool and identify its parameters: `section_id`, `title`, `template_id`, `type_id`, `priority_id`, etc.)_

2.  **Provide the necessary information.**
    > **Cursor:** `I can do that. What is the section ID for the new test case? What is its title?`
    > 
    > **You:** `Section is 42. Title is "Verify login with biometrics".`

3.  **Execute the tool.**
    > **Cursor:** _(Now has enough information. It calls `execute_tool` with the name `add_case` and the parameters `{section_id: 42, title: "Verify login with biometrics", priority_id: 1}`. It then confirms the new test case was created.)_

### Example 3: Advanced Query - Chaining Multiple Tools

This example demonstrates the server's true power, as the AI assistant chains multiple tools together to answer a complex, time-based question.

> **You:** `@testrail give me all test failures within the last 24 hours for the "Platform Engineering" project.`

Behind the scenes, Cursor will perform a series of steps:

1.  **Find Project ID:** It calls `get_projects` to find the project named "Platform Engineering" and get its ID.
2.  **Calculate Timestamp:** It calculates the UNIX timestamp for 24 hours ago.
3.  **Find Recent Runs:** It calls `get_runs` using the project ID and the `created_after` parameter with the timestamp it just calculated.
4.  **Aggregate Failures:** For each run returned, it calls `get_results_for_run` with the `status_id` set to `5` (Failed).
5.  **Synthesize and Respond:** Finally, it collects all the failed results from all the recent runs and presents them to you in a summarized, readable format.

If you can complete these flows, your TestRail MCP server is successfully integrated! 🎉

---

## Troubleshooting

- **"Command not found"**: Double-check the absolute path to the JAR file in `mcp.json`.
- **Authentication errors**: Verify that your `TESTRAIL_URL`, `TESTRAIL_USERNAME`, and `TESTRAIL_API_KEY` are correct.
- **No response from `@testrail`**: Ensure you have restarted Cursor after configuring `mcp.json`.
- **Build failures**: Make sure you have JDK 17+ installed and that you are in the project's root directory when running the build command.
