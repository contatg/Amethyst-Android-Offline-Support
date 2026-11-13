#include "render_performance.h"

#include <dlfcn.h>
#include <stdbool.h>
#include <android/performance_hint.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <time.h>
#include <unistd.h>

void bigcore_set_affinity(void);

static __thread bool initialized;
static __thread bool measure_frames;
static __thread int64_t frame_start_cpu_ns;
static __thread int64_t last_present_ns;
static __thread int64_t sample_time_ns;
static __thread int64_t sample_cpu_ns;
static __thread int64_t frame_work_ns;
static __thread int sample_frames;
static __thread int slow_frames;
static __thread APerformanceHintSession *session;
static __thread int (*report_work)(APerformanceHintSession *, int64_t);
static __thread void (*close_session)(APerformanceHintSession *);

static int64_t current_time_ns(clockid_t clock) {
    struct timespec timestamp;
    if (clock_gettime(clock, &timestamp) != 0) return 0;
    return (int64_t) timestamp.tv_sec * 1000000000LL + timestamp.tv_nsec;
}

static void initialize(void) {
    initialized = true;
    if (getenv("POJAV_BIG_CORE_AFFINITY") != NULL) bigcore_set_affinity();
    measure_frames = getenv("POJAV_LEGACY_RENDER_HINTS") != NULL;
    if (!measure_frames) return;

    // Resolve the API at runtime so this binary can still run on Android versions below 13.
    void *android_lib = dlopen("libandroid.so", RTLD_NOW | RTLD_LOCAL);
    if (android_lib == NULL) return;
    APerformanceHintManager *(*get_manager)(void) = dlsym(android_lib, "APerformanceHint_getManager");
    APerformanceHintSession *(*create_session)(APerformanceHintManager *, const int32_t *, size_t, int64_t) =
            dlsym(android_lib, "APerformanceHint_createSession");
    report_work = dlsym(android_lib, "APerformanceHint_reportActualWorkDuration");
    close_session = dlsym(android_lib, "APerformanceHint_closeSession");
    if (get_manager == NULL || create_session == NULL || report_work == NULL || close_session == NULL) {
        dlclose(android_lib);
        return;
    }

    APerformanceHintManager *manager = get_manager();
    if (manager != NULL) {
        int32_t render_tid = (int32_t) syscall(SYS_gettid);
        const int64_t target_ns = 1000000000LL / 90;
        session = create_session(manager, &render_tid, 1, target_ns);
    }
    // libandroid is also linked by the bridge, so its symbols remain available.
    dlclose(android_lib);
    if (session != NULL) printf("Render performance: ADPF enabled for the render thread\n");
}

void render_performance_before_present(void) {
    if (!initialized) initialize();
    if (!measure_frames || frame_start_cpu_ns == 0) return;
    // Count the render thread's CPU work and exclude display waits and sleeps.
    frame_work_ns = current_time_ns(CLOCK_THREAD_CPUTIME_ID) - frame_start_cpu_ns;
    if (session != NULL && frame_work_ns > 0 && report_work(session, frame_work_ns) != 0) {
        render_performance_close();
    }
}

void render_performance_after_present(void) {
    if (!measure_frames) return;
    frame_start_cpu_ns = current_time_ns(CLOCK_THREAD_CPUTIME_ID);
    int64_t now = current_time_ns(CLOCK_MONOTONIC);
    int64_t elapsed = now - last_present_ns;
    last_present_ns = now;
    // Skip the first presentation and pauses while the app is in the background.
    if (elapsed <= 0 || elapsed >= 1000000000LL) return;
    sample_frames++;
    sample_time_ns += elapsed;
    if (frame_work_ns > 0) sample_cpu_ns += frame_work_ns;
    if (elapsed >= 25000000LL) slow_frames++;
    if (sample_frames == 300) {
        printf("Render performance: %.1f FPS, %.2f ms render CPU, %d/300 frames >= 25 ms\n",
               sample_frames * 1e9 / sample_time_ns, sample_cpu_ns / (sample_frames * 1e6), slow_frames);
        sample_frames = 0;
        slow_frames = 0;
        sample_time_ns = 0;
        sample_cpu_ns = 0;
    }
}

void render_performance_close(void) {
    if (session == NULL) return;
    close_session(session);
    session = NULL;
    frame_start_cpu_ns = 0;
}
