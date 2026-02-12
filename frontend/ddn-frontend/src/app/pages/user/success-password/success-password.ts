import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-success-password',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './success-password.html',
  styleUrls: ['./success-password.css'],
})
export class SuccessPassword {}
