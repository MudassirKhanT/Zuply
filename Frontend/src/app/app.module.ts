// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Interceptor
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';

// Shared Components
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { ProductCardComponent } from './shared/components/product-card/product-card.component';
import { CategoryChipComponent } from './shared/components/category-chip/category-chip.component';
import { LoadingSpinnerComponent } from './shared/components/loading-spinner/loading-spinner.component';
import { AiBadgeComponent } from './shared/components/ai-badge/ai-badge.component';

// Pages — Customer
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { RegisterComponent } from './pages/auth/register/register.component';
import { ListComponent as ProductListComponent } from './pages/products/list/list.component';
import { DetailComponent as ProductDetailComponent } from './pages/products/detail/detail.component';
import { CartComponent } from './pages/cart/cart.component';
import { WishlistComponent } from './pages/wishlist/wishlist.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { ListComponent as OrderListComponent } from './pages/orders/list/list.component';
import { DetailComponent as OrderDetailComponent } from './pages/orders/detail/detail.component';
import { ProfileComponent } from './pages/profile/profile.component';

// Pages — Seller
import { DashboardComponent as SellerDashboardComponent } from './pages/seller/dashboard/dashboard.component';
import { UploadComponent } from './pages/seller/upload/upload.component';
import { ProductsComponent as SellerProductsComponent } from './pages/seller/products/products.component';
import { OrdersComponent as SellerOrdersComponent } from './pages/seller/orders/orders.component';
import { ListingPreviewComponent } from './pages/seller/listing-preview/listing-preview.component';

// Pages — Admin
import { DashboardComponent as AdminDashboardComponent } from './pages/admin/dashboard/dashboard.component';
import { SellersComponent } from './pages/admin/sellers/sellers.component';
import { ProductsComponent as AdminProductsComponent } from './pages/admin/products/products.component';
import { ReportsComponent } from './pages/admin/reports/reports.component';

@NgModule({
  declarations: [
    AppComponent,

    // Shared
    NavbarComponent,
    FooterComponent,
    ProductCardComponent,
    CategoryChipComponent,
    LoadingSpinnerComponent,
    AiBadgeComponent,

    // Customer pages
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    ProductListComponent,
    ProductDetailComponent,
    CartComponent,
    WishlistComponent,
    CheckoutComponent,
    OrderListComponent,
    OrderDetailComponent,
    ProfileComponent,

    // Seller pages
    SellerDashboardComponent,
    UploadComponent,
    SellerProductsComponent,
    SellerOrdersComponent,
    ListingPreviewComponent,

    // Admin pages
    AdminDashboardComponent,
    SellersComponent,
    AdminProductsComponent,
    ReportsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
