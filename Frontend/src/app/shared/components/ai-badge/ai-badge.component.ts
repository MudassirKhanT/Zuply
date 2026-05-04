import { Component } from '@angular/core';
@Component({
  selector: 'app-ai-badge',
  template: '<span class="ai-badge">✦ AI Generated</span>',
  styles: ['.ai-badge { display:inline-flex; align-items:center; gap:3px; background:#E8F5F6; border:1px solid rgba(201,168,76,0.3); color:#0D5C63; font-size:9px; font-weight:600; padding:2px 8px; border-radius:999px; }']
})
export class AiBadgeComponent {}