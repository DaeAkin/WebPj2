package com.min.www.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
//import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.min.www.Service.BoardService;
import com.min.www.Service.member.MemberService;
import com.min.www.dao.BoardDao;
import com.min.www.dto.BoardAndAlertJoinDto;
import com.min.www.dto.BoardDto;
import com.min.www.dto.BoardReplyDto;
import com.min.www.dto.member.MemberDto;
import com.min.www.util.FileUtils;
import com.min.www.util.TimeUtil;

/*
 * 

 * 
 * 4. 회원가입이나 	 수정 시 자바스크립트로 유효성 검사하기. 
 */

@Controller
public class BoardController {

	@Autowired
	BoardService boardService;
	
	@Autowired
	BoardDao boardDao;
	
	@Autowired
	MemberService memberService;
	

	@Autowired
	private TimeUtil timeUtil;

	@Autowired
	private FileUtils fileutils;

	public void setTimeUtil(TimeUtil timeUtil) {
		this.timeUtil = timeUtil;
	}

	public void setFileutils(FileUtils fileutils) {
		this.fileutils = fileutils;
	}

	String uploadPath = "/Users/donghyeonmin/git/WebPj2/src/main/webapp/resources/imageupload/";
	
	private Map<String, Object> getAlerts() {
		//리턴해줄 맵
		Map<String, Object> returnMap = new HashMap<>();
		
		//https://docs.spring.io/spring/docs/5.0.1.RELEASE/javadoc-api/index.html?org/springframework/web/context/request/RequestContextHolder.html
		// session들은 톰캣에 저장되어 분류되는가? 저장되있는 session 파일들이 존재하는 것인가? 
		//getRequestAttributes들은 어디서 가져오는거지? request가 들어올때마다 파일이 지정되있나.. 
		HttpServletRequest request = 
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		
		HttpSession session = request.getSession();
		

		
		//test code
		String writer = (String)session.getAttribute("loginuser");
//		System.out.println("getAlerts : " + writer);
		
		return returnMap;
	}

	

	@RequestMapping(value = "/board/write")
	public String boardwrite() {

		return "boardwrite";
	}

	// 게시글 리스트 조회.
	@RequestMapping(value = "/board/list")
	public String boardList(@RequestParam Map<String, Object> paramMap, Model model,HttpSession session) {
		// select * from freeboard where title like '%test%' or content like '%test%';
		
		String writer = (String)session.getAttribute("loginuser");
		memberService.getMember(writer);
		getAlerts();
		
		System.out.println("로그인한 유저:" + writer);
		// 로그인 상태일 경우 코드 시작
		if(session.getAttribute("memberInfo") != null) {
		MemberDto memberDto = (MemberDto)session.getAttribute("memberInfo");
		// 로그인한 유저의 닉네임 대입해서 알람 가져오기 
		List<BoardAndAlertJoinDto> BoardAndAlertJoinDtos =
				boardDao.getAlerts(memberDto.getNickname());
		
		int totalAlert = BoardAndAlertJoinDtos.size();
		
		
		System.out.println("총 알람 갯수 : " +totalAlert);
		
		model.addAttribute("totalAlert",totalAlert);
		
		model.addAttribute("boardAndAlertJoinDtos",BoardAndAlertJoinDtos);
		} // 로그인 상태일 경우 코드 끝
		int totalCnt;

		String searchContent = (String) paramMap.get("searchArea");
		System.out.println("검색할 내용 :" + searchContent);
		// 조회 하려는 페이지
		int startPage = (paramMap.get("startPage") != null ? Integer.parseInt(paramMap.get("startPage").toString())
				: 1);
		// 한페이지에 보여줄 리스트 수
		int visiblePages = (paramMap.get("visiblePages") != null
				? Integer.parseInt(paramMap.get("visiblePages").toString())
				: 30);

		/*
		 * 검색할 경우와 전체 게시물을 가져올 경우 2가지.
		 */
		if (searchContent == null) { // 검색 안하는 경우.
			totalCnt = boardService.getContentCnt(paramMap);
			System.out.println("totalCnt : " + totalCnt);
		} else {

			totalCnt = boardService.searchedContentCnt(paramMap);

		}

		System.out.println("게시물 갯수  :" + totalCnt);
		System.out.println("시작 페이지  :" + startPage);
		System.out.println("한 페이지에 보여줄 리스트  : " + visiblePages);

		BigDecimal decima1 = new BigDecimal(totalCnt);
		BigDecimal decima2 = new BigDecimal(visiblePages);
		BigDecimal totalPage = decima1.divide(decima2, 0, BigDecimal.ROUND_UP);

		int startLimitPage = 0;
		// 2. mysql limit 범위를 구하기 위해 계산
		if (startPage == 1) {
			startLimitPage = 0;
		} else {
			startLimitPage = (startPage - 1) * visiblePages;
		}

		paramMap.put("start", startLimitPage);
		paramMap.put("end", visiblePages);

		// jsp 에서 보여줄 정보 추출
		model.addAttribute("startPage", startPage + ""); // 현재 페이지
		model.addAttribute("totalCnt", totalCnt); // 전체 게시물수
		model.addAttribute("totalPage", totalPage); // 페이지 네비게이션에 보여줄 리스트 수
		if (searchContent == null) {
			model.addAttribute("boardList", boardService.getContentList(paramMap)); // 전체 게시물 조회
		} else {
			model.addAttribute("boardList", boardService.searchedContentCntList(paramMap));
		}

		return "boardList";

	}

	// 게시글 테스트
	@RequestMapping(value = "/board/listTest")
	public String boardListTest() {
		return "boardListTest";
	}

	// 게시글 상세 보기
	@RequestMapping(value = "/board/view")
	public String boardView(@RequestParam Map<String, Object> paramMap, Model model) {

		model.addAttribute("replyList", boardService.getReplyList(paramMap));
		
		List<BoardReplyDto> replyCount = boardService.getReplyList(paramMap);
		
		model.addAttribute("replyCount",replyCount.size());
		BoardDto boardDto = boardService.getContentView(paramMap);
		model.addAttribute("boardView",boardDto);
//		System.out.println("boardDto.getWriter();" + boardDto.getWriter());
		MemberDto memberDto = memberService.getMemberUsingNickanme(boardDto.getWriter());
//		System.out.println("memberDto의 imageUrl :" +memberDto.getImageurl());
		model.addAttribute("member",memberDto);
		return "boardView";
	}

	// 게시글 등록 및 수정
	@RequestMapping(value = "/board/edit")
	public String boardEdit(HttpServletRequest request, @RequestParam Map<String, Object> paramMap, Model model) {
		System.out.println("게시글 등록 및 수정 ()");
		
		// Referer 검사
		String Referer = request.getHeader("referer");

		if (Referer != null) { // URL로 직접 접근 불가
			if (paramMap.get("id") != null) {// 게시글 수정
				if (Referer.indexOf("/board/view") > -1) {

					// 정보를 가져온다.
					model.addAttribute("boardView", boardService.getContentView(paramMap));
					return "boardEdit";
				} else {
					return "/board/list";
				}
			} else { // 게시글 등록
				if (Referer.indexOf("/board/list") > -1) {
					return "boardEdit";

				} else {
					return "/board/list";
				}

			}

		} else {
			return "/board/list";
		}

	}

	/*
	 * AJAX호출 (게시글 등록) and 파일 업로드 구현.
	 */
	@RequestMapping(value = "/board/save", method = RequestMethod.POST)
	public String boardSave(@RequestParam Map<String, Object> paramMap, HttpServletRequest request) throws Exception {

		// 리턴 값
		/*
		 * //패스워드 암호화 ShaPasswordEncoder encoder = new ShaPasswordEncoder(256); String
		 * password = encoder.encodePassword(paramMap.get("password").toString(), null);
		 * paramMap.put("password", password);
		 */

		MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
		/*
		 * Iterator는 자바의 컬렉션 프레임웍에서 컬렉션에 저장되어 있는 요소들을 읽어오는 방법을 표준화 하여쓴ㄴ데 그중 하나가
		 * Iterator이다.
		 */
		Iterator<String> iterator = multipartHttpServletRequest.getFileNames();
		MultipartFile multipartFile = null;
		while (iterator.hasNext()) {
			multipartFile = multipartHttpServletRequest.getFile(iterator.next());
			System.out.println("업로드된 파일 갯수 : " + iterator.toString());
			if (multipartFile.isEmpty() == false) {
				System.out.println("----------------file start------------");
				System.out.println("name : " + multipartFile.getName());
				System.out.println("filename : " + multipartFile.getOriginalFilename());
				System.out.println("size : " + multipartFile.getSize());
				System.out.println("----------------file end------------");
			}
		}

		// 정보입력
		boardService.regContent(paramMap, request);

		return "redirect:/board/list";
	}

	// 원래 업로드컨트롤러.
	/*
	 * @ResponseBody
	 * 
	 * @RequestMapping(value="/board/upload", method=RequestMethod.POST ) public
	 * String uploadAjax(HttpServletRequest request) throws Exception {
	 * MultipartHttpServletRequest multipartHttpServletRequest =
	 * (MultipartHttpServletRequest)request; System.out.println("업로드 시작. ");
	 * System.out.println("업로드된 파일 이름 : " +
	 * multipartHttpServletRequest.getFileNames().toString()); Iterator<String>
	 * iterator = multipartHttpServletRequest.getFileNames();
	 * System.out.println("iterator.hasNext() :" + iterator.hasNext());
	 * MultipartFile multipartFile = null; while(iterator.hasNext()) { multipartFile
	 * = multipartHttpServletRequest.getFile(iterator.next());
	 * System.out.println("업로드된 파일 갯수 : " + iterator.toString());
	 * if(multipartFile.isEmpty() == false) {
	 * System.out.println("----------------file start------------");
	 * System.out.println("name : " + multipartFile.getName());
	 * System.out.println("filename : " + multipartFile.getOriginalFilename());
	 * System.out.println("size : " + multipartFile.getSize());
	 * System.out.println("----------------file end------------"); } }
	 * fileutils.parseInsertFileInfoAjax(multipartHttpServletRequest); return "aa";
	 * 
	 * }
	 */

	// 아작스 업로드 처리매핑
	@ResponseBody // view가아닌 data 리턴
	@RequestMapping(value = "/board/upload", method = RequestMethod.POST)
	public Map<String, Object> uploadAjax(MultipartFile file) throws Exception {

		System.out.println("size : " + file.getSize());
		System.out.println("contentType :" + file.getContentType());
		System.out.println("uploadPath : " + uploadPath);
		// originalFile = 원본 파일
		// boardInsertImage = 리사이징 or Width가 1200이하일 땐 원본 파일
		// makeThumbnail 썸네일 만드는

		Map<String, Object> testMap = new HashMap<>();

		testMap = FileUtils.uploadFile(uploadPath, file.getOriginalFilename(), file.getBytes());
		System.out.println("testMap.get(boardInsertImage) :" + testMap.get("boardInsertImage"));
		System.out.println("testMap.get(makeThumnail) : " + testMap.get("makeThumbnail"));

		// String jsonData = new ObjectMapper().writeValueAsString(testMap);
		//
		// return jsonData;

		return testMap;

		// return new
		// ResponseEntity<Map<String,Object>>(FileUtils.uploadFile(uploadPath,
		// file.getOriginalFilename(), file.getBytes()), HttpStatus.OK);
	}

	// 이미지 표시 매핑.
	@ResponseBody // view가 아닌 data 리턴
	@RequestMapping("/board/displayFile")
	public ResponseEntity<byte[]> displayFile(String fileName) throws Exception {

		System.out.println("*********************** displayFile 메소드 시작 *********************");
		System.out.println("*********************** displayFile 메소드 시작 *********************");
		System.out.println("*********************** displayFile 메소드 시작 *********************");
		System.out.println("*********************** displayFile 메소드 시작 *********************");
		// 서버의 파일을 다운로드하기 위한 스트림
		InputStream in = null; // java.io
		ResponseEntity<byte[]> entity = null;

		try {
			// 확장자를 추출하여 formatName에 저장.
			String formatName = fileName.substring(fileName.lastIndexOf(".") + 1);
			// 추출한 확장자를 MediaUtil 클래스나 다른 클래스를 만들어서 이미지파일여부를 검사하고 리턴하기.
			// MediaType mType = MediaUtils.getMediaType(formatName);
			//

			// 헤더 구성 객체(외부에서 데이터를 주고받을 때에는 header와 body를 구성해야하기 때문에 )
			HttpHeaders headers = new HttpHeaders();
			// InputStream 생성
			System.out.println("InputStream 할 경로 : " + uploadPath + fileName);
			in = new FileInputStream(uploadPath + fileName);

			// 이미지 파일이면 if 쓰고

			// else 아니면 ( 일단은 이미지파일이라고 생각하고 코딩 )
			// headers.setContentType(mType);
			entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.OK);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			// HTTP 상태 코드()
			entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
		} finally {
			in.close(); // 스트림 닫기.
		}

		return entity;
	}

	// AJAX 호출 ( 댓글 등록)
	@RequestMapping(value = "/board/reply/save", method = RequestMethod.POST)
	@ResponseBody
	public Object boardReplySave(@RequestParam Map<String, Object> paramMap, HttpServletRequest request) {
		System.out.println("댓글 AJAX 호출()");
		HttpSession session = request.getSession();
	
		String id = (String) session.getAttribute("loginuser");
		
		MemberDto replyUser = memberService.getMember(id);
		
		String nickname = replyUser.getNickname();
		
		
		paramMap.put("reply_writer", nickname);
		System.out.println("댓글단 사람 닉네임은 :" +nickname);
		// 리턴 값
		Map<String, Object> retVal = new HashMap<String, Object>();

		// 패스워드 암호화
		// ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
		// String password =
		// encoder.encodePassword(paramMap.get("reply_password").toString(), null);
		// paramMap.put("reply_password", password);

		// 정보 입력

		boardService.regReply(paramMap);
		// 방금 등록한 board_id랑 reply_id 가져와야함 .
		// 알림 추가
		
//		int result = boardService.regReply(paramMap);

//		if (result > 0) {
//			retVal.put("code", "OK");
//			retVal.put("reply_id", paramMap.get("reply_id"));
//			retVal.put("message", "등록에 성공 하였습니다.");
//		} else {
//			retVal.put("code", "FAIL");
//			retVal.put("message", "등록에 실패 하였습니다.");
//		}

		return retVal;
	}

	// AJAX 호출 ( 댓글 삭제 )
	@RequestMapping(value = "/board/reply/del", method = RequestMethod.POST)
	@ResponseBody
	public Object boardReplyDel(@RequestParam Map<String, Object> paramMap) {

		// 리턴값
		Map<String, Object> retVal = new HashMap<String, Object>();

		// 패스워드 암호화
		ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
		String password = encoder.encodePassword(paramMap.get("reply_password").toString(), null);
		paramMap.put("reply_password", password);

		// 정보입력
		int result = boardService.delReply(paramMap);

		if (result > 0) {
			retVal.put("code", "OK");
		} else {
			retVal.put("code", "FAIL");
			retVal.put("message", "삭제에 실패했습니다. 패스워드를 확인해주세요.");
		}

		return retVal;
	}

	@RequestMapping(value = "/socketTest")
	public String socketTest() {

		return "socketTest";
	}
	
	@RequestMapping(value = "/testDto")
	public String testDto() {
		
		return "testDto";
	}
	
	// 이미지 업로드 팝업창 띄우는 메소드 
	@RequestMapping(value ="/imageUploadView")
	public String imageUploadView() {
		return "imageUploadView";
	}
	
	

}
