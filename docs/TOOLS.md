# Tools Reference Documentation

Complete reference for all 26 MCP tools provided by the TestRail MCP Server.

## Table of Contents

- [Overview](#overview)
- [Tool Categories](#tool-categories)
- [Cases Tools](#cases-tools)
- [Projects Tools](#projects-tools)
- [Runs Tools](#runs-tools)
- [Results Tools](#results-tools)
- [Sections Tools](#sections-tools)
- [Tool Metadata Schema](#tool-metadata-schema)
- [Error Handling](#error-handling)

---

## Overview

### Tool Distribution

```mermaid
pie title Tools by Category
    "Cases" : 6
    "Projects" : 5
    "Runs" : 6
    "Results" : 5
    "Sections" : 6
```

### Tool Characteristics

```mermaid
pie title Tool Types
    "Read-only" : 14
    "Create" : 6
    "Update" : 5
    "Delete (Destructive)" : 5
```

### Tool Decision Flow

```mermaid
flowchart TD
    Start([User Request]) --> Analyze{Analyze<br/>Intent}

    Analyze -->|View/Read| ReadOps[Read Operations]
    Analyze -->|Create New| CreateOps[Create Operations]
    Analyze -->|Modify Existing| UpdateOps[Update Operations]
    Analyze -->|Remove| DeleteOps[Delete Operations]

    ReadOps --> ReadTools["getTestCase<br/>getTestCases<br/>getProject<br/>getProjects<br/>getRun<br/>getRuns<br/>getResults<br/>getResultsForRun<br/>getSection<br/>getSections"]

    CreateOps --> CreateTools["addTestCase<br/>addProject<br/>addRun<br/>addResult<br/>addResults<br/>addResultsForCases<br/>addSection"]

    UpdateOps --> UpdateTools["updateTestCase<br/>updateProject<br/>updateRun<br/>updateSection<br/>moveSection<br/>closeRun<br/>cloneTestCase"]

    DeleteOps --> Confirm{Requires<br/>Confirmation}
    Confirm -->|Yes| DeleteTools["deleteTestCase<br/>deleteProject<br/>deleteRun<br/>deleteSection"]

    style ReadOps fill:#27ae60,color:#fff
    style CreateOps fill:#3498db,color:#fff
    style UpdateOps fill:#f39c12,color:#fff
    style DeleteOps fill:#e74c3c,color:#fff
```

---

## Tool Categories

```mermaid
flowchart LR
    subgraph Cases["Test Cases"]
        direction TB
        C1[getTestCase]
        C2[getTestCases]
        C3[addTestCase]
        C4[updateTestCase]
        C5[deleteTestCase]
        C6[cloneTestCase]
    end

    subgraph Projects["Projects"]
        direction TB
        P1[getProject]
        P2[getProjects]
        P3[addProject]
        P4[updateProject]
        P5[deleteProject]
    end

    subgraph Runs["Test Runs"]
        direction TB
        R1[getRun]
        R2[getRuns]
        R3[addRun]
        R4[updateRun]
        R5[closeRun]
        R6[deleteRun]
    end

    subgraph Results["Test Results"]
        direction TB
        RS1[getResults]
        RS2[getResultsForRun]
        RS3[addResult]
        RS4[addResults]
        RS5[addResultsForCases]
    end

    subgraph Sections["Sections"]
        direction TB
        S1[getSection]
        S2[getSections]
        S3[addSection]
        S4[updateSection]
        S5[deleteSection]
        S6[moveSection]
    end

    Cases ~~~ Projects
    Projects ~~~ Runs
    Runs ~~~ Results
    Results ~~~ Sections

    style Cases fill:#3498db,color:#fff
    style Projects fill:#9b59b6,color:#fff
    style Runs fill:#27ae60,color:#fff
    style Results fill:#f39c12,color:#fff
    style Sections fill:#e74c3c,color:#fff
```

---

## Cases Tools

### Tool Relationship Map

```mermaid
flowchart TD
    subgraph CasesWorkflow["Test Cases Workflow"]
        getTestCases["getTestCases<br/>━━━━━━━━━━━━<br/>List all cases"]
        getTestCase["getTestCase<br/>━━━━━━━━━━━━<br/>View single case"]
        addTestCase["addTestCase<br/>━━━━━━━━━━━━<br/>Create new case"]
        updateTestCase["updateTestCase<br/>━━━━━━━━━━━━<br/>Modify case"]
        cloneTestCase["cloneTestCase<br/>━━━━━━━━━━━━<br/>Copy with changes"]
        deleteTestCase["deleteTestCase<br/>━━━━━━━━━━━━<br/>Remove case"]
    end

    getTestCases -->|"select one"| getTestCase
    getTestCase -->|"modify"| updateTestCase
    getTestCase -->|"duplicate"| cloneTestCase
    getTestCase -->|"remove"| deleteTestCase
    addTestCase -->|"verify"| getTestCase
    updateTestCase -->|"verify"| getTestCase
    cloneTestCase -->|"verify"| getTestCase

    style getTestCases fill:#27ae60,color:#fff
    style getTestCase fill:#27ae60,color:#fff
    style addTestCase fill:#3498db,color:#fff
    style updateTestCase fill:#f39c12,color:#fff
    style cloneTestCase fill:#3498db,color:#fff
    style deleteTestCase fill:#e74c3c,color:#fff
```

### getTestCase

Retrieves a specific test case by ID.

| Property | Value |
|----------|-------|
| **Category** | Cases |
| **Destructive** | No |
| **Requires Confirmation** | No |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `caseId` | Integer | Yes | Test case ID (with or without 'C' prefix) |

**Returns:** `TestCase` object with all fields

**Example Prompts:**
- "Show me test case C123"
- "Get the details of case 456"
- "What are the steps in test case C789?"

**Might Lead To:**
- `updateTestCase` - to modify the case
- `cloneTestCase` - to copy with changes
- `deleteTestCase` - to remove the case

```mermaid
sequenceDiagram
    participant User
    participant Tool as getTestCase
    participant API as TestRail API

    User->>Tool: caseId=123
    Tool->>API: GET /get_case/123
    API-->>Tool: {id, title, steps, ...}
    Tool-->>User: TestCase object
```

---

### getTestCases

Retrieves test cases for a project with pagination.

| Property | Value |
|----------|-------|
| **Category** | Cases |
| **Destructive** | No |
| **Requires Confirmation** | No |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `projectId` | Integer | Yes | Project ID |
| `suiteId` | Integer | No | Suite ID (for multi-suite projects) |
| `sectionId` | Integer | No | Filter by section |
| `limit` | Integer | No | Max results (1-250, default 250) |
| `offset` | Integer | No | Pagination offset |

**Returns:** `List<TestCase>`

**Example Prompts:**
- "List all test cases in project 1"
- "Show me test cases in suite 5"
- "Get cases from section 10 in project 3"

**Pagination Example:**

```mermaid
sequenceDiagram
    participant User
    participant Tool as getTestCases

    User->>Tool: projectId=1, limit=50, offset=0
    Tool-->>User: Cases 1-50

    User->>Tool: projectId=1, limit=50, offset=50
    Tool-->>User: Cases 51-100

    User->>Tool: projectId=1, limit=50, offset=100
    Tool-->>User: Cases 101-150
```

---

### addTestCase

Creates a new test case in a section.

| Property | Value |
|----------|-------|
| **Category** | Cases |
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `sectionId` | Integer | Yes | Target section ID |
| `title` | String | Yes | Test case title |
| `steps` | String | No | Test steps |
| `expectedResult` | String | No | Expected outcome |
| `preconditions` | String | No | Prerequisites |
| `priorityId` | Integer | No | 1=Low, 2=Medium, 3=High, 4=Critical |
| `typeId` | Integer | No | Test type ID |
| `refs` | String | No | References (e.g., "JIRA-123") |

**Returns:** Created `TestCase` object

**Example Prompts:**
- "Create a new test case for login validation"
- "Add a test case to section 5 for password reset"

```mermaid
flowchart LR
    Input["title: 'Login Test'<br/>steps: '1. Enter creds...'<br/>priorityId: 3"] --> Tool["addTestCase"]
    Tool --> Output["TestCase {<br/>  id: 456,<br/>  title: 'Login Test'<br/>}"]

    style Input fill:#3498db,color:#fff
    style Output fill:#27ae60,color:#fff
```

---

### updateTestCase

Updates an existing test case.

| Property | Value |
|----------|-------|
| **Category** | Cases |
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `caseId` | Integer | Yes | Case ID to update |
| `title` | String | No | New title |
| `steps` | String | No | New steps |
| `expectedResult` | String | No | New expected result |
| `preconditions` | String | No | New preconditions |
| `priorityId` | Integer | No | New priority |
| `typeId` | Integer | No | New type |
| `refs` | String | No | New references |

**Note:** Only provided fields are updated; others remain unchanged.

**Returns:** Updated `TestCase` object

---

### deleteTestCase

Permanently deletes a test case.

| Property | Value |
|----------|-------|
| **Category** | Cases |
| **Destructive** | **YES** |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `caseId` | Integer | Yes | Case ID to delete |

**Returns:** `OperationResult` with success message

```mermaid
flowchart LR
    Warning["⚠️ WARNING<br/>This action cannot be undone"]
    Tool["deleteTestCase(123)"]
    Result["OperationResult {<br/>  success: true,<br/>  message: 'C123 deleted'<br/>}"]

    Warning --> Tool --> Result

    style Warning fill:#e74c3c,color:#fff
    style Result fill:#27ae60,color:#fff
```

---

### cloneTestCase

Creates a copy of an existing test case with optional modifications.

| Property | Value |
|----------|-------|
| **Category** | Cases |
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `sourceCaseId` | Integer | Yes | Case ID to clone |
| `targetSectionId` | Integer | No | Destination section (default: same) |
| `newTitle` | String | No | New title (default: "Copy of...") |
| `newSteps` | String | No | Modified steps |
| `newExpectedResult` | String | No | Modified expected result |

**Returns:** Newly created `TestCase` object

```mermaid
sequenceDiagram
    participant User
    participant Tool as cloneTestCase
    participant API as TestRail API

    User->>Tool: sourceCaseId=100,<br/>newTitle="HTTP 201 Test"

    Tool->>API: GET /get_case/100
    API-->>Tool: Original case data

    Tool->>Tool: Merge modifications

    Tool->>API: POST /add_case/{sectionId}
    API-->>Tool: New case {id: 101}

    Tool-->>User: TestCase {id: 101}
```

---

## Projects Tools

### Tool Relationship Map

```mermaid
flowchart TD
    subgraph ProjectsWorkflow["Projects Workflow"]
        getProjects["getProjects<br/>━━━━━━━━━━━━<br/>List all projects"]
        getProject["getProject<br/>━━━━━━━━━━━━<br/>View project details"]
        addProject["addProject<br/>━━━━━━━━━━━━<br/>Create new project"]
        updateProject["updateProject<br/>━━━━━━━━━━━━<br/>Modify settings"]
        deleteProject["deleteProject<br/>━━━━━━━━━━━━<br/>Remove project"]
    end

    getProjects -->|"select"| getProject
    getProject -->|"modify"| updateProject
    getProject -->|"remove"| deleteProject
    addProject -->|"verify"| getProject

    style getProjects fill:#27ae60,color:#fff
    style getProject fill:#27ae60,color:#fff
    style addProject fill:#3498db,color:#fff
    style updateProject fill:#f39c12,color:#fff
    style deleteProject fill:#e74c3c,color:#fff
```

### getProject

| Property | Value |
|----------|-------|
| **Destructive** | No |
| **Requires Confirmation** | No |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `projectId` | Integer | Yes | Project ID |

---

### getProjects

| Property | Value |
|----------|-------|
| **Destructive** | No |
| **Requires Confirmation** | No |

**Parameters:** None

**Returns:** `List<Project>` - All accessible projects

---

### addProject

| Property | Value |
|----------|-------|
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `name` | String | Yes | Project name |
| `announcement` | String | No | Announcement text |
| `showAnnouncement` | Boolean | No | Show announcement |
| `suiteMode` | Integer | No | 1=Single, 2=Baselines, 3=Multiple |

---

### updateProject

| Property | Value |
|----------|-------|
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `projectId` | Integer | Yes | Project ID |
| `name` | String | No | New name |
| `announcement` | String | No | New announcement |
| `showAnnouncement` | Boolean | No | Show announcement |
| `isCompleted` | Boolean | No | Mark as completed |

---

### deleteProject

| Property | Value |
|----------|-------|
| **Destructive** | **YES** |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `projectId` | Integer | Yes | Project ID to delete |

**Warning:** Deletes ALL test suites, cases, runs, and results!

---

## Runs Tools

### Tool Relationship Map

```mermaid
flowchart TD
    subgraph RunsWorkflow["Test Runs Workflow"]
        getRuns["getRuns<br/>━━━━━━━━━━━━<br/>List all runs"]
        getRun["getRun<br/>━━━━━━━━━━━━<br/>View run details"]
        addRun["addRun<br/>━━━━━━━━━━━━<br/>Create new run"]
        updateRun["updateRun<br/>━━━━━━━━━━━━<br/>Modify settings"]
        closeRun["closeRun<br/>━━━━━━━━━━━━<br/>Finalize run"]
        deleteRun["deleteRun<br/>━━━━━━━━━━━━<br/>Remove run"]
    end

    getRuns -->|"select"| getRun
    getRun -->|"modify"| updateRun
    getRun -->|"complete"| closeRun
    getRun -->|"remove"| deleteRun
    addRun -->|"verify"| getRun
    closeRun -->|"verify"| getRun

    style getRuns fill:#27ae60,color:#fff
    style getRun fill:#27ae60,color:#fff
    style addRun fill:#3498db,color:#fff
    style updateRun fill:#f39c12,color:#fff
    style closeRun fill:#f39c12,color:#fff
    style deleteRun fill:#e74c3c,color:#fff
```

### addRun

| Property | Value |
|----------|-------|
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `projectId` | Integer | Yes | Project ID |
| `name` | String | Yes | Run name |
| `description` | String | No | Run description |
| `suiteId` | Integer | No | Suite ID |
| `milestoneId` | Integer | No | Milestone ID |
| `assignedtoId` | Integer | No | Assigned user ID |
| `includeAll` | Boolean | No | Include all cases (default: true) |
| `caseIds` | String | No | Comma-separated case IDs |

---

### closeRun

| Property | Value |
|----------|-------|
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `runId` | Integer | Yes | Run ID to close |

**Note:** Once closed, no more results can be added.

---

## Results Tools

### Tool Relationship Map

```mermaid
flowchart TD
    subgraph ResultsWorkflow["Test Results Workflow"]
        getResultsForRun["getResultsForRun<br/>━━━━━━━━━━━━<br/>All results in run"]
        getResults["getResults<br/>━━━━━━━━━━━━<br/>Results for test"]
        addResult["addResult<br/>━━━━━━━━━━━━<br/>Add single result"]
        addResults["addResults<br/>━━━━━━━━━━━━<br/>Bulk by test IDs"]
        addResultsForCases["addResultsForCases<br/>━━━━━━━━━━━━<br/>Bulk by case IDs"]
    end

    getResultsForRun -->|"filter"| getResults
    addResult -->|"verify"| getResults
    addResults -->|"verify"| getResultsForRun
    addResultsForCases -->|"verify"| getResultsForRun

    style getResultsForRun fill:#27ae60,color:#fff
    style getResults fill:#27ae60,color:#fff
    style addResult fill:#3498db,color:#fff
    style addResults fill:#3498db,color:#fff
    style addResultsForCases fill:#3498db,color:#fff
```

### Status IDs Reference

```mermaid
flowchart LR
    subgraph StatusIDs["Test Result Status IDs"]
        S1["1 = Passed ✓"]
        S2["2 = Blocked ⊘"]
        S3["3 = Untested ○"]
        S4["4 = Retest ↻"]
        S5["5 = Failed ✗"]
    end

    style S1 fill:#27ae60,color:#fff
    style S2 fill:#f39c12,color:#fff
    style S3 fill:#95a5a6,color:#fff
    style S4 fill:#3498db,color:#fff
    style S5 fill:#e74c3c,color:#fff
```

### addResult

| Property | Value |
|----------|-------|
| **Destructive** | No |
| **Requires Confirmation** | Yes |

**Parameters:**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `testId` | Integer | Yes | Test ID (not case ID) |
| `statusId` | Integer | Yes | 1=Passed, 2=Blocked, 3=Untested, 4=Retest, 5=Failed |
| `comment` | String | No | Execution notes |
| `defects` | String | No | Defect references |
| `elapsed` | String | No | Time spent (e.g., "30s", "1m 45s") |
| `version` | String | No | Build version |

---

## Sections Tools

### Tool Relationship Map

```mermaid
flowchart TD
    subgraph SectionsWorkflow["Sections Workflow"]
        getSections["getSections<br/>━━━━━━━━━━━━<br/>List all sections"]
        getSection["getSection<br/>━━━━━━━━━━━━<br/>View section details"]
        addSection["addSection<br/>━━━━━━━━━━━━<br/>Create new section"]
        updateSection["updateSection<br/>━━━━━━━━━━━━<br/>Modify section"]
        moveSection["moveSection<br/>━━━━━━━━━━━━<br/>Reorganize"]
        deleteSection["deleteSection<br/>━━━━━━━━━━━━<br/>Remove section"]
    end

    getSections -->|"select"| getSection
    getSection -->|"modify"| updateSection
    getSection -->|"relocate"| moveSection
    getSection -->|"remove"| deleteSection
    addSection -->|"verify"| getSection
    addSection -->|"add child"| addSection

    style getSections fill:#27ae60,color:#fff
    style getSection fill:#27ae60,color:#fff
    style addSection fill:#3498db,color:#fff
    style updateSection fill:#f39c12,color:#fff
    style moveSection fill:#f39c12,color:#fff
    style deleteSection fill:#e74c3c,color:#fff
```

### Section Hierarchy Example

```mermaid
flowchart TD
    Root["Project Root"]

    Root --> Auth["Authentication<br/>(Section 1)"]
    Root --> API["API Tests<br/>(Section 2)"]
    Root --> UI["UI Tests<br/>(Section 3)"]

    Auth --> Login["Login<br/>(Section 4)"]
    Auth --> Logout["Logout<br/>(Section 5)"]
    Auth --> Password["Password Reset<br/>(Section 6)"]

    API --> REST["REST Endpoints<br/>(Section 7)"]
    API --> GraphQL["GraphQL<br/>(Section 8)"]

    UI --> Forms["Forms<br/>(Section 9)"]
    UI --> Navigation["Navigation<br/>(Section 10)"]

    style Root fill:#3498db,color:#fff
    style Auth fill:#9b59b6,color:#fff
    style API fill:#27ae60,color:#fff
    style UI fill:#f39c12,color:#fff
```

---

## Tool Metadata Schema

Each tool includes rich metadata for LLM understanding:

```mermaid
classDiagram
    class ToolMetadata {
        +String name
        +String description
        +String whenToUse
        +String[] mightLeadTo
        +String[] examplePrompts
        +ToolCategory category
        +boolean isDestructive
        +boolean requiresConfirmation
        +String[] relatedTools
    }

    class ToolParameter {
        +String name
        +String type
        +boolean required
        +String description
        +String defaultValue
    }

    ToolMetadata "1" --> "*" ToolParameter

    note for ToolMetadata "Enhanced descriptions help<br/>LLM select the right tool"
```

---

## Error Handling

```mermaid
flowchart TD
    Call["Tool Call"] --> Execute{Execute}

    Execute -->|Success| Success["Return Result"]
    Execute -->|Error| ErrorType{Error Type}

    ErrorType -->|400| BadRequest["Bad Request<br/>(Invalid parameters)"]
    ErrorType -->|401| Unauthorized["Unauthorized<br/>(Check credentials)"]
    ErrorType -->|403| Forbidden["Forbidden<br/>(Insufficient permissions)"]
    ErrorType -->|404| NotFound["Not Found<br/>(Resource doesn't exist)"]
    ErrorType -->|429| RateLimit["Rate Limited<br/>(Too many requests)"]
    ErrorType -->|500| ServerError["Server Error<br/>(TestRail issue)"]

    BadRequest --> Exception["TestrailApiException"]
    Unauthorized --> Exception
    Forbidden --> Exception
    NotFound --> Exception
    RateLimit --> Exception
    ServerError --> Exception

    Exception --> ErrorResult["Error Response<br/>to LLM"]

    style Success fill:#27ae60,color:#fff
    style Exception fill:#e74c3c,color:#fff
    style ErrorResult fill:#f39c12,color:#fff
```
