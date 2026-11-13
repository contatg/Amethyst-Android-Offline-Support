#define _GNU_SOURCE

#include <errno.h>
#include <fcntl.h>
#include <sched.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static unsigned long read_cpu_value(int cpu, const char *property) {
    char path[96];
    snprintf(path, sizeof(path), "/sys/devices/system/cpu/cpu%d/%s", cpu, property);
    int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) return 0;

    char value[32];
    ssize_t count = read(fd, value, sizeof(value) - 1);
    close(fd);
    if (count <= 0) return 0;
    value[count] = '\0';
    return strtoul(value, NULL, 10);
}

static int select_fast_cores(const cpu_set_t *allowed, cpu_set_t *fast, const char *property) {
    CPU_ZERO(fast);
    unsigned long maximum = 0;
    for (int cpu = 0; cpu < CPU_SETSIZE; cpu++) {
        if (!CPU_ISSET(cpu, allowed)) continue;
        unsigned long value = read_cpu_value(cpu, property);
        if (value == 0 || value < maximum) continue;
        if (value > maximum) {
            CPU_ZERO(fast);
            maximum = value;
        }
        CPU_SET(cpu, fast);
    }
    return CPU_COUNT(fast);
}

void bigcore_set_affinity(void) {
    cpu_set_t allowed;
    if (sched_getaffinity(0, sizeof(allowed), &allowed) != 0) return;
    int allowed_count = CPU_COUNT(&allowed);

    cpu_set_t fast;
    int count = select_fast_cores(&allowed, &fast, "cpu_capacity");
    if (count == 0 || count == allowed_count) {
        count = select_fast_cores(&allowed, &fast, "cpufreq/cpuinfo_max_freq");
    }
    if (count == 0 || count == allowed_count) {
        printf("Render performance: no faster CPU group was detected\n");
        return;
    }

    if (sched_setaffinity(0, sizeof(fast), &fast) == 0) {
        printf("Render performance: render thread may run on %d of %d available CPUs\n",
               count, allowed_count);
    } else {
        printf("Render performance: keeping default CPU affinity: %s\n", strerror(errno));
    }
}
