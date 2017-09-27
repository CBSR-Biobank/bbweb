/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const MODULE_NAME = 'shipmentsTableModule';

angular.module(MODULE_NAME, [])
  .constant('SHIPMENT_TYPES',  require('./shipmentTypes'))
  .component('shipmentsTable', require('./shipmentsTableComponent'));

export default MODULE_NAME;
