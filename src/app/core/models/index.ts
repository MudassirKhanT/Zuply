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
  categoryId?: number;
  category?: string;
  sellerId?: number;
  sellerName?: string;
  imageUrl?: string;
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

export interface CheckoutRequest {
  deliveryAddress: string;
  city: string;
  pincode: string;
  phone: string;
  paymentMethod: 'UPI' | 'CARD' | 'COD';
}

export interface Order {
  id: number;
  customerId?: number;
  orderDate: string;
  status: 'PLACED' | 'PROCESSING' | 'DELIVERED';
  grandTotal: number;
  deliveryAddress?: string;
  paymentMethod?: string;
  items?: OrderItem[];
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  priceAtOrder: number;
}

// ── Sprint 2 AI Listing models ────────────────────────
export interface UploadResponse {
  imageId: number;
  originalUrl: string;
  status: ImageStatus;
  message: string;
}

export type ImageStatus =
  'PENDING' | 'PROCESSING' | 'PROCESSED' | 'COMPLETED' | 'FAILED';

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
