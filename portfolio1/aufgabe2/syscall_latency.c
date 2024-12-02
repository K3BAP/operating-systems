#include <stdio.h>
#include <unistd.h>
#include <stdint.h>
#include <time.h>

#define ITERATIONS 1000000

// Funktion zur Messung der Zeit in Nanosekunden
uint64_t get_nanoseconds() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (uint64_t)ts.tv_sec * 1000000000L + ts.tv_nsec;
}

int main() {
    uint64_t start, end, elapsed, min_latency = UINT64_MAX, max_latency = 0;

    for (int i = 0; i < ITERATIONS; i++) {
        // Zeit vor dem System-Call
        start = get_nanoseconds();

        // Ein einfacher System-Call
        getpid();

        // Zeit nach dem System-Call
        end = get_nanoseconds();

        // Latenz berechnen
        elapsed = end - start;

        // Maximalwert aktualisieren
        if(elapsed > max_latency) {
            max_latency = elapsed;
        }

        // Minimalwert aktualisieren
        if (elapsed < min_latency) {
            min_latency = elapsed;
        }
    }

    printf("Minimale Latenz eines System-Calls: %lu ns\n", min_latency);
    printf("Maximale Latenz eines System-Calls: %lu ns\n", max_latency);


    return 0;
}
