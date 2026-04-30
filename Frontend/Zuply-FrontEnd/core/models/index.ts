// ============================================================
// Zuply — Core Models (aligned with backend DTOs)
// ============================================================

// ── Generic API wrapper ──────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

// ── Auth ─────────────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
  role?: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
  role: 'CUSTOMER' | 'SELLER' | 'ADMIN';
  storeName?: string;
}

export interface LoginResponse {
  token: string;
  role: string;
  name: string;
  email: string;
}

export interface CurrentUser {
  token: string;
  role: string;
  name: string;
  email: string;
}

// ── User Profile ─────────────────────────────────────────────
export interface UserProfile {
  name: string;
  email: string;
  phone: string;
  address: string;
  pincode: string;
}

// ── Product ──────────────────────────────────────────────────
export interface Product {
  id: number;
  name: string;
  categoryName: string;
  price: number;
  stock: number;
  variations?: string;
  deliveryMethod?: string;
  returnPolicy?: string;
  imageUrl?: string;
  sellerId?: number;
  sellerName?: string;
  sellerPincode?: string;
  status?: string;
  // extended / optional
  description?: string;
  aiGenerated?: boolean;
  distance?: string;
  category?: string;    // alias populated from categoryName
  pincode?: string;
}

// ── Category ─────────────────────────────────────────────────
export interface Category {
  id: number;
  name: string;
}

// ── Cart ─────────────────────────────────────────────────────
export interface CartItem {
  itemId: number;
  productId: number;
  productName: string;
  quantity: number;
  pricePerUnit: number;
  totalPrice: number;
}

export interface CartResponse {
  cartId: number;
  items: CartItem[];
  grandTotal: number;
}

// ── Order ────────────────────────────────────────────────────
export interface OrderItem {
  productId: number;
  productName: string;
  productImage?: string;
  quantity: number;
  price: number;
  lineTotal: number;
}

// ── Saved Address (localStorage) ─────────────────────────────
export interface SavedAddress {
  id: string;
  label: 'Home' | 'Work' | 'Other';
  customerName: string;
  phone: string;
  address: string;
  city: string;
  pincode: string;
  isDefault: boolean;
}

// ── Seller summary (for local sellers section) ────────────────
export interface SellerSummary {
  sellerId: number;
  sellerName: string;
  pincode: string;
  productCount: number;
  categories: string[];
  sampleImage?: string;
}

export interface Order {
  orderId: number;
  status: string;
  totalAmount: number;
  deliveryAddress: string;
  city?: string;
  pincode?: string;
  paymentMethod: string;
  createdAt: string;
  items: OrderItem[];
}

export interface CheckoutPayload {
  customerId?: number;
  deliveryAddress: DeliveryAddress;
  paymentMethod: string;
  items?: CheckoutItem[];
}

export interface DeliveryAddress {
  customerName: string;
  phone: string;
  address: string;
  city: string;
  pincode: string;
}

export interface CheckoutItem {
  productId: number;
  quantity: number;
  price: number;
}

// ── Wishlist ─────────────────────────────────────────────────
export interface WishlistItem {
  wishlistId?: number;
  productId: number;
  productName: string;
  price: number;
  sellerName?: string;
  imageUrl?: string;
  // raw wishlist entity fields (backend returns Wishlist entity)
  id?: number;
  product?: Product;
}

// ── Seller ───────────────────────────────────────────────────
export interface SellerDashboard {
  totalProductsUploaded: number;
  totalOrdersReceived: number;
  pendingOrders: number;
}

export interface SellerOrder {
  orderId: number;
  customerName: string;
  productName: string;
  quantity: number;
  orderStatus: string;
}

// ── Admin ────────────────────────────────────────────────────
export interface AdminStats {
  totalSellers: number;
  totalProducts: number;
  totalOrders: number;
}

export interface AdminReport {
  totalSales: number;
  totalSellers: number;
  totalCustomers: number;
  productsByCategory: { [key: string]: number };
}

// ── Listing (AI) ─────────────────────────────────────────────
export type ImageStatus = 'PENDING' | 'PROCESSING' | 'PROCESSED' | 'COMPLETED';

export interface UploadResponse {
  imageId: number;
  status: ImageStatus;
  imageUrl?: string;
}

export interface ListingResponse {
  productId: number;
  title: string;
  description: string;
  category: string;
  price?: number;
  status: string;
  originalImageUrl?: string;
  processedImageUrl?: string;
  tags?: string[];
  highlights?: string[];
  suggestedPriceMin?: string;
  suggestedPriceMax?: string;
  aiSuggestedCategory?: boolean;
}

export interface ListingEditRequest {
  title?: string;
  description?: string;
  price?: number;
  category?: string;
  tags?: string[];
  highlights?: string[];
}
