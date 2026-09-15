package com.apisentinel.api;

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
@RequestMapping("/api/v1/apis")
public class ApiController {

    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApiConfigurationDto>> create(@Valid @RequestBody CreateApiRequest request) {
        ApiConfigurationDto dto = apiService.createApi(request);
        return new ResponseEntity<>(ApiResponse.ok(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page != null && size != null) {
            int boundedSize = Math.min(Math.max(1, size), 100);
            PageRequest pageRequest = PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            PagedResponse<ApiConfigurationDto> paged = apiService.getUserApisPaged(pageRequest);
            return ResponseEntity.ok(ApiResponse.ok(paged));
        }

        List<ApiConfigurationDto> list = apiService.getUserApis();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiConfigurationDto>> getById(@PathVariable UUID id) {
        ApiConfigurationDto dto = apiService.getApiById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiConfigurationDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApiRequest request) {
        ApiConfigurationDto dto = apiService.updateApi(id, request);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        apiService.deleteApi(id);
        return ResponseEntity.ok(ApiResponse.empty());
    }
}
