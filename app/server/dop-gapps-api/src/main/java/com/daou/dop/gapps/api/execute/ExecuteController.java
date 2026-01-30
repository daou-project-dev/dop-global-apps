package com.daou.dop.gapps.api.execute;

import com.daou.dop.gapps.core.dto.ExecuteCommand;
import com.daou.dop.gapps.core.dto.ExecuteResult;
import com.daou.dop.gapps.core.execute.PluginExecutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
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
    public ResponseEntity<ExecuteResult> execute(@RequestBody ExecuteCommand command) {
        ExecuteResult result = executorService.execute(command);

        return ResponseEntity
                .status(result.statusCode())
                .body(result);
    }
}
