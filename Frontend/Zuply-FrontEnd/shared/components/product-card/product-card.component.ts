import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Product } from '../../../core/models';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.scss']
})
export class ProductCardComponent {
  @Input() product!: Product;
  @Input() adding = false;
  @Output() addToCartEvent = new EventEmitter<Product>();
  @Output() cardClickEvt   = new EventEmitter<Product>();
}
