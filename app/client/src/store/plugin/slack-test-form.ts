import type { PluginTestForm } from '../types';

export const SLACK_TEST_FORM: PluginTestForm = {
  pluginId: 'slack',
  tabs: [
    {
      tabId: 'channels',
      tabName: '채널 목록 조회',
      description: '워크스페이스의 채널 목록을 조회합니다.',
      controls: [
        {
          controlType: 'INPUT_TEXT',
          label: 'Team ID',
          name: 'teamId',
          placeholder: 'TXXXXXXXXX',
          required: true,
        },
      ],
      api: {
        method: 'GET',
        uri: 'conversations.list',
        bodyTemplate: '{}',
      },
    },
    {
      tabId: 'sendMessage',
      tabName: '메시지 전송',
      description: '지정한 채널에 메시지를 전송합니다.',
      controls: [
        {
          controlType: 'INPUT_TEXT',
          label: 'Team ID',
          name: 'teamId',
          placeholder: 'TXXXXXXXXX',
          required: true,
        },
        {
          controlType: 'INPUT_TEXT',
          label: 'Channel ID',
          name: 'channel',
          placeholder: 'CXXXXXXXXX',
          required: true,
        },
        {
          controlType: 'TEXTAREA',
          label: '메시지',
          name: 'text',
          placeholder: '전송할 메시지',
          required: true,
        },
      ],
      api: {
        method: 'POST',
        uri: 'chat.postMessage',
        bodyTemplate: '{ "channel": "{{channel}}", "text": "{{text}}" }',
      },
    },
  ],
};
