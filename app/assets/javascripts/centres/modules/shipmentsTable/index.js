/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const ShipmentsTableModule = angular.module('biobank.shipmentsTableModule', [])
      .constant('SHIPMENT_TYPES',  require('./shipmentTypes'))
      .component('shipmentsTable', require('./shipmentsTableComponent'))
      .name;

export default ShipmentsTableModule;
