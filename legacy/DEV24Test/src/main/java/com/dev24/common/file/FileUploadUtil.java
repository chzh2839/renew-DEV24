package com.dev24.common.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.dev24.client.book.vo.BookVO;

import lombok.extern.log4j.Log4j;

@Log4j
public class FileUploadUtil {

	// 파일 업로드할 폴더 생성 자바에서는 자동으로 폴더가 생성 가능하다. 조건문: 만약 fileDirectory(파일 경로) 가 존재한다면,
	// 메소드 종료 (return;)
	/* 파일 관련된 메소드는 공통으로 사용되기때문에 정적으로 생성해준다 */
	public static void makeDir(String docRoot) {
		log.info("docRoot: " + docRoot);
		File fileDir = new File(docRoot);
		if (fileDir.exists()) {
			log.info("[" + fileDir + "].exists() : " + fileDir.exists());
			return;
		}
		fileDir.mkdirs();
	}

	public static String bookImgUpload(MultipartFile file, BookVO bvo, String imgUsage)
			throws IllegalStateException, IOException {
		log.info("bookImgUpload 호출 성공");

		String docRoot = "";
		String fullPath = "";
		int b_num = bvo.getB_num();
		int cateOne_num = bvo.getCateOne_num();
		int cateTwo_num = bvo.getCateTwo_num();

		String real_name = null;
		// MultipleFile 클래스의 getFile()메서드로 클라이언트가 업로드한 파일
		String org_name = file.getOriginalFilename();
		log.info("org_name : " + org_name);

		// 파일명 변경
		if (org_name != null && (!org_name.equals(""))) {

			if (imgUsage.equals("listcover")) {
				real_name = b_num + "-listcover.jpg";
			} else if (imgUsage.equals("detailcover")) {
				real_name = b_num + "-detailcover.jpg";
			} else if (imgUsage.equals("detail")) {
				real_name = b_num + "-detail.jpg";
			} else {
				log.info("imgUsage는 listcover/detailcover/detail 만 명시할수 있습니다.");
				return null;
			}
			
			docRoot = "C:\\uploadStorage\\bookimg\\" + cateOne_num + "\\" + cateTwo_num + "\\";
			makeDir(docRoot);
			
			fullPath =  docRoot + real_name;
			File fileAdd = new File(fullPath); // 파일 생성 후
			log.info("업로드할 파일(fileAdd) : " + fileAdd);

			file.transferTo(fileAdd);
		}
		return fullPath.replace("C:", "");
	}

	
	/**
	 * 파일 업로드 메서드 파일명 중복시 해결 방법 System.currentTimeMillis() 를 사용하거나 UUID는 128비트의 수이다.
	 * 표준 형식에서 UUID는 32개의 16진수로 표현되며 36개 문자(32개 문자와 4개의 하이픈)로 된 8-4-4-4-12라는 5개의 그룹을
	 * 하이픈으로 구분한다. 이를테면 다음과 같다. 이때 UUID.randomUUID().toString()를 이용해서 얻는다.
	 * 50e8400-e29b-41d4-a716-d446655440000 fileName<예> : board_0000000_a.gif
	 * 
	 * @param file
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String fileUpload(MultipartFile file, String fileName) throws IOException {
		log.info("fileUpload호출 성공");

		String real_name = null;
		// MultipleFile 클래스의 getFile()메서드로 클라이언트가 업로드한 파일
		String org_name = file.getOriginalFilename();
		log.info("org_name : " + org_name);

		// 파일명 변경
		if (org_name != null && (!org_name.equals(""))) {
			real_name = fileName + "_" + System.currentTimeMillis() + "_" + org_name;// 저장할 파일 이름

			String docRoot = "C:\\uploadStorage\\" + fileName;
			makeDir(docRoot);

			File fileAdd = new File(docRoot + "/" + real_name); // 파일 생성 후
			log.info("업로드할 파일(fileAdd) : " + fileAdd);

			file.transferTo(fileAdd);
		}
		return real_name;
	}

	/**
	 * 여러 파일 업로드 메소드
	 * 
	 * @param file
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static List<String> MultipleFileUpload(List<MultipartFile> file, String fileName) throws IOException {

		log.info("fileUpload 호출 성공");

		List<String> real_name = new ArrayList<String>();
		String name = "";
		// 파일명 변경
		if (!file.isEmpty()) {

			String docRoot = "C:\\uploadStorage\\" + fileName;
			makeDir(docRoot);
			File fileAdd = null;
			for (MultipartFile multiFile : file) {
				name = fileName + "_" + UUID.randomUUID().toString().replaceAll("-", "") + "_"
						+ multiFile.getOriginalFilename();// 저장할 파일 이름

				fileAdd = new File(docRoot + "/" + name);
				log.info("업로드할 파일(fileAdd) : " + fileAdd);

				multiFile.transferTo(fileAdd);// 파일 저장
				real_name.add(name);
			}
		}
		return real_name;
	}

	public static void fileDelete(String fileName) throws IOException {
		log.info("fileDelete 호출 성공");
		boolean result = false;
		String startDirName = "", docRoot = "";
		String dirName = fileName.substring(0, fileName.indexOf("_"));

		if (dirName.equals("thumnail")) {
			startDirName = fileName.substring(dirName.length() + 1, fileName.indexOf("_", dirName.length() + 1));
			docRoot = "C:\\uploadStorage\\" + startDirName + "\\" + dirName;
		} else {
			docRoot = "C:\\uploadStorage\\" + dirName;
		}

		File fileDelete = new File(docRoot + "/" + fileName); // 파일 생성 후
		log.info("삭제할 파일(fileDelete) : " + fileDelete);
		if (fileDelete.exists() && fileDelete.isFile()) {
			result = fileDelete.delete();
		}
		log.info("파일 삭제 여부(true/false) : " + result);

	}
}