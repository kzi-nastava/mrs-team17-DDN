export type ChatThreadResponse = {
  id: number;
  userId: number;
  lastMessageAt: string | null;
};

export type ChatMessageResponse = {
  id: number;
  senderRole: 'PASSENGER' | 'DRIVER' | 'ADMIN';
  content: string;
  sentAt: string;
};

export type ChatSendMessageRequest = {
  content: string;
};
