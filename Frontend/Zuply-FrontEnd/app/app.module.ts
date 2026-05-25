import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent }     from './app.component';
import { SharedModule }     from '../shared/shared.module';
import { JwtInterceptor }   from '../core/interceptors/jwt.interceptor';

// ── Page Components ──────────────────────────────────────────

// Landing / Home
import { LandingComponent } from '../zuply-all-pages/landing/landing.component';
import { HomeComponent }    from '../zuply-all-pages/home/home.component';

// Auth
import { LoginComponent }    from '../zuply-all-pages/auth/login/login.component';
import { RegisterComponent } from '../zuply-all-pages/auth/register/register.component';

// Profile & Address
import { ProfileComponent } from '../zuply-all-pages/profile/profile.component';
import { AddressComponent } from '../zuply-all-pages/address/address.component';

// Products
import { ListComponent    as ProductListComponent   } from '../zuply-all-pages/products/list/list.component';
import { DetailComponent  as ProductDetailComponent } from '../zuply-all-pages/products/detail/detail.component';

// Cart / Wishlist / Checkout
import { CartComponent }     from '../zuply-all-pages/cart/cart.component';
import { WishlistComponent } from '../zuply-all-pages/wishlist/wishlist.component';
import { CheckoutComponent } from '../zuply-all-pages/checkout/checkout.component';

// Orders
import { ListComponent   as OrderListComponent   } from '../zuply-all-pages/orders/list/list.component';
import { DetailComponent as OrderDetailComponent } from '../zuply-all-pages/orders/detail/detail.component';

// Public Sellers marketplace
import { SellersComponent }      from '../zuply-all-pages/sellers/sellers.component';
import { SellerDetailComponent } from '../zuply-all-pages/sellers/detail/seller-detail.component';

// Info / Misc pages
import { BecomeASellerComponent } from '../zuply-all-pages/become-a-seller/become-a-seller.component';
import { CustomerCareComponent }  from '../zuply-all-pages/customer-care/customer-care.component';
import { AdvertiseComponent }     from '../zuply-all-pages/advertise/advertise.component';

// Seller
import { DashboardComponent      as SellerDashboardComponent } from '../zuply-all-pages/seller/dashboard/dashboard.component';
import { UploadComponent }                                       from '../zuply-all-pages/seller/upload/upload.component';
import { ListingPreviewComponent }                               from '../zuply-all-pages/seller/listing-preview/listing-preview.component';
import { OrdersComponent         as SellerOrdersComponent }     from '../zuply-all-pages/seller/orders/orders.component';
import { ProductsComponent       as SellerProductsComponent }   from '../zuply-all-pages/seller/products/products.component';

// Admin
import { DashboardComponent  as AdminDashboardComponent } from '../zuply-all-pages/admin/dashboard/dashboard.component';
import { SellersComponent    as AdminSellersComponent }   from '../zuply-all-pages/admin/sellers/sellers.component';
import { ProductsComponent   as AdminProductsComponent }  from '../zuply-all-pages/admin/products/products.component';
import { ReportsComponent    as AdminReportsComponent }   from '../zuply-all-pages/admin/reports/reports.component';
import { AdminOrdersComponent }                            from '../zuply-all-pages/admin/orders/orders.component';

@NgModule({
  declarations: [
    AppComponent,

    // Landing / Home
    LandingComponent,
    HomeComponent,

    // Auth
    LoginComponent,
    RegisterComponent,

    // Profile & Address
    ProfileComponent,
    AddressComponent,

    // Products
    ProductListComponent,
    ProductDetailComponent,

    // Cart / Wishlist / Checkout
    CartComponent,
    WishlistComponent,
    CheckoutComponent,

    // Orders
    OrderListComponent,
    OrderDetailComponent,

    // Public Sellers marketplace
    SellersComponent,
    SellerDetailComponent,

    // Info / Misc pages
    BecomeASellerComponent,
    CustomerCareComponent,
    AdvertiseComponent,

    // Seller
    SellerDashboardComponent,
    UploadComponent,
    ListingPreviewComponent,
    SellerOrdersComponent,
    SellerProductsComponent,

    // Admin
    AdminDashboardComponent,
    AdminSellersComponent,
    AdminProductsComponent,
    AdminReportsComponent,
    AdminOrdersComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
    SharedModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
