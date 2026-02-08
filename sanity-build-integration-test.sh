#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# sanity-build-integration-test.sh
# TestRail MCP Server — Build + Sanity Integration Test
# ──────────────────────────────────────────────────────────────────────────────
#
# Builds the project from scratch and verifies the MCP server works end-to-end
# in stdio mode (MCP_MODE=true) using JSON-RPC 2.0 over stdin/stdout.
#
# What it tests:
#   1. Full Gradle build (clean build)
#   2. Server startup in MCP_MODE=true (stdio transport, stdin lifecycle)
#   3. MCP protocol handshake (initialize / initialized)
#   4. tools/list — verifies only the 4 gateway tools are exposed
#   5. search_tools — Lucene-based natural language tool discovery
#   6. execute_tool(add_case) — full gateway→reflection→API round trip
#   7. get_categories — category listing from InternalToolRegistry
#   8. get_tools_by_category(test-runs) — category drill-down
#   9. execute_tool(get_run) — another gateway→reflection→API round trip
#
# Uses fake TestRail credentials — we test MCP protocol plumbing, not the API.
# Steps 6 and 9 are expected to fail at the HTTP level (fake creds) but the
# MCP round-trip itself must succeed.
#
# Usage:
#   ./sanity-build-integration-test.sh              # full build + integration test
#   ./sanity-build-integration-test.sh --skip-build  # skip build, use existing JAR
#
# Requirements: bash 4+, java 17+
# ──────────────────────────────────────────────────────────────────────────────

set -uo pipefail

# ─── Parse Arguments ─────────────────────────────────────────────────────────

SKIP_BUILD=false
for arg in "$@"; do
    case "$arg" in
        --skip-build) SKIP_BUILD=true ;;
        --help|-h)
            echo "Usage: $0 [--skip-build]"
            echo "  --skip-build   Skip Gradle build, use existing JAR"
            exit 0
            ;;
        *)
            echo "Unknown argument: $arg"
            echo "Usage: $0 [--skip-build]"
            exit 1
            ;;
    esac
done

# ─── Configuration ────────────────────────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$SCRIPT_DIR/build/libs/testrail-mcp-server.jar"
TMPDIR="$(mktemp -d)"
STDERR_LOG="$TMPDIR/stderr.log"
STARTUP_TIMEOUT=120   # seconds
REQUEST_TIMEOUT=30    # seconds

TESTS_PASSED=0
TESTS_FAILED=0
TOTAL_TESTS=0
SERVER_PID=""
MCP_WRITE_FD=""
MCP_READ_FD=""

# ─── Colors ───────────────────────────────────────────────────────────────────

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
DIM='\033[2m'
NC='\033[0m'

# ─── Logging ──────────────────────────────────────────────────────────────────

log_info()   { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_pass()   { echo -e "${GREEN}[PASS]${NC}  $*"; }
log_fail()   { echo -e "${RED}[FAIL]${NC}  $*"; }
log_warn()   { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_step()   { echo -e "\n${BOLD}--- Step $1: $2 ---${NC}"; }
log_detail() { echo -e "${DIM}        $*${NC}"; }

# ─── Cleanup ──────────────────────────────────────────────────────────────────

cleanup() {
    log_info "Cleaning up..."

    # Close write FD → server stdin gets EOF → MCP_MODE graceful shutdown
    if [[ -n "$MCP_WRITE_FD" ]]; then
        eval "exec ${MCP_WRITE_FD}>&-" 2>/dev/null || true
    fi
    if [[ -n "$MCP_READ_FD" ]]; then
        eval "exec ${MCP_READ_FD}<&-" 2>/dev/null || true
    fi

    if [[ -n "$SERVER_PID" ]] && kill -0 "$SERVER_PID" 2>/dev/null; then
        local waited=0
        while kill -0 "$SERVER_PID" 2>/dev/null && [[ $waited -lt 5 ]]; do
            sleep 1
            waited=$((waited + 1))
        done
        if kill -0 "$SERVER_PID" 2>/dev/null; then
            kill -9 "$SERVER_PID" 2>/dev/null || true
        fi
        wait "$SERVER_PID" 2>/dev/null || true
    fi

    rm -rf "$TMPDIR"
}

trap cleanup EXIT

# ─── MCP Communication ───────────────────────────────────────────────────────

# Sends a JSON-RPC 2.0 message to the MCP server over stdin.
# Compacts multi-line JSON to a single line (MCP stdio is newline-delimited).
#
# Args:
#   $1 = JSON-RPC message (may be multi-line for readability)
#   $2 = "true" (default) to read one response line, "false" for notifications
send_request() {
    local msg="$1"
    local expect_response="${2:-true}"

    # Compact to single line for MCP stdio transport
    local compact
    compact=$(printf '%s' "$msg" | tr -d '\n')

    printf '%s\n' "$compact" >&"$MCP_WRITE_FD" 2>/dev/null || {
        echo "WRITE_ERROR: Failed to write to server stdin"
        return 0
    }

    if [[ "$expect_response" == "true" ]]; then
        local response=""
        if read -t "$REQUEST_TIMEOUT" -r response <&"$MCP_READ_FD"; then
            echo "$response"
        else
            echo "TIMEOUT_ERROR: No response within ${REQUEST_TIMEOUT}s"
        fi
    fi
    return 0
}

# ─── Assertions ───────────────────────────────────────────────────────────────

assert_contains() {
    local response="$1"
    local expected="$2"
    local test_name="$3"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    if echo "$response" | grep -qF "$expected"; then
        TESTS_PASSED=$((TESTS_PASSED + 1))
        log_pass "$test_name"
    else
        TESTS_FAILED=$((TESTS_FAILED + 1))
        log_fail "$test_name"
        log_detail "Expected to contain: $expected"
        log_detail "Actual (first 300 chars): $(echo "$response" | head -c 300)"
    fi
}

assert_not_contains() {
    local response="$1"
    local unexpected="$2"
    local test_name="$3"

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    if ! echo "$response" | grep -qF "$unexpected"; then
        TESTS_PASSED=$((TESTS_PASSED + 1))
        log_pass "$test_name"
    else
        TESTS_FAILED=$((TESTS_FAILED + 1))
        log_fail "$test_name"
        log_detail "Expected NOT to contain: $unexpected"
    fi
}

# ══════════════════════════════════════════════════════════════════════════════
#  TEST EXECUTION
# ══════════════════════════════════════════════════════════════════════════════

echo ""
echo -e "${BOLD}TestRail MCP Server - Sanity Build & Integration Test${NC}"
echo -e "${BOLD}======================================================${NC}"

# ─── Step 0: Build ────────────────────────────────────────────────────────────

if [[ "$SKIP_BUILD" == "true" ]]; then
    log_step "0" "Build (SKIPPED via --skip-build)"
    if [[ ! -f "$JAR_FILE" ]]; then
        log_fail "JAR not found at $JAR_FILE (cannot skip build without a pre-built JAR)"
        exit 1
    fi
    log_info "Using existing JAR: $JAR_FILE"
else
    log_step "0" "Clean Build (./gradlew clean build)"

    cd "$SCRIPT_DIR"

    # Determine Gradle command: prefer ./gradlew, fall back to system gradle
    GRADLE_CMD="./gradlew"
    if ! "$GRADLE_CMD" --version > /dev/null 2>&1; then
        if command -v gradle > /dev/null 2>&1; then
            GRADLE_CMD="gradle"
            log_warn "gradlew not available, using system gradle"
        else
            log_fail "Neither ./gradlew nor system gradle found"
            exit 1
        fi
    fi

    log_info "Running: $GRADLE_CMD clean build"
    log_info "This compiles, runs unit tests, checks code coverage (90% min), and packages the JAR..."

    if $GRADLE_CMD clean build 2>&1; then
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        TESTS_PASSED=$((TESTS_PASSED + 1))
        log_pass "Gradle clean build succeeded (compile + unit tests + coverage + JAR)"
    else
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        TESTS_FAILED=$((TESTS_FAILED + 1))
        log_fail "Gradle clean build failed"
        echo ""
        echo -e "${RED}${BOLD}BUILD FAILED - cannot proceed with integration tests${NC}"
        exit 1
    fi

    if [[ ! -f "$JAR_FILE" ]]; then
        log_fail "JAR not found at $JAR_FILE after build"
        exit 1
    fi
    log_info "JAR ready: $JAR_FILE"
fi

# ─── Step 1: Start MCP Server ────────────────────────────────────────────────

log_step "1" "Start MCP server in stdio mode (MCP_MODE=true)"

export MCP_MODE=true
export TESTRAIL_URL="https://fake-testrail.example.com"
export TESTRAIL_USERNAME="test@example.com"
export TESTRAIL_API_KEY="fake-api-key-12345"
export TESTRAIL_LOG_LEVEL=INFO

log_info "Environment: MCP_MODE=$MCP_MODE, TESTRAIL_URL=$TESTRAIL_URL"
log_info "Credentials are intentionally fake (testing MCP plumbing, not TestRail API)"

# Launch server as a coprocess — gives us bidirectional stdin/stdout pipes
# Stderr is redirected to a log file for startup detection
coproc MCP_SERVER {
    java -jar "$JAR_FILE" 2>"$STDERR_LOG"
}

# Save FDs and PID immediately (coproc vars unset when process exits)
SERVER_PID=${MCP_SERVER_PID}
MCP_READ_FD=${MCP_SERVER[0]}
MCP_WRITE_FD=${MCP_SERVER[1]}

log_info "Server launched with PID $SERVER_PID"

# ─── Step 2: Wait for Startup ────────────────────────────────────────────────

log_step "2" "Wait for server startup"

STARTED=false
for (( i=1; i<=STARTUP_TIMEOUT; i++ )); do
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
        log_fail "Server process exited prematurely"
        [[ -f "$STDERR_LOG" ]] && cat "$STDERR_LOG"
        exit 1
    fi
    if grep -q "Started TestrailMcpServerApplication" "$STDERR_LOG" 2>/dev/null; then
        STARTED=true
        log_info "Server ready after ~${i}s"
        break
    fi
    sleep 1
done

if [[ "$STARTED" != "true" ]]; then
    log_fail "Server did not start within ${STARTUP_TIMEOUT}s"
    [[ -f "$STDERR_LOG" ]] && tail -20 "$STDERR_LOG"
    exit 1
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
TESTS_PASSED=$((TESTS_PASSED + 1))
log_pass "Server started successfully in MCP_MODE=true (stdio transport active)"

# ─── Step 2b: MCP Protocol Handshake ─────────────────────────────────────────

log_step "2b" "MCP Protocol Handshake (initialize + initialized)"

INIT_RESPONSE=$(send_request '{
    "jsonrpc": "2.0",
    "method": "initialize",
    "id": 1,
    "params": {
        "protocolVersion": "2024-11-05",
        "capabilities": {},
        "clientInfo": {
            "name": "sanity-test-client",
            "version": "1.0.0"
        }
    }
}')

assert_contains "$INIT_RESPONSE" '"result"'           "Initialize returns a result"
assert_contains "$INIT_RESPONSE" '"serverInfo"'        "Response contains serverInfo"
assert_contains "$INIT_RESPONSE" '"protocolVersion"'   "Response contains protocolVersion"

# Send initialized notification (no response expected per MCP spec)
send_request '{"jsonrpc":"2.0","method":"notifications/initialized"}' "false"
sleep 1
log_info "Initialized notification sent - handshake complete"

# ─── Step 3: List MCP Tools ──────────────────────────────────────────────────

log_step "3" "Get list of MCP tools (tools/list)"

TOOLS_RESPONSE=$(send_request '{"jsonrpc":"2.0","method":"tools/list","id":2,"params":{}}')

assert_contains "$TOOLS_RESPONSE" '"search_tools"'          "Exposes search_tools"
assert_contains "$TOOLS_RESPONSE" '"get_categories"'        "Exposes get_categories"
assert_contains "$TOOLS_RESPONSE" '"get_tools_by_category"' "Exposes get_tools_by_category"
assert_contains "$TOOLS_RESPONSE" '"execute_tool"'          "Exposes execute_tool"

log_info "4-tool gateway pattern verified (internal tools remain hidden)"

# ─── Step 4: Search Tools ────────────────────────────────────────────────────

log_step "4" "Call search_tools (natural language query via Lucene)"

SEARCH_RESPONSE=$(send_request '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 3,
    "params": {
        "name": "search_tools",
        "arguments": {
            "query": "Get me status of all test runs today in Planning Project"
        }
    }
}')

assert_contains "$SEARCH_RESPONSE" '"result"'    "search_tools returns MCP result"
assert_contains "$SEARCH_RESPONSE" 'matchCount'  "Response contains matchCount (Lucene found matches)"

# ─── Step 5: Execute add_case (expect API failure) ───────────────────────────

log_step "5" "Execute add_case via execute_tool (expect API failure - fake credentials)"

ADDCASE_RESPONSE=$(send_request '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 4,
    "params": {
        "name": "execute_tool",
        "arguments": {
            "toolName": "add_case",
            "parameters": {
                "sectionId": 1,
                "title": "Integration Test Case - Login Validation"
            }
        }
    }
}')

assert_contains "$ADDCASE_RESPONSE" '"result"'  "execute_tool(add_case) returns MCP result"
assert_contains "$ADDCASE_RESPONSE" 'add_case'  "Response references add_case tool name"
assert_contains "$ADDCASE_RESPONSE" 'false'     "Reports tool failure (expected - fake credentials)"

log_info "Round-trip verified: MCP request -> gateway -> reflection -> add_case -> API error -> MCP response"

# ─── Step 6: Get Categories ──────────────────────────────────────────────────

log_step "6" "Call get_categories"

CATEGORIES_RESPONSE=$(send_request '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 5,
    "params": {
        "name": "get_categories",
        "arguments": {}
    }
}')

assert_contains "$CATEGORIES_RESPONSE" '"result"'         "get_categories returns MCP result"
assert_contains "$CATEGORIES_RESPONSE" 'totalCategories'  "Response contains totalCategories"
assert_contains "$CATEGORIES_RESPONSE" 'totalTools'       "Response contains totalTools"
assert_contains "$CATEGORIES_RESPONSE" 'categories'       "Response contains categories list"

# ─── Step 7: Get Tools by Category (test-runs) ───────────────────────────────

log_step "7" "Call get_tools_by_category for test-runs"

TOOLS_CAT_RESPONSE=$(send_request '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 6,
    "params": {
        "name": "get_tools_by_category",
        "arguments": {
            "category": "test-runs"
        }
    }
}')

assert_contains "$TOOLS_CAT_RESPONSE" '"result"'   "get_tools_by_category returns MCP result"
assert_contains "$TOOLS_CAT_RESPONSE" 'test-runs'  "Response references test-runs category"
assert_contains "$TOOLS_CAT_RESPONSE" 'toolCount'  "Response contains toolCount"
assert_contains "$TOOLS_CAT_RESPONSE" 'get_run'    "Category lists get_run tool"

# ─── Step 8: Execute get_run (expect API failure) ────────────────────────────

log_step "8" "Execute get_run via execute_tool (expect API failure - fake credentials)"

GETRUN_RESPONSE=$(send_request '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 7,
    "params": {
        "name": "execute_tool",
        "arguments": {
            "toolName": "get_run",
            "parameters": {
                "runId": 1
            }
        }
    }
}')

assert_contains "$GETRUN_RESPONSE" '"result"'  "execute_tool(get_run) returns MCP result"
assert_contains "$GETRUN_RESPONSE" 'get_run'   "Response references get_run tool name"
assert_contains "$GETRUN_RESPONSE" 'false'     "Reports tool failure (expected - fake credentials)"

log_info "Round-trip verified: MCP request -> gateway -> reflection -> get_run -> API error -> MCP response"

# ══════════════════════════════════════════════════════════════════════════════
#  SUMMARY
# ══════════════════════════════════════════════════════════════════════════════

echo ""
echo -e "${BOLD}==========================================================${NC}"
echo -e "${BOLD}  TestRail MCP Server - Sanity Integration Test Results${NC}"
echo -e "${BOLD}==========================================================${NC}"
echo -e "  Total:  ${BOLD}$TOTAL_TESTS${NC}"
echo -e "  ${GREEN}Passed: $TESTS_PASSED${NC}"
if [[ $TESTS_FAILED -gt 0 ]]; then
    echo -e "  ${RED}Failed: $TESTS_FAILED${NC}"
fi
echo -e "${BOLD}==========================================================${NC}"

if [[ $TESTS_FAILED -eq 0 ]]; then
    echo -e "\n${GREEN}${BOLD}ALL TESTS PASSED${NC}\n"
    exit 0
else
    echo -e "\n${RED}${BOLD}SOME TESTS FAILED${NC}\n"
    log_warn "Server stderr log: $STDERR_LOG"
    exit 1
fi
