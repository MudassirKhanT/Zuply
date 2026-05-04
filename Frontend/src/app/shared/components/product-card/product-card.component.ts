import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Product } from '../../../core/models';

const CAT: Record<string, { color: string; bg: string; emoji: string }> = {
  'Grocery':               { color: '#0D5C63', bg: '#E8F5F6', emoji: '🛒' },
  'Electronics':           { color: '#3730A3', bg: '#EEF2FF', emoji: '📱' },
  'Clothing':              { color: '#9D174D', bg: '#FFF0F9', emoji: '👗' },
  'Fashion & Footwear':    { color: '#9D174D', bg: '#FFF0F9', emoji: '👗' },
  'Food & Beverage':       { color: '#C9A84C', bg: '#FFFBEB', emoji: '🥗' },
  'Home & Kitchen':        { color: '#C2410C', bg: '#FFF7ED', emoji: '🏠' },
  'Beauty & Personal Care':{ color: '#7E22CE', bg: '#FDF4FF', emoji: '💄' },
  'Health & Wellness':     { color: '#0F766E', bg: '#F0FDF4', emoji: '💊' },
  'Agriculture':           { color: '#4D7C0F', bg: '#F7FEE7', emoji: '🌾' },
};

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.scss']
})
export class ProductCardComponent implements OnInit {

  @Input() product!: Product;
  @Input() showWishlist = true;
  @Input() wishlisted   = false;

  @Output() addToCartEvent     = new EventEmitter<Product>();
  @Output() wishlistToggleEvt  = new EventEmitter<Product>();
  @Output() cardClickEvt       = new EventEmitter<Product>();

  catColor = '#0D5C63';
  catBg    = '#E8F5F6';
  catEmoji = '🛒';

  ngOnInit(): void {
    const c = CAT[this.product?.category || 'Grocery'] || CAT['Grocery'];
    this.catColor = c.color;
    this.catBg    = c.bg;
    this.catEmoji = c.emoji;
  }

  onCardClick():            void { this.cardClickEvt.emit(this.product); }
  addToCart(e: Event):      void { e.stopPropagation(); this.addToCartEvent.emit(this.product); }
  toggleWishlist(e: Event): void { e.stopPropagation(); this.wishlistToggleEvt.emit(this.product); }
}
