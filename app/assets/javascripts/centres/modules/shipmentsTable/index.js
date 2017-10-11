/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';
import shipmentsTable from './shipmentsTableComponent';

const ShipmentsTableModule = angular.module('biobank.shipmentsTableModule', [])
      .constant('SHIPMENT_TYPES',  require('./shipmentTypes'))
      .component('shipmentsTable', shipmentsTable)
      .name;

export default ShipmentsTableModule;
