# Architecture Documentation

This document provides detailed architectural documentation for the TestRail MCP Server.

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
    subgraph UserLayer["User Layer"]
        User[/"Developer"/]
    end

    subgraph IDELayer["IDE Layer"]
        Cursor["Cursor IDE"]
        Claude["Claude LLM<br/>(Anthropic Cloud)"]
    end

    subgraph MCPLayer["MCP Server Layer (Local)"]
        direction TB
        Transport["STDIO Transport"]
        ToolRegistry["Tool Registry"]

        subgraph ToolsGroup["MCP Tools"]
            CT["Cases Tools"]
            PT["Projects Tools"]
            RT["Runs Tools"]
            RST["Results Tools"]
            ST["Sections Tools"]
        end

        ApiClient["TestRail API Client"]
    end

    subgraph ConfigLayer["Configuration Layer"]
        EnvVars["Environment Variables"]
        AppConfig["application.yml"]
    end

    subgraph ExternalLayer["External Services"]
        TestRail["TestRail Instance<br/>(Cloud/On-Premise)"]
    end

    User --> Cursor
    Cursor <--> Claude
    Claude <-->|"MCP Protocol<br/>(JSON-RPC over STDIO)"| Transport
    Transport --> ToolRegistry
    ToolRegistry --> ToolsGroup
    ToolsGroup --> ApiClient

    EnvVars --> AppConfig
    AppConfig -.->|"Credentials"| ApiClient

    ApiClient <-->|"HTTPS<br/>Basic Auth"| TestRail

    style Claude fill:#a29bfe,color:#fff
    style TestRail fill:#74b9ff,color:#fff
    style EnvVars fill:#ff7675,color:#fff
    style ApiClient fill:#00b894,color:#fff
```

---

## Component Architecture

### Layer Diagram

```mermaid
flowchart TB
    subgraph Presentation["Presentation Layer"]
        MCP["MCP Server<br/>(STDIO Transport)"]
    end

    subgraph Application["Application Layer"]
        subgraph Tools["Tool Components"]
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

    MCP --> Tools
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
    box Local Machine
        participant STDIO as STDIO Transport
        participant Tool as Tool Component
        participant Client as TestrailApiClient
        participant WC as WebClient
    end

    box External
        participant TR as TestRail API
    end

    STDIO->>Tool: Tool invocation (JSON-RPC)
    activate Tool

    Tool->>Tool: Validate parameters
    Tool->>Client: Call API method
    activate Client

    Client->>Client: Build request
    Client->>WC: HTTP request
    activate WC

    WC->>TR: HTTPS + Basic Auth
    activate TR
    TR-->>WC: JSON Response
    deactivate TR

    WC-->>Client: Response body
    deactivate WC

    Client->>Client: Parse response
    Client-->>Tool: Domain object
    deactivate Client

    Tool-->>STDIO: Tool result (JSON)
    deactivate Tool
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
