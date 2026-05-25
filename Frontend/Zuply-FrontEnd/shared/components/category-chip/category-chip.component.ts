import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-category-chip',
  template: `
    <button class="chip" [class.active]="active" (click)="chipClick.emit(name)">
      {{ name }}
    </button>
  `,
  styleUrls: ['./category-chip.component.scss']
})
export class CategoryChipComponent {
  @Input() name = '';
  @Input() active = false;
  @Output() chipClick = new EventEmitter<string>();
}
