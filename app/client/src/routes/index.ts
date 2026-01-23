import { createRouter, createRootRoute, createRoute } from '@tanstack/react-router';

import { Layout } from '../components/layout';
import { DatasourcesPage } from '../pages/datasources';
import { MicrosoftTestPage } from '../pages/microsoft-test';
import { PluginAuthPage } from '../pages/plugin-auth';

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

const microsoftTestRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/microsoft-test',
  component: MicrosoftTestPage,
});

const routeTree = rootRoute.addChildren([pluginAuthRoute, datasourcesRoute, microsoftTestRoute]);

export const router = createRouter({ routeTree });

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}
