import { Component } from '@angular/core';

@Component({
  selector: 'app-ai-badge',
  template: `<span class="ai-badge-pill">✦ AI</span>`,
  styles: [`
    .ai-badge-pill {
      display: inline-block;
      background: linear-gradient(135deg, #0D5C63, #C9A84C);
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      padding: 2px 8px;
      border-radius: 999px;
      letter-spacing: .04em;
    }
  `]
})
export class AiBadgeComponent {}
