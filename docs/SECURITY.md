# Security Documentation

This document details the security architecture of the TestRail MCP Server, explaining how credentials are protected and why this approach is secure.

## Table of Contents

- [Security Overview](#security-overview)
- [Credential Management](#credential-management)
- [Data Flow Security](#data-flow-security)
- [Permission Model](#permission-model)
- [Threat Model](#threat-model)
- [Best Practices](#best-practices)
- [Compliance Considerations](#compliance-considerations)

---

## Security Overview

### Core Security Principle

```mermaid
flowchart TB
    subgraph Principle["🔐 Core Security Principle"]
        direction TB
        P1["API credentials NEVER leave your local machine"]
        P2["API credentials are NEVER sent to the LLM"]
        P3["All TestRail API calls happen locally"]
    end

    style Principle fill:#27ae60,color:#fff
```

### Security Architecture Overview

```mermaid
flowchart TB
    subgraph Cloud["☁️ Cloud (Untrusted Zone)"]
        LLM["Claude LLM<br/>━━━━━━━━━━━━<br/>Processes natural language<br/>Selects appropriate tools<br/>Formats responses"]
    end

    subgraph Local["🏠 Local Machine (Trusted Zone)"]
        subgraph Cursor["Cursor IDE"]
            UserInput["User Input"]
            ToolExec["Tool Executor"]
            Display["Response Display"]
        end

        subgraph MCP["MCP Server Process"]
            Transport["STDIO Transport"]
            Tools["Tool Handlers"]
            Client["API Client"]
        end

        subgraph Secure["🔒 Secure Storage"]
            Env["Environment Variables"]
            Creds["Credentials"]
        end
    end

    subgraph External["🌐 External (Authenticated Zone)"]
        TestRail["TestRail API"]
    end

    UserInput --> LLM
    LLM --> ToolExec
    ToolExec --> Transport
    Transport --> Tools
    Tools --> Client

    Creds --> Client

    Client <-->|"HTTPS + Auth"| TestRail

    Display --> UserInput

    Tools --> Display

    style Cloud fill:#e74c3c,color:#fff
    style Local fill:#27ae60,color:#fff
    style Secure fill:#f39c12,color:#fff
```

---

## Credential Management

### Credential Storage

```mermaid
flowchart LR
    subgraph Storage["Credential Storage Options"]
        direction TB
        Env["Environment Variables<br/>━━━━━━━━━━━━<br/>✓ Recommended<br/>✓ Not in code<br/>✓ Easy to rotate"]

        Config["mcp.json env section<br/>━━━━━━━━━━━━<br/>✓ Per-project<br/>✓ Not in git<br/>⚠ Plaintext file"]

        AppYaml["application.yml<br/>━━━━━━━━━━━━<br/>⚠ Only for defaults<br/>✗ Never store secrets"]
    end

    Env --> Best["✓ Best Practice"]
    Config --> Acceptable["⚠ Acceptable"]
    AppYaml --> Avoid["✗ Avoid for secrets"]

    style Env fill:#27ae60,color:#fff
    style Best fill:#27ae60,color:#fff
    style Config fill:#f39c12,color:#fff
    style Acceptable fill:#f39c12,color:#fff
    style AppYaml fill:#e74c3c,color:#fff
    style Avoid fill:#e74c3c,color:#fff
```

### Credential Loading Flow

```mermaid
sequenceDiagram
    participant OS as Operating System
    participant Cursor as Cursor IDE
    participant MCP as MCP Server
    participant Config as TestrailProperties

    Note over OS: Environment variables set<br/>by user (secure)

    Cursor->>MCP: Launch with env vars
    MCP->>Config: Spring loads @ConfigurationProperties

    Config->>OS: Read TESTRAIL_URL
    OS-->>Config: https://company.testrail.io

    Config->>OS: Read TESTRAIL_USERNAME
    OS-->>Config: user@company.com

    Config->>OS: Read TESTRAIL_API_KEY
    OS-->>Config: ********** (masked in logs)

    Config->>Config: Validate @NotBlank

    Note over MCP,Config: Credentials stored in memory only<br/>Never written to disk by application
```

### Credential Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Configured: User sets env vars

    Configured --> Loaded: MCP Server starts
    Loaded --> InMemory: Properties validated

    InMemory --> Used: API calls made
    Used --> InMemory: Call complete

    InMemory --> Cleared: Server stops
    Cleared --> [*]

    note right of InMemory
        Credentials exist only
        in process memory
    end note

    note right of Used
        Sent only to TestRail
        via HTTPS
    end note
```

---

## Data Flow Security

### What Data Goes Where

```mermaid
flowchart TB
    subgraph DataTypes["Data Classification"]
        Creds["🔴 Credentials<br/>(API Key, Password)"]
        ToolDef["🟢 Tool Definitions<br/>(Public metadata)"]
        ToolParams["🟡 Tool Parameters<br/>(User data)"]
        Results["🟡 Tool Results<br/>(TestRail data)"]
        Prompts["🟢 User Prompts<br/>(Natural language)"]
    end

    subgraph Destinations["Destinations"]
        LLM["Claude LLM"]
        Local["Local Process"]
        TR["TestRail API"]
        Logs["Log Files"]
    end

    Creds -->|"✗ NEVER"| LLM
    Creds -->|"✓ Memory only"| Local
    Creds -->|"✓ Auth header"| TR
    Creds -->|"✗ NEVER"| Logs

    ToolDef -->|"✓ Always"| LLM
    ToolParams -->|"✓ Yes"| LLM
    Results -->|"✓ Sanitized"| LLM
    Prompts -->|"✓ Yes"| LLM

    style Creds fill:#e74c3c,color:#fff
    style ToolDef fill:#27ae60,color:#fff
    style ToolParams fill:#f39c12,color:#fff
    style Results fill:#f39c12,color:#fff
    style Prompts fill:#27ae60,color:#fff
```

### Network Security

```mermaid
sequenceDiagram
    participant IDE as Cursor IDE
    participant LLM as Claude (Cloud)
    participant MCP as MCP Server
    participant TR as TestRail

    Note over IDE,LLM: Connection 1: IDE ↔ LLM<br/>Anthropic's secure API

    IDE->>LLM: User prompt (no credentials)
    LLM-->>IDE: Tool selection

    Note over IDE,MCP: Connection 2: IDE ↔ MCP<br/>Local STDIO (same machine)

    IDE->>MCP: Tool call via STDIO
    MCP-->>IDE: Tool result

    Note over MCP,TR: Connection 3: MCP ↔ TestRail<br/>HTTPS with Basic Auth

    MCP->>TR: API request + Auth header
    TR-->>MCP: API response

    Note over IDE,TR: Credentials only travel<br/>in Connection 3 (HTTPS)
```

### Request Sanitization

```mermaid
flowchart LR
    subgraph Input["Tool Result from TestRail"]
        Raw["Raw Response<br/>━━━━━━━━━━━━<br/>May contain:<br/>• Internal URLs<br/>• User emails<br/>• System info"]
    end

    subgraph Process["Sanitization"]
        Filter["Remove sensitive<br/>metadata if needed"]
        Transform["Transform to<br/>domain model"]
    end

    subgraph Output["To LLM"]
        Clean["Sanitized Result<br/>━━━━━━━━━━━━<br/>Only contains:<br/>• Requested data<br/>• Safe metadata"]
    end

    Raw --> Filter --> Transform --> Clean

    style Raw fill:#f39c12,color:#fff
    style Clean fill:#27ae60,color:#fff
```

---

## Permission Model

### Tool Execution Confirmation

```mermaid
sequenceDiagram
    participant User
    participant Cursor
    participant LLM as Claude
    participant MCP as MCP Server

    User->>Cursor: "Delete test case C123"
    Cursor->>LLM: Prompt + available tools
    LLM->>Cursor: Select deleteTestCase(123)

    rect rgb(255, 200, 200)
        Note over Cursor,User: 🔔 Confirmation Required
        Cursor->>User: "Allow deleteTestCase(caseId=123)?"
        Note over User: User reviews parameters
        alt User Approves
            User->>Cursor: [Allow]
            Cursor->>MCP: Execute tool
            MCP-->>Cursor: Success
        else User Denies
            User->>Cursor: [Deny]
            Cursor->>LLM: Tool denied
            LLM-->>User: "Operation cancelled"
        end
    end
```

### Permission Levels

```mermaid
flowchart TD
    subgraph Levels["Permission Levels"]
        direction TB

        L1["Level 1: Ask Every Time<br/>━━━━━━━━━━━━<br/>Default behavior<br/>Maximum security"]

        L2["Level 2: Allow for Session<br/>━━━━━━━━━━━━<br/>Click 'Allow All'<br/>Convenient for batch ops"]

        L3["Level 3: Trust Specific Tools<br/>━━━━━━━━━━━━<br/>Configure in settings<br/>For read-only tools"]
    end

    L1 -->|"Less secure<br/>More convenient"| L2
    L2 -->|"Less secure<br/>More convenient"| L3

    style L1 fill:#27ae60,color:#fff
    style L2 fill:#f39c12,color:#fff
    style L3 fill:#e74c3c,color:#fff
```

### Tool Risk Classification

```mermaid
quadrantChart
    title Tool Risk vs Frequency Matrix
    x-axis Low Frequency --> High Frequency
    y-axis Low Risk --> High Risk

    quadrant-1 Monitor Carefully
    quadrant-2 Require Confirmation
    quadrant-3 Consider Auto-Allow
    quadrant-4 Standard Confirmation

    deleteTestCase: [0.2, 0.9]
    deleteProject: [0.1, 0.95]
    deleteRun: [0.15, 0.85]
    deleteSection: [0.15, 0.85]

    addTestCase: [0.6, 0.3]
    updateTestCase: [0.7, 0.4]
    addResult: [0.8, 0.3]
    addRun: [0.5, 0.35]

    getTestCase: [0.9, 0.05]
    getTestCases: [0.85, 0.05]
    getProjects: [0.7, 0.05]
    getRun: [0.75, 0.05]
```

---

## Threat Model

### Potential Threats and Mitigations

```mermaid
flowchart TB
    subgraph Threats["Potential Threats"]
        T1["Credential Exposure<br/>to LLM"]
        T2["Unauthorized<br/>Operations"]
        T3["Data Exfiltration<br/>via LLM"]
        T4["Man-in-the-Middle<br/>Attack"]
        T5["Malicious Tool<br/>Invocation"]
    end

    subgraph Mitigations["Mitigations"]
        M1["Credentials stored<br/>locally only"]
        M2["User confirmation<br/>before execution"]
        M3["Results sanitized<br/>before sending"]
        M4["HTTPS for all<br/>external calls"]
        M5["Parameter validation<br/>in tools"]
    end

    T1 --> M1
    T2 --> M2
    T3 --> M3
    T4 --> M4
    T5 --> M5

    style Threats fill:#e74c3c,color:#fff
    style Mitigations fill:#27ae60,color:#fff
```

### Attack Surface Analysis

```mermaid
flowchart LR
    subgraph Surface["Attack Surface"]
        S1["Environment<br/>Variables"]
        S2["STDIO<br/>Communication"]
        S3["HTTPS<br/>Connection"]
        S4["Log Files"]
    end

    subgraph Protection["Protection"]
        P1["OS-level access<br/>control"]
        P2["Local only<br/>(same machine)"]
        P3["TLS encryption<br/>+ cert validation"]
        P4["No credentials<br/>in logs"]
    end

    S1 --- P1
    S2 --- P2
    S3 --- P3
    S4 --- P4

    style Surface fill:#f39c12,color:#fff
    style Protection fill:#27ae60,color:#fff
```

---

## Best Practices

### Credential Security

```mermaid
flowchart TB
    subgraph Do["✓ DO"]
        D1["Use environment variables"]
        D2["Rotate API keys regularly"]
        D3["Use least-privilege keys"]
        D4["Keep mcp.json out of git"]
    end

    subgraph Dont["✗ DON'T"]
        X1["Hardcode credentials"]
        X2["Share API keys"]
        X3["Use admin keys<br/>for testing"]
        X4["Log credentials"]
    end

    style Do fill:#27ae60,color:#fff
    style Dont fill:#e74c3c,color:#fff
```

### Secure Configuration Example

```mermaid
flowchart LR
    subgraph Secure["✓ Secure Setup"]
        direction TB
        E1["export TESTRAIL_URL=..."]
        E2["export TESTRAIL_USERNAME=..."]
        E3["export TESTRAIL_API_KEY=..."]
        MCP1["mcp.json references env vars"]
    end

    subgraph Insecure["✗ Insecure Setup"]
        direction TB
        I1["Credentials in mcp.json"]
        I2["mcp.json committed to git"]
    end

    style Secure fill:#27ae60,color:#fff
    style Insecure fill:#e74c3c,color:#fff
```

### API Key Recommendations

```mermaid
flowchart TD
    subgraph Recommendations["API Key Best Practices"]
        R1["Create dedicated API key<br/>for MCP Server"]
        R2["Use descriptive name<br/>'MCP-Server-Dev'"]
        R3["Review key permissions<br/>in TestRail admin"]
        R4["Rotate keys quarterly"]
        R5["Revoke keys when<br/>no longer needed"]
    end

    R1 --> R2 --> R3 --> R4 --> R5

    style Recommendations fill:#3498db,color:#fff
```

---

## Compliance Considerations

### Data Handling Summary

```mermaid
flowchart TB
    subgraph DataHandling["Data Handling by Location"]
        subgraph Cloud["Cloud (LLM)"]
            C1["Tool definitions"]
            C2["User prompts"]
            C3["Sanitized results"]
        end

        subgraph LocalMachine["Local Machine"]
            L1["Credentials (memory)"]
            L2["Full API responses"]
            L3["Debug logs (if enabled)"]
        end

        subgraph TestRail["TestRail Instance"]
            T1["All test data"]
            T2["Access logs"]
            T3["User activity"]
        end
    end

    style Cloud fill:#e74c3c,color:#fff
    style LocalMachine fill:#f39c12,color:#fff
    style TestRail fill:#3498db,color:#fff
```

### Audit Trail

```mermaid
sequenceDiagram
    participant User
    participant MCP as MCP Server
    participant TR as TestRail

    User->>MCP: Execute tool

    Note over MCP: Local logging (optional)<br/>- Tool name<br/>- Parameters (sanitized)<br/>- Timestamp<br/>- Result status

    MCP->>TR: API call

    Note over TR: TestRail audit log<br/>- User (from API key)<br/>- Action performed<br/>- Affected resources<br/>- Timestamp
```

### Security Checklist

```mermaid
flowchart TB
    subgraph Checklist["Security Checklist"]
        C1["☐ Environment variables configured"]
        C2["☐ mcp.json in .gitignore"]
        C3["☐ API key has appropriate permissions"]
        C4["☐ HTTPS verified for TestRail URL"]
        C5["☐ Debug logging disabled in production"]
        C6["☐ Team aware of tool permissions"]
    end

    style Checklist fill:#3498db,color:#fff
```
