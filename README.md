# 🎵 Chord DHT

A Java implementation of the [Chord](https://en.wikipedia.org/wiki/Chord_(peer-to-peer)) distributed hash table protocol with gRPC-based node communication.

[![Java CI](https://github.com/vkuzdas/chord/actions/workflows/java_ci.yml/badge.svg)](https://github.com/vkuzdas/chord/actions/workflows/java_ci.yml)

## Overview

Chord is a foundational peer-to-peer lookup protocol that maps keys to nodes using consistent hashing. Each node maintains a **finger table** for $O(\log N)$ lookups and uses **stabilization** to handle dynamic membership.

This implementation supports:
- **DHT Operations** — `put`, `get`, `delete` with automatic key routing
- **Finger Table** — Efficient $O(\log N)$ lookup via $m$-bit finger entries
- **Stabilization** — Periodic successor/predecessor repair
- **Key Migration** — Automatic data transfer on join/leave

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      ChordNode                          │
├─────────────────────────────────────────────────────────┤
│  Finger Table (m entries)                               │
│  ├── finger[i].start = (n + 2^(i-1)) mod 2^m           │
│  └── finger[i].node  = successor(finger[i].start)      │
├─────────────────────────────────────────────────────────┤
│  Predecessor pointer                                    │
├─────────────────────────────────────────────────────────┤
│  Local Data (keys in range (predecessor, self])         │
├─────────────────────────────────────────────────────────┤
│  gRPC Server (ChordServiceGrpc)                         │
│  ├── FindSuccessor / ClosestPrecedingFinger            │
│  ├── Notify / GetPredecessor                           │
│  └── MoveKeys (data migration)                         │
└─────────────────────────────────────────────────────────┘
```

## Project Structure

```
src/
├── main/java/chord/
│   ├── ChordNode.java      # Core node logic & gRPC server
│   ├── Finger.java         # Finger table entry
│   ├── NodeReference.java  # Node identity (ip, port, id)
│   └── Util.java           # Hashing & ID utilities
└── test/java/
    ├── ChordNodeTest.java  # Unit tests
    └── BigTest.java        # Large-scale network tests
```

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| Build | Maven |
| RPC | gRPC + Protocol Buffers |
| Logging | SLF4J + Logback |
| Testing | JUnit 5 |

## Getting Started

```bash
# Build & test
mvn clean install

# Run a bootstrap node
java -cp target/classes chord.ChordNode
```

## Configuration

```java
// ID space: m-bit identifiers (2^m nodes max)
ChordNode.m = 8;  // 256 possible IDs

// Stabilization interval (ms)
ChordNode.STABILIZATION_INTERVAL = 2000;
```

## Key Operations

| Operation | Complexity | Description |
|-----------|------------|-------------|
| `lookup(key)` | $O(\log N)$ | Find successor node for key |
| `put(key, value)` | $O(\log N)$ | Store at responsible node |
| `get(key)` | $O(\log N)$ | Retrieve from responsible node |

## References

- [Chord: A Scalable Peer-to-peer Lookup Service for Internet Applications](https://pdos.csail.mit.edu/papers/chord:sigcomm01/chord_sigcomm.pdf) — Stoica et al., SIGCOMM 2001
