/**
 * Centres configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const MODULE_NAME = 'biobank.shipmentSpecimens';

angular.module(MODULE_NAME, [])
  .component('ssSpecimensPagedTable',
             require('./components/ssSpecimensPagedTable/ssSpecimensPagedTableComponent'))
  .component('specimenTableAction',
             require('./components/specimenTableAction/specimenTableActionComponent'));

export default MODULE_NAME;
