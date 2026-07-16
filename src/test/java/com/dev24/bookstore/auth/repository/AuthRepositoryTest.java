package com.dev24.bookstore.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.AdminRole;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.common.config.QuerydslConfig;

// CustomerRepository/AdminRepository의 파생 쿼리 메서드(findByLoginId/existsByLoginId/findAllByAdminRole)를 직접 검증
// @DataJpaTest는 앱 전역의 모든 Repository 빈을 스캔하므로 BookQueryRepository(QueryDSL)도 함께 로드되는데,
// JPAQueryFactory 빈이 없으면 컨텍스트 로딩 자체가 실패한다(PurchaseModuleRepositoryTest와 동일한 이유로 Import 필요).
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig.class)
@Testcontainers
class AuthRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AdminRepository adminRepository;

    private Customer customer(String loginId) {
        return customerRepository.save(new Customer(
                loginId, "encoded-password", "홍길동", "길동이", loginId + "@example.com",
                "010-0000-0000", "서울시 강남구", "소설", false));
    }

    // 로그인 시 CustomerService.authenticate()가 이 메서드로 계정을 조회하므로, 저장된 loginId로
    // 정상적으로 찾아지는지가 로그인 흐름의 전제 조건이다.
    @Test
    void customer_findByLoginId_found() {
        customer("customer-found");

        assertThat(customerRepository.findByLoginId("customer-found")).isPresent();
    }

    // 존재하지 않는 loginId는 예외 없이 Optional.empty()로 와야 서비스 레이어가 ENTITY_NOT_FOUND로
    // 안전하게 분기할 수 있다.
    @Test
    void customer_findByLoginId_notFound_returnsEmpty() {
        assertThat(customerRepository.findByLoginId("no-such-customer")).isEmpty();
    }

    // 회원가입 시 CustomerService.signUp()이 이 메서드로 중복 loginId를 걸러내므로, 존재/미존재
    // 두 경우 모두 정확한 boolean을 반환하는지 검증한다.
    @Test
    void customer_existsByLoginId_trueAndFalse() {
        customer("customer-exists");

        assertThat(customerRepository.existsByLoginId("customer-exists")).isTrue();
        assertThat(customerRepository.existsByLoginId("no-such-customer")).isFalse();
    }

    // 관리자 로그인(AdminService.authenticate())의 전제 조건 - Customer와 동일한 계약을 Admin에서도 보장하는지 확인.
    @Test
    void admin_findByLoginId_found() {
        adminRepository.save(new Admin("admin-found", "encoded-password", "관리자"));

        assertThat(adminRepository.findByLoginId("admin-found")).isPresent();
    }

    // 존재하지 않는 관리자 계정도 예외 없이 Optional.empty()로 와야 한다.
    @Test
    void admin_findByLoginId_notFound_returnsEmpty() {
        assertThat(adminRepository.findByLoginId("no-such-admin")).isEmpty();
    }

    // Customer와 동일하게, 관리자 쪽도 존재/미존재 두 분기 모두 정확한 boolean을 반환하는지 확인.
    @Test
    void admin_existsByLoginId_trueAndFalse() {
        adminRepository.save(new Admin("admin-exists", "encoded-password", "관리자"));

        assertThat(adminRepository.existsByLoginId("admin-exists")).isTrue();
        assertThat(adminRepository.existsByLoginId("no-such-admin")).isFalse();
    }

    // 재고 부족 알림(LowStockEventConsumer)이 STOCK_ADMIN에게만 발송되므로, 역할별 필터링이
    // 정확히 동작하는지가 이 메서드의 핵심 계약이다.
    @Test
    void admin_findAllByAdminRole_filtersByRoleOnly() {
        adminRepository.save(new Admin("stock-admin-1", "encoded-password", "재고관리자1", null, AdminRole.STOCK_ADMIN));
        adminRepository.save(new Admin("stock-admin-2", "encoded-password", "재고관리자2", null, AdminRole.STOCK_ADMIN));
        adminRepository.save(new Admin("general-admin", "encoded-password", "일반관리자", null, AdminRole.GENERAL));

        assertThat(adminRepository.findAllByAdminRole(AdminRole.STOCK_ADMIN))
                .extracting(Admin::getLoginId)
                .containsExactlyInAnyOrder("stock-admin-1", "stock-admin-2");
    }

    // STOCK_ADMIN이 한 명도 없는 상태에서 GENERAL 관리자만 있어도 예외 없이 빈 리스트로 와야
    // LowStockEventConsumer가 알림 발송을 그냥 건너뛸 수 있다.
    @Test
    void admin_findAllByAdminRole_noMatches_returnsEmptyList() {
        adminRepository.save(new Admin("general-only", "encoded-password", "일반관리자"));

        assertThat(adminRepository.findAllByAdminRole(AdminRole.STOCK_ADMIN)).isEmpty();
    }
}
