import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { SavedAddress } from '../../core/models';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {

  activeTab: 'profile' | 'addresses' = 'profile';

  // ── User info ─────────────────────────────────────────
  user: { name: string; email: string; phone?: string } = { name: '', email: '' };
  pfp = '';

  // ── Addresses ─────────────────────────────────────────
  addresses: SavedAddress[] = [];
  showAddressForm = false;
  editingAddressId: string | null = null;

  addressForm: Omit<SavedAddress, 'id' | 'isDefault'> = {
    label: 'Home',
    customerName: '',
    phone: '',
    address: '',
    city: '',
    pincode: ''
  };

  labelOptions: ('Home' | 'Work' | 'Other')[] = ['Home', 'Work', 'Other'];
  saveSuccess = false;

  constructor(
    private auth: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const u = this.auth.getCurrentUser();
    if (u) this.user = { name: u.name, email: u.email, phone: u.phone };

    const stored = localStorage.getItem('zuply_pfp');
    if (stored) this.pfp = stored;

    this.loadAddresses();

    // Check query param ?tab=addresses
    this.route.queryParams.subscribe(params => {
      if (params['tab'] === 'addresses') this.activeTab = 'addresses';
    });
  }

  // ── PFP ──────────────────────────────────────────────
  onPfpChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      this.pfp = reader.result as string;
      localStorage.setItem('zuply_pfp', this.pfp);
    };
    reader.readAsDataURL(file);
  }

  removePfp(): void {
    this.pfp = '';
    localStorage.removeItem('zuply_pfp');
  }

  // ── Addresses ─────────────────────────────────────────
  loadAddresses(): void {
    const raw = localStorage.getItem('zuply_addresses');
    this.addresses = raw ? JSON.parse(raw) : [];
  }

  saveAddresses(): void {
    localStorage.setItem('zuply_addresses', JSON.stringify(this.addresses));
  }

  openAddForm(): void {
    this.editingAddressId = null;
    this.addressForm = { label: 'Home', customerName: this.user.name, phone: this.user.phone || '', address: '', city: '', pincode: '' };
    this.showAddressForm = true;
  }

  editAddress(addr: SavedAddress): void {
    this.editingAddressId = addr.id;
    this.addressForm = {
      label: addr.label,
      customerName: addr.customerName,
      phone: addr.phone,
      address: addr.address,
      city: addr.city,
      pincode: addr.pincode
    };
    this.showAddressForm = true;
  }

  saveAddress(): void {
    if (!this.addressForm.customerName || !this.addressForm.phone ||
        !this.addressForm.address || !this.addressForm.city || !this.addressForm.pincode) return;

    if (this.editingAddressId) {
      const idx = this.addresses.findIndex(a => a.id === this.editingAddressId);
      if (idx !== -1) {
        this.addresses[idx] = { ...this.addressForm, id: this.editingAddressId, isDefault: this.addresses[idx].isDefault };
      }
    } else {
      const newAddr: SavedAddress = {
        ...this.addressForm,
        id: Date.now().toString(),
        isDefault: this.addresses.length === 0
      };
      this.addresses.push(newAddr);
    }

    this.saveAddresses();
    this.showAddressForm = false;
    this.editingAddressId = null;
    this.saveSuccess = true;
    setTimeout(() => this.saveSuccess = false, 2500);
  }

  removeAddress(id: string): void {
    this.addresses = this.addresses.filter(a => a.id !== id);
    // If removed was default, make first the new default
    if (this.addresses.length > 0 && !this.addresses.some(a => a.isDefault)) {
      this.addresses[0].isDefault = true;
    }
    this.saveAddresses();
  }

  setDefault(id: string): void {
    this.addresses.forEach(a => a.isDefault = (a.id === id));
    this.saveAddresses();
  }

  cancelForm(): void {
    this.showAddressForm = false;
    this.editingAddressId = null;
  }

  labelIcon(label: string): string {
    const map: Record<string, string> = { Home: '🏠', Work: '💼', Other: '📍' };
    return map[label] || '📍';
  }
}
