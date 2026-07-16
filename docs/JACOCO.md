# JaCoCo 테스트 커버리지 가이드

이 프로젝트가 **JaCoCo**(Java Code Coverage)로 테스트 커버리지를 측정하는 방법과, 리포트를 읽는 법을 정리한 문서다. `MODERNIZATION_PLAN.md` Phase 7("GitHub Actions 테스트 스텝 추가 + Jacoco")에서 도입했다.

## JaCoCo가 측정하는 것

"테스트 커버리지"는 테스트의 **품질**이 아니라 **실행 여부**를 잰다. 예를 들어:

```java
public String greet(String name) {
    if (name == null) {
        return "Hello, stranger";   // 이 줄
    }
    return "Hello, " + name;        // 이 줄
}
```

테스트가 `greet("Bob")`만 호출한다면 두 번째 줄만 실행되고, `if` 안쪽은 한 번도 안 지나간다. JaCoCo는 이런 걸 명령어(instruction)/분기(branch)/줄(line) 단위로 센다.

**커버리지 100% ≠ 완벽한 테스트.** 코드가 실행됐다는 것과 제대로 검증(assert)됐다는 건 다른 얘기다. 그래서 이 프로젝트는 커버리지 %를 강제하는 빌드 게이트를 걸지 않고 참고 지표로만 쓴다.

## 측정 원리 (에이전트 방식)

JaCoCo는 테스트를 실행하는 JVM에 Java agent로 끼어들어, 바이트코드가 실행되는 순간마다 "이 줄 지나갔다"를 기록한다. 그 결과를 `.exec` 바이너리로 저장했다가, 소스코드와 대조해 HTML/XML 리포트로 변환한다.

```
./gradlew test 실행
  → 테스트가 도는 동안 JaCoCo agent가 기록
  → build/jacoco/test.exec 생성 (원본 실행 기록, 바이너리)
  → (finalizedBy로 자동 연결) jacocoTestReport 태스크 실행
  → test.exec + 소스코드를 합쳐서
  → build/reports/jacoco/test/html/index.html 생성 (사람이 읽는 리포트)
```

## `build.gradle.kts` 설정

```kotlin
plugins {
    jacoco   // JaCoCo 기능 자체를 켜는 플러그인
}

jacoco {
    toolVersion = "0.8.12"   // 엔진 버전 고정(재현성)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)   // "test 끝나면(성공/실패 무관) jacocoTestReport도 실행"
}

tasks.jacocoTestReport {
    // dependsOn(tasks.test)는 일부러 넣지 않는다 - 아래 "테스트 실패 시 리포트가 생성 안 되던 문제" 참고
    reports {
        xml.required.set(true)    // CI 도구가 파싱하기 좋은 XML
        html.required.set(true)   // 사람이 브라우저로 보는 HTML
    }
    // QueryDSL이 자동 생성하는 Q클래스(QBook.java 등)는 우리가 짠 로직이 아니라 집계에서 제외
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude("**/Q*.class") }
        })
    )
}
```

### 테스트 실패 시 리포트가 생성 안 되던 문제

처음엔 관용적으로 `jacocoTestReport { dependsOn(tasks.test) }`도 같이 넣었는데, 로컬에서 테스트 하나가 실패하는 상황(`BookstoreApplicationTests` - docker-compose 미기동)으로 검증해보니 **리포트 자체가 생성되지 않는** 걸 발견했다.

원인은 Gradle의 알려진 이슈([gradle/gradle#27707](https://github.com/gradle/gradle/issues/27707))다. `finalizedBy`(뒤에 실행)와 `dependsOn`(먼저 실행돼야 함)이 서로를 가리키면, 앞 작업(`test`)이 실패했을 때 Gradle이 "의존성이 실패했으니 후속 작업도 취소"로 판단해 `finalizedBy` 자체를 건너뛴다. `dependsOn(tasks.test)`를 빼고 `finalizedBy`만 남기자 테스트가 실패해도 그 시점까지의 커버리지 리포트가 정상 생성됐다(실제로 재현 확인함).

이 트레이드오프: `jacocoTestReport`를 단독으로 실행하면 먼저 `./gradlew test`를 실행해둬야 최신 실행 데이터(`test.exec`)를 기준으로 리포트가 만들어진다.

## 로컬에서 리포트 열어보기

```bash
./gradlew test
```

위 명령이 끝나면 `finalizedBy` 덕분에 `build/reports/jacoco/test/html/index.html`이 자동 생성/갱신된다. 이 파일을 브라우저로 열면 된다(더블클릭 또는 `Start-Process` 등).

## 리포트 읽는 법

| 컬럼 | 의미 |
|---|---|
| **Missed Instructions** | 가장 세밀한 단위(바이트코드 명령어)에서 실행이 안 된 개수. 상위 목록 화면에서는 **초록/빨강 막대그래프**로 표시되고, 막대에 마우스를 올리면 정확한 개수가 툴팁으로 뜬다 |
| **Cov.** (막대 옆 %) | 커버된 비율. **100%에 가까울수록 좋음** |
| **Missed Branches** | `if`/`switch`처럼 갈림길 중 한쪽만 타고 반대쪽은 안 지나간 개수. 마찬가지로 **0(막대가 초록 위주)일수록 좋음** |
| **Lines / Methods / Classes** | 각각 줄/메서드/클래스 단위로 같은 방식 |

**막대그래프 읽는 법**: 초록 = 커버됨, 빨강 = 놓침(Missed). 막대가 거의 다 초록이면 좋은 것이고, 빨간 비율이 크면 그만큼 테스트가 안 지나간 코드가 많다는 뜻이다. 정확한 숫자가 필요하면:
- 막대 위에 마우스 hover → 툴팁으로 정확한 개수
- 클래스명을 클릭해서 들어가면 소스코드 줄 단위로 초록/빨강이 칠해지고, 해당 클래스만의 숫자 요약도 보임
- `build/reports/jacoco/test/jacocoTestReport.xml`에는 클래스별 `<counter type="INSTRUCTION" missed="N" covered="M"/>` 형태의 원자료가 그대로 있음(스크립트로 파싱하고 싶을 때)

**"n/a"는 좋다/나쁘다가 아니라 "해당 없음"**: 코드에 분기문 자체가 없으면(DTO record, 단순 getter, 설정 클래스 등) 잴 대상이 없어서 n/a로 뜬다. 걱정할 필요 없다.

## 빨간 부분을 봤을 때 — 항상 테스트를 추가해야 하나?

아니다. 빨간 부분은 "테스트가 안 지나간 코드"라는 뜻이지 "버그가 있다"는 뜻이 아니다. **분기/에러 처리 로직이 있는지**로 판단해야지, 색깔만 보고 기계적으로 판단하면 안 된다.

- **빨간색이어도 괜찮은 경우**: 단순 getter/DTO record, 로직 없는 설정 클래스, QueryDSL Q클래스(이미 집계에서 제외), `main()` 진입점
- **신경 써야 하는 경우**: `if`/`try-catch` 등 분기가 있는데 한쪽 경로가 한 번도 안 지나간 경우, 실제 비즈니스 규칙이 들어있는 서비스 로직

이 기준으로 실제 감사했던 사례는 [`docs/TESTING.md`](./TESTING.md) 참고 — `Stock`/`Review` 같은 순수 데이터 홀더 엔티티는 빨간 부분이 있어도 그대로 뒀고, 반대로 `BearerTokenResolver`(분기 3개), `OrderCompletedEventPublisher`(예외를 삼키는 catch문)처럼 분기·에러처리가 있는데 테스트가 없던 곳은 실제로 테스트를 추가했다.

## CI에서 리포트 확인하기

`.github/workflows/build.yml`의 "Test" 스텝(`./gradlew test`)이 끝나면 `finalizedBy` 덕분에 커버리지 리포트도 함께 생성되고, 이어지는 스텝에서 `test-report`/`jacoco-report` 두 아티팩트로 업로드된다.

**웹 UI에서 받기**:
1. GitHub 저장소 → **Actions** 탭
2. 원하는 워크플로우 실행(run) 클릭
3. 실행 페이지 맨 아래 **Artifacts** 섹션에서 `jacoco-report`(또는 `test-report`) 클릭 → zip 다운로드
4. 압축을 풀면 `index.html`이 로컬에서 본 것과 동일한 리포트

아직 push/PR로 트리거된 적이 없다면, Actions 탭 → "Build" 워크플로우 → **Run workflow** 버튼으로 수동 실행(`workflow_dispatch`)해서 확인할 수 있다.

**`gh` CLI로 받기** (설치돼 있다면):
```bash
gh run list --repo chzh2839/renew-DEV24
gh run download <run-id> --repo chzh2839/renew-DEV24
```

아티팩트는 기본 90일 뒤 자동 삭제되니, 오래된 실행 결과가 필요하면 그 안에 받아둬야 한다.
