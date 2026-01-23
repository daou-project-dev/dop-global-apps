package com.daou.dop.global.apps.api.connection.controller;

import com.daou.dop.global.apps.api.connection.dto.ConnectionResponse;
import com.daou.dop.global.apps.api.connection.dto.CreateConnectionRequest;
import com.daou.dop.global.apps.core.connection.ConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Connection API
 * - GET /api/connections: 활성 연동 목록 조회
 * - POST /api/connections: 간단한 연동 생성
 */
@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * 활성 연동 목록 조회
     *
     * @return 연동 목록
     */
    @GetMapping
    public ResponseEntity<List<ConnectionResponse>> getConnections() {
        // TODO: 인증 구현 후 companyId, userId를 요청에서 추출
        List<ConnectionResponse> connections = connectionService.getActiveConnections(null, null)
                .stream()
                .map(ConnectionResponse::from)
                .toList();
        return ResponseEntity.ok(connections);
    }

    /**
     * 간단한 연동 생성 (Service Account, Local ADC 등)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createConnection(@RequestBody CreateConnectionRequest request) {
        Long connectionId = connectionService.createSimpleConnection(
                request.pluginId(),
                request.externalId(),
                request.externalName()
        );
        return ResponseEntity.ok(Map.of(
                "success", true,
                "connectionId", connectionId
        ));
    }
}
