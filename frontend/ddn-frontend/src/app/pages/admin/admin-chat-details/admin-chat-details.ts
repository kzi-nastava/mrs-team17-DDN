import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';

import { CHAT_DS } from '../../../api/chat/chat.datasource';
import { ChatMessageResponse } from '../../../api/chat/models/chat.models';

@Component({
  selector: 'app-admin-chat-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-chat-details.html',
  styleUrl: './admin-chat-details.css',
})
export class AdminChatDetails implements OnInit, OnDestroy {
  private ds = inject(CHAT_DS);
  private route = inject(ActivatedRoute);

  threadId = 0;
  messages: ChatMessageResponse[] = [];
  input = '';
  loading = true;

  private pollTimer: any = null;
  private lastId: number | null = null;

  ngOnInit(): void {
    this.threadId = Number(this.route.snapshot.paramMap.get('threadId') ?? '0');
    this.loadInitial();
  }

  ngOnDestroy(): void {
    if (this.pollTimer != null) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }

  send(): void {
    const text = this.input.trim();
    if (!text) return;
    this.input = '';

    this.ds.sendAdminMessage(this.threadId, { content: text }).subscribe({
      next: (msg) => {
        this.messages = [...this.messages, msg];
        this.lastId = msg.id;
        this.scrollToBottomSoon();
      },
    });
  }

  private loadInitial(): void {
    this.loading = true;
    this.ds.getThreadMessages(this.threadId, null, 50).subscribe({
      next: (msgs) => {
        this.messages = msgs ?? [];
        this.lastId = this.messages.length ? this.messages[this.messages.length - 1].id : null;
        this.loading = false;
        this.startPolling();
        this.scrollToBottomSoon();
      },
      error: () => (this.loading = false),
    });
  }

  private startPolling(): void {
    if (this.pollTimer != null) return;

    this.pollTimer = setInterval(() => {
      const after = this.lastId;
      this.ds.getThreadMessages(this.threadId, after, 50).subscribe({
        next: (newMsgs) => {
          if (!newMsgs || newMsgs.length === 0) return;
          this.messages = [...this.messages, ...newMsgs];
          this.lastId = newMsgs[newMsgs.length - 1].id;
          this.scrollToBottomSoon();
        },
      });
    }, 2500);
  }

  private scrollToBottomSoon(): void {
    setTimeout(() => {
      const el = document.getElementById('admin-chat-scroll');
      if (el) el.scrollTop = el.scrollHeight;
    }, 0);
  }
}
