package lazyteam.cooking_hansu.domain.lecture.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;

@Component // 스프링이 자동으로 Bean으로 등록해서 DI 주입 가능하게 함
public class VideoUtil {

    private static final Logger log = LoggerFactory.getLogger(VideoUtil.class);

    // MultipartFile(업로드된 파일)에서 영상 길이를 초 단위로 추출하는 메서드
    public int extractDuration(MultipartFile multipartFile) throws IOException, InterruptedException {
        // MultipartFile을 실제 임시 File 객체로 변환 (ffprobe는 File 경로 기반으로만 분석 가능)
        File file = convertToFile(multipartFile);

        log.info("임시파일 전환성공 : "+ file);
        // ffprobe 명령어 구성, 경로를 절대 경로로 지정
        // -v error: 에러만 출력
        // -show_entries format=duration: 영상 길이 정보만 추출
        // -of default=noprint_wrappers=1:nokey=1: 포맷을 숫자만 출력되게 설정
        // 마지막 인자는 분석할 파일 경로
        String[] command = {
                "C:\\ffmpeg-7.0.2-essentials_build\\ffmpeg-7.0.2-essentials_build\\bin\\ffprobe.exe", // 여기에 절대 경로를 입력합니다. 추후에 yml에 경로 지정후 불러오는 식으로 디벨롭
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
        // 원본 파일명 가져오기
        String originalName = multipartFile.getOriginalFilename();

        // 확장자만 추출 (없으면 기본값 .mp4)
        String suffix = ".mp4";
        if (originalName != null && originalName.lastIndexOf('.') >= 0) {
            suffix = originalName.substring(originalName.lastIndexOf('.'));
        }

        // 임시 파일 생성 (영문 prefix + 안전한 suffix)
        File convFile = File.createTempFile("upload_", suffix);

        // MultipartFile → File 변환
        multipartFile.transferTo(convFile);

        return convFile;
    }
}
