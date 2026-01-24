import { Injectable } from '@angular/core';

export type AppRole = 'DRIVER' | 'PASSENGER' | 'ADMIN' | string;

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly KEY = 'ddn_token';

  setToken(token: string): void {
    localStorage.setItem(this.KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.KEY);
  }

  clear(): void {
    localStorage.removeItem(this.KEY);
  }
  logout(): void {
  this.clear();
  }


  getRoleFromToken(token: string): AppRole | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;

      const payload = parts[1];
      const json = this.base64UrlDecode(payload);
      const obj = JSON.parse(json);

      return (obj?.role ?? null) as AppRole | null;
    } catch {
      return null;
    }
  }

  private base64UrlDecode(input: string): string {
    let s = input.replace(/-/g, '+').replace(/_/g, '/');
    const pad = s.length % 4;
    if (pad) s += '='.repeat(4 - pad);

    return decodeURIComponent(
      Array.prototype.map
        .call(atob(s), (c: string) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
  }
}
