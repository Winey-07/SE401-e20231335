# Lab 01: Multithreading


This project demonstrates parallel word counting and the producer-consumer pattern in Java 21.

## Build and run

```bash
mvn -q compile
java -cp target/classes edu.itc.se.lab01.challenge.DataGenerator data 200 50000
java -cp target/classes edu.itc.se.lab01.challenge.WordCount data --top 5 --timeout 30
mvn test
```

`WordCount` accepts `<dir> [--top N] [--timeout SECONDS] [--virtual] [--workers N]`.
It counts UTF-8 text files in parallel; exit codes are `0` (complete), `1` (partial),
and `2` (invalid usage). `Bench data 5` measures 1, 2, 4, and 8 platform workers
and virtual threads.

## Word-count design

A scanner feeds file paths into a bounded `ArrayBlockingQueue` (capacity 16).
Workers count normalized words in a `ConcurrentHashMap<String, LongAdder>`.
The bounded queue applies back-pressure to limit queued work; `LongAdder` reduces
contention on frequent words. A reporter displays progress without blocking workers.
Timeout and Ctrl+C share interrupt-based shutdown, producing a partial result when
the run is stopped early. Virtual mode uses 64 worker tasks.

Tests compare results with a single-threaded reference and cover worker counts,
timeout, shutdown, thread cleanup, and CSV output.

## Benchmark

For 200 files / 10 million words (62 MB), on a 2-vCPU Linux machine with OpenJDK 21,
median times across five runs were:

| Workers/mode | Median time |
|---|---:|
| 1 platform worker | 2.36 s |
| 2 platform workers | 1.68 s |
| 4 platform workers | 1.64 s |
| 8 platform workers | 1.75 s |
| 64 virtual tasks | 1.69 s |

The workload benefits from a second worker, then levels off near the machine's
two-core limit. Re-run `Bench` to measure another machine.

## Producer-consumer

Task 3 compares a custom `BoundedBuffer` (ArrayDeque with synchronized `put()` and
`take()`, `wait()`, and `notifyAll()`) with `ArrayBlockingQueue`. Both process 100
orders using 2 producers, 3 consumers, capacity 5, and 3 poison pills. Condition
checks must use `while`, not `if`; `notify()` can wake a thread that still cannot
proceed and leave an eligible thread waiting. `ArrayBlockingQueue` provides the
same blocking behavior with less custom synchronization code.

## Deadlock notes

The word-count pipeline has one-way dependencies: the scanner produces queue items,
workers consume them, and poison pills let workers finish. Blocking operations are
interruptible during shutdown. Keep waits outside locks/resources needed by other
threads, and avoid circular resource ordering to prevent deadlocks.
