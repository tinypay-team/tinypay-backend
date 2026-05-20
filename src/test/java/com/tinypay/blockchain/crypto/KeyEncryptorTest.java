package com.tinypay.blockchain.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * KeyEncryptor 단위 테스트
 *
 * 검사 카테고리:
 * - 정상 암호화/복호화 (round-trip)
 * - 랜덤성 검증 (같은 평문 두 번 암호화 시 결과 다름)
 * - 변조 감지 (GCM 인증 태그)
 * - 마스터 키 검증 (잘못된 길이)
 * - 엣지 케이스 (빈 문자열, 한글, 긴 문자열)
 */
class KeyEncryptorTest {

    // 테스트용 32바이트 키 (실제 .env 키와 무관)
    private static final String TEST_MASTER_KEY =
        Base64.getEncoder().encodeToString(new byte[32]);

    private KeyEncryptor encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new KeyEncryptor(TEST_MASTER_KEY);
        // @PostConstruct는 Spring 컨텍스트 없이 자동 호출 안 되므로 수동 호출
        encryptor.init();
    }

    @Nested
    @DisplayName("정상 암호화/복호화 검증")
    class 정상_케이스 {

        @Test
        @DisplayName("암호화 후 복호화하면 원본과 일치한다")
        void 라운드_트립_검증() {
            String original = "0xabc123def456ghi789jkl012mno345pqr678stu901vwx234yz567abc890def123";

            String encrypted = encryptor.encrypt(original);
            String decrypted = encryptor.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(original);
        }

        @Test
        @DisplayName("암호화 결과는 평문과 다르다")
        void 암호화_결과_평문과_다름() {
            String original = "my_secret_private_key";

            String encrypted = encryptor.encrypt(original);

            assertThat(encrypted).isNotEqualTo(original);
            assertThat(encrypted).isNotEmpty();
        }

        @Test
        @DisplayName("같은 평문을 두 번 암호화하면 결과가 다르다 (IV 랜덤성)")
        void IV_랜덤성_검증() {
            String original = "same_plaintext";

            String encrypted1 = encryptor.encrypt(original);
            String encrypted2 = encryptor.encrypt(original);

            assertThat(encrypted1).isNotEqualTo(encrypted2);

            // 그래도 둘 다 복호화하면 동일
            assertThat(encryptor.decrypt(encrypted1)).isEqualTo(original);
            assertThat(encryptor.decrypt(encrypted2)).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("변조 감지 검증")
    class 변조_감지 {

        @Test
        @DisplayName("암호문이 변조되면 복호화 실패한다")
        void 변조된_암호문_복호화_실패() {
            String original = "private_key_content";
            String encrypted = encryptor.encrypt(original);

            // 마지막 글자를 다른 글자로 변조
            char lastChar = encrypted.charAt(encrypted.length() - 1);
            char modifiedChar = (lastChar == 'A') ? 'B' : 'A';
            String tampered = encrypted.substring(0, encrypted.length() - 1) + modifiedChar;

            assertThatThrownBy(() -> encryptor.decrypt(tampered))
                .isInstanceOf(EncryptionException.class)
                .hasMessageContaining("복호화 실패");
        }

        @Test
        @DisplayName("길이가 너무 짧은 암호문은 복호화 실패한다")
        void 짧은_암호문_복호화_실패() {
            // IV(12바이트)도 안 되는 짧은 데이터
            String tooShort = Base64.getEncoder().encodeToString(new byte[5]);

            assertThatThrownBy(() -> encryptor.decrypt(tooShort))
                .isInstanceOf(EncryptionException.class);
        }
    }

    @Nested
    @DisplayName("마스터 키 검증")
    class 마스터키_검증 {

        @Test
        @DisplayName("32바이트가 아닌 마스터 키는 init 시 실패한다")
        void 잘못된_길이_마스터키() {
            // 16바이트 키 (32바이트 아님)
            String invalidKey = Base64.getEncoder().encodeToString(new byte[16]);
            KeyEncryptor invalidEncryptor = new KeyEncryptor(invalidKey);

            assertThatThrownBy(invalidEncryptor::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32바이트");
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class 엣지_케이스 {

        @Test
        @DisplayName("빈 문자열도 암호화/복호화 가능하다")
        void 빈_문자열_라운드_트립() {
            String original = "";

            String encrypted = encryptor.encrypt(original);
            String decrypted = encryptor.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(original);
        }

        @Test
        @DisplayName("한글 문자열도 암호화/복호화 가능하다")
        void 한글_라운드_트립() {
            String original = "안녕하세요 티니페이 입니다";

            String encrypted = encryptor.encrypt(original);
            String decrypted = encryptor.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(original);
        }

        @Test
        @DisplayName("긴 문자열도 암호화/복호화 가능하다")
        void 긴_문자열_라운드_트립() {
            String original = "a".repeat(10000);

            String encrypted = encryptor.encrypt(original);
            String decrypted = encryptor.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(original);
        }
    }
}
