/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.studies.ceventTypes',
      module;

  module = angular.module(name, [
    'biobank.users'
  ]);

  module.config(require('./states'));

  module.controller('CeventTypeEditCtrl', require('./CeventTypeEditCtrl'));
  module.directive('ceventTypesPanel', require('./directives/ceventTypesPanel/ceventTypesPanelDirective'));
  module.directive('ceventTypesAddAndSelect', require('./directives/ceventTypesAddAndSelect/ceventTypesAddAndSelectDirective'));
  module.directive('ceventTypeView', require('./directives/ceventTypeView/ceventTypeViewDirective'));

  return {
    name: name,
    module: module
  };
});
