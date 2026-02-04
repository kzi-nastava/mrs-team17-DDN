export interface Notification {
  id: number;
  type: string;
  title: string;
  message: string;
  linkUrl: string;
  createdAt: string;
  readAt: string | null;
}
