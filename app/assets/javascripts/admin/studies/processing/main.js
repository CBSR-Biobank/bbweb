/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.directives.studies.processing',
      module;

  module = angular.module(name, [ 'biobank.users' ]);

  module.directive('processingTypesPanel',
                   require('./processingTypesPanel/processingTypesPanelDirective'));
  module.directive('spcLinkTypesPanel',
                   require('./spcLinkTypesPanel/spcLinkTypesPanelDirective'));

  return {
    name: name,
    module: module
  };
});
