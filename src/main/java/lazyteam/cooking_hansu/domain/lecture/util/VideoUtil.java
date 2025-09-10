package lazyteam.cooking_hansu.domain.lecture.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.*;

@Component // 스프링이 자동으로 Bean으로 등록해서 DI 주입 가능하게 함
public class VideoUtil {

    private static final Logger log = LoggerFactory.getLogger(VideoUtil.class);

    // MultipartFile(업로드된 파일)에서 영상 길이를 초 단위로 추출하는 메서드
    public int extractDuration(MultipartFile multipartFile) throws IOException, InterruptedException {
        // MultipartFile을 실제 임시 File 객체로 변환 (ffprobe는 File 경로 기반으로만 분석 가능)
        File file = convertToFileVideoValidated(multipartFile); //fix

        log.info("임시파일 전환성공 : "+ file);
        // ffprobe 명령어 구성, 경로를 절대 경로로 지정
        // -v error: 에러만 출력
        // -show_entries format=duration: 영상 길이 정보만 추출
        // -of default=noprint_wrappers=1:nokey=1: 포맷을 숫자만 출력되게 설정
        // 마지막 인자는 분석할 파일 경로
        String[] command = {
                "ffprobe", // 여기에 절대 경로를 입력합니다. 추후에 yml에 경로 지정후 불러오는 식으로 디벨롭
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                file.getAbsolutePath()
        };

        log.info(command + " 명령어 생성완료");

        // 명령어 실행을 위한 프로세스 생성
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // stderr(에러)도 stdout으로 합쳐서 읽을 수 있게 함
        Process process = pb.start(); // ffprobe 실행

        log.info("명령어 프로세스 생성완료");

        // ffprobe 결과를 읽기 위한 스트림 준비
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine(); // 결과는 영상 길이(초)가 문자열로 한 줄 출력됨
        log.info("line 완료 " + line);

        int exitCode = process.waitFor(); // 프로세스 종료까지 대기
        file.delete(); // 사용 끝난 임시 파일 삭제 (자원 정리)

        log.info("임시파일 삭제 완료 ");

        // 결과 값 없거나 실패했을 경우 예외 던지기
        if (exitCode != 0 || line == null) {
            throw new RuntimeException("영상 길이 추출 실패");
        }

        // 문자열로 받아온 영상 길이 (ex. "123.456")를 double로 파싱
        double durationSeconds = Double.parseDouble(line);
        log.info("영상길이 생성 완료");
        return (int) durationSeconds; // 소숫점 버리고 정수로 반환

    }

    // MultipartFile을 임시 File 객체로 변환 (ffmpeg/ffprobe는 MultipartFile 직접 사용 불가)
//    이전 코드는 영문 파일명이 들어가면 오류가 발생하지 않았지만 한글 파일이 들어갈 경우 오류가 날 수도 있어서 파일의 확장자만 임시 파일로 넣음
    private File convertToFile(MultipartFile multipartFile) throws IOException {
        String originalName = multipartFile.getOriginalFilename();
        String suffix = ".mp4";
        if (originalName != null && originalName.lastIndexOf('.') >= 0) {
            suffix = originalName.substring(originalName.lastIndexOf('.'));
        }
        
        File convFile = File.createTempFile("upload_", suffix);
        log.debug("[CONVERT] 임시파일 생성: {} -> {}", originalName, convFile.getAbsolutePath());

        //  move(transferTo) 대신 copy: 원본 임시파일을 건드리지 않음
        try (InputStream in = multipartFile.getInputStream()) {
            long bytesCopied = Files.copy(
                    in, convFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            log.debug("[CONVERT] 파일 복사 완료: {} bytes", bytesCopied);
        } catch (IOException e) {
            log.error("[CONVERT] 파일 복사 실패: {}", e.getMessage(), e);
            // 임시 파일 정리
            if (!convFile.delete()) {
                convFile.deleteOnExit();
            }
            throw e;
        }
        
        return convFile;
    }


    // ffprobe로 컨테이너 포맷 추출 후 확장자 검증 후, 확장자 리턴.
    // VideoUtil.java
    public String detectAndValidateContainerExt(MultipartFile multipartFile) {
        final Set<String> mp4Family = Set.of("mp4","mov","m4a","3gp","3g2","mj2");
        File file = null;
        try {
            // 중복 검증 제거: convertToFileVideoValidated 대신 convertToFile 사용
            file = convertToFile(multipartFile);
            log.info("[FFPROBE] 임시파일 생성 완료: {}", file.getAbsolutePath());

            // 1차 시도: 기본 ffprobe 명령어
            String output = tryFfprobe(file, multipartFile.getOriginalFilename());
            
            if (output != null && !output.isBlank()) {
                // 핵심 로그: 여기 찍히면 ffprobe 감지는 됨
                log.info("[FFPROBE] 성공 - filename={}, format_name={}", multipartFile.getOriginalFilename(), output);

                var names = Arrays.stream(output.toLowerCase().split(","))
                        .map(String::trim).filter(s -> !s.isBlank())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                // 포맷 검증
                if (names.contains("avi")) return "avi";
                boolean isMp4 = names.stream().anyMatch(mp4Family::contains);
                if (isMp4) {
                    String ori = Optional.ofNullable(multipartFile.getOriginalFilename()).orElse("").toLowerCase();
                    return ori.endsWith(".mov") ? "mov" : "mp4";
                }
                throw new IllegalArgumentException("허용되지 않은 영상 포맷: " + output);
            }

            // 2차 시도: 파일 확장자 기반으로 추정
            log.warn("[FFPROBE] ffprobe 실패, 파일 확장자로 추정 - filename: {}", multipartFile.getOriginalFilename());
            return fallbackToExtension(multipartFile.getOriginalFilename());

        } catch (IOException | InterruptedException e) {
            log.error("[FFPROBE] 예외 발생 - filename: {}, error: {}", 
                     multipartFile.getOriginalFilename(), e.getMessage(), e);
            throw new IllegalArgumentException("영상 포맷 감지 실패: " + e.getMessage(), e);
        } finally {
            // 파일 정리
            if (file != null && !file.delete()) {
                file.deleteOnExit();
                log.debug("[FFPROBE] 임시파일 정리 완료: {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * ffprobe 실행 시도
     */
    private String tryFfprobe(File file, String originalFilename) throws IOException, InterruptedException {
        String[] cmd = {"ffprobe","-v","error","-show_entries","format=format_name",
                "-of","default=noprint_wrappers=1:nokey=1", file.getAbsolutePath()};
        
        log.info("[FFPROBE] 명령어 실행: {}", String.join(" ", cmd));
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();

        String output;
        try (var br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            output = br.readLine();
        }
        int exit = p.waitFor();

        if (exit != 0 || output == null || output.isBlank()) {
            log.error("[FFPROBE] 실패 - exit: {}, output: '{}', filename: {}", 
                     exit, output, originalFilename);
            return null;
        }

        return output;
    }

    /**
     * ffprobe 실패 시 파일 확장자로 포맷 추정
     */
    private String fallbackToExtension(String originalFilename) {
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 null입니다.");
        }

        String lowerName = originalFilename.toLowerCase();
        
        if (lowerName.endsWith(".avi")) {
            return "avi";
        } else if (lowerName.endsWith(".mov")) {
            return "mov";
        } else if (lowerName.endsWith(".mp4") || lowerName.endsWith(".m4a") || 
                   lowerName.endsWith(".3gp") || lowerName.endsWith(".3g2") || 
                   lowerName.endsWith(".mj2")) {
            return lowerName.endsWith(".mov") ? "mov" : "mp4";
        } else {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자: " + originalFilename);
        }
    }


    // 이미지 포맷(헤더) 감지 후 허용 확장자 검증 -> 확장자 리턴
// 허용: png, jpg, jpeg, bmp
    public String detectAndValidateImageExt(MultipartFile multipartFile) {
        File file = null; //fix
        try { //fix
            file = convertToFile(multipartFile); //fix
            String codec = probeFirstVideoCodecName(file); //fix
            if (codec == null || codec.isBlank()) { //fix
                throw new IllegalArgumentException("이미지 형식 인식 실패(ffprobe)"); //fix
            } //fix
            codec = codec.toLowerCase(); //fix

            // jpg/jpeg 정규화는 codec 기준으로 처리
            switch (codec) { //fix
                case "png":
                    return "png"; //fix
                case "bmp":
                    return "bmp"; //fix

                case "mjpeg": // ffmpeg에서 JPEG 코덱명 //fix
                case "jpeg":
                case "jpg":
                    return "jpg"; //fix
                case "webp":  return "webp";

                default:
                    throw new IllegalArgumentException("허용되지 않은 이미지 포맷: " + codec); //fix
            }
        } catch (IOException | InterruptedException e) { //fix
            throw new IllegalArgumentException("이미지 포맷 감지 실패(ffprobe)", e); //fix
        } finally { //fix
            if (file != null && !file.delete()) file.deleteOnExit(); //fix
        } //fix
    }


    // ======================= 아래는 추가된 헬퍼들 =======================

    private static final Set<String> MP4_ALIASES = java.util.Set.of("mp4","mov","m4a","3gp","3g2","mj2"); //fix

    private Set<String> probeFormatNames(File file) throws IOException, InterruptedException { //fix
        String[] cmd = {
                "ffprobe","-v","error",
                "-show_entries","format=format_name",
                "-of","default=noprint_wrappers=1:nokey=1",
                file.getAbsolutePath()
        };
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String out;
        try (var br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            out = br.readLine();
        }
        int exit = p.waitFor();
        if (exit != 0 || out == null || out.isBlank()) {
            throw new IllegalArgumentException("영상 포맷 추출 실패(ffprobe)");
        }
        log.info("[FFPROBE] temp={}, format_name={}", file.getName(), out);
        return Arrays.stream(out.toLowerCase().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    } //fix

    private File convertToFileVideoValidated(MultipartFile multipartFile) throws IOException, InterruptedException { //fix
        File convFile = convertToFile(multipartFile);
        try {
            var names = probeFormatNames(convFile);
            boolean isAvi = names.contains("avi");
            boolean isMp4Family = names.stream().anyMatch(MP4_ALIASES::contains);
            if (!(isAvi || isMp4Family)) {
                Files.deleteIfExists(convFile.toPath());
                throw new IllegalArgumentException("허용되지 않은 영상 포맷입니다. (허용: mp4, mov, avi) → " + names);
            }
            return convFile;
        } catch (RuntimeException | IOException | InterruptedException e) {
            Files.deleteIfExists(convFile.toPath());
            throw e;
        }
    } //fix

    private String probeFirstVideoCodecName(File file) throws IOException, InterruptedException { //fix
        String[] cmd = { //fix
                "ffprobe","-v","error", //fix
                "-select_streams","v:0", // 첫 번째 비디오 스트림 선택 //fix
                "-show_entries","stream=codec_name", // codec_name만 출력 //fix
                "-of","default=noprint_wrappers=1:nokey=1", // 값만 출력 //fix
                file.getAbsolutePath() //fix
        }; //fix
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start(); //fix
        String out; //fix
        try (var br = new BufferedReader(new InputStreamReader(p.getInputStream()))) { //fix
            out = br.readLine(); //fix
        } //fix
        int exit = p.waitFor(); //fix
        if (exit != 0) return null; //fix
        return out; //fix
    } //fix
}
