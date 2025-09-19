# OIDC

> OpenID Connect의 약자로 OAuth 2.0에 기반한 인증 프로토콜 \
> OIDC를 사용하면 긴 수명의 AWS 엑세스키를 GitHub에 제공할 필요가 없다. \
> 사용할 때만 유효한 짧은 생명의 엑세스 토큰을 사용할 수 있기 때문에 탈취당하더라도 보안상 훨씬 안전

---

### OIDC를 활용한 GitHub Actions > AWS EC2 배포 흐름
1. **IAM Role & ARN**
   - AWS에서 SSM 권한을 가진 **IAM Role**을 생성
   - 이 역할의 **ARN**을 GitHub Secrets에 저장 (`AWS_ROLE_ARN`)

2. **OIDC 기반 인증**
   - GitHub Actions에서 OIDC 토큰을 통해 AWS에 AssumeRole 요청
   - 요청 시 전달되는 `sub`(예: GitHub 리포지토리·브랜치 정보)와 `region`이 신뢰 정책(Trust Policy) 조건에 부합하면 Role 권한 부여

3. **SSM Document 실행**
   - 부여된 권한으로 AWS CLI/SDK에서 **SSM SendCommand** 호출
   - 대상은 **특정 태그(SSMTarget)** 가 붙은 EC2 인스턴스들
   - SSM Document에 정의된 **배포/운영 스크립트**가 해당 인스턴스에서 실행됨

---

### Github actions에서 AWS 접근 시 허용할 최소 권한의 정책 생성
<img width="2541" height="973" alt="image" src="https://github.com/user-attachments/assets/aa1e45f9-0621-4fd6-a1d1-07e2afb660e8" />

### IAM > Identity provider
<img width="2106" height="754" alt="image" src="https://github.com/user-attachments/assets/a2548036-56e2-41b8-a4f5-2f9282ca62bc" />

### IAM > Role
<img width="1796" height="887" alt="image" src="https://github.com/user-attachments/assets/6683043c-a5a6-4ba8-88fe-2342be44cda6" />
<img width="2550" height="495" alt="image" src="https://github.com/user-attachments/assets/53dc9c1f-5301-40f1-b28b-114f0c80386f" />
