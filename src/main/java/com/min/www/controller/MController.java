package com.min.www.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.omg.CORBA.MARSHAL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.min.www.Exception.CreateSessionError;
import com.min.www.Exception.MemberDuplicationException;
import com.min.www.Exception.SessionloginUserInvalid;
import com.min.www.Service.member.MemberService;
import com.min.www.dto.member.MemberDto;
import com.min.www.util.FileUtils;
import com.min.www.util.ParamFactory;

@Controller
public class MController {
	
	@Autowired
	MemberService memberService;
	
	
	
	
	@Resource
	FileUtils fileUtils;
	
	@Autowired
	ParamFactory paramFactory;
	
	
	
	@RequestMapping(value="/member")
	public String member() {
		
		return "memberCheck";
	}
	
	
	@RequestMapping(value="/member/check" , method=RequestMethod.POST)
	public String memberCheck(@RequestParam Map<String, Object> paramMap) {
		System.out.println("ID : " +paramMap.get("id"));
		System.out.println("PASSWORD : " + paramMap.get("password").toString());
		System.out.println("email : " +paramMap.get("email").toString());
	
		
		System.out.println("insert 성공 메세지 : " + 
		memberService.insertMember(paramMap));
	
		System.out.println("성공");
		
		
		
		return "boardList";
	}
	
	@RequestMapping(value="/member/idCheck")
	@ResponseBody
	public Object memberIdCheck(@RequestParam Map<String, Object> paramMap) {
		
		Map<String,Object> reVal = new HashMap<String, Object>();
		System.out.println("중복 확인할 아이디 : " + paramMap.get("id"));
		
		
		int result = memberService.memberIdCheck(paramMap);
		
		System.out.println("ID Check 쿼리 확인 :" + result);
		
		if(result == 0) {
			reVal.put("code", "OK");
		
		} else {
			reVal.put("code", "FAIL");
			
		}
		System.out.println("reVal의 변수는 : " +reVal.get("code"));

		return reVal;
	}
	
	
	@RequestMapping(value="/member/nickCheck")
	@ResponseBody
	public Object memberNickCheck(@RequestParam Map<String, Object> paramMap) {
		
		System.out.println("중복 확인할 닉네임 : " + paramMap.get("nickname"));
		
		Map<String, Object> reVal = new HashMap<>();

		int result = memberService.memberNickCheck(paramMap);
		
		if(result == 0 ) {
			reVal.put("code", "OK");
		} else {
			reVal.put("code", "FAIL");
		}
		
		return reVal;
	}
	
	
	
//	@RequestMapping(value="/member/loginform")
//	public String memberLoginform() {
//		
//		return "loginform";
//	}
	
	@RequestMapping(value="/member/login", method = RequestMethod.POST)
//	@ResponseBody
	public String memberLogin(@RequestParam Map<String, Object> paramMap,  HttpSession session,HttpServletRequest request,
			Model model) {
	
		Map<String, Object> retVal = new HashMap<String, Object>();
		
		//1. sql문에 대입하여 아이디 확인 작업 서비스에서 처리.
		int reVal = memberService.memberLogin(paramMap);
		
		//로그인 했을 때 닉네임으로 뜨게하고싶으면
		//request에 넣어줘야함 가져와서 
		
		
		//2. session 생성해주기. 
		// 0이 아니면 로그인처리 
		if(reVal != 0) {
			session.setAttribute("loginuser", paramMap.get("id"));
			
			MemberDto memberInfo = memberService.getMember((String)paramMap.get("id"));
			// 로그인 객체 세션 생성
			session.setAttribute("memberInfo", memberInfo);
			
			System.out.println("로그인 한사람 "  + memberInfo.getId());
			
			if(session.getAttribute("memberInfo") == null)
				throw new CreateSessionError("세션 생성하는 과정에 오류가 생겼습니다.");
			
			retVal.put("code", "OK");
			
			
			// 세션 이름 = loginuser에 id를 넣어줌.
		
			//redirect로 메인메이피로 넘어가기는 jsp에서 처리.
			System.out.println(paramMap.get("id") + "님이 로그인.");
		
			
		}
		
		model.addAttribute("nickname", request.getAttribute("nickname"));
		model.addAttribute("hi","hi");	
		System.out.println("닉네임은? :" + request.getAttribute("nickname"));
		
	 
		
		return "redirect:/board/list";
	}
	//회원가입 폼
	@RequestMapping(value="/member/memberSignupForm")
	public String memberSignupForm(HttpServletRequest request) {
		System.out.println(" ----- memberSignupForm --------");
		System.out.println(request.getSession().getServletContext().getRealPath("/"));
		
		return "memberSignupForm";
	}
	
	
	// 회원가입 처리해주는 메소드 
	@RequestMapping(value="/member/memberSignupFromInsert")
	public String memberSignupFromInsert(@RequestParam Map<String, Object> paramMap,Model model) {
		// id , nickname , email , password //img 
		System.out.println("----- memberSignupFromInsert -----");

				
		System.out.println("회원 가입할 ID :" +paramMap.get("id"));
		
		
		// insertMember return 값 받는 변수 
		Map<String, Object> insertMemberMap = new HashMap<>();
		
		try {
			insertMemberMap = 
					memberService.insertMember(paramMap);
			
			// insertMember Return 값 받기.
			paramMap.put("isInvalid",insertMemberMap);
			return "redirect:/board/list"; // 추후에 홈으로 수정
		} catch (MemberDuplicationException e) {
			// 중복 값이 있다면 ~ 
			
			paramMap.put("isInvalid",e.getReturnMap());
			model.addAttribute("returnJsp",paramMap);
			return "memberSignupForm";
		}
		
	}
	@RequestMapping(value="/member/logout")
	public String memberLogout(HttpSession session) {
		
		session.invalidate();
		return "redirect:/board/list"; //추후에 홈으로 수정.
	}
	
	@RequestMapping(value="/member/editForm") 
	public String memberEditForm(HttpServletRequest request,Model model) {
		HttpSession session = request.getSession();
		String id = (String)session.getAttribute("loginuser");
//		Map<String, Object> paramMap = new HashMap<>();
////		paramMap.put("id", id);
		// 
		model.addAttribute("member", memberService.getMember(id));
		System.out.println(" ---- /member/edit ----");
		return "memberEditForm";
	}
	
	
	@RequestMapping(value="/member/edit")
	public String memberEdit(HttpServletRequest request) {
		HttpSession session = request.getSession();
		
		String id = (String)session.getAttribute("loginuser");
		
		MemberDto memberDto = 
				(MemberDto)session.getAttribute("memberInfo");
		
		if(id == null || memberDto == null) {
			throw new SessionloginUserInvalid("세션이 만료되었습니다.");
		}
		
		
		
		System.out.println("기존 패스워드 : " + memberDto.getPassword());
		System.out.println("변경된 패스워드 : " + request.getParameter("password"));
		
		System.out.println("크롭 한 이미지 주소  : " +request.getParameter("IMAGEURL"));
		System.out.println("원래 이미지 주소  : " +request.getParameter("ORIGINALIMAGEURL"));
		//아이디,닉네임은 변경불가.
		memberDto.setPassword(request.getParameter("password"));
		memberDto.setEmail(request.getParameter("email"));
		// 64x64는 s_로 시작함
		memberDto.setImageurl(request.getParameter("IMAGEURL"));
		memberDto.setORIGINALIMAGEURL(request.getParameter("ORIGINALIMAGEURL"));
		
		Map<String, Object> paramMap = new HashMap<>();
		
		paramMap = paramFactory.memberDtoFactory(memberDto);
		
		memberService.memberEdit(paramMap);
		return "redirect:/board/list";
		
	}
//	@ResponseBody
//	@RequestMapping(value="/member/memberEdit")
//	public void memberEdit(@RequestMapping Map<String,Object> paramMap) {
//		
//		
//		
//	}
	
	@RequestMapping(value="/member/image/upload")
	@ResponseBody
	public Map<String, Object> memberImageUpload(HttpServletRequest request,MultipartFile file,HttpSession session)  throws Exception{
		
		System.out.println("----- 사용자 이미지 업로드 -----");
		String imageUploadPath = request.getSession().getServletContext().getRealPath("/") + "resources/profileImage/";
		byte[] fileData = file.getBytes();
		String originalName = file.getOriginalFilename();
		System.out.println("originalName :" + originalName);
		
		//session에 저장된 사용자 id값 가져오기.
		String user = (String)session.getAttribute("loginuser");
		
		System.out.println("이미지 업로드 경로위치는 ? : " + imageUploadPath);
		
		Map<String, Object> paramMap = new HashMap<>();
		//파일을 저장하는 Service
		paramMap = 
				memberService.memberImageUpload(request, user,imageUploadPath, originalName, fileData);
		
		
		return paramMap;
		
	}

}
