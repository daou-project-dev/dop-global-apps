package com.daou.dop.global.apps.api.execute;

import com.daou.dop.global.apps.plugin.sdk.ExecuteRequest;
import com.daou.dop.global.apps.plugin.sdk.ExecuteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExecuteController {

    private final PluginExecutorService executorService;

    public ExecuteController(PluginExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * 플러그인 API 실행
     *
     * POST /execute
     * {
     *   "pluginId": "slack",
     *   "action": "chat.postMessage",
     *   "params": {
     *     "channel": "C123",
     *     "text": "Hello",
     *     "externalId": "T123456"
     *   }
     * }
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecuteResponse> execute(@RequestBody ExecuteRequest request) {
        ExecuteResponse response = executorService.execute(request);

        return ResponseEntity
                .status(response.statusCode())
                .body(response);
    }
}
