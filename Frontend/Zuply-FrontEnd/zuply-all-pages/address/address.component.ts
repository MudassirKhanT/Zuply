import { Component, OnInit } from '@angular/core';
import { SavedAddress } from '../../core/models';

@Component({
  selector: 'app-address',
  templateUrl: './address.component.html',
  styleUrls: ['./address.component.scss']
})
export class AddressComponent implements OnInit {

  addresses: SavedAddress[] = [];
  showForm = false;
  editingId: string | null = null;
  successMsg = '';
  formError  = '';

  form: Omit<SavedAddress, 'id' | 'isDefault'> = {
    label: 'Home',
    address: '',
    city: '',
    pincode: '',
    phone: ''
  };

  // touched flags
  addressTouched = false;
  cityTouched    = false;
  pincodeTouched = false;
  phoneTouched   = false;

  labels: Array<'Home' | 'Work' | 'Other'> = ['Home', 'Work', 'Other'];

  private readonly STORAGE_KEY = 'zuply_addresses';

  // ── Validators ────────────────────────────────────────────────
  get isPincodeValid(): boolean {
    return /^\d{6}$/.test(this.form.pincode.trim());
  }

  get isPhoneValid(): boolean {
    return /^[6-9]\d{9}$/.test(this.form.phone.trim());
  }

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    try {
      this.addresses = JSON.parse(localStorage.getItem(this.STORAGE_KEY) || '[]');
    } catch {
      this.addresses = [];
    }
  }

  private save(): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(this.addresses));
  }

  openAdd(): void {
    this.editingId = null;
    this.form = { label: 'Home', address: '', city: '', pincode: '', phone: '' };
    this.resetTouched();
    this.formError = '';
    this.showForm = true;
  }

  openEdit(addr: SavedAddress): void {
    this.editingId = addr.id;
    this.form = { label: addr.label, address: addr.address, city: addr.city, pincode: addr.pincode, phone: addr.phone };
    this.resetTouched();
    this.formError = '';
    this.showForm = true;
  }

  private resetTouched(): void {
    this.addressTouched = this.cityTouched = this.pincodeTouched = this.phoneTouched = false;
  }

  submitForm(): void {
    this.addressTouched = this.cityTouched = this.pincodeTouched = this.phoneTouched = true;

    if (!this.form.address.trim()) {
      this.formError = 'Please enter the full address.'; return;
    }
    if (!this.form.city.trim()) {
      this.formError = 'Please enter the city.'; return;
    }
    if (!this.isPincodeValid) {
      this.formError = 'Pincode must be a valid 6-digit number.'; return;
    }
    if (!this.isPhoneValid) {
      this.formError = 'Phone must be a valid 10-digit Indian mobile number (starts with 6–9).'; return;
    }

    this.formError = '';

    if (this.editingId) {
      const idx = this.addresses.findIndex(a => a.id === this.editingId);
      if (idx > -1) {
        this.addresses[idx] = { ...this.addresses[idx], ...this.form };
      }
    } else {
      const newAddr: SavedAddress = {
        id: Date.now().toString(),
        ...this.form,
        isDefault: this.addresses.length === 0
      };
      this.addresses.push(newAddr);
    }

    this.save();
    const wasEditing = !!this.editingId;
    this.showForm = false;
    this.editingId = null;
    this.flash(wasEditing ? 'Address updated!' : 'Address saved!');
  }

  setDefault(id: string): void {
    this.addresses.forEach(a => a.isDefault = (a.id === id));
    this.save();
    this.flash('Default address updated!');
  }

  remove(id: string): void {
    this.addresses = this.addresses.filter(a => a.id !== id);
    if (this.addresses.length > 0 && !this.addresses.some(a => a.isDefault)) {
      this.addresses[0].isDefault = true;
    }
    this.save();
  }

  cancel(): void {
    this.showForm = false;
    this.editingId = null;
    this.formError = '';
  }

  private flash(msg: string): void {
    this.successMsg = msg;
    setTimeout(() => this.successMsg = '', 3000);
  }

  labelIcon(label: string): string {
    return label === 'Home' ? '🏠' : label === 'Work' ? '🏢' : '📍';
  }
}
