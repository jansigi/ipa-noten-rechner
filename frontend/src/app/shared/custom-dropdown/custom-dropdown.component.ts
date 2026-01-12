import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  ViewChild,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkConnectedOverlay, CdkOverlayOrigin, OverlayModule } from '@angular/cdk/overlay';
import { MatIconModule } from '@angular/material/icon';

export interface DropdownOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-custom-dropdown',
  standalone: true,
  imports: [CommonModule, OverlayModule, CdkConnectedOverlay, CdkOverlayOrigin, MatIconModule],
  templateUrl: './custom-dropdown.component.html',
  styleUrls: ['./custom-dropdown.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CustomDropdownComponent {
  @Input() label?: string;
  @Input() placeholder = 'Bitte auswählen';
  @Input() options: DropdownOption[] = [];
  @Input() selected?: string;
  @Output() readonly selectionChange = new EventEmitter<string>();

  readonly labelId = `dropdown-label-${Math.random().toString(36).slice(2, 9)}`;
  readonly triggerId = `dropdown-trigger-${Math.random().toString(36).slice(2, 9)}`;

  @ViewChild(CdkOverlayOrigin, { static: true }) origin!: CdkOverlayOrigin;
  @ViewChild('listbox') listboxRef?: ElementRef<HTMLDivElement>;

  open = signal(false);
  activeIndex = signal<number>(-1);

  get selectedOption(): DropdownOption | undefined {
    return this.options.find((o) => o.value === this.selected);
  }

  toggle(): void {
    if (this.open()) {
      this.close();
    } else {
      this.openMenu();
    }
  }

  openMenu(): void {
    this.open.set(true);
    const idx = this.options.findIndex((o) => o.value === this.selected);
    this.activeIndex.set(idx >= 0 ? idx : 0);
    setTimeout(() => this.scrollActiveIntoView(), 0);
  }

  close(): void {
    this.open.set(false);
    this.activeIndex.set(-1);
  }

  select(option: DropdownOption): void {
    this.selectionChange.emit(option.value);
    this.close();
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      if (!this.open()) {
        this.openMenu();
        return;
      }
      this.moveActive(1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      if (!this.open()) {
        this.openMenu();
        return;
      }
      this.moveActive(-1);
    } else if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      if (!this.open()) {
        this.openMenu();
      } else if (this.activeIndex() >= 0) {
        const option = this.options[this.activeIndex()];
        this.select(option);
      }
    } else if (event.key === 'Escape') {
      if (this.open()) {
        event.preventDefault();
        this.close();
      }
    }
  }

  private moveActive(delta: number): void {
    if (!this.options.length) return;
    let next = this.activeIndex();
    if (next < 0) next = 0;
    next = (next + delta + this.options.length) % this.options.length;
    this.activeIndex.set(next);
    this.scrollActiveIntoView();
  }

  private scrollActiveIntoView(): void {
    const list = this.listboxRef?.nativeElement;
    if (!list) return;
    const idx = this.activeIndex();
    if (idx < 0) return;
    const item = list.querySelector<HTMLElement>(`[data-option-idx="${idx}"]`);
    item?.scrollIntoView({ block: 'nearest' });
  }

  @HostListener('document:keydown.escape', ['$event'])
  onEscape(event: Event): void {
    if (this.open()) {
      event.stopPropagation();
      this.close();
    }
  }
}
