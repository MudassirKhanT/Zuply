export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface User {
  id?: number;
  name: string;
  email: string;
  phone?: string;
  role: 'CUSTOMER' | 'SELLER' | 'ADMIN';
  city?: string;
  pincode?: string;
}

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
  id: number;
  token: string;
  role: string;
  name: string;
  email: string;
}

export interface Category {
  id: number;
  name: string;
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  categoryName?: string;
  category?: string;
  sellerId?: number;
  sellerName?: string;
  sellerPincode?: string;
  imageUrl?: string;
  variations?: string;
  deliveryMethod?: string;
  returnPolicy?: string;
  pincode?: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
  distance?: string;
  aiGenerated?: boolean;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImage?: string;
  price: number;
  quantity: number;
  lineTotal: number;
}

export interface CartResponse {
  items: CartItem[];
  grandTotal: number;
}

export interface WishlistItem {
  id: number;
  productId: number;
  productName: string;
  productImage?: string;
  price: number;
  sellerName?: string;
}

export interface DeliveryAddress {
  customerName: string;
  phone: string;
  address: string;
  city: string;
  pincode: string;
}

export interface CheckoutRequest {
  customerId?: number;
  deliveryAddress: DeliveryAddress;
  paymentMethod: 'UPI' | 'CARD' | 'COD';
  items?: CheckoutItem[];
}

export interface CheckoutItem {
  productId: number;
  quantity: number;
  price: number;
}

export interface Order {
  orderId: number;
  createdAt: string;
  status: 'PLACED' | 'PROCESSING' | 'DELIVERED';
  totalAmount: number;
  paymentMethod?: string;
  deliveryAddress?: string;
  city?: string;
  pincode?: string;
  items?: OrderItem[];
}

export interface OrderItem {
  productId: number;
  productName: string;
  productImage?: string;
  quantity: number;
  price: number;
  lineTotal: number;
}

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

export interface Review {
  id: number;
  customerName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface ReviewsResponse {
  reviews: Review[];
  averageRating: number;
  count: number;
}

export interface ReviewRequest {
  rating: number;
  comment: string;
}

export interface PaymentOrderRequest {
  amount: number;
  orderId: number;
}

export interface PaymentOrderResponse {
  razorpayOrderId: string;
  amount: number;
  currency: string;
  keyId: string;
}

// ── Sprint 2 AI Listing models ────────────────────────
export interface UploadResponse {
  imageId: number;
  originalUrl: string;
  status: ImageStatus;
  message: string;
}

export type ImageStatus = 'PENDING' | 'PROCESSING' | 'PROCESSED' | 'COMPLETED' | 'FAILED';

export interface ListingResponse {
  productId: number;
  imageId: number;
  originalImageUrl?: string;
  processedImageUrl?: string;
  title: string;
  description: string;
  category: string;
  aiSuggestedCategory: boolean;
  color?: string;
  material?: string;
  productType?: string;
  price?: number;
  suggestedPriceMin?: string;
  suggestedPriceMax?: string;
  tags: string[];
  highlights: string[];
  status: 'DRAFT' | 'PUBLISHED';
}

export interface ListingEditRequest {
  title?: string;
  description?: string;
  category?: string;
  price?: number;
  color?: string;
  material?: string;
  productType?: string;
  tags?: string[];
  highlights?: string[];
}

export interface PublishResponse {
  productId: number;
  status: string;
  title: string;
  message: string;
}

// ── Admin models ──────────────────────────────────────
export interface AdminStats {
  totalSellers: number;
  totalProducts: number;
  totalOrders: number;
}

export interface SellerRecord {
  id: number;
  name: string;
  storeName?: string;
  contact?: string;
  verificationStatus: string;
}

export interface SellerAdminDto {
  id: number;
  name: string;
  email: string;
  phone?: string;
  storeName?: string;
  location?: string;
  pincode?: string;
  verificationStatus: string;
  active: boolean;
}

export interface AdminProductUpdateRequest {
  title?: string;
  description?: string;
  category?: string;
  price?: number;
  status?: 'DRAFT' | 'PUBLISHED';
}

export interface SellerSummary {
  sellerId: number;
  sellerName: string;
  pincode: string;
  productCount: number;
  categories: string[];
  sampleImage?: string;
}
