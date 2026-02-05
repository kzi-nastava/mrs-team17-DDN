export type ChatThreadResponse = {
  id: number;
  userId: number;
  userName: string | null;
  userEmail: string | null;
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
