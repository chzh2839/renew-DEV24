package com.dev24.bookstore.book.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.domain.QBook;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookQueryRepositoryImpl implements BookQueryRepository {

    private static final QBook book = QBook.book;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Book> search(BookSearchCondition condition, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder()
                .and(keywordContains(condition.keyword()))
                .and(categoryEq(condition.category()))
                .and(statusEq(condition.status()));

        List<Book> content = queryFactory
                .selectFrom(book)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(book.count())
                .from(book)
                .where(where);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return book.title.containsIgnoreCase(keyword)
                .or(book.authors.containsIgnoreCase(keyword))
                .or(book.publisher.containsIgnoreCase(keyword))
                .or(book.contents.containsIgnoreCase(keyword));
    }

    private BooleanExpression categoryEq(String category) {
        return StringUtils.hasText(category) ? book.category.eq(category) : null;
    }

    private BooleanExpression statusEq(BookStatus status) {
        return status == null ? null : book.status.eq(status);
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        PathBuilder<Book> entityPath = new PathBuilder<>(Book.class, book.getMetadata());
        return sort.stream()
                .map((Sort.Order order) -> new OrderSpecifier<>(
                        order.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC,
                        entityPath.getComparable(order.getProperty(), Comparable.class)))
                .toArray(OrderSpecifier[]::new);
    }
}
