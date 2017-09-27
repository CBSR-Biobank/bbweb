/**
 * Centres configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const ShipmentSpecimensModule = angular.module('biobank.shipmentSpecimens', [])
  .component('ssSpecimensPagedTable',
             require('./components/ssSpecimensPagedTable/ssSpecimensPagedTableComponent'))
  .component('specimenTableAction',
             require('./components/specimenTableAction/specimenTableActionComponent'))
  .name;

export default ShipmentSpecimensModule;
