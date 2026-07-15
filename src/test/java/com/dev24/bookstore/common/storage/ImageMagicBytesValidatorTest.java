package com.dev24.bookstore.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ImageMagicBytesValidatorTest {

    private final ImageMagicBytesValidator validator = new ImageMagicBytesValidator();

    @Test
    void isValidImage_jpegSignature_returnsTrue() {
        byte[] content = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01, 0x02};
        assertThat(validator.isValidImage(content, "reviews/photo.jpg")).isTrue();
        assertThat(validator.isValidImage(content, "reviews/photo.jpeg")).isTrue();
    }

    @Test
    void isValidImage_pngSignature_returnsTrue() {
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00};
        assertThat(validator.isValidImage(content, "reviews/photo.png")).isTrue();
    }

    @Test
    void isValidImage_gifSignature_returnsTrue() {
        byte[] content = "GIF89a".getBytes();
        assertThat(validator.isValidImage(content, "reviews/photo.gif")).isTrue();
    }

    @Test
    void isValidImage_webpSignature_returnsTrue() {
        byte[] content = new byte[]{
                'R', 'I', 'F', 'F', 0x00, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P'};
        assertThat(validator.isValidImage(content, "reviews/photo.webp")).isTrue();
    }

    // 실행 파일 시그니처(예: ELF)처럼 이미지가 아닌 임의 바이트는 어떤 확장자를 붙여도 거부되는지 검증
    @Test
    void isValidImage_nonImageBytes_returnsFalse() {
        byte[] content = {0x7F, 'E', 'L', 'F', 0x02, 0x01};
        assertThat(validator.isValidImage(content, "reviews/malware.jpg")).isFalse();
    }

    // 확장자는 jpg인데 실제 내용은 PNG인 경우처럼 확장자-시그니처 불일치도 거부되는지 검증
    @Test
    void isValidImage_extensionSignatureMismatch_returnsFalse() {
        byte[] pngContent = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        assertThat(validator.isValidImage(pngContent, "reviews/fake.jpg")).isFalse();
    }

    @Test
    void isValidImage_unsupportedExtension_returnsFalse() {
        byte[] content = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        assertThat(validator.isValidImage(content, "reviews/photo.exe")).isFalse();
    }
}
