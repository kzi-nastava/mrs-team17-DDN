import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { CHAT_DS } from '../../../api/chat/chat.datasource';
import { ChatThreadResponse } from '../../../api/chat/models/chat.models';

@Component({
  selector: 'app-admin-chats',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-chats.html',
  styleUrl: './admin-chats.css',
})
export class AdminChats implements OnInit {
  private ds = inject(CHAT_DS);

  query = '';
  threads: ChatThreadResponse[] = [];
  loading = true;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.ds.listThreads(this.query, 50).subscribe({
      next: (res) => {
        this.threads = res ?? [];
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }
}
