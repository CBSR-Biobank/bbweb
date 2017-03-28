/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular');

  return angular.module('shipmentsTableModule', [])
    .constant('SHIPMENT_TYPES',  require('./shipmentTypes'))
    .component('shipmentsTable', require('./shipmentsTableComponent'));
});
