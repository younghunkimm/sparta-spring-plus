# OIDC

> OpenID Connect의 약자로 OAuth 2.0에 기반한 인증 프로토콜 \
> OIDC를 사용하면 긴 수명의 AWS 엑세스키를 GitHub에 제공할 필요가 없다. \
> 사용할 때만 유효한 짧은 생명의 엑세스 토큰을 사용할 수 있기 때문에 탈취당하더라도 보안상 훨씬 안전

---

### OIDC를 활용한 GitHub Actions > AWS EC2 배포 흐름
1. **IAM Role & ARN**
   - AWS에서 필요한 권한을 가진 **IAM Role**을 생성
   - 이 역할의 **ARN**을 GitHub Secrets에 저장 (`AWS_ROLE_ARN` 등)

2. **OIDC 기반 인증**
   - GitHub Actions에서 OIDC 토큰을 통해 AWS에 AssumeRole 요청
   - 요청 시 전달되는 `sub`(리포지토리·브랜치)와 `region`이 **Trust Policy 조건**에 맞을 경우 권한이 부여됨

3. **부여된 권한**
   - **SSM 권한**
     - 태그 기반으로 지정된 EC2 인스턴스에 명령 실행 가능
       - `ssm:SendCommand`
     - 실행 결과 확인 가능
       - `ssm:GetCommandInvocation`
       - `ssm:ListCommands`
       - `ssm:ListCommandInvocations`
   - **Parameter Store 읽기 권한**
     - `github/spring-plus/...` 경로 하위 매개변수 읽기 가능
       - `ssm:GetParameter`
       - `ssm:GetParameters`
       - `ssm:GetParametersByPath`

4. **실행 절차**
   - GitHub Actions Workflow 실행 → OIDC 인증 → Role Assume → 권한 획득
   - **SSM SendCommand** 호출
     - 대상: 특정 태그(예: `SSMTarget=spring-plus`)가 지정된 EC2 인스턴스
     - 실행 문서: `spring-plus-deploy` (SSM Document)
   - **SSM Document 스크립트 실행**
     - EC2 인스턴스에서 배포/운영 스크립트 수행
   - 실행 중 필요한 **환경 변수/시크릿 값**은 Parameter Store에서 읽어옴

---

### Github actions에서 AWS 접근 시 허용할 최소 권한의 정책 생성
<img width="2541" height="973" alt="image" src="https://github.com/user-attachments/assets/aa1e45f9-0621-4fd6-a1d1-07e2afb660e8" />

### IAM > Identity provider
<img width="2106" height="754" alt="image" src="https://github.com/user-attachments/assets/a2548036-56e2-41b8-a4f5-2f9282ca62bc" />

### IAM > Role
<img width="1796" height="887" alt="image" src="https://github.com/user-attachments/assets/6683043c-a5a6-4ba8-88fe-2342be44cda6" />
<img width="2540" height="975" alt="image" src="https://github.com/user-attachments/assets/987ea873-b494-4035-814b-0a6357111ff7" />
<img width="2550" height="495" alt="image" src="https://github.com/user-attachments/assets/53dc9c1f-5301-40f1-b28b-114f0c80386f" />
