package com.ll.olol.boundedContext.comment.service;

import com.ll.olol.base.rq.Rq;
import com.ll.olol.base.rsData.RsData;
import com.ll.olol.boundedContext.comment.entity.Comment;
import com.ll.olol.boundedContext.comment.entity.CommentDto;
import com.ll.olol.boundedContext.comment.repository.CommentRepository;
import com.ll.olol.boundedContext.member.entity.Member;
import com.ll.olol.boundedContext.notification.event.EventAfterComment;
import com.ll.olol.boundedContext.recruitment.entity.RecruitmentArticle;
import com.ll.olol.boundedContext.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 아래 메서드들이 전부 readonly 라는 것을 명시, 나중을 위해
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final Rq rq;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public RsData commentSave(CommentDto commentDto, Long articleId) {
        Member member = rq.getMember();

        Optional<RecruitmentArticle> article = recruitmentRepository.findById(articleId);
        if (article.isEmpty()) {
            return RsData.of("F-1", "게시물이 없습니다.");
        }
        Comment savedComment = new Comment();
        savedComment.setContent(commentDto.getContent());
        savedComment.setRecruitmentArticle(article.get());
        savedComment.setMember(member);

        Comment save = commentRepository.save(savedComment);
        publisher.publishEvent(new EventAfterComment(this, save.getRecruitmentArticle(), save));

        return RsData.of("S-1", "댓글 작성 성공");
    }

    public List<Comment> findComments() {
        return commentRepository.findAll();
    }

    public Comment findOne(Long id) {
        return commentRepository.findById(id).get();
    }

    @Transactional
    public RsData update(Long id, String content) {
        Comment updateComment = findOne(id);
        if (updateComment == null) {
            return RsData.of("F-1", "업데이트 하려는 댓글이 없습니다.");
        }
        updateComment.setContent(content);
        return RsData.of("S-1", "댓글 수정 성공");
    }

    @Transactional
    public RsData commentDelete(Long id) {
        Optional<Comment> id1 = commentRepository.findById(id);
        if (id1.isEmpty()) {
            return RsData.of("F-1", "삭제하려는 댓글이 없습니다.");
        }
        commentRepository.delete(id1.get());
        return RsData.of("S-1", "댓글 삭제 성공");
    }

    public RsData isEqualMemberById(Long id) {
        if (rq.getMember().getId() != findOne(id).getMember().getId()) {
            return RsData.of("F-1", "동일한 사용자만 할 수 있습니다.");
        }
        return RsData.of("S-1", "성공");
    }
}
