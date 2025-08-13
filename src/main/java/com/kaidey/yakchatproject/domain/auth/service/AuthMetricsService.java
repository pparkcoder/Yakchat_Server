package com.kaidey.yakchatproject.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AuthMetricsService {
    private final AtomicLong ocrAttempts = new AtomicLong(0);
    private final AtomicLong ocrSuccesses = new AtomicLong(0);
    private final AtomicLong registrationCompleted = new AtomicLong(0);

    public void recordOcrAttempt(){ ocrAttempts.incrementAndGet(); }
    public void recordOcrSuccess(){ ocrSuccesses.incrementAndGet(); }
    public void recordRegistrationComplete(){ registrationCompleted.incrementAndGet(); }

    public Map<String,Object> getMetrics(){
        Map<String,Object> m = new HashMap<>();
        long a = ocrAttempts.get();
        long s = ocrSuccesses.get();
        long r = registrationCompleted.get();
        m.put("ocr_attempts", a);
        m.put("ocr_successes", s);
        m.put("registrations_completed", r);
        if (a>0) m.put("ocr_success_rate", (double)s/a*100);
        if (s>0) m.put("conversion_rate", (double)r/s*100);
        return m;
    }
}