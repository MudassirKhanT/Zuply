import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { NavbarComponent }         from './components/navbar/navbar.component';
import { FooterComponent }         from './components/footer/footer.component';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner.component';
import { ProductCardComponent }    from './components/product-card/product-card.component';
import { CategoryChipComponent }   from './components/category-chip/category-chip.component';
import { AiBadgeComponent }        from './components/ai-badge/ai-badge.component';

@NgModule({
  declarations: [
    NavbarComponent,
    FooterComponent,
    LoadingSpinnerComponent,
    ProductCardComponent,
    CategoryChipComponent,
    AiBadgeComponent,
  ],
  imports: [CommonModule, RouterModule],
  exports: [
    NavbarComponent,
    FooterComponent,
    LoadingSpinnerComponent,
    ProductCardComponent,
    CategoryChipComponent,
    AiBadgeComponent,
    CommonModule,
    RouterModule,
  ]
})
export class SharedModule {}
