# AWS EC2

### Instance
<img width="2301" height="212" alt="image" src="https://github.com/user-attachments/assets/cb742de9-684f-4712-b271-e2572d9aad80" />

### User Data Script
- EC2 인스턴스가 처음 부팅될 때 자동 실행되는 초기화 스크립트(User Data)입니다.
- `apt-get update -y`: 우분투 패키지 목록을 최신 상태로 갱신
- `apt-get upgrade -y` -> 설치된 패키지들을 최신 버전으로 자동 업그레이드
<img width="1080" height="485" alt="image" src="https://github.com/user-attachments/assets/ebf9558f-3a65-4f67-8e66-b3835ab42a37" />

- 서버에 필요한 도구(Docker, crul, AWS CLI v2 등)들은 `SSM Run Command` 서비스를 사용하여 각 EC2 Instance에 설치 진행

### Security
- `Application Load Balancer`에서 접근할 수 있도록 `8080` 포트를 열어두었습니다.
- SSH로 접근하여 서버 상태를 직접 확인할 수 있도록 `22` 포트를 열어두었습니다.
<img width="1618" height="693" alt="image" src="https://github.com/user-attachments/assets/18e7d9bf-d4d1-45a5-87f9-255802c1c1eb" />

### Tag
- 해당 태그를 기준으로 배포 됩니다.
<img width="695" height="275" alt="image" src="https://github.com/user-attachments/assets/0692e6d1-a2a9-412e-af2f-be5ed4cf7c15" />

### Role & Policy
<img width="1619" height="304" alt="image" src="https://github.com/user-attachments/assets/946e54f7-93c0-468e-bb09-60e078618c20" />

- `AmazonSSMManagedInstanceCore`: AWS Systems Manager(SSM)와 통신할 수 있는 최소 권한을 부여하는 관리형 IAM 정책
- `read-parameter-store`: SSM Parameter Store에 저장된 파라미터(환경변수) 읽기 권한을 지정한 커스텀 인라인 정책
- `S3ObjectsReadWritePolicy`: S3 버킷/경로 안의 객체들에 접근할 수 있는 최소 권한을 지정한 커스텀 인라인 정책
