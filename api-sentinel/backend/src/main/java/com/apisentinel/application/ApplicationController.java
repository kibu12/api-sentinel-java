package com.apisentinel.application;

import com.apisentinel.common.ApiResponse;
import com.apisentinel.common.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationDto>> create(@Valid @RequestBody CreateApplicationRequest request) {
        ApplicationDto dto = applicationService.createApplication(request);
        return new ResponseEntity<>(ApiResponse.ok(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page != null && size != null) {
            int boundedSize = Math.min(Math.max(1, size), 100);
            PageRequest pageRequest = PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            PagedResponse<ApplicationDto> paged = applicationService.getUserApplicationsPaged(pageRequest);
            return ResponseEntity.ok(ApiResponse.ok(paged));
        }

        List<ApplicationDto> list = applicationService.getUserApplications();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDto>> getById(@PathVariable UUID id) {
        ApplicationDto dto = applicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationRequest request) {
        ApplicationDto dto = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.ok(ApiResponse.empty());
    }
}
