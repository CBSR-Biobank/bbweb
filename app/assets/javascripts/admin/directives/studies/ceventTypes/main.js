/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.directives.studies.ceventTypes',
      module;

  module = angular.module(name, [
    'biobank.users'
  ]);

  module.directive('ceventTypesPanel',
                   require('./ceventTypesPanel/ceventTypesPanelDirective'));
  module.directive('ceventTypeAdd',
                   require('./ceventTypeAdd/ceventTypeAddDirective'));
  module.directive('ceventTypesAddAndSelect',
                   require('./ceventTypesAddAndSelect/ceventTypesAddAndSelectDirective'));
  module.directive('ceventTypeView',
                   require('./ceventTypeView/ceventTypeViewDirective'));

  return {
    name: name,
    module: module
  };
});
