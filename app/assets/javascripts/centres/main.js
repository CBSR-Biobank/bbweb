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

  module = angular.module(name, [])
    .config(require('./states'))
    .constant('shipmentSendProgressItems', [
      'Shipping information',
      'Items to ship',
      'Packed'
    ])
    .constant('shipmentReceiveProgressItems', [
      'Sent',
      'Receive',
      'Unpack',
      'Items Received'
    ])

    .controller('ShipmentSpecimenController', require('./controllers/ShipmentSpecimensController'))

    .component('centreShipments',      require('./components/centreShipments/centreShipmentsComponent'))
    .component('shippingHome',         require('./components/shippingHome/shippingHomeComponent'))
    .component('selectCentre',         require('./components/selectCentre/selectCentreComponent'))
    .component('shipmentAdd',          require('./components/shipmentAdd/shipmentAddComponent'))
    .component('shippingInfoView',     require('./components/shippingInfoView/shippingInfoViewComponent'))
    .component('shipmentAddItems',     require('./components/shipmentAddItems/shipmentAddItemsComponent'))
    .component('shipmentView',         require('./components/shipmentView/shipmentViewComponent'))
    .component('shipmentViewPacked',   require('./components/shipmentViewPacked/shipmentViewPackedComponent'))
    .component('shipmentViewSent',     require('./components/shipmentViewSent/shipmentViewSentComponent'))
    .component(
      'shipmentViewReceived',
      require('./components/shipmentViewReceived/shipmentViewReceivedComponent'))
    .component('shipmentUnpack',       require('./components/shipmentUnpack/shipmentUnpackComponent'))
    .component('shipmentsTable',       require('./components/shipmentsTable/shipmentsTableComponent'))
    .component(
      'shipmentSpecimensAdd',
      require('./components/shipmentSpecimensAdd/shipmentSpecimensAddComponent'))
    .component(
      'shipmentSpecimensView',
      require('./components/shipmentSpecimensView/shipmentSpecimensViewComponent'))
    .component(
      'shipmentSpecimensPanel',
      require('./components/shipmentSpecimensPanel/shipmentSpecimensPanelComponent'))
    .component(
      'unpackedShipmentView',
      require('./components/unpackedShipmentView/unpackedShipmentViewComponent'))
    .component(
      'unpackedShipmentInfo',
      require('./components/unpackedShipmentInfo/unpackedShipmentInfoComponent'))
    .component(
      'unpackedShipmentReceive',
      require('./components/unpackedShipmentReceive/unpackedShipmentReceiveComponent'))
    .component(
      'unpackedShipmentUnpacked',
      require('./components/unpackedShipmentUnpacked/unpackedShipmentUnpackedComponent'))

    .service(
      'centreLocationsModalService',
      require('./services/centreLocationsModal/centreLocationsModalService'))
    .service(
      'shipmentSkipToSentModalService',
      require('./services/shipmentSkipToSentModal/shipmentSkipToSentModalService'))
    .service(
      'shipmentSkipToUnpackedModalService',
      require('./services/shipmentSkipToUnpackedModal/shipmentSkipToUnpackedModalService'));

  return module;
});
