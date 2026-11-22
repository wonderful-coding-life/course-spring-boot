---
name: rebuild-practice-docx
description: 최신 practice-book Markdown으로 실습 교재 DOCX 배포판을 재생성할 때 사용합니다. 기존 표지·캡처·다이어그램과 편집 형식을 유지하고 Word에서 목차·페이지 번호를 갱신한 뒤 코드 일치와 렌더링을 검증합니다.
---

# 실습 교재 DOCX 배포판 갱신

이 프로젝트의 `practice-book` Markdown을 원본으로 사용한다. 배포 대상은 `practice-book/distribution/올인원 스프링부트 정복_실습교재.docx`다. 본문 수정이나 PDF 배포는 요청에 포함된 경우에만 수행한다.

## 유지할 내용

- 표지는 기존 `올인원 스프링부트 정복_표지.docx`를 사용한다. 제목·저자·연락처·그림을 재작성하지 않는다.
- 기존 캡처와 다이어그램 PNG를 재사용한다. Mermaid 원문이 바뀌었거나 새 그림이 생겼다면 원문과 일치하는 그림을 준비하고 `references/book.json`의 매핑을 갱신한다. 해시만 바꿔 오래된 그림을 재사용하지 않는다.
- Markdown의 설명과 코드를 보존한다. 코드의 탭은 공백 네 칸으로 바꾸되 내용은 바꾸지 않는다. 코드 비교 예제도 배포판에 포함한다.
- 기존 글꼴·제목·코드 배경·표·그림 설명 형식을 따른다. 세부 기준과 변경 지점은 [편집 형식](references/layout.md)을 참고한다.
- 표지에는 쪽번호를 표시하지 않고, 앞부분은 로마 숫자, 본문은 1부터 아라비아 숫자로 매긴다. 실제 Word 목차는 제목 수준 1~2로 갱신한다.

## 실행

Windows PowerShell 7, Microsoft Word, Java 21 이상을 사용한다. Word를 통한 목차 갱신과 렌더링에 필요한 실행 권한은 도구의 권한 절차로 처리한다. 기존 결과물은 검증이 끝날 때까지 덮어쓰지 않는다.

프로젝트 루트에서 실행한다.

```powershell
& ./.agents/skills/rebuild-practice-docx/scripts/build-release.ps1
```

이 명령은 고유한 임시 폴더에 DOCX를 생성하고 Word로 목차·쪽번호를 갱신한 다음 구조 검증과 페이지 렌더링을 수행한다. 출력한 작업 폴더의 `release.json`, `verification.json`, `render/layout-audit.json`을 확인한다. PDFBox 렌더러는 기존 사용자 Gradle 캐시에서 라이브러리를 찾으며, 없는 경우 다른 PDF 렌더러를 준비해 같은 검사를 수행한다. 프로젝트의 Gradle 의존성을 변경하지 않는다.

`render/sheet-*.png`로 전체 흐름을 확인하고, `page-*.png`로 변경된 부분과 표지·목차·표·코드·캡처·다이어그램 및 페이지 경계를 읽을 수 있는 크기로 점검한다. `page-stats.tsv`의 짧은 페이지도 확인해 잘린 문단이나 불필요한 마지막 쪽을 찾는다. 결함이 있으면 [편집 형식](references/layout.md)에 따라 변환 도구를 조정하고 새 작업 폴더에서 다시 실행한다. 화면을 확인하지 않은 채 배포 완료로 보고하지 않는다.

검증과 시각 검토가 끝나면 위 명령이 출력한 **해당 작업 폴더**를 지정해 배포한다.

```powershell
& ./.agents/skills/rebuild-practice-docx/scripts/publish-release.ps1 -WorkDir '<작업 폴더>'
```

배포 명령은 원본 파일이 생성 이후 바뀌지 않았는지 다시 검사하고 직전 DOCX를 작업 폴더에 보관한 뒤 결과물을 교체한다. 사용자가 요청한 갱신 범위에서 추가 승인을 요구하지 않는다. 다른 Word 문서를 닫거나 Word 프로세스를 일괄 종료하지 않는다.

## 완료 보고

최종 DOCX 링크와 갱신된 총 쪽수, 표지·다이어그램 유지 및 목차·쪽번호 갱신 여부를 간단히 알린다. 검사에 사용한 PDF·PNG·로그는 별도 요청이 없으면 배포하지 않는다.
