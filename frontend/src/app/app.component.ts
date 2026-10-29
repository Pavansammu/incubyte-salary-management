import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatButtonModule, MatIconModule],
  template: `
    <mat-toolbar color="primary" class="toolbar">
      <mat-icon aria-hidden="true">payments</mat-icon>
      <span class="brand">ACME Salary Management</span>
      <span class="spacer"></span>
      <nav aria-label="Main">
        <a mat-button routerLink="/employees" routerLinkActive="active">
          <mat-icon>people</mat-icon> Employees
        </a>
        <a mat-button routerLink="/insights" routerLinkActive="active">
          <mat-icon>insights</mat-icon> Pay insights
        </a>
      </nav>
    </mat-toolbar>
    <main class="content">
      <router-outlet />
    </main>
  `,
  styleUrl: './app.component.scss',
})
export class AppComponent {}
