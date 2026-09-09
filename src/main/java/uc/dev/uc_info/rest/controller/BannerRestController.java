package uc.dev.uc_info.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uc.dev.uc_info.model.Banner;
import uc.dev.uc_info.rest.dto.BannerResponse;
import uc.dev.uc_info.service.BannerService;

import java.util.List;

/**
 * 배너 학생 앱 REST 컨트롤러. 관리자 웹의 {@link BannerService}를 그대로
 * 재사용하고, 응답만 {@link BannerResponse}로 변환해서 내보낸다.
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerRestController {

    private final BannerService bannerService;

    /**
     * 학생 앱 메인 화면에 노출할 활성 배너 목록을 조회한다. 인증된 학생의
     * 소속 학과 기준으로 스코프된다(공지 미연결/전체 대상/본인 학과
     * 배너만).
     *
     * @return 200 + 배너 목록
     */
    @GetMapping
    public ResponseEntity<List<BannerResponse>> list() {
        String studentId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<BannerResponse> banners = bannerService.findActiveForStudent(studentId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(banners);
    }

    /**
     * Banner 엔티티를 응답 DTO로 변환한다.
     *
     * @param banner 변환할 배너
     * @return 변환된 응답 DTO
     */
    private BannerResponse toResponse(Banner banner) {
        BannerResponse response = new BannerResponse();

        response.setId(banner.getBannerId());
        response.setTitle(banner.getTitle());
        response.setSubtitle(banner.getSubtitle());
        response.setStatus(banner.getStatus());

        return response;
    }
}