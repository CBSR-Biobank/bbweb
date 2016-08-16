/**
 * Centres configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.centres',
      module;

  module = angular.module(name, []);

  module.config(require('./states'));

  module.constant('shipmentProgressItems', [ 'Shipping information', 'Items to ship', 'Ready to send' ]);

  module.component('centreShipments',      require('./components/centreShipments/centreShipmentsComponent'));
  module.component('shippingHome',         require('./components/shippingHome/shippingHomeComponent'));
  module.component('selectCentre',         require('./components/selectCentre/selectCentreComponent'));
  module.component('shipmentAdd',          require('./components/shipmentAdd/shipmentAddComponent'));
  module.component('shippingInfoView',     require('./components/shippingInfoView/shippingInfoViewComponent'));
  module.component('shipmentAddItems',     require('./components/shipmentAddItems/shipmentAddItemsComponent'));
  module.component('shipmentView',         require('./components/shipmentView/shipmentViewComponent'));
  module.component('shipmentViewPacked',   require('./components/shipmentViewPacked/shipmentViewPackedComponent'));
  module.component('shipmentViewSent',     require('./components/shipmentViewSent/shipmentViewSentComponent'));
  module.component('shipmentsTable',       require('./components/shipmentsTable/shipmentsTableComponent'));

  module.component('shipmentSpecimensView',
                   require('./components/shipmentSpecimensView/shipmentSpecimensViewComponent'));

  module.service('centreStatusLabel', require('./services/centreStatusLabelService'));

  return module;
});
