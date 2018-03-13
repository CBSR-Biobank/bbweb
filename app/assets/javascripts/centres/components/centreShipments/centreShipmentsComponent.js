/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.centreShipments
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { TabbedPageController } from '../../../common/controllers/TabbedPageController';


/*
 * Controller for this component.
 */
class CentreShipmentsController extends TabbedPageController {

  constructor($controller,
              $scope,
              $state,
              Shipment,
              ShipmentState,
              gettextCatalog,
              breadcrumbService) {
    'ngInject';
    super(
      [
        {
          heading: gettextCatalog.getString('Incoming'),
          sref: 'home.shipping.centre.incoming',
          active: true
        },
        {
          heading: gettextCatalog.getString('Outgoing'),
          sref: 'home.shipping.centre.outgoing',
          active: false
        },
        {
          heading: gettextCatalog.getString('Completed'),
          sref: 'home.shipping.centre.completed',
          active: false
        }
      ],
      0,
      $scope,
      $state);
    Object.assign(this,
                  {
                    gettextCatalog,
                    breadcrumbService
                  });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.shipping'),
      this.breadcrumbService.forStateWithFunc('home.shipping.centre', () => this.centre.name)
    ];
  }

}

/**
 * An AngularJS component that displays {@link domain.centres.Shipment Shipments} destined to or from a
 * {@link domain.centres.Centre Centre}.
 *
 * @memberOf centres.components.centreShipments
 *
 * @param {domain.centres.Centre} centre - the centre to display shipments for.
 */
const centreShipmentsComponent = {
  template: require('./centreShipments.html'),
  controller: CentreShipmentsController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('centreShipments', centreShipmentsComponent)
