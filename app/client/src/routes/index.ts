import { createRouter, createRootRoute, createRoute } from '@tanstack/react-router';

import { Layout } from '../components/layout';
import { DatasourcesPage } from '../pages/datasources';
import { PluginAuthPage } from '../pages/plugin-auth';
import { WebhookLogsPage } from '../pages/webhook-logs';

const rootRoute = createRootRoute({
  component: Layout,
});

const pluginAuthRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/',
  component: PluginAuthPage,
});

const datasourcesRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/datasources',
  component: DatasourcesPage,
});

const webhookLogsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/webhook-logs',
  component: WebhookLogsPage,
});

const routeTree = rootRoute.addChildren([pluginAuthRoute, datasourcesRoute, webhookLogsRoute]);

export const router = createRouter({ routeTree });

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}
