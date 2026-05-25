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
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
  role: 'CUSTOMER' | 'SELLER' | 'ADMIN';
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

// ── Product Create (manual upload) ───────────────────────────
export interface ProductCreateRequest {
  name: string;
  description?: string;
  categoryId: number;
  price: number;
  stock: number;
  imageUrl?: string;
  extraImages?: string[];
  variations?: string;
  deliveryMethod?: string;
  returnPolicy?: string;
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
  extraImages?: string[];
  sellerId?: number;
  sellerName?: string;
  sellerPincode?: string;
  pincode?: string;
  status?: string;
  description?: string;
  aiGenerated?: boolean;
  distance?: string;
  category?: string;
  averageRating?: number;
  reviewCount?: number;
}

// ── Seller Summary (aggregated from products) ────────────────
export interface SellerSummary {
  sellerId: number;
  sellerName: string;
  pincode: string;
  productCount: number;
  categories: string[];
  sampleImage?: string;
}

// ── Review ────────────────────────────────────────────────────
export interface Review {
  id: number;
  customerName: string;   // matches backend ReviewDto field name
  rating: number;
  comment?: string;
  createdAt: string;
}

export interface ReviewsResponse {
  reviews: Review[];
  averageRating: number;
  count: number;
}

export interface ReviewRequest {
  rating: number;
  comment?: string;
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

// ── Saved Address ────────────────────────────────────────────
export interface SavedAddress {
  id: string;
  label: 'Home' | 'Work' | 'Other';
  address: string;
  city: string;
  pincode: string;
  phone: string;
  isDefault: boolean;
}

// ── Order ────────────────────────────────────────────────────
export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  lineTotal: number;
  imageUrl?: string;
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
  totalRevenue: number;
  totalSellers: number;
  totalCustomers: number;
  totalOrders: number;
  categoryBreakdown: { category: string; count: number; percentage: number }[];
}

// ── Listing (AI) ─────────────────────────────────────────────
export type ImageStatus = 'PENDING' | 'PROCESSING' | 'PROCESSED' | 'COMPLETED';

export interface UploadResponse {
  imageId: number;
  originalUrl?: string;
  imageUrl?: string;
  status: ImageStatus;
  message?: string;
}

export interface ListingResponse {
  productId: number;
  title: string;
  description: string;
  category: string;
  price?: number;
  stock?: number;
  status: string;
  originalImageUrl?: string;
  processedImageUrl?: string;
  tags?: string[];
  highlights?: string[];
  extraImages?: string[];
  suggestedPriceMin?: string;
  suggestedPriceMax?: string;
  aiSuggestedCategory?: boolean;
  deliveryMethod?: string;
  returnPolicy?: string;
}

export interface ListingEditRequest {
  title?: string;
  description?: string;
  price?: number;
  stock?: number;
  category?: string;
  deliveryMethod?: string;
  returnPolicy?: string;
}
