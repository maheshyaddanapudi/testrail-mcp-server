# Integration Guide

This document provides comprehensive guidance for integrating the TestRail MCP Server with Cursor IDE and other MCP-compatible clients.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Cursor IDE Setup](#cursor-ide-setup)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Advanced Configuration](#advanced-configuration)

---

## Prerequisites

### System Requirements

```mermaid
flowchart TB
    subgraph Requirements["System Requirements"]
        direction TB
        Java["Java 17+<br/>━━━━━━━━━━━━<br/>Required runtime<br/>OpenJDK or Oracle JDK"]
        Cursor["Cursor IDE<br/>━━━━━━━━━━━━<br/>MCP-compatible IDE<br/>Latest version recommended"]
        TestRail["TestRail Access<br/>━━━━━━━━━━━━<br/>Valid account<br/>API access enabled"]
    end

    Java --> Build["Build & Run Server"]
    Cursor --> Connect["Connect via MCP"]
    TestRail --> API["API Authentication"]

    style Requirements fill:#3498db,color:#fff
```

### Checklist

```mermaid
flowchart LR
    subgraph Checklist["Pre-Installation Checklist"]
        C1["☐ Java 17+ installed"]
        C2["☐ JAVA_HOME configured"]
        C3["☐ Cursor IDE installed"]
        C4["☐ TestRail account ready"]
        C5["☐ API key generated"]
    end

    C1 --> C2 --> C3 --> C4 --> C5

    style Checklist fill:#27ae60,color:#fff
```

---

## Installation

### Build Process

```mermaid
flowchart TB
    subgraph Build["Build Process"]
        Clone["Clone Repository<br/>━━━━━━━━━━━━<br/>git clone ..."]
        Navigate["Navigate to Directory<br/>━━━━━━━━━━━━<br/>cd testrail-mcp-server"]
        GradleBuild["Build with Gradle<br/>━━━━━━━━━━━━<br/>./gradlew build"]
        Verify["Verify JAR Created<br/>━━━━━━━━━━━━<br/>build/libs/*.jar"]
    end

    Clone --> Navigate --> GradleBuild --> Verify

    style Build fill:#3498db,color:#fff
```

### Build Commands

```bash
# Clone the repository
git clone https://github.com/your-org/testrail-mcp-server.git
cd testrail-mcp-server

# Build the project
./gradlew build

# Run tests with coverage
./gradlew test jacocoTestReport

# Build without tests (faster)
./gradlew build -x test
```

### Build Output Structure

```mermaid
flowchart TB
    subgraph Output["Build Output"]
        Build["build/"]
        Libs["libs/<br/>━━━━━━━━━━━━<br/>testrail-mcp-server.jar"]
        Reports["reports/<br/>━━━━━━━━━━━━<br/>Test reports<br/>Coverage reports"]
        Classes["classes/<br/>━━━━━━━━━━━━<br/>Compiled classes"]
    end

    Build --> Libs
    Build --> Reports
    Build --> Classes

    style Libs fill:#27ae60,color:#fff
```

---

## Configuration

### Environment Variables

```mermaid
flowchart TB
    subgraph EnvVars["Environment Variables"]
        direction TB
        URL["TESTRAIL_URL<br/>━━━━━━━━━━━━<br/>Your TestRail instance URL<br/>Example: https://company.testrail.io"]
        User["TESTRAIL_USERNAME<br/>━━━━━━━━━━━━<br/>Your TestRail email<br/>Example: user@company.com"]
        Key["TESTRAIL_API_KEY<br/>━━━━━━━━━━━━<br/>Your API key<br/>From TestRail settings"]
    end

    URL --> Required["All Required"]
    User --> Required
    Key --> Required

    style EnvVars fill:#f39c12,color:#fff
    style Required fill:#e74c3c,color:#fff
```

### Setting Environment Variables

```mermaid
flowchart LR
    subgraph Methods["Configuration Methods"]
        Shell["Shell Export<br/>━━━━━━━━━━━━<br/>export VAR=value<br/>Session only"]
        Profile["Shell Profile<br/>━━━━━━━━━━━━<br/>~/.bashrc or ~/.zshrc<br/>Persistent"]
        MCPJson["mcp.json env<br/>━━━━━━━━━━━━<br/>Per-project<br/>Cursor-specific"]
    end

    Shell --> Temp["Temporary"]
    Profile --> Perm["Permanent"]
    MCPJson --> Project["Project-specific"]

    style Profile fill:#27ae60,color:#fff
    style MCPJson fill:#3498db,color:#fff
```

#### Option 1: Shell Profile (Recommended)

```bash
# Add to ~/.bashrc or ~/.zshrc
export TESTRAIL_URL="https://your-company.testrail.io"
export TESTRAIL_USERNAME="your.email@company.com"
export TESTRAIL_API_KEY="your-api-key-here"

# Reload profile
source ~/.bashrc  # or source ~/.zshrc
```

#### Option 2: mcp.json env Section

```json
{
  "mcpServers": {
    "testrail": {
      "command": "java",
      "args": ["-jar", "/path/to/testrail-mcp-server.jar"],
      "env": {
        "TESTRAIL_URL": "https://your-company.testrail.io",
        "TESTRAIL_USERNAME": "your.email@company.com",
        "TESTRAIL_API_KEY": "your-api-key-here"
      }
    }
  }
}
```

---

## Cursor IDE Setup

### Configuration Flow

```mermaid
sequenceDiagram
    participant User
    participant Cursor as Cursor IDE
    participant Config as mcp.json
    participant MCP as MCP Server
    participant TR as TestRail

    Note over User,Config: Step 1: Create Configuration
    User->>Config: Create ~/.cursor/mcp.json

    Note over Cursor,MCP: Step 2: Launch Server
    Cursor->>MCP: Start MCP Server process
    MCP->>MCP: Load credentials from env

    Note over MCP,TR: Step 3: Validate Connection
    MCP->>TR: Test API connection
    TR-->>MCP: Connection OK

    Note over Cursor,MCP: Step 4: Ready for Use
    MCP-->>Cursor: Tools available
    Cursor-->>User: Ready to use TestRail tools
```

### mcp.json Location

```mermaid
flowchart TB
    subgraph Locations["mcp.json Locations"]
        Global["Global Configuration<br/>━━━━━━━━━━━━<br/>~/.cursor/mcp.json<br/>Applies to all projects"]
        Project["Project Configuration<br/>━━━━━━━━━━━━<br/>.cursor/mcp.json<br/>Project-specific"]
    end

    Global --> Priority["Global loads first"]
    Project --> Priority
    Priority --> Merged["Configurations merged<br/>Project overrides global"]

    style Global fill:#3498db,color:#fff
    style Project fill:#27ae60,color:#fff
```

### Complete mcp.json Example

```json
{
  "mcpServers": {
    "testrail": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/testrail-mcp-server.jar"
      ],
      "env": {
        "TESTRAIL_URL": "https://your-company.testrail.io",
        "TESTRAIL_USERNAME": "your.email@company.com",
        "TESTRAIL_API_KEY": "your-api-key-here"
      }
    }
  }
}
```

> **Note:** The server automatically manages its lifecycle and shuts down gracefully when Cursor disconnects, preventing orphaned Java processes.

### Step-by-Step Setup

```mermaid
flowchart TB
    subgraph Steps["Setup Steps"]
        S1["1. Build JAR<br/>━━━━━━━━━━━━<br/>./gradlew build"]
        S2["2. Note JAR Path<br/>━━━━━━━━━━━━<br/>build/libs/testrail-mcp-server.jar"]
        S3["3. Create mcp.json<br/>━━━━━━━━━━━━<br/>~/.cursor/mcp.json"]
        S4["4. Add Configuration<br/>━━━━━━━━━━━━<br/>Server command & env"]
        S5["5. Restart Cursor<br/>━━━━━━━━━━━━<br/>Reload configuration"]
        S6["6. Verify Tools<br/>━━━━━━━━━━━━<br/>Check MCP panel"]
    end

    S1 --> S2 --> S3 --> S4 --> S5 --> S6

    style Steps fill:#3498db,color:#fff
```

---

## Verification

### Verification Process

```mermaid
sequenceDiagram
    participant User
    participant Cursor as Cursor IDE
    participant Claude as Claude AI
    participant MCP as MCP Server

    User->>Cursor: Open Cursor IDE
    Cursor->>MCP: Initialize MCP connection

    alt Success
        MCP-->>Cursor: Tools registered
        Cursor-->>User: MCP indicator shows connected

        User->>Claude: "List all TestRail projects"
        Claude->>MCP: getProjects()
        MCP-->>Claude: Project list
        Claude-->>User: Formatted project list
    else Failure
        MCP-->>Cursor: Connection error
        Cursor-->>User: Error indicator
        Note over User: Check troubleshooting
    end
```

### Testing the Connection

```mermaid
flowchart TB
    subgraph Tests["Verification Tests"]
        T1["Test 1: List Projects<br/>━━━━━━━━━━━━<br/>'Show me all TestRail projects'"]
        T2["Test 2: Get Project Details<br/>━━━━━━━━━━━━<br/>'Get details for project ID 1'"]
        T3["Test 3: List Test Cases<br/>━━━━━━━━━━━━<br/>'Show test cases in project 1'"]
    end

    T1 --> |"Success"| T2
    T2 --> |"Success"| T3
    T3 --> Ready["✓ Integration Complete"]

    T1 --> |"Failure"| Debug["Check Troubleshooting"]
    T2 --> |"Failure"| Debug
    T3 --> |"Failure"| Debug

    style Ready fill:#27ae60,color:#fff
    style Debug fill:#e74c3c,color:#fff
```

### Sample Verification Commands

Try these prompts in Cursor to verify the integration:

1. **Basic connectivity:**
   > "List all TestRail projects"

2. **Read operations:**
   > "Show me the test cases in project 1, section 2"

3. **Detailed view:**
   > "Get the details of test case C123"

---

## Troubleshooting

### Common Issues

```mermaid
flowchart TB
    subgraph Issues["Common Issues"]
        I1["Server Not Starting"]
        I2["Authentication Failed"]
        I3["Tools Not Appearing"]
        I4["Connection Timeout"]
        I5["Permission Denied"]
    end

    I1 --> S1["Check Java version<br/>Verify JAR path"]
    I2 --> S2["Verify API key<br/>Check URL format"]
    I3 --> S3["Restart Cursor<br/>Check mcp.json syntax"]
    I4 --> S4["Check network<br/>Verify TestRail URL"]
    I5 --> S5["Check API permissions<br/>Verify user role"]

    style Issues fill:#e74c3c,color:#fff
```

### Diagnostic Flow

```mermaid
flowchart TB
    Start["Issue Detected"] --> Q1{"Server starts?"}

    Q1 -->|No| Java["Check Java Installation"]
    Java --> JavaFix["java -version<br/>Should be 17+"]

    Q1 -->|Yes| Q2{"Tools visible?"}

    Q2 -->|No| Config["Check mcp.json"]
    Config --> ConfigFix["Validate JSON syntax<br/>Check file path"]

    Q2 -->|Yes| Q3{"Auth works?"}

    Q3 -->|No| Auth["Check Credentials"]
    Auth --> AuthFix["Verify API key<br/>Check URL format"]

    Q3 -->|Yes| Q4{"API calls work?"}

    Q4 -->|No| Network["Check Network"]
    Network --> NetworkFix["Test TestRail URL<br/>Check firewall"]

    Q4 -->|Yes| Success["✓ Working"]

    style Start fill:#f39c12,color:#fff
    style Success fill:#27ae60,color:#fff
```

### Error Messages and Solutions

```mermaid
flowchart LR
    subgraph Errors["Error Messages"]
        E1["'Invalid API key'"]
        E2["'Connection refused'"]
        E3["'404 Not Found'"]
        E4["'Permission denied'"]
    end

    subgraph Solutions["Solutions"]
        S1["Regenerate API key<br/>in TestRail settings"]
        S2["Check TESTRAIL_URL<br/>includes https://"]
        S3["Verify API endpoint<br/>Check TestRail version"]
        S4["Check user role<br/>in TestRail admin"]
    end

    E1 --> S1
    E2 --> S2
    E3 --> S3
    E4 --> S4

    style Errors fill:#e74c3c,color:#fff
    style Solutions fill:#27ae60,color:#fff
```

### Debug Logging

Enable debug logging for troubleshooting:

```yaml
# Add to application.yml or via environment
logging:
  level:
    io.github.testrail.mcp: DEBUG
```

Or set via environment:

```bash
export LOGGING_LEVEL_IO_GITHUB_TESTRAIL_MCP=DEBUG
```

---

## Advanced Configuration

### Custom Application Properties

```mermaid
flowchart TB
    subgraph Config["Configuration Sources"]
        direction TB
        Env["Environment Variables<br/>━━━━━━━━━━━━<br/>Highest priority<br/>TESTRAIL_*"]
        CmdLine["Command Line Args<br/>━━━━━━━━━━━━<br/>--testrail.url=..."]
        AppYaml["application.yml<br/>━━━━━━━━━━━━<br/>In JAR or external<br/>Default values"]
    end

    Env --> |"Overrides"| CmdLine
    CmdLine --> |"Overrides"| AppYaml

    style Env fill:#27ae60,color:#fff
```

### Multiple TestRail Instances

```mermaid
flowchart TB
    subgraph MultiInstance["Multiple Instances Setup"]
        subgraph Dev["Development"]
            D1["testrail-dev server"]
            D2["Dev credentials"]
        end

        subgraph Prod["Production"]
            P1["testrail-prod server"]
            P2["Prod credentials"]
        end
    end

    User["User"] --> Cursor["Cursor IDE"]
    Cursor --> D1
    Cursor --> P1

    style Dev fill:#f39c12,color:#fff
    style Prod fill:#27ae60,color:#fff
```

#### mcp.json for Multiple Instances

```json
{
  "mcpServers": {
    "testrail-dev": {
      "command": "java",
      "args": ["-jar", "/path/to/testrail-mcp-server.jar"],
      "env": {
        "TESTRAIL_URL": "https://dev.testrail.io",
        "TESTRAIL_USERNAME": "dev@company.com",
        "TESTRAIL_API_KEY": "dev-api-key"
      }
    },
    "testrail-prod": {
      "command": "java",
      "args": ["-jar", "/path/to/testrail-mcp-server.jar"],
      "env": {
        "TESTRAIL_URL": "https://prod.testrail.io",
        "TESTRAIL_USERNAME": "prod@company.com",
        "TESTRAIL_API_KEY": "prod-api-key"
      }
    }
  }
}
```

### JVM Configuration

```mermaid
flowchart LR
    subgraph JVMConfig["JVM Options"]
        Memory["Memory Settings<br/>━━━━━━━━━━━━<br/>-Xmx512m<br/>-Xms256m"]
        GC["Garbage Collection<br/>━━━━━━━━━━━━<br/>-XX:+UseG1GC"]
        Debug["Debug Options<br/>━━━━━━━━━━━━<br/>-Xdebug<br/>-agentlib:jdwp=..."]
    end

    style JVMConfig fill:#3498db,color:#fff
```

#### Example with JVM Options

```json
{
  "mcpServers": {
    "testrail": {
      "command": "java",
      "args": [
        "-Xmx512m",
        "-Xms256m",
        "-XX:+UseG1GC",
        "-jar",
        "/path/to/testrail-mcp-server.jar"
      ],
      "env": {
        "TESTRAIL_URL": "https://your-company.testrail.io",
        "TESTRAIL_USERNAME": "user@company.com",
        "TESTRAIL_API_KEY": "your-api-key"
      }
    }
  }
}
```

### Proxy Configuration

```mermaid
flowchart LR
    subgraph Proxy["Proxy Setup"]
        JVMProxy["JVM Proxy Settings<br/>━━━━━━━━━━━━<br/>-Dhttp.proxyHost=...<br/>-Dhttp.proxyPort=..."]
        EnvProxy["Environment Proxy<br/>━━━━━━━━━━━━<br/>HTTP_PROXY=...<br/>HTTPS_PROXY=..."]
    end

    JVMProxy --> Server["MCP Server"]
    EnvProxy --> Server
    Server --> Proxy2["Corporate Proxy"]
    Proxy2 --> TestRail["TestRail API"]

    style Proxy fill:#f39c12,color:#fff
```

#### Proxy Configuration Example

```json
{
  "mcpServers": {
    "testrail": {
      "command": "java",
      "args": [
        "-Dhttp.proxyHost=proxy.company.com",
        "-Dhttp.proxyPort=8080",
        "-Dhttps.proxyHost=proxy.company.com",
        "-Dhttps.proxyPort=8080",
        "-jar",
        "/path/to/testrail-mcp-server.jar"
      ],
      "env": {
        "TESTRAIL_URL": "https://your-company.testrail.io",
        "TESTRAIL_USERNAME": "user@company.com",
        "TESTRAIL_API_KEY": "your-api-key"
      }
    }
  }
}
```

---

## Quick Reference

### Essential Commands

| Task | Command |
|------|---------|
| Build | `./gradlew build` |
| Test | `./gradlew test` |
| Coverage Report | `./gradlew jacocoTestReport` |
| Clean Build | `./gradlew clean build` |

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `TESTRAIL_URL` | TestRail instance URL | `https://company.testrail.io` |
| `TESTRAIL_USERNAME` | Login email | `user@company.com` |
| `TESTRAIL_API_KEY` | API key from settings | `abc123...` |

### File Locations

| File | Location | Purpose |
|------|----------|---------|
| Global mcp.json | `~/.cursor/mcp.json` | All projects |
| Project mcp.json | `.cursor/mcp.json` | This project only |
| Built JAR | `build/libs/testrail-mcp-server.jar` | Server executable |

