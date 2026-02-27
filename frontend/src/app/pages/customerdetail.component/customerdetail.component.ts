import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { HeaderComponent } from '../../shell/header.component/header.component';
type TabKey = 'overview' | 'attributes' | 'users' | 'segments';
@Component({
  selector: 'app-customerdetail.component',
  imports: [CommonModule, HeaderComponent],
  templateUrl: './customerdetail.component.html',
  styleUrl: './customerdetail.component.css',
})
export class CustomerdetailComponent {
  activeTab: TabKey = 'overview';

  goBack() {
    history.back();
  }

}
