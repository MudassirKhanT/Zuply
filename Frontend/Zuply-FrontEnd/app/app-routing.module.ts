import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from '../core/guards/auth.guard';
import { RoleGuard }  from '../core/guards/role.guard';

// Landing
import { LandingComponent } from '../zuply-all-pages/landing/landing.component';

// Auth
import { LoginComponent }    from '../zuply-all-pages/auth/login/login.component';
import { RegisterComponent } from '../zuply-all-pages/auth/register/register.component';

// Profile
import { ProfileComponent } from '../zuply-all-pages/profile/profile.component';

// Products — public, no guard
import { ListComponent    as ProductListComponent   } from '../zuply-all-pages/products/list/list.component';
import { DetailComponent  as ProductDetailComponent } from '../zuply-all-pages/products/detail/detail.component';

// Cart / Wishlist / Checkout
import { CartComponent }     from '../zuply-all-pages/cart/cart.component';
import { WishlistComponent } from '../zuply-all-pages/wishlist/wishlist.component';
import { CheckoutComponent } from '../zuply-all-pages/checkout/checkout.component';

// Orders
import { ListComponent   as OrderListComponent   } from '../zuply-all-pages/orders/list/list.component';
import { DetailComponent as OrderDetailComponent } from '../zuply-all-pages/orders/detail/detail.component';

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

const routes: Routes = [
  // Landing page — public home
  { path: '', component: LandingComponent, pathMatch: 'full' },

  // Auth
  { path: 'login',    component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Products — fully public, no login required
  { path: 'products',     component: ProductListComponent },
  { path: 'products/:id', component: ProductDetailComponent },

  // Customer (login required)
  { path: 'profile',    component: ProfileComponent,       canActivate: [AuthGuard] },
  { path: 'cart',       component: CartComponent,          canActivate: [AuthGuard] },
  { path: 'wishlist',   component: WishlistComponent,      canActivate: [AuthGuard] },
  { path: 'checkout',   component: CheckoutComponent,      canActivate: [AuthGuard] },  // ← login required
  { path: 'orders',     component: OrderListComponent,     canActivate: [AuthGuard] },
  { path: 'orders/:id', component: OrderDetailComponent,   canActivate: [AuthGuard] },

  // Seller (login + SELLER role required)
  { path: 'seller/dashboard',                component: SellerDashboardComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' } },
  { path: 'seller/upload',                   component: UploadComponent,          canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' } },
  { path: 'seller/listing-preview/:imageId', component: ListingPreviewComponent,  canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' } },
  { path: 'seller/orders',                   component: SellerOrdersComponent,    canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' } },
  { path: 'seller/products',                 component: SellerProductsComponent,  canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' } },

  // Admin (login + ADMIN role required)
  { path: 'admin/dashboard', component: AdminDashboardComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/sellers',   component: AdminSellersComponent,   canActivate: [AuthGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/products',  component: AdminProductsComponent,  canActivate: [AuthGuard, RoleGuard], data: { role: 'ADMIN' } },
  { path: 'admin/reports',   component: AdminReportsComponent,   canActivate: [AuthGuard, RoleGuard], data: { role: 'ADMIN' } },

  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
