package com.min.www.dao.member;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.min.www.Exception.IsNotValidId;
import com.min.www.Exception.IsNotValidNickname;
import com.min.www.dto.member.MemberDto;

public interface MemberDao {
	
	
	List<MemberDto> getMemberlist(Map<String,Object> paramMap);
	
	MemberDto getMember(String id);
	
	int insertMember(Map<String,Object> paramMap);
	
	int deleteMember(Map<String,Object> paramMap);

	
	
	int memberNickCheck(Map<String, Object> paramMap);
	
	void insertMemberImage(Map<String, Object> paramMap);
	
	int isInvalidId(Map<String, Object> paramMap);
	
	int isInvalidNickname(Map<String, Object> paramMap);
	
	void deleteAllMember();
	
	int selectMemberCnt();
	
	void memberEdit(Map<String, Object> paramMap);

	MemberDto getMemberUsingNickanme(String nickname);

}
