package com.kaidey.yakchatproject.domain.scrap.repository;

import com.kaidey.yakchatproject.domain.question.entity.Question;
import com.kaidey.yakchatproject.domain.scrap.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {


    @Query("SELECT s FROM Scrap s WHERE s.scraperId = :userId AND s.question.id = :questionId")
    List<Scrap> findByUserIdAndQuestionId(Long userId, Long questionId);

    @Query("SELECT s FROM Scrap s JOIN FETCH s.question WHERE s.scraperId = :userId ORDER BY s.question.createdAt DESC LIMIT 5")
    List<Scrap> findByUserIdCreatedAtDesc(Long userId);
}
