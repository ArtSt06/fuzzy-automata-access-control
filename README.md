# Fuzzy Automata for Adaptive Access Control

A research project exploring the use of fuzzy finite automata for adaptive access control in information systems.

## Research Objective

To develop and compare classical and fuzzy finite automata for adaptive access control.

The model considers three input characteristics:
- Device trust
- API request frequency
- Session inactivity time

Access-control states:
- `NORMAL`
- `PARTIALLY_RESTRICTED`
- `RESTRICTED`

## Technology Stack

- Java 21
- Spring Boot
- Maven

## Getting Started

Run the tests using Maven Wrapper.

**Windows:**
```powershell
.\mvnw.cmd clean verify
```

**Linux / macOS:**
```bash
./mvnw clean verify
```