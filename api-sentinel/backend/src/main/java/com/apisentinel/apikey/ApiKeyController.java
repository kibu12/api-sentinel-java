package com.apisentinel.apikey;

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
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateApiKeyResponse>> create(@Valid @RequestBody CreateApiKeyRequest request) {
        CreateApiKeyResponse response = apiKeyService.createApiKey(request);
        return new ResponseEntity<>(ApiResponse.ok(response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page != null && size != null) {
            int boundedSize = Math.min(Math.max(1, size), 100);
            PageRequest pageRequest = PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            PagedResponse<ApiKeyDto> paged = apiKeyService.getUserApiKeysPaged(pageRequest);
            return ResponseEntity.ok(ApiResponse.ok(paged));
        }

        List<ApiKeyDto> list = apiKeyService.getUserApiKeys();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable UUID id) {
        apiKeyService.revokeApiKey(id);
        return ResponseEntity.ok(ApiResponse.empty());
    }
}
