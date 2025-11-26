package com.codit.worldcup.domain.repository;

import java.util.List;

import com.codit.worldcup.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	// 특정 월드컵의 모든 댓글을 찾는 메서드
	List<Comment> findAllByWorldcupIdOrderByCreatedAtDesc(Long worldcupId);
	void deleteAllByWorldcupId(Long worldcupId);
}