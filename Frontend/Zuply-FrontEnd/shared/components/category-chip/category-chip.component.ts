import { Component, Input, Output, EventEmitter } from '@angular/core';

const CATEGORY_ICONS: Record<string, string> = {
  'Food & Beverage':      '🍔',
  'Grocery':              '🛒',
  'Fashion & Footwear':   '👗',
  'Home & Kitchen':       '🏠',
  'Electronics':          '📱',
  'Beauty & Personal Care':'💄',
  'Health & Wellness':    '💊',
  'Agriculture':          '🌾',
  'Sports & Fitness':     '🏋️',
  'Books & Stationery':   '📚',
  'Toys & Games':         '🎮',
  'Automotive':           '🚗',
  'Pets':                 '🐾',
  'Art & Craft':          '🎨',
};

@Component({
  selector: 'app-category-chip',
  template: `
    <button class="chip" [class.active]="active" (click)="chipClick.emit(name)">
      <span class="chip-icon">{{ icon }}</span>
      <span class="chip-name">{{ name }}</span>
    </button>
  `,
  styleUrls: ['./category-chip.component.scss']
})
export class CategoryChipComponent {
  @Input() name   = '';
  @Input() active = false;
  @Output() chipClick = new EventEmitter<string>();

  get icon(): string {
    return CATEGORY_ICONS[this.name] || '📦';
  }
}
