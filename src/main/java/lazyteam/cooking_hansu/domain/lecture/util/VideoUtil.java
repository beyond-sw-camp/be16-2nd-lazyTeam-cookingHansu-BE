package lazyteam.cooking_hansu.domain.lecture.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.Arrays;
import java.util.Set;

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

        //  move(transferTo) 대신 copy: 원본 임시파일을 건드리지 않음
        try (InputStream in = multipartFile.getInputStream()) {
            java.nio.file.Files.copy(
                    in, convFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }
        return convFile;
    }


    // ffprobe로 컨테이너 포맷 추출 후 확장자 검증 후, 확장자 리턴.
    // VideoUtil.java
    public String detectAndValidateContainerExt(MultipartFile multipartFile) {
        final Set<String> mp4Family = Set.of("mp4","mov","m4a","3gp","3g2","mj2");
        try {
            File file = convertToFileVideoValidated(multipartFile); //fix

            String[] cmd = {"ffprobe","-v","error","-show_entries","format=format_name",
                    "-of","default=noprint_wrappers=1:nokey=1", file.getAbsolutePath()};
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();

            String output;
            try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
                output = br.readLine();
            }
            int exit = p.waitFor();
            boolean deleted = file.delete(); if (!deleted) file.deleteOnExit();

            if (exit != 0 || output == null || output.isBlank()) {
                throw new IllegalArgumentException("영상 포맷 추출 실패(ffprobe)");
            }

            // 핵심 로그: 여기 찍히면 ffprobe 감지는 됨
            log.info("[FFPROBE] filename={}, format_name={}", multipartFile.getOriginalFilename(), output);

            var names = java.util.Arrays.stream(output.toLowerCase().split(","))
                    .map(String::trim).filter(s -> !s.isBlank())
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

            if (names.contains("avi")) return "avi";
            boolean isMp4 = names.stream().anyMatch(mp4Family::contains);
            if (isMp4) {
                String ori = java.util.Optional.ofNullable(multipartFile.getOriginalFilename()).orElse("").toLowerCase();
                return ori.endsWith(".mov") ? "mov" : "mp4";
            }
            throw new IllegalArgumentException("허용되지 않은 영상 포맷: " + output); // 이 메시지 보이면 ffprobe 검증이 원인
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("영상 포맷 감지 실패: " + e.getMessage(), e); // 이 메시지 보이면 ffprobe 실행 자체 문제
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
                case "png": return "png"; //fix
                case "mjpeg": // ffmpeg의 jpeg 계열 이름 //fix
                case "jpeg": return "jpg"; //fix
                case "bmp": return "bmp"; //fix
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

    private static final java.util.Set<String> MP4_ALIASES = java.util.Set.of("mp4","mov","m4a","3gp","3g2","mj2"); //fix

    private java.util.Set<String> probeFormatNames(File file) throws IOException, InterruptedException { //fix
        String[] cmd = {
                "ffprobe","-v","error",
                "-show_entries","format=format_name",
                "-of","default=noprint_wrappers=1:nokey=1",
                file.getAbsolutePath()
        };
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String out;
        try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
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
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    } //fix

    private File convertToFileVideoValidated(MultipartFile multipartFile) throws IOException, InterruptedException { //fix
        File convFile = convertToFile(multipartFile);
        try {
            var names = probeFormatNames(convFile);
            boolean isAvi = names.contains("avi");
            boolean isMp4Family = names.stream().anyMatch(MP4_ALIASES::contains);
            if (!(isAvi || isMp4Family)) {
                java.nio.file.Files.deleteIfExists(convFile.toPath());
                throw new IllegalArgumentException("허용되지 않은 영상 포맷입니다. (허용: mp4, mov, avi) → " + names);
            }
            return convFile;
        } catch (RuntimeException | IOException | InterruptedException e) {
            java.nio.file.Files.deleteIfExists(convFile.toPath());
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
        try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) { //fix
            out = br.readLine(); //fix
        } //fix
        int exit = p.waitFor(); //fix
        if (exit != 0) return null; //fix
        return out; //fix
    } //fix
}
