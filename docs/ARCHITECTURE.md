# Architecture Documentation

This document provides a detailed overview of the TestRail MCP Server's architecture, including component diagrams, data flow, and the tool discovery mechanism.

## Table of Contents

- [System Overview](#system-overview)
- [Component Architecture](#component-architecture)
- [Data Flow](#data-flow)
- [Class Diagrams](#class-diagrams)
- [Package Structure](#package-structure)
- [Technology Stack](#technology-stack)

---

## System Overview

The TestRail MCP Server acts as a bridge between AI assistants (like Cursor) and TestRail, enabling natural language interactions with test management operations.

```mermaid
flowchart TB
    subgraph IDE["Cursor IDE"]
        UP[User Prompt]
        LLM[Claude LLM]
        UP --> LLM
    end

    subgraph MCP["MCP Server - Local Machine"]
        direction TB
        STDIO[STDIO Transport]
        McpExposed[4-Tool Gateway]
        Lucene[LuceneToolIndexService]
        Registry[InternalToolRegistry]

        subgraph Tools["Internal Tools (101)"]
            CT[Cases Tools]
            PT[Projects Tools]
            RT[Runs Tools]
            RST[Results Tools]
            ST[Sections Tools]
        end

        TC[TestRail API Client]

        STDIO --> McpExposed
        McpExposed --> Lucene
        McpExposed --> Registry
        Registry --> Tools
        Tools --> TC
    end

    subgraph Config["Local Configuration"]
        ENV[Environment Variables]
        CREDS["TESTRAIL_URL<br/>TESTRAIL_USERNAME<br/>TESTRAIL_API_KEY"]
        ENV --> CREDS
    end

    subgraph TR["TestRail Instance"]
        API[TestRail API v2]
    end

    LLM <-->|MCP Protocol| STDIO
    TC <-->|HTTPS + Basic Auth| API
    CREDS -.->|Loaded at startup| TC

    style CREDS fill:#ff6b6b,color:#fff
    style LLM fill:#a29bfe,color:#fff
    style API fill:#74b9ff,color:#fff
```

---

## Tool Discovery and Execution

The server does **not** expose all 101 internal tools directly to the MCP client. Doing so would consume a massive number of tokens in the LLM's context window, making it inefficient and expensive. Instead, it exposes a **4-tool gateway** that provides two distinct paths for discovering the internal tools:

1.  **Search Path**: A natural language, fuzzy-search endpoint (`search_tools`).
2.  **Browse Path**: A structured, categorical browsing endpoint (`get_categories` and `get_tools_by_category`).

Once a tool is discovered, the `execute_tool` endpoint is used to run it.

```mermaid
flowchart TD
    subgraph Client["MCP Client (LLM)"]
        direction LR
        Start((Start))
        Search[search_tools(query)]
        BrowseCat[get_categories()]
        BrowseTools[get_tools_by_category(category)]
    end

    subgraph Server["MCP Server"]
        direction TB
        subgraph Gateway["4-Tool Gateway"]
            G_Search[search_tools]
            G_BrowseCat[get_categories]
            G_BrowseTools[get_tools_by_category]
            G_Execute[execute_tool]
        end
        subgraph Discovery["Discovery Services"]
            Lucene[LuceneToolIndexService]
            Registry[InternalToolRegistry]
        end
        subgraph InternalTools["Internal Tools (101)"]
            GetCase[get_case]
            AddRun[add_run]
            UpdateProject[update_project]
        end
    end

    Start --> Search
    Start --> BrowseCat
    
    Search --> G_Search
    BrowseCat --> G_BrowseCat
    BrowseTools --> G_BrowseTools

    G_Search --> Lucene
    G_BrowseCat --> Registry
    G_BrowseTools --> Registry
    
    subgraph Results
        direction TB
        ToolList[Tool List + Details]
    end

    Lucene --> ToolList
    Registry --> ToolList

    subgraph Execution
        Execute[execute_tool(toolName, params)] --> G_Execute
        G_Execute -- Invokes by name via reflection --> InternalTools
    end

    ToolList --> Execute

    style Gateway fill:#a29bfe,color:#fff
    style Discovery fill:#74b9ff,color:#fff
    style InternalTools fill:#55efc4,color:#fff
```

---

## Component Architecture

### Layer Diagram

```mermaid
flowchart TB
    subgraph Presentation["Presentation Layer"]
        MCP["4-Tool Gateway<br/>(McpExposedTools)"]
    end

    subgraph Application["Application Layer"]
        subgraph Tools["Internal Tool Components (101)"]
            direction LR
            CasesTools["CasesTools"]
            ProjectsTools["ProjectsTools"]
            RunsTools["RunsTools"]
            ResultsTools["ResultsTools"]
            SectionsTools["SectionsTools"]
        end
    end

    subgraph Domain["Domain Layer"]
        subgraph Models["Domain Models"]
            direction LR
            TestCase["TestCase"]
            Project["Project"]
            TestRun["TestRun"]
            TestResult["TestResult"]
            Section["Section"]
        end
    end

    subgraph Infrastructure["Infrastructure Layer"]
        ApiClient["TestrailApiClient"]
        WebClient["WebClient<br/>(Spring WebFlux)"]
        Config["Configuration<br/>(TestrailProperties)"]
    end

    subgraph External["External"]
        TestRailAPI["TestRail API v2"]
    end

    MCP --> DiscoveryServices[Discovery Services]
        DiscoveryServices --> Tools
    Tools --> Models
    Tools --> ApiClient
    ApiClient --> WebClient
    Config --> ApiClient
    WebClient --> TestRailAPI

    style MCP fill:#3498db,color:#fff
    style Tools fill:#9b59b6,color:#fff
    style Models fill:#2ecc71,color:#fff
    style ApiClient fill:#e74c3c,color:#fff
```

### Component Interaction

```mermaid
sequenceDiagram
    participant C as MCP Client (LLM)
    participant S as MCP Server (Gateway)
    participant D as Discovery Services
    participant I as Internal Tool
    participant T as TestRail API

    C->>S: tools/call(search_tools, {query: "..."})
    activate S
    S->>D: search("...")
    activate D
    D-->>S: List<ToolDefinition>
    deactivate D
    S-->>C: JSON result with tool details
    deactivate S

    C->>S: tools/call(execute_tool, {toolName: "get_case", ...})
    activate S
    S->>I: getCase(1)
    activate I
    I->>T: GET /index.php?/api/v2/get_case/1
    activate T
    T-->>I: Test Case JSON
    deactivate T
    I-->>S: TestCase object
    deactivate I
    S-->>C: JSON result
    deactivate S
```

---

## Data Flow

### Request Flow

```mermaid
flowchart LR
    subgraph Input
        UP["User Prompt"]
    end

    subgraph Processing
        LLM["LLM Analysis"]
        TS["Tool Selection"]
        TE["Tool Execution"]
        RP["Response Processing"]
    end

    subgraph Output
        NL["Natural Language<br/>Response"]
    end

    UP --> LLM
    LLM --> TS
    TS --> TE
    TE --> RP
    RP --> NL

    style UP fill:#3498db,color:#fff
    style NL fill:#27ae60,color:#fff
```

### Tool Execution Flow

```mermaid
flowchart TD
    Start([Tool Invoked]) --> Validate{Validate<br/>Parameters}

    Validate -->|Invalid| Error[Return Error]
    Validate -->|Valid| BuildReq[Build API Request]

    BuildReq --> AddAuth[Add Authentication]
    AddAuth --> SendReq[Send HTTP Request]

    SendReq --> CheckResp{Response<br/>Status?}

    CheckResp -->|2xx| ParseSuccess[Parse Success Response]
    CheckResp -->|4xx/5xx| ParseError[Parse Error Response]

    ParseSuccess --> Transform[Transform to Domain Model]
    ParseError --> ThrowEx[Throw TestrailApiException]

    Transform --> Return([Return Result])
    ThrowEx --> Error
    Error --> Return

    style Start fill:#3498db,color:#fff
    style Return fill:#27ae60,color:#fff
    style Error fill:#e74c3c,color:#fff
```

### Authentication Flow

```mermaid
sequenceDiagram
    participant App as Application Startup
    participant Config as TestrailProperties
    participant Env as Environment
    participant Client as WebClient Builder

    App->>Config: Load configuration
    Config->>Env: Read TESTRAIL_URL
    Env-->>Config: https://company.testrail.io
    Config->>Env: Read TESTRAIL_USERNAME
    Env-->>Config: user@company.com
    Config->>Env: Read TESTRAIL_API_KEY
    Env-->>Config: ******* (secret)

    App->>Client: Create WebClient
    Client->>Client: Base64 encode credentials
    Client->>Client: Set Authorization header
    Client->>Client: Set base URL
    Client-->>App: Configured WebClient

    Note over App,Client: Credentials stored in memory only
    Note over Env: Credentials never logged or exposed
```

---

## Class Diagrams

### Core Classes

```mermaid
classDiagram
    class TestrailMcpServerApplication {
        +main(String[] args)
    }

    class TestrailProperties {
        -String baseUrl
        -String username
        -String apiKey
        +getApiUrl() String
    }

    class TestrailClientConfig {
        -TestrailProperties properties
        +testrailWebClient() WebClient
    }

    class TestrailApiClient {
        -WebClient webClient
        +getCase(Integer) TestCase
        +getCases(Integer, Integer, Integer, Integer, Integer) List~TestCase~
        +addCase(Integer, Map) TestCase
        +updateCase(Integer, Map) TestCase
        +deleteCase(Integer) void
        +getProject(Integer) Project
        +getProjects() List~Project~
        +getRun(Integer) TestRun
        +getRuns(Integer, Integer, Integer) List~TestRun~
        +addResult(Integer, Map) TestResult
        +getSection(Integer) Section
        +getSections(Integer, Integer, Integer, Integer) List~Section~
    }

    class TestrailApiException {
        -int statusCode
        -String responseBody
        +isNotFound() boolean
        +isAuthenticationError() boolean
    }

    TestrailClientConfig --> TestrailProperties
    TestrailApiClient --> TestrailApiException
    TestrailClientConfig ..> TestrailApiClient : creates
```

### Domain Models

```mermaid
classDiagram
    class TestCase {
        +Integer id
        +String title
        +Integer sectionId
        +Integer templateId
        +Integer typeId
        +Integer priorityId
        +String refs
        +String preconditions
        +String steps
        +String expectedResult
        +Long createdOn
        +Long updatedOn
    }

    class Project {
        +Integer id
        +String name
        +String announcement
        +Boolean showAnnouncement
        +Boolean isCompleted
        +Integer suiteMode
        +getSuiteModeDescription() String
    }

    class TestRun {
        +Integer id
        +String name
        +String description
        +Integer suiteId
        +Integer milestoneId
        +Boolean includeAll
        +Boolean isCompleted
        +Integer passedCount
        +Integer failedCount
        +Integer blockedCount
        +Integer untestedCount
        +getTotalCount() int
    }

    class TestResult {
        +Integer id
        +Integer testId
        +Integer statusId
        +String comment
        +String defects
        +String elapsed
        +String version
        +getStatusDescription() String
    }

    class Section {
        +Integer id
        +String name
        +String description
        +Integer parentId
        +Integer suiteId
        +Integer depth
        +isRootSection() boolean
    }

    class OperationResult {
        +boolean success
        +String message
        +success(String) OperationResult$
        +failure(String) OperationResult$
    }

    TestCase "many" --o "1" Section : belongs to
    TestRun "many" --o "1" Project : belongs to
    TestResult "many" --o "1" TestRun : belongs to
```

### Tool Classes

```mermaid
classDiagram
    class McpExposedTools {
        +search_tools(String): String
        +get_categories(): String
        +get_tools_by_category(String): String
        +execute_tool(String, String): String
    }

    class CasesTools {
        -TestrailApiClient apiClient
        +getTestCase(Integer) TestCase
        +getTestCases(Integer, Integer, Integer, Integer, Integer) List~TestCase~
        +addTestCase(Integer, String, String, String, String, Integer, Integer, String) TestCase
        +updateTestCase(Integer, String, String, String, String, Integer, Integer, String) TestCase
        +deleteTestCase(Integer) OperationResult
        +cloneTestCase(Integer, Integer, String, String, String) TestCase
    }

    class ProjectsTools {
        -TestrailApiClient apiClient
        +getProject(Integer) Project
        +getProjects() List~Project~
        +addProject(String, String, Boolean, Integer) Project
        +updateProject(Integer, String, String, Boolean, Boolean) Project
        +deleteProject(Integer) OperationResult
    }

    class RunsTools {
        -TestrailApiClient apiClient
        +getRun(Integer) TestRun
        +getRuns(Integer, Integer, Integer) List~TestRun~
        +addRun(Integer, String, String, Integer, Integer, Integer, Boolean, String) TestRun
        +updateRun(Integer, String, String, Integer, Integer) TestRun
        +closeRun(Integer) TestRun
        +deleteRun(Integer) OperationResult
    }

    class ResultsTools {
        -TestrailApiClient apiClient
        +getResults(Integer, Integer, Integer) List~TestResult~
        +getResultsForRun(Integer, Integer, Integer) List~TestResult~
        +addResult(Integer, Integer, String, String, String, String) TestResult
        +addResults(Integer, String, Integer, String) List~TestResult~
        +addResultsForCases(Integer, String, Integer, String) List~TestResult~
    }

    class SectionsTools {
        -TestrailApiClient apiClient
        +getSection(Integer) Section
        +getSections(Integer, Integer, Integer, Integer) List~Section~
        +addSection(Integer, String, String, Integer, Integer) Section
        +updateSection(Integer, String, String) Section
        +deleteSection(Integer, Boolean) OperationResult
        +moveSection(Integer, Integer, Integer) Section
    }

    class TestrailApiClient {
        <<interface>>
    }

    CasesTools --> TestrailApiClient
    ProjectsTools --> TestrailApiClient
    RunsTools --> TestrailApiClient
    ResultsTools --> TestrailApiClient
    SectionsTools --> TestrailApiClient
```

---

## Package Structure

```mermaid
flowchart TD
    subgraph root["io.github.testrail.mcp"]
        App["TestrailMcpServerApplication"]

        subgraph config["config"]
            Props["TestrailProperties"]
            ClientConfig["TestrailClientConfig"]
        end

        subgraph client["client"]
            ApiClient["TestrailApiClient"]
            ApiException["TestrailApiException"]
        end

        subgraph model["model"]
            TC["TestCase"]
            Proj["Project"]
            Run["TestRun"]
            Result["TestResult"]
            Sec["Section"]
            Op["OperationResult"]
            Page["PaginatedResponse"]
            Step["TestStep"]
        end

        subgraph tools["tools"]
            subgraph annotation["annotation"]
                Cat["ToolCategory"]
            end
            subgraph cases["cases"]
                CTools["CasesTools"]
            end
            subgraph projects["projects"]
                PTools["ProjectsTools"]
            end
            subgraph runs["runs"]
                RTools["RunsTools"]
            end
            subgraph results["results"]
                ResTools["ResultsTools"]
            end
            subgraph sections["sections"]
                STools["SectionsTools"]
            end
        end
    end

    App --> config
    App --> tools
    tools --> client
    tools --> model
    client --> model

    style App fill:#3498db,color:#fff
    style config fill:#e74c3c,color:#fff
    style client fill:#9b59b6,color:#fff
    style model fill:#27ae60,color:#fff
    style tools fill:#f39c12,color:#fff
```

---

## Technology Stack

```mermaid
flowchart LR
    subgraph Runtime["Runtime"]
        Java["Java 17 LTS"]
    end

    subgraph Framework["Framework"]
        Boot["Spring Boot 3.3"]
        AI["Spring AI MCP"]
        Flux["Spring WebFlux"]
    end

    subgraph Libraries["Libraries"]
        Jackson["Jackson JSON"]
        Validation["Jakarta Validation"]
    end

    subgraph Build["Build"]
        Gradle["Gradle 8.x"]
        JaCoCo["JaCoCo Coverage"]
    end

    subgraph Testing["Testing"]
        JUnit["JUnit 5"]
        Mockito["Mockito"]
        MockWeb["MockWebServer"]
    end

    Java --> Framework
    Framework --> Libraries
    Build --> Runtime
    Testing --> Framework

    style Java fill:#f39c12,color:#fff
    style Boot fill:#27ae60,color:#fff
    style AI fill:#3498db,color:#fff
```

### Dependency Graph

```mermaid
flowchart TD
    subgraph App["Application"]
        Main["testrail-mcp-server"]
    end

    subgraph SpringBoot["Spring Boot"]
        Starter["spring-boot-starter"]
        Validation["spring-boot-starter-validation"]
        WebFlux["spring-boot-starter-webflux"]
    end

    subgraph SpringAI["Spring AI"]
        MCP["spring-ai-mcp-server-spring-boot-starter"]
    end

    subgraph JSON["JSON Processing"]
        Jackson["jackson-databind"]
        JacksonJSR["jackson-datatype-jsr310"]
    end

    subgraph Testing["Test Dependencies"]
        BootTest["spring-boot-starter-test"]
        MockWebServer["okhttp3:mockwebserver"]
        ReactorTest["reactor-test"]
    end

    Main --> Starter
    Main --> Validation
    Main --> WebFlux
    Main --> MCP
    Main --> Jackson
    Main --> JacksonJSR
    Main -.-> BootTest
    Main -.-> MockWebServer
    Main -.-> ReactorTest

    style Main fill:#3498db,color:#fff
    style MCP fill:#9b59b6,color:#fff
```
