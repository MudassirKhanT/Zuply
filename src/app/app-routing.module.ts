// src/app/app-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

// Customer pages
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

// Seller pages
import { DashboardComponent as SellerDashboardComponent } from './pages/seller/dashboard/dashboard.component';
import { UploadComponent } from './pages/seller/upload/upload.component';
import { ProductsComponent as SellerProductsComponent } from './pages/seller/products/products.component';
import { OrdersComponent as SellerOrdersComponent } from './pages/seller/orders/orders.component';
import { ListingPreviewComponent } from './pages/seller/listing-preview/listing-preview.component';

// Admin pages
import { DashboardComponent as AdminDashboardComponent } from './pages/admin/dashboard/dashboard.component';
import { SellersComponent } from './pages/admin/sellers/sellers.component';
import { ProductsComponent as AdminProductsComponent } from './pages/admin/products/products.component';
import { ReportsComponent } from './pages/admin/reports/reports.component';

const routes: Routes = [

  // ── Public ────────────────────────────────────────────
  { path: '',          component: HomeComponent },
  { path: 'login',     component: LoginComponent },
  { path: 'register',  component: RegisterComponent },
  { path: 'products',  component: ProductListComponent },
  { path: 'products/:id', component: ProductDetailComponent },

  // ── Customer ──────────────────────────────────────────
  { path: 'cart',      component: CartComponent,     canActivate: [AuthGuard], data: { role: 'CUSTOMER' } },
  { path: 'wishlist',  component: WishlistComponent, canActivate: [AuthGuard], data: { role: 'CUSTOMER' } },
  { path: 'checkout',  component: CheckoutComponent, canActivate: [AuthGuard], data: { role: 'CUSTOMER' } },
  { path: 'orders',    component: OrderListComponent,  canActivate: [AuthGuard], data: { role: 'CUSTOMER' } },
  { path: 'orders/:id', component: OrderDetailComponent, canActivate: [AuthGuard], data: { role: 'CUSTOMER' } },
  { path: 'profile',   component: ProfileComponent,  canActivate: [AuthGuard] },

  // ── Seller ────────────────────────────────────────────
  { path: 'seller/dashboard',       component: SellerDashboardComponent, canActivate: [AuthGuard], data: { role: 'SELLER' } },
  { path: 'seller/upload',          component: UploadComponent,          canActivate: [AuthGuard], data: { role: 'SELLER' } },
  { path: 'seller/products',        component: SellerProductsComponent,  canActivate: [AuthGuard], data: { role: 'SELLER' } },
  { path: 'seller/orders',          component: SellerOrdersComponent,    canActivate: [AuthGuard], data: { role: 'SELLER' } },
  { path: 'seller/listing-preview/:imageId', component: ListingPreviewComponent, canActivate: [AuthGuard], data: { role: 'SELLER' } },

  // ── Admin ─────────────────────────────────────────────
  { path: 'admin/dashboard', component: AdminDashboardComponent, canActivate: [AuthGuard], data: { role: 'ADMIN' } },
  { path: 'admin/sellers',   component: SellersComponent,        canActivate: [AuthGuard], data: { role: 'ADMIN' } },
  { path: 'admin/products',  component: AdminProductsComponent,  canActivate: [AuthGuard], data: { role: 'ADMIN' } },
  { path: 'admin/reports',   component: ReportsComponent,        canActivate: [AuthGuard], data: { role: 'ADMIN' } },

  // ── Fallback ──────────────────────────────────────────
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'top' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
