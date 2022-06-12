package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // 문제점: 엔티티에 프레젠테이션 계층을 위한 @NotEmpty등의 로직이 추가된다.
    // 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
    // DTO로 전환한다


    // 회원등록 v1:
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) { // @Valid가 선언되어야 Member에 대한 유효성검사 진행
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 회원등록 v2: v1에서 요청을 Dto로 변경
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id); // 요청으로 들어온 id 정보를 응답 dto를 통해 리턴한다
    }

    // 회원 수정 v2
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());

        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName()); // 응답 dto에 값을 담아 리턴한다. json으로 뿌려진다.
    }

    // 회원 조회 v1
    // 모든 엔티티가 응답으로 노출된다. @JsonIgnore 어노테이션으로 엔티티 클래스에서 무시할 목록이나 속성을 직접 지정해주는 것은 최악의 방법이다. api가 이거 하나가 아니다.
    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
    }

    // 회원 조회 v2: 별도의 DTO로 응답 분리
    @GetMapping("/api/v2/members")
    public Result membersV2(){
        List<Member> findMembers = memberService.findMembers();

        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect); //  어떤 형태가 될지 모르는 Result를 지네릭으로 정의해줌
    }

    // 회원 등록 DTO
    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse{
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    // 회원 수정 DTO
    @Data
    static class UpdateMemberRequest {
        String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }

    // 회원 조회 DTO
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }
}
