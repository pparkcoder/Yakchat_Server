package com.kaidey.yakchatproject.domain.user.service;

import com.kaidey.yakchatproject.domain.user.entity.UserGrade;
import com.kaidey.yakchatproject.domain.user.entity.GradeType;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class GradeService {

    public GradeType calculateGrade(UserGrade g) {
        int q = g.getQuestionCount();
        int a = g.getAnswerCount();
        int sum = q + a;

        if (sum >= 100) return GradeType.MYEONGYAK;
        if (sum >= 40)  return GradeType.GOSU;
        if (q >= 15 || a >= 15) return GradeType.DUAL;
        if (q >= 5  || a >= 5)  return GradeType.HANAL;
        if (q >= 1  || a >= 1)  return GradeType.SESSAK;
        return GradeType.NONE;
    }

    public void updateUserGrade(UserGrade userGrade) {
        userGrade.setGrade(calculateGrade(userGrade));
    }

    public NextPromotion getNextPromotion(UserGrade g) {
        int q = g.getQuestionCount();
        int a = g.getAnswerCount();
        int sum = q + a;

        switch (g.getGrade()) {
            case NONE:
                return new NextPromotion(GradeType.SESSAK, 1, Math.max(q, a));
            case SESSAK:
                return new NextPromotion(GradeType.HANAL, 5, Math.max(q, a));
            case HANAL:
                return new NextPromotion(GradeType.DUAL, 15, Math.max(q, a));
            case DUAL:
                return new NextPromotion(GradeType.GOSU, 40, sum);
            case GOSU:
                return new NextPromotion(GradeType.MYEONGYAK, 100, sum);
            case MYEONGYAK:
            default:
                return new NextPromotion(null, 0, 0);
        }
    }

    @Getter
    public static class NextPromotion {
        private final GradeType nextGrade;
        private final int target;
        private final int progress;

        public NextPromotion(GradeType nextGrade, int target, int progress) {
            this.nextGrade = nextGrade;
            this.target = target;
            this.progress = progress;
        }

        public double progressRate() {
            return (target == 0) ? 1.0 : Math.min(1.0, (double) progress / target);
        }
    }

}
