package com.kaidey.yakchatproject.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
public class OcrValidationService {
    private static final List<String> PHARMACY_STUDENT_KEYWORDS = Arrays.asList("약학과","약학대학","약대","약학","Pharmacy","College of Pharmacy");
    private static final List<String> PHARMACY_LICENSE_KEYWORDS = Arrays.asList("약사","면허증","보건복지부","면허","Pharmacist");
    private static final List<String> STUDENT_CARD_KEYWORDS = Arrays.asList("학생증","학번","Student ID","학과","대학교","University");

    public boolean isValidStudentCard(String fullText, Map<String, Object> fields) {
        boolean k1 = STUDENT_CARD_KEYWORDS.stream().anyMatch(fullText::contains);
        boolean k2 = PHARMACY_STUDENT_KEYWORDS.stream().anyMatch(fullText::contains);
        String name = (String) fields.get("name");
        String sid = (String) fields.get("studentId");
        String univ = (String) fields.get("university");
        boolean req = notEmpty(name) && notEmpty(sid) && notEmpty(univ);
        log.info("학생증 검증 - cardKw:{}, pharmKw:{}, fields:{}", k1, k2, req);
        return k1 && k2 && req;
    }

    public boolean isValidProfessionalLicense(String fullText, Map<String, Object> fields) {
        boolean k = PHARMACY_LICENSE_KEYWORDS.stream().anyMatch(fullText::contains);
        String name = (String) fields.get("name");
        String no = (String) fields.get("licenseNumber");
        String dt = (String) fields.get("issueDate");
        boolean req = notEmpty(name) && notEmpty(no) && notEmpty(dt);
        log.info("면허증 검증 - licenseKw:{}, fields:{}", k, req);
        return k && req;
    }

    private boolean notEmpty(String s){ return s!=null && !s.trim().isEmpty(); }
}
