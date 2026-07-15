package com.dev24.bookstore.common.storage;

import org.springframework.stereotype.Component;

// 파일 확장자가 아니라 실제 바이트 시그니처(매직 바이트)로 진짜 이미지가 맞는지 검증한다
//  - presigned URL 업로드 방식은 서버가 업로드 시점에 바이트를 못 보므로(PresignedUploadRequest의 @Pattern은 파일명만 확인),
// 업로드 후 이 검증이 유일하게 실제 내용을 확인하는 지점이다.
//
// ImageIO.read(...)로 디코딩을 시도하는 방식도 검토했지만, JDK 기본 ImageIO는 WebP를 지원하지 않아(플러그인
// 필요) 정상 webp 파일도 검증 실패로 오판하게 된다 - 그래서 포맷별 매직 바이트 직접 비교를 택했다.
@Component
public class ImageMagicBytesValidator {

    public boolean isValidImage(byte[] content, String objectKey) {
        String extension = extractExtension(objectKey);
        return switch (extension) {
            case "jpg", "jpeg" -> startsWith(content, 0xFF, 0xD8, 0xFF);
            case "png" -> startsWith(content, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif" -> startsWith(content, 'G', 'I', 'F', '8'); // GIF87a/GIF89a 공통 접두
            case "webp" -> content.length >= 12
                    && startsWith(content, 'R', 'I', 'F', 'F')
                    && matchesAt(content, 8, 'W', 'E', 'B', 'P');
            default -> false;
        };
    }

    private String extractExtension(String objectKey) {
        int dotIndex = objectKey.lastIndexOf('.');
        return dotIndex < 0 ? "" : objectKey.substring(dotIndex + 1).toLowerCase();
    }

    private boolean startsWith(byte[] content, int... signature) {
        return matchesAt(content, 0, signature);
    }

    private boolean matchesAt(byte[] content, int offset, int... signature) {
        if (content.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((content[offset + i] & 0xFF) != (signature[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}
