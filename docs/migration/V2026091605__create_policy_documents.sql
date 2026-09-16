-- WAVEY terms and privacy policy documents
-- One active document is stored for each category and language pair.

CREATE TABLE IF NOT EXISTS policy_documents (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(20) NOT NULL,
    language VARCHAR(2) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    effective_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT ck_policy_documents_category CHECK (category IN ('TERMS', 'PRIVACY')),
    CONSTRAINT ck_policy_documents_language CHECK (language IN ('KO', 'EN')),
    CONSTRAINT uk_policy_documents_category_language UNIQUE (category, language)
);

INSERT INTO policy_documents (category, language, title, content, version, effective_date, is_active)
VALUES ('TERMS', 'KO', 'WAVEY 이용약관', $$# WAVEY 이용약관

공고일: 2026년 09월 16일  
시행일: 2026년 09월 16일

운영자: WAVEY 개발팀 (대표자: 유시영)<br>
주소: 서울특별시 구로구 경인로 445 동양미래대학교 3호관 511호<br>
문의: wavey164@gmail.com

## 제1조 목적

이 약관은 WAVEY 개발팀(대표자: 유시영, 이하 “운영자”)이 제공하는 WAVEY 서비스(이하 “서비스”)의 이용과 관련하여 운영자와 이용자 사이의 권리, 의무 및 책임사항을 정하는 것을 목적으로 합니다.

## 제2조 용어의 정의

1. “이용자”란 이 약관에 따라 서비스를 이용하는 회원과 비회원을 말합니다.
2. “회원”이란 Google 또는 Kakao 등 소셜 로그인 절차를 거쳐 이용계약을 체결한 이용자를 말합니다.
3. “루트”란 회원이 서비스에서 생성·저장하거나 다른 이용자에게 공개할 수 있는 여행 경로 및 관련 정보를 말합니다.
4. “콘텐츠”란 이용자가 서비스에 입력·업로드·작성하는 루트, 리뷰, 이미지, 텍스트 및 그 밖의 정보를 말합니다.

## 제3조 약관의 효력 및 이용계약의 성립

1. 운영자는 서비스 화면에 이 약관을 게시하고, 이용자가 내용을 확인한 후 동의 의사를 표시할 수 있도록 합니다.
2. 이용계약은 이용자가 이 약관에 동의하고 회원가입을 신청한 후 운영자가 신청을 승낙하면 성립합니다.
3. 비회원은 약관에 동의한 범위에서 회원가입 없이 이용할 수 있는 서비스를 이용할 수 있습니다.
4. 만 14세 미만 이용자의 회원가입 또는 개인정보 처리는 법정대리인의 동의와 본인확인 등 관련 법령에서 정한 절차에 따릅니다. 운영자가 만 14세 미만 이용자의 가입을 허용하지 않는 경우에는 가입 화면에서 이를 안내합니다.
5. 이 약관에서 정하지 않은 사항은 관련 법령, 개인정보 처리방침, 위치정보 이용약관 및 운영자가 정한 서비스별 운영정책에 따릅니다.

## 제4조 서비스의 제공

1. 운영자는 다음 서비스를 제공할 수 있습니다.
   - 여행 루트의 생성·조회·수정·삭제 및 공개
   - 관광 장소와 문화유산 정보 제공
   - 이미지 OCR, 번역, 문화유산·랜드마크 식별 및 웹 탐지
   - 위치 기반 장소 확인, 방문 확인 및 스탬프·배지 기능
   - 리뷰, 저장한 장소 등 이용자 편의 기능
2. 서비스의 일부 기능은 이용자의 기기, 권한 설정, 네트워크 또는 외부 서비스의 상태에 따라 제한될 수 있습니다.
3. 현재 서비스는 무료로 제공됩니다. 유료 기능을 추가하는 경우 별도의 이용조건, 요금 및 환불정책을 사전에 안내합니다.
4. 서비스에 외부 서비스나 링크가 포함될 수 있으며, 외부 서비스에는 해당 사업자의 약관과 개인정보 처리방침이 적용됩니다.

## 제5조 회원정보 및 계정 관리

1. 이용자는 정확한 정보를 제공해야 하며, 타인의 명의나 계정을 사용해서는 안 됩니다.
2. 이용자는 자신의 계정을 다른 사람에게 양도·대여하거나 이용하게 해서는 안 됩니다.
3. 이용자는 계정의 무단 사용을 알게 된 경우 즉시 운영자에게 알려야 합니다.
4. 이용자는 서비스에서 제공하는 방법으로 닉네임, 국가, 언어 및 설정을 수정할 수 있습니다.

## 제6조 이용자의 의무 및 금지행위

이용자는 다음 행위를 해서는 안 됩니다.

1. 타인의 개인정보, 계정 또는 인증정보를 도용하거나 부정하게 사용하는 행위
2. 허위·위법·음란하거나 타인의 명예, 사생활, 초상권, 저작권 등 권리를 침해하는 콘텐츠를 게시하는 행위
3. 서비스 또는 서비스와 연결된 시스템의 정상적인 운영을 방해하는 행위
4. 자동화된 수단으로 과도한 요청을 보내거나 외부 API의 할당량을 부당하게 사용하는 행위
5. 악성코드, 불법 프로그램 또는 서비스의 보안 취약점을 이용하는 행위
6. 서비스의 소스코드, 인증정보 또는 보안기능을 무단으로 복제·변경·분석하는 행위
7. 서비스에서 얻은 정보를 운영자의 사전 동의 없이 영리 목적으로 재판매·재배포하는 행위
8. 그 밖에 법령, 공서양속, 이 약관 또는 운영정책을 위반하는 행위

## 제7조 이용자 콘텐츠 및 권리침해 신고

1. 이용자가 작성한 콘텐츠에 대한 저작권 등 권리는 원칙적으로 해당 이용자에게 귀속됩니다.
2. 이용자는 운영자에게 서비스 제공과 운영에 필요한 범위에서 콘텐츠를 저장, 복제, 전송, 공개 및 형식 변환할 수 있는 비독점적·무상 이용권을 부여합니다. 운영자는 이용자의 콘텐츠를 서비스 제공 목적을 벗어나 임의로 판매하거나 별도의 광고 소재로 이용하지 않습니다.
3. 이용자는 자신이 게시하는 콘텐츠에 필요한 권리를 보유하고 있으며, 제3자의 개인정보나 권리를 침해하지 않음을 보증합니다.
4. 공개 루트와 리뷰는 다른 이용자에게 노출될 수 있습니다. 이용자는 공개 전에 포함된 개인정보와 위치정보를 확인해야 합니다.
5. 운영자는 권리침해, 불법성, 보안위협 또는 서비스 운영상 중대한 문제가 확인된 콘텐츠를 제한·임시 차단·삭제할 수 있습니다.
6. 권리침해 또는 잘못된 조치에 대한 신고·이의제기는 wavey164@gmail.com로 접수할 수 있습니다.

## 제8조 서비스 정보의 한계

1. 서비스의 장소, 경로, 운영시간, 요금, 교통, 문화유산 및 안전 관련 정보는 참고용입니다.
2. 이용자는 실제 방문이나 이동 전에 해당 시설과 관계기관의 최신 정보를 직접 확인해야 합니다.
3. 외부 서비스가 제공하는 정보의 정확성, 최신성, 계속적인 이용 가능성에 대해서 운영자는 보증하지 않습니다.

## 제9조 서비스의 변경 및 중단

1. 운영자는 서비스의 품질 향상, 보안, 기술적 필요 또는 법령·정책 변경을 위해 서비스의 전부 또는 일부를 변경할 수 있습니다.
2. 운영자는 서비스 내용을 변경하는 경우 변경 내용과 적용일을 서비스 화면 또는 공지사항을 통해 안내합니다.
3. 점검, 장애, 통신 두절, 외부 서비스 중단, 천재지변 등 부득이한 사유가 있는 경우 서비스가 일시 중단될 수 있습니다.
4. 서비스의 전부를 종료하는 경우 운영자는 종료일과 회원의 데이터 처리 방법을 합리적인 기간 전에 공지합니다.

## 제10조 이용 제한 및 계약 해지

1. 이용자가 이 약관이나 관련 법령을 위반한 경우 운영자는 경고, 일부 기능 제한, 일시 정지 또는 이용계약 해지 조치를 할 수 있습니다.
2. 불법 콘텐츠, 계정 도용, 보안 공격 등 긴급한 조치가 필요한 경우 운영자는 사전 통지 없이 콘텐츠를 삭제하거나 이용을 제한할 수 있습니다.
3. 긴급한 사유가 아닌 경우 운영자는 제한 사유와 기간을 이용자에게 안내하고, 이용자가 이의를 제기할 수 있는 방법을 제공합니다.
4. 이용자는 서비스에서 제공하는 방법으로 언제든지 이용계약 해지와 회원 탈퇴를 신청할 수 있습니다.

## 제11조 회원 탈퇴 및 데이터 처리

1. 회원 탈퇴 시 운영자는 관련 법령에 따라 보관해야 하는 정보를 제외하고 회원정보와 회원이 생성한 데이터를 지체 없이 삭제합니다.
2. 공개 루트·리뷰 또는 다른 이용자의 공유·인용으로 인해 이미 제3자에게 제공된 콘텐츠는 즉시 회수되지 않을 수 있습니다. 이 경우 운영자는 회원 식별정보를 제거하거나 법령과 서비스 운영에 필요한 범위에서 처리합니다.
3. 회원 탈퇴 전에 이용자는 삭제를 원하는 루트, 리뷰, 프로필 이미지 등 콘텐츠를 직접 확인해야 합니다.

## 제12조 개인정보 및 위치정보 보호

1. 운영자는 이용자의 개인정보를 개인정보 처리방침에 따라 처리합니다.
2. 현재 위치 또는 이미지 분석에 입력한 좌표를 이용하는 기능에는 별도의 위치정보 관련 고지와 동의 절차가 적용될 수 있습니다.
3. 이용자는 위치 권한을 거부하거나 위치정보 이용 동의를 철회할 수 있으며, 이 경우 위치 기반 기능의 일부 이용이 제한될 수 있습니다.

## 제13조 책임의 제한 및 손해배상

1. 운영자는 운영자의 고의 또는 과실이 없는 장애, 외부 서비스의 장애, 이용자의 귀책사유 또는 천재지변으로 발생한 손해에 대해서는 책임을 지지 않습니다.
2. 운영자는 서비스 정보의 변경·누락·오류로 인해 이용자에게 발생한 손해에 대해 운영자의 고의 또는 과실이 있는 경우를 제외하고 책임을 지지 않습니다.
3. 이 조항은 운영자의 고의·중대한 과실에 따른 책임이나 관련 법령상 제한할 수 없는 책임을 제한하지 않습니다.
4. 이용자가 이 약관을 위반하여 운영자 또는 제3자에게 손해를 입힌 경우 이용자는 그 손해를 배상해야 합니다.

## 제14조 약관의 변경

1. 운영자는 관련 법령을 위반하지 않는 범위에서 이 약관을 변경할 수 있습니다.
2. 일반적인 변경은 적용일 15일 전부터 서비스 화면 또는 공지사항을 통해 안내합니다.
3. 이용자에게 불리하거나 권리·의무에 중대한 영향을 주는 변경은 적용일 30일 전부터 개별 통지 또는 이에 준하는 방법으로 안내합니다.
4. 변경에 동의하지 않는 이용자는 시행일 전까지 회원 탈퇴를 신청할 수 있습니다. 운영자는 변경 내용, 시행일 및 거부 방법을 함께 안내합니다.
5. 약관의 변경 이력과 적용일은 이용자가 확인할 수 있도록 관리합니다.

## 제15조 통지 및 문의

1. 운영자는 회원이 제공한 이메일, 서비스 내 알림 또는 공지사항을 통해 통지할 수 있습니다.
2. 회원 전체에 대한 통지는 서비스 공지사항에 게시하는 방법으로 할 수 있습니다. 다만, 이용자에게 중대한 영향을 미치는 사항은 가능한 경우 개별 통지합니다.
3. 서비스 이용 및 약관에 관한 문의는 wavey164@gmail.com로 접수할 수 있습니다.

## 제16조 준거법 및 관할

1. 이 약관은 대한민국 법령에 따라 해석됩니다.
2. 서비스 이용과 관련하여 분쟁이 발생한 경우 운영자와 이용자는 성실히 협의합니다.
3. 협의로 해결되지 않는 분쟁은 관련 법령에 따른 관할 법원에서 해결합니다.

## 부칙

본 약관은 2026년 09월 16일부터 시행합니다.
$$, 1, DATE '2026-09-16', TRUE)
ON CONFLICT (category, language) DO UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, version = EXCLUDED.version, effective_date = EXCLUDED.effective_date, is_active = EXCLUDED.is_active, updated_at = CURRENT_TIMESTAMP;

INSERT INTO policy_documents (category, language, title, content, version, effective_date, is_active)
VALUES ('TERMS', 'EN', 'WAVEY Terms of Service', $$# WAVEY Terms of Service

Announcement date: September 16, 2026  
Effective date: September 16, 2026

Operator: WAVEY Development Team (Representative: Si-young Yoo)<br>
Address: Room 511, Building 3, Dongyang Mirae University, 445 Gyeongin-ro, Guro-gu, Seoul, Republic of Korea<br>
Contact: wavey164@gmail.com

## Article 1. Purpose

These Terms of Service establish the rights, obligations, and responsibilities of WAVEY Development Team (Representative: Si-young Yoo, the “Operator”) and Users in connection with the WAVEY service (the “Service”).

## Article 2. Definitions

1. “User” means a member or non-member who uses the Service under these Terms.
2. “Member” means a User who enters into a service agreement through social login such as Google or Kakao.
3. “Route” means a travel route and related information created, stored, or made public by a Member.
4. “Content” means Routes, reviews, images, text, and other information entered, uploaded, or created by a User.

## Article 3. Effect and Formation of the Service Agreement

1. The Operator posts these Terms on the Service and provides a way for Users to review and consent to them.
2. The service agreement is formed when a User agrees to these Terms, applies for membership, and the Operator accepts the application.
3. Non-members may use features available without membership within the scope of their consent.
4. Registration and personal information processing for persons under 14 follow applicable laws, including legal representative consent and identity verification. If such registration is not allowed, the registration screen will state so.
5. Matters not specified in these Terms are governed by applicable laws, the Privacy Policy, Location Information Terms of Use, and service-specific operating policies.

## Article 4. Provision of the Service

1. The Operator may provide the following services.
   - Creation, viewing, editing, deletion, and publication of travel Routes
   - Tourist attraction and cultural heritage information
   - Image OCR, translation, cultural heritage and landmark identification, and web detection
   - Location-based place and visit verification, stamps, and badges
   - Reviews, saved places, and other convenience features
2. Some features may be limited by the User’s device, permissions, network, or external services.
3. The Service is currently free. Paid features, if added, will be notified with their terms, fees, and refund policy in advance.
4. External services or links may be included, and their providers’ terms and privacy policies apply.

## Article 5. Member Information and Account Management

1. Users must provide accurate information and keep it up to date.
2. Users must not use another person’s information or account, or transfer, lend, or share an account.
3. The Operator may suspend or restrict an account for long-term inactivity, false information, violation of these Terms, or violation of law.
4. Users are responsible for managing their accounts and devices and must promptly report unauthorized use or security incidents.

## Article 6. User Obligations and Prohibited Acts

Users must not:

1. Provide false information, impersonate another person, or unlawfully use another person’s account
2. Infringe copyrights, portrait rights, privacy rights, trademarks, or other rights of the Operator, Users, or third parties
3. Upload or distribute illegal, obscene, violent, discriminatory, defamatory, or harmful Content
4. Disrupt the Service through scraping, crawling, reverse engineering, excessive automated requests, malware, or attacks
5. Use the Service for unauthorized advertising, solicitation, resale, or other commercial purposes
6. Collect, disclose, or misuse another User’s personal information
7. Violate applicable laws, these Terms, or the Operator’s operating policies

## Article 7. User Content and Intellectual Property

1. Copyright and other rights in User-created Content belong to the User unless applicable law or a separate agreement provides otherwise.
2. By uploading or publishing Content, the User grants the Operator a non-exclusive, worldwide, royalty-free license to store, reproduce, transmit, publish, and transform the Content as necessary to operate and improve the Service. The Operator will not sell the Content or use it as separate advertising material outside the Service purpose.
3. The User must hold the rights necessary to publish Content and is responsible for infringement or disputes.
4. Public Routes and reviews may be exposed to other Users. Users must check for personal and location information before publication.
5. The Operator may restrict, temporarily block, or delete Content when infringement, illegality, a security threat, or a serious operational issue is identified.
6. Reports or appeals may be submitted to wavey164@gmail.com.

## Article 8. Limitations of Service Information

1. Information about places, Routes, operating hours, fees, transportation, cultural heritage, and safety is for reference only.
2. Users should verify the latest information with the relevant facility or authority before visiting or traveling.
3. The Operator does not guarantee the accuracy, currency, or continuous availability of external information.

## Article 9. Changes and Suspension of the Service

1. The Operator may change or suspend all or part of the Service for maintenance, security, technical improvements, external-service changes, legal requirements, or other reasonable operational reasons.
2. Advance notice will be provided where reasonably possible. It may not be possible during emergencies, security incidents, failures, or other unavoidable circumstances.
3. The Operator is not responsible for interruptions caused by force majeure, the User’s device or network, or external providers, except as required by law or caused by the Operator’s willful misconduct or negligence.

## Article 10. Restrictions and Termination

1. The Operator may restrict or suspend all or part of the Service when a User violates these Terms, applicable laws, operating policies, or creates a security or operational risk.
2. In urgent cases involving illegal Content, account takeover, or security attacks, the Operator may delete Content or restrict use without prior notice.
3. Unless urgent action is required, the Operator will notify the User of the reason and period and provide an appeal method.
4. Users may terminate the service agreement and request membership withdrawal at any time through the Service.

## Article 11. Membership Withdrawal and Data Processing

1. Upon withdrawal, the Operator promptly deletes member information and Member-created data except information required to be retained by law.
2. Public Routes, reviews, or Content already shared or quoted by other Users may not be immediately recalled. The Operator may remove identifying information or process the Content as necessary for legal compliance and Service operation.
3. Before withdrawal, Users should review the Routes, reviews, profile images, and other Content they want to delete.

## Article 12. Protection of Personal and Location Information

1. The Operator processes personal information under the Privacy Policy.
2. Separate notices and consent procedures may apply to features using current location or coordinates entered for image analysis.
3. Users may deny location permissions or withdraw consent. Some location-based features may then be unavailable.

## Article 13. Limitation of Liability and Damages

1. The Operator is not liable for damage caused by interruptions without the Operator’s willful misconduct or negligence, external-service failures, the User’s fault, or force majeure.
2. Except for the Operator’s willful misconduct or negligence, the Operator is not liable for damage caused by changes, omissions, or errors in Service information.
3. This Article does not limit liability for willful misconduct, gross negligence, or liability that cannot be limited by law.
4. A User who causes damage by violating these Terms must compensate the Operator or the affected third party.

## Article 14. Changes to the Terms

1. The Operator may change these Terms within the limits of applicable law.
2. General changes will be announced through the Service or notices at least 15 days before the effective date.
3. Unfavorable or material changes will be individually notified or announced by equivalent means at least 30 days before the effective date.
4. Users who do not agree may request withdrawal before the effective date. The Operator will provide the changes, effective date, and refusal method.
5. The change history and effective dates will be managed for User review.

## Article 15. Notices and Contact

1. The Operator may notify Members by email, in-Service notifications, or notices.
2. Notices to all Members may be posted in Service notices, but material matters will be individually notified where possible.
3. Inquiries may be submitted to wavey164@gmail.com.

## Article 16. Governing Law and Jurisdiction

1. These Terms are interpreted under the laws of the Republic of Korea.
2. The Operator and User will make good-faith efforts to resolve disputes through consultation.
3. Unresolved disputes will be handled by a court with jurisdiction under applicable law.

## Addendum

These Terms of Service take effect on September 16, 2026.$$, 1, DATE '2026-09-16', TRUE)
ON CONFLICT (category, language) DO UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, version = EXCLUDED.version, effective_date = EXCLUDED.effective_date, is_active = EXCLUDED.is_active, updated_at = CURRENT_TIMESTAMP;

INSERT INTO policy_documents (category, language, title, content, version, effective_date, is_active)
VALUES ('PRIVACY', 'KO', 'WAVEY 개인정보 처리방침', $$# WAVEY 개인정보 처리방침

공고일: 2026년 09월 16일  
시행일: 2026년 09월 16일

## 1. 총칙 및 개인정보처리자

WAVEY 개발팀(대표자: 유시영, 이하 “운영자”)은 WAVEY 서비스(이하 “서비스”)를 제공하면서 이용자의 개인정보를 보호하고 관련 법령을 준수합니다.

- 운영자 또는 법인명: WAVEY 개발팀
- 대표자: 유시영
- 주소: 서울특별시 구로구 경인로 445 동양미래대학교 3호관 511호
- 개인정보 보호책임자 또는 담당자: 유시영
- 이메일: wavey164@gmail.com

## 2. 개인정보의 처리 목적

운영자는 다음 목적에 필요한 범위에서 개인정보를 처리합니다.

1. 회원 식별, 소셜 로그인, 회원가입 및 계정 관리
2. 로그인 코드 발급·교환, Access Token 및 Refresh Token의 발급·재발급·폐기
3. 프로필과 서비스 설정의 저장 및 제공
4. 여행 루트, 리뷰, 저장한 장소, 스탬프 및 배지 기능 제공
5. 이미지 OCR, 번역, 문화유산·랜드마크 식별 및 웹 탐지
6. 현재 위치 표시, 방문 확인, 거리 계산 및 좌표 기반 정보 제공
7. 비정상적인 요청, 계정 도용 및 서비스 보안 위협의 탐지·방지
8. 문의·민원 처리, 장애 대응 및 서비스 공지
9. 서비스 이용 통계와 품질 개선

운영자는 위 목적 외의 용도로 개인정보를 이용하지 않습니다. 처리 목적이 변경되는 경우 관련 법령에 따라 별도로 안내하거나 필요한 동의를 받습니다.

## 3. 처리하는 개인정보 항목, 법적 근거 및 보유기간

### 가. 회원가입 및 계정 관리

- 처리 항목: 소셜 로그인 제공자, 제공자별 회원 식별값, 이메일, 이름, 닉네임, 서비스 내부 회원번호 및 권한
- 수집 출처: Google 또는 Kakao 소셜 로그인 제공자 및 이용자
- 처리 목적: 회원 식별, 로그인, 회원 관리 및 관리자 권한 확인
- 법적 근거: 서비스 이용계약의 체결·이행 또는 이용자의 동의
- 보유기간: 회원 탈퇴 또는 이용계약 종료 시까지. 법령에 따라 보존해야 하는 정보는 해당 기간 동안 별도 보관합니다.

### 나. 프로필 및 서비스 설정

- 처리 항목: 프로필 이미지, 닉네임, 국가코드, 언어 설정, 위치·알림·스탬프·루트·마케팅 설정
- 처리 목적: 프로필 표시, 서비스 환경 설정 및 기능 제공
- 법적 근거: 서비스 이용계약의 이행 또는 이용자의 동의
- 보유기간: 회원 탈퇴 또는 해당 정보의 삭제·변경 시까지
- 비고: 프로필 이미지는 서비스 설정에 따라 S3 호환 오브젝트 스토리지에 저장될 수 있습니다.

### 다. 여행 루트 및 이용자 콘텐츠

- 처리 항목: 회원번호, 루트 이름, 설명, 공개 여부, 포함 장소와 장소 순서, 리뷰 내용, 이미지, 생성·수정 시각
- 처리 목적: 루트와 리뷰의 저장·조회·수정·삭제, 공개 콘텐츠 제공 및 서비스 운영
- 법적 근거: 서비스 이용계약의 이행
- 보유기간: 이용자가 삭제하거나 회원 탈퇴할 때까지. 다른 이용자가 이미 공유·인용한 공개 콘텐츠는 식별정보를 제거하거나 필요한 범위에서 별도 처리할 수 있습니다.

### 라. 저장·방문·스탬프 기능

- 처리 항목: 회원번호, 저장한 장소 식별값, 방문일, 스탬프·배지 획득 및 이용 기록
- 처리 목적: 저장 장소, 방문 확인, 스탬프·배지 및 개인화된 서비스 제공
- 법적 근거: 서비스 이용계약의 이행
- 보유기간: 회원 탈퇴 또는 이용자가 해당 데이터를 삭제할 때까지

### 마. 이미지 분석

- 처리 항목: 이용자가 업로드한 이미지, 선택한 분석 기능, 이미지에서 추출된 텍스트, 분석 결과, 선택적으로 입력한 위도·경도
- 처리 목적: OCR, 번역, 문화유산·랜드마크 식별, 웹 탐지 및 관련 설명 제공
- 법적 근거: 이용자의 기능 이용 요청에 따른 서비스 이용계약의 이행
- 보유기간: WAVEY 서버는 분석 요청 처리에 필요한 동안만 이미지를 처리하며, 처리 완료 후 회원 DB에 영구 저장하지 않습니다.
- 비고: 분석을 위해 이미지·추출 텍스트·선택적 좌표가 Google Cloud 서비스로 전송될 수 있습니다. 외부 서비스의 임시 보관과 데이터 학습 여부는 실제 계약 및 콘솔 설정에 따라 확정하여 별도 공개합니다.

### 바. 위치 기반 기능

- 처리 항목: 기기에서 확인된 현재 위도·경도, 방문 장소와의 거리 계산 결과
- 처리 목적: 현재 위치 표시, 방문 확인, 거리 계산, 좌표 기반 문화유산 후보 보정 및 주소 변환
- 법적 근거: 위치정보 이용에 대한 이용자의 동의 및 서비스 이용계약의 이행
- 보유기간: 지도 표시·방문 확인은 원칙적으로 기능 제공에 필요한 동안만 처리하며, 분석 요청에 함께 입력된 좌표는 분석 처리 완료 시까지 보유합니다.
- 비고: 위치정보를 이용하는 기능은 별도의 위치정보 관련 고지·동의 절차가 적용될 수 있으며, 위치 권한을 거부하면 해당 기능 이용이 제한될 수 있습니다.

### 사. 인증 및 보안

- 처리 항목: 일회용 로그인 코드의 해시값, Refresh Token의 해시값, Access Token 식별자, 회원번호, 토큰 만료정보, 회원번호 또는 비회원의 IP 주소, 요청 횟수 및 시간 구간
- 처리 목적: 로그인 완료, 토큰 재발급, 중복 사용 방지, 로그아웃 처리, 비정상 요청 제한 및 계정 보안
- 법적 근거: 서비스 이용계약의 이행 및 운영자의 정당한 이익
- 보유기간:
  - 일회용 로그인 코드: 기본 3분 또는 코드 사용 시까지
  - Refresh Token 해시: 토큰 만료 또는 로그아웃·탈퇴 시까지
  - 로그아웃된 Access Token 식별자: 해당 토큰 만료 시까지
  - 요청 제한 정보: 기본 60초의 제한 구간 종료 시까지
- 비고: 현재 인증 로직은 원문 토큰 대신 해시값을 Redis에 저장합니다.

### 아. 서비스 이용기록 및 로그

- 처리 항목: 접속 일시, 요청 경로, 오류 기록, 브라우저·기기 정보, IP 주소
- 처리 목적: 장애 대응, 보안, 부정 이용 방지 및 서비스 운영
- 법적 근거: 운영자의 정당한 이익 또는 관계 법령상 의무
- 보유기간: 실제 Railway 및 애플리케이션 로그 설정에 따른 기간을 확정하여 공개합니다. 법령상 보존이 필요한 경우 해당 기간 동안 보관합니다.

## 4. 개인정보의 수집 출처

1. 이용자가 직접 입력하거나 업로드한 정보
2. Google 또는 Kakao가 이용자의 동의에 따라 제공하는 소셜 계정 정보
3. 이용자의 브라우저 또는 기기가 권한에 따라 제공하는 위치 정보
4. 서비스 이용 과정에서 자동 생성되는 접속·오류·보안 정보

소셜 로그인 제공자가 전달하는 항목은 각 제공자의 동의 화면에서 이용자에게 안내됩니다.

## 5. 개인정보의 제3자 제공

운영자는 원칙적으로 이용자의 개인정보를 제3자에게 제공하지 않습니다. 다만 다음 경우에는 예외로 합니다.

1. 이용자가 사전에 동의한 경우
2. 법령에 특별한 규정이 있는 경우
3. 이용자 또는 제3자의 생명·신체·재산 보호를 위해 긴급하게 필요한 경우로서 법령상 요건을 충족한 경우

Google 또는 Kakao 소셜 로그인 과정에서 각 제공자가 정한 범위의 정보가 제공될 수 있으며, 이용자에게 제공받는 자·목적·항목을 별도로 안내하고 동의를 받습니다.

## 6. 개인정보 처리위탁 및 외부 처리 서비스

운영자는 원활한 서비스 제공을 위해 개인정보 처리 업무의 일부를 외부 업체에 위탁할 수 있습니다. 수탁자는 위탁 목적을 벗어나 개인정보를 처리할 수 없으며, 운영자는 안전성 확보조치와 재위탁 여부를 관리·감독합니다.

| 수탁자 또는 서비스 | 위탁 업무 | 처리 항목 | 보유기간 및 국가 |
| --- | --- | --- | --- |
| Railway | PostgreSQL, Redis 및 서버 인프라 운영 | 회원정보, 루트·리뷰·설정 데이터, 인증 해시, 운영 로그 | 계약·서비스 운영기간 및 실제 리전 기준으로 확정 |
| Google Cloud Platform | OCR, 번역, 문화유산·웹 탐지, 지오코딩 | 업로드 이미지, 추출 텍스트, 선택적 좌표 | 요청 처리 또는 계약상 기간, 실제 처리 국가 기준으로 확정 |
| S3 호환 오브젝트 스토리지 제공자 | 프로필 이미지 저장·조회 | 프로필 이미지 및 객체 식별정보 | 회원 탈퇴·이미지 삭제 또는 계약 종료 시까지, 실제 사업자·국가 기준으로 확정 |
| SK텔레콤 TMAP | 관광 장소 간 이동 경로 계산 | 출발·도착 장소 좌표, 이동수단 | 요청 처리 또는 API 정책상 기간 |

위 표의 대괄호 또는 “확정” 항목은 실제 계약서, 콘솔, 리전 및 로그 설정을 확인한 후 공개 전에 구체적인 값으로 교체해야 합니다. 이용하지 않는 업체는 표에서 삭제합니다.

## 7. 개인정보의 국외 이전

Google Cloud, Railway 또는 해외 스토리지 사업자를 이용하는 경우 개인정보가 국외에서 처리·보관될 수 있습니다. 국외 이전이 발생하는 업체별로 다음 사항을 실제 계약과 콘솔 설정에 맞게 공개합니다.

- 이전받는 자의 정확한 법인명과 연락처
- 이전 국가
- 이전되는 개인정보 항목
- 이전 목적
- 이전 시기 및 방법
- 보유·이용기간
- 국외 이전의 법적 근거
- 이전 거부 방법과 거부 시 제한되는 기능

현재 국외 이전 여부와 국가·법인·연락처는 Railway, Google Cloud 및 실제 스토리지 계약과 리전 설정을 확인한 뒤 확정합니다. 국외 이전이 필수인 기능과 선택 기능을 구분하여 안내하고, 가능한 경우 기능별로 이전에 동의하지 않을 방법을 제공합니다.

## 8. 개인정보의 보유 및 파기

1. 운영자는 처리 목적이 달성되거나 보유기간이 끝난 개인정보를 지체 없이 파기합니다.
2. 전자적 파일은 복구·재생이 어렵도록 안전하게 삭제합니다.
3. Redis 인증정보는 로그아웃·탈퇴 또는 설정된 만료시간에 따라 삭제합니다.
4. 관계 법령에 따라 보존해야 하는 정보는 보존 근거와 기간을 확인하여 다른 개인정보와 분리 보관하고, 해당 기간이 끝나면 파기합니다.
5. 회원 탈퇴 시 회원 테이블뿐 아니라 루트, 리뷰, 저장 장소, 설정, 스탬프·배지 기록 및 프로필 이미지 등 연계 데이터를 삭제하거나, 서비스 운영상 필요한 경우 식별정보를 제거하여 익명화합니다.
6. 백업에 사본이 남는 경우 복구·운영 절차상 필요한 기간 동안 제한적으로 보관한 후 순차적으로 삭제합니다.

## 9. 이용자 기기에 저장되는 정보 및 자동 수집 장치

### 가. 브라우저 저장소

WAVEY 프론트엔드는 서비스 운영을 위해 다음 정보를 이용자 기기에 저장할 수 있습니다.

- 세션 저장소: Access Token 또는 화면 상태 정보
- 로컬 저장소: Refresh Token, 프로필 표시정보, 위치·알림 설정, 선택한 루트 장소 식별값, 스탬프·방문일, 읽은 알림 식별값 및 개발·테스트용 모의 위치정보

이용자는 브라우저의 저장 데이터 삭제 기능으로 이를 삭제할 수 있습니다. 회원 탈퇴 시 서버 데이터와 브라우저 저장정보가 함께 삭제되도록 서비스 구현을 유지·점검합니다.

### 나. 쿠키 및 자동 생성 정보

소셜 로그인과 서비스 운영 과정에서 인증 상태 유지를 위한 임시 쿠키 또는 세션 정보가 사용될 수 있습니다. 쿠키를 차단하면 로그인 등 일부 기능이 제한될 수 있습니다. 분석·광고 목적의 쿠키나 SDK를 추가하는 경우 종류, 목적, 보유기간 및 거부 방법을 본 방침에 반영합니다.

## 10. 이용자와 법정대리인의 권리 및 행사 방법

이용자는 언제든지 다음 권리를 행사할 수 있습니다.

1. 개인정보 처리 여부 확인 및 열람 요구
2. 개인정보 정정 또는 삭제 요구
3. 개인정보 처리정지 요구
4. 동의 철회 및 회원 탈퇴
5. 위치정보 이용의 일시 중지, 동의 철회, 이용·제공사실 확인자료의 열람 및 정정 요구

회원정보와 설정은 서비스에서 직접 조회·수정할 수 있으며, 그 밖의 권리 행사는 wavey164@gmail.com로 요청할 수 있습니다. 운영자는 본인 또는 정당한 대리인인지 확인한 후 관련 법령이 정한 기간 내에 처리합니다.

만 14세 미만 이용자의 권리는 법정대리인이 행사할 수 있으며, 법정대리인 확인을 위해 필요한 최소한의 정보를 요청할 수 있습니다.

## 11. 개인위치정보의 처리

1. 서비스는 현재 위치 표시, 방문 확인, 거리 계산 및 좌표 기반 정보 제공을 위해 개인위치정보를 이용할 수 있습니다.
2. 위치정보를 이용하는 기능의 구체적인 내용, 동의 철회, 일시 중지, 열람·정정 및 문의 방법은 별도의 위치정보 이용약관과 서비스 화면의 동의 절차에서 안내합니다.
3. 운영자는 이용자의 사전 동의 없이 개인위치정보를 제3자에게 제공하지 않습니다. 제3자 제공이 발생하는 경우 제공받는 자, 제공 목적 및 제공 항목을 사전에 안내합니다.
4. 위치정보 이용·제공사실 확인자료의 기록·보존 여부와 보존기간은 위치정보법 및 실제 시스템 구현에 맞게 확정하여 공개합니다.
5. 위치정보 이용약관이 적용되는 위치기반서비스사업의 신고 필요 여부를 출시 전에 확인합니다.

## 12. 개인정보의 안전성 확보조치

운영자는 개인정보 보호를 위해 다음 조치를 시행합니다.

1. Refresh Token 및 일회용 로그인 코드의 원문 대신 해시값 처리
2. Access Token과 Refresh Token의 용도 및 만료기간 분리
3. 일회용 로그인 코드의 단기 만료와 일회성 사용
4. 로그아웃된 Access Token의 식별자 차단
5. 관리자 기능에 대한 권한 검사
6. 비정상적인 이미지 분석 요청 제한
7. 개인정보 접근 권한의 최소화와 정기적인 점검
8. 통신 구간 암호화 및 중요정보의 암호화
9. 인증정보와 API 키의 환경변수 분리 관리
10. 개인정보 처리시스템 접속기록 및 오류 기록의 관리

## 13. 권익침해 구제방법

개인정보 침해에 대한 상담이나 신고가 필요한 경우 다음 기관에 문의할 수 있습니다.

- 개인정보침해 신고센터: 국번 없이 118
- 개인정보분쟁조정위원회: 1833-6972
- 경찰청 사이버범죄 신고시스템: 국번 없이 182

## 14. 개인정보 처리방침의 변경

1. 본 방침은 2026년 09월 16일부터 적용합니다.
2. 일반적인 변경은 시행일 최소 7일 전에 서비스 공지사항 등을 통해 안내합니다.
3. 이용자 권리 또는 처리 목적·항목·보유기간에 중대한 변경이 있는 경우 시행일 최소 30일 전에 별도로 안내합니다.
4. 변경된 방침의 공고일·시행일과 이전 방침은 이용자가 확인할 수 있도록 관리합니다.

## 부칙

본 개인정보 처리방침은 2026년 09월 16일부터 시행합니다.
$$, 1, DATE '2026-09-16', TRUE)
ON CONFLICT (category, language) DO UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, version = EXCLUDED.version, effective_date = EXCLUDED.effective_date, is_active = EXCLUDED.is_active, updated_at = CURRENT_TIMESTAMP;

INSERT INTO policy_documents (category, language, title, content, version, effective_date, is_active)
VALUES ('PRIVACY', 'EN', 'WAVEY Privacy Policy', $$# WAVEY Privacy Policy

Announcement date: September 16, 2026  
Effective date: September 16, 2026

## 1. General Provisions and Personal Information Controller

WAVEY Development Team (Representative: Si-young Yoo, the “Operator”) provides the WAVEY service (the “Service”) while protecting Users’ personal information and complying with applicable laws.

- Operator or entity: WAVEY Development Team
- Representative: Si-young Yoo
- Address: Room 511, Building 3, Dongyang Mirae University, 445 Gyeongin-ro, Guro-gu, Seoul, Republic of Korea
- Personal Information Protection Officer or contact person: Si-young Yoo
- Email: wavey164@gmail.com

## 2. Purposes of Processing Personal Information

The Operator processes personal information only as necessary for the following purposes.

1. Member identification, social login, membership registration, and account management
2. Issuing, exchanging, renewing, and revoking login codes, Access Tokens, and Refresh Tokens
3. Storing and providing profiles and Service settings
4. Providing travel Routes, reviews, saved places, stamps, and badges
5. Providing image OCR, translation, cultural heritage and landmark identification, and web detection
6. Displaying current location, verifying visits, calculating distances, and providing coordinate-based information
7. Detecting and preventing abnormal requests, account takeovers, and security threats
8. Handling inquiries and complaints, responding to failures, and providing notices
9. Measuring Service usage and improving quality

The Operator does not use personal information for other purposes. If a purpose changes, separate notice or consent will be provided as required by law.

## 3. Personal Information Items, Legal Basis, and Retention

### A. Membership Registration and Account Management

- Items: social login provider, provider-specific Member identifier, email, name, nickname, internal Member number, and role
- Source: Google or Kakao and the User
- Purpose: Member identification, login, account management, and administrator permission checks
- Legal basis: entering into or performing the Service agreement or User consent
- Retention: until withdrawal or termination of the Service agreement, except information retained separately for the period required by law

### B. Profile and Service Settings

- Items: profile image, nickname, country code, language setting, and location, notification, stamp, Route, and marketing settings
- Purpose: profile display, Service settings, and feature provision
- Legal basis: performance of the Service agreement or User consent
- Retention: until withdrawal or deletion or modification of the relevant information
- Note: Profile images may be stored in S3-compatible object storage.

### C. Travel Routes and User Content

- Items: Member number, Route name, description, visibility, places and their order, review content, images, and creation and modification times
- Purpose: storing, viewing, editing, and deleting Routes and reviews, providing public Content, and operating the Service
- Legal basis: performance of the Service agreement
- Retention: until deletion by the User or withdrawal. Public Content already shared or quoted may be processed after removing identifiers or as necessary.

### D. Saved Places, Visits, Stamps, and Badges

- Items: Member number, saved place identifiers, visit date, and stamp and badge records
- Purpose: saved places, visit verification, stamps, badges, and personalized features
- Legal basis: performance of the Service agreement
- Retention: until withdrawal or deletion by the User

### E. Image Analysis

- Items: uploaded images, selected analysis features, extracted text, analysis results, and optional latitude and longitude
- Purpose: OCR, translation, cultural heritage and landmark identification, web detection, and related explanations
- Legal basis: performance of the Service agreement requested by the User
- Retention: images are processed only as long as necessary for the request and are not permanently stored in the Member database after processing.
- Note: Images, extracted text, and optional coordinates may be sent to Google Cloud. Temporary storage and training practices will be disclosed after confirming contracts and console settings.

### F. Location-Based Features

- Items: current latitude and longitude identified by the device and distance-calculation results
- Purpose: displaying current location, verifying visits, calculating distances, coordinate-based candidate correction, and address conversion
- Legal basis: User consent to location information processing and performance of the Service agreement
- Retention: current location is generally processed only as long as necessary for the feature, and coordinates included in an analysis request are retained until processing is complete.
- Note: Separate location notices and consent procedures may apply. Denying permission may limit the features.

### G. Authentication and Security

- Items: hashes of one-time login codes and Refresh Tokens, Access Token identifiers, Member number, token expiration information, Member or non-member IP address, request counts, and time windows
- Purpose: login, token renewal, duplicate-use prevention, logout, abnormal-request limits, and account security
- Legal basis: performance of the Service agreement and the Operator’s legitimate interest
- Retention:
  - One-time login code: three minutes by default or until use
  - Refresh Token hash: until expiration, logout, or withdrawal
  - Logged-out Access Token identifier: until token expiration
  - Rate-limit information: until the default 60-second window ends
- Note: The current authentication logic stores hashes in Redis, not raw tokens.

### H. Service Usage Records and Logs

- Items: access time, request path, error records, browser and device information, and IP address
- Purpose: failure response, security, misuse prevention, and Service operation
- Legal basis: the Operator’s legitimate interest or legal obligation
- Retention: the actual period configured for Railway and application logs will be confirmed and disclosed. Legally required records are kept for the required period.

## 4. Sources of Personal Information

1. Information entered or uploaded directly by the User
2. Social account information provided by Google or Kakao with the User’s consent
3. Location information provided by the User’s browser or device according to granted permissions
4. Access, error, and security information automatically generated during Service use

Items provided by social login providers are disclosed on each provider’s consent screen.

## 5. Provision to Third Parties

The Operator does not generally provide personal information to third parties. Exceptions apply when:

1. The User has given prior consent.
2. A special legal provision applies.
3. Disclosure is urgently necessary to protect the life, body, or property of the User or a third party and legal requirements are met.

Information within the scope provided by Google or Kakao may be shared during social login. The recipient, purpose, items, and consent process will be separately disclosed.

## 6. Entrustment and External Services

The Operator may entrust part of personal information processing to external providers. Providers may not process information beyond the entrusted purpose, and the Operator manages their safeguards and re-entrustment.

| Entrusted provider or service | Entrusted work | Items processed | Retention period and country |
| --- | --- | --- | --- |
| Railway | PostgreSQL, Redis, and server infrastructure | Member information, Route, review and settings data, authentication hashes, and logs | During the contract and Service operation; actual region to be confirmed |
| Google Cloud Platform | OCR, translation, cultural heritage and web detection, and geocoding | Uploaded images, extracted text, and optional coordinates | During processing or the contractual period; actual country to be confirmed |
| S3-compatible object storage provider | Profile image storage and retrieval | Profile images and object identifiers | Until withdrawal, image deletion, or contract termination; provider and country to be confirmed |
| SK Telecom TMAP | Route calculation between tourist places | Origin and destination coordinates and travel mode | During processing or the API policy period |

Items requiring confirmation must be replaced with specific values based on actual contracts, console settings, regions, and logs before publication. Unused providers must be removed.

## 7. International Transfer

If Google Cloud, Railway, or an overseas storage provider is used, personal information may be processed or stored outside the Republic of Korea. For each provider, the following will be disclosed based on the actual contract and console settings.

- Exact recipient legal name and contact
- Transfer country
- Items transferred
- Purpose, timing, and method
- Retention and use period
- Legal basis
- How to refuse and resulting Service limitations

International transfer status, countries, legal entities, contacts, and purposes will be confirmed based on actual contracts and region settings. Required and optional transfers will be distinguished where possible.

## 8. Retention and Destruction

1. Personal information is promptly destroyed when the purpose is achieved or the retention period ends.
2. Electronic files are safely deleted to make recovery or reproduction difficult.
3. Redis authentication information is deleted on logout, withdrawal, or expiration.
4. Legally retained information is stored separately and destroyed when the required period ends.
5. Upon withdrawal, linked data including Members, Routes, reviews, saved places, settings, stamps, badges, and profile images is deleted or anonymized as necessary.
6. Backup copies are deleted according to the backup deletion process after the period necessary for recovery and operation.

## 9. Device Storage and Automatic Collection

### A. Browser Storage

The frontend may store the following information on the User’s device.

- Session storage: Access Token or screen state
- Local storage: Refresh Token, profile display information, location and notification settings, selected Route place identifiers, stamp and visit dates, read-notification identifiers, and development or test mock location information

Users may delete this information through the browser’s clear-storage feature. The Service is maintained so that browser storage is also deleted upon withdrawal.

### B. Cookies and Automatically Generated Information

Temporary cookies or session information may be used for authentication and Service operation. Blocking cookies may limit login and other features. If analytics or advertising cookies or SDKs are added, their types, purposes, retention periods, and refusal methods will be reflected in this Policy.

## 10. Rights and How to Exercise Them

Users may exercise the following rights at any time.

1. Request confirmation and access to processing
2. Request correction or deletion
3. Request suspension of processing
4. Withdraw consent and request membership withdrawal
5. Temporarily suspend location processing, withdraw consent, and request access to or correction of records confirming use or provision of location information

Members may view or modify their information and settings in the Service. Other rights may be exercised by contacting wavey164@gmail.com. The Operator will verify the requester and process the request within the period required by law.

Legal representatives may exercise the rights of Users under 14. The Operator may request the minimum information necessary for verification.

## 11. Processing of Personal Location Information

1. The Service may use personal location information to display current location, verify visits, calculate distances, and provide coordinate-based information.
2. Details about location features, consent withdrawal, temporary suspension, access, correction, and inquiries are provided in separate Location Information Terms of Use and the Service consent process.
3. The Operator does not provide personal location information to third parties without prior consent. If provision occurs, the recipient, purpose, and items are disclosed in advance.
4. Whether records confirming use or provision are recorded and retained, and their retention period, will be confirmed based on the Location Information Act and actual implementation.
5. Before launch, the Operator will confirm whether location-based service provider registration is required.

## 12. Safeguards

The Operator implements the following measures.

1. Hashing Refresh Tokens and one-time login codes
2. Separating Access Token and Refresh Token purposes and expiration
3. Short expiration and one-time use of login codes
4. Blocking logged-out Access Token identifiers
5. Authorization checks for administrator features
6. Limiting abnormal image-analysis requests
7. Minimizing and reviewing access permissions
8. Encrypting communications and important information
9. Separating credentials and API keys into environment variables
10. Managing access and error records

## 13. Remedies

Users may contact the following organizations for consultation or reporting.

- Personal Information Infringement Report Center: 118
- Personal Information Dispute Mediation Committee: 1833-6972
- National Police Agency Cybercrime Report System: 182

## 14. Changes

1. This Policy applies from September 16, 2026.
2. General changes will be announced at least seven days before the effective date.
3. Material changes to rights, purposes, items, or retention periods will be announced at least 30 days before the effective date.
4. Announcement and effective dates and previous versions will be managed for User review.

## Addendum

This Privacy Policy takes effect on September 16, 2026.$$, 1, DATE '2026-09-16', TRUE)
ON CONFLICT (category, language) DO UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, version = EXCLUDED.version, effective_date = EXCLUDED.effective_date, is_active = EXCLUDED.is_active, updated_at = CURRENT_TIMESTAMP;

