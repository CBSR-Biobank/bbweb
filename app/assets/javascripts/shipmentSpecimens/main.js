/**
 * Centres configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.shipmentSpecimens',
      module;

  module = angular.module(name, [])
    .component('ssSpecimensPagedTable',
               require('./components/ssSpecimensPagedTable/ssSpecimensPagedTableComponent'))
    .component('specimenTableAction',
               require('./components/specimenTableAction/specimenTableActionComponent'));

  return module;
});
