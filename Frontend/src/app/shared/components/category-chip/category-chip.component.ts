import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';

const ICON_MAP: Record<string, string> = {
  'Grocery': '🛒', 'Electronics': '📱', 'Clothing': '👗',
  'Fashion & Footwear': '👗', 'Food & Beverage': '🥗',
  'Home & Kitchen': '🏠', 'Beauty & Personal Care': '💄',
  'Health & Wellness': '💊', 'Agriculture': '🌾',
};
const CLASS_MAP: Record<string, string> = {
  'Grocery': 'grocery', 'Electronics': 'electronics',
  'Clothing': 'fashion', 'Fashion & Footwear': 'fashion',
  'Food & Beverage': 'food', 'Home & Kitchen': 'home',
  'Beauty & Personal Care': 'beauty', 'Health & Wellness': 'health',
  'Agriculture': 'agriculture',
};

@Component({
  selector: 'app-category-chip',
  templateUrl: './category-chip.component.html',
  styleUrls: ['./category-chip.component.scss']
})
export class CategoryChipComponent implements OnInit {
  @Input() name   = '';
  @Input() active = false;
  @Output() chipClick = new EventEmitter<string>();

  icon  = '🛒';
  cls   = 'grocery';

  ngOnInit(): void {
    this.icon = ICON_MAP[this.name]  || '🛒';
    this.cls  = CLASS_MAP[this.name] || 'grocery';
  }

  onClick(): void { this.chipClick.emit(this.name); }
}
