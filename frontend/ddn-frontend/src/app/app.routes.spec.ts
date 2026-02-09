import { Route } from '@angular/router';

import { routes } from './app.routes';
import { CHAT_DS } from './api/chat/chat.datasource';
import { ChatHttpDataSource } from './api/chat/chat.http.datasource';

function findChildRoute(parentPath: string, childPath: string): Route | undefined {
  const parent = routes.find((route) => route.path === parentPath);
  return parent?.children?.find((route) => route.path === childPath);
}

describe('app routes', () => {
  it('should register driver support route', () => {
    const route = findChildRoute('driver', 'support');
    expect(route).toBeTruthy();
    expect(typeof route?.loadComponent).toBe('function');
  });

  it('should use chat datasource provider for driver support', () => {
    const route = findChildRoute('driver', 'support');
    const providers = (route?.providers ?? []) as Array<{ provide?: unknown; useClass?: unknown }>;
    const chatProvider = providers.find((provider) => provider.provide === CHAT_DS);

    expect(chatProvider).toBeTruthy();
    expect(chatProvider?.useClass).toBe(ChatHttpDataSource);
  });
});
