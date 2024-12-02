#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <stdint.h>
#include <time.h>

#define ITERATIONS 1000000

int pipe_fd[2];
uint64_t get_nanoseconds() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (uint64_t)ts.tv_sec * 1000000000L + ts.tv_nsec;
}

void* thread_function(void* arg) {
    char buffer;
    for (int i = 0; i < ITERATIONS; i++) {
        read(pipe_fd[0], &buffer, 1); // Lesen erzwingt Kontextwechsel
        write(pipe_fd[1], &buffer, 1); // Schreiben erzwingt Kontextwechsel
    }
    return NULL;
}

int main() {
    if (pipe(pipe_fd) == -1) {
        perror("pipe");
        exit(EXIT_FAILURE);
    }

    pthread_t thread;
    if (pthread_create(&thread, NULL, thread_function, NULL) != 0) {
        perror("pthread_create");
        exit(EXIT_FAILURE);
    }

    char buffer = 'x';
    uint64_t start = get_nanoseconds();

    for (int i = 0; i < ITERATIONS; i++) {
        write(pipe_fd[1], &buffer, 1); // Schreiben erzwingt Kontextwechsel
        read(pipe_fd[0], &buffer, 1); // Lesen erzwingt Kontextwechsel
    }

    uint64_t end = get_nanoseconds();
    pthread_join(thread, NULL);

    close(pipe_fd[0]);
    close(pipe_fd[1]);

    double avg_context_switch_time = (double)(end - start) / (ITERATIONS * 2); // 2 Kontextwechsel pro Iteration
    printf("Durchschnittliche Kontextwechselzeit: %.2f ns\n", avg_context_switch_time);

    return 0;
}
