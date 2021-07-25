package com.ssafy.square4us.api.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.square4us.api.mvc.model.entity.Member;
import com.ssafy.square4us.api.mvc.service.MemberService;
import com.ssafy.square4us.api.request.MemberJoinPostReq;
import com.ssafy.square4us.api.response.BasicResponseBody;
import com.ssafy.square4us.api.response.MemberInfoGetRes;
import com.ssafy.square4us.common.auth.MemberDetails;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Api(value = "멤버 API", tags = "Member")
@RestController
@RequestMapping(value = "/member")
public class MemberController {
	@Autowired MemberService memberService;
	
	@PostMapping("/join")
	@ApiOperation(value = "회원 가입", notes = "<strong>아이디와 패스워드</strong>를 통해 회원가입 한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<? extends BasicResponseBody> register(
			@RequestBody @ApiParam(value="회원가입 정보", required = true) MemberJoinPostReq joinInfo) {
		
		Member confirmMember = memberService.getMemberByEmail(joinInfo.getEmail());
		if(confirmMember!=null) {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(BasicResponseBody.of(406, "이미 존재하는 id입니다"));
		}
		
		//임의로 리턴된 Member 인스턴스. 현재 코드는 회원 가입 성공 여부만 판단하기 때문에 굳이 Insert 된 유저 정보를 응답하지 않음.
		Member Member = memberService.createMember(joinInfo);
		
		return ResponseEntity.status(200).body(BasicResponseBody.of(200, "Success"));
	}
	
	@GetMapping("/me")
	@ApiOperation(value = "회원 본인 정보 조회", notes = "로그인한 회원 본인의 정보를 응답한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<MemberInfoGetRes> getUserInfo(@ApiIgnore Authentication authentication) {
		/**
		 * 요청 헤더 액세스 토큰이 포함된 경우에만 실행되는 인증 처리이후, 리턴되는 인증 정보 객체(authentication) 통해서 요청한 유저 식별.
		 * 액세스 토큰이 없이 요청하는 경우, 403 에러({"error": "Forbidden", "message": "Access Denied"}) 발생.
		 */
		if(authentication==null) System.out.println("NULL");
		System.out.println(authentication.toString());
		MemberDetails userDetails = (MemberDetails)authentication.getDetails();
		System.out.println(userDetails.toString());
		String userId = userDetails.getUsername();
		Member user = memberService.getMemberByEmail(userId);
		
		return ResponseEntity.status(200).body(MemberInfoGetRes.of(user));
	}
}
