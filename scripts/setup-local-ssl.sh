#!/bin/bash
#
# 로컬 개발 환경 SSL 인증서 설정 스크립트
# mkcert를 사용하여 신뢰할 수 있는 로컬 인증서 생성
#

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 프로젝트 루트 디렉토리
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
CERTS_DIR="$PROJECT_ROOT/certs"

# 인증서 파일 경로
CERT_FILE="$CERTS_DIR/localhost.pem"
KEY_FILE="$CERTS_DIR/localhost-key.pem"
P12_FILE="$CERTS_DIR/localhost.p12"
P12_PASSWORD="changeit"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  로컬 SSL 인증서 설정${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 1. mkcert 설치 확인
echo -e "${YELLOW}[1/4] mkcert 설치 확인...${NC}"
if ! command -v mkcert &> /dev/null; then
    echo -e "${RED}mkcert가 설치되어 있지 않습니다.${NC}"
    echo ""

    # OS 감지 및 설치 안내
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macOS에서 설치:"
        echo -e "  ${GREEN}brew install mkcert${NC}"
        echo -e "  ${GREEN}brew install nss${NC}  # Firefox 사용 시"
        echo ""
        read -p "Homebrew로 자동 설치하시겠습니까? (y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            brew install mkcert
            if command -v firefox &> /dev/null; then
                brew install nss
            fi
        else
            exit 1
        fi
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "Linux에서 설치:"
        echo -e "  ${GREEN}sudo apt install libnss3-tools${NC}"
        echo -e "  ${GREEN}brew install mkcert${NC}  # Linuxbrew 사용 시"
        echo ""
        echo "또는 GitHub에서 직접 다운로드:"
        echo "  https://github.com/FiloSottile/mkcert/releases"
        exit 1
    else
        echo "지원하지 않는 OS입니다. mkcert를 수동으로 설치해주세요."
        echo "  https://github.com/FiloSottile/mkcert"
        exit 1
    fi
fi
echo -e "${GREEN}✓ mkcert 설치 확인 완료${NC}"
echo ""

# 2. 로컬 CA 설치
echo -e "${YELLOW}[2/4] 로컬 CA 설치...${NC}"
if ! mkcert -CAROOT &> /dev/null || [ ! -f "$(mkcert -CAROOT)/rootCA.pem" ]; then
    echo "로컬 CA를 시스템에 설치합니다. (관리자 권한 필요)"
    mkcert -install
else
    echo -e "${GREEN}✓ 로컬 CA가 이미 설치되어 있습니다.${NC}"
fi
echo ""

# 3. 인증서 디렉토리 생성
echo -e "${YELLOW}[3/4] 인증서 생성...${NC}"
mkdir -p "$CERTS_DIR"

# 기존 인증서 확인
if [ -f "$CERT_FILE" ] && [ -f "$KEY_FILE" ]; then
    read -p "기존 인증서가 있습니다. 재생성하시겠습니까? (y/n): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "기존 인증서를 유지합니다."
    else
        rm -f "$CERT_FILE" "$KEY_FILE" "$P12_FILE"
    fi
fi

# 인증서 생성
if [ ! -f "$CERT_FILE" ] || [ ! -f "$KEY_FILE" ]; then
    cd "$CERTS_DIR"
    mkcert -cert-file localhost.pem -key-file localhost-key.pem localhost 127.0.0.1 ::1
    echo -e "${GREEN}✓ 인증서 생성 완료${NC}"
fi
echo ""

# 4. PKCS12 형식 변환 (Spring Boot용)
echo -e "${YELLOW}[4/4] PKCS12 변환 (Spring Boot용)...${NC}"
if [ ! -f "$P12_FILE" ] || [ "$CERT_FILE" -nt "$P12_FILE" ]; then
    openssl pkcs12 -export \
        -in "$CERT_FILE" \
        -inkey "$KEY_FILE" \
        -out "$P12_FILE" \
        -name localhost \
        -password pass:$P12_PASSWORD
    echo -e "${GREEN}✓ PKCS12 변환 완료${NC}"
else
    echo -e "${GREEN}✓ PKCS12 파일이 이미 존재합니다.${NC}"
fi
echo ""

# 5. Spring Boot 리소스에 복사
SPRING_RESOURCES="$PROJECT_ROOT/app/server/dop-global-apps-server/src/main/resources"
if [ -d "$SPRING_RESOURCES" ]; then
    cp "$P12_FILE" "$SPRING_RESOURCES/localhost.p12"
    echo -e "${GREEN}✓ Spring Boot 리소스에 복사 완료${NC}"
fi

# 완료 메시지
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  설정 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "생성된 파일:"
echo "  - $CERT_FILE"
echo "  - $KEY_FILE"
echo "  - $P12_FILE"
echo ""
echo "CA Root 위치:"
echo "  $(mkcert -CAROOT)"
echo ""
echo -e "${YELLOW}다음 단계:${NC}"
echo "  1. 백엔드 실행: cd app/server && ./gradlew bootRun"
echo "  2. 프론트엔드 실행: cd app/client && npm run dev"
echo "  3. 브라우저에서 https://localhost:8443 접속"
echo ""
echo -e "${GREEN}브라우저 경고 없이 HTTPS 사용 가능합니다!${NC}"
