package com.daou.dop.global.apps.core.execute;

import com.daou.dop.global.apps.core.execute.dto.ExecuteRequest;
import com.daou.dop.global.apps.core.execute.dto.ExecuteResponse;
import org.pf4j.ExtensionPoint;

/**
 * 플러그인 API 실행을 위한 ExtensionPoint
 */
public interface PluginExecutor extends ExtensionPoint {

    /**
     * 플러그인 이름 (요청의 plugin 필드와 매칭)
     */
    String getPluginName();

    /**
     * API 실행
     *
     * @param request 실행 요청
     * @return 실행 결과
     */
    ExecuteResponse execute(ExecuteRequest request);
}
