// src/app/pages/orders/detail/detail.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models';

@Component({ selector:'app-detail', templateUrl:'./detail.component.html', styleUrls:['./detail.component.scss'] })
export class DetailComponent implements OnInit {
  order: Order | null = null;
  loading = true;

  constructor(private route: ActivatedRoute, private orderService: OrderService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.orderService.getOrderById(id).subscribe({
      next: res => { if (res.success) this.order = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  getStatusClass(status: string): string { return `status-${status.toLowerCase()}`; }
}
