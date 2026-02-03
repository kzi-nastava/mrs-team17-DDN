import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { UserNavbarComponent } from '../../../components/user-navbar/user-navbar';
import { CHAT_DS } from '../../../api/chat/chat.datasource';
import { ChatMessageResponse } from '../../../api/chat/models/chat.models';

@Component({
  selector: 'app-user-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, UserNavbarComponent],
  templateUrl: './user-chat.html',
  styleUrl: './user-chat.css',
})
export class UserChat implements OnInit, OnDestroy {
  private ds = inject(CHAT_DS);

  messages: ChatMessageResponse[] = [];
  input = '';
  loading = true;

  private pollTimer: any = null;
  private lastId: number | null = null;
  private sub: Subscription | null = null;

  ngOnInit(): void {
    // ensure thread exists + initial load
    this.sub = this.ds.getMyThread().subscribe({
      next: () => {
        this.ds.getMyMessages(null, 50).subscribe({
          next: (msgs) => {
            this.messages = msgs;
            this.lastId = msgs.length ? msgs[msgs.length - 1].id : null;
            this.loading = false;
            this.startPolling();
            this.scrollToBottomSoon();
          },
          error: () => (this.loading = false),
        });
      },
      error: () => (this.loading = false),
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.sub = null;

    if (this.pollTimer != null) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }

  send(): void {
    const text = this.input.trim();
    if (!text) return;

    this.input = '';

    this.ds.sendMyMessage({ content: text }).subscribe({
      next: (msg) => {
        this.messages = [...this.messages, msg];
        this.lastId = msg.id;
        this.scrollToBottomSoon();
      },
      error: () => {
        // ako želiš: vrati input ili pokaži toast
      },
    });
  }

  private startPolling(): void {
    if (this.pollTimer != null) return;

    this.pollTimer = setInterval(() => {
      const after = this.lastId;
      this.ds.getMyMessages(after, 50).subscribe({
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
      const el = document.getElementById('chat-scroll');
      if (el) el.scrollTop = el.scrollHeight;
    }, 0);
  }

  isMine(m: ChatMessageResponse): boolean {
    return m.senderRole !== 'ADMIN';
  }
}
