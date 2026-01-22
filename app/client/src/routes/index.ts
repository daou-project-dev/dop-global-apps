import {
  createRouter,
  createRootRoute,
  createRoute,
} from '@tanstack/react-router';

import { Layout } from '../components/layout';
import { PluginAuthPage } from '../pages/plugin-auth';
import { DatasourcesPage } from '../pages/datasources';
import { MicrosoftTestPage } from '../pages/microsoft-test';

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
