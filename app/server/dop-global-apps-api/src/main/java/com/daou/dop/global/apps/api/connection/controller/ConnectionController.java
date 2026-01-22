package com.daou.dop.global.apps.api.connection.controller;

import com.daou.dop.global.apps.api.connection.dto.ConnectionResponse;
import com.daou.dop.global.apps.core.connection.ConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Connection API
 * - GET /api/connections: 활성 연동 목록 조회
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
}
