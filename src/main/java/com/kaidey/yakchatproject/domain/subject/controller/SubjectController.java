package com.kaidey.yakchatproject.domain.subject.controller;
import com.kaidey.yakchatproject.domain.subject.entity.Subject;
import com.kaidey.yakchatproject.domain.subject.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService svc;

    @GetMapping("/ui")
    public Map<String, Object> listForUi(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return svc.listForUi(activeOnly);
    }

    @GetMapping("/search")
    public List<Map<String, String>> search(@RequestParam String q) {
        return svc.search(q);
    }

    @GetMapping("/{code}")
    public Subject getDetail(@PathVariable String code) {
        return svc.getByCode(code);
    }
}