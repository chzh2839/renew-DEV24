package com.dev24.bookstore.review.event;

// 포토 리뷰(imageUrl 있는 리뷰)가 커밋된 뒤, 업로드된 오브젝트의 실제 내용을 비동기로 검증하기 위해
// ReviewCommandService가 발행하는 스프링 내부 이벤트. NATS로는 안 나간다(ReviewImageValidationListener 참고).
public record ReviewImageUploadedEvent(Long reviewId, String objectKey) {
}
