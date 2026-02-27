import { Component } from '@angular/core';
type TabKey = 'overview' | 'attributes' | 'users' | 'segments';
@Component({
  selector: 'app-customerdetail.component',
  imports: [],
  templateUrl: './customerdetail.component.html',
  styleUrl: './customerdetail.component.css',
})
export class CustomerdetailComponent {
  activeTab: TabKey = 'overview';

  goBack() {
    history.back();
  }

}
