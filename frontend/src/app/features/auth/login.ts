import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';

/** Sign-in page — ports auth/login.jsp (same markup/classes). */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  template: `
    <div class="login-card">
      <h1 class="login-title">Sign in</h1>
      <p class="login-hint">Access your claims workspace</p>

      @if (error()) {
        <div class="alert alert-error">{{ error() }}</div>
      }

      <form class="login-form" [formGroup]="form" (ngSubmit)="submit()">
        <div class="field">
          <label for="username">Username</label>
          <input id="username" class="input" formControlName="username"
                 autocomplete="username" placeholder="e.g. agent" />
        </div>
        <div class="field">
          <label for="password">Password</label>
          <input id="password" type="password" class="input" formControlName="password"
                 autocomplete="current-password" placeholder="Your password" />
        </div>
        <button type="submit" class="btn btn-primary btn-block" [disabled]="submitting()">
          {{ submitting() ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>

      <p class="login-foot">Insurance Claim Management System</p>
    </div>
  `,
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  error = signal<string | null>(null);
  submitting = signal(false);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.error.set('Please enter your username and password.');
      return;
    }
    this.submitting.set(true);
    const { username, password } = this.form.getRawValue();
    this.auth.login(username, password).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.router.navigateByUrl(this.auth.dashboardFor(res.data.user.role));
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(err?.error?.error?.message || 'Invalid username or password');
      },
    });
  }
}
