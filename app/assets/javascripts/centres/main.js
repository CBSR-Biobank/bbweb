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

  module.component('shippingHome',         require('./components/shippingHome/shippingHomeComponent'));
  module.component('selectCentre',         require('./components/selectCentre/selectCentreComponent'));
  module.component('shipmentAdd',          require('./components/shipmentAdd/shipmentAddComponent'));
  module.component('shipmentAddSpecimens', require('./components/shipmentAddSpecimens/shipmentAddSpecimensComponent'));
  module.component('shipmentsTable',       require('./components/shipmentsTable/shipmentsTableComponent'));

  module.service('centreStatusLabel', require('./services/centreStatusLabelService'));

  return module;
});
