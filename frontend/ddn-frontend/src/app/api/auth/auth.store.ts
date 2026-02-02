import { Injectable } from '@angular/core';

export type AppRole = 'DRIVER' | 'PASSENGER' | 'ADMIN' | string;

type JwtPayload = {
  sub?: string;
  email?: string;
  role?: string;
  driverId?: number | string;
};

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
    const payload = this.getPayload(token);
    return (payload?.role ?? null) as AppRole | null;
  }

  getUserIdFromToken(token: string): number | null {
    const payload = this.getPayload(token);
    const sub = (payload?.sub ?? '').toString();
    const id = Number(sub);
    return Number.isFinite(id) && id > 0 ? id : null;
  }

  getDriverIdFromToken(token: string): number | null {
    const payload = this.getPayload(token);
    const raw = payload?.driverId;
    const id = Number(raw);
    return Number.isFinite(id) && id > 0 ? id : null;
  }

  getCurrentUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;
    return this.getUserIdFromToken(token);
  }

  getCurrentDriverId(): number | null {
    const token = this.getToken();
    if (!token) return null;
    return this.getDriverIdFromToken(token);
  }

  private getPayload(token: string): JwtPayload | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;

      const payloadB64 = parts[1];
      const json = this.base64UrlDecode(payloadB64);
      return JSON.parse(json) as JwtPayload;
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
